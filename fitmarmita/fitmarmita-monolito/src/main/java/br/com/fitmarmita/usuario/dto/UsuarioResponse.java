package br.com.fitmarmita.usuario.dto;

import br.com.fitmarmita.usuario.entity.Usuario;

import java.time.LocalDateTime;

public record UsuarioResponse(
        Long id,
        String nome,
        String email,
        String telefone,
        Boolean ativo,
        LocalDateTime criadoEm
) {
    public static UsuarioResponse de(Usuario u) {
        return new UsuarioResponse(
                u.getId(), u.getNome(), u.getEmail(),
                u.getTelefone(), u.getAtivo(), u.getCriadoEm()
        );
    }
}
