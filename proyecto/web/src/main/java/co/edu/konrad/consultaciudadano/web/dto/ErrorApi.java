package co.edu.konrad.consultaciudadano.web.dto;

import java.util.List;

/**
 * Cuerpo de error de la API (esquema ErrorResponse). 'detalles' trae los errores de validación
 * con el formato "campo: mensaje".
 */
public record ErrorApi(String codigo, String mensaje, List<String> detalles) {}
