package fi.weequ.fingridprediction.controller;

import fi.weequ.fingridprediction.form.PredictionForm;
import fi.weequ.fmidatafetcher.FMIQuery;
import fi.weequ.fmidatafetcher.FMIQueryBuilder;
import fi.weequ.fmidatafetcher.Settings;
import fi.weequ.fmidatafetcher.WeatherObservationParser;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.validation.Valid;
import javax.xml.parsers.ParserConfigurationException;
import org.encog.ml.MLRegression;
import org.encog.ml.data.MLData;
import org.encog.ml.data.versatile.NormalizationHelper;
import org.encog.ml.data.versatile.columns.ColumnDefinition;
import org.encog.persist.EncogDirectoryPersistence;
import org.encog.util.obj.SerializeObject;
import org.joda.time.DateTime;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.xml.sax.SAXException;

//http://data.fmi.fi/fmi-apikey//wfs?request=getFeature&storedquery_id=fmi::forecast::hirlam::surface::point::multipointcoverage&place=helsinki&timestep=1&parameters=Temperature
//helsinki kaisaniemi: latlon=60.18,24.94
//kuopio ritoniemi: 62.80,27.90
//rovaniemi rautatieasema: 66.50,25.71
@Controller
public class TestController {
    
    private final static double helsinki_kaisaniemi_lat = 60.18;
    private final static double helsinki_kaisaniemi_lon = 24.94;
    private final static double kuopio_ritoniemi_lat = 62.80;
    private final static double kuopio_ritoniemi_lon = 27.90;
    private final static double rovaniemi_rautatieasema_lat = 66.50;
    private final static double rovaniemi_rautatieasema_lon = 25.71;
    
    private static String[] inputss = new String[] {"temp helsinki kaisaniemi", "temp kuopio ritoniemi", 
        "temp rovaniemi rautatieasema", "dayofweek",
        "hourofday"};
    
    
    private static void getWeatherForecast() throws URISyntaxException, IOException, ParseException, ParserConfigurationException, SAXException {
        WeatherObservationParser helsinki = queryPredictions(helsinki_kaisaniemi_lat, helsinki_kaisaniemi_lon);
        WeatherObservationParser kuopio = queryPredictions(kuopio_ritoniemi_lat, kuopio_ritoniemi_lon);
        WeatherObservationParser rovaniemi = queryPredictions(rovaniemi_rautatieasema_lat, rovaniemi_rautatieasema_lon);
        Iterator<String[]> helsinkiIter = helsinki.iterator();
        Iterator<String[]> kuopioIter = kuopio.iterator();
        Iterator<String[]> rovaniemiIter = rovaniemi.iterator();
        while (helsinkiIter.hasNext() && kuopioIter.hasNext() && rovaniemiIter.hasNext()) {
            double helsinkiPrediction = Double.parseDouble(helsinkiIter.next()[0]);
            double kuopioPrediction = Double.parseDouble(kuopioIter.next()[0]);
            double rovaniemiPrediction = Double.parseDouble(rovaniemiIter.next()[0]);
            System.out.println("helsinki="+helsinkiPrediction+", kuopio="+kuopioPrediction+", rovaniemi="+rovaniemiPrediction);
        }
    }
    
    private static WeatherObservationParser queryPredictions(double latitude, double longitude) throws URISyntaxException, IOException, ParseException, ParserConfigurationException, SAXException {
        FMIQuery query = new FMIQueryBuilder(Settings.getProperty("api-key"), "fmi::forecast::hirlam::surface::point::multipointcoverage")
        .setTimeStep("60")
        .setParameters("Temperature")
        .setLatLong(latitude, longitude)
        .build();
        return new WeatherObservationParser(query.execute(), DateTime.now(), false);
    }
    
    
    @RequestMapping(value="predict", method = RequestMethod.GET)
    public String predictGet(Map<String, Object> model) {
        try {
            getWeatherForecast();
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        System.out.println("predict..");
        model.put("predictionForm", new PredictionForm());
        return "predictionForm";
    }
    
    @RequestMapping(value="predict", method = RequestMethod.POST)
    public String predictPost(@Valid @ModelAttribute("predictionForm") PredictionForm predictionForm,
            Map<String, Object> model) throws IOException, ClassNotFoundException {
        NormalizationHelper normalizationHelper = (NormalizationHelper) SerializeObject.load(new File("/data/PPML/normalization.bin"));
        MLRegression mlModel = (MLRegression) EncogDirectoryPersistence.loadObject(new File("/data/PPML/bestmethod.eg"));
        List<ColumnDefinition> inputColumns = normalizationHelper.getInputColumns();
        String[] inputLine = new String[inputColumns.size()];
        int i = 0;
        for (ColumnDefinition inputColumn : inputColumns) {
            if (inputColumn.getName().equals("temp helsinki kaisaniemi")) {
                inputLine[i] = ""+predictionForm.getHelsinkiTemp();
            } else if (inputColumn.getName().equals("temp kuopio ritoniemi")) {
                inputLine[i] = ""+predictionForm.getKuopioTemp();
            } else if (inputColumn.getName().equals("temp rovaniemi rautatieasema")) {
                inputLine[i] = ""+predictionForm.getRovaniemiTemp();
            } else if (inputColumn.getName().equals("dayofweek")) {
                inputLine[i] = ""+predictionForm.getDayOfWeek();
            } else if (inputColumn.getName().equals("hourofday")) {
                inputLine[i] = ""+predictionForm.getHourOfDay();
            }
            i++;
        }
        double[] normalizedInput = new double[normalizationHelper.calculateNormalizedInputCount()];
        normalizationHelper.normalizeInputVector(inputLine, normalizedInput, true);
        MLData input = normalizationHelper.allocateInputVector();
        input.setData(normalizedInput);
        MLData output = mlModel.compute(input);
        String[] denormalizedOutput = normalizationHelper.denormalizeOutputVectorToString(output);
        System.out.println(Arrays.toString(denormalizedOutput));
        model.put("predictionResult", denormalizedOutput[0]);
        model.put("predictionForm", predictionForm);
        return "predictionFormResult";
    }
    
    
    @RequestMapping("hoi")
    public String asd(Map<String, Object> model) {
        System.out.println("asd()...");
        model.put("asd", true);
        return "test";
    }
}
