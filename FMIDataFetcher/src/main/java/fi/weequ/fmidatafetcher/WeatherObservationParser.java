package fi.weequ.fmidatafetcher;

import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Scanner;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.joda.time.DateTime;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class WeatherObservationParser implements CSVIterable {

    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    
    private final InputStream is;
    
    private final DocumentBuilderFactory factory;
    private final DocumentBuilder builder;
    private DateTime currentTime;
    private boolean includeTime;
    
    
    
    public WeatherObservationParser(InputStream is, DateTime startDate, boolean includeTime) throws ParseException, ParserConfigurationException {
        factory = DocumentBuilderFactory.newInstance();
        builder = factory.newDocumentBuilder();
        this.is = is;
        currentTime = startDate;
        this.includeTime = includeTime;
    }
    
    

    
    
    @Override
    public Iterator<String[]> iterator() {
        
        Document doc;
        String allValues;
        Scanner scanner;
        String[] params;
        try {
            doc = builder.parse(is);
            allValues = doc.getElementsByTagName("gml:doubleOrNilReasonTupleList").item(0).getTextContent().trim();
            scanner = new Scanner(allValues);
        } catch (Exception ex) {
            return Collections.emptyIterator();
        }
        
        NodeList paramNodes = doc.getElementsByTagName("swe:field");
        if (paramNodes.getLength() == 0) return Collections.emptyIterator();
        params = new String[paramNodes.getLength()];
        for (int paramIndex = 0; paramIndex <  paramNodes.getLength(); paramIndex++) {
            Node current = paramNodes.item(paramIndex);
            String param = current.getAttributes().getNamedItem("name").getNodeValue();
            params[paramIndex] = param;
        }
        
        
        return new Iterator<String[]>() {
            private boolean first = true;
            
            @Override
            public boolean hasNext() {
                return scanner.hasNextLine();
            }

            @Override
            public String[] next() {
//                if (first) {
//                    first = false;
//                    return params;
//                }
                String tenMinValues = scanner.nextLine().trim();
                Scanner valueScanner = new Scanner(tenMinValues);
                ArrayList<String> values = new ArrayList<>();
                if (includeTime) {
                    values.add(""+currentTime.getMillis());
                }
                while(valueScanner.hasNext()) {
                    values.add(valueScanner.next());
                }
                valueScanner.close();
                if (values.isEmpty()) {
                    throw new NoSuchElementException();
                }
                currentTime = currentTime.plusMinutes(10);
                return values.toArray(new String[values.size()]);
            }
        };
    }
    
}
