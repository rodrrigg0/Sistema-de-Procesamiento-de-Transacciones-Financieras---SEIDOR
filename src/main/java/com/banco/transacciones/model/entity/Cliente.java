package com.banco.transacciones.model.entity;

import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "clientes")
public class Cliente {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(nullable = false)
	 private String nombre;
	
	@Column(nullable = false, unique = true)
	private String dni;
	
	@Column(nullable = false, unique = true)
	private String email;
	
	@Column(name = "fecha_alta", nullable = false, updatable = false)
	private LocalDate fechaAlta;
	
	@OneToMany(mappedBy = "cliente")
	private List<Cuenta> cuentas;
	
	@PrePersist
	protected void onCreate() {
		fechaAlta = LocalDate.now();
	}
}
