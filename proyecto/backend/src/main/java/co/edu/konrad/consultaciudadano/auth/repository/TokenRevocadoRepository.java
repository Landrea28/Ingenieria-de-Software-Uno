package co.edu.konrad.consultaciudadano.auth.repository;

import co.edu.konrad.consultaciudadano.auth.entity.TokenRevocado;
import java.time.OffsetDateTime;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TokenRevocadoRepository extends JpaRepository<TokenRevocado, String> {
    void deleteAllByExpiraEnBefore(OffsetDateTime ahora);
}
