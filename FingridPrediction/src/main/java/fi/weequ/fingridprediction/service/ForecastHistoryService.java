package fi.weequ.fingridprediction.service;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.stereotype.Service;

@Service
public class ForecastHistoryService {
    private SortedMap<DateTime, double[]> predictTimeToPredictions = loadForecastHistory();
    
    private static final String HISTORY_FILE = "/data/PPML/history.csv";
    
    /**
     * Load from file if available. Otherwise empty history
     * @return 
     */
    private SortedMap<DateTime, double[]> loadForecastHistory() {
        try {
            SortedMap<DateTime, double[]> result = new TreeMap<>();
            CSVReader reader = new CSVReader(new FileReader(HISTORY_FILE));
            for (String[] line : reader) {
                DateTime dt = new DateTime(Long.parseLong(line[0]), DateTimeZone.UTC);
                double[] la = new double[line.length-1];
                for (int i = 1; i < line.length; i++) {
                    la[i-1] = Double.parseDouble(line[i]);
                }
                result.put(dt, la);
            }
            System.out.println("forecast history loaded from "+HISTORY_FILE);
            return result;
        } catch(Exception ex) {
            return new TreeMap<>();
        }
    } 
    
    /**
     * Save current forecast history to file
     * @throws IOException 
     */
    private void saveForecastHistory() throws IOException {
        File old = new File(HISTORY_FILE);
        File backup = new File("/data/PPML/history_backups/history"+System.currentTimeMillis()+".csv");
        old.renameTo(backup);
        CSVWriter writer = new CSVWriter(new FileWriter(HISTORY_FILE));
        for (Map.Entry<DateTime, double[]> e: predictTimeToPredictions.entrySet()) {
            String[] line = new String[e.getValue().length+1];
            line[0] = ""+e.getKey().getMillis();
            for (int i = 0; i < e.getValue().length; i++) {
                line[i+1] = ""+e.getValue()[i];
            }
            writer.writeNext(line);
        }
        System.out.println("forecast history saved to "+HISTORY_FILE);
        writer.close();
    }
    
    /**
     * 
     * @param since
     * @param predictionHours
     * @return An highcharts readable array
     */
    public Object[] getForecastHistory(DateTime since, int predictionHours) {
        return predictTimeToPredictions.tailMap(since).entrySet().stream().map(e -> new Object[] {e.getKey().plusHours(predictionHours).getMillis(), e.getValue()[predictionHours]}).toArray();
    }
    
    public void addToForecastHistory(DateTime forecastTime, double[] forecast) throws IOException {
        predictTimeToPredictions.put(forecastTime, forecast);
        saveForecastHistory();
    }
}
