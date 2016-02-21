package fi.weequ.fmidatafetcher.mainclasses;

import com.opencsv.CSVWriter;
import fi.weequ.fmidatafetcher.FMIQuery;
import fi.weequ.fmidatafetcher.FMIQueryBuilder;
import fi.weequ.fmidatafetcher.Settings;
import fi.weequ.fmidatafetcher.WeatherObservationParser;
import fi.weequ.fmidatafetcher.WeatherObservationsFetcher;
import fi.weequ.fmidatafetcher.util.FMIUtils;
import java.io.FileWriter;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@ComponentScan("fi.weequ")
@Configuration
//@EnableJpaRepositories(basePackages = {"fi.weequ.ppmlbackend.repository"})
//@EnableAutoConfiguration
//@EnableTransactionManagement
public class TenMinute2014To2016v2 {
    public static void main(String[] args) {
        SpringApplication.run(TenMinute2014To2016v2.class);
    }
    
    
//    private static String jodaToFMIDate(DateTime jodaTime) {
//        return jodaTime.toString("yyyy-MM-dd'T'HH:mm:ss'Z'");//jodaTime.getYear()+"-"+jodaTime.getMonthOfYear()+"-"+jodaTime.getDayOfMonth()+"T00:00:00Z";
//    }
    
    
    @Bean
    public CommandLineRunner demo(WeatherObservationsFetcher wof) {
        return (args) -> {
            
            CSVWriter csvWriter = new CSVWriter(new FileWriter("results/kuopio maaninka lämpötilat 2014TO2016.csv"), ',');
            
            DateTime startDate = new DateTime()
                    .withZone(DateTimeZone.UTC)
                    .withYear(2014)
                    .monthOfYear()
                    .withMinimumValue()
                    .dayOfMonth()
                    .withMinimumValue()
                    .withTimeAtStartOfDay();
            while(startDate.getYear() < 2016) {

                DateTime endDate = startDate.plusDays(1);
                FMIQuery query = new FMIQueryBuilder(Settings.getProperty("api-key"), 
                "fmi::observations::weather::multipointcoverage")
                .setStartTime(FMIUtils.jodaToFMIDate(startDate))
                .setEndTime(FMIUtils.jodaToFMIDate(endDate.minusSeconds(1)))
                //.setWmo(Settings.getProperty("wmo"))
                //.setPlace("Espoo")
                .setTimeStep("10")
                .setFMSID("101572")
                .setParameters("t2m")
                .build();
                
                WeatherObservationParser wop = new WeatherObservationParser(query.execute(), 
                        startDate, true);
                int count = 0;
                for (String[] sa : wop) {
                    csvWriter.writeNext(sa);
                    count++;
                }
                if (count < 144) {
                    System.out.println("count = "+count);
                    System.out.println("startDate = "+FMIUtils.jodaToFMIDate(startDate)+", endTime="+FMIUtils.jodaToFMIDate(endDate.minusSeconds(1)));
                }
                
                startDate = endDate;
                Thread.sleep(500);
            }
            csvWriter.close();

        };
    }
}
