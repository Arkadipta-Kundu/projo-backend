package org.arkadipta.projobackend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

        @Value("${server.port:8080}")
        private String serverPort;

        @Bean
        public OpenAPI customOpenAPI() {
                return new OpenAPI()
                                .info(new Info()
                                                .title("Projo Backend API")
                                                .description(
                                                                "A comprehensive project management system backend built with Spring Boot, providing REST APIs for managing projects, tasks, notes, issues, time tracking, and task reminders.")
                                                .version("1.0.0")
                                                .contact(new Contact()
                                                                .name("Arkadipta Kundu")
                                                                .email("arkadipta.kundu@gmail.com")
                                                                .url("https://github.com/arkadipta"))
                                                .license(new License()
                                                                .name("MIT License")
                                                                .url("https://opensource.org/licenses/MIT")))
                                .servers(List.of(
                                                new Server()
                                                                .url("http://localhost:" + serverPort)
                                                                .description("Local Development Server"),
                                                new Server()
                                                                .url("https://your-production-domain.com")
                                                                .description("Production Server")))
                                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                                .components(new Components()
                                                .addSecuritySchemes("Bearer Authentication", createAPIKeyScheme()));
        }

        private SecurityScheme createAPIKeyScheme() {
                return new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .bearerFormat("JWT")
                                .scheme("bearer")
                                .description("Enter JWT Bearer token in the format: Bearer {token}");
        }
}
