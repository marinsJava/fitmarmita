package br.com.fitmarmita.endereco.service;

import br.com.fitmarmita.endereco.client.ViaCepClient;
import br.com.fitmarmita.endereco.dto.ViaCepResponse;
import br.com.fitmarmita.shared.exception.BusinessException;
import br.com.fitmarmita.shared.exception.NotFoundException;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EnderecoService {

    private final ViaCepClient viaCepClient;

    public ViaCepResponse buscarPorCep(String cep) {
        String cepLimpo = cep.replaceAll("\\D", "");

        if (cepLimpo.length() != 8) {
            throw new BusinessException("CEP deve conter 8 dígitos");
        }

        ViaCepResponse response;
        try {
            response = viaCepClient.buscarPorCep(cepLimpo);
        } catch (FeignException ex) {
            throw new BusinessException("Não foi possível consultar o CEP no momento");
        }

        if (response == null || Boolean.TRUE.equals(response.erro())) {
            throw new NotFoundException("CEP não encontrado");
        }

        return response;
    }
}