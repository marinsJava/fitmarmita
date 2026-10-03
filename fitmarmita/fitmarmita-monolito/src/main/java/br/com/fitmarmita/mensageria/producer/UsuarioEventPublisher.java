package br.com.fitmarmita.mensageria.producer;

import br.com.fitmarmita.mensageria.config.RabbitMQConfig;
import br.com.fitmarmita.mensageria.event.UsuarioCadastradoEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UsuarioEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publicarUsuarioCadastrado(Long usuarioId, String nome, String email) {
        UsuarioCadastradoEvent evento = UsuarioCadastradoEvent.de(usuarioId, nome, email);
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE,
                RabbitMQConfig.ROUTING_KEY_USUARIO_CADASTRADO,
                evento);
        log.info("Evento USUARIO_CADASTRADO publicado para o usuario {}", usuarioId);
    }
}
