package fi.weequ.modeltrainer;

import java.io.File;
import java.io.IOException;
import org.encog.ConsoleStatusReportable;
import org.encog.EncogError;
import org.encog.ml.MLMethod;
import org.encog.ml.MLRegression;
import org.encog.ml.TrainingImplementationType;
import org.encog.ml.data.MLDataSet;
import org.encog.ml.data.versatile.NormalizationHelper;
import org.encog.ml.data.versatile.VersatileMLDataSet;
import org.encog.ml.data.versatile.columns.ColumnDefinition;
import org.encog.ml.data.versatile.columns.ColumnType;
import org.encog.ml.data.versatile.sources.CSVDataSource;
import org.encog.ml.data.versatile.sources.VersatileDataSource;
import org.encog.ml.factory.MLMethodFactory;
import org.encog.ml.factory.MLTrainFactory;
import org.encog.ml.model.EncogModel;
import org.encog.ml.model.config.MethodConfig;
import org.encog.ml.train.MLTrain;
import org.encog.ml.train.strategy.StopTrainingStrategy;
import org.encog.neural.networks.BasicNetwork;
import org.encog.persist.EncogDirectoryPersistence;
import org.encog.util.Format;
import org.encog.util.csv.CSVFormat;
import org.encog.util.obj.SerializeObject;


public class Trainer {
    
    private static MLTrain createTrainer(EncogModel model, MLMethod method, MLDataSet dataset) {
		MLTrainFactory trainFactory = new MLTrainFactory();
		MLTrain train = trainFactory.create(method, dataset, MLTrainFactory.TYPE_SVM,
				"");
		return train;
	}
    
    private static MLRegression train(EncogModel model, int iterations, boolean shuffle) {
            MLMethod method = model.createMethod();
            MLTrain train = createTrainer(model, method, model.getTrainingDataset());

            if (train.getImplementationType() == TrainingImplementationType.Iterative) {
                    //StopTrainingStrategy earlyStop = new StopTrainingStrategy(0.0000001,1);
                    //train.addStrategy(earlyStop);

                    StringBuilder line = new StringBuilder();
                    for(int i=1;i<=iterations;i++) {
                            train.iteration();
                            if ((i & 31) == 0) {
                                line.setLength(0);
                                line.append("Iteration #");
                                line.append(train.getIteration());
                                line.append("/");
                                line.append(iterations);
                                line.append(", Training Error: ");
                                line.append(Format.formatDouble(train.getError(), 8));
                                model.getReport().report(0, 0, line.toString());
                            }
                            if (train.isTrainingDone()) break;
                    }
            } else if (train.getImplementationType() == TrainingImplementationType.OnePass) {
                    train.iteration();
                    model.getReport().report(0, 0,
                                    "Trained, Training Error: " + train.getError());
            } else {
                    throw new EncogError("Unsupported training type for EncogModel: "
                                    + train.getImplementationType());
            }
            return (MLRegression)method;
    }

    
    //Modified from encog sources
    public static String suggestModelArchitecture(VersatileMLDataSet dataset) {
        int inputColumns = dataset.getNormHelper().getInputColumns().size();
        int outputColumns = dataset.getNormHelper().getOutputColumns().size();
        int hiddenCount = (int) ((double)(inputColumns+outputColumns) * 1.5);
        StringBuilder result = new StringBuilder();
        result.append("?:B->TANH->");
        result.append(hiddenCount);
//        result.append(":B->TANH->");
//        result.append(hiddenCount);
//        result.append(":B->TANH->");
//        result.append(hiddenCount);
        result.append(":B->TANH->?");
        return result.toString();
    }
    
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        CSVFormat format = new CSVFormat('.', ',');
        VersatileDataSource source = new CSVDataSource(new File("binaryfiles/data2.csv"), true,
					format);
        VersatileMLDataSet data = new VersatileMLDataSet(source);
        data.getNormHelper().setFormat(format);
        ColumnDefinition input = data.defineSourceColumn("temp helsinki kaisaniemi",
                        ColumnType.continuous);
        ColumnDefinition input2 = data.defineSourceColumn("temp kuopio ritoniemi",
                        ColumnType.continuous); 
        ColumnDefinition input3 = data.defineSourceColumn("temp rovaniemi rautatieasema",
                        ColumnType.continuous);   
        ColumnDefinition input4 = data.defineSourceColumn("dayofweek", ColumnType.nominal);
        ColumnDefinition input5 = data.defineSourceColumn("hourofday", ColumnType.nominal);
        ColumnDefinition input6 = data.defineSourceColumn("time", ColumnType.continuous);
        ColumnDefinition output = data.defineSourceColumn("energyConsumption",
                        ColumnType.continuous);
        
        data.analyze();
        data.defineInput(input);
        data.defineInput(input2);
        data.defineInput(input3);
        data.defineInput(input4);
        data.defineInput(input5);
        //data.defineInput(input6);
        data.defineOutput(output);
        
        EncogModel model = new EncogModel(data);
        model.selectMethod(data, MLMethodFactory.TYPE_NEAT);
        //model.selectMethod(data, MLMethodFactory.TYPE_FEEDFORWARD, suggestModelArchitecture(data), MLMethodFactory.TYPE_FEEDFORWARD, suggestModelArchitecture(data));
        //System.out.println(suggestModelArchitecture(data));
        model.setReport(new ConsoleStatusReportable());
        data.normalize();
        
        model.holdBackValidation(0.10, true, 1001);
        
        model.selectTrainingType(data);
        
        //MethodConfig config = model.getMethodConfigurations().get(MLMethodFactory.TYPE_FEEDFORWARD);
        //System.out.println("suggested training type:"+config.suggestTrainingType());
//        System.out.println("suggested training args:"+config.suggestTrainingArgs(config.suggestTrainingType()));
//        System.out.println("asd1:"+config.suggestTrainingArgs(MLMethodFactory.TYPE_FEEDFORWARD));
//        System.out.println("asd:"+config.suggestModelArchitecture(data));
        
        
        //MLRegression bestMethod = train(model, 1000000, true);
        MLRegression bestMethod = (MLRegression) model.crossvalidate(10, true);
        if (bestMethod instanceof BasicNetwork) {
            BasicNetwork bs = (BasicNetwork) bestMethod;
            System.out.println("bs...");
            System.out.println(bs.getLayerCount());
        }
        
        System.out.println(bestMethod);
        
        
        System.out.println("Training error: "
					+ model.calculateError(bestMethod,
							model.getTrainingDataset()));
        System.out.println("Validation error: "
                        + model.calculateError(bestMethod,
                                        model.getValidationDataset()));
        
        // Display our normalization parameters.
        NormalizationHelper helper = data.getNormHelper();
        System.out.println("helper:"+helper);
        SerializeObject.save(new File("binaryfiles/normalizationAVG.eg"), helper);
        //NormalizationHelper helperloaded = (NormalizationHelper) SerializeObject.load(new File("binaryfiles/normalization.eg"));
        //System.out.println("helperloaded"+helperloaded
        //);
        //EncogDirectoryPersistence.saveObject(new File("normalization"), helper);
        //System.out.println(helper.toString());

        // Display the final model.
        System.out.println("Final model: " + bestMethod);
        EncogDirectoryPersistence.saveObject(new File("binaryfiles/bestmethodAVG.EG"), bestMethod);
        //MLRegression bm2 = (MLRegression) EncogDirectoryPersistence.loadObject(new File("binaryfiles/bestmethod2.EG"));
        //System.out.println("bm2:"+bm2);
    }
}
