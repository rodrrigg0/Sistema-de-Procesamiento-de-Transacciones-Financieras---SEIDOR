package com.banco.transacciones.mapper;

import com.banco.transacciones.model.dto.response.TransaccionResponse;
import com.banco.transacciones.model.entity.Transaccion;
import com.banco.transacciones.model.enums.EstadoTransaccion;
import com.banco.transacciones.model.enums.TipoTransaccion;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest
@ActiveProfiles("test")
class TransaccionMapperTest {

    private static final String CUENTA_ORIGEN = "ES1234567890123456789012";
    private static final String CUENTA_DESTINO = "ES9876543210987654321098";

    private final TransaccionMapper transaccionMapper;

    @Autowired
    TransaccionMapperTest(TransaccionMapper transaccionMapper) {
        this.transaccionMapper = transaccionMapper;
    }

    private Transaccion crearTransaccion() {
        Transaccion t = new Transaccion();
        t.setId(1L);
        t.setCuentaOrigen(CUENTA_ORIGEN);
        t.setCuentaDestino(CUENTA_DESTINO);
        t.setMonto(new BigDecimal("250.00"));
        t.setTipo(TipoTransaccion.TRANSFERENCIA);
        t.setEstado(EstadoTransaccion.COMPLETADA);
        t.setFechaHora(Instant.now());
        t.setDescripcion("Test mapper");
        t.setRiesgoFraude(0.0);
        return t;
    }

    @Test
    void toResponseDevuelveNullCuandoTransaccionEsNull() {
        assertNull(transaccionMapper.toResponse(null));
    }

    @Test
    void toResponseListDevuelveNullCuandoListaEsNull() {
        assertNull(transaccionMapper.toResponseList(null));
    }

    @Test
    void toResponseListMappeaListaDeTransacciones() {
        List<TransaccionResponse> result = transaccionMapper.toResponseList(
                List.of(crearTransaccion()));

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(CUENTA_ORIGEN, result.get(0).cuentaOrigen());
        assertEquals(CUENTA_DESTINO, result.get(0).cuentaDestino());
    }
}
