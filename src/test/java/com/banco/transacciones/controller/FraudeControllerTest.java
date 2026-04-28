package com.banco.transacciones.controller;

import com.banco.transacciones.model.entity.AlertaFraude;
import com.banco.transacciones.model.entity.Transaccion;
import com.banco.transacciones.model.enums.EstadoTransaccion;
import com.banco.transacciones.model.enums.NivelRiesgo;
import com.banco.transacciones.model.enums.TipoTransaccion;
import com.banco.transacciones.repository.AlertaFraudeRepository;
import com.banco.transacciones.repository.TransaccionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FraudeControllerTest {

    private static final String URL_ALERTAS = "/api/fraude/alertas";
    private static final String URL_REVISAR = "/api/fraude/alertas/{id}/revisar";
    private static final String CUENTA_ORIGEN = "ES1100000000000000000011";
    private static final String CUENTA_DESTINO = "ES2200000000000000000022";
    private static final String MOTIVO_TEST = "Motivo de prueba";
    private static final long ID_INEXISTENTE = 999999L;

    private final MockMvc mockMvc;
    private final AlertaFraudeRepository alertaFraudeRepository;
    private final TransaccionRepository transaccionRepository;

    private Long transaccionId;
    private Long alertaId;

    @Autowired
    FraudeControllerTest(MockMvc mockMvc,
                         AlertaFraudeRepository alertaFraudeRepository,
                         TransaccionRepository transaccionRepository) {
        this.mockMvc = mockMvc;
        this.alertaFraudeRepository = alertaFraudeRepository;
        this.transaccionRepository = transaccionRepository;
    }

    @BeforeEach
    void setUp() {
        alertaFraudeRepository.deleteAll();
        transaccionRepository.deleteAll();

        Transaccion transaccion = new Transaccion();
        transaccion.setCuentaOrigen(CUENTA_ORIGEN);
        transaccion.setCuentaDestino(CUENTA_DESTINO);
        transaccion.setMonto(new BigDecimal("200.00"));
        transaccion.setTipo(TipoTransaccion.TRANSFERENCIA);
        transaccion.setEstado(EstadoTransaccion.COMPLETADA);
        transaccionId = transaccionRepository.save(transaccion).getId();

        AlertaFraude alerta = new AlertaFraude();
        alerta.setTransaccionId(transaccionId);
        alerta.setNivel(NivelRiesgo.MEDIO);
        alerta.setMotivo(MOTIVO_TEST);
        alerta.setRevisada(false);
        alertaId = alertaFraudeRepository.save(alerta).getId();
    }

    @Test
    void obtenerAlertasDevuelve200ConAlertasNoRevisadas() throws Exception {
        mockMvc.perform(get(URL_ALERTAS))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void revisarAlertaDevuelve200YMarcaComoRevisada() throws Exception {
        mockMvc.perform(put(URL_REVISAR, alertaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(alertaId))
                .andExpect(jsonPath("$.revisada").value(true));
    }

    @Test
    void revisarAlertaDevuelve404CuandoAlertaNoExiste() throws Exception {
        mockMvc.perform(put(URL_REVISAR, ID_INEXISTENTE))
                .andExpect(status().isNotFound());
    }

    @Test
    void revisarAlertaNivelCriticoDevuelve200YDispararaNotificacion() throws Exception {
        AlertaFraude alertaCritica = new AlertaFraude();
        alertaCritica.setTransaccionId(transaccionId);
        alertaCritica.setNivel(NivelRiesgo.CRITICO);
        alertaCritica.setMotivo(MOTIVO_TEST);
        alertaCritica.setRevisada(false);
        Long alertaCriticaId = alertaFraudeRepository.save(alertaCritica).getId();

        mockMvc.perform(put(URL_REVISAR, alertaCriticaId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.revisada").value(true))
                .andExpect(jsonPath("$.nivel").value("CRITICO"));
    }
}
