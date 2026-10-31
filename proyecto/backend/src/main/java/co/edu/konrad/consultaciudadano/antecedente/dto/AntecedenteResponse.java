package co.edu.konrad.consultaciudadano.antecedente.dto;

import co.edu.konrad.consultaciudadano.antecedente.entity.Antecedente;
import co.edu.konrad.consultaciudadano.antecedente.entity.EstadoAntecedente;
import java.time.OffsetDateTime;

public record AntecedenteResponse(String identificacion, EstadoAntecedente estado,
        String fuente, OffsetDateTime fechaActualizacion) {
    public static AntecedenteResponse desde(Antecedente antecedente) {
        return new AntecedenteResponse(antecedente.getCiudadano().getIdentificacion(),
                antecedente.getEstado(), antecedente.getFuente(), antecedente.getFechaActualizacion());
    }
}
