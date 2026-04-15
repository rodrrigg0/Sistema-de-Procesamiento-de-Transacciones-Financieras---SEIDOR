package com.banco.transacciones.model.dto.response;

import java.util.List;

public record LoteResultadoResponse(
    Integer totalRecibidas,
    Integer totalProcesadas,
    Integer totalRechazadas,
    List<TransaccionResponse> transacciones
) {}