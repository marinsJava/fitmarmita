package br.com.fitmarmita.batch.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/batch")
@RequiredArgsConstructor
public class BatchController {

    private final JobLauncher jobLauncher;
    private final Job importarMarmitasJob;

    @PostMapping("/marmitas/importar")
    public ResponseEntity<Map<String, Object>> importarMarmitas() throws Exception {
        var jobParameters = new JobParametersBuilder()
                .addLong("run.id", System.currentTimeMillis())
                .toJobParameters();

        JobExecution execucao = jobLauncher.run(importarMarmitasJob, jobParameters);

        return ResponseEntity.ok(Map.of(
                "jobExecutionId", execucao.getId(),
                "status", execucao.getStatus().toString(),
                "itensLidos", execucao.getStepExecutions().stream().mapToLong(s -> s.getReadCount()).sum(),
                "itensGravados", execucao.getStepExecutions().stream().mapToLong(s -> s.getWriteCount()).sum(),
                "itensIgnorados", execucao.getStepExecutions().stream()
                        .mapToLong(s -> s.getReadCount() - s.getWriteCount()).sum()
        ));
    }
}
