package br.com.fitmarmita.cardapio.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "marmitas")
@Getter
@Setter
@NoArgsConstructor
public class Marmita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(length = 500)
    private String descricao;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal preco;

    private Integer calorias;
    private BigDecimal proteinas;
    private BigDecimal carboidratos;
    private BigDecimal gorduras;

    @Column(name = "imagem_url", length = 300)
    private String imagemUrl;

    @Column(nullable = false)
    private Boolean esgotada = false;

    @Column(nullable = false)
    private Boolean ativa = true;

    @Column(name = "semana_referencia", nullable = false)
    private LocalDate semanaReferencia;

    @ManyToMany
    @JoinTable(
            name = "marmita_tag",
            joinColumns = @JoinColumn(name = "marmita_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @Column(name = "atualizado_em")
    private LocalDateTime atualizadoEm;

    @PrePersist
    void aoCriar() {
        this.criadoEm = LocalDateTime.now();
    }

    @PreUpdate
    void aoAtualizar() {
        this.atualizadoEm = LocalDateTime.now();
    }
}
