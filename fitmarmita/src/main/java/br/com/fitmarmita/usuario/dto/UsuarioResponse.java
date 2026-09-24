package br.com.fitmarmita.usuario.dto;

import br.com.fitmarmita.usuario.entity.Usuario;

import java.time.LocalDateTime;

public record UsuarioResponse(
        Long id,
        String nome,
        String email,
        String senhaHash,
        String telefone,
        Boolean ativo,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static UsuarioResponse from(Usuario u) {
        return new UsuarioResponse(
                u.getId(),
                u.getNome(),
                u.getEmail(),
                u.getSenhaHash(),
                u.getTelefone(),
                u.getAtivo(),
                u.getCreatedAt(),
                u.getUpdatedAt()
        );
    }
}
