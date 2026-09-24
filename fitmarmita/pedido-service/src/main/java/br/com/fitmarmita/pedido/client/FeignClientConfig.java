package br.com.fitmarmita.pedido.client;

import feign.Logger;
import feign.Request;
import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.concurrent.TimeUnit;

/**
 * Configuracao especifica do CardapioClient. NAO leva @Configuration:
 * se levasse, o Spring aplicaria estes beans a TODOS os FeignClients
 * do serviço, nao so a este.
 */
public class FeignClientConfig {

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;
    }

    @Bean
    public Request.Options requestOptions() {
        // conexao curta: se o cardapio nao responder rapido, falha e deixa
        // o pedido cair no 503 em vez de travar a criacao do pedido.
        return new Request.Options(2, TimeUnit.SECONDS, 3, TimeUnit.SECONDS, true);
    }

    @Bean
    public ErrorDecoder errorDecoder() {
        return new CardapioErrorDecoder();
    }

    @Bean
    public RequestInterceptor authorizationForwardingInterceptor() {
        return requestTemplate -> {
            var attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes == null) {
                return;
            }
            String authorization = attributes.getRequest().getHeader("Authorization");
            if (authorization != null) {
                requestTemplate.header("Authorization", authorization);
            }
        };
    }
}