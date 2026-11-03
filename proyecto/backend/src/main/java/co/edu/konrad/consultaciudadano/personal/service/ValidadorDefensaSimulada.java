package co.edu.konrad.consultaciudadano.personal.service;

import co.edu.konrad.consultaciudadano.personal.dto.RegistroPersonalRequest;
import co.edu.konrad.consultaciudadano.personal.repository.DefensaPersonalSimuladoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

@Service
public class ValidadorDefensaSimulada implements ValidadorDefensa {
    private static final Logger LOGGER = LoggerFactory.getLogger(ValidadorDefensaSimulada.class);
    private final DefensaPersonalSimuladoRepository defensa;

    public ValidadorDefensaSimulada(DefensaPersonalSimuladoRepository defensa) {
        this.defensa = defensa;
    }

    @Override
    public ResultadoValidacionDefensa validar(RegistroPersonalRequest solicitud) {
        ResultadoValidacionDefensa resultado;
        try {
            resultado = defensa.findByIdentificacion(solicitud.identificacion())
                    .filter(persona -> persona.isActivo()
                            && persona.getNombres().equalsIgnoreCase(solicitud.nombres())
                            && persona.getApellidos().equalsIgnoreCase(solicitud.apellidos())
                            && persona.getFuerza().getCodigo().equals(solicitud.fuerzaCodigo())
                            && persona.getRango().getId().equals(solicitud.rangoId())
                            && persona.getIdFuerza().equals(solicitud.idFuerza()))
                    .map(persona -> ResultadoValidacionDefensa.ACEPTADO)
                    .orElse(ResultadoValidacionDefensa.RECHAZADO);
        } catch (DataAccessException error) {
            LOGGER.error("SIMULADO: validador Defensa no disponible");
            return ResultadoValidacionDefensa.NO_DISPONIBLE;
        }
        LOGGER.info("SIMULADO: validacion de personal ejecutada; resultado={}", resultado);
        return resultado;
    }
}
