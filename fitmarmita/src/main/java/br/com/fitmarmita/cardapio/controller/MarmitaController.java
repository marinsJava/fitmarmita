package br.com.fitmarmita.cardapio.controller;

import br.com.fitmarmita.cardapio.dto.MarmitaResponse;
import br.com.fitmarmita.cardapio.service.CardapioService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/marmita")
@RequiredArgsConstructor
public class MarmitaController {

    private final CardapioService cardapioService;

    @GetMapping("/{id}")
    public MarmitaResponse detalhe(@PathVariable Long id) {
        return cardapioService.detalhe(id);
    }
}