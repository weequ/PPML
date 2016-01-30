package fi.weequ.fmidatafetcher;

import fi.weequ.ppmlbackend.domain.WeatherObservation;
import fi.weequ.ppmlbackend.repository.WeatherObservationRepository;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Scanner;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

@Service
public class MultipointCoverageFetcher {
    
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    
    @Autowired
    private WeatherObservationRepository weatherObservationRepository;
    
    public InputStream fetch(String startTime, String endTime) throws URISyntaxException, IOException {
        CloseableHttpClient httpclient = HttpClients.createDefault();
        String apiKey = Settings.getProperty("api-key");
        if (apiKey == null) {
            throw new RuntimeException("MISSING api-key in settings.properties");
        }
        URI uri = new URIBuilder()
        .setScheme("http")
        .setHost("data.fmi.fi")
        .setPath("/fmi-apikey/"+Settings.getProperty("api-key")+"/wfs")
        .setParameter("request", "getFeature")
        .setParameter("storedquery_id", "fmi::observations::weather::daily::multipointcoverage")
        .setParameter("wmo", Settings.getProperty("wmo"))
        .setParameter("starttime", startTime)
        .setParameter("endtime", endTime)
        .build();
        HttpGet httpget = new HttpGet(uri);
        CloseableHttpResponse response = httpclient.execute(httpget);
        System.out.println("Response status: "+response.getStatusLine());
        
        InputStream is = response.getEntity().getContent();
        
        return is;
    }
    
    public InputStream fetch() throws URISyntaxException, IOException {
        return fetch(Settings.getProperty("starttime"), Settings.getProperty("endtime"));
    }
    
    private Double numericOrNull(Double d) {
        if (d == null || d.isNaN() || d.isInfinite()) return null;
        return d;
    }
    
    public void parse(InputStream is) throws SAXException, IOException, ParserConfigurationException, ParseException {
        parse(is, Settings.getProperty("starttime"));
    }
    
    public void parse(InputStream is, String startDateStr) throws SAXException, IOException, ParserConfigurationException, ParseException {
        DocumentBuilderFactory factory =
        DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        Document doc = builder.parse(is);
        
        String allValues = doc.getElementsByTagName("gml:doubleOrNilReasonTupleList").item(0).getTextContent().trim();
        //System.out.println("values:"+allValues);
        Date startDate = sdf.parse(startDateStr);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        Scanner scanner = new Scanner(allValues);
        while (scanner.hasNextLine()) {
            String dayValues = scanner.nextLine().trim();
            Scanner dayScanner = new Scanner(dayValues);
            Double rrday = numericOrNull(dayScanner.nextDouble());
            Double ttday = numericOrNull(dayScanner.nextDouble());
            Double snow = numericOrNull(dayScanner.nextDouble());
            Double tmin = numericOrNull(dayScanner.nextDouble());
            Double tmax = numericOrNull(dayScanner.nextDouble());
            dayScanner.close();
            WeatherObservation weatherObservation = new WeatherObservation();
            weatherObservation.setDate(new Date(calendar.getTime().getTime()));
            calendar.add(Calendar.DATE, 1);
            weatherObservation.setRrday(rrday);
            weatherObservation.setTtday(ttday);
            weatherObservation.setSnow(snow);
            weatherObservation.setTmin(tmin);
            weatherObservation.setTmax(tmax);
            weatherObservation.setWmo(Settings.getProperty("wmo"));
            //System.out.println(weatherObservation);
            try {
                weatherObservationRepository.save(weatherObservation);
            } catch(ConstraintViolationException cve) {
                System.out.println("Constraint violation while saving: "+weatherObservation);
            } catch(Exception ex) {
                System.out.println("Exception while saving:"+ weatherObservation);
            }
        }
        scanner.close();
    }
}
