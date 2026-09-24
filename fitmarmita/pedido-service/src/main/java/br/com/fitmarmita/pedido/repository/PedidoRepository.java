package br.com.fitmarmita.pedido.repository;

import br.com.fitmarmita.pedido.entity.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    @Query("SELECT p FROM Pedido p LEFT JOIN FETCH p.itens WHERE p.id = :id")
    Optional<Pedido> buscarComItensPorId(Long id);

    @Query("""
            SELECT DISTINCT p FROM Pedido p LEFT JOIN FETCH p.itens
            WHERE p.usuarioId = :usuarioId
            ORDER BY p.criadoEm DESC
            """)
    List<Pedido> buscarComItensPorUsuarioId(Long usuarioId);
}