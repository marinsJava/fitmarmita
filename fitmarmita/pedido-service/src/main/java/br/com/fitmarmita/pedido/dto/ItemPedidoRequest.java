package br.com.fitmarmita.pedido.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ItemPedidoRequest(

        @NotNull(message = "O id da marmita e obrigatorio")
        Long marmitaId,

        @NotNull(message = "A quantidade e obrigatoria")
        @Min(value = 1, message = "A quantidade minima e 1")
        Integer quantidade
) {}
