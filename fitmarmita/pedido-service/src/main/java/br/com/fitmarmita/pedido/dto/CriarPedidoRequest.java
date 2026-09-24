package br.com.fitmarmita.pedido.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CriarPedidoRequest(

        @NotBlank(message = "A forma de pagamento e obrigatoria")
        String formaPagamento,

        @NotBlank(message = "O endereco de entrega e obrigatorio")
        String enderecoEntrega,

        @NotEmpty(message = "O pedido precisa de pelo menos um item")
        @Valid
        List<ItemPedidoRequest> itens
) {}
