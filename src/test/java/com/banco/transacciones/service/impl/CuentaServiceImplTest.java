package com.banco.transacciones.service.impl;

import com.banco.transacciones.exception.CuentaBloqueadaException;
import com.banco.transacciones.mapper.CuentaMapper;
import com.banco.transacciones.model.dto.response.ResumenCuentaResponse;
import com.banco.transacciones.model.entity.Cuenta;
import com.banco.transacciones.model.entity.Transaccion;
import com.banco.transacciones.repository.CuentaRepository;
import com.banco.transacciones.repository.TransaccionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CuentaServiceImplTest {

    private static final String NUMERO_CUENTA = "ES9900000000000000000099";
    private static final Long CUENTA_ID = 1L;
    private static final Long ID_INEXISTENTE = 999L;

    @Mock
    private CuentaRepository cuentaRepository;

    @Mock
    private TransaccionRepository transaccionRepository;

    @Mock
    private CuentaMapper cuentaMapper;

    @InjectMocks
    private CuentaServiceImpl cuentaService;

    private Cuenta crearCuenta() {
        Cuenta cuenta = new Cuenta();
        cuenta.setId(CUENTA_ID);
        cuenta.setNumeroCuenta(NUMERO_CUENTA);
        cuenta.setSaldo(new BigDecimal("1000.00"));
        return cuenta;
    }

    private Transaccion crearTransaccion(String monto) {
        Transaccion t = new Transaccion();
        t.setMonto(new BigDecimal(monto));
        return t;
    }

    @Test
    void obtenerResumenLanzaCuentaBloqueadaExceptionCuandoCuentaNoExiste() {
        when(cuentaRepository.findById(ID_INEXISTENTE)).thenReturn(Optional.empty());

        assertThrows(CuentaBloqueadaException.class,
                () -> cuentaService.obtenerResumen(ID_INEXISTENTE));
    }

    @Test
    void obtenerResumenRetornaCerosConListaDeTransaccionesVacia() {
        when(cuentaRepository.findById(CUENTA_ID)).thenReturn(Optional.of(crearCuenta()));
        when(transaccionRepository.findByCuentaOrigen(NUMERO_CUENTA))
                .thenReturn(Collections.emptyList());

        ResumenCuentaResponse response = cuentaService.obtenerResumen(CUENTA_ID);

        assertNotNull(response);
        assertEquals(NUMERO_CUENTA, response.numeroCuenta());
        assertEquals(0L, response.totalMovimientos());
        assertEquals(BigDecimal.ZERO, response.montoPromedio());
        assertEquals(BigDecimal.ZERO, response.desviacionEstandar());
    }

    @Test
    void obtenerResumenCalculaPromedioYDesviacionCeroConUnaTransaccion() {
        when(cuentaRepository.findById(CUENTA_ID)).thenReturn(Optional.of(crearCuenta()));
        when(transaccionRepository.findByCuentaOrigen(NUMERO_CUENTA))
                .thenReturn(List.of(crearTransaccion("500.00")));

        ResumenCuentaResponse response = cuentaService.obtenerResumen(CUENTA_ID);

        assertEquals(1L, response.totalMovimientos());
        assertEquals(0, new BigDecimal("500.00").compareTo(response.montoPromedio()));
        assertEquals(BigDecimal.ZERO, response.desviacionEstandar());
    }

    @Test
    void obtenerResumenCalculaPromedioYDesviacionConMultiplesTransacciones() {
        List<Transaccion> transacciones = List.of(
                crearTransaccion("100.00"),
                crearTransaccion("300.00"),
                crearTransaccion("500.00")
        );
        when(cuentaRepository.findById(CUENTA_ID)).thenReturn(Optional.of(crearCuenta()));
        when(transaccionRepository.findByCuentaOrigen(NUMERO_CUENTA)).thenReturn(transacciones);

        ResumenCuentaResponse response = cuentaService.obtenerResumen(CUENTA_ID);

        assertEquals(3L, response.totalMovimientos());
        assertEquals(0, new BigDecimal("300").compareTo(response.montoPromedio()));
        assertTrue(response.desviacionEstandar().compareTo(BigDecimal.ZERO) > 0);
    }
}
