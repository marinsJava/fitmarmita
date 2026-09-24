package br.com.fitmarmita.pedido.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI().info(new Info()
                .title("FitMarmita - Pedido Service")
                .description("Microsservico responsavel pela criacao e consulta de pedidos")
                .version("v1"));
    }
}
