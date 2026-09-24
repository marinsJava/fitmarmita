package br.com.fitmarmita.pedido.dto;

import br.com.fitmarmita.pedido.entity.ItemPedido;

import java.math.BigDecimal;

public record ItemPedidoResponse(
        Long marmitaId,
        String nomeMarmita,
        BigDecimal precoUnitario,
        Integer calorias,
        Integer quantidade
) {
    public static ItemPedidoResponse de(ItemPedido i) {
        return new ItemPedidoResponse(
                i.getMarmitaId(), i.getNomeMarmita(),
                i.getPrecoUnitario(), i.getCalorias(), i.getQuantidade()
        );
    }
}
