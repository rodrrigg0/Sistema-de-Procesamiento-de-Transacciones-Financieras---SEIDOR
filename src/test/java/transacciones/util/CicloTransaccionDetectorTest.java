package transacciones.util;

import com.banco.transacciones.model.entity.Transaccion;
import com.banco.transacciones.model.enums.EstadoTransaccion;
import com.banco.transacciones.model.enums.TipoTransaccion;
import com.banco.transacciones.repository.TransaccionRepository;
import com.banco.transacciones.util.CicloTransaccionDetector;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CicloTransaccionDetectorTest {

    @Mock
    private TransaccionRepository transaccionRepository;

    @InjectMocks
    private CicloTransaccionDetector cicloTransaccionDetector;

    @Test
    void detectarCiclo_cuandoExisteCiclo() {
        // Grafo con ciclo: A → B → C → A
        List<Transaccion> transacciones = List.of(
            crearTransaccion("ES11", "ES22"),
            crearTransaccion("ES22", "ES33"),
            crearTransaccion("ES33", "ES11") 
        );

        when(transaccionRepository.findByFechaHoraAfter(any()))
                .thenReturn(transacciones);

        boolean tieneCiclo = cicloTransaccionDetector.detectarCiclo();

        assertTrue(tieneCiclo, "Deberia detectar el ciclo A→B→C→A");
    }

    @Test
    void noDetectarCiclo_cuandoNoExisteCiclo() {
        // Grafo sin ciclo: A → B → C
        List<Transaccion> transacciones = List.of(
            crearTransaccion("ES11", "ES22"),
            crearTransaccion("ES22", "ES33")
        );

        when(transaccionRepository.findByFechaHoraAfter(any()))
                .thenReturn(transacciones);

        boolean tieneCiclo = cicloTransaccionDetector.detectarCiclo();

        assertFalse(tieneCiclo, "No deberia detectar ciclo en A→B→C");
    }

    @Test
    void noDetectarCiclo_cuandoNoHayTransacciones() {
        when(transaccionRepository.findByFechaHoraAfter(any()))
                .thenReturn(List.of());

        boolean tieneCiclo = cicloTransaccionDetector.detectarCiclo();

        assertFalse(tieneCiclo, "No deberia detectar ciclo si no hay transacciones");
    }

    private Transaccion crearTransaccion(String origen, String destino) {
        Transaccion t = new Transaccion();
        t.setCuentaOrigen(origen);
        t.setCuentaDestino(destino);
        t.setMonto(new BigDecimal("100"));
        t.setTipo(TipoTransaccion.TRANSFERENCIA);
        t.setEstado(EstadoTransaccion.COMPLETADA);
        t.setFechaHora(Instant.now());
        return t;
    }
}