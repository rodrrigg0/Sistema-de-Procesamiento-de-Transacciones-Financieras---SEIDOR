package com.banco.transacciones.mapper;

import com.banco.transacciones.model.dto.response.ResumenCuentaResponse;
import com.banco.transacciones.model.entity.Cuenta;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CuentaMapper {

    @Mapping(target = "saldoActual", source = "saldo")
    @Mapping(target = "totalMovimientos", ignore = true)
    @Mapping(target = "montoPromedio", ignore = true)
    @Mapping(target = "desviacionEstandar", ignore = true)
    ResumenCuentaResponse toResumenResponse(Cuenta cuenta);
}