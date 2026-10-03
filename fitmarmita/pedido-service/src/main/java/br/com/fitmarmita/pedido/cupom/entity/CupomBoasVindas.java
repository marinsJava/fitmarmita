package br.com.fitmarmita.pedido.cupom.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "cupons_boas_vindas")
@Getter
@Setter
@NoArgsConstructor
public class CupomBoasVindas {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(nullable = false, unique = true, length = 20)
    private String codigo;

    @Column(name = "percentual_desconto", nullable = false)
    private Integer percentualDesconto;

    @Column(nullable = false)
    private Boolean utilizado = false;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @PrePersist
    void aoCriar() {
        this.criadoEm = LocalDateTime.now();
    }
}
