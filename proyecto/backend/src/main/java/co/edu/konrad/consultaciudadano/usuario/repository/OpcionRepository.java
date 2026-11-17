package co.edu.konrad.consultaciudadano.usuario.repository;

import co.edu.konrad.consultaciudadano.usuario.entity.Opcion;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Acceso a datos de las opciones del sistema.
 */
public interface OpcionRepository extends JpaRepository<Opcion, Long> {}
