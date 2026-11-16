package com.rotacultural.rcservice.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI rotaCulturalOpenAPI() {
        final String securitySchemeName = "bearerAuth";
        return new OpenAPI()
                .info(new Info()
                        .title("Rota Cultural API")
                        .version("1.0.0")
                        .description(
                                "REST API for managing users, cultural places, " +
                                        "categories, trips, itineraries, preferences, " +
                                        "favorites, feedbacks and deterministic recommendations."
                        )
                        .contact(new Contact()
                                .name("Guia Cultural")
                                .url("https://github.com/Guia-Cultural/rc-service")
                        )
                )
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                        )
                );
    }
}
