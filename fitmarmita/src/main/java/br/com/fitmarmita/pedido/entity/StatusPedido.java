package br.com.fitmarmita.pedido.entity;

import java.util.EnumSet;
import java.util.Set;

public enum StatusPedido {

    AGUARDANDO_PAGAMENTO {
        @Override
        public Set<StatusPedido> transicoesPermitidas() {
            return EnumSet.of(PAGO, CANCELADO);
        }
    },
    PAGO {
        @Override
        public Set<StatusPedido> transicoesPermitidas() {
            return EnumSet.of(EM_PREPARO, CANCELADO);
        }
    },
    EM_PREPARO {
        @Override
        public Set<StatusPedido> transicoesPermitidas() {
            return EnumSet.of(EM_ENTREGA, CANCELADO);
        }
    },
    EM_ENTREGA {
        @Override
        public Set<StatusPedido> transicoesPermitidas() {
            return EnumSet.of(ENTREGUE);
        }
    },
    ENTREGUE {
        @Override
        public Set<StatusPedido> transicoesPermitidas() {
            return EnumSet.noneOf(StatusPedido.class); // estado final, sem transições
        }
    },
    CANCELADO {
        @Override
        public Set<StatusPedido> transicoesPermitidas() {
            return EnumSet.noneOf(StatusPedido.class); // estado final, sem transições
        }
    };

    public abstract Set<StatusPedido> transicoesPermitidas();

    public boolean podeTransicionarPara(StatusPedido novoStatus) {
        return transicoesPermitidas().contains(novoStatus);
    }
}
