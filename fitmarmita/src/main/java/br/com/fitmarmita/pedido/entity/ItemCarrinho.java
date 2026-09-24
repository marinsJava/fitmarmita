package br.com.fitmarmita.pedido.entity;

import br.com.fitmarmita.cardapio.entity.Marmita;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Check;

@Entity
@Table(name = "item_carrinho")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ItemCarrinho extends Item {

    @ManyToOne
    @JoinColumn(name = "carrinho_id", foreignKey = @ForeignKey(name = "fk_item_carrinho"), nullable = false)
    private Carrinho carrinho;


}
