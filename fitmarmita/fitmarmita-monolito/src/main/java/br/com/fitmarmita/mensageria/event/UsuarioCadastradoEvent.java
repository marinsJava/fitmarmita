package br.com.fitmarmita.mensageria.event;

import java.time.LocalDateTime;

public record UsuarioCadastradoEvent(
        String tipo,
        Long identificador,
        String nome,
        String email,
        LocalDateTime dataCadastro
) {
    public static UsuarioCadastradoEvent de(Long usuarioId, String nome, String email) {
        return new UsuarioCadastradoEvent("USUARIO_CADASTRADO", usuarioId, nome, email, LocalDateTime.now());
    }
}
