package fi.weequ.fingridprediction.service;

import fi.weequ.fmidatafetcher.FMIQuery;
import fi.weequ.fmidatafetcher.FMIQueryBuilder;
import fi.weequ.fmidatafetcher.Settings;
import fi.weequ.fmidatafetcher.WeatherObservationParser;
import fi.weequ.fmidatafetcher.util.FMIUtils;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Arrays;
import javax.xml.parsers.ParserConfigurationException;
import org.encog.ml.MLRegression;
import org.encog.ml.data.MLData;
import org.encog.ml.data.versatile.NormalizationHelper;
import org.encog.persist.EncogDirectoryPersistence;
import org.encog.util.obj.SerializeObject;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

@Service
public class ForecastService {
    
    private final static double helsinki_kaisaniemi_lat = 60.18;
    private final static double helsinki_kaisaniemi_lon = 24.94;
    private final static double kuopio_ritoniemi_lat = 62.80;
    private final static double kuopio_ritoniemi_lon = 27.90;
    private final static double rovaniemi_rautatieasema_lat = 66.50;
    private final static double rovaniemi_rautatieasema_lon = 25.71;
    private static DateTime helsinkiStart = null;
    private static DateTime kuopioStart = null;
    private static DateTime rovaniemiStart = null;
    private static double[] helsinkiForecast = null;
    private static double[] kuopioForecast = null;
    private static double[] rovaniemiForecast = null;
    private static DateTime electricityForecastStart = null;
    private static double[] electricityForecast = null;
    
    @Autowired
    private ForecastHistoryService forecastHistoryService;
    
    @Scheduled(fixedRate = 5000)
    public void createForecast() throws ParseException, URISyntaxException, IOException, ParserConfigurationException, SAXException, ClassNotFoundException {
        getElectricityForecast();
        //System.out.println("The time is now " + System.currentTimeMillis());
    }
    
    
    private DateTime beginningOfCurrentHour() {
        return DateTime.now(DateTimeZone.UTC).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
    }
    
    public double[] getHelsinkiForecast() throws ParseException, URISyntaxException, IOException, ParserConfigurationException, SAXException {
        DateTime beginningOfHour = beginningOfCurrentHour();
        if (helsinkiStart == null || beginningOfHour.isAfter(helsinkiStart)) {
            double[] result = next36HoursLatLon(helsinki_kaisaniemi_lat, helsinki_kaisaniemi_lon, beginningOfHour);
            helsinkiStart = beginningOfHour;
            helsinkiForecast = result;
        }
        return helsinkiForecast;
    }
    
    public double[] getKuopioForecast() throws ParseException, URISyntaxException, IOException, ParserConfigurationException, SAXException {
        DateTime beginningOfHour = beginningOfCurrentHour();
        if (kuopioStart == null || beginningOfHour.isAfter(kuopioStart)) {
            double[] result = next36HoursLatLon(kuopio_ritoniemi_lat, kuopio_ritoniemi_lon, beginningOfHour);
            kuopioStart = beginningOfHour;
            kuopioForecast = result;
        }
        return kuopioForecast;
    }
    
    public double[] getRovaniemiForecast() throws ParseException, URISyntaxException, IOException, ParserConfigurationException, SAXException {
        DateTime beginningOfHour = beginningOfCurrentHour();
        if (rovaniemiStart == null || beginningOfHour.isAfter(rovaniemiStart)) {
            double[] result = next36HoursLatLon(rovaniemi_rautatieasema_lat, rovaniemi_rautatieasema_lon, beginningOfHour);
            rovaniemiStart = beginningOfHour;
            rovaniemiForecast = result;
        }
        return rovaniemiForecast;
    }
    
    
    
    
    public double[] getElectricityForecast() throws ParseException, URISyntaxException, IOException, ParserConfigurationException, SAXException, ClassNotFoundException {
        DateTime beginningOfHour = beginningOfCurrentHour();
        if (electricityForecastStart == null || beginningOfHour.isAfter(electricityForecastStart)) {
            System.out.println("hour has changed to "+beginningOfHour+". creating a new forecast.");
            double[] helsinkiWF = getHelsinkiForecast();
            double[] kuopioWF = getKuopioForecast();
            double[] rovaniemiWF = getRovaniemiForecast();
            double[] result = new double[helsinkiWF.length];
            DateTime currentTime = beginningOfHour;
            for (int i = 0; i < helsinkiWF.length; i++) {
                int dayOfWeek = currentTime.getDayOfWeek();
                int hourOfDay = currentTime.getHourOfDay();
                result[i] = predict(helsinkiWF[i], kuopioWF[i], rovaniemiWF[i], dayOfWeek, hourOfDay);
                currentTime = currentTime.plusHours(1);
            }
            electricityForecastStart = beginningOfHour;
            forecastHistoryService.addToForecastHistory(beginningOfHour, result);
            electricityForecast = result;
        }
        return electricityForecast;
    }

    
    private static Document queryPredictions(double latitude, double longitude, DateTime startTime) throws URISyntaxException, IOException, ParseException, ParserConfigurationException, SAXException {
        FMIQuery query = new FMIQueryBuilder(Settings.getProperty("api-key"), "fmi::forecast::hirlam::surface::point::multipointcoverage")
        .setTimeStep("60")
        .setParameters("Temperature")
        .setStartTime(FMIUtils.jodaToFMIDate(startTime))
        .setLatLong(latitude, longitude)
        .build();
        return query.execute();
    }
    
    
    public double[] next36HoursLatLon(double latitude, double longitude, DateTime startTime) throws URISyntaxException, IOException, ParseException, ParserConfigurationException, SAXException {
        Document doc = queryPredictions(latitude, longitude, startTime);
        WeatherObservationParser docparser = new WeatherObservationParser(doc);
        int counter = 0;
        double[] result = new double[37];
        Arrays.fill(result, Double.NaN);//Because trailing NaNs are not included in the FMI data
        for (String[] sa : docparser) {
            try {
                result[counter] = Double.parseDouble(sa[0]);
            }catch(Exception ex) {
                ex.printStackTrace();
            }
            counter++;
        }
        return result;
    }
    
    public double predict(double helsinki, double kuopio, double rovaniemi, int dayOfWeek, int hourOfDay) throws IOException, ClassNotFoundException {
        NormalizationHelper normalizationHelper = (NormalizationHelper) SerializeObject.load(new File("/data/PPML/normalization.bin"));
        MLRegression mlModel = (MLRegression) EncogDirectoryPersistence.loadObject(new File("/data/PPML/bestmethod.eg"));
        String[] inputLine = {""+helsinki, ""+kuopio, ""+rovaniemi, ""+dayOfWeek, ""+hourOfDay};
        double[] normalizedInput = new double[normalizationHelper.calculateNormalizedInputCount()];
        normalizationHelper.normalizeInputVector(inputLine, normalizedInput, true);
        MLData input = normalizationHelper.allocateInputVector();
        input.setData(normalizedInput);
        MLData output = mlModel.compute(input);
        return Double.parseDouble(normalizationHelper.denormalizeOutputVectorToString(output)[0]);
    }
    
}
