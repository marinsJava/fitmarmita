package br.com.fitmarmita;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Ponto de entrada da aplicacao Spring Boot.
 *
 * A anotacao @SpringBootApplication combina tres outras:
 *   - @Configuration: marca a classe como fonte de beans
 *   - @EnableAutoConfiguration: Spring detecta dependencias no classpath e configura sozinho
 *   - @ComponentScan: escaneia pacotes a partir daqui em busca de @Component, @Service, etc
 *
 * Esta classe deve ficar no pacote raiz para que o ComponentScan encontre
 * todos os pacotes filhos automaticamente.
 */
@SpringBootApplication
@EnableFeignClients
public class FitMarmitaApplication {

    public static void main(String[] args) {
        SpringApplication.run(FitMarmitaApplication.class, args);
    }

}
