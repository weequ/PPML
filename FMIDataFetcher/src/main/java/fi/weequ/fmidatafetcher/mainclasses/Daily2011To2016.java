package fi.weequ.fmidatafetcher.mainclasses;

import fi.weequ.fmidatafetcher.DailyMultipointCoverageFetcher;
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
public class Daily2011To2016 {
    public static void main(String[] args) {
        SpringApplication.run(Daily2011To2016.class);
    }
    
    @Bean
    public CommandLineRunner demo(WeatherObservationRepository repository, DailyMultipointCoverageFetcher dmcf) {
        return (args) -> {
            for (int year = 2004; year <= 2016; year++) {
                try(InputStream is = dmcf.fetch(year+"-01-01T00:00:00Z", year+"-12-31T00:00:00Z")) {
                    dmcf.parse(is, year+"-01-01T00:00:00Z");
                } catch(Exception ex) {
                    ex.printStackTrace();
                }

            }
        };
    }
}
