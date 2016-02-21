package fi.weequ.fmidatafetcher.mainclasses;

import com.opencsv.CSVWriter;
import fi.weequ.fmidatafetcher.DailyMultipointCoverageFetcher;
import fi.weequ.fmidatafetcher.MastMultipointCoverageFetcher;
import fi.weequ.ppmlbackend.repository.WeatherObservationRepository;
import java.io.FileWriter;
import java.io.InputStream;
import org.joda.time.DateTime;
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
@Deprecated
public class Application {
    
    public static CSVWriter writer = null;
    
    
    public static void main(String[] args) {
        SpringApplication.run(Application.class);
    }
    
    
    private static String jodaToFMIDate(DateTime jodaTime) {
        //jodaTime.toString("yyyy-MM-dd'T'HH:mm:ss'Z'");
        return jodaTime.toString("yyyy-MM-dd'T'HH:mm:ss'Z'");//jodaTime.getYear()+"-"+jodaTime.getMonthOfYear()+"-"+jodaTime.getDayOfMonth()+"T00:00:00Z";
    }
    
    @Bean
    public CommandLineRunner demo(WeatherObservationRepository repository, DailyMultipointCoverageFetcher dmcf, MastMultipointCoverageFetcher mmcf) {
//            return (args) -> {
//                    for (int year = 2004; year <= 2016; year++) {
//                        try(InputStream is = mcf.fetch(year+"-01-01T00:00:00Z", year+"-12-31T00:00:00Z")) {
//                            mcf.parse(is, year+"-01-01T00:00:00Z");
//                        } catch(Exception ex) {
//                            ex.printStackTrace();
//                        }
//
//                    }
//
//
//            };
        return (args) -> {
            writer = new CSVWriter(new FileWriter("espoo lämpötilat.csv"), ',');
            
            DateTime startDate = new DateTime().withYear(2014)
                    .monthOfYear()
                    .withMinimumValue()
                    .dayOfMonth()
                    .withMinimumValue()
                    .withTimeAtStartOfDay();
            while(startDate.getYear() < 2016) {
                DateTime endDate = startDate.plusDays(1);
                try(InputStream is = mmcf.fetch(jodaToFMIDate(startDate), jodaToFMIDate(endDate.minusSeconds(1)))) {
                    mmcf.parse(is);
                } catch(Exception ex) {
                    ex.printStackTrace();
                }
                startDate = endDate;
                Thread.sleep(100);
            }
            writer.close();

        };
    }
}
