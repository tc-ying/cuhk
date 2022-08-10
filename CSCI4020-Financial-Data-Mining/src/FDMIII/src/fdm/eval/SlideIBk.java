package fdm.eval;

import weka.classifiers.Evaluation;
import weka.classifiers.evaluation.ConfusionMatrix;
import weka.classifiers.evaluation.NominalPrediction;
import weka.classifiers.lazy.IBk;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

/**
 *     
 * @date		
 * @author		Ying Ting Chung
 */
public class SlideIBk extends IBk	{
	
	private Instances masterData;
	private String[] attributes;
	private ConfusionMatrix confusionMatrix;
	private int windowSize;
	private int horizonSize;
	private Instances trainingSet;
	private Instances testSet;
	private FastVector atts;
	private Instances predData;
	
	public SlideIBk()	{
		super();
	}
	
	public SlideIBk(int k)	{
		super(k);
	}
	
	public SlideIBk(Instances master)	{
		this.masterData = master;
		this.attributes = new String[masterData.numClasses()];
		for (int i = 0; i < attributes.length; i++)	{
			attributes[i] = masterData.classAttribute().value(i);
		}
		atts = new FastVector();
		FastVector labels = new FastVector(); 
		labels.addElement("-1");	// pred_val == 0
		labels.addElement("0"); 
		labels.addElement("1");		// pred_val == 2
		atts.addElement(new Attribute("predictions", labels));
	}
	
	public void setWindowSize(int windowSize)	{this.windowSize = windowSize;}
	
	public void setHorizonSize(int horizonSize)	{this.horizonSize = horizonSize;}
	
	public int getWindowSize()	{return this.windowSize;}
	
	public int getHorizonSize()	{return this.horizonSize;}
	
	public ConfusionMatrix getConfusionMatrix()	{return confusionMatrix;}
	
	public Instances getPredictions()	{return predData;}
	
	public void buildClassifer() throws Exception	{
		IBk kNN = new IBk(getKNN());
		Evaluation eval;
		NominalPrediction nom;
		
		predData = new Instances("Predictions", atts, 0);
		
		confusionMatrix = new ConfusionMatrix(attributes);
		
		for (int i = 0; i < windowSize; i++)	{
			double[] pred_val = new double[predData.numAttributes()];
			pred_val[0] = 1.0;
			predData.add(new Instance(1.0, pred_val));
		}
		for (int t = 0; t + windowSize + horizonSize <= masterData.numInstances(); t++)	{
			double[] pred_val = new double[predData.numAttributes()];
			trainingSet = new Instances(masterData, t, windowSize);
			testSet = new Instances(masterData, t + windowSize, horizonSize);
			kNN.buildClassifier(trainingSet);
			eval = new Evaluation(trainingSet);
			eval.evaluateModel(kNN, testSet);
			nom = (NominalPrediction) eval.predictions().elementAt(0);
			pred_val[0] = nom.predicted();
			predData.add(new Instance(1.0, pred_val));
			confusionMatrix.addPredictions(eval.predictions());
		}
	}
}
