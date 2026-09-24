package br.com.fitmarmita.pedido.dto;

import br.com.fitmarmita.pedido.entity.StatusPedido;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AtualizarStatusRequest {

    @NotNull
    private StatusPedido novoStatus;
}
