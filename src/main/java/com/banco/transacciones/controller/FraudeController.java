package com.banco.transacciones.controller;

import com.banco.transacciones.model.dto.response.AlertaFraudeResponse;
import com.banco.transacciones.service.FraudeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/fraude")
@RequiredArgsConstructor
public class FraudeController {

    private final FraudeService fraudeService;

    @GetMapping("/alertas")
    public ResponseEntity<Page<AlertaFraudeResponse>> obtenerAlertas(Pageable pageable) {
        return ResponseEntity.ok(fraudeService.obtenerAlertas(pageable));
    }

    @PutMapping("/alertas/{id}/revisar")
    public ResponseEntity<AlertaFraudeResponse> revisarAlerta(@PathVariable Long id) {
        return ResponseEntity.ok(fraudeService.revisarAlerta(id));
    }
}