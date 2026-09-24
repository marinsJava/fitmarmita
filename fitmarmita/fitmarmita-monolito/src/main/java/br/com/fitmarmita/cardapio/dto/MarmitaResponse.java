package br.com.fitmarmita.cardapio.dto;

import br.com.fitmarmita.cardapio.entity.Marmita;

import java.math.BigDecimal;
import java.util.List;

public record MarmitaResponse(
        Long id,
        String nome,
        String descricao,
        BigDecimal preco,
        Integer calorias,
        BigDecimal proteinas,
        BigDecimal carboidratos,
        BigDecimal gorduras,
        Boolean esgotada,
        Boolean ativa,
        List<String> tags
) {
    public static MarmitaResponse de(Marmita m) {
        return new MarmitaResponse(
                m.getId(), m.getNome(), m.getDescricao(), m.getPreco(), m.getCalorias(),
                m.getProteinas(), m.getCarboidratos(), m.getGorduras(),
                m.getEsgotada(), m.getAtiva(),
                m.getTags().stream().map(t -> t.getSlug()).toList()
        );
    }
}
