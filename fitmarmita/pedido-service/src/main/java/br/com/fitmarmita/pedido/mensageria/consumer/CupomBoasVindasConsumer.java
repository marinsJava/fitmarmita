package br.com.fitmarmita.pedido.mensageria.consumer;

import br.com.fitmarmita.pedido.cupom.entity.CupomBoasVindas;
import br.com.fitmarmita.pedido.cupom.repository.CupomBoasVindasRepository;
import br.com.fitmarmita.pedido.mensageria.config.RabbitMQConfig;
import br.com.fitmarmita.pedido.mensageria.event.UsuarioCadastradoEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class CupomBoasVindasConsumer {

    private static final int PERCENTUAL_PADRAO = 10;

    private final CupomBoasVindasRepository repository;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_USUARIO_CADASTRADO)
    @Transactional
    public void aoReceberUsuarioCadastrado(UsuarioCadastradoEvent evento) {
        log.info("Evento recebido: {} (usuario {}, email {})",
                evento.tipo(), evento.identificador(), evento.email());

        if (repository.existsByUsuarioId(evento.identificador())) {
            log.info("Usuario {} ja possui cupom de boas-vindas, ignorando", evento.identificador());
            return;
        }

        CupomBoasVindas cupom = new CupomBoasVindas();
        cupom.setUsuarioId(evento.identificador());
        cupom.setCodigo(gerarCodigo());
        cupom.setPercentualDesconto(PERCENTUAL_PADRAO);
        cupom.setUtilizado(false);
        repository.save(cupom);

        log.info("Cupom {} ({}% de desconto) gerado para {} <{}>",
                cupom.getCodigo(), PERCENTUAL_PADRAO, evento.nome(), evento.email());
    }

    private String gerarCodigo() {
        return "BEMVINDO-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
