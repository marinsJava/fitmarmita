package br.com.fitmarmita.mensageria.config;

import br.com.fitmarmita.mensageria.event.UsuarioCadastradoEvent;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.DefaultClassMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "fitmarmita.exchange";
    public static final String QUEUE_USUARIO_CADASTRADO = "usuario.cadastrado.queue";
    public static final String ROUTING_KEY_USUARIO_CADASTRADO = "usuario.cadastrado";

    private static final String TYPE_ID_USUARIO_CADASTRADO = "usuarioCadastrado";

    @Bean
    public TopicExchange fitmarmitaExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public Queue usuarioCadastradoQueue() {
        return new Queue(QUEUE_USUARIO_CADASTRADO);
    }

    @Bean
    public Binding usuarioCadastradoBinding(Queue usuarioCadastradoQueue, TopicExchange fitmarmitaExchange) {
        return BindingBuilder.bind(usuarioCadastradoQueue)
                .to(fitmarmitaExchange)
                .with(ROUTING_KEY_USUARIO_CADASTRADO);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        DefaultClassMapper classMapper = new DefaultClassMapper();
        classMapper.setTrustedPackages("*");
        classMapper.setIdClassMapping(Map.of(TYPE_ID_USUARIO_CADASTRADO, UsuarioCadastradoEvent.class));
        converter.setClassMapper(classMapper);
        return converter;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(org.springframework.amqp.rabbit.connection.ConnectionFactory connectionFactory,
                                          MessageConverter jsonMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter);
        return template;
    }
}
