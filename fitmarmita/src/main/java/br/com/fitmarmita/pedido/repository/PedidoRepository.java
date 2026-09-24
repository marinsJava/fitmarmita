package br.com.fitmarmita.pedido.repository;

import br.com.fitmarmita.pedido.entity.Pedido;
import br.com.fitmarmita.pedido.entity.StatusPedido;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    List<Pedido> findByUsuarioId(Long usuarioId);
    List<Pedido> findByStatus(StatusPedido status, Sort sort);
    List<Pedido> findAll(Sort sort);
}