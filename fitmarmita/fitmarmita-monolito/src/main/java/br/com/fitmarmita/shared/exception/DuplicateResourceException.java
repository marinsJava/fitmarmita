package br.com.fitmarmita.shared.exception;

public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String mensagem) {
        super(mensagem);
    }
}
