package br.com.fitmarmita.pedido.service;

import br.com.fitmarmita.pedido.entity.Carrinho;
import br.com.fitmarmita.pedido.entity.ItemCarrinho;
import br.com.fitmarmita.pedido.repository.CarrinhoRepository;
import br.com.fitmarmita.shared.exception.BusinessException;
import br.com.fitmarmita.shared.exception.NotFoundException;
import br.com.fitmarmita.usuario.entity.Usuario;
import br.com.fitmarmita.usuario.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CarrinhoService {

    private final CarrinhoRepository carrinhoRepository;
    private final UsuarioService usuarioService;

    @Transactional
    public Carrinho obterOuCriarCarrinho(Long usuarioId) {
        return carrinhoRepository.findByUsuarioId(usuarioId)
                .orElseGet(() -> criarCarrinho(usuarioId));
    }

    private Carrinho criarCarrinho(Long usuarioId) {
        Usuario usuario = usuarioService.obterPorId(usuarioId);
        Carrinho novoCarrinho = Carrinho.builder()
                .usuario(usuario)
                .valorTotal(BigDecimal.ZERO)
                .itens(new ArrayList<>())
                .build();
        return carrinhoRepository.save(novoCarrinho);
    }

    @Transactional
    public Carrinho adicionarItemCarrinho(Long usuarioId, ItemCarrinho novoItemCarrinho) {
        Carrinho carrinho = obterOuCriarCarrinho(usuarioId);
        novoItemCarrinho.setPrecoUnitario(novoItemCarrinho.getMarmita().getPreco());

        Optional<ItemCarrinho> itemExistente = carrinho.getItens().stream()
                .filter(item -> item.getMarmita().getId().equals(novoItemCarrinho.getMarmita().getId()))
                .findFirst();

        if (itemExistente.isPresent()) {
            itemExistente.get().setQuantidade(
                    itemExistente.get().getQuantidade() + novoItemCarrinho.getQuantidade()
            );
        } else {
            novoItemCarrinho.setCarrinho(carrinho);
            carrinho.getItens().add(novoItemCarrinho);
        }

        carrinho.setValorTotal(carrinho.calcularValorTotal());
        return carrinhoRepository.save(carrinho);
    }

    @Transactional
    public Carrinho atualizarItemCarrinho(Long usuarioId, Long itemId, Integer quantidade) {
        if (quantidade == null || quantidade <= 0) {
            throw new BusinessException("Quantidade deve ser maior que zero");
        }

        Carrinho carrinho = obterOuCriarCarrinho(usuarioId);

        ItemCarrinho item = carrinho.getItens().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Item não encontrado no carrinho"));

        item.setQuantidade(quantidade);
        carrinho.setValorTotal(carrinho.calcularValorTotal());

        return carrinhoRepository.save(carrinho);
    }

    @Transactional
    public Carrinho removerItemCarrinho(Long usuarioId, Long itemId) {
        Carrinho carrinho = obterOuCriarCarrinho(usuarioId);
        carrinho.getItens().removeIf(item -> item.getId().equals(itemId));
        carrinho.setValorTotal(carrinho.calcularValorTotal());
        return carrinhoRepository.save(carrinho);
    }

    @Transactional
    public void esvaziarCarrinho(Long usuarioId) {
        Carrinho carrinho = obterOuCriarCarrinho(usuarioId);
        carrinho.getItens().clear();
        carrinho.setValorTotal(BigDecimal.ZERO);
        carrinhoRepository.save(carrinho);
    }

    public Carrinho buscarCarrinho(Long usuarioId) {
        return obterOuCriarCarrinho(usuarioId);
    }
}