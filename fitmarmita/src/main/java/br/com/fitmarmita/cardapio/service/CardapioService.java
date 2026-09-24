package br.com.fitmarmita.cardapio.service;

import br.com.fitmarmita.cardapio.dto.MarmitaResponse;
import br.com.fitmarmita.cardapio.entity.Marmita;
import br.com.fitmarmita.cardapio.repository.MarmitaRepository;
import br.com.fitmarmita.shared.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CardapioService {

    private final MarmitaRepository marmitaRepository;

    public List<MarmitaResponse> semanal(List<String> tags) {
        LocalDate semana = semanaAtual();
        List<Marmita> marmitas = (tags == null || tags.isEmpty())
                ? marmitaRepository.findBySemanaReferenciaAndAtivaTrue(semana)
                : marmitaRepository.findByTodasTags(semana, tags, tags.size());
        return marmitas.stream().map(MarmitaResponse::from).toList();
    }

    public MarmitaResponse detalhe(Long id) {
        Marmita marmita = buscarPorId(id);
        return MarmitaResponse.from(marmita);
    }

    public Marmita buscarPorId(Long id) {
        return marmitaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Marmita não encontrada"));
    }

    private LocalDate semanaAtual() {
        return LocalDate.now().with(DayOfWeek.MONDAY);
    }

}