package fi.weequ.fmidatafetcher.mainclasses;

import com.opencsv.CSVWriter;
import fi.weequ.fmidatafetcher.FMIQuery;
import fi.weequ.fmidatafetcher.FMIQueryBuilder;
import fi.weequ.fmidatafetcher.Settings;
import fi.weequ.fmidatafetcher.WeatherObservationParser;
import fi.weequ.fmidatafetcher.util.FMIUtils;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import javax.xml.parsers.ParserConfigurationException;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.xml.sax.SAXException;

public class TenMinute2014To2016v2 {
    
    public static void main(String[] args) throws InterruptedException, IOException, ParseException, URISyntaxException, ParserConfigurationException, SAXException {
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
    }

}
