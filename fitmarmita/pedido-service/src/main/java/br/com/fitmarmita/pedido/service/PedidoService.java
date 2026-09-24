package br.com.fitmarmita.pedido.service;

import br.com.fitmarmita.pedido.client.CardapioClient;
import br.com.fitmarmita.pedido.dto.CriarPedidoRequest;
import br.com.fitmarmita.pedido.dto.ItemPedidoRequest;
import br.com.fitmarmita.pedido.dto.PedidoResponse;
import br.com.fitmarmita.pedido.entity.ItemPedido;
import br.com.fitmarmita.pedido.entity.Pedido;
import br.com.fitmarmita.pedido.exception.BusinessException;
import br.com.fitmarmita.pedido.exception.NotFoundException;
import br.com.fitmarmita.pedido.repository.PedidoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PedidoService {

    private final PedidoRepository repository;
    private final CardapioClient cardapioClient;

    @Transactional
    public Pedido criar(Long usuarioId, CriarPedidoRequest request) {
        Pedido pedido = new Pedido();
        pedido.setUsuarioId(usuarioId);
        pedido.setFormaPagamento(request.formaPagamento());
        pedido.setEnderecoEntrega(request.enderecoEntrega());

        for (ItemPedidoRequest itemReq : request.itens()) {
            pedido.adicionarItem(montarItem(itemReq));
        }

        pedido.recalcularValorTotal();
        return repository.save(pedido);
    }

    private ItemPedido montarItem(ItemPedidoRequest req) {
        CardapioClient.MarmitaDto marmita = cardapioClient.buscarMarmita(req.marmitaId());

        if (marmita == null) {
            throw new NotFoundException("Marmita nao encontrada: " + req.marmitaId());
        }
        if (Boolean.TRUE.equals(marmita.esgotada()) || !Boolean.TRUE.equals(marmita.ativa())) {
            throw new BusinessException("Marmita indisponivel: " + marmita.nome());
        }

        ItemPedido item = new ItemPedido();
        item.setMarmitaId(marmita.id());
        item.setNomeMarmita(marmita.nome());
        item.setPrecoUnitario(marmita.preco());
        item.setCalorias(marmita.calorias());
        item.setQuantidade(req.quantidade());
        return item;
    }
    @Transactional(readOnly = true)
    public PedidoResponse buscarPorId(Long id, Long usuarioId) {
        Pedido pedido = repository.buscarComItensPorId(id)
                .orElseThrow(() -> new NotFoundException("Pedido nao encontrado: " + id));
        if (!pedido.getUsuarioId().equals(usuarioId)) {
            throw new NotFoundException("Pedido nao encontrado: " + id);
        }
        return PedidoResponse.de(pedido);
    }

    @Transactional(readOnly = true)
    public List<PedidoResponse> listarPorUsuario(Long usuarioId) {
        return repository.buscarComItensPorUsuarioId(usuarioId).stream()
                .map(PedidoResponse::de)
                .toList();
    }
}