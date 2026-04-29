package com.banco.transacciones.util;

import com.banco.transacciones.model.entity.Transaccion;
import com.banco.transacciones.model.enums.EstadoTransaccion;
import com.banco.transacciones.model.enums.TipoTransaccion;
import com.banco.transacciones.repository.CuentaRepository;
import com.banco.transacciones.repository.TransaccionRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import com.banco.transacciones.model.entity.Cuenta;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FraudeScoreCalculatorTest {

    @Mock
    private TransaccionRepository transaccionRepository;

    @Mock
    private CuentaRepository cuentaRepository;

    @InjectMocks
    private FraudeScoreCalculator fraudeScoreCalculator;

    private Transaccion transaccion;

    @BeforeEach
    void setUp() {
        transaccion = new Transaccion();
        transaccion.setId(1L);
        transaccion.setCuentaOrigen("ES1234567890123456789012");
        transaccion.setCuentaDestino("ES9876543210987654321098");
        transaccion.setMonto(new BigDecimal("500"));
        transaccion.setTipo(TipoTransaccion.TRANSFERENCIA);
        transaccion.setEstado(EstadoTransaccion.PROCESANDO);
        transaccion.setFechaHora(Instant.now());
    }

    @Test
    void scoreCero_cuandoNoSeActivaNingunIndicador() {
        // monto bajo, hora normal, sin transacciones recientes
        transaccion.setMonto(new BigDecimal("100"));
        transaccion.setFechaHora(
            ZonedDateTime.now(ZoneId.systemDefault())
                .withHour(12).toInstant());

        when(transaccionRepository.countByCuentaOrigenAndFechaHoraAfter(
                anyString(), any())).thenReturn(0L);

        double score = fraudeScoreCalculator.calcularScore(transaccion);

        assertEquals(0.0, score, 0.001);
    }

    @Test
    void scorePeso030_cuandoMontoSuperaDiezMil() {
        transaccion.setMonto(new BigDecimal("15000"));
        transaccion.setFechaHora(
            ZonedDateTime.now(ZoneId.systemDefault())
                .withHour(12).toInstant());

        when(transaccionRepository.countByCuentaOrigenAndFechaHoraAfter(
                anyString(), any())).thenReturn(0L);

        double score = fraudeScoreCalculator.calcularScore(transaccion);

        assertEquals(0.30, score, 0.001);
    }

    @Test
    void scorePeso020_cuandoHoraEsMadrugada() {
        transaccion.setMonto(new BigDecimal("100"));
        transaccion.setFechaHora(
            ZonedDateTime.now(ZoneId.systemDefault())
                .withHour(3).toInstant());

        when(transaccionRepository.countByCuentaOrigenAndFechaHoraAfter(
                anyString(), any())).thenReturn(0L);

        double score = fraudeScoreCalculator.calcularScore(transaccion);

        assertEquals(0.20, score, 0.001);
    }

    @Test
    void scorePeso025_cuandoHayMasDeTresTransaccionesRecientes() {
        transaccion.setMonto(new BigDecimal("100"));
        transaccion.setFechaHora(
            ZonedDateTime.now(ZoneId.systemDefault())
                .withHour(12).toInstant());

        when(transaccionRepository.countByCuentaOrigenAndFechaHoraAfter(
                anyString(), any())).thenReturn(4L);

        double score = fraudeScoreCalculator.calcularScore(transaccion);

        assertEquals(0.25, score, 0.001);
    }

    @Test
    void scoreMaximo_cuandoSeActivanTodosLosIndicadores() {
        transaccion.setMonto(new BigDecimal("15000"));
        transaccion.setFechaHora(
            ZonedDateTime.now(ZoneId.systemDefault())
                .withHour(3).toInstant());

        when(transaccionRepository.countByCuentaOrigenAndFechaHoraAfter(
                anyString(), any())).thenReturn(4L);

        double score = fraudeScoreCalculator.calcularScore(transaccion);

        assertTrue(score >= 0.75);
    }

    @Test
    void scorePeso010_cuandoPaisDestinoDistintoAlOrigen() {
        transaccion.setMonto(new BigDecimal("100"));
        transaccion.setFechaHora(
            ZonedDateTime.now(ZoneId.systemDefault()).withHour(12).toInstant());

        Cuenta cuentaDestino = new Cuenta();
        cuentaDestino.setNumeroCuenta(transaccion.getCuentaDestino());
        cuentaDestino.setPaisHabitual("GBR");

        Cuenta cuentaOrigen = new Cuenta();
        cuentaOrigen.setNumeroCuenta(transaccion.getCuentaOrigen());
        cuentaOrigen.setPaisHabitual("ESP");

        when(transaccionRepository.countByCuentaOrigenAndFechaHoraAfter(
                anyString(), any())).thenReturn(0L);
        when(cuentaRepository.findByNumeroCuenta(eq(transaccion.getCuentaDestino())))
                .thenReturn(java.util.Optional.of(cuentaDestino));
        when(cuentaRepository.findByNumeroCuenta(eq(transaccion.getCuentaOrigen())))
                .thenReturn(java.util.Optional.of(cuentaOrigen));

        double score = fraudeScoreCalculator.calcularScore(transaccion);

        assertEquals(0.10, score, 0.001);
    }

    @Test
    void scoreNoCambia_cuandoCuentaDestinoNoTienePaisHabitual() {
        transaccion.setMonto(new BigDecimal("100"));
        transaccion.setFechaHora(
            ZonedDateTime.now(ZoneId.systemDefault()).withHour(12).toInstant());

        Cuenta cuentaDestino = new Cuenta();
        cuentaDestino.setNumeroCuenta(transaccion.getCuentaDestino());
        cuentaDestino.setPaisHabitual(null);

        Cuenta cuentaOrigen = new Cuenta();
        cuentaOrigen.setNumeroCuenta(transaccion.getCuentaOrigen());
        cuentaOrigen.setPaisHabitual("ESP");

        when(transaccionRepository.countByCuentaOrigenAndFechaHoraAfter(
                anyString(), any())).thenReturn(0L);
        when(cuentaRepository.findByNumeroCuenta(eq(transaccion.getCuentaDestino())))
                .thenReturn(java.util.Optional.of(cuentaDestino));
        when(cuentaRepository.findByNumeroCuenta(eq(transaccion.getCuentaOrigen())))
                .thenReturn(java.util.Optional.of(cuentaOrigen));

        double score = fraudeScoreCalculator.calcularScore(transaccion);

        assertEquals(0.0, score, 0.001);
    }

    @Test
    void scorePeso015_cuandoCuentaDestinoEsNueva() {
        transaccion.setMonto(new BigDecimal("100"));
        transaccion.setFechaHora(
            ZonedDateTime.now(ZoneId.systemDefault()).withHour(12).toInstant());

        Cuenta cuentaDestino = new Cuenta();
        cuentaDestino.setNumeroCuenta(transaccion.getCuentaDestino());
        cuentaDestino.setFechaCreacion(LocalDateTime.now());

        when(transaccionRepository.countByCuentaOrigenAndFechaHoraAfter(
                anyString(), any())).thenReturn(0L);
        when(cuentaRepository.findByNumeroCuenta(eq(transaccion.getCuentaDestino())))
                .thenReturn(java.util.Optional.of(cuentaDestino));
        when(cuentaRepository.findByNumeroCuenta(eq(transaccion.getCuentaOrigen())))
                .thenReturn(java.util.Optional.empty());

        double score = fraudeScoreCalculator.calcularScore(transaccion);

        assertEquals(0.15, score, 0.001);
    }

    @Test
    void scoreNoCambia_cuandoCuentaDestinoEsAntigua() {
        transaccion.setMonto(new BigDecimal("100"));
        transaccion.setFechaHora(
            ZonedDateTime.now(ZoneId.systemDefault()).withHour(12).toInstant());

        Cuenta cuentaDestino = new Cuenta();
        cuentaDestino.setNumeroCuenta(transaccion.getCuentaDestino());
        cuentaDestino.setFechaCreacion(LocalDateTime.now().minusDays(30));

        when(transaccionRepository.countByCuentaOrigenAndFechaHoraAfter(
                anyString(), any())).thenReturn(0L);
        when(cuentaRepository.findByNumeroCuenta(eq(transaccion.getCuentaDestino())))
                .thenReturn(java.util.Optional.of(cuentaDestino));
        when(cuentaRepository.findByNumeroCuenta(eq(transaccion.getCuentaOrigen())))
                .thenReturn(java.util.Optional.empty());

        double score = fraudeScoreCalculator.calcularScore(transaccion);

        assertEquals(0.0, score, 0.001);
    }

    @Test
    void scoreNoCambia_cuandoCuentaOrigenNoExisteEnRepositorio() {
        transaccion.setMonto(new BigDecimal("100"));
        transaccion.setFechaHora(
            ZonedDateTime.now(ZoneId.systemDefault()).withHour(12).toInstant());

        Cuenta cuentaDestino = new Cuenta();
        cuentaDestino.setNumeroCuenta(transaccion.getCuentaDestino());
        cuentaDestino.setPaisHabitual("GBR");

        when(transaccionRepository.countByCuentaOrigenAndFechaHoraAfter(
                anyString(), any())).thenReturn(0L);
        when(cuentaRepository.findByNumeroCuenta(eq(transaccion.getCuentaDestino())))
                .thenReturn(java.util.Optional.of(cuentaDestino));
        when(cuentaRepository.findByNumeroCuenta(eq(transaccion.getCuentaOrigen())))
                .thenReturn(java.util.Optional.empty());

        double score = fraudeScoreCalculator.calcularScore(transaccion);

        assertEquals(0.0, score, 0.001);
    }
}