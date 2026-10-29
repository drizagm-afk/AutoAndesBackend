package pe.edu.upeu.autoandes.config;

import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import jakarta.servlet.DispatcherType;
import pe.edu.upeu.autoandes.security.*;

@Configuration @EnableWebSecurity @EnableMethodSecurity
public class SecurityConfig {
    @Bean PasswordEncoder passwordEncoder() {return new BCryptPasswordEncoder();}
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http,JwtService jwt,UsuarioDetailsService usuarios,
        RestAuthenticationEntryPoint entryPoint,RestAccessDeniedHandler denied) throws Exception {
        return http.cors(Customizer.withDefaults()).csrf(c -> c.disable())
            .formLogin(f -> f.disable()).httpBasic(b -> b.disable()).logout(l -> l.disable())
            .requestCache(c -> c.disable())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(e -> e.authenticationEntryPoint(entryPoint).accessDeniedHandler(denied))
            .authorizeHttpRequests(a -> a
                .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                .requestMatchers(HttpMethod.OPTIONS,"/**").permitAll()
                .requestMatchers(HttpMethod.POST,"/api/v1/auth/login").permitAll()
                .requestMatchers("/v3/api-docs","/v3/api-docs/**","/swagger-ui/**","/swagger-ui.html").permitAll()
                .requestMatchers(HttpMethod.GET,"/api/v1/auth/me").authenticated()
                .requestMatchers(HttpMethod.GET,"/api/v1/clientes","/api/v1/vehiculos","/api/v1/vehiculos/*").hasAuthority("VEHICULO_LEER")
                .requestMatchers(HttpMethod.POST,"/api/v1/vehiculos").hasAuthority("VEHICULO_ESCRIBIR")
                .requestMatchers(HttpMethod.PUT,"/api/v1/vehiculos/*").hasAuthority("VEHICULO_ESCRIBIR")
                .requestMatchers(HttpMethod.DELETE,"/api/v1/vehiculos/*").hasAuthority("VEHICULO_ESCRIBIR")
                .requestMatchers(HttpMethod.GET,"/api/v1/servicios","/api/v1/servicios/*").hasAuthority("SERVICIO_LEER")
                .requestMatchers(HttpMethod.POST,"/api/v1/servicios").hasAuthority("SERVICIO_ESCRIBIR")
                .requestMatchers(HttpMethod.PUT,"/api/v1/servicios/*").hasAuthority("SERVICIO_ESCRIBIR")
                .requestMatchers(HttpMethod.DELETE,"/api/v1/servicios/*").hasAuthority("SERVICIO_ESCRIBIR")
                .requestMatchers(HttpMethod.PATCH,"/api/v1/servicios/*/estado").hasAuthority("SERVICIO_ESCRIBIR")
                .requestMatchers(HttpMethod.GET,"/api/v1/ordenes","/api/v1/ordenes/*").hasAuthority("ORDEN_LEER")
                .requestMatchers(HttpMethod.POST,"/api/v1/ordenes").hasAuthority("ORDEN_REGISTRAR")
                .requestMatchers(HttpMethod.PATCH,"/api/v1/ordenes/*/anular").hasAuthority("ORDEN_ANULAR")
                .requestMatchers("/api/v1/reportes/**").authenticated()
                .anyRequest().authenticated())
            .addFilterBefore(new JwtAuthenticationFilter(jwt,usuarios,entryPoint),UsernamePasswordAuthenticationFilter.class).build();
    }
}
