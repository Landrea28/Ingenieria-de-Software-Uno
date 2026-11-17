package co.edu.konrad.consultaciudadano.catalogo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Entidad JPA del catálogo de fuerzas públicas (Policía, Ejército, Armada y Fuerza Aérea).
 */
@Entity
@Table(name = "fuerza_publica")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FuerzaPublica {
    @Id
    @Column(length = 30)
    private String codigo;

    @Column(nullable = false, length = 70)
    private String nombre;
}
