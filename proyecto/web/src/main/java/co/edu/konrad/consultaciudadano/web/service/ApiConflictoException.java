package co.edu.konrad.consultaciudadano.web.service;

/**
 * HTTP 409: conflicto con el estado actual. El 'codigo' de la API distingue el motivo
 * (DUPLICADO, AUTODESACTIVACION_NO_PERMITIDA, ULTIMO_ADMIN_ACTIVO, PERMISOS_ADMIN_REQUERIDOS).
 */
public class ApiConflictoException extends ApiException {

    public static final String DUPLICADO = "DUPLICADO";
    public static final String AUTODESACTIVACION = "AUTODESACTIVACION_NO_PERMITIDA";
    public static final String ULTIMO_ADMIN = "ULTIMO_ADMIN_ACTIVO";
    public static final String PERMISOS_ADMIN = "PERMISOS_ADMIN_REQUERIDOS";

    private final String codigo;

    public ApiConflictoException(String mensaje, String codigo) {
        super(mensaje);
        this.codigo = codigo;
    }

    public ApiConflictoException(String mensaje) {
        this(mensaje, DUPLICADO);
    }

    public String getCodigo() {
        return codigo;
    }
}
