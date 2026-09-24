package br.com.fitmarmita.pedido.client;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.math.BigDecimal;

@FeignClient(
        name = "cardapio-service",
        url = "${fitmarmita.cardapio-service.url}",
        configuration = FeignClientConfig.class
)
public interface CardapioClient {

    @GetMapping("/api/v1/marmitas/{id}")
    MarmitaDto buscarMarmita(@PathVariable("id") Long id);

    record MarmitaDto(
            Long id,
            String nome,
            BigDecimal preco,
            Integer calorias,
            Boolean esgotada,
            Boolean ativa
    ) {
    }
}
