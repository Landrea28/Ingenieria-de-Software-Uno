package co.edu.konrad.consultaciudadano.personal.config;

import co.edu.konrad.consultaciudadano.personal.service.RegistroPersonalIntegracion;
import co.edu.konrad.consultaciudadano.personal.service.RegistroPersonalIntegracionPendiente;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registra la implementación de respaldo del registro de personal cuando no hay otra disponible en el contexto.
 */
@Configuration
public class PersonalIntegracionConfig {
    @Bean
    @ConditionalOnMissingBean(RegistroPersonalIntegracion.class)
    RegistroPersonalIntegracion registroPersonalIntegracion() {
        return new RegistroPersonalIntegracionPendiente();
    }
}
