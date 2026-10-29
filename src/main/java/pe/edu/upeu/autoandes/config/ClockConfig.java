package pe.edu.upeu.autoandes.config;

import java.time.Clock;
import java.time.ZoneId;

import org.springframework.context.annotation.*;

@Configuration
public class ClockConfig {
    @Bean
    public Clock clock() {
        return Clock.system(ZoneId.of("America/Lima"));
    }
}
