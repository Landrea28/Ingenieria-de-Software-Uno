package co.edu.konrad.consultaciudadano.ciudadano.dto;

import co.edu.konrad.consultaciudadano.ciudadano.entity.Ciudadano;
import co.edu.konrad.consultaciudadano.ciudadano.entity.GrupoSanguineo;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Datos de un ciudadano que devuelve la API; evita exponer la entidad JPA.
 */
public record CiudadanoResponse(Long id, String identificacion, String nombres, String apellidos,
        LocalDate fechaNacimiento, String lugarNacimiento, LocalDate fechaExpedicion,
        String lugarExpedicion, String rh, GrupoSanguineo grupoSanguineo, BigDecimal estatura) {
    public static CiudadanoResponse desde(Ciudadano c) {
        return new CiudadanoResponse(c.getId(), c.getIdentificacion(), c.getNombres(), c.getApellidos(),
                c.getFechaNacimiento(), c.getLugarNacimiento(), c.getFechaExpedicion(),
                c.getLugarExpedicion(), c.getRh(), c.getGrupoSanguineo(), c.getEstatura());
    }
}
