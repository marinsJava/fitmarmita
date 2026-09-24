package br.com.fitmarmita.usuario.controller;

import br.com.fitmarmita.usuario.dto.CadastroUsuarioRequest;
import br.com.fitmarmita.usuario.dto.UsuarioResponse;
import br.com.fitmarmita.usuario.entity.Usuario;
import br.com.fitmarmita.usuario.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse cadastrarUsuario(
            @Valid @RequestBody CadastroUsuarioRequest cadastroUsuarioRequest
    ){
        return usuarioService.cadastrar(cadastroUsuarioRequest);
    }
}
