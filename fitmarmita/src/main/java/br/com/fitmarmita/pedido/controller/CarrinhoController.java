package br.com.fitmarmita.pedido.controller;

import br.com.fitmarmita.pedido.dto.AdicionarItemRequest;
import br.com.fitmarmita.pedido.dto.AtualizarQuantidadeRequest;
import br.com.fitmarmita.cardapio.entity.Marmita;
import br.com.fitmarmita.cardapio.service.CardapioService;
import br.com.fitmarmita.pedido.dto.CarrinhoResponse;
import br.com.fitmarmita.pedido.entity.Carrinho;
import br.com.fitmarmita.pedido.entity.ItemCarrinho;
import br.com.fitmarmita.pedido.service.CarrinhoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping("/api/v1/carrinho")
@RequiredArgsConstructor
public class CarrinhoController {

    private final CarrinhoService carrinhoService;
    private final CardapioService cardapioService;

    @GetMapping("/{usuarioId}")
    public ResponseEntity<CarrinhoResponse> listarCarrinho(@PathVariable Long usuarioId) {
        Carrinho carrinho = carrinhoService.obterOuCriarCarrinho(usuarioId);

        return ResponseEntity.ok(
                CarrinhoResponse.from(carrinho));
    }

    @PostMapping("/{usuarioId}/itens")
    public ResponseEntity<CarrinhoResponse> adicionarItemCarrinho(
            @PathVariable Long usuarioId,
            @RequestBody @Valid AdicionarItemRequest request) {

        Marmita marmita = cardapioService.buscarPorId(request.getMarmitaId());

        ItemCarrinho novoItem = ItemCarrinho.builder()
                .marmita(marmita)
                .quantidade(request.getQuantidade())
                .build();

        Carrinho carrinho = carrinhoService.adicionarItemCarrinho(usuarioId, novoItem);
        return ResponseEntity.status(HttpStatus.CREATED).body(CarrinhoResponse.from(carrinho));
    }

    @PatchMapping("/{usuarioId}/itens/{itemId}")
    public ResponseEntity<CarrinhoResponse> atualizarQuantidadeItemCarrinho(
            @PathVariable Long usuarioId,
            @PathVariable Long itemId,
            @RequestBody @Valid AtualizarQuantidadeRequest request) {

        Carrinho carrinho = carrinhoService.atualizarItemCarrinho(usuarioId, itemId, request.getQuantidade());
        return ResponseEntity.ok(CarrinhoResponse.from(carrinho));
    }

    @DeleteMapping("/{usuarioId}/itens/{itemId}")
    public ResponseEntity<Void> removerItemCarrinho(
            @PathVariable Long usuarioId,
            @PathVariable Long itemId) {
        carrinhoService.removerItemCarrinho(usuarioId, itemId);
        return ResponseEntity.noContent().build();
    }
        @DeleteMapping("/{usuarioId}")
        public ResponseEntity<Void> esvaziarCarrinho(@PathVariable Long usuarioId) {
            carrinhoService.esvaziarCarrinho(usuarioId);
            return ResponseEntity.noContent().build();
    }
}
