package com.banco.transacciones.model.entity;

import com.banco.transacciones.model.enums.EstadoTransaccion;
import com.banco.transacciones.model.enums.TipoTransaccion;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.hibernate.annotations.ValueGenerationType;

@Data
@Entity
@Table(name = "transacciones")
public class Transaccion {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "cuenta_origen", nullable = false, length = 34)
	private String cuentaOrigen;
	
	@Column(name = "cuenta_origen", nullable = false, length = 34)
	private String cuentaDestino;
	
	@Column(nullable = false, precision = 15, scale = 2)
	private BigDecimal monto;
	
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private EstadoTransaccion estado;
	
	@Column(name= "fecha_hora", nullable = false)
	private Instant fechaHora;
	
	@Column(length = 255)
	private String  descripcion;
	
	@Column(name = "riesgo_fraude")
	private Double riesgoFraude;
	
	@OneToMany(mappedBy = "transaccion")
	private List<AlertaFraude> alertas;
	
	@PrePersist
    protected void onCreate() {
        fechaHora = Instant.now();
        if (riesgoFraude == null) riesgoFraude = 0.0;
    }
}
