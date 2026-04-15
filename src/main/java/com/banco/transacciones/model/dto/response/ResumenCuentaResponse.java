package com.banco.transacciones.model.dto.response;

import java.math.BigDecimal;


public record ResumenCuentaResponse(
    String numeroCuenta,
    BigDecimal saldoActual,
    Long totalMovimientos,
    BigDecimal montoPromedio,
    BigDecimal desviacionEstandar,
    BigDecimal scoreRiesgo
) {}