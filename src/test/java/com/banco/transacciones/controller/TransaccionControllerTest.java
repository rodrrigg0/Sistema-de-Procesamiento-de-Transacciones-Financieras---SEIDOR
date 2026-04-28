package com.banco.transacciones.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.util.List;
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

    private static final String CUENTA_ORIGEN_IBAN = "ES1234567890123456789012";
    private static final String CUENTA_DESTINO_IBAN = "ES9876543210987654321098";
    private static final String CUENTA_ORIGEN_KEY = "cuentaOrigen";
    private static final String CUENTA_DESTINO_KEY = "cuentaDestino";
    private static final String MONTO_KEY = "monto";
    private static final String DESCRIPCION_KEY = "descripcion";
    private static final String URL_TRANSFERENCIA = "/api/transacciones/transferencia";
    private static final String URL_ESTADO = "/api/transacciones/{id}/estado";
    private static final String URL_LOTE = "/api/transacciones/lote";
    private static final long ID_INEXISTENTE = 999999L;

    private final MockMvc mockMvc;
    private final CuentaRepository cuentaRepository;
    private final ClienteRepository clienteRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public TransaccionControllerTest(MockMvc mockMvc,
                                     CuentaRepository cuentaRepository,
                                     ClienteRepository clienteRepository,
                                     ObjectMapper objectMapper) {
        this.mockMvc = mockMvc;
        this.cuentaRepository = cuentaRepository;
        this.clienteRepository = clienteRepository;
        this.objectMapper = objectMapper;
    }

    @BeforeEach
    void setUp() {
        cuentaRepository.deleteAll();
        clienteRepository.deleteAll();

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

        Cuenta cuentaOrigen = new Cuenta();
        cuentaOrigen.setNumeroCuenta(CUENTA_ORIGEN_IBAN);
        cuentaOrigen.setSaldo(new BigDecimal("1000.00"));
        cuentaOrigen.setTipoCuenta(TipoCuenta.CORRIENTE);
        cuentaOrigen.setEstadoCuenta(EstadoCuenta.ACTIVA);
        cuentaOrigen.setClienteId(cliente1.getId());
        cuentaRepository.save(cuentaOrigen);

        Cuenta cuentaDestino = new Cuenta();
        cuentaDestino.setNumeroCuenta(CUENTA_DESTINO_IBAN);
        cuentaDestino.setSaldo(new BigDecimal("500.00"));
        cuentaDestino.setTipoCuenta(TipoCuenta.CORRIENTE);
        cuentaDestino.setEstadoCuenta(EstadoCuenta.ACTIVA);
        cuentaDestino.setClienteId(cliente2.getId());
        cuentaRepository.save(cuentaDestino);
    }

    @Test
    void procesarTransferenciaDevuelve202CuandoTodoEsCorrecto() throws Exception {
        Map<String, Object> request = Map.of(
                CUENTA_ORIGEN_KEY, CUENTA_ORIGEN_IBAN,
                CUENTA_DESTINO_KEY, CUENTA_DESTINO_IBAN,
                MONTO_KEY, 100.00,
                DESCRIPCION_KEY, "Test transferencia"
        );

        mockMvc.perform(post(URL_TRANSFERENCIA)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted());
    }

    @Test
    void procesarTransferenciaDevuelve400CuandoSaldoInsuficiente() throws Exception {
        Map<String, Object> request = Map.of(
                CUENTA_ORIGEN_KEY, CUENTA_ORIGEN_IBAN,
                CUENTA_DESTINO_KEY, CUENTA_DESTINO_IBAN,
                MONTO_KEY, 9999.00,
                DESCRIPCION_KEY, "Test saldo insuficiente"
        );

        mockMvc.perform(post(URL_TRANSFERENCIA)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void procesarTransferenciaDevuelve400CuandoCuentasIguales() throws Exception {
        Map<String, Object> request = Map.of(
                CUENTA_ORIGEN_KEY, CUENTA_ORIGEN_IBAN,
                CUENTA_DESTINO_KEY, CUENTA_ORIGEN_IBAN,
                MONTO_KEY, 100.00,
                DESCRIPCION_KEY, "Test cuentas iguales"
        );

        mockMvc.perform(post(URL_TRANSFERENCIA)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void consultarEstadoDevuelve200CuandoTransaccionExiste() throws Exception {
        Map<String, Object> transferRequest = Map.of(
                CUENTA_ORIGEN_KEY, CUENTA_ORIGEN_IBAN,
                CUENTA_DESTINO_KEY, CUENTA_DESTINO_IBAN,
                MONTO_KEY, 50.00,
                DESCRIPCION_KEY, "Test estado"
        );

        String responseBody = mockMvc.perform(post(URL_TRANSFERENCIA)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(transferRequest)))
                .andExpect(status().isAccepted())
                .andReturn().getResponse().getContentAsString();

        Long transaccionId = objectMapper.readTree(responseBody).get("transaccionId").asLong();

        mockMvc.perform(get(URL_ESTADO, transaccionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(transaccionId))
                .andExpect(jsonPath("$.cuentaOrigen").value(CUENTA_ORIGEN_IBAN));
    }

    @Test
    void consultarEstadoDevuelve404CuandoTransaccionNoExiste() throws Exception {
        mockMvc.perform(get(URL_ESTADO, ID_INEXISTENTE))
                .andExpect(status().isNotFound());
    }

    @Test
    void procesarLoteDevuelve200ConUnaTransaccionExitosa() throws Exception {
        Map<String, Object> transaccion = Map.of(
                CUENTA_ORIGEN_KEY, CUENTA_ORIGEN_IBAN,
                CUENTA_DESTINO_KEY, CUENTA_DESTINO_IBAN,
                MONTO_KEY, 50.00,
                DESCRIPCION_KEY, "Trans lote"
        );
        Map<String, Object> loteRequest = Map.of("transacciones", List.of(transaccion));

        mockMvc.perform(post(URL_LOTE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loteRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRecibidas").value(1))
                .andExpect(jsonPath("$.totalProcesadas").exists());
    }
}