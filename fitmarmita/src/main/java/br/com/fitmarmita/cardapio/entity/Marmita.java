package br.com.fitmarmita.cardapio.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "marmita")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Marmita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nome;
    private String descricao;
    private BigDecimal preco;
    private int calorias;

    @Column(name = "proteinas_g")
    private BigDecimal proteinas;

    @Column(name = "carboidratos_g")
    private BigDecimal carboidratos;

    @Column(name = "gorduras_g")
    private BigDecimal gorduras;

    @Size(max = 500)
    @Column(name = "imagem_url", length = 500)
    private String imagemUrl;

    @Column(nullable = false)
    private Boolean esgotada;

    @Column(nullable = false)
    private Boolean ativa;

    @NotNull
    @Column(name = "semana_referencia", nullable = false)
    private LocalDate semanaReferencia;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "marmita_tag",
            joinColumns = @JoinColumn(name = "marmita_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    @Builder.Default
    private Set<Tag> tags = new HashSet<>();
}
