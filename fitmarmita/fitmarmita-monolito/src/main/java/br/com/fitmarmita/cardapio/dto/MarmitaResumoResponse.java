package br.com.fitmarmita.cardapio.dto;

import br.com.fitmarmita.cardapio.entity.Marmita;

import java.math.BigDecimal;
import java.util.List;

public record MarmitaResumoResponse(
        Long id,
        String nome,
        BigDecimal preco,
        Integer calorias,
        Boolean esgotada,
        List<String> tags
) {
    public static MarmitaResumoResponse de(Marmita m) {
        return new MarmitaResumoResponse(
                m.getId(), m.getNome(), m.getPreco(), m.getCalorias(), m.getEsgotada(),
                m.getTags().stream().map(t -> t.getSlug()).toList()
        );
    }
}
