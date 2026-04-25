package com.banco.transacciones.controller;

import com.banco.transacciones.model.dto.request.LoteTransaccionRequest;
import com.banco.transacciones.model.dto.request.TransferenciaRequest;
import com.banco.transacciones.model.dto.response.LoteResultadoResponse;
import com.banco.transacciones.model.dto.response.SeguimientoResponse;
import com.banco.transacciones.model.dto.response.TransaccionResponse;
import com.banco.transacciones.service.TransaccionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transacciones")
@RequiredArgsConstructor
public class TransaccionController {

    private final TransaccionService transaccionService;

    @PostMapping("/transferencia")
    public ResponseEntity<SeguimientoResponse> procesarTransferencia(
            @Valid @RequestBody TransferenciaRequest request) {
        SeguimientoResponse response = transaccionService.procesarTransferencia(request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    @GetMapping("/{id}/estado")
    public ResponseEntity<TransaccionResponse> consultarEstado(@PathVariable Long id) {
        return ResponseEntity.ok(transaccionService.consultarEstado(id));
    }

    @PostMapping("/lote")
    public ResponseEntity<LoteResultadoResponse> procesarLote(
            @Valid @RequestBody LoteTransaccionRequest request) {
        return ResponseEntity.ok(transaccionService.procesarLote(request));
    }
}