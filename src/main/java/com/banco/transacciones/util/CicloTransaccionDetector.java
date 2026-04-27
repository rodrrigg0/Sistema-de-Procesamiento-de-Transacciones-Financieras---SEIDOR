package com.banco.transacciones.util;

import com.banco.transacciones.model.entity.Transaccion;
import com.banco.transacciones.repository.TransaccionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class CicloTransaccionDetector {

    private final TransaccionRepository transaccionRepository;

    public boolean detectarCiclo() {
        log.info("Iniciando deteccion de ciclos en transacciones del ultimo dia");

        // Obtener todas las transacciones del último día
        Instant unDiaAtras = Instant.now().minus(1, ChronoUnit.DAYS);
        List<Transaccion> transacciones = transaccionRepository
                .findByFechaHoraAfter(unDiaAtras);

        // Construir el grafo de transferencias
        // clave = cuenta origen, valor = lista de cuentas destino
        Map<String, List<String>> grafo = new HashMap<>();
        for (Transaccion t : transacciones) {
            grafo.computeIfAbsent(t.getCuentaOrigen(), k -> new ArrayList<>())
                    .add(t.getCuentaDestino());
        }

        // DFS iterativo para detectar ciclos
        Set<String> visitados = new HashSet<>();
        Set<String> enPila = new HashSet<>();

        for (String cuenta : grafo.keySet()) {
            if (!visitados.contains(cuenta) && dfsTieneCiclo(grafo, cuenta, visitados, enPila)) {
                log.warn("Ciclo detectado en las transacciones del ultimo dia");
                return true;
            }
        }

        log.info("No se detectaron ciclos en las transacciones del ultimo dia");
        return false;
    }

    private boolean dfsTieneCiclo(Map<String, List<String>> grafo,
                                   String inicio,
                                   Set<String> visitados,
                                   Set<String> enPila) {
        // Uso una pila para el DFS iterativo
        Deque<String> pila = new ArrayDeque<>();
        pila.push(inicio);

        while (!pila.isEmpty()) {
            String cuenta = pila.peek();

            if (!visitados.contains(cuenta)) {
                visitados.add(cuenta);
                enPila.add(cuenta);
            }

            List<String> vecinos = grafo.getOrDefault(cuenta, Collections.emptyList());
            boolean todosVisitados = true;

            for (String vecino : vecinos) {
                if (!visitados.contains(vecino)) {
                    pila.push(vecino);
                    todosVisitados = false;
                    break;
                } else if (enPila.contains(vecino)) {
                    log.warn("Ciclo detectado entre cuentas: {} -> {}", cuenta, vecino);
                    return true;
                }
            }

            if (todosVisitados) {
                enPila.remove(cuenta);
                pila.pop();
            }
        }
        return false;
    }
}