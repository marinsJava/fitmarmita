package br.com.fitmarmita.cardapio.repository;

import br.com.fitmarmita.cardapio.entity.Marmita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface MarmitaRepository extends JpaRepository<Marmita, Long> {

    List<Marmita> findBySemanaReferenciaAndAtivaTrue(LocalDate semana);

    @Query("""
      SELECT DISTINCT m FROM Marmita m
      JOIN m.tags t
      WHERE m.semanaReferencia = :semana
        AND m.ativa = true
        AND t.slug IN :tagsSlugs
      GROUP BY m
      HAVING COUNT(DISTINCT t.slug) = :totalTags
  """)
    List<Marmita> findByTodasTags(LocalDate semana, List<String> tagsSlugs, long totalTags);
}
