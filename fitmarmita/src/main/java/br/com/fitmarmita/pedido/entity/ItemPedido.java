package br.com.fitmarmita.pedido.entity;

import br.com.fitmarmita.cardapio.entity.Marmita;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Check;

import java.math.BigDecimal;

@Entity
@Table(name = "item_pedido")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ItemPedido extends Item {

    @ManyToOne
    @JoinColumn(name = "pedido_id", foreignKey = @ForeignKey(name = "fk_item_pedido"))
    private Pedido pedido;


}
