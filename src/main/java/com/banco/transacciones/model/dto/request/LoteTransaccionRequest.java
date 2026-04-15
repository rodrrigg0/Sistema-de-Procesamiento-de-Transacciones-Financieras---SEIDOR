package com.banco.transacciones.model.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public class LoteTransaccionRequest {

    @NotEmpty(message = "El lote no puede estar vacío")
    @Size(max = 500, message = "El lote no puede superar 500 transacciones")
    @Valid
    private List<TransferenciaRequest> transacciones;

    public List<TransferenciaRequest> getTransacciones() { return transacciones; }
    public void setTransacciones(List<TransferenciaRequest> transacciones) {
        this.transacciones = transacciones;
    }
}