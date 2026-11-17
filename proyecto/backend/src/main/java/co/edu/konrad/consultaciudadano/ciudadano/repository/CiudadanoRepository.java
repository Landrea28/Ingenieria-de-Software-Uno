package co.edu.konrad.consultaciudadano.ciudadano.repository;

import co.edu.konrad.consultaciudadano.ciudadano.entity.Ciudadano;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Acceso a datos de ciudadanos con Spring Data JPA: búsqueda por identificación, existencia y listado paginado.
 */
public interface CiudadanoRepository extends JpaRepository<Ciudadano, Long> {
    Optional<Ciudadano> findByIdentificacion(String identificacion);
    boolean existsByIdentificacion(String identificacion);
    Page<Ciudadano> findByIdentificacionContainingIgnoreCaseOrNombresContainingIgnoreCaseOrApellidosContainingIgnoreCase(
            String identificacion, String nombres, String apellidos, Pageable pageable);
}
