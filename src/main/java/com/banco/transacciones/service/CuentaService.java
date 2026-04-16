package com.banco.transacciones.service;

import com.banco.transacciones.model.dto.response.ResumenCuentaResponse;

public interface CuentaService {

    // GET /api/cuentas/{id}/resumen
    ResumenCuentaResponse obtenerResumen(Long id);
}