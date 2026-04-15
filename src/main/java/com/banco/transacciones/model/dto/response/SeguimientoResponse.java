package com.banco.transacciones.model.dto.response;

import com.banco.transacciones.model.enums.EstadoTransaccion;

public record SeguimientoResponse(
    Long transaccionId,
    EstadoTransaccion estado,
    String mensaje
) {}