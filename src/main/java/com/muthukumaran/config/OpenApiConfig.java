package com.muthukumaran.organization.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {
    
    @Bean
    public OpenAPI organizationServiceOpenAPI() {
        Server localServer = new Server();
        localServer.setUrl("http://localhost:8080");
        localServer.setDescription("Local Development Server");
        
        Contact contact = new Contact();
        contact.setName("Muthukumaran Muthiah");
        contact.setEmail("support@muthukumaran-muthiah.com");
        
        License license = new License()
            .name("MIT License")
            .url("https://choosealicense.com/licenses/mit/");
        
        Info info = new Info()
            .title("Organization Service API")
            .version("1.0.0")
            .contact(contact)
            .description("REST API for managing groups and user memberships in a hierarchical organization structure")
            .termsOfService("https://www.muthukumaran-muthiah.com/terms")
            .license(license);
        
        return new OpenAPI()
            .info(info)
            .servers(List.of(localServer));
    }
}
