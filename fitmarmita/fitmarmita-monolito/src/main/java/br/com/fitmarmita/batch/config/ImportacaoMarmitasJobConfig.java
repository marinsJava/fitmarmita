package br.com.fitmarmita.batch.config;

import br.com.fitmarmita.batch.dto.MarmitaCsvRecord;
import br.com.fitmarmita.batch.processor.MarmitaItemProcessor;
import br.com.fitmarmita.cardapio.entity.Marmita;
import br.com.fitmarmita.cardapio.repository.MarmitaRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class ImportacaoMarmitasJobConfig {

    private static final int CHUNK_SIZE = 5;

    @Value("${fitmarmita.batch.marmitas.arquivo:classpath:batch/marmitas-importacao.csv}")
    private Resource arquivoCsv;

    @Bean
    public FlatFileItemReader<MarmitaCsvRecord> marmitaItemReader() {
        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setNames("nome", "descricao", "preco", "calorias", "semanaReferencia");

        BeanWrapperFieldSetMapper<MarmitaCsvRecord> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(MarmitaCsvRecord.class);

        DefaultLineMapper<MarmitaCsvRecord> lineMapper = new DefaultLineMapper<>();
        lineMapper.setLineTokenizer(tokenizer);
        lineMapper.setFieldSetMapper(fieldSetMapper);

        return new FlatFileItemReaderBuilder<MarmitaCsvRecord>()
                .name("marmitaItemReader")
                .resource(arquivoCsv)
                .linesToSkip(1) // cabecalho: nome,descricao,preco,calorias,semanaReferencia
                .lineMapper(lineMapper)
                .build();
    }

    @Bean
    public RepositoryItemWriter<Marmita> marmitaItemWriter(MarmitaRepository marmitaRepository) {
        RepositoryItemWriter<Marmita> writer = new RepositoryItemWriter<>();
        writer.setRepository(marmitaRepository);
        writer.setMethodName("save");
        return writer;
    }

    @Bean
    public Step importarMarmitasStep(JobRepository jobRepository,
                                      PlatformTransactionManager transactionManager,
                                      FlatFileItemReader<MarmitaCsvRecord> marmitaItemReader,
                                      MarmitaItemProcessor marmitaItemProcessor,
                                      RepositoryItemWriter<Marmita> marmitaItemWriter) {
        return new StepBuilder("importarMarmitasStep", jobRepository)
                .<MarmitaCsvRecord, Marmita>chunk(CHUNK_SIZE, transactionManager)
                .reader(marmitaItemReader)
                .processor(marmitaItemProcessor)
                .writer(marmitaItemWriter)
                .build();
    }

    @Bean
    public Job importarMarmitasJob(JobRepository jobRepository, Step importarMarmitasStep) {
        return new JobBuilder("importarMarmitasJob", jobRepository)
                .start(importarMarmitasStep)
                .build();
    }
}
