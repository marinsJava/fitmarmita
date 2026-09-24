package br.com.fitmarmita.pedido.exception;

public class DuplicateResourceException extends BusinessException {
    public DuplicateResourceException(String mensagem) {
        super(mensagem);
    }
}