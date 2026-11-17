package co.edu.konrad.consultaciudadano.web.form;

import co.edu.konrad.consultaciudadano.web.dto.CiudadanoDto;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

import static co.edu.konrad.consultaciudadano.web.form.ReglasCiudadano.*;

/**
 * Objeto que respalda el formulario de registro/edición (th:object="${ciudadanoForm}").
 * Es mutable porque Spring MVC lo llena campo por campo con los datos enviados.
 * Las reglas copian las de CiudadanoRequest del backend y la tabla ciudadano; el backend
 * vuelve a validar, esto solo da mensajes claros antes de llamar a la API.
 */
public class CiudadanoForm {

    @NotBlank(message = "La identificación es obligatoria.")
    @Pattern(regexp = PATRON_IDENTIFICACION, message = MSG_IDENTIFICACION)
    private String identificacion;

    @NotBlank(message = "Los nombres son obligatorios.")
    @Size(max = MAX_NOMBRES, message = "Máximo 100 caracteres.")
    @Pattern(regexp = PATRON_SOLO_LETRAS, message = MSG_SOLO_LETRAS)
    private String nombres;

    @NotBlank(message = "Los apellidos son obligatorios.")
    @Size(max = MAX_NOMBRES, message = "Máximo 100 caracteres.")
    @Pattern(regexp = PATRON_SOLO_LETRAS, message = MSG_SOLO_LETRAS)
    private String apellidos;

    @NotNull(message = "La fecha de nacimiento es obligatoria.")
    @PastOrPresent(message = MSG_FECHA_FUTURA)
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaNacimiento;

    @NotBlank(message = "El lugar de nacimiento es obligatorio.")
    @Size(max = MAX_LUGAR, message = "Máximo 150 caracteres.")
    private String lugarNacimiento;

    @NotNull(message = "La fecha de expedición es obligatoria.")
    @PastOrPresent(message = MSG_FECHA_FUTURA)
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaExpedicion;

    @NotBlank(message = "El lugar de expedición es obligatorio.")
    @Size(max = MAX_LUGAR, message = "Máximo 150 caracteres.")
    private String lugarExpedicion;

    @NotBlank(message = "Seleccione el RH.")
    @Pattern(regexp = "[+-]", message = "Seleccione + o -.")
    private String rh;

    @NotBlank(message = "Seleccione el grupo sanguíneo.")
    @Pattern(regexp = "A|B|AB|O", message = "Seleccione A, B, AB u O.")
    private String grupoSanguineo;

    @NotNull(message = "La estatura es obligatoria.")
    @DecimalMin(value = ESTATURA_MIN, message = MSG_ESTATURA)
    @DecimalMax(value = ESTATURA_MAX, message = MSG_ESTATURA)
    @Digits(integer = 1, fraction = 2, message = MSG_ESTATURA)
    private BigDecimal estatura;

    public static CiudadanoForm desde(CiudadanoDto c) {
        CiudadanoForm form = new CiudadanoForm();
        form.identificacion = c.identificacion();
        form.nombres = c.nombres();
        form.apellidos = c.apellidos();
        form.fechaNacimiento = c.fechaNacimiento();
        form.lugarNacimiento = c.lugarNacimiento();
        form.fechaExpedicion = c.fechaExpedicion();
        form.lugarExpedicion = c.lugarExpedicion();
        form.rh = c.rh();
        form.grupoSanguineo = c.grupoSanguineo();
        form.estatura = c.estatura();
        return form;
    }

    /** Datos a enviar a la API (el id lo asigna el backend). Quita espacios sobrantes de los textos. */
    public CiudadanoDto aDto() {
        return new CiudadanoDto(null, identificacion, limpiar(nombres), limpiar(apellidos), fechaNacimiento,
                limpiar(lugarNacimiento), fechaExpedicion, limpiar(lugarExpedicion), rh, grupoSanguineo, estatura);
    }

    /** Regla de la tabla ciudadano (ck_ciudadano_fechas): la expedición es posterior al nacimiento. */
    public boolean fechasCoherentes() {
        return fechaNacimiento == null || fechaExpedicion == null || fechaExpedicion.isAfter(fechaNacimiento);
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

    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }

    public String getLugarNacimiento() { return lugarNacimiento; }
    public void setLugarNacimiento(String lugarNacimiento) { this.lugarNacimiento = lugarNacimiento; }

    public LocalDate getFechaExpedicion() { return fechaExpedicion; }
    public void setFechaExpedicion(LocalDate fechaExpedicion) { this.fechaExpedicion = fechaExpedicion; }

    public String getLugarExpedicion() { return lugarExpedicion; }
    public void setLugarExpedicion(String lugarExpedicion) { this.lugarExpedicion = lugarExpedicion; }

    public String getRh() { return rh; }
    public void setRh(String rh) { this.rh = rh; }

    public String getGrupoSanguineo() { return grupoSanguineo; }
    public void setGrupoSanguineo(String grupoSanguineo) { this.grupoSanguineo = grupoSanguineo; }

    public BigDecimal getEstatura() { return estatura; }
    public void setEstatura(BigDecimal estatura) { this.estatura = estatura; }
}
