package br.com.fitmarmita.shared.exception;

public class DuplicateResourceException extends BusinessException {
    public DuplicateResourceException(String mensagem) {
        super(mensagem);
    }
}