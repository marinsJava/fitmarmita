package br.com.fitmarmita.pedido.dto;

import br.com.fitmarmita.pedido.entity.ItemPedido;
import br.com.fitmarmita.pedido.entity.Pedido;
import br.com.fitmarmita.pedido.entity.StatusPedido;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class PedidoResponse {

    private Long id;
    private Long usuarioId;
    private StatusPedido status;
    private BigDecimal valorTotal;
    private String formaPagamento;
    private String enderecoEntrega;
    private String observacoes;
    private LocalDateTime createdAt;
    private List<ItemPedidoResponse> itens;

    public static PedidoResponse from(Pedido pedido) {
        return PedidoResponse.builder()
                .id(pedido.getId())
                .usuarioId(pedido.getUsuario().getId())
                .status(pedido.getStatus())
                .valorTotal(pedido.getValorTotal())
                .formaPagamento(pedido.getFormaPagamento())
                .enderecoEntrega(pedido.getEnderecoEntrega())
                .observacoes(pedido.getObservacoes())
                .createdAt(pedido.getCreatedAt())
                .itens(pedido.getItens().stream()
                        .map(ItemPedidoResponse::from)
                        .toList())
                .build();
    }

    @Getter
    @Builder
    public static class ItemPedidoResponse {
        private Long id;
        private Long marmitaId;
        private String marmitaNome;
        private Integer quantidade;
        private BigDecimal precoUnitario;

        public static ItemPedidoResponse from(ItemPedido item) {
            return ItemPedidoResponse.builder()
                    .id(item.getId())
                    .marmitaId(item.getMarmita().getId())
                    .marmitaNome(item.getMarmita().getNome())
                    .quantidade(item.getQuantidade())
                    .precoUnitario(item.getPrecoUnitario())
                    .build();
        }
    }
}