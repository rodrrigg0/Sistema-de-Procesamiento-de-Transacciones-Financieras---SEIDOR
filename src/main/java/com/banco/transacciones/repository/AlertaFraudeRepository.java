package com.banco.transacciones.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.banco.transacciones.model.entity.AlertaFraude;
import com.banco.transacciones.model.enums.NivelRiesgo;

@Repository
public interface AlertaFraudeRepository extends JpaRepository<AlertaFraude, Long>{

	Page<AlertaFraude> findByRevisadaFalseOrderByNivelDescTransaccionIdAsc(Pageable pageable);
    List<AlertaFraude> findByTransaccionId(Long transaccionId);
    List<AlertaFraude> findByNivel(NivelRiesgo nivel);
    long countByTransaccionIdAndNivel(Long transaccionId, NivelRiesgo nivel);
	
}
