package com.banco.transacciones.service.impl;

import com.banco.transacciones.exception.CuentaBloqueadaException;
import com.banco.transacciones.exception.SaldoInsuficienteException;
import com.banco.transacciones.exception.TransaccionNotFoundException;
import com.banco.transacciones.mapper.TransaccionMapper;
import com.banco.transacciones.model.dto.request.LoteTransaccionRequest;
import com.banco.transacciones.model.dto.request.TransferenciaRequest;
import com.banco.transacciones.model.dto.response.LoteResultadoResponse;
import com.banco.transacciones.model.dto.response.SeguimientoResponse;
import com.banco.transacciones.model.dto.response.TransaccionResponse;
import com.banco.transacciones.model.entity.Cuenta;
import com.banco.transacciones.model.entity.Transaccion;
import com.banco.transacciones.model.enums.EstadoCuenta;
import com.banco.transacciones.model.enums.EstadoTransaccion;
import com.banco.transacciones.model.enums.TipoTransaccion;
import com.banco.transacciones.repository.CuentaRepository;
import com.banco.transacciones.repository.TransaccionRepository;
import com.banco.transacciones.service.TransaccionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransaccionServiceImpl implements TransaccionService {

    private final CuentaRepository cuentaRepository;
    private final TransaccionRepository transaccionRepository;
    private final TransaccionMapper transaccionMapper;

    private static final int TAMANO_SUBLOTE = 50;
    private static final double UMBRAL_FRAUDE = 0.75;

    @Override
    @Async("transaccionExecutor")
    @Transactional
    public SeguimientoResponse procesarTransferencia(TransferenciaRequest request) {
        MDC.put("correlationId", UUID.randomUUID().toString());
        log.info("Iniciando transferencia de {} a {} por {}€",
                request.getCuentaOrigen(),
                request.getCuentaDestino(),
                request.getMonto());

        // PASO 1 — Buscar cuenta origen con bloqueo pesimista
        Cuenta cuentaOrigen = cuentaRepository
                .findByNumeroCuentaWithLock(request.getCuentaOrigen())
                .orElseThrow(() -> new TransaccionNotFoundException(
                        "Cuenta origen no encontrada: " + request.getCuentaOrigen()));

        // PASO 2 — Comprobar que la cuenta origen está activa
        if (cuentaOrigen.getEstadocuenta() != EstadoCuenta.ACTIVA) {
            throw new CuentaBloqueadaException(
                    "La cuenta origen está bloqueada o cerrada: " + request.getCuentaOrigen());
        }

        // PASO 3 — Comprobar saldo suficiente
        if (cuentaOrigen.getSaldo().compareTo(request.getMonto()) < 0) {
            throw new SaldoInsuficienteException(
                    "La cuenta " + request.getCuentaOrigen() +
                    " no tiene saldo suficiente para la operación");
        }

        // PASO 4 — Comprobar que la cuenta destino existe
        Cuenta cuentaDestino = cuentaRepository
                .findByNumeroCuenta(request.getCuentaDestino())
                .orElseThrow(() -> new TransaccionNotFoundException(
                        "Cuenta destino no encontrada: " + request.getCuentaDestino()));

        // PASO 5 — Crear la transacción en estado PROCESANDO
        Transaccion transaccion = new Transaccion();
        transaccion.setCuentaOrigen(request.getCuentaOrigen());
        transaccion.setCuentaDestino(request.getCuentaDestino());
        transaccion.setMonto(request.getMonto());
        transaccion.setTipo(TipoTransaccion.TRANSFERENCIA);
        transaccion.setEstado(EstadoTransaccion.PROCESANDO);
        transaccion.setDescripcion(request.getDescripcion());
        Transaccion transaccionGuardada = transaccionRepository.save(transaccion);

        log.debug("Transaccion creada con id: {}", transaccionGuardada.getId());

        // PASO 6 — Calcular score de fraude (por ahora un valor fijo, 
        //          luego lo implementará FraudeScoreCalculator)
        double scoreFraude = 0.0;

        // PASO 7 — Si el score supera 0.75 bloquear la transacción
        if (scoreFraude > UMBRAL_FRAUDE) {
            transaccionGuardada.setEstado(EstadoTransaccion.RECHAZADA);
            transaccionRepository.save(transaccionGuardada);
            log.warn("Transaccion {} rechazada por riesgo de fraude. Score: {}",
                    transaccionGuardada.getId(), scoreFraude);
            return new SeguimientoResponse(
                    transaccionGuardada.getId(),
                    EstadoTransaccion.RECHAZADA,
                    "Transaccion rechazada por riesgo de fraude");
        }

        // PASO 8 — Mover el dinero
        cuentaOrigen.setSaldo(cuentaOrigen.getSaldo().subtract(request.getMonto()));
        cuentaDestino.setSaldo(cuentaDestino.getSaldo().add(request.getMonto()));
        cuentaRepository.save(cuentaOrigen);
        cuentaRepository.save(cuentaDestino);

        // PASO 9 — Marcar la transacción como completada
        transaccionGuardada.setEstado(EstadoTransaccion.COMPLETADA);
        transaccionGuardada.setRiesgoFraude(scoreFraude);
        transaccionRepository.save(transaccionGuardada);

        log.info("Transferencia completada. ID: {}", transaccionGuardada.getId());

        return new SeguimientoResponse(
                transaccionGuardada.getId(),
                EstadoTransaccion.COMPLETADA,
                "Transferencia procesada correctamente");
    }

    @Override
    @Transactional(readOnly = true)
    public TransaccionResponse consultarEstado(Long id) {
        MDC.put("correlationId", UUID.randomUUID().toString());
        log.info("Consultando estado de transaccion con id: {}", id);

        Transaccion transaccion = transaccionRepository.findById(id)
                .orElseThrow(() -> new TransaccionNotFoundException(
                        "Transaccion no encontrada con id: " + id));

        log.info("Estado de transaccion {}: {}", id, transaccion.getEstado());
        return transaccionMapper.toResponse(transaccion);
    }

    @Override
    @Transactional
    public LoteResultadoResponse procesarLote(LoteTransaccionRequest request) {
        MDC.put("correlationId", UUID.randomUUID().toString());
        log.info("Iniciando procesamiento de lote con {} transacciones",
                request.getTransacciones().size());

        // Partir el lote en sublotes de 50
        List<List<TransferenciaRequest>> sublotes = partirEnSublotes(
                request.getTransacciones(), TAMANO_SUBLOTE);

        List<TransaccionResponse> resultados = new ArrayList<>();
        int totalProcesadas = 0;
        int totalRechazadas = 0;

        // Procesar cada sublote
        for (List<TransferenciaRequest> sublote : sublotes) {
            log.debug("Procesando sublote de {} transacciones", sublote.size());
            for (TransferenciaRequest transferencia : sublote) {
                try {
                    SeguimientoResponse seguimiento = procesarTransferencia(transferencia);
                    if (seguimiento.estado() == EstadoTransaccion.COMPLETADA) {
                        totalProcesadas++;
                    } else {
                        totalRechazadas++;
                    }
                } catch (Exception e) {
                    log.error("Error procesando transaccion en lote: {}", e.getMessage());
                    totalRechazadas++;
                }
            }
        }

        log.info("Lote procesado. Completadas: {}, Rechazadas: {}",
                totalProcesadas, totalRechazadas);

        return new LoteResultadoResponse(
                request.getTransacciones().size(),
                totalProcesadas,
                totalRechazadas,
                resultados
        );
    }

    private <T> List<List<T>> partirEnSublotes(List<T> lista, int tamano) {
        List<List<T>> sublotes = new ArrayList<>();
        for (int i = 0; i < lista.size(); i += tamano) {
            sublotes.add(lista.subList(i, Math.min(i + tamano, lista.size())));
        }
        return sublotes;
    }
}