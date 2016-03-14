package fi.weequ.fingridprediction.service;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import fi.weequ.fingriddatafetcher.FingridDataFetcher;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ForecastHistoryService {
    private final SortedMap<DateTime, double[]> predictTimeToPredictions = loadForecastHistory();
    private final SortedMap<DateTime, Double> fingridHistory = loadFingridHistory();
    
    private static final String HISTORY_FILE = "/data/PPML/history.csv";
   
    private SortedMap<DateTime, Double> loadFingridHistory() {
        try {
            return FingridDataFetcher.fetch(new DateTime(DateTimeZone.UTC).minusDays(200), new DateTime(DateTimeZone.UTC).plusDays(1));
        } catch(Exception ex) {
            ex.printStackTrace();
            return new TreeMap<>();
        }
    }
    
    @Scheduled(fixedDelay = 1000*60*60)//Every 60 minutes
    public void loadFingridLatest() throws URISyntaxException, IOException {
        DateTime from;
        if (fingridHistory.isEmpty()) {
            from = new DateTime(DateTimeZone.UTC).minusDays(200);
        } else {
            from = fingridHistory.lastKey().minusDays(1);
        }
        DateTime to = new DateTime(DateTimeZone.UTC).plusDays(1);
        fingridHistory.putAll(FingridDataFetcher.fetch(from, to));
        System.out.println("Loading fingrid data from "+from+" to "+to);
    }
    
    
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
    
    
    public Object[] getHistoryCompareData() {
        DateTime firstPredictions = predictTimeToPredictions.firstKey().plusHours(24);
        DateTime firstData = fingridHistory.firstKey();
        DateTime since;
        if (firstPredictions.isAfter(firstData)) {
            since = firstPredictions;
        } else {
            since = firstData;
        }
        return new Object[] {getForecastHistory(since, 24), getFingridHistory(since)};
    }
    
    /**
     * 
     * @return An highcharts readable array
     */
    public Object[] getFingridHistory(DateTime since) {
        return fingridHistory.tailMap(since).entrySet().stream().map(e -> new Object[] {e.getKey().getMillis(), e.getValue()}).toArray();
    }
    
    /**
     * 
     * @param since
     * @param predictionHours
     * @return An highcharts readable array
     */
    public Object[] getForecastHistory(DateTime since, int predictionHours) {
        return predictTimeToPredictions.tailMap(since.minusHours(predictionHours)).entrySet().stream().map(e -> new Object[] {e.getKey().plusHours(predictionHours).getMillis(), e.getValue()[predictionHours]}).toArray();
    }
    
    public void addToForecastHistory(DateTime forecastTime, double[] forecast) throws IOException {
        predictTimeToPredictions.put(forecastTime, forecast);
        saveForecastHistory();
    }
}
