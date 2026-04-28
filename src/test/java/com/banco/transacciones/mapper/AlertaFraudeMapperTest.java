package com.banco.transacciones.mapper;

import com.banco.transacciones.model.dto.response.AlertaFraudeResponse;
import com.banco.transacciones.model.entity.AlertaFraude;
import com.banco.transacciones.model.enums.NivelRiesgo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest
@ActiveProfiles("test")
class AlertaFraudeMapperTest {

    private static final String MOTIVO = "Operacion sospechosa en mapper test";

    private final AlertaFraudeMapper alertaFraudeMapper;

    @Autowired
    AlertaFraudeMapperTest(AlertaFraudeMapper alertaFraudeMapper) {
        this.alertaFraudeMapper = alertaFraudeMapper;
    }

    private AlertaFraude crearAlerta() {
        AlertaFraude alerta = new AlertaFraude();
        alerta.setId(1L);
        alerta.setTransaccionId(10L);
        alerta.setNivel(NivelRiesgo.ALTO);
        alerta.setMotivo(MOTIVO);
        alerta.setRevisada(false);
        return alerta;
    }

    @Test
    void toResponseDevuelveNullCuandoAlertaEsNull() {
        assertNull(alertaFraudeMapper.toResponse(null));
    }

    @Test
    void toResponseListDevuelveNullCuandoListaEsNull() {
        assertNull(alertaFraudeMapper.toResponseList(null));
    }

    @Test
    void toResponseListMappeaListaDeAlertas() {
        List<AlertaFraudeResponse> result = alertaFraudeMapper.toResponseList(
                List.of(crearAlerta()));

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(NivelRiesgo.ALTO, result.get(0).nivel());
        assertEquals(MOTIVO, result.get(0).motivo());
    }
}
