
package com.rotacultural.rcservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI rotaCulturalOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Rota Cultural API")
                        .version("1.0.0")
                        .description(
                                "REST API for managing users, cultural places, " +
                                        "categories, trips, itineraries, preferences, " +
                                        "favorites and feedbacks."
                        )
                        .contact(new Contact()
                                .name("Guia Cultural")
                                .url("https://github.com/Guia-Cultural/rc-service")
                        )
                );
    }
}
