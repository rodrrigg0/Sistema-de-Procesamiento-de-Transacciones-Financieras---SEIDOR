package com.banco.transacciones.exception.handler;

import com.banco.transacciones.exception.CuentaBloqueadaException;
import com.banco.transacciones.exception.ErrorResponse;
import com.banco.transacciones.exception.SaldoInsuficienteException;
import com.banco.transacciones.exception.TransaccionNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    private static final String REQUEST_URI = "/api/test";
    private static final String MSG_SALDO = "Saldo insuficiente en la cuenta";
    private static final String MSG_BLOQUEADA = "La cuenta está bloqueada";
    private static final String MSG_NO_ENCONTRADA = "Transaccion no encontrada";
    private static final String MSG_VALIDACION = "El campo es obligatorio";
    private static final String MSG_INTERNO = "NullPointerException inesperada";

    private GlobalExceptionHandler handler;

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        when(request.getRequestURI()).thenReturn(REQUEST_URI);
    }

    @Test
    void handleSaldoInsuficienteDevuelve400() {
        ResponseEntity<ErrorResponse> response = handler.handleSaldoInsuficiente(
                new SaldoInsuficienteException(MSG_SALDO), request);

        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(MSG_SALDO, response.getBody().detalle());
        assertEquals(REQUEST_URI, response.getBody().path());
        assertNotNull(response.getBody().timestamp());
    }

    @Test
    void handleCuentaBloqueadaDevuelve403() {
        ResponseEntity<ErrorResponse> response = handler.handleCuentaBloqueada(
                new CuentaBloqueadaException(MSG_BLOQUEADA), request);

        assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(MSG_BLOQUEADA, response.getBody().detalle());
        assertEquals(REQUEST_URI, response.getBody().path());
    }

    @Test
    void handleTransaccionNotFoundDevuelve404() {
        ResponseEntity<ErrorResponse> response = handler.handleTransaccionNotFound(
                new TransaccionNotFoundException(MSG_NO_ENCONTRADA), request);

        assertEquals(HttpStatus.NOT_FOUND.value(), response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(MSG_NO_ENCONTRADA, response.getBody().detalle());
        assertEquals(REQUEST_URI, response.getBody().path());
    }

    @Test
    void handleValidacionDevuelve400ConPrimerMensajeDeError() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(
                List.of(new ObjectError("campo", MSG_VALIDACION)));

        ResponseEntity<ErrorResponse> response = handler.handleValidacion(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(MSG_VALIDACION, response.getBody().detalle());
    }

    @Test
    void handleGenericoDevuelve500() {
        ResponseEntity<ErrorResponse> response = handler.handleGeneral(
                new RuntimeException(MSG_INTERNO), request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(MSG_INTERNO, response.getBody().detalle());
        assertEquals(REQUEST_URI, response.getBody().path());
    }
}
