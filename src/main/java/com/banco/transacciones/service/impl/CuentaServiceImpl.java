package com.banco.transacciones.service.impl;

import com.banco.transacciones.exception.CuentaBloqueadaException;
import com.banco.transacciones.mapper.CuentaMapper;
import com.banco.transacciones.model.dto.response.ResumenCuentaResponse;
import com.banco.transacciones.model.entity.Cuenta;
import com.banco.transacciones.model.entity.Transaccion;
import com.banco.transacciones.repository.CuentaRepository;
import com.banco.transacciones.repository.TransaccionRepository;
import com.banco.transacciones.service.CuentaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CuentaServiceImpl implements CuentaService {

    private final CuentaRepository cuentaRepository;
    private final TransaccionRepository transaccionRepository;
    private final CuentaMapper cuentaMapper;

    @Override
    @Transactional(readOnly = true)
    public ResumenCuentaResponse obtenerResumen(Long id) {
        MDC.put("correlationId", UUID.randomUUID().toString());
        log.info("Obteniendo resumen de cuenta con id: {}", id);

        Cuenta cuenta = cuentaRepository.findById(id)
                .orElseThrow(() -> new CuentaBloqueadaException("Cuenta no encontrada con id: " + id));

        List<Transaccion> transacciones = transaccionRepository
                .findByCuentaOrigen(cuenta.getNumeroCuenta());

        long totalMovimientos = transacciones.size();

        BigDecimal montoPromedio = calcularPromedio(transacciones);
        BigDecimal desviacionEstandar = calcularDesviacionEstandar(transacciones, montoPromedio);

        log.info("Resumen obtenido correctamente para cuenta id: {}", id);

        return new ResumenCuentaResponse(
                cuenta.getNumeroCuenta(),
                cuenta.getSaldo(),
                totalMovimientos,
                montoPromedio,
                desviacionEstandar,
                BigDecimal.ZERO
        		);
    }

    private BigDecimal calcularPromedio(List<Transaccion> transacciones) {
        if (transacciones.isEmpty()) return BigDecimal.ZERO;
        BigDecimal suma = transacciones.stream()
                .map(Transaccion::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return suma.divide(BigDecimal.valueOf(transacciones.size()), MathContext.DECIMAL64);
    }

    private BigDecimal calcularDesviacionEstandar(List<Transaccion> transacciones, BigDecimal promedio) {
        if (transacciones.size() < 2) return BigDecimal.ZERO;
        BigDecimal sumaCuadrados = transacciones.stream()
                .map(t -> t.getMonto().subtract(promedio).pow(2))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal varianza = sumaCuadrados.divide(
                BigDecimal.valueOf(transacciones.size()), MathContext.DECIMAL64);
        return varianza.sqrt(MathContext.DECIMAL64);
    }
}