package br.com.fitmarmita.batch.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MarmitaCsvRecord {
    private String nome;
    private String descricao;
    private String preco;
    private String calorias;
    private String semanaReferencia;
}
