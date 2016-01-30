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
        System.out.println("lol");
    }
    
    @Bean
    public CommandLineRunner demo(WeatherObservationRepository repository, MultipointCoverageFetcher mcf) {
            return (args) -> {
                    System.out.println("testing");
                    if (repository == null) {
                        System.out.println("repository is null");
                    } else {
                        System.out.println("repository is not null");
                    }
                    try(InputStream is = mcf.fetch()) {
                        mcf.parse(is);
                    } catch(Exception ex) {
                        ex.printStackTrace();
                    }
                    //for (WeatherObservation observation : repository.findAll()) {
                    //        System.out.println(observation);
                    //}

            };
    }
}
