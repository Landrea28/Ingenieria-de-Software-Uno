package co.edu.konrad.consultaciudadano.personal.entity;

import co.edu.konrad.consultaciudadano.catalogo.entity.FuerzaPublica;
import co.edu.konrad.consultaciudadano.catalogo.entity.Rango;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "defensa_personal_simulado")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DefensaPersonalSimulado {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 10)
    private String identificacion;
    @Column(nullable = false, length = 100)
    private String nombres;
    @Column(nullable = false, length = 100)
    private String apellidos;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fuerza_codigo", nullable = false)
    private FuerzaPublica fuerza;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "rango_id", nullable = false)
    private Rango rango;
    @Column(name = "id_fuerza", nullable = false, length = 40)
    private String idFuerza;
    @Column(nullable = false)
    private boolean activo;
}
