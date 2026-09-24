package br.com.fitmarmita.usuario.entity;

import br.com.fitmarmita.pedido.entity.Pedido;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

@Entity
@Table(name = "usuario")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(nullable = false, length = 150, unique = true)
    private String email;

    @Column(nullable = false, length = 255)
    private String senhaHash;

    @Column(length = 20)
    private String telefone;

    @Column(nullable = false)
    @Builder.Default
    private Boolean ativo = Boolean.TRUE;

    @Column(nullable = false)
    @Builder.Default
    private Integer tentativasLogin = 0;

    private LocalDateTime bloqueadoAte;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "usuario")
    @Builder.Default
    private List<Pedido> pedidos = new ArrayList<>();

    @Override
    public String toString() {
        return "Usuario{" +
                "id=" + id +
                ", nome=" + nome +
                '}';
    }
}
