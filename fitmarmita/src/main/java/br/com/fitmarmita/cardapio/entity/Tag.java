package br.com.fitmarmita.cardapio.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tag")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, unique = true, length = 50)
    private String nome;

    @NotBlank
    @Column(nullable = false, unique = true, length = 50)
    private String slug;

    @ManyToMany(mappedBy = "tags")
    private Set<Marmita> marmitas = new HashSet<>();
}
