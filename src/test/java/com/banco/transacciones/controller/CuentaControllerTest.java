package com.banco.transacciones.controller;

import com.banco.transacciones.model.entity.Cliente;
import com.banco.transacciones.model.entity.Cuenta;
import com.banco.transacciones.model.enums.EstadoCuenta;
import com.banco.transacciones.model.enums.TipoCuenta;
import com.banco.transacciones.repository.ClienteRepository;
import com.banco.transacciones.repository.CuentaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CuentaControllerTest {

    private static final String CUENTA_IBAN = "ES9900000000000000000099";
    private static final String URL_RESUMEN = "/api/cuentas/{id}/resumen";
    private static final String DNI_CLIENTE = "11111111C";
    private static final String EMAIL_CLIENTE = "cuenta.ctrl.test@test.com";
    private static final long ID_INEXISTENTE = 999999L;

    private final MockMvc mockMvc;
    private final CuentaRepository cuentaRepository;
    private final ClienteRepository clienteRepository;

    private Long cuentaId;

    @Autowired
    CuentaControllerTest(MockMvc mockMvc,
                         CuentaRepository cuentaRepository,
                         ClienteRepository clienteRepository) {
        this.mockMvc = mockMvc;
        this.cuentaRepository = cuentaRepository;
        this.clienteRepository = clienteRepository;
    }

    @BeforeEach
    void setUp() {
        cuentaRepository.deleteAll();
        clienteRepository.deleteAll();

        Cliente cliente = new Cliente();
        cliente.setNombre("Cliente Cuenta Test");
        cliente.setDni(DNI_CLIENTE);
        cliente.setEmail(EMAIL_CLIENTE);
        cliente = clienteRepository.save(cliente);

        Cuenta cuenta = new Cuenta();
        cuenta.setNumeroCuenta(CUENTA_IBAN);
        cuenta.setSaldo(new BigDecimal("2500.00"));
        cuenta.setTipoCuenta(TipoCuenta.CORRIENTE);
        cuenta.setEstadoCuenta(EstadoCuenta.ACTIVA);
        cuenta.setClienteId(cliente.getId());
        cuentaId = cuentaRepository.save(cuenta).getId();
    }

    @Test
    void obtenerResumenDevuelve200CuandoCuentaExiste() throws Exception {
        mockMvc.perform(get(URL_RESUMEN, cuentaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numeroCuenta").value(CUENTA_IBAN))
                .andExpect(jsonPath("$.saldoActual").value(2500.00))
                .andExpect(jsonPath("$.totalMovimientos").value(0));
    }

    @Test
    void obtenerResumenDevuelve403CuandoCuentaNoExiste() throws Exception {
        mockMvc.perform(get(URL_RESUMEN, ID_INEXISTENTE))
                .andExpect(status().isForbidden());
    }
}
