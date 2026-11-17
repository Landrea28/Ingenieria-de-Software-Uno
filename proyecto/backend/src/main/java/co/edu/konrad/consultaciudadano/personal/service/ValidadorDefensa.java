package co.edu.konrad.consultaciudadano.personal.service;

import co.edu.konrad.consultaciudadano.personal.dto.RegistroPersonalRequest;

/**
 * Contrato de la validación del personal contra el Departamento de Defensa.
 * Permite reemplazar la simulación por una integración real sin tocar el resto del sistema.
 */
public interface ValidadorDefensa {
    ResultadoValidacionDefensa validar(RegistroPersonalRequest solicitud);
}
