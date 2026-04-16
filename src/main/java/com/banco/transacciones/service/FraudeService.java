package com.banco.transacciones.service;

import com.banco.transacciones.model.dto.response.AlertaFraudeResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FraudeService {

    // GET /api/fraude/alertas
    Page<AlertaFraudeResponse> obtenerAlertas(Pageable pageable);

    // PUT /api/fraude/alertas/{id}/revisar
    AlertaFraudeResponse revisarAlerta(Long id);
}