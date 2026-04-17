package com.banco.transacciones.exception;

public class SaldoInsuficienteException extends RuntimeException {
	public SaldoInsuficienteException(String mensaje) {
		super(mensaje);
	}
}
