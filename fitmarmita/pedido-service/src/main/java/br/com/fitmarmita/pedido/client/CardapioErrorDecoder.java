package br.com.fitmarmita.pedido.client;

import br.com.fitmarmita.pedido.exception.NotFoundException;
import br.com.fitmarmita.pedido.exception.ServicoIndisponivelException;
import feign.Response;
import feign.codec.ErrorDecoder;

public class CardapioErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder padrao = new Default();


    @Override
    public Exception decode(String methodKey, Response response) {
        if (response.status() == 404) {
            return new NotFoundException("Marmita nao encontrada no catalogo");
        }
        if (response.status() >= 500) {
            return new ServicoIndisponivelException("Cardapio respondeu com erro interno");
        }
        return padrao.decode(methodKey, response);
    }
}
