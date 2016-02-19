package fi.weequ.modeltrainer;

import java.io.File;
import org.encog.ConsoleStatusReportable;
import org.encog.ml.MLRegression;
import org.encog.ml.data.MLData;
import org.encog.ml.data.versatile.NormalizationHelper;
import org.encog.ml.data.versatile.VersatileMLDataSet;
import org.encog.ml.data.versatile.columns.ColumnDefinition;
import org.encog.ml.data.versatile.columns.ColumnType;
import org.encog.ml.data.versatile.sources.CSVDataSource;
import org.encog.ml.data.versatile.sources.VersatileDataSource;
import org.encog.ml.factory.MLMethodFactory;
import org.encog.ml.model.EncogModel;
import org.encog.util.csv.CSVFormat;
import org.encog.util.csv.ReadCSV;


public class DataLoader {
    public void load() {
        File sourceFile = new File("");
        
        VersatileDataSource source = new CSVDataSource(sourceFile, false, CSVFormat.DECIMAL_POINT);
        VersatileMLDataSet data = new VersatileMLDataSet(source);
	data.defineSourceColumn("sepal-length", 0, ColumnType.continuous);
	data.defineSourceColumn("sepal-width", 1, ColumnType.continuous);
        data.defineSourceColumn("petal-length", 2, ColumnType.continuous);
        data.defineSourceColumn("petal-width", 3, ColumnType.continuous);
        
        ColumnDefinition outputColumn = data.defineSourceColumn("species", 4, ColumnType.nominal);
        
        data.analyze();
        
        data.defineSingleOutputOthersInput(outputColumn);
        
        EncogModel model = new EncogModel(data);
        model.selectMethod(data, MLMethodFactory.TYPE_FEEDFORWARD);
        
        model.setReport(new ConsoleStatusReportable());
        
        data.normalize();
        
        model.holdBackValidation(0.3, true, 1001);
        
        model.selectTrainingType(data);
        
        MLRegression bestMethod = (MLRegression) model.crossvalidate(5, true);
        
        NormalizationHelper helper = data.getNormHelper();
        
        ReadCSV csv = new ReadCSV(sourceFile, false, CSVFormat.DECIMAL_POINT);
			String[] line = new String[4];
			MLData input = helper.allocateInputVector();
        
    }
}
