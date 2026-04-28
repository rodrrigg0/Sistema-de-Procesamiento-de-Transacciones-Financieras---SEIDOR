package com.banco.transacciones.mapper;

import com.banco.transacciones.model.dto.response.ResumenCuentaResponse;
import com.banco.transacciones.model.entity.Cuenta;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest
@ActiveProfiles("test")
class CuentaMapperTest {

    private static final String NUMERO_CUENTA = "ES4400000000000000000044";
    private static final String SALDO_STR = "1500.00";

    private final CuentaMapper cuentaMapper;

    @Autowired
    CuentaMapperTest(CuentaMapper cuentaMapper) {
        this.cuentaMapper = cuentaMapper;
    }

    @Test
    void toResumenResponseMappeaNumeroCuentaYSaldoActual() {
        Cuenta cuenta = new Cuenta();
        cuenta.setNumeroCuenta(NUMERO_CUENTA);
        cuenta.setSaldo(new BigDecimal(SALDO_STR));
        cuenta.setScoreRiesgo(new BigDecimal("0.25"));

        ResumenCuentaResponse response = cuentaMapper.toResumenResponse(cuenta);

        assertNotNull(response);
        assertEquals(NUMERO_CUENTA, response.numeroCuenta());
        assertEquals(0, new BigDecimal(SALDO_STR).compareTo(response.saldoActual()));
        assertEquals(0, new BigDecimal("0.25").compareTo(response.scoreRiesgo()));
    }

    @Test
    void toResumenResponseDevuelveNullCuandoCuentaEsNull() {
        assertNull(cuentaMapper.toResumenResponse(null));
    }
}
