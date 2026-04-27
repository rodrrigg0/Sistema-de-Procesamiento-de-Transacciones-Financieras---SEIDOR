package com.banco.transacciones.model.dto.request;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CuentasDistintasValidator
        implements ConstraintValidator<CuentasDistintas, TransferenciaRequest> {

    @Override
    public boolean isValid(TransferenciaRequest request,
                           ConstraintValidatorContext context) {
        if (request.getCuentaOrigen() == null || request.getCuentaDestino() == null) {
            return true;
        }
        return !request.getCuentaOrigen().equals(request.getCuentaDestino());
    }
}