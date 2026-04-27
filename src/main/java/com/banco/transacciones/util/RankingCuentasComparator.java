package com.banco.transacciones.util;

import com.banco.transacciones.model.entity.Cuenta;
import com.banco.transacciones.model.enums.NivelRiesgo;
import com.banco.transacciones.repository.AlertaFraudeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.Comparator;
import java.util.List;

@Component
@RequiredArgsConstructor
public class RankingCuentasComparator {

    private final AlertaFraudeRepository alertaFraudeRepository;

    public List<Cuenta> ordenarPorRiesgo(List<Cuenta> cuentas) {
        return cuentas.stream()
                .sorted(crearComparador())
                .toList();
    }

    private Comparator<Cuenta> crearComparador() {
        // Criterio 1 — Score de fraude acumulado (descendente)
        Comparator<Cuenta> porScore = Comparator
                .comparing(Cuenta::getScoreRiesgo,
                        Comparator.nullsLast(Comparator.reverseOrder()));

        // Criterio 2 — Número de alertas CRITICAS (descendente)
        Comparator<Cuenta> porAlertas = Comparator.comparingLong(
                cuenta -> -alertaFraudeRepository
                        .countByTransaccionIdAndNivel(cuenta.getId(), NivelRiesgo.CRITICO));

        // Criterio 3 — Antigüedad de la cuenta (ascendente - cuentas nuevas primero)
        Comparator<Cuenta> porAntiguedad = Comparator.comparing(Cuenta::getId);

        return porScore.thenComparing(porAlertas).thenComparing(porAntiguedad);
    }
}