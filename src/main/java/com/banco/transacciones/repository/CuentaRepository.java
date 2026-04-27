package com.banco.transacciones.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.banco.transacciones.model.entity.Cuenta;
import com.banco.transacciones.model.enums.EstadoCuenta;

import jakarta.persistence.LockModeType;

@Repository
public interface CuentaRepository extends JpaRepository<Cuenta, Long> {
	
	Optional<Cuenta>findByNumeroCuenta(String numeroCuenta);
	
	 @Lock(LockModeType.PESSIMISTIC_WRITE)
	 @Query("SELECT c FROM Cuenta c WHERE c.numeroCuenta = :numeroCuenta")
	 Optional<Cuenta> findByNumeroCuentaWithLock(@Param("numeroCuenta") String numeroCuenta);
	
	List<Cuenta> findByClienteId(Long clienteId);
	List<Cuenta> findByEstadoCuenta(EstadoCuenta estadoCuenta);
	boolean existsByNumeroCuenta(String numeroCuenta);
}
