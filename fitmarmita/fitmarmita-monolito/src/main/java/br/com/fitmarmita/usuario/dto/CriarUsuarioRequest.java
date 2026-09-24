package br.com.fitmarmita.usuario.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CriarUsuarioRequest(

        @NotBlank(message = "O nome e obrigatorio")
        @Size(min = 3, max = 120, message = "O nome deve ter entre 3 e 120 caracteres")
        String nome,

        @NotBlank(message = "O e-mail e obrigatorio")
        @Email(message = "E-mail invalido")
        @Size(max = 180)
        String email,

        @NotBlank(message = "A senha e obrigatoria")
        @Size(min = 8, max = 72, message = "A senha deve ter no minimo 8 caracteres")
        String senha,

        @Size(max = 20)
        String telefone
) {}
