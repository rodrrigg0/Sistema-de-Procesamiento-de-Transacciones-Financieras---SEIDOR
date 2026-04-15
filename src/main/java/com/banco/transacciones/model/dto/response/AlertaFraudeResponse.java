package com.banco.transacciones.model.dto.response;

import com.banco.transacciones.model.enums.NivelRiesgo;

public record AlertaFraudeResponse(
    Long id,
    Long transaccionId,
    NivelRiesgo nivel,
    String motivo,
    Boolean revisada
) {}