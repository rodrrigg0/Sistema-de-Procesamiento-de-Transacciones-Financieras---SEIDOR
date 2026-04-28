package com.banco.transacciones.service.impl;

import com.banco.transacciones.exception.TransaccionNotFoundException;
import com.banco.transacciones.mapper.AlertaFraudeMapper;
import com.banco.transacciones.model.dto.response.AlertaFraudeResponse;
import com.banco.transacciones.model.entity.AlertaFraude;
import com.banco.transacciones.model.enums.NivelRiesgo;
import com.banco.transacciones.repository.AlertaFraudeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FraudeServiceImplTest {

    private static final Long ALERTA_ID = 1L;
    private static final Long TRANSACCION_ID = 10L;
    private static final Long ID_INEXISTENTE = 999L;
    private static final String MOTIVO_TEST = "Operacion sospechosa detectada";

    @Mock
    private AlertaFraudeRepository alertaFraudeRepository;

    @Mock
    private AlertaFraudeMapper alertaFraudeMapper;

    @Mock
    private FraudeServiceImpl selfMock;

    private FraudeServiceImpl fraudeService;

    @BeforeEach
    void setUp() {
        fraudeService = new FraudeServiceImpl(alertaFraudeRepository, alertaFraudeMapper, selfMock);
    }

    private AlertaFraude crearAlerta(NivelRiesgo nivel) {
        AlertaFraude alerta = new AlertaFraude();
        alerta.setId(ALERTA_ID);
        alerta.setTransaccionId(TRANSACCION_ID);
        alerta.setNivel(nivel);
        alerta.setMotivo(MOTIVO_TEST);
        alerta.setRevisada(false);
        return alerta;
    }

    private AlertaFraudeResponse crearAlertaResponse(NivelRiesgo nivel, boolean revisada) {
        return new AlertaFraudeResponse(ALERTA_ID, TRANSACCION_ID, nivel, MOTIVO_TEST, revisada);
    }

    @Test
    void obtenerAlertasRetornaPaginaVacia() {
        Pageable pageable = PageRequest.of(0, 10);
        when(alertaFraudeRepository.findByRevisadaFalseOrderByNivelDescTransaccionIdAsc(pageable))
                .thenReturn(Page.empty());

        Page<AlertaFraudeResponse> resultado = fraudeService.obtenerAlertas(pageable);

        assertTrue(resultado.isEmpty());
        assertEquals(0, resultado.getTotalElements());
    }

    @Test
    void obtenerAlertasRetornaPaginaConAlertasNoRevisadas() {
        Pageable pageable = PageRequest.of(0, 10);
        AlertaFraude alerta = crearAlerta(NivelRiesgo.ALTO);
        AlertaFraudeResponse alertaResponse = crearAlertaResponse(NivelRiesgo.ALTO, false);
        when(alertaFraudeRepository.findByRevisadaFalseOrderByNivelDescTransaccionIdAsc(pageable))
                .thenReturn(new PageImpl<>(List.of(alerta)));
        when(alertaFraudeMapper.toResponse(alerta)).thenReturn(alertaResponse);

        Page<AlertaFraudeResponse> resultado = fraudeService.obtenerAlertas(pageable);

        assertEquals(1, resultado.getTotalElements());
        assertEquals(NivelRiesgo.ALTO, resultado.getContent().get(0).nivel());
    }

    @Test
    void revisarAlertaLanzaTransaccionNotFoundCuandoAlertaNoExiste() {
        when(alertaFraudeRepository.findById(ID_INEXISTENTE)).thenReturn(Optional.empty());

        assertThrows(TransaccionNotFoundException.class,
                () -> fraudeService.revisarAlerta(ID_INEXISTENTE));
    }

    @Test
    void revisarAlertaMarcaComoRevisadaCuandoNivelNoCritico() {
        AlertaFraude alerta = crearAlerta(NivelRiesgo.BAJO);
        AlertaFraudeResponse alertaResponse = crearAlertaResponse(NivelRiesgo.BAJO, true);
        when(alertaFraudeRepository.findById(ALERTA_ID)).thenReturn(Optional.of(alerta));
        when(alertaFraudeRepository.save(alerta)).thenReturn(alerta);
        when(alertaFraudeMapper.toResponse(alerta)).thenReturn(alertaResponse);

        AlertaFraudeResponse resultado = fraudeService.revisarAlerta(ALERTA_ID);

        assertTrue(alerta.getRevisada());
        assertNotNull(resultado);
        verify(selfMock, never()).enviarNotificacionAsincrona(any());
    }

    @Test
    void revisarAlertaNivelCriticoDispararNotificacionAsincrona() {
        AlertaFraude alerta = crearAlerta(NivelRiesgo.CRITICO);
        AlertaFraudeResponse alertaResponse = crearAlertaResponse(NivelRiesgo.CRITICO, true);
        when(alertaFraudeRepository.findById(ALERTA_ID)).thenReturn(Optional.of(alerta));
        when(alertaFraudeRepository.save(alerta)).thenReturn(alerta);
        when(alertaFraudeMapper.toResponse(alerta)).thenReturn(alertaResponse);

        AlertaFraudeResponse resultado = fraudeService.revisarAlerta(ALERTA_ID);

        assertTrue(alerta.getRevisada());
        assertNotNull(resultado);
        verify(selfMock).enviarNotificacionAsincrona(alerta);
    }
}
