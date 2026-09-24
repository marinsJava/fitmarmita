package br.com.fitmarmita.pedido.controller;

import br.com.fitmarmita.pedido.dto.CriarPedidoRequest;
import br.com.fitmarmita.pedido.dto.PedidoResponse;
import br.com.fitmarmita.pedido.service.PedidoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/pedidos")
@RequiredArgsConstructor
@Tag(name = "Pedidos", description = "Criacao e consulta de pedidos (requer JWT)")
public class PedidoController {

    private final PedidoService service;

    @PostMapping
    @Operation(summary = "Cria um pedido para o usuario autenticado")
    public ResponseEntity<PedidoResponse> criar(@RequestBody @Valid CriarPedidoRequest request,
                                                Authentication auth,
                                                UriComponentsBuilder uriBuilder) {
        Long usuarioId = usuarioIdDe(auth);
        var pedido = service.criar(usuarioId, request);
        URI uri = uriBuilder.path("/api/v1/pedidos/{id}").buildAndExpand(pedido.getId()).toUri();
        return ResponseEntity.created(uri).body(PedidoResponse.de(pedido));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalha um pedido do usuario autenticado")
    public ResponseEntity<PedidoResponse> detalhar(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(service.buscarPorId(id, usuarioIdDe(auth)));
    }

    @GetMapping
    @Operation(summary = "Lista os pedidos do usuario autenticado")
    public ResponseEntity<List<PedidoResponse>> listar(Authentication auth) {
        return ResponseEntity.ok(service.listarPorUsuario(usuarioIdDe(auth)));
    }

    private Long usuarioIdDe(Authentication auth) {
        return Long.valueOf(auth.getName());
    }
}