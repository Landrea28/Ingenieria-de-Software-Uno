package co.edu.konrad.consultaciudadano.web.form;

import static co.edu.konrad.consultaciudadano.web.form.ReglasCiudadano.MSG_IDENTIFICACION;
import static co.edu.konrad.consultaciudadano.web.form.ReglasCiudadano.PATRON_IDENTIFICACION;
import static co.edu.konrad.consultaciudadano.web.form.ReglasUsuario.MAX_CORREO;
import static co.edu.konrad.consultaciudadano.web.form.ReglasUsuario.MAX_NOMBRES;
import static co.edu.konrad.consultaciudadano.web.form.ReglasUsuario.MSG_CORREO;

import co.edu.konrad.consultaciudadano.web.dto.UsuarioDatos;
import co.edu.konrad.consultaciudadano.web.dto.UsuarioDto;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Objeto que respalda el formulario de crear/editar usuario (th:object="${usuarioForm}").
 * No tiene contraseña: la genera el backend y la envía por correo. El estado solo se usa al editar
 * (un usuario nuevo nace ACTIVO). El backend vuelve a validar todo.
 */
public class UsuarioForm {

    @NotBlank(message = "La identificación es obligatoria.")
    @Pattern(regexp = PATRON_IDENTIFICACION, message = MSG_IDENTIFICACION)
    private String identificacion;

    @NotBlank(message = "Los nombres son obligatorios.")
    @Size(max = MAX_NOMBRES, message = "Máximo 100 caracteres.")
    private String nombres;

    @NotBlank(message = "Los apellidos son obligatorios.")
    @Size(max = MAX_NOMBRES, message = "Máximo 100 caracteres.")
    private String apellidos;

    @NotBlank(message = "El correo es obligatorio.")
    @Email(message = MSG_CORREO)
    @Size(max = MAX_CORREO, message = "Máximo 200 caracteres.")
    private String correo;

    @NotNull(message = "Seleccione el perfil.")
    private Long perfilId;

    @Pattern(regexp = "ACTIVO|INACTIVO", message = "Seleccione Activo o Inactivo.")
    private String estado = UsuarioDto.ACTIVO;

    public static UsuarioForm desde(UsuarioDto u, Long perfilId) {
        UsuarioForm form = new UsuarioForm();
        form.identificacion = u.identificacion();
        form.nombres = u.nombres();
        form.apellidos = u.apellidos();
        form.correo = u.correo();
        form.perfilId = perfilId;
        form.estado = u.estado();
        return form;
    }

    /** Datos a enviar a la API. Quita espacios sobrantes de los textos. */
    public UsuarioDatos aDatos() {
        return new UsuarioDatos(identificacion, limpiar(nombres), limpiar(apellidos),
                correo == null ? null : correo.trim(), perfilId, estado);
    }

    private static String limpiar(String texto) {
        return texto == null ? null : texto.trim().replaceAll("\\s+", " ");
    }

    public String getIdentificacion() { return identificacion; }
    public void setIdentificacion(String identificacion) { this.identificacion = identificacion; }
    public String getNombres() { return nombres; }
    public void setNombres(String nombres) { this.nombres = nombres; }
    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public Long getPerfilId() { return perfilId; }
    public void setPerfilId(Long perfilId) { this.perfilId = perfilId; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}
