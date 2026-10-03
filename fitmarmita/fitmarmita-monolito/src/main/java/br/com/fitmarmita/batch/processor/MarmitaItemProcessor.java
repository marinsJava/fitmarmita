package br.com.fitmarmita.batch.processor;

import br.com.fitmarmita.batch.dto.MarmitaCsvRecord;
import br.com.fitmarmita.cardapio.entity.Marmita;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAdjusters;

/**
 * Regra de negocio do Job de importacao:
 * <ul>
 *     <li>nome e preco sao obrigatorios e precisam ser validos - registro
 *     invalido e IGNORADO (retorna null), nao interrompe o Job inteiro;</li>
 *     <li>nome e descricao sao normalizados (espacos em excesso removidos);</li>
 *     <li>a semana de referencia informada no CSV e normalizada para a
 *     segunda-feira daquela semana, do mesmo jeito que o cadastro manual de
 *     marmitas ja faz (ver V002__marmita_teste.sql, que usa
 *     {@code date_trunc('week', ...)} no Postgres).</li>
 * </ul>
 */
@Slf4j
@Component
public class MarmitaItemProcessor implements ItemProcessor<MarmitaCsvRecord, Marmita> {

    private static final DateTimeFormatter DATA_CSV = DateTimeFormatter.ISO_LOCAL_DATE;

    @Override
    public Marmita process(MarmitaCsvRecord item) {
        String nome = normalizarTexto(item.getNome());
        if (nome == null || nome.isBlank()) {
            log.warn("Registro ignorado: nome em branco ({})", item);
            return null;
        }

        BigDecimal preco = parsePreco(item.getPreco());
        if (preco == null || preco.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("Registro ignorado: preco invalido para '{}' ({})", nome, item.getPreco());
            return null;
        }

        LocalDate semana = parseSemana(item.getSemanaReferencia());
        if (semana == null) {
            log.warn("Registro ignorado: semanaReferencia invalida para '{}' ({})", nome, item.getSemanaReferencia());
            return null;
        }

        Marmita marmita = new Marmita();
        marmita.setNome(nome);
        marmita.setDescricao(normalizarTexto(item.getDescricao()));
        marmita.setPreco(preco);
        marmita.setCalorias(parseCalorias(item.getCalorias()));
        marmita.setEsgotada(false);
        marmita.setAtiva(true);
        marmita.setSemanaReferencia(semana.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)));
        return marmita;
    }

    private String normalizarTexto(String texto) {
        if (texto == null) {
            return null;
        }
        String normalizado = texto.trim().replaceAll("\\s+", " ");
        return normalizado.isBlank() ? null : normalizado;
    }

    private BigDecimal parsePreco(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(texto.trim().replace(",", "."));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer parseCalorias(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        try {
            return Integer.valueOf(texto.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private LocalDate parseSemana(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(texto.trim(), DATA_CSV);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
