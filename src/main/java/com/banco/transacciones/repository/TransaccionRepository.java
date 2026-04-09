package com.banco.transacciones.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.banco.transacciones.model.entity.Transaccion;

import java.time.Instant;
import java.util.List;
import com.banco.transacciones.model.enums.EstadoTransaccion;



@Repository
public interface TransaccionRepository extends JpaRepository<Transaccion, Long>{
	List<Transaccion> findByCuentaOrigen(String cuentaOrigen);
	List<Transaccion> findByCuentaDestino(String cuentaOrigen);
	List<Transaccion> findByEstado(EstadoTransaccion estado);
	
	List<Transaccion> findByCuentaOrigenAndFechaHoraBetween(
			String cuentaOrigen,
			Instant desde,
			Instant hasta
			);
	List<Transaccion> findByFechaHoraAfter(Instant desde);

	    long countByCuentaOrigenAndFechaHoraAfter(
	        String cuentaOrigen,
	        Instant desde
	    );	
}
