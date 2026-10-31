package co.edu.konrad.consultaciudadano.antecedente.entity;

import co.edu.konrad.consultaciudadano.ciudadano.entity.Ciudadano;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "antecedente")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Antecedente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ciudadano_id", nullable = false, unique = true)
    private Ciudadano ciudadano;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoAntecedente estado;

    @Column(nullable = false, length = 50)
    private String fuente;

    @Column(name = "fecha_actualizacion", nullable = false)
    private OffsetDateTime fechaActualizacion;

    public Antecedente(Ciudadano ciudadano, EstadoAntecedente estado) {
        this.ciudadano = ciudadano;
        this.estado = estado;
        this.fuente = "SIMULADA";
        this.fechaActualizacion = OffsetDateTime.now();
    }

    public void cambiarEstado(EstadoAntecedente nuevoEstado) {
        this.estado = nuevoEstado;
        this.fechaActualizacion = OffsetDateTime.now();
    }
}
