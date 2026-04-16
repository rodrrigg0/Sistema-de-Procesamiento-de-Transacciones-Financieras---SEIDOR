package com.banco.transacciones.service;

import com.banco.transacciones.model.dto.request.LoteTransaccionRequest;
import com.banco.transacciones.model.dto.request.TransferenciaRequest;
import com.banco.transacciones.model.dto.response.LoteResultadoResponse;
import com.banco.transacciones.model.dto.response.SeguimientoResponse;
import com.banco.transacciones.model.dto.response.TransaccionResponse;

public interface TransaccionService {

    // POST /api/transacciones/transferencia
    SeguimientoResponse procesarTransferencia(TransferenciaRequest request);

    // GET /api/transacciones/{id}/estado
    TransaccionResponse consultarEstado(Long id);

    // POST /api/transacciones/lote
    LoteResultadoResponse procesarLote(LoteTransaccionRequest request);
}