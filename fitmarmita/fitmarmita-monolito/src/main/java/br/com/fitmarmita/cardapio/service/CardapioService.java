package br.com.fitmarmita.cardapio.service;

import br.com.fitmarmita.cardapio.dto.MarmitaResponse;
import br.com.fitmarmita.cardapio.dto.MarmitaResumoResponse;
import br.com.fitmarmita.cardapio.repository.MarmitaRepository;
import br.com.fitmarmita.shared.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CardapioService {

    private final MarmitaRepository repository;

    @Transactional(readOnly = true)
    public List<MarmitaResumoResponse> semanal(List<String> tags) {
        LocalDate inicioDaSemana = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        var marmitas = (tags == null || tags.isEmpty())
                ? repository.findByAtivaTrueAndSemanaReferencia(inicioDaSemana)
                : repository.findByAtivaTrueAndSemanaReferenciaAndTagSlugIn(inicioDaSemana, tags);
        return marmitas.stream().map(MarmitaResumoResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public MarmitaResponse detalhar(Long id) {
        var marmita = repository.buscarComTagsPorId(id)
                .orElseThrow(() -> new NotFoundException("Marmita nao encontrada: " + id));
        return MarmitaResponse.de(marmita);
    }
}