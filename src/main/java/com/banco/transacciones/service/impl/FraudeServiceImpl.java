package com.banco.transacciones.service.impl;

import com.banco.transacciones.exception.TransaccionNotFoundException;
import com.banco.transacciones.mapper.AlertaFraudeMapper;
import com.banco.transacciones.model.dto.response.AlertaFraudeResponse;
import com.banco.transacciones.model.entity.AlertaFraude;
import com.banco.transacciones.model.enums.NivelRiesgo;
import com.banco.transacciones.repository.AlertaFraudeRepository;
import com.banco.transacciones.service.FraudeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FraudeServiceImpl implements FraudeService {

    private final AlertaFraudeRepository alertaFraudeRepository;
    private final AlertaFraudeMapper alertaFraudeMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<AlertaFraudeResponse> obtenerAlertas(Pageable pageable) {
        MDC.put("correlationId", UUID.randomUUID().toString());
        log.info("Obteniendo alertas de fraude no revisadas");

        Page<AlertaFraude> alertas = alertaFraudeRepository
                .findByRevisadaFalseOrderByNivelDescTransaccionIdAsc(pageable);

        log.info("Se encontraron {} alertas", alertas.getTotalElements());
        return alertas.map(alertaFraudeMapper::toResponse);
    }

    @Override
    @Transactional
    public AlertaFraudeResponse revisarAlerta(Long id) {
        MDC.put("correlationId", UUID.randomUUID().toString());
        log.info("Revisando alerta de fraude con id: {}", id);

        AlertaFraude alerta = alertaFraudeRepository.findById(id)
                .orElseThrow(() -> new TransaccionNotFoundException(
                        "Alerta no encontrada con id: " + id));

        alerta.setRevisada(true);
        AlertaFraude alertaGuardada = alertaFraudeRepository.save(alerta);

        if (alerta.getNivel() == NivelRiesgo.CRITICO) {
            log.warn("Alerta CRITICA revisada, disparando notificacion asincrona");
            enviarNotificacionAsincrona(alerta);
        }

        log.info("Alerta {} marcada como revisada", id);
        return alertaFraudeMapper.toResponse(alertaGuardada);
    }

    @Async("transaccionExecutor")
    public void enviarNotificacionAsincrona(AlertaFraude alerta) {
        log.info("Enviando notificacion asincrona para alerta CRITICA id: {}", alerta.getId());
    }
}