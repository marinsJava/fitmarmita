package br.com.fitmarmita.auth.dto;

public record TokenResponse(String token, String tipo) {
    public static TokenResponse de(String token) {
        return new TokenResponse(token, "Bearer");
    }
}
