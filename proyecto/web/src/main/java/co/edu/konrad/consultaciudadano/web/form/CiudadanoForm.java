package co.edu.konrad.consultaciudadano.web.form;

import co.edu.konrad.consultaciudadano.web.dto.CiudadanoDto;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * Objeto que respalda el formulario de registro/edición (th:object="${ciudadanoForm}").
 * Es mutable porque Spring MVC lo llena campo por campo con los datos enviados.
 * Las validaciones (@NotBlank, @Pattern, ...) se agregarán en los próximos días.
 */
public class CiudadanoForm {

    private String identificacion;
    private String nombres;
    private String apellidos;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaNacimiento;
    private String lugarNacimiento;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaExpedicion;
    private String lugarExpedicion;
    private String rh;
    private String grupoSanguineo;
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
