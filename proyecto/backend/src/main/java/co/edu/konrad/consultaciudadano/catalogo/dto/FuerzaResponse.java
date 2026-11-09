package co.edu.konrad.consultaciudadano.catalogo.dto;

import java.util.List;

public record FuerzaResponse(String codigo, String nombre, List<RangoResponse> rangos) {}
