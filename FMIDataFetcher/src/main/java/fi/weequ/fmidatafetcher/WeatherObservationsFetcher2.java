package fi.weequ.fmidatafetcher;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Arrays;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;


public class WeatherObservationsFetcher2 {
//    public InputStream fetch(String startTime, String endTime) throws URISyntaxException, IOException, ParseException, ParserConfigurationException {
//        CloseableHttpClient httpclient = HttpClients.createDefault();
//        String apiKey = Settings.getProperty("api-key");
//        if (apiKey == null) {
//            throw new RuntimeException("MISSING api-key in settings.properties");
//        }
//        URI uri = new FMIQueryBuilder(Settings.getProperty("api-key"), 
//                "fmi::observations::weather::multipointcoverage")
//                .setWmo(Settings.getProperty("wmo"))
//                .setStartTime(startTime)
//                .setEndTime(endTime)
//                .build();
//        HttpGet httpget = new HttpGet(uri);
//        CloseableHttpResponse response = httpclient.execute(httpget);
//        System.out.println("Response status: "+response.getStatusLine());
//        
//        InputStream is = response.getEntity().getContent();
//         
//        WeatherObservationParser wop = new WeatherObservationParser(is, startTime);
//        for (String[] s : wop) {
//            System.out.println(Arrays.toString(s));
//        }
//        return is;
//    }
}
