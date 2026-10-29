package pe.edu.upeu.autoandes.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;
@Configuration
@OpenAPIDefinition(info=@Info(title="AutoAndes API", version="v1", description="Servicios, vehículos y órdenes del taller AutoAndes"))
public class OpenApiConfig {}
