package fi.weequ.combinefmicsv;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Map;
import java.util.TreeMap;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;


public class FMICSVCombiner {
    
    private static Map<Long, Double> readElectricityCSV(String filePath) throws FileNotFoundException {
        Map<Long, Double> result = new TreeMap<>();
        CSVReader csvReader = new CSVReader(new FileReader(filePath), ',');
        for (String[] line : csvReader) {
            String utcTimeStr = line[0];
            String electricityConsumptionStr = line[2];
            try {
                DateTimeFormatter fmt = DateTimeFormat.forPattern("dd.MM.yyyy HH:mm");
                fmt = fmt.withZoneUTC();
                DateTime time = fmt.parseDateTime(utcTimeStr);
                DecimalFormat formatter = new DecimalFormat("#,###.00");
                double electricityConsumptionDouble = formatter.parse(electricityConsumptionStr).doubleValue();
                //double electricityConsumptionDouble = Double.parseDouble(electricityConsumptionStr);
                if (!Double.isNaN(electricityConsumptionDouble)) {
                    result.put(time.getMillis(), electricityConsumptionDouble);
                }
            } catch(Exception ex) {
                System.out.println(ex);
            }
        }
        return result;
    }
    
    private static Map<Long, Double> readTempCsv(String filePath) throws FileNotFoundException {
        Map<Long, Double> result = new TreeMap<>();
        CSVReader csvReader = new CSVReader(new FileReader(filePath), ',');
        for (String[] line : csvReader) {
            String timeStr = line[0];
            String tempStr = line[1];
            try {
                long timeStampLong = Long.parseLong(timeStr);
                double tempDouble = Double.parseDouble(tempStr);
                if (!Double.isNaN(tempDouble)) {
                    result.put(timeStampLong, tempDouble);
                }
            } catch(Exception ex) {
                
            }
            
        }
        return result;
    }
    
    public static void main(String[] args) throws FileNotFoundException, IOException {
        Map<Long, Double> timeToHelsinkiTemp = new TreeMap<>();
        Map<Long, Double> timeToKuopioTemp = new TreeMap<>();
        Map<Long, Double> timeToRovaniemiTemp = new TreeMap<>();
        Map<Long, Double> timeToElectricityConsumption = new TreeMap<>();
        timeToHelsinkiTemp.putAll(readTempCsv("helsinki kaisaniemi lämpötilat 2014TO2016.csv"));
        timeToKuopioTemp.putAll(readTempCsv("kuopio ritoniemi lämpötilat 2014TO2016.csv"));
        timeToRovaniemiTemp.putAll(readTempCsv("rovaniemi rautatieasema lämpötilat 2014TO2016.csv"));
        timeToElectricityConsumption.putAll(readElectricityCSV("TimeSeries 2014.csv"));
        timeToElectricityConsumption.putAll(readElectricityCSV("TimeSeries 2015.csv"));
        System.out.println(timeToElectricityConsumption.size());
        timeToElectricityConsumption.keySet().retainAll(timeToHelsinkiTemp.keySet());
        System.out.println(timeToElectricityConsumption.size());
        timeToElectricityConsumption.keySet().retainAll(timeToKuopioTemp.keySet());
        System.out.println(timeToElectricityConsumption.size());
        timeToElectricityConsumption.keySet().retainAll(timeToRovaniemiTemp.keySet());
        System.out.println(timeToElectricityConsumption.size());
        CSVWriter writer = new CSVWriter(new FileWriter("data2.csv"));
        writer.writeNext(new String[] {"time", "energyConsumption", 
            "temp helsinki kaisaniemi", "temp kuopio ritoniemi", "temp rovaniemi rautatieasema", 
            "dayofweek", "hourofday"});
        for (long time : timeToElectricityConsumption.keySet()) {
            DateTime dt = new DateTime(time);
            dt = dt.withZone(DateTimeZone.forOffsetHours(2));
            writer.writeNext(new String[] {
                ""+time, 
                ""+timeToElectricityConsumption.get(time), 
                ""+timeToHelsinkiTemp.get(time),
                ""+timeToKuopioTemp.get(time),
                ""+timeToRovaniemiTemp.get(time),
                ""+dt.getDayOfWeek(),
                ""+dt.getHourOfDay()});
        }
        writer.close();
        //System.out.println(timeToElectricityConsumption);
    }
}
