package co.edu.konrad.consultaciudadano.auth.config;

import co.edu.konrad.consultaciudadano.auth.service.AutenticacionIntegracion;
import co.edu.konrad.consultaciudadano.auth.service.AutenticacionIntegracionPendiente;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthIntegracionConfig {
    @Bean
    @ConditionalOnMissingBean(AutenticacionIntegracion.class)
    AutenticacionIntegracion autenticacionIntegracion() {
        return new AutenticacionIntegracionPendiente();
    }
}
