package com.banco.transacciones.model.dto.request;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CuentasDistintasValidator.class)
public @interface CuentasDistintas {

    String message() default "La cuenta origen y destino no pueden ser la misma";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}