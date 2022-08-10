package fdm;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JFrame;

import weka.classifiers.evaluation.TwoClassStats;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

import fdm.etl.*;
import fdm.eval.SlideIBk;
import fdm.ods.*;
import fdm.settings.Settings;

/**
 * @description	Batch routines in data mining process
 * @date		
 * @author		Ying Ting Chung
 */
public class Main {
	/* FileName = TableName + ".csv" */
	private static DB db = null;
	private static WekaFilter f = null;
	/**
	 * Routine. Download data from various sites. Save data as flat files in the directory of ODS.
	 * Convert files to CSV format preferably.
	 */
	public static void collect()	{
		String odsDir = Settings.getProperty("ODS.dir");
		Crawler c = new Crawler();
		c.YahooBatchDownload(odsDir + "temp\\", odsDir + "Listed ETFs.csv");
	}
	
	/** 
	 * Routine. Connect to MySQL. Define correct table schema according to data file.
	 * Batch import from CSV to MySQL table. 
	 * @throws Exception 
	 */
	public static void CSVtoMySQL() throws Exception	{
		db.importYahooCSV(Settings.getProperty("ODS.dir") + "quote\\index\\", "_HSI.csv");
		db.importDealCSV(Settings.getProperty("ODS.dir"), "hkmkt.csv");
	}
	
	/**
	 * Routine. Connect to DBMS. Batch import from ARFF to DBMS relation.
	 * @throws Exception 
	 */
	public static void ARFFtoDB() throws Exception	{
		db.importARFF("hsi_nstopBottoms", Settings.getProperty("DW.dir"), "hsi_nstopBottoms.arff");
		db.importARFF("hsi_nstopTops", Settings.getProperty("DW.dir"), "hsi_nstopTops.arff");
	}
	
	/**
	 * Routine. Extract, transform, and load.
	 * SQL query to join/drop columns and rows. Feature extraction and selection, record sub-sampling.
	 * @throws Exception
	 */
	public static void etl() throws Exception	{
		Extraction extract = new Extraction();
		Instances data;
		
//		data = db.exportResultSet("SELECT * FROM (SELECT date, h FROM _hsi) A INNER JOIN hsi_sma USING(date) ORDER BY date");
		data = db.exportResultSet("SELECT date, adj_c, l FROM _hsi ORDER BY date");

		System.out.println(data.toSummaryString());
		data = extract.stopBottoms(data, 1, 2, 2);
		
//		ArffSaver saver = new ArffSaver();
//		saver.setInstances(data);
//		saver.setFile(new File(Settings.getProperty("DW_dir") + ".arff"));
//		saver.writeBatch();
		data.deleteAttributeAt(1);
		data.deleteAttributeAt(1);

		System.out.println(data.toSummaryString());
		db.importInstances("hsi_stop2bottom", data);
	}
	
	/**
	 * Routine. Exploratory data analysis. 
	 * @throws Exception 
	 */
	public static void eda() throws Exception	{
		Instances data = null;
		
		data = db.exportResultSet("SELECT * FROM" +
				" (SELECT date, adj_c FROM _hsi) A" +
				" NATURAL JOIN (SELECT date, ema8slope, ema13slope, ema18slope FROM hsi_emaslope) C" +
//				" NATURAL JOIN (SELECT date, devema8, devema16 FROM hsi_ldevema) D" +
//				" NATURAL JOIN (SELECT date, devsma9, devsma20 FROM hsi_ldevsma) E" +
				" NATURAL JOIN (SELECT date, rocp5, rocp6 FROM hsi_rocp) F" +
				" NATURAL JOIN (SELECT date, rsi13 FROM hsi_rsi) B" +
				" NATURAL JOIN  (SELECT date, 14bottom FROM hsi_stop1bottom) ZA" +
//				" NATURAL JOIN  (SELECT date, 5bottom AS stop5bottom FROM hsi_stop0_0bottom) ZB" +
//				" NATURAL JOIN  (SELECT date, 14bottom FROM hsi_bottom) ZC" +
				" WHERE date > '2006-01-01'" +
				" ORDER BY date");
		
//		data = f.fAddID("1", "t", data);
//		data = f.fNumericToNominal("last", data);
		
		System.out.println(data.toSummaryString());
		
		SlidePlot2D plotter = new SlidePlot2D(data);
		plotter.setY(1);
		plotter.setX(0);
		plotter.setC(2);
		plotter.setTime(650);
		plotter.setWindowSize(75);
		plotter.updatePlot();
		
		JFrame frame = new JFrame("sliding window plot");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    frame.getContentPane().setLayout(new BorderLayout());
	    frame.setSize(new Dimension(1024, 800));
	    frame.setLocationRelativeTo(null);
	    frame.addKeyListener(plotter);
	    frame.getContentPane().add(plotter);
	    frame.setVisible(true);
	}
	
	/**
	 * Routine. k-NN Experiment.
	 * @param windowSize - look-back period
	 * @param horizonSize - look-ahead period
	 * @throws Exception
	 */
	public static void kNNexperiment(String param) throws Exception	{
		StringBuilder sb = new StringBuilder();
		File log = new File(Settings.getProperty("log.dir") + "tmp.txt");
		File csvlog = new File(Settings.getProperty("log.dir") + "tmp.csv");
		BufferedWriter bw = new BufferedWriter(new FileWriter(log));
		BufferedWriter csvbw = new BufferedWriter(new FileWriter(csvlog));
		
		DateFormat dateFormatter = new SimpleDateFormat("MMddHHmmss"); 
		
		TwoClassStats stat;
		
		Instances masterData = db.exportResultSet("SELECT * FROM" +
				" (SELECT date, adj_c FROM _hsi) A" +
				" NATURAL JOIN (SELECT date, ema8slope, ema13slope, ema18slope FROM hsi_emaslope) C" +
				" NATURAL JOIN (SELECT date, sma22slope FROM hsi_smaslope) H" +
//				" NATURAL JOIN (SELECT date, kama17slope, kama21slope FROM hsi_kamaslope) I" +	// recall
				" NATURAL JOIN (SELECT date, rocp5, rocp6 FROM hsi_rocp) D" +
				" NATURAL JOIN (SELECT date, devema8, devema16 FROM hsi_ldevema) E" +
				" NATURAL JOIN (SELECT date, devsma9, devsma22 FROM hsi_ldevsma) F" +
				" NATURAL JOIN (SELECT date, devsma10 FROM hsi_hdevsma) G" +
				" NATURAL JOIN (SELECT date, gap FROM hsi_gap) I" +
				" NATURAL JOIN (SELECT date, rsi13 FROM hsi_rsi) B" +
				" NATURAL JOIN (SELECT date, 14bottom FROM hsi_stop1bottom) Z" +
//				" NATURAL JOIN (SELECT date, 5bottom FROM hsi_hold0bottom) Z" +
				" WHERE date > '2006-01-01'" +
				" ORDER BY date");
		
		Instances inputData = new Instances(masterData);
		if (inputData.classIndex() == -1)	{
			inputData.setClassIndex(inputData.numAttributes() - 1);
		}
		inputData.deleteAttributeAt(0);	// delete date attribute
		inputData.deleteAttributeAt(0);	// delete price attribute
		inputData = f.fAddID("1", "t", inputData);
//		inputData = f.fAddExpression("(a1/10)^3", inputData);
		inputData = f.fAddExpression("sin(a1/6)", inputData);
		inputData = f.fAddExpression("sin(a1/4)", inputData);
		inputData = f.fAddExpression("exp(a1/20)", inputData);
		inputData.deleteAttributeAt(0); // delete time index
		inputData = f.fNumericToNominal(Integer.toString(inputData.classIndex()+1), inputData);
		System.out.println(inputData.toSummaryString());
		bw.write(inputData.toSummaryString());
		bw.newLine();

		/* Precision:	measure of risk
		 * Recall:		measure of opportunity */
		String header = "K, WindowSize, Precision, Recall, F1";
		System.out.println(header);
		bw.write(header);
		bw.newLine();
		csvbw.write(header);
		csvbw.newLine();
		
		SlideIBk kNN = new SlideIBk(inputData);
//		for (int k = 3; k <= 25; k=k+2)	{
		for (int k = 3; k <= 3; k=k+2)	{	// do not bother for now
			for(int windowSize = 6; windowSize <= 360 ; windowSize=windowSize+2)	{
//			for(int windowSize = 45; windowSize <= 45; windowSize=windowSize+1)	{
				if (windowSize < k)	continue;
				sb.delete(0, sb.length());
//				kNN.setOptions(weka.core.Utils.splitOptions("-I"));
				kNN.setKNN(k);
				kNN.setWindowSize(windowSize);
				kNN.setHorizonSize(1);
				kNN.buildClassifer();
				
				stat = kNN.getConfusionMatrix().getTwoClassStats(2);
				
				sb.append(kNN.getKNN()).append(", ").append(kNN.getWindowSize());
				sb.append(", ").append(String.format("%.3f", stat.getPrecision()));
				sb.append(", ").append(String.format("%.3f", stat.getRecall()));
				sb.append(", ").append(String.format("%.3f", stat.getFMeasure()));
				System.out.println(sb.toString());
//				System.out.println(kNN.getConfusionMatrix());
				bw.write(sb.toString());
				bw.newLine();
				csvbw.write(sb.toString());
				csvbw.newLine();
			}
		}
		bw.close();
		csvbw.close();

		String newPath = Settings.getProperty("log.dir") + "e" + dateFormatter.format(new Date());
		log.renameTo(new File(newPath + ".txt"));
		csvlog.renameTo(new File(newPath + ".csv"));
		csvlog = new File(newPath + ".csv");
		// 'precision' is a reserved word in MySQL
//		db.importCSV("k INT,windowSize INT,PPV REAL,recall REAL,F1 REAL", csvlog.getParentFile().getCanonicalPath() + "\\", csvlog.getName());
		
		Instances rst = Instances.mergeInstances(f.fNumericToNominal("last", masterData), kNN.getPredictions());
		System.out.println(rst.toSummaryString());
//		SlidePlot2D plotter = new SlidePlot2D(rst);
//		plotter.setY(1);
//		plotter.setX(0);
//		plotter.setC(14);
//		plotter.setTime(650);
//		plotter.setWindowSize(200);
//		plotter.updatePlot();
//		
//		JFrame frame = new JFrame("sliding window experiment");
//		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//	    frame.getContentPane().setLayout(new BorderLayout());
//	    frame.setSize(new Dimension(800, 600));
//	    frame.setLocationRelativeTo(null);
//	    frame.addKeyListener(plotter);
//	    frame.getContentPane().add(plotter);
//	    frame.setVisible(true);
	}
	
	public static void DEMO_visual() throws Exception	{
		Instances data = db.exportResultSet("SELECT * FROM" +
				" (SELECT date, adj_c FROM _hsi) A" +
				" NATURAL JOIN (SELECT * FROM hsi_rsi) B" +
				" NATURAL JOIN (SELECT * FROM hsi_emaslope) C" +
				" NATURAL JOIN (SELECT * FROM hsi_ldevema) D" +
				" NATURAL JOIN (SELECT date, devsma9, devsma20 FROM hsi_ldevsma) E" +
				" NATURAL JOIN (SELECT * FROM hsi_rocp) F" +
				" NATURAL JOIN (SELECT * FROM hsi_gap) G" +
//				" NATURAL JOIN  (SELECT date, 14bottom FROM hsi_stop1bottom) ZA" +
				" NATURAL JOIN  (SELECT date, 8bottom AS 5bottom FROM hsi_hold0bottom) ZB" +
				" WHERE date > '2006-01-01'" +
				" ORDER BY date");
		
//		data = f.fNumericToNominal("last", data);
		System.out.println(data.toSummaryString());
		
		SlidePlot2D plotter = new SlidePlot2D(data);
		plotter.setY(1);
		plotter.setX(0);
		plotter.setC(84);
		plotter.setTime(600);
		plotter.setWindowSize(120);
		plotter.updatePlot();
		
		JFrame frame = new JFrame("sliding window plot");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    frame.getContentPane().setLayout(new BorderLayout());
	    frame.setSize(new Dimension(1024, 800));
	    frame.setLocationRelativeTo(null);
	    frame.addKeyListener(plotter);
	    frame.getContentPane().add(plotter);
	    frame.setVisible(true);
	}
	
	public static void DEMO_hold() throws Exception	{
		Instances data = db.exportResultSet("SELECT * FROM" +
				" (SELECT date, adj_c FROM _hsi) A" +
				" NATURAL JOIN (SELECT * FROM hsi_hold5bottom) Z" +
				" WHERE date > '2006-01-01'" +
				" ORDER BY date");
		System.out.println(data.toSummaryString());
		
		SlidePlot2D plotter = new SlidePlot2D(data);
		plotter.setY(1);
		plotter.setX(0);
		plotter.setC(2);
		plotter.setTime(600);
		plotter.setWindowSize(360);
		plotter.updatePlot();
		
		JFrame frame = new JFrame("5% above current price after N days");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    frame.getContentPane().setLayout(new BorderLayout());
	    frame.setSize(new Dimension(1024, 800));
	    frame.setLocationRelativeTo(null);
	    frame.addKeyListener(plotter);
	    frame.getContentPane().add(plotter);
	    frame.setVisible(true);
	}
	
	public static void DEMO_stop() throws Exception	{
		Instances data = db.exportResultSet("SELECT * FROM" +
				" (SELECT date, adj_c FROM _hsi) A" +
				" NATURAL JOIN (SELECT * FROM hsi_stop0_5top) Z" +
				" WHERE date > '2006-01-01'" +
				" ORDER BY date");
		System.out.println(data.toSummaryString());
		
		SlidePlot2D plotter = new SlidePlot2D(data);
		plotter.setY(1);
		plotter.setX(0);
		plotter.setC(2);
		plotter.setTime(600);
		plotter.setWindowSize(360);
		plotter.updatePlot();
		
		JFrame frame = new JFrame("1% stop-loss margin");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    frame.getContentPane().setLayout(new BorderLayout());
	    frame.setSize(new Dimension(1024, 800));
	    frame.setLocationRelativeTo(null);
	    frame.addKeyListener(plotter);
	    frame.getContentPane().add(plotter);
	    frame.setVisible(true);
	}
	
	/* Workflow:
	 * 1. collect()
	 * 2. Identify inconsistency, duplication in data; handle missing values
	 * 3. CSVtoMySQL() / ARFFtoDB()
	 * 4. etl()
	 * 5. eda()
	 * 6. experiment()
	 */
	/**
	 * Organise and execute data mining routines.
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			f = new WekaFilter();
			db = new DB();
//			collect();
//			CSVtoMySQL();
//			ARFFtoDB();
//			etl();
//			eda();
			for (int i = 3; i < 4; i++)	{
				// kNNexperiment(Integer.toString(i));
			}
			
			
//			DEMO_visual();
//			DEMO_hold();
			DEMO_stop();
			
			
			db.disconnectFromDatabase();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
