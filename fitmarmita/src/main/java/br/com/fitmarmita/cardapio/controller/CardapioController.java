package br.com.fitmarmita.cardapio.controller;

import br.com.fitmarmita.cardapio.dto.MarmitaResponse;
import br.com.fitmarmita.cardapio.service.CardapioService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cardapio")
@RequiredArgsConstructor
public class CardapioController {

    private final CardapioService cardapioService;

    @GetMapping("/semanal")
    public List<MarmitaResponse> listarCardapioSemanal(@RequestParam(required = false)List<String> tags) {
        return cardapioService.semanal(tags);
    }

}

