package com.banco.transacciones.model.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

@CuentasDistintas
public class TransferenciaRequest {

    @NotBlank(message = "La cuenta origen es obligatoria")
    @Pattern(regexp = "[A-Z]{2}[0-9]{22}", message = "Formato de cuenta inválido")
    private String cuentaOrigen;

    @NotBlank(message = "La cuenta destino es obligatoria")
    @Pattern(regexp = "[A-Z]{2}[0-9]{22}", message = "Formato de cuenta inválido")
    private String cuentaDestino;

    @Positive(message = "El monto debe ser positivo")
    @DecimalMax(value = "50000.00", message = "El monto no puede superar 50000€")
    private BigDecimal monto;

    private String descripcion;

    public String getCuentaOrigen() { return cuentaOrigen; }
    public void setCuentaOrigen(String cuentaOrigen) { this.cuentaOrigen = cuentaOrigen; }
    public String getCuentaDestino() { return cuentaDestino; }
    public void setCuentaDestino(String cuentaDestino) { this.cuentaDestino = cuentaDestino; }
    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
}