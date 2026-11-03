package co.edu.konrad.consultaciudadano.personal.service;

import co.edu.konrad.consultaciudadano.personal.dto.RegistroPersonalRequest;

public interface ValidadorDefensa {
    ResultadoValidacionDefensa validar(RegistroPersonalRequest solicitud);
}
