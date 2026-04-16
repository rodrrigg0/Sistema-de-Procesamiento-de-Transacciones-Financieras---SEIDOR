package com.banco.transacciones.mapper;

import com.banco.transacciones.model.dto.response.TransaccionResponse;
import com.banco.transacciones.model.entity.Transaccion;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface TransaccionMapper {

    TransaccionResponse toResponse(Transaccion transaccion);

    List<TransaccionResponse> toResponseList(List<Transaccion> transacciones);
}