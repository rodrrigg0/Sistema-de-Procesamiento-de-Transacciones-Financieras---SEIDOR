package com.banco.transacciones.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.banco.transacciones.model.entity.Cliente;
import com.banco.transacciones.model.entity.Cuenta;
import com.banco.transacciones.model.enums.EstadoCuenta;
import com.banco.transacciones.model.enums.TipoCuenta;
import com.banco.transacciones.repository.ClienteRepository;
import com.banco.transacciones.repository.CuentaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TransaccionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CuentaRepository cuentaRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        cuentaRepository.deleteAll();
        clienteRepository.deleteAll();

        // Crear clientes primero
        Cliente cliente1 = new Cliente();
        cliente1.setNombre("Cliente Uno");
        cliente1.setDni("12345678A");
        cliente1.setEmail("cliente1@test.com");
        cliente1 = clienteRepository.save(cliente1);

        Cliente cliente2 = new Cliente();
        cliente2.setNombre("Cliente Dos");
        cliente2.setDni("87654321B");
        cliente2.setEmail("cliente2@test.com");
        cliente2 = clienteRepository.save(cliente2);

        // Crear cuentas con los IDs reales de los clientes
        Cuenta cuentaOrigen = new Cuenta();
        cuentaOrigen.setNumeroCuenta("ES1234567890123456789012");
        cuentaOrigen.setSaldo(new BigDecimal("1000.00"));
        cuentaOrigen.setTipoCuenta(TipoCuenta.CORRIENTE);
        cuentaOrigen.setEstadoCuenta(EstadoCuenta.ACTIVA);
        cuentaOrigen.setClienteId(cliente1.getId());
        cuentaRepository.save(cuentaOrigen);

        Cuenta cuentaDestino = new Cuenta();
        cuentaDestino.setNumeroCuenta("ES9876543210987654321098");
        cuentaDestino.setSaldo(new BigDecimal("500.00"));
        cuentaDestino.setTipoCuenta(TipoCuenta.CORRIENTE);
        cuentaDestino.setEstadoCuenta(EstadoCuenta.ACTIVA);
        cuentaDestino.setClienteId(cliente2.getId());
        cuentaRepository.save(cuentaDestino);
    }

    @Test
    void procesarTransferencia_devuelve202_cuandoTodoEsCorrecto() throws Exception {
        Map<String, Object> request = Map.of(
                "cuentaOrigen", "ES1234567890123456789012",
                "cuentaDestino", "ES9876543210987654321098",
                "monto", 100.00,
                "descripcion", "Test transferencia"
        );

        mockMvc.perform(post("/api/transacciones/transferencia")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted());
    }

    @Test
    void procesarTransferencia_devuelve400_cuandoSaldoInsuficiente() throws Exception {
        Map<String, Object> request = Map.of(
                "cuentaOrigen", "ES1234567890123456789012",
                "cuentaDestino", "ES9876543210987654321098",
                "monto", 9999.00,
                "descripcion", "Test saldo insuficiente"
        );

        mockMvc.perform(post("/api/transacciones/transferencia")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void procesarTransferencia_devuelve400_cuandoCuentasIguales() throws Exception {
        Map<String, Object> request = Map.of(
                "cuentaOrigen", "ES1234567890123456789012",
                "cuentaDestino", "ES1234567890123456789012",
                "monto", 100.00,
                "descripcion", "Test cuentas iguales"
        );

        mockMvc.perform(post("/api/transacciones/transferencia")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}