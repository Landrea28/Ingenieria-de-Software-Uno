package co.edu.konrad.consultaciudadano.web.form;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Reglas de los campos del ciudadano en un solo lugar. Las usan las anotaciones de
 * CiudadanoForm (servidor web) y los atributos HTML del formulario (navegador).
 * Fuente: CiudadanoRequest del backend y la tabla ciudadano (V1__esquema_inicial.sql).
 */
public final class ReglasCiudadano {

    public static final String PATRON_IDENTIFICACION = "[0-9]{6,10}";
    public static final String PATRON_SOLO_LETRAS = "[\\p{L} ]+";
    public static final int MAX_NOMBRES = 100;
    public static final int MAX_LUGAR = 150;
    public static final String ESTATURA_MIN = "0.50";
    public static final String ESTATURA_MAX = "2.50";

    public static final List<String> RH = List.of("+", "-");
    public static final List<String> GRUPOS_SANGUINEOS = List.of("A", "B", "AB", "O");

    public static final String MSG_IDENTIFICACION = "Debe tener entre 6 y 10 dígitos, sin puntos ni espacios.";
    public static final String MSG_SOLO_LETRAS = "Solo letras y espacios.";
    public static final String MSG_FECHA_FUTURA = "No puede ser una fecha futura.";
    public static final String MSG_FECHAS = "Debe ser posterior a la fecha de nacimiento.";
    public static final String MSG_ESTATURA = "Debe estar entre 0.50 y 2.50 m, con máximo 2 decimales (por ejemplo 1.70).";

    private ReglasCiudadano() {}

    /**
     * Atributos HTML de cada campo de texto/fecha/número, para el componente 'campo'.
     * 'hoy' limita las fechas (no se permiten fechas futuras).
     */
    public static Map<String, Map<String, Object>> paraFormulario(LocalDate hoy) {
        String maxFecha = hoy.toString(); // formato yyyy-MM-dd, el que usa <input type="date">
        return Map.of(
                "identificacion", Map.of("patron", PATRON_IDENTIFICACION, "maximo", 10, "teclado", "numeric",
                        "ejemplo", "Ej. 1012345678", "mensaje", MSG_IDENTIFICACION),
                "nombres", Map.of("patron", PATRON_SOLO_LETRAS, "maximo", MAX_NOMBRES, "mensaje", MSG_SOLO_LETRAS),
                "apellidos", Map.of("patron", PATRON_SOLO_LETRAS, "maximo", MAX_NOMBRES, "mensaje", MSG_SOLO_LETRAS),
                "fechaNacimiento", Map.of("max", maxFecha, "mensaje", MSG_FECHA_FUTURA),
                "lugarNacimiento", Map.of("maximo", MAX_LUGAR),
                "fechaExpedicion", Map.of("max", maxFecha, "mensaje", MSG_FECHA_FUTURA,
                        "despuesDe", "fechaNacimiento", "mensajeDespuesDe", MSG_FECHAS),
                "lugarExpedicion", Map.of("maximo", MAX_LUGAR),
                "estatura", Map.of("min", ESTATURA_MIN, "max", ESTATURA_MAX, "paso", "0.01", "teclado", "decimal",
                        "ejemplo", "Ej. 1.70", "mensaje", MSG_ESTATURA));
    }
}
