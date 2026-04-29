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
import com.banco.transacciones.repository.AlertaFraudeRepository;
import com.banco.transacciones.repository.CuentaRepository;
import com.banco.transacciones.repository.TransaccionRepository;
import com.banco.transacciones.service.TransaccionService;
import com.banco.transacciones.util.FraudeScoreCalculator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransaccionServiceImplTest {

    private static final String CUENTA_ORIGEN = "ES1234567890123456789012";
    private static final String CUENTA_DESTINO = "ES9876543210987654321098";
    private static final String DESCRIPCION = "Test unitario";
    private static final double SCORE_SEGURO = 0.10;
    private static final double SCORE_FRAUDE = 0.80;

    @Mock
    private CuentaRepository cuentaRepository;

    @Mock
    private TransaccionRepository transaccionRepository;

    @Mock
    private TransaccionMapper transaccionMapper;

    @Mock
    private FraudeScoreCalculator fraudeScoreCalculator;

    @Mock
    private AlertaFraudeRepository alertaFraudeRepository;

    @Mock
    private TransaccionService self;

    @InjectMocks
    private TransaccionServiceImpl transaccionService;

    private TransferenciaRequest crearRequest(String origen, String destino, String monto) {
        TransferenciaRequest req = new TransferenciaRequest();
        req.setCuentaOrigen(origen);
        req.setCuentaDestino(destino);
        req.setMonto(new BigDecimal(monto));
        req.setDescripcion(DESCRIPCION);
        return req;
    }

    private Cuenta crearCuenta(String numero, BigDecimal saldo, EstadoCuenta estado) {
        Cuenta cuenta = new Cuenta();
        cuenta.setId(1L);
        cuenta.setNumeroCuenta(numero);
        cuenta.setSaldo(saldo);
        cuenta.setEstadoCuenta(estado);
        return cuenta;
    }

    private Transaccion crearTransaccionGuardada() {
        Transaccion t = new Transaccion();
        t.setId(1L);
        t.setCuentaOrigen(CUENTA_ORIGEN);
        t.setCuentaDestino(CUENTA_DESTINO);
        t.setMonto(new BigDecimal("100.00"));
        t.setTipo(TipoTransaccion.TRANSFERENCIA);
        t.setEstado(EstadoTransaccion.PROCESANDO);
        t.setFechaHora(Instant.now());
        return t;
    }

    @Test
    void procesarTransferenciaLanzaNotFoundCuandoCuentaOrigenNoExiste() {
        when(cuentaRepository.findByNumeroCuentaWithLock(CUENTA_ORIGEN))
                .thenReturn(Optional.empty());

        assertThrows(TransaccionNotFoundException.class,
                () -> transaccionService.procesarTransferencia(
                        crearRequest(CUENTA_ORIGEN, CUENTA_DESTINO, "100.00")));
    }

    @Test
    void procesarTransferenciaLanzaBloqueadaCuandoCuentaOrigenNoBloqueada() {
        Cuenta bloqueada = crearCuenta(CUENTA_ORIGEN, new BigDecimal("1000.00"), EstadoCuenta.BLOQUEADA);
        when(cuentaRepository.findByNumeroCuentaWithLock(CUENTA_ORIGEN))
                .thenReturn(Optional.of(bloqueada));

        assertThrows(CuentaBloqueadaException.class,
                () -> transaccionService.procesarTransferencia(
                        crearRequest(CUENTA_ORIGEN, CUENTA_DESTINO, "100.00")));
    }

    @Test
    void procesarTransferenciaLanzaBloqueadaCuandoCuentaOrigenEstaCerrada() {
        Cuenta cerrada = crearCuenta(CUENTA_ORIGEN, new BigDecimal("1000.00"), EstadoCuenta.CERRADA);
        when(cuentaRepository.findByNumeroCuentaWithLock(CUENTA_ORIGEN))
                .thenReturn(Optional.of(cerrada));

        assertThrows(CuentaBloqueadaException.class,
                () -> transaccionService.procesarTransferencia(
                        crearRequest(CUENTA_ORIGEN, CUENTA_DESTINO, "100.00")));
    }

    @Test
    void procesarTransferenciaLanzaSaldoInsuficienteCuandoSaldoMenor() {
        Cuenta activa = crearCuenta(CUENTA_ORIGEN, new BigDecimal("50.00"), EstadoCuenta.ACTIVA);
        when(cuentaRepository.findByNumeroCuentaWithLock(CUENTA_ORIGEN))
                .thenReturn(Optional.of(activa));

        assertThrows(SaldoInsuficienteException.class,
                () -> transaccionService.procesarTransferencia(
                        crearRequest(CUENTA_ORIGEN, CUENTA_DESTINO, "200.00")));
    }

    @Test
    void procesarTransferenciaLanzaNotFoundCuandoCuentaDestinoNoExiste() {
        Cuenta origen = crearCuenta(CUENTA_ORIGEN, new BigDecimal("1000.00"), EstadoCuenta.ACTIVA);
        when(cuentaRepository.findByNumeroCuentaWithLock(CUENTA_ORIGEN))
                .thenReturn(Optional.of(origen));
        when(cuentaRepository.findByNumeroCuenta(CUENTA_DESTINO))
                .thenReturn(Optional.empty());

        assertThrows(TransaccionNotFoundException.class,
                () -> transaccionService.procesarTransferencia(
                        crearRequest(CUENTA_ORIGEN, CUENTA_DESTINO, "100.00")));
    }

    @Test
    void procesarTransferenciaDevuelveRechazadaCuandoScoreSuperaUmbral() {
        Cuenta origen = crearCuenta(CUENTA_ORIGEN, new BigDecimal("1000.00"), EstadoCuenta.ACTIVA);
        Cuenta destino = crearCuenta(CUENTA_DESTINO, new BigDecimal("500.00"), EstadoCuenta.ACTIVA);
        Transaccion guardada = crearTransaccionGuardada();

        when(cuentaRepository.findByNumeroCuentaWithLock(CUENTA_ORIGEN))
                .thenReturn(Optional.of(origen));
        when(cuentaRepository.findByNumeroCuenta(CUENTA_DESTINO))
                .thenReturn(Optional.of(destino));
        when(transaccionRepository.save(any())).thenReturn(guardada);
        when(fraudeScoreCalculator.calcularScore(any())).thenReturn(SCORE_FRAUDE);

        SeguimientoResponse response = transaccionService.procesarTransferencia(
                crearRequest(CUENTA_ORIGEN, CUENTA_DESTINO, "100.00"));

        assertEquals(EstadoTransaccion.RECHAZADA, response.estado());
        assertNotNull(response.mensaje());
    }

    @Test
    void procesarTransferenciaDevuelveCompletadaCuandoScoreEsSeguro() {
        Cuenta origen = crearCuenta(CUENTA_ORIGEN, new BigDecimal("1000.00"), EstadoCuenta.ACTIVA);
        Cuenta destino = crearCuenta(CUENTA_DESTINO, new BigDecimal("500.00"), EstadoCuenta.ACTIVA);
        Transaccion guardada = crearTransaccionGuardada();

        when(cuentaRepository.findByNumeroCuentaWithLock(CUENTA_ORIGEN))
                .thenReturn(Optional.of(origen));
        when(cuentaRepository.findByNumeroCuenta(CUENTA_DESTINO))
                .thenReturn(Optional.of(destino));
        when(transaccionRepository.save(any())).thenReturn(guardada);
        when(cuentaRepository.save(any())).thenReturn(origen);
        when(fraudeScoreCalculator.calcularScore(any())).thenReturn(SCORE_SEGURO);

        SeguimientoResponse response = transaccionService.procesarTransferencia(
                crearRequest(CUENTA_ORIGEN, CUENTA_DESTINO, "100.00"));

        assertEquals(EstadoTransaccion.COMPLETADA, response.estado());
    }

    @Test
    void consultarEstadoRetornaResponseCuandoTransaccionExiste() {
        Transaccion transaccion = crearTransaccionGuardada();
        TransaccionResponse expectedResponse = new TransaccionResponse(
                1L, CUENTA_ORIGEN, CUENTA_DESTINO, new BigDecimal("100.00"),
                TipoTransaccion.TRANSFERENCIA, EstadoTransaccion.COMPLETADA,
                Instant.now(), DESCRIPCION, 0.0);
        when(transaccionRepository.findById(1L)).thenReturn(Optional.of(transaccion));
        when(transaccionMapper.toResponse(transaccion)).thenReturn(expectedResponse);

        TransaccionResponse response = transaccionService.consultarEstado(1L);

        assertNotNull(response);
        assertEquals(1L, response.id());
    }

    @Test
    void consultarEstadoLanzaNotFoundCuandoTransaccionNoExiste() {
        when(transaccionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(TransaccionNotFoundException.class,
                () -> transaccionService.consultarEstado(999L));
    }

    @Test
    void procesarLoteConUnaTransaccionCompletada() {
        TransferenciaRequest req = crearRequest(CUENTA_ORIGEN, CUENTA_DESTINO, "100.00");
        LoteTransaccionRequest lote = new LoteTransaccionRequest();
        lote.setTransacciones(List.of(req));
        SeguimientoResponse completada = new SeguimientoResponse(1L, EstadoTransaccion.COMPLETADA, "OK");
        when(self.procesarTransferencia(req)).thenReturn(completada);

        LoteResultadoResponse response = transaccionService.procesarLote(lote);

        assertEquals(1, response.totalRecibidas());
        assertEquals(1, response.totalProcesadas());
        assertEquals(0, response.totalRechazadas());
    }

    @Test
    void procesarLoteIncrementaRechazadasCuandoEstadoEsRechazado() {
        TransferenciaRequest req = crearRequest(CUENTA_ORIGEN, CUENTA_DESTINO, "100.00");
        LoteTransaccionRequest lote = new LoteTransaccionRequest();
        lote.setTransacciones(List.of(req));
        SeguimientoResponse rechazada = new SeguimientoResponse(1L, EstadoTransaccion.RECHAZADA, "Fraude");
        when(self.procesarTransferencia(req)).thenReturn(rechazada);

        LoteResultadoResponse response = transaccionService.procesarLote(lote);

        assertEquals(1, response.totalRecibidas());
        assertEquals(0, response.totalProcesadas());
        assertEquals(1, response.totalRechazadas());
    }

    @Test
    void procesarLoteIncrementaRechazadasCuandoSeLanzaExcepcion() {
        TransferenciaRequest req = crearRequest(CUENTA_ORIGEN, CUENTA_DESTINO, "100.00");
        LoteTransaccionRequest lote = new LoteTransaccionRequest();
        lote.setTransacciones(List.of(req));
        when(self.procesarTransferencia(req))
                .thenThrow(new SaldoInsuficienteException("Sin saldo"));

        LoteResultadoResponse response = transaccionService.procesarLote(lote);

        assertEquals(1, response.totalRecibidas());
        assertEquals(0, response.totalProcesadas());
        assertEquals(1, response.totalRechazadas());
    }
}
