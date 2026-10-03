package br.com.fitmarmita.pedido.cupom.repository;

import br.com.fitmarmita.pedido.cupom.entity.CupomBoasVindas;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CupomBoasVindasRepository extends JpaRepository<CupomBoasVindas, Long> {

    Optional<CupomBoasVindas> findByUsuarioId(Long usuarioId);

    boolean existsByUsuarioId(Long usuarioId);
}
