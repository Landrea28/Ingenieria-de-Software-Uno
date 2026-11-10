package co.edu.konrad.consultaciudadano.web.service;

/**
 * HTTP 404. El 'codigo' de la API distingue el motivo: NO_ENCONTRADO (el recurso o la ruta no existe)
 * o SIN_RESULTADOS (la consulta es válida pero no hay datos, p. ej. un reporte vacío).
 */
public class ApiNoEncontradoException extends ApiException {

    public static final String SIN_RESULTADOS = "SIN_RESULTADOS";

    private final String codigo;

    public ApiNoEncontradoException(String mensaje, String codigo) {
        super(mensaje);
        this.codigo = codigo;
    }

    public ApiNoEncontradoException(String mensaje) {
        this(mensaje, "NO_ENCONTRADO");
    }

    public boolean sinResultados() {
        return SIN_RESULTADOS.equals(codigo);
    }
}
