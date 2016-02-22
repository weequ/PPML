package fi.weequ.fingridprediction.controller;

import fi.weequ.fingridprediction.form.PredictionForm;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.validation.Valid;
import org.encog.ml.MLRegression;
import org.encog.ml.data.MLData;
import org.encog.ml.data.versatile.NormalizationHelper;
import org.encog.ml.data.versatile.columns.ColumnDefinition;
import org.encog.persist.EncogDirectoryPersistence;
import org.encog.util.obj.SerializeObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

//http://data.fmi.fi/fmi-apikey//wfs?request=getFeature&storedquery_id=fmi::forecast::hirlam::surface::point::multipointcoverage&place=helsinki&timestep=1&parameters=Temperature
//helsinki kaisaniemi: latlon=60.18,24.94
//kuopio ritoniemi: 62.80,27.90
//rovaniemi rautatieasema: 66.50,25.71
@Controller
public class TestController {
    
    private static String[] inputss = new String[] {"temp helsinki kaisaniemi", "temp kuopio ritoniemi", 
        "temp rovaniemi rautatieasema", "dayofweek",
        "hourofday"};
    
    @RequestMapping(value="predict", method = RequestMethod.GET)
    public String predictGet(Map<String, Object> model) {
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
