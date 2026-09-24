package br.com.fitmarmita.cardapio.dto;

import br.com.fitmarmita.cardapio.entity.Marmita;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record MarmitaResponse (
        Long id,
        String nome,
        String descricao,
        BigDecimal preco,
        int calorias,
        BigDecimal proteinas,
        BigDecimal carboidratos,
        BigDecimal gorduras,
        String imagemUrl,
        Boolean esgotada,
        LocalDate semanaReferencia

) {
    public static MarmitaResponse from(Marmita m) {
        return new MarmitaResponse(
                m.getId(),
                m.getNome(),
                m.getDescricao(),
                m.getPreco(),
                m.getCalorias(),
                m.getProteinas(),
                m.getCarboidratos(),
                m.getGorduras(),
                m.getImagemUrl(),
                m.getEsgotada(),
                m.getSemanaReferencia()
        );
    }

}
