package co.edu.konrad.consultaciudadano.catalogo.dto;

import java.util.List;

/**
 * Fuerza pública con su código, nombre y lista de rangos.
 */
public record FuerzaResponse(String codigo, String nombre, List<RangoResponse> rangos) {}
