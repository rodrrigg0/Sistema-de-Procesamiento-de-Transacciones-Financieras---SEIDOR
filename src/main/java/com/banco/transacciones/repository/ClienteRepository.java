package com.banco.transacciones.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.banco.transacciones.model.entity.Cliente;

public interface ClienteRepository extends JpaRepository<Cliente, Long>{
	Optional<Cliente> findByDni(String dni);
	Optional<Cliente> findByEmail(String email);
	boolean existsByDni(String dni);
	boolean existsByEmail(String email);
}
