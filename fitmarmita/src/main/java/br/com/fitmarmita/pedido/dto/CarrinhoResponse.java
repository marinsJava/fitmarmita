package br.com.fitmarmita.pedido.dto;

import br.com.fitmarmita.pedido.entity.Carrinho;
import br.com.fitmarmita.pedido.entity.ItemCarrinho;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class CarrinhoResponse {

    private Long id;
    private Long usuarioId;
    private BigDecimal valorTotal;
    private List<ItemCarrinhoResponse> itens;

    public static CarrinhoResponse from(Carrinho carrinho) {
        return CarrinhoResponse.builder()
                .id(carrinho.getId())
                .usuarioId(carrinho.getUsuario().getId())
                .valorTotal(carrinho.getValorTotal())
                .itens(carrinho.getItens().stream()
                        .map(ItemCarrinhoResponse::from)
                        .toList())
                .build();
    }

    @Getter
    @Builder
    public static class ItemCarrinhoResponse {
        private Long id;
        private Long marmitaId;
        private String marmitaNome;
        private Integer quantidade;
        private BigDecimal precoUnitario;

        public static ItemCarrinhoResponse from(ItemCarrinho item) {
            return ItemCarrinhoResponse.builder()
                    .id(item.getId())
                    .marmitaId(item.getMarmita().getId())
                    .marmitaNome(item.getMarmita().getNome())
                    .quantidade(item.getQuantidade())
                    .precoUnitario(item.getPrecoUnitario())
                    .build();
        }
    }
}