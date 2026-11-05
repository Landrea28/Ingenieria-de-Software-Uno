package co.edu.konrad.consultaciudadano.personal.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ServicioCorreoSmtp implements ServicioCorreo {
    private final JavaMailSender mailSender;
    private final String remitente;

    public ServicioCorreoSmtp(JavaMailSender mailSender,
            @Value("${app.mail.from:no-reply@localhost}") String remitente) {
        this.mailSender = mailSender;
        this.remitente = remitente;
    }

    @Override
    public void enviarCredenciales(String correo, String contrasena) {
        var mensaje = new SimpleMailMessage();
        mensaje.setFrom(remitente);
        mensaje.setTo(correo);
        mensaje.setSubject("[SIMULADO] Credenciales de acceso - Sistema de Consulta Ciudadano");
        mensaje.setText("[SIMULADO] Su registro fue aprobado. Su contraseña temporal es: " + contrasena
                + "\nEsta contraseña es personal y no debe compartirse.");
        mailSender.send(mensaje);
    }
}
