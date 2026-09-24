package br.com.fitmarmita.pedido.controller;

import br.com.fitmarmita.pedido.dto.AtualizarStatusRequest;
import br.com.fitmarmita.pedido.dto.FinalizarCarrinhoRequest;
import br.com.fitmarmita.pedido.dto.PedidoResponse;
import br.com.fitmarmita.pedido.entity.Pedido;
import br.com.fitmarmita.pedido.entity.StatusPedido;
import br.com.fitmarmita.pedido.service.PedidoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/pedidos")
@RequiredArgsConstructor
public class PedidoController {

    private final PedidoService pedidoService;

    @PostMapping
    public ResponseEntity<PedidoResponse> finalizarCarrinho(
            @RequestParam Long usuarioId,
            @RequestBody @Valid FinalizarCarrinhoRequest request) {
        Pedido pedido = pedidoService.finalizarCarrinho(usuarioId, request);
        return ResponseEntity
                .created(URI.create("/api/v1/pedidos/" + pedido.getId()))
                .body(PedidoResponse.from(pedido));
    }

    @GetMapping("/{pedidoId}")
    public ResponseEntity<PedidoResponse> buscarPedidoPorId(@PathVariable Long pedidoId) {
        Pedido pedido = pedidoService.buscarPedidoPorId(pedidoId);
        return ResponseEntity.ok(PedidoResponse.from(pedido));
    }

    @GetMapping
    public ResponseEntity<List<PedidoResponse>> listarPedidosPorUsuario(@RequestParam Long usuarioId) {
        List<PedidoResponse> pedidos = pedidoService.listarPedidosPorUsuario(usuarioId).stream()
                .map(PedidoResponse::from)
                .toList();
        return ResponseEntity.ok(pedidos);
    }

    @PatchMapping("/{pedidoId}/status")
    public ResponseEntity<PedidoResponse> atualizarStatus(@PathVariable Long pedidoId, @RequestBody @Valid AtualizarStatusRequest request) {
        Pedido pedido = pedidoService.atualizarStatus(pedidoId, request.getNovoStatus());
        return ResponseEntity.ok(PedidoResponse.from(pedido));
    }

    @PostMapping("/{pedidoId}/confirmar-pagamento")
    public ResponseEntity<PedidoResponse> confirmarPagamento(@PathVariable Long pedidoId) {
        Pedido pedido = pedidoService.confirmarPagamento(pedidoId);
        return ResponseEntity.ok(PedidoResponse.from(pedido));
    }

    @PostMapping("/{pedidoId}/cancelar")
    public ResponseEntity<PedidoResponse> cancelarPedido(@PathVariable Long pedidoId) {
        Pedido pedido = pedidoService.cancelarPedido(pedidoId);
        return ResponseEntity.ok(PedidoResponse.from(pedido));
    }


    @GetMapping("/admin/todos")
    public ResponseEntity<List<PedidoResponse>> listarTodosPedidos(
            @RequestParam(required = false) StatusPedido status,
            @RequestParam(defaultValue = "createdAt") String ordenarPor,
            @RequestParam(defaultValue = "DESC") String direcao) {

        List<PedidoResponse> pedidos = pedidoService.listarTodosPedidos(status, ordenarPor, direcao)
                .stream()
                .map(PedidoResponse::from)
                .toList();

        return ResponseEntity.ok(pedidos);
    }
}