package br.com.fitmarmita.pedido.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FinalizarCarrinhoRequest {

    @NotBlank(message = "Endereço de entrega é obrigatório")
    private String enderecoEntrega;

    private String formaPagamento;

    private String observacoes;
}
