package com.banco.transacciones.mapper;

import com.banco.transacciones.model.dto.response.AlertaFraudeResponse;
import com.banco.transacciones.model.entity.AlertaFraude;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AlertaFraudeMapper {

    AlertaFraudeResponse toResponse(AlertaFraude alertaFraude);

    List<AlertaFraudeResponse> toResponseList(List<AlertaFraude> alertas);
}