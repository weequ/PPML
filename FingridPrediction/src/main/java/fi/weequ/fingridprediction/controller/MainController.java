package fi.weequ.fingridprediction.controller;

import fi.weequ.fingridprediction.form.PredictionForm;
import fi.weequ.fingridprediction.service.ForecastHistoryService;
import fi.weequ.fingridprediction.service.ForecastService;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Map;
import javax.validation.Valid;
import javax.xml.parsers.ParserConfigurationException;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.xml.sax.SAXException;

//http://data.fmi.fi/fmi-apikey//wfs?request=getFeature&storedquery_id=fmi::forecast::hirlam::surface::point::multipointcoverage&place=helsinki&timestep=1&parameters=Temperature
//helsinki kaisaniemi: latlon=60.18,24.94
//kuopio ritoniemi: 62.80,27.90
//rovaniemi rautatieasema: 66.50,25.71
@Controller
public class MainController {
    
    @Autowired
    private ForecastService forecastService;
    
    @Autowired
    private ForecastHistoryService forecastHistoryService;
    
//    private static String[] inputss = new String[] {"temp helsinki kaisaniemi", "temp kuopio ritoniemi", 
//        "temp rovaniemi rautatieasema", "dayofweek",
//        "hourofday"};
    
    
    private DateTime beginningOfCurrentHour() {
        return DateTime.now(DateTimeZone.UTC).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
    }
    
    
    /**
     * 
     * @param data data[0] should be the begining of current hour
     * @return 
     */
    private Object[] toHichartFormat(double[] data) {
        Object[] result = new Object[data.length];
        DateTime time = beginningOfCurrentHour();
        for (int i = 0; i < data.length; i++) {
            Object[] elem = new Object[2];
            elem[0] = time.getMillis();
            elem[1] = data[i];
            result[i] = elem;
            time = time.plusHours(1);
        }
        return result;
    }

    @RequestMapping(value="forecast_electricity", method = RequestMethod.GET)
    public @ResponseBody Object[] forecastElectricity() throws URISyntaxException, IOException, ParseException, ParserConfigurationException, SAXException, ClassNotFoundException {
        double[] electricityForecast = forecastService.getElectricityForecast();
        return toHichartFormat(electricityForecast);
    }
    
    
    
    @RequestMapping(value="weather_forecast", method = RequestMethod.GET)
    public @ResponseBody Object[] weatherForecast() throws ParseException, URISyntaxException, IOException, ParserConfigurationException, SAXException {
        return new Object[] {forecastHelsinkiKaisaniemi(), forecastKuopioRitoniemi(), forecastRovaniemiRautatieasema()};
    }
    
    @RequestMapping(value="wether_forecast_helsinki_kaisaniemi", method = RequestMethod.GET)
    public @ResponseBody Object[] forecastHelsinkiKaisaniemi() throws ParseException, URISyntaxException, IOException, ParserConfigurationException, SAXException  {
        double[] helsinkiForecast = forecastService.getHelsinkiForecast();
        return toHichartFormat(helsinkiForecast);
    }
    
    @RequestMapping(value="wether_forecast_kuopio_ritoniemi", method = RequestMethod.GET)
    public @ResponseBody Object[] forecastKuopioRitoniemi() throws ParseException, URISyntaxException, IOException, ParserConfigurationException, SAXException  {
        double[] kuopioForecast = forecastService.getKuopioForecast();
        return toHichartFormat(kuopioForecast);
    }
    
    @RequestMapping(value="wether_forecast_rovaniemi_rautatieasema", method = RequestMethod.GET)
    public @ResponseBody Object[] forecastRovaniemiRautatieasema() throws ParseException, URISyntaxException, IOException, ParserConfigurationException, SAXException  {
        double[] rovaniemiForecast = forecastService.getRovaniemiForecast();
        return toHichartFormat(rovaniemiForecast);
    }
    
    
    @RequestMapping(value="history", method = RequestMethod.GET)
    public String history() {
        return "history";
    }
    
    
    @RequestMapping(value="forecasthistory/{predictionHours}", method = RequestMethod.GET)
    public @ResponseBody Object[] getForecastHistory(@PathVariable String predictionHours) {
        return forecastHistoryService.getForecastHistory(new DateTime(0), Integer.parseInt(predictionHours));//Since 1970..
    }

    @RequestMapping(value="custom", method = RequestMethod.GET)
    public String customPredictGet(Map<String, Object> model) {
        System.out.println("predict..");
        model.put("predictionForm", new PredictionForm());
        return "predictionForm";
    }
    
    
    @RequestMapping(value="custom", method = RequestMethod.POST)
    public String customPredictPost(@Valid @ModelAttribute("predictionForm") PredictionForm predictionForm,
            Map<String, Object> model) throws IOException, ClassNotFoundException {
        double result = forecastService.predict(predictionForm.getHelsinkiTemp(), predictionForm.getKuopioTemp(), predictionForm.getRovaniemiTemp(), predictionForm.getDayOfWeek(), predictionForm.getHourOfDay());
        model.put("predictionResult", result);
        model.put("predictionForm", predictionForm);
        return "predictionFormResult";
    }
    
    
    @RequestMapping("forecast")
    public String forecast36hours(Map<String, Object> model) {
        return "test";
    }
}
