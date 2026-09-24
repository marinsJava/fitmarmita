package br.com.fitmarmita.cardapio.controller;

import br.com.fitmarmita.cardapio.dto.MarmitaResponse;
import br.com.fitmarmita.cardapio.dto.MarmitaResumoResponse;
import br.com.fitmarmita.cardapio.service.CardapioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Cardapio", description = "Cardapio semanal e detalhe de marmitas")
public class CardapioController {

    private final CardapioService service;

    @GetMapping("/cardapio/semanal")
    @Operation(summary = "Cardapio da semana atual (rota publica, filtro opcional por tags)")
    public ResponseEntity<List<MarmitaResumoResponse>> semanal(
            @RequestParam(required = false) String tags) {
        List<String> filtro = (tags == null || tags.isBlank())
                ? List.of()
                : Arrays.stream(tags.split(",")).map(String::trim).toList();
        return ResponseEntity.ok(service.semanal(filtro));
    }

    @GetMapping("/marmitas/{id}")
    @Operation(summary = "Detalha uma marmita (requer JWT). Rota tambem consumida pelo pedido-service.")
    public ResponseEntity<MarmitaResponse> detalhar(@PathVariable Long id) {
        return ResponseEntity.ok(service.detalhar(id));
    }
}