package br.com.fitmarmita.pedido.mensageria.event;

import java.time.LocalDateTime;

public record UsuarioCadastradoEvent(
        String tipo,
        Long identificador,
        String nome,
        String email,
        LocalDateTime dataCadastro
) {
}
