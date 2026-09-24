package br.com.fitmarmita.usuario.controller;

import br.com.fitmarmita.usuario.dto.CriarUsuarioRequest;
import br.com.fitmarmita.usuario.dto.UsuarioResponse;
import br.com.fitmarmita.usuario.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor
@Tag(name = "Usuarios", description = "Cadastro de usuarios")
public class UsuarioController {

    private final UsuarioService service;

    @PostMapping
    @Operation(summary = "Cadastra um usuario (rota publica)")
    public ResponseEntity<UsuarioResponse> criar(@RequestBody @Valid CriarUsuarioRequest request,
                                                 UriComponentsBuilder uriBuilder) {
        var usuario = service.criar(request);
        URI uri = uriBuilder.path("/api/v1/usuarios/{id}").buildAndExpand(usuario.getId()).toUri();
        return ResponseEntity.created(uri).body(UsuarioResponse.de(usuario));
    }
}
