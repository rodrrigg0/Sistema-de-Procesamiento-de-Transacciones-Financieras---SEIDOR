package com.banco.transacciones.util;

import com.banco.transacciones.model.entity.Cuenta;
import com.banco.transacciones.model.enums.NivelRiesgo;
import com.banco.transacciones.repository.AlertaFraudeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RankingCuentasComparatorTest {

    @Mock
    private AlertaFraudeRepository alertaFraudeRepository;

    @InjectMocks
    private RankingCuentasComparator rankingCuentasComparator;

    private Cuenta crearCuenta(Long id, BigDecimal score) {
        Cuenta cuenta = new Cuenta();
        cuenta.setId(id);
        cuenta.setScoreRiesgo(score);
        return cuenta;
    }

    @Test
    void ordenarPorRiesgoConListaVaciaRetornaListaVacia() {
        List<Cuenta> resultado = rankingCuentasComparator.ordenarPorRiesgo(Collections.emptyList());

        assertTrue(resultado.isEmpty());
    }

    @Test
    void ordenarPorRiesgoOrdenaPorScoreDescendente() {
        Cuenta cuentaBaja = crearCuenta(1L, new BigDecimal("0.10"));
        Cuenta cuentaAlta = crearCuenta(2L, new BigDecimal("0.90"));
        Cuenta cuentaMedia = crearCuenta(3L, new BigDecimal("0.50"));

        List<Cuenta> resultado = rankingCuentasComparator.ordenarPorRiesgo(
                List.of(cuentaBaja, cuentaAlta, cuentaMedia));

        assertEquals(cuentaAlta, resultado.get(0));
        assertEquals(cuentaMedia, resultado.get(1));
        assertEquals(cuentaBaja, resultado.get(2));
    }

    @Test
    void ordenarPorRiesgoColocaScoreNuloAlFinal() {
        Cuenta cuentaConScore = crearCuenta(1L, new BigDecimal("0.50"));
        Cuenta cuentaSinScore = crearCuenta(2L, null);

        List<Cuenta> resultado = rankingCuentasComparator.ordenarPorRiesgo(
                List.of(cuentaSinScore, cuentaConScore));

        assertEquals(cuentaConScore, resultado.get(0));
        assertEquals(cuentaSinScore, resultado.get(1));
    }

    @Test
    void ordenarPorRiesgoConIgualScoreUsaAlertasCriticasDescendente() {
        Cuenta cuentaPocosCriticos = crearCuenta(1L, new BigDecimal("0.50"));
        Cuenta cuentaMuchosCriticos = crearCuenta(2L, new BigDecimal("0.50"));
        when(alertaFraudeRepository.countByTransaccionIdAndNivel(1L, NivelRiesgo.CRITICO))
                .thenReturn(1L);
        when(alertaFraudeRepository.countByTransaccionIdAndNivel(2L, NivelRiesgo.CRITICO))
                .thenReturn(5L);

        List<Cuenta> resultado = rankingCuentasComparator.ordenarPorRiesgo(
                List.of(cuentaPocosCriticos, cuentaMuchosCriticos));

        assertEquals(cuentaMuchosCriticos, resultado.get(0));
        assertEquals(cuentaPocosCriticos, resultado.get(1));
    }

    @Test
    void ordenarPorRiesgoConIgualScoreEIgualAlertasUsaIdAscendente() {
        Cuenta cuentaIdMenor = crearCuenta(1L, new BigDecimal("0.50"));
        Cuenta cuentaIdMayor = crearCuenta(3L, new BigDecimal("0.50"));
        when(alertaFraudeRepository.countByTransaccionIdAndNivel(any(), eq(NivelRiesgo.CRITICO)))
                .thenReturn(0L);

        List<Cuenta> resultado = rankingCuentasComparator.ordenarPorRiesgo(
                List.of(cuentaIdMayor, cuentaIdMenor));

        assertEquals(cuentaIdMenor, resultado.get(0));
        assertEquals(cuentaIdMayor, resultado.get(1));
    }
}
