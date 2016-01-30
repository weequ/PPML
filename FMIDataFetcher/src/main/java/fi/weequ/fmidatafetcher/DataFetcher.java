package fi.weequ.fmidatafetcher;

import java.io.IOException;
import java.io.InputStream;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.net.URI;
import java.net.URISyntaxException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.xml.sax.SAXException;


public class DataFetcher {
    
    public static void main(String[] args) {
        try(InputStream is = fetch()) {
            parse(is);
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    
    public static InputStream fetch() throws URISyntaxException, IOException {
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
        .setParameter("storedquery_id", "fmi::observations::weather::daily::timevaluepair")
        .setParameter("wmo", Settings.getProperty("wmo"))
        .setParameter("starttime", Settings.getProperty("starttime"))
        .setParameter("endtime", Settings.getProperty("endtime"))
        .build();
        HttpGet httpget = new HttpGet(uri);
        CloseableHttpResponse response1 = httpclient.execute(httpget);
        System.out.println("Response status: "+response1.getStatusLine());
        
        InputStream is = response1.getEntity().getContent();
        
        return is;
    }
    
    
    public static void parse(InputStream is) throws SAXException, IOException, ParserConfigurationException {
        DocumentBuilderFactory factory =
        DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        Document doc = builder.parse(is);

        NodeList measurements = doc.getElementsByTagName("wml2:MeasurementTimeseries");
        
        for (int i = 0; i < measurements.getLength(); i++) {
            Node node = measurements.item(i);
            
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element measurement = (Element) node;
                String idStr = measurement.getAttribute("gml:id");
                System.out.println("idstr:"+idStr);
                Element timeValuePair = (Element) measurement.getElementsByTagName("wml2:MeasurementTVP").item(0);
                String time = timeValuePair.getElementsByTagName("wml2:time").item(0).getTextContent();
                String value = timeValuePair.getElementsByTagName("wml2:value").item(0).getTextContent();
                System.out.println("time:"+time);
                System.out.println("value:"+value);
            }
        }
    }

}
