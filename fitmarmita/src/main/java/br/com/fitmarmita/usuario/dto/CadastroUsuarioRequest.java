package br.com.fitmarmita.usuario.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CadastroUsuarioRequest (
    @NotBlank @Size(min = 3) String nome,
    @NotBlank @Email String email,
    @NotBlank @Size(min = 8) String senha,
    String telefone
) {}
