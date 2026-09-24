package br.com.fitmarmita.cardapio.repository;

import br.com.fitmarmita.cardapio.entity.Marmita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface MarmitaRepository extends JpaRepository<Marmita, Long> {

    @Query("""
            SELECT DISTINCT m FROM Marmita m LEFT JOIN FETCH m.tags
            WHERE m.ativa = true AND m.semanaReferencia = :semana
            """)
    List<Marmita> findByAtivaTrueAndSemanaReferencia(LocalDate semana);

    @Query("""
            SELECT DISTINCT m FROM Marmita m JOIN FETCH m.tags t
            WHERE m.ativa = true AND m.semanaReferencia = :semana AND t.slug IN :slugs
            """)
    List<Marmita> findByAtivaTrueAndSemanaReferenciaAndTagSlugIn(LocalDate semana, List<String> slugs);

    @Query("""
            SELECT m FROM Marmita m LEFT JOIN FETCH m.tags
            WHERE m.id = :id
            """)
    Optional<Marmita> buscarComTagsPorId(Long id);
}