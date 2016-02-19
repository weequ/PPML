package fi.weequ.fmidatafetcher;

import fi.weequ.fmidatafetcher.mainclasses.Application;
import fi.weequ.ppmlbackend.repository.WeatherObservationRepository;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

@Service
public class MastMultipointCoverageFetcher {
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
        .setParameter("storedquery_id", "fmi::observations::weather::mast::multipointcoverage")
        .setParameter("place", "espoo")
        .setParameter("starttime", startTime)
        .setParameter("endtime", endTime)
        .setParameter("parameters", "TA")
        .build();
        HttpGet httpget = new HttpGet(uri);
        System.out.println("httpget:"+httpget);
        CloseableHttpResponse response = httpclient.execute(httpget);
        //System.out.println("response entity:"+response.getEntity());
        //System.out.println("Response status: "+response.getStatusLine());
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
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        Document doc = builder.parse(is);
        
        NodeList times = doc.getElementsByTagName("gml:timePosition");
        NodeList values = doc.getElementsByTagName("gml:doubleOrNilReasonTupleList");
        if (values.getLength()*2 != times.getLength()) {
            throw new RuntimeException("There should be 2 times for each value!");
        }
        
        //CSVWriter writer = new CSVWriter(new FileWriter("yourfile.csv"), ',');
        
        for (int i = 0; i < values.getLength(); i++) {
            String dateString1 = times.item(i*2).getTextContent().trim();
            String dateString2 = times.item(i*2+1).getTextContent().trim();
            if (!dateString1.equals(dateString2)) {
                System.out.println("dateString1:"+dateString1);
                System.out.println("dateString2:"+dateString2);
                throw new RuntimeException("dateString1 != dateString2");
            }
            String valuesString = values.item(i).getTextContent().trim();
            String[] valuesArray = valuesString.split(" ");
            ArrayList<String> valuesList = new ArrayList(Arrays.asList(valuesArray));
            valuesList.add(0, dateString1);
            valuesArray = valuesList.toArray(new String[valuesList.size()]);
            Application.writer.writeNext(valuesArray);
            
            //System.out.println(valuesString);
        }
        //writer.close();
        
        
//        String allValues = doc.getElementsByTagName("gml:doubleOrNilReasonTupleList").item(0).getTextContent().trim();
//        //System.out.println("values:"+allValues);
//        Date startDate = sdf.parse(startDateStr);
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTime(startDate);
//        Scanner scanner = new Scanner(allValues);
//        while (scanner.hasNextLine()) {
//            String dayValues = scanner.nextLine().trim();
//            Scanner dayScanner = new Scanner(dayValues);
//            Double rrday = numericOrNull(dayScanner.nextDouble());
//            Double ttday = numericOrNull(dayScanner.nextDouble());
//            Double snow = numericOrNull(dayScanner.nextDouble());
//            Double tmin = numericOrNull(dayScanner.nextDouble());
//            Double tmax = numericOrNull(dayScanner.nextDouble());
//            dayScanner.close();
//            WeatherObservation weatherObservation = new WeatherObservation();
//            weatherObservation.setDate(new Date(calendar.getTime().getTime()));
//            calendar.add(Calendar.DATE, 1);
//            weatherObservation.setRrday(rrday);
//            weatherObservation.setTtday(ttday);
//            weatherObservation.setSnow(snow);
//            weatherObservation.setTmin(tmin);
//            weatherObservation.setTmax(tmax);
//            weatherObservation.setWmo(Settings.getProperty("wmo"));
//            //System.out.println(weatherObservation);
//            try {
//                //weatherObservationRepository.save(weatherObservation);
//            } catch(ConstraintViolationException cve) {
//                System.out.println("Constraint violation while saving: "+weatherObservation);
//            } catch(Exception ex) {
//                System.out.println("Exception while saving:"+ weatherObservation);
//            }
//        }
//        scanner.close();
    }
}
