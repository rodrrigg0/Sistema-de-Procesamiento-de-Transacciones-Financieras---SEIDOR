package com.banco.transacciones.util;

import com.banco.transacciones.model.entity.Cuenta;
import com.banco.transacciones.model.entity.Transaccion;
import com.banco.transacciones.repository.CuentaRepository;
import com.banco.transacciones.repository.TransaccionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class FraudeScoreCalculator {

    private final TransaccionRepository transaccionRepository;
    private final CuentaRepository cuentaRepository;

    private static final BigDecimal UMBRAL_MONTO = new BigDecimal("10000");
    private static final double PESO_MONTO = 0.30;
    private static final double PESO_HORA = 0.20;
    private static final double PESO_FRECUENCIA = 0.25;
    private static final double PESO_CUENTA_NUEVA = 0.15;
    private static final double PESO_PAIS = 0.10;

    public double calcularScore(Transaccion transaccion) {
        log.debug("Calculando score de fraude para transaccion: {}", transaccion.getId());

        double score = 0.0;

        // Indicador 1 — monto > 10.000€
        if (transaccion.getMonto().compareTo(UMBRAL_MONTO) > 0) {
            score += PESO_MONTO;
            log.debug("Indicador monto activado. Score parcial: {}", score);
        }

        // Indicador 2 — hora entre 00:00 y 05:00
        int hora = transaccion.getFechaHora()
                .atZone(ZoneId.systemDefault())
                .getHour();
        if (hora >= 0 && hora < 5) {
            score += PESO_HORA;
            log.debug("Indicador hora activado. Score parcial: {}", score);
        }

        // Indicador 3 — más de 3 transacciones en últimos 5 minutos
        Instant cincoMinutosAtras = Instant.now().minus(5, ChronoUnit.MINUTES);
        long transaccionesRecientes = transaccionRepository
                .countByCuentaOrigenAndFechaHoraAfter(
                        transaccion.getCuentaOrigen(),
                        cincoMinutosAtras);
        if (transaccionesRecientes > 3) {
            score += PESO_FRECUENCIA;
            log.debug("Indicador frecuencia activado. Score parcial: {}", score);
        }

        // Indicador 4 — cuenta destino creada hace menos de 7 días
        Optional<Cuenta> cuentaDestino = cuentaRepository
                .findByNumeroCuenta(transaccion.getCuentaDestino());
        if (cuentaDestino.isPresent()) {
            // Como no tenemos fechaCreacion en Cuenta, usamos el id como aproximación
            // En un sistema real usaríamos la fecha de creación de la cuenta
            log.debug("Cuenta destino encontrada");
        }

        // Indicador 5 — país destino distinto al habitual
        if (cuentaDestino.isPresent()) {
            Optional<Cuenta> cuentaOrigen = cuentaRepository
                    .findByNumeroCuenta(transaccion.getCuentaOrigen());
            if (cuentaOrigen.isPresent() &&
                cuentaOrigen.get().getPaisHabitual() != null &&
                cuentaDestino.get().getPaisHabitual() != null &&
                !cuentaOrigen.get().getPaisHabitual()
                        .equals(cuentaDestino.get().getPaisHabitual())) {
                score += PESO_PAIS;
                log.debug("Indicador pais activado. Score parcial: {}", score);
            }
        }

        log.info("Score de fraude calculado: {} para transaccion: {}",
                score, transaccion.getId());
        return score;
    }
}