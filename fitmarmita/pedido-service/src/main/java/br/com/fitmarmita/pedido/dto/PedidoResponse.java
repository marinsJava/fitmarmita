package br.com.fitmarmita.pedido.dto;

import br.com.fitmarmita.pedido.entity.Pedido;
import br.com.fitmarmita.pedido.entity.StatusPedido;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PedidoResponse(
        Long id,
        Long usuarioId,
        StatusPedido status,
        BigDecimal valorTotal,
        String formaPagamento,
        String enderecoEntrega,
        List<ItemPedidoResponse> itens,
        LocalDateTime criadoEm
) {
    public static PedidoResponse de(Pedido p) {
        return new PedidoResponse(
                p.getId(), p.getUsuarioId(), p.getStatus(), p.getValorTotal(),
                p.getFormaPagamento(), p.getEnderecoEntrega(),
                p.getItens().stream().map(ItemPedidoResponse::de).toList(),
                p.getCriadoEm()
        );
    }
}
