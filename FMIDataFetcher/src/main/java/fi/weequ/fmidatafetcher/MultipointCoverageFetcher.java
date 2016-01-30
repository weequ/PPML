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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

@Service
public class MultipointCoverageFetcher {
    
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    
//    public static void main2(String[] args) {
//        try(InputStream is = fetch()) {
//            parse(is);
//        } catch(Exception ex) {
//            ex.printStackTrace();
//        }
//    }

    @Autowired
    private WeatherObservationRepository weatherObservationRepository;
    
    
    
    public InputStream fetch() throws URISyntaxException, IOException {
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
        .setParameter("starttime", Settings.getProperty("starttime"))
        .setParameter("endtime", Settings.getProperty("endtime"))
        .build();
        HttpGet httpget = new HttpGet(uri);
        CloseableHttpResponse response = httpclient.execute(httpget);
        System.out.println("Response status: "+response.getStatusLine());
        
        InputStream is = response.getEntity().getContent();
        
        return is;
    }
    
    
    public void parse(InputStream is) throws SAXException, IOException, ParserConfigurationException, ParseException {
        DocumentBuilderFactory factory =
        DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        Document doc = builder.parse(is);
        
        String allValues = doc.getElementsByTagName("gml:doubleOrNilReasonTupleList").item(0).getTextContent().trim();
        //System.out.println("values:"+allValues);
        Date startDate = sdf.parse(Settings.getProperty("starttime"));
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        Scanner scanner = new Scanner(allValues);
        while (scanner.hasNextLine()) {
            String dayValues = scanner.nextLine().trim();
            Scanner dayScanner = new Scanner(dayValues);
            Double rrday = dayScanner.nextDouble();
            Double ttday = dayScanner.nextDouble();
            Double snow = dayScanner.nextDouble();
            Double tmin = dayScanner.nextDouble();
            Double tmax = dayScanner.nextDouble();
            WeatherObservation weatherObservation = new WeatherObservation();
            weatherObservation.setDate(new Date(calendar.getTime().getTime()));
            calendar.add(Calendar.DATE, 1);
            weatherObservation.setRrday(rrday);
            weatherObservation.setTtday(ttday);
            weatherObservation.setSnow(snow);
            weatherObservation.setTmin(tmin);
            weatherObservation.setTmax(tmax);
            weatherObservation.setWmo(Settings.getProperty("wmo"));
            System.out.println(weatherObservation);
            weatherObservationRepository.save(weatherObservation);
            dayScanner.close();
        }
        scanner.close();
    }
}
