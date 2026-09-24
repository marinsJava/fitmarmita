package br.com.fitmarmita.pedido.service;

import br.com.fitmarmita.pedido.dto.FinalizarCarrinhoRequest;
import br.com.fitmarmita.pedido.entity.*;
import br.com.fitmarmita.pedido.repository.PedidoRepository;
import br.com.fitmarmita.shared.exception.BusinessException;
import br.com.fitmarmita.shared.exception.NotFoundException;
import br.com.fitmarmita.usuario.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final UsuarioService usuarioService;
    private final CarrinhoService carrinhoService;

    @Transactional
    public Pedido finalizarCarrinho(Long usuarioId, FinalizarCarrinhoRequest request) {
        Carrinho carrinho = carrinhoService.obterOuCriarCarrinho(usuarioId);

        if (carrinho.getItens().isEmpty()) {
            throw new BusinessException("Carrinho vazio");
        }

        Pedido pedido = Pedido.builder()
                .usuario(usuarioService.obterPorId(usuarioId))
                .status(StatusPedido.AGUARDANDO_PAGAMENTO)
                .valorTotal(carrinho.calcularValorTotal())
                .enderecoEntrega(request.getEnderecoEntrega())
                .formaPagamento(request.getFormaPagamento())
                .observacoes(request.getObservacoes())
                .itens(new ArrayList<>())
                .build();

        List<ItemPedido> itensPedidos = converterItens(carrinho.getItens(), pedido);
        pedido.setItens(itensPedidos);

        Pedido pedidoSalvo = pedidoRepository.save(pedido);

        carrinhoService.esvaziarCarrinho(usuarioId);

        return pedidoSalvo;
    }

    public List<ItemPedido> converterItens(List<ItemCarrinho> itensCarrinho, Pedido pedido) {
        return itensCarrinho.stream()
                .map(itemCarrinho -> ItemPedido.builder()
                        .marmita(itemCarrinho.getMarmita())
                        .quantidade(itemCarrinho.getQuantidade())
                        .precoUnitario(itemCarrinho.getPrecoUnitario())
                        .pedido(pedido)
                        .build())
                .collect(Collectors.toList());
    }

    public Pedido buscarPedidoPorId(Long pedidoId) {
        return pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new NotFoundException("Pedido não encontrado"));
    }

    public List<Pedido> listarPedidosPorUsuario(Long usuarioId) {
        return pedidoRepository.findByUsuarioId(usuarioId);
    }

    public List<Pedido> listarTodosPedidos(StatusPedido status, String ordenarPor, String direcao) {
        Sort.Direction dir = Sort.Direction.fromString(direcao);
        Sort sort = Sort.by(dir, ordenarPor);

        if (status != null) {
            return pedidoRepository.findByStatus(status, sort);
        }
        return pedidoRepository.findAll(sort);
    }

    @Transactional
    public Pedido atualizarStatus(Long pedidoId, StatusPedido novoStatus) {
        Pedido pedido = buscarPedidoPorId(pedidoId);
        StatusPedido statusAtual = pedido.getStatus();

        if (!statusAtual.podeTransicionarPara(novoStatus)) {
            throw new BusinessException(
                    "Transição inválida: não é possível mudar de %s para %s"
                            .formatted(statusAtual, novoStatus)
            );
        }

        pedido.setStatus(novoStatus);
        return pedidoRepository.save(pedido);
    }

    @Transactional
    public Pedido cancelarPedido(Long pedidoId) {
        Pedido pedido = buscarPedidoPorId(pedidoId);

        if (pedido.getStatus() != StatusPedido.AGUARDANDO_PAGAMENTO) {
            throw new BusinessException("Não é possível cancelar um pedido que não está aguardando pagamento");
        }

        pedido.setStatus(StatusPedido.CANCELADO);
        return pedidoRepository.save(pedido);
    }

    @Transactional
    public Pedido confirmarPagamento(Long pedidoId) {
        Pedido pedido = buscarPedidoPorId(pedidoId);

        if (pedido.getStatus() != StatusPedido.AGUARDANDO_PAGAMENTO) {
            throw new BusinessException("Não é possível confirmar o pagamento de um pedido que não está aguardando pagamento");
        }

        pedido.setStatus(StatusPedido.PAGO);
        return pedidoRepository.save(pedido);
    }

    public BigDecimal calcularValorTotal(Pedido pedido) {
        return pedido.getItens().stream()
                .map(item -> item.getPrecoUnitario().multiply(BigDecimal.valueOf(item.getQuantidade())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}