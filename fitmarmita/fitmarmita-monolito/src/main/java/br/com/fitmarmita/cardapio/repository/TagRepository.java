package br.com.fitmarmita.cardapio.repository;

import br.com.fitmarmita.cardapio.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TagRepository extends JpaRepository<Tag, Long> {

    List<Tag> findBySlugIn(List<String> slugs);
}
