package com.banco.transacciones.model.entity;

import com.banco.transacciones.model.enums.NivelRiesgo;
import jakarta.persistence.*;
import lombok.Data;

	@Data
	@Entity
	@Table(name = "alertas_fraude")
	public class AlertaFraude {
		@Id
		@GeneratedValue(strategy = GenerationType.IDENTITY)
		private Long id;
		
		@Column(name = "transaccion_id", nullable = false)
		private Long transaccionId;
		
		@ManyToOne
		@JoinColumn(name = "transaccion_id", insertable = false, updatable = false)
		private Transaccion transaccion;
		
		@Enumerated(EnumType.STRING)
	    @Column(nullable = false)
	    private NivelRiesgo nivel;

	    @Column(nullable = false)
	    private String motivo;

	    @Column(nullable = false)
	    private Boolean revisada = false;
	}

