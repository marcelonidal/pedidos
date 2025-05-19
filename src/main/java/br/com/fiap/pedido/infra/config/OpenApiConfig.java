package br.com.fiap.pedido.infra.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI pedidoOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API de Pedidos")
                        .description("Microsserviço responsável pela criação e consulta de pedidos. Obs: Use o seletor acima para alternar entre a documentação `publico` (gateway externo) e `interno` (endpoints internos).")
                        .version("1.0.0"));
    }

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("publico")
                .packagesToScan("br.com.fiap.pedido.app.controller")
                .pathsToMatch("/api/**") // endpoint externo
                .build();
    }

    @Bean
    public GroupedOpenApi internalApi() {
        return GroupedOpenApi.builder()
                .group("interno")
                .packagesToScan("br.com.fiap.pedido.app.controller")
                .pathsToMatch("/internal/**") // endpoint do pedido-service
                .build();
    }

}