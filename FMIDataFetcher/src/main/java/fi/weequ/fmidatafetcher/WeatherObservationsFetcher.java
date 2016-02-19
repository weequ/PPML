package fi.weequ.fmidatafetcher;

import fi.weequ.ppmlbackend.repository.WeatherObservationRepository;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
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

//parameters:
//t2m = air temperature (degC)
//ws_10min = wind speed (m/s) (wind), avg
//wg_10min = gust speed (m/s) (wind), max
//wd_10min = Wind direction (deg), avg
//rh = Relative humidity (%), avg
//td = Dew-point temperature (degC) (Humidity)
//r_1h = Precipitation amount (mm), acc
//ri_10min = Amount of precipitation (mm/h), avg
//snow_avs = Snow depth
//p_sea = air pressure (hPa), avg
//vis = Horizontal visibility (m), avg
//n_man = Cloud amount (1/8), instant
//wawa = Present weather (auto), rank


@Service
public class WeatherObservationsFetcher {
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
        .setParameter("storedquery_id", "fmi::observations::weather::multipointcoverage")
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
            String tenMinValues = scanner.nextLine().trim();
            Scanner tenMinScanner = new Scanner(tenMinValues);
            Double t2m = numericOrNull(tenMinScanner.nextDouble());
            Double ws_10min = numericOrNull(tenMinScanner.nextDouble());
            Double wg_10min = numericOrNull(tenMinScanner.nextDouble());
            Double wd_10min = numericOrNull(tenMinScanner.nextDouble());
            Double rh = numericOrNull(tenMinScanner.nextDouble());
            Double td = numericOrNull(tenMinScanner.nextDouble());
            Double r_1h = numericOrNull(tenMinScanner.nextDouble());
            Double ri_10min = numericOrNull(tenMinScanner.nextDouble());
            Double snow_avs = numericOrNull(tenMinScanner.nextDouble());
            Double p_sea = numericOrNull(tenMinScanner.nextDouble());
            Double vis = numericOrNull(tenMinScanner.nextDouble());
            Double n_man = numericOrNull(tenMinScanner.nextDouble());
            Double wawa = numericOrNull(tenMinScanner.nextDouble());
            System.out.println(calendar.getTime());
            System.out.println("t2m="+t2m+", ws10min="+ws_10min+", wg10min="+wg_10min+
                    ", wd10min="+wg_10min+", rh="+rh+", td="+td+", r_1h="+r_1h+
                    ", ri_10min="+ri_10min+", snow_aws="+snow_avs+", p_sea="+p_sea+", vis="+vis+", n_man="+n_man+", wava = "+wawa);
            
            
            tenMinScanner.close();
            calendar.add(Calendar.MINUTE, 10);
        }
        scanner.close();
    }
}
