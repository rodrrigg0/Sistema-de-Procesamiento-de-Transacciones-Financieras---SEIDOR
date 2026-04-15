package com.banco.transacciones.model.dto.response;

import com.banco.transacciones.model.enums.EstadoTransaccion;
import com.banco.transacciones.model.enums.TipoTransaccion;
import java.math.BigDecimal;
import java.time.Instant;


public record TransaccionResponse(
    Long id,
    String cuentaOrigen,
    String cuentaDestino,
    BigDecimal monto,
    TipoTransaccion tipo,
    EstadoTransaccion estado,
    Instant fechaHora,
    String descripcion,
    Double riesgoFraude
) {}