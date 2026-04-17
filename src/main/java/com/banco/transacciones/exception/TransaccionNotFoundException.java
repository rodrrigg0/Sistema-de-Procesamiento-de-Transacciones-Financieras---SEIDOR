package com.banco.transacciones.exception;

public class TransaccionNotFoundException extends RuntimeException {
    public TransaccionNotFoundException(String mensaje) {
        super(mensaje);
    }
}