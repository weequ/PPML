package fi.weequ.fmidatafetcher;

import fi.weequ.ppmlbackend.repository.WeatherObservationRepository;
import java.io.InputStream;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@ComponentScan("fi.weequ")
@Configuration
@EnableJpaRepositories(basePackages = {"fi.weequ.ppmlbackend.repository"})
@EnableAutoConfiguration
@EnableTransactionManagement
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class);
    }
    
    @Bean
    public CommandLineRunner demo(WeatherObservationRepository repository, MultipointCoverageFetcher mcf) {
            return (args) -> {
                    try(InputStream is = mcf.fetch()) {
                        mcf.parse(is);
                    } catch(Exception ex) {
                        ex.printStackTrace();
                    }

            };
    }
}
