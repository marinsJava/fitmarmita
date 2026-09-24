package br.com.fitmarmita.pedido.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdicionarItemRequest {

    @NotNull
    private Long marmitaId;

    @NotNull
    @Positive
    private Integer quantidade;
}
