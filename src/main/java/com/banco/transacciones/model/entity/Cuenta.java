package com.banco.transacciones.model.entity;

import java.math.BigDecimal;
import java.util.List;

import com.banco.transacciones.model.enums.EstadoCuenta;
import com.banco.transacciones.model.enums.TipoCuenta;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "cuentas")
public class Cuenta {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "numero_cuenta", unique = true, nullable = false, length = 34)
	private String numeroCuenta;
	
	@Column(nullable = false, precision = 15, scale = 2)
	private BigDecimal saldo;
	
	
	@Enumerated(EnumType.STRING)
	@Column(name = "tipo_cuenta", nullable = false)
	private TipoCuenta tipocuenta;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "estado", nullable = false)
	private EstadoCuenta estadocuenta;
	
	@ManyToOne
	@JoinColumn(name= "cliente_id", insertable = false, updatable = false)
	private Cliente cliente;
	
	@OneToMany(mappedBy = "cuentaOrigen")
	private List<Transaccion> transacciones;
	
	
	
	
}
