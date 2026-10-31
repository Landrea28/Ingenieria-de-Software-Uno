package co.edu.konrad.consultaciudadano.ciudadano.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ciudadano")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Ciudadano {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @Column(nullable = false, length = 10, unique = true)
    @Setter(AccessLevel.NONE)
    private String identificacion;

    @Column(nullable = false, length = 100)
    private String nombres;

    @Column(nullable = false, length = 100)
    private String apellidos;

    @Column(name = "fecha_nacimiento", nullable = false)
    private LocalDate fechaNacimiento;

    @Column(name = "lugar_nacimiento", nullable = false, length = 150)
    private String lugarNacimiento;

    @Column(name = "fecha_expedicion", nullable = false)
    private LocalDate fechaExpedicion;

    @Column(name = "lugar_expedicion", nullable = false, length = 150)
    private String lugarExpedicion;

    @Column(nullable = false, length = 1)
    private String rh;

    @Enumerated(EnumType.STRING)
    @Column(name = "grupo_sanguineo", nullable = false, length = 2)
    private GrupoSanguineo grupoSanguineo;

    @Column(nullable = false, precision = 3, scale = 2)
    private BigDecimal estatura;

    public Ciudadano(String identificacion) { this.identificacion = identificacion; }
}
