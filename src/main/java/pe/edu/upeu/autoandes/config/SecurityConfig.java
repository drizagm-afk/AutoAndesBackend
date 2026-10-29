package pe.edu.upeu.autoandes.config;

import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/** Temporary Questions 1–2 configuration: JWT and permission enforcement are implemented in Question 3. */
@Configuration @EnableWebSecurity
public class SecurityConfig {
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http.cors(org.springframework.security.config.Customizer.withDefaults()).csrf(c -> c.disable())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(a -> a
                .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/api/v1/ordenes", "/api/v1/ordenes/**", "/api/v1/reportes/**", "/api/v1/clientes", "/api/v1/servicios", "/api/v1/servicios/**", "/api/v1/vehiculos", "/api/v1/vehiculos/**", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/error").permitAll()
                .anyRequest().denyAll()).build();
    }
}
