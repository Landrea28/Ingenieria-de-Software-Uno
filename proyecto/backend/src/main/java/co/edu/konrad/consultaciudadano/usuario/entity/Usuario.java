package co.edu.konrad.consultaciudadano.usuario.entity;

import co.edu.konrad.consultaciudadano.catalogo.entity.FuerzaPublica;
import co.edu.konrad.consultaciudadano.catalogo.entity.Rango;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "usuario")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 10)
    private String identificacion;

    @Column(nullable = false, length = 100)
    private String nombres;

    @Column(nullable = false, length = 100)
    private String apellidos;

    @Column(nullable = false, unique = true, length = 200)
    private String correo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fuerza_codigo")
    private FuerzaPublica fuerza;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rango_id")
    private Rango rango;

    @Column(name = "id_fuerza", length = 40)
    private String idFuerza;

    @Getter(AccessLevel.NONE)
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(nullable = false)
    private boolean activo = true;

    @Column(name = "fecha_creacion", nullable = false)
    private OffsetDateTime fechaCreacion;

    @ManyToMany
    @JoinTable(name = "usuario_perfil", joinColumns = @JoinColumn(name = "usuario_id"),
            inverseJoinColumns = @JoinColumn(name = "perfil_id"))
    private Set<Perfil> perfiles = new HashSet<>();

    public Usuario(String identificacion, String nombres, String apellidos, String correo,
            FuerzaPublica fuerza, Rango rango, String idFuerza, String passwordHash) {
        this.identificacion = identificacion;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.correo = correo;
        this.fuerza = fuerza;
        this.rango = rango;
        this.idFuerza = idFuerza;
        this.passwordHash = passwordHash;
    }

    @PrePersist
    void prePersistir() { this.fechaCreacion = OffsetDateTime.now(); }
}
