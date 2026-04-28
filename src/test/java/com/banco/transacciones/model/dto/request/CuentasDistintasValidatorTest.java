package com.banco.transacciones.model.dto.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CuentasDistintasValidatorTest {

    private static final String CUENTA_IBAN = "ES9876543210987654321098";

    private CuentasDistintasValidator validator;

    @BeforeEach
    void setUp() {
        validator = new CuentasDistintasValidator();
    }

    @Test
    void isValidReturnsTrueCuandoCuentaOrigenEsNull() {
        TransferenciaRequest request = new TransferenciaRequest();
        request.setCuentaOrigen(null);
        request.setCuentaDestino(CUENTA_IBAN);

        assertTrue(validator.isValid(request, null));
    }

    @Test
    void isValidReturnsTrueCuandoCuentaDestinoEsNull() {
        TransferenciaRequest request = new TransferenciaRequest();
        request.setCuentaOrigen(CUENTA_IBAN);
        request.setCuentaDestino(null);

        assertTrue(validator.isValid(request, null));
    }
}
