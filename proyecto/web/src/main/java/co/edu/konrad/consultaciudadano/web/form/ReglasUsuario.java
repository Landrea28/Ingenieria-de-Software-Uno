package co.edu.konrad.consultaciudadano.web.form;

import java.util.Map;

/**
 * Reglas de los campos del usuario en un solo lugar: las usan las anotaciones de UsuarioForm
 * (servidor web) y los atributos HTML del formulario (navegador).
 * Fuente: CrearUsuarioRequest y ActualizarUsuarioRequest del backend.
 */
public final class ReglasUsuario {

    public static final int MAX_NOMBRES = 100;
    public static final int MAX_CORREO = 200;
    public static final String MSG_CORREO = "Escriba un correo válido, por ejemplo nombre@dominio.com.";

    private ReglasUsuario() {}

    /** Atributos HTML de cada campo, para el componente 'campo'. */
    public static Map<String, Map<String, Object>> paraFormulario() {
        return Map.of(
                "identificacion", Map.of("patron", ReglasCiudadano.PATRON_IDENTIFICACION, "maximo", 10,
                        "teclado", "numeric", "ejemplo", "Ej. 1012345678", "mensaje", ReglasCiudadano.MSG_IDENTIFICACION),
                "nombres", Map.of("maximo", MAX_NOMBRES),
                "apellidos", Map.of("maximo", MAX_NOMBRES),
                "correo", Map.of("maximo", MAX_CORREO, "teclado", "email", "ejemplo", "nombre@dominio.com",
                        "mensaje", MSG_CORREO));
    }
}
