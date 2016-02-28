package fi.weequ.fingridprediction.controller;

import fi.weequ.fingridprediction.form.PredictionForm;
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
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import javax.validation.Valid;
import javax.xml.parsers.ParserConfigurationException;
import org.encog.ml.MLRegression;
import org.encog.ml.data.MLData;
import org.encog.ml.data.versatile.NormalizationHelper;
import org.encog.persist.EncogDirectoryPersistence;
import org.encog.util.obj.SerializeObject;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

//http://data.fmi.fi/fmi-apikey//wfs?request=getFeature&storedquery_id=fmi::forecast::hirlam::surface::point::multipointcoverage&place=helsinki&timestep=1&parameters=Temperature
//helsinki kaisaniemi: latlon=60.18,24.94
//kuopio ritoniemi: 62.80,27.90
//rovaniemi rautatieasema: 66.50,25.71
@Controller
public class MainController {
    
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
    
    private static String[] inputss = new String[] {"temp helsinki kaisaniemi", "temp kuopio ritoniemi", 
        "temp rovaniemi rautatieasema", "dayofweek",
        "hourofday"};
    
    
    private double[] next36HoursLatLon(double latitude, double longitude, DateTime startTime) throws URISyntaxException, IOException, ParseException, ParserConfigurationException, SAXException {
        //DateTime startTime = DateTime.now(DateTimeZone.UTC).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
        Document doc = queryPredictions(latitude, longitude, startTime);
        WeatherObservationParser docparser = new WeatherObservationParser(doc);
        int counter = 0;
        double[] result = new double[37];
        Arrays.fill(result, Double.NaN);//Because trailing NaNs are not included the FMI data
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
    
    private DateTime beginningOfCurrentHour() {
        return DateTime.now(DateTimeZone.UTC).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
    }
    
    private double[] getHelsinkiForecast() throws ParseException, URISyntaxException, IOException, ParserConfigurationException, SAXException {
        DateTime beginningOfHour = beginningOfCurrentHour();
        if (helsinkiStart == null || beginningOfHour.isAfter(helsinkiStart)) {
            double[] result = next36HoursLatLon(helsinki_kaisaniemi_lat, helsinki_kaisaniemi_lon, beginningOfHour);
            helsinkiStart = beginningOfHour;
            helsinkiForecast = result;
        }
        return helsinkiForecast;
    }
    
    private double[] getKuopioForecast() throws ParseException, URISyntaxException, IOException, ParserConfigurationException, SAXException {
        DateTime beginningOfHour = beginningOfCurrentHour();
        if (kuopioStart == null || beginningOfHour.isAfter(kuopioStart)) {
            double[] result = next36HoursLatLon(kuopio_ritoniemi_lat, kuopio_ritoniemi_lon, beginningOfHour);
            kuopioStart = beginningOfHour;
            kuopioForecast = result;
        }
        return kuopioForecast;
    }
    
    private double[] getRovaniemiForecast() throws ParseException, URISyntaxException, IOException, ParserConfigurationException, SAXException {
        DateTime beginningOfHour = beginningOfCurrentHour();
        if (rovaniemiStart == null || beginningOfHour.isAfter(rovaniemiStart)) {
            double[] result = next36HoursLatLon(rovaniemi_rautatieasema_lat, rovaniemi_rautatieasema_lon, beginningOfHour);
            rovaniemiStart = beginningOfHour;
            rovaniemiForecast = result;
        }
        return rovaniemiForecast;
    }
    
    private double[] getElectricityForecast() throws ParseException, URISyntaxException, IOException, ParserConfigurationException, SAXException, ClassNotFoundException {
        DateTime beginningOfHour = beginningOfCurrentHour();
        if (electricityForecastStart == null || beginningOfHour.isAfter(electricityForecastStart)) {
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
            electricityForecast = result;
        }
        return electricityForecast;
    }
    
    
    @RequestMapping(value="forecast_electricity", method = RequestMethod.GET)
    public @ResponseBody Object[] forecastElectricity() throws URISyntaxException, IOException, ParseException, ParserConfigurationException, SAXException, ClassNotFoundException {
        Object[] result = new Object[getElectricityForecast().length];
        DateTime time = electricityForecastStart;
        for (int i = 0; i < electricityForecast.length; i++) {
            Object[] elem = new Object[2];
            elem[0] = time.getMillis();
            elem[1] = electricityForecast[i];
            result[i] = elem;
            time = time.plusHours(1);
        }
        return result;
    }
    
    @RequestMapping(value="forecast_electricity_old", method = RequestMethod.GET)
    public @ResponseBody Object[] forecastElectricityOld() throws URISyntaxException, IOException, ParseException, ParserConfigurationException, SAXException, ClassNotFoundException {
        System.out.println("forecast!");
        return getWeatherForecast();
    }
    
    
    @RequestMapping(value="weather_forecast", method = RequestMethod.GET)
    public @ResponseBody Object[] weatherForecast() throws ParseException, URISyntaxException, IOException, ParserConfigurationException, SAXException {
        return new Object[] {forecastHelsinkiKaisaniemi(), forecastKuopioRitoniemi(), forecastRovaniemiRautatieasema()};
    }
    
    @RequestMapping(value="wether_forecast_helsinki_kaisaniemi", method = RequestMethod.GET)
    public @ResponseBody Object[] forecastHelsinkiKaisaniemi() throws ParseException, URISyntaxException, IOException, ParserConfigurationException, SAXException  {
        Object[] result = new Object[getHelsinkiForecast().length];
        DateTime time = helsinkiStart;
        for (int i = 0; i < helsinkiForecast.length; i++) {
            Object[] elem = new Object[2];
            elem[0] = time.getMillis();
            elem[1] = helsinkiForecast[i];
            result[i] = elem;
            time = time.plusHours(1);
        }
        return result;
    }
    
    @RequestMapping(value="wether_forecast_kuopio_ritoniemi", method = RequestMethod.GET)
    public @ResponseBody Object[] forecastKuopioRitoniemi() throws ParseException, URISyntaxException, IOException, ParserConfigurationException, SAXException  {
        Object[] result = new Object[getKuopioForecast().length];
        DateTime time = kuopioStart;
        for (int i = 0; i < kuopioForecast.length; i++) {
            Object[] elem = new Object[2];
            elem[0] = time.getMillis();
            elem[1] = kuopioForecast[i];
            result[i] = elem;
            time = time.plusHours(1);
        }
        return result;
    }
    
    @RequestMapping(value="wether_forecast_rovaniemi_rautatieasema", method = RequestMethod.GET)
    public @ResponseBody Object[] forecastRovaniemiRautatieasema() throws ParseException, URISyntaxException, IOException, ParserConfigurationException, SAXException  {
        Object[] result = new Object[getRovaniemiForecast().length];
        DateTime time = rovaniemiStart;
        for (int i = 0; i < rovaniemiForecast.length; i++) {
            Object[] elem = new Object[2];
            elem[0] = time.getMillis();
            elem[1] = rovaniemiForecast[i];
            result[i] = elem;
            time = time.plusHours(1);
        }
        return result;
    }
    
    
    private Object[] getWeatherForecast() throws URISyntaxException, IOException, ParseException, ParserConfigurationException, SAXException, ClassNotFoundException {
        DateTime startTime = DateTime.now();
        startTime = startTime.withZone(DateTimeZone.UTC);
        startTime = startTime.withMinuteOfHour(0);
        startTime = startTime.withSecondOfMinute(0);
        startTime = startTime.withMillisOfSecond(0);
        Document helsinki = queryPredictions(helsinki_kaisaniemi_lat, helsinki_kaisaniemi_lon, startTime);
        Document kuopio = queryPredictions(kuopio_ritoniemi_lat, kuopio_ritoniemi_lon, startTime);
        Document rovaniemi = queryPredictions(rovaniemi_rautatieasema_lat, rovaniemi_rautatieasema_lon, startTime);
        WeatherObservationParser wopHelsinki = new WeatherObservationParser(helsinki);
        WeatherObservationParser wopKuopio = new WeatherObservationParser(kuopio);
        WeatherObservationParser wopRovaniemi = new WeatherObservationParser(rovaniemi);
        Iterator<String[]> helsinkiIter = wopHelsinki.iterator();
        Iterator<String[]> kuopioIter = wopKuopio.iterator();
        Iterator<String[]> rovaniemiIter = wopRovaniemi.iterator();
        DateTime currentTime = startTime;
        Map<DateTime, Double> asd = new TreeMap<>();
        while (helsinkiIter.hasNext() && kuopioIter.hasNext() && rovaniemiIter.hasNext()) {
            double helsinkiPrediction = Double.parseDouble(helsinkiIter.next()[0]);
            double kuopioPrediction = Double.parseDouble(kuopioIter.next()[0]);
            double rovaniemiPrediction = Double.parseDouble(rovaniemiIter.next()[0]);
            int dayOfWeek = currentTime.getDayOfWeek();
            int hourOfDay = currentTime.getHourOfDay();
            double electricityConsumption = predict(helsinkiPrediction, kuopioPrediction, rovaniemiPrediction, dayOfWeek, hourOfDay);
            asd.put(currentTime, electricityConsumption);
            currentTime = currentTime.plusHours(1);
            //System.out.println("helsinki="+helsinkiPrediction+", kuopio="+kuopioPrediction+", rovaniemi="+rovaniemiPrediction);
        }
        Object[] result = asd.entrySet().stream().map(e -> new Object[] {e.getKey().getMillis(), e.getValue()}).toArray(size -> new Object[size]);
        return result;
    }
    
    private static Document queryPredictions(double latitude, double longitude, DateTime startTime) throws URISyntaxException, IOException, ParseException, ParserConfigurationException, SAXException {
        FMIQuery query = new FMIQueryBuilder(Settings.getProperty("api-key"), "fmi::forecast::hirlam::surface::point::multipointcoverage")
        .setTimeStep("60")
        .setParameters("Temperature")
        .setStartTime(FMIUtils.jodaToFMIDate(startTime))
        .setLatLong(latitude, longitude)
        .build();
        return query.execute();
        //return new WeatherObservationParser(query.execute(), DateTime.now(), false);
    }
    
    
    @RequestMapping(value="custom", method = RequestMethod.GET)
    public String predictGet(Map<String, Object> model) {
//        try {
//            getWeatherForecast();
//        } catch(Exception ex) {
//            ex.printStackTrace();
//        }
        System.out.println("predict..");
        model.put("predictionForm", new PredictionForm());
        return "predictionForm";
    }
    
    private double predict(double helsinki, double kuopio, double rovaniemi, int dayOfWeek, int hourOfDay) throws IOException, ClassNotFoundException {
        NormalizationHelper normalizationHelper = (NormalizationHelper) SerializeObject.load(new File("/data/PPML/normalization.bin"));
        System.out.println("normalizationhelper:"+normalizationHelper);
        MLRegression mlModel = (MLRegression) EncogDirectoryPersistence.loadObject(new File("/data/PPML/bestmethod.eg"));
        String[] inputLine = {""+helsinki, ""+kuopio, ""+rovaniemi, ""+dayOfWeek, ""+hourOfDay};
        double[] normalizedInput = new double[normalizationHelper.calculateNormalizedInputCount()];
        normalizationHelper.normalizeInputVector(inputLine, normalizedInput, true);
        MLData input = normalizationHelper.allocateInputVector();
        input.setData(normalizedInput);
        MLData output = mlModel.compute(input);
        return Double.parseDouble(normalizationHelper.denormalizeOutputVectorToString(output)[0]);
    }
    
    @RequestMapping(value="custom", method = RequestMethod.POST)
    public String predictPost(@Valid @ModelAttribute("predictionForm") PredictionForm predictionForm,
            Map<String, Object> model) throws IOException, ClassNotFoundException {
        double result = predict(predictionForm.getHelsinkiTemp(), predictionForm.getKuopioTemp(), predictionForm.getRovaniemiTemp(), predictionForm.getDayOfWeek(), predictionForm.getHourOfDay());
        model.put("predictionResult", result);
        model.put("predictionForm", predictionForm);
        return "predictionFormResult";
    }
    
    
    @RequestMapping("forecast")
    public String asd(Map<String, Object> model) {
        return "test";
    }
}
