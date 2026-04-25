package com.banco.transacciones.controller;

import com.banco.transacciones.model.dto.response.ResumenCuentaResponse;
import com.banco.transacciones.service.CuentaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cuentas")
@RequiredArgsConstructor
public class CuentaController {

    private final CuentaService cuentaService;

    @GetMapping("/{id}/resumen")
    public ResponseEntity<ResumenCuentaResponse> obtenerResumen(@PathVariable Long id) {
        return ResponseEntity.ok(cuentaService.obtenerResumen(id));
    }
}