package co.edu.konrad.consultaciudadano.personal.repository;

import co.edu.konrad.consultaciudadano.personal.entity.DefensaPersonalSimulado;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Acceso a datos del dataset SIMULADO de Defensa.
 */
public interface DefensaPersonalSimuladoRepository extends JpaRepository<DefensaPersonalSimulado, Long> {
    Optional<DefensaPersonalSimulado> findByIdentificacion(String identificacion);
}
