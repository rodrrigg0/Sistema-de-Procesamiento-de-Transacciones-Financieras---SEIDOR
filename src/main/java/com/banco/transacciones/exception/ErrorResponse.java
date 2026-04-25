package com.banco.transacciones.exception;

import java.time.Instant;

public record ErrorResponse(
    Instant timestamp,
    int status,
    String error,
    String detalle,
    String path
) {}