package fdm.etl;

import com.tictactec.ta.lib.*;
import weka.core.*;

/**
 * @description	Extract new attribute from existing ones
 * @date		
 * @author		Ying Ting Chung
 */
public class Extraction {
	
	/**
	 * Convert double array to WEKA instances
	 * 
	 * @param attName - attribute name
	 * @param col - double array
	 * @return single column {@code weka.core.Instance}
	 */
	public Instances doubleArrayToAttribute(String attName, double[] col)	{
		FastVector atts = new FastVector(); 
		atts.addElement(new Attribute(attName));
		double[] vals;
		Instances newCol = new Instances(attName, atts, 0);
		for(int i = 0; i < col.length; i++)	{
			vals = new double[1];
			vals[0] = col[i];
			newCol.add(new Instance(1, vals));
		}
		return newCol;
	}
	
	/**
	 * After N time points, defined price arrives above current price by certain percentage.
	 * Positive label represented by 1.0.
	 * Profit taking is safe for long and hold at the end of look ahead period, if low price as the future series.    
	 * 
	 * @param curPrice - asset price measure at current time point
	 * @param futurePrice - asset price measure at {@code horizonSize} later
	 * @param horizonSize - number of time points look ahead
	 * @param ror - rate of return desired
	 * @return bottoms - 
	 */
	private double[] bottom(double[] curPrice, double[] futurePrice, int horizonSize, double ror)	{
		int timePointCnt = curPrice.length;
		double[] bottoms = new double[timePointCnt];
		
		for(int t = 0; t < timePointCnt; t++){
			try	{
				if(curPrice[t] * (1+ror) < (futurePrice[t+horizonSize]))
					bottoms[t] = 1;
				else
					bottoms[t] = -1;
			} catch (ArrayIndexOutOfBoundsException e)	{
				bottoms[t] = 0;
			}
		}
		return bottoms;
	}
	
	/**
	 * Within N time points, defined future price does not shake lower than current price.
	 * Positive class represented by 1.0. Profit taking possible for long and hold within look ahead period
	 * Negative class represented by -1.0.
	 * 
	 * @param curPrice - asset price measure at current time point
	 * @param futurePrice - asset price measure at {@code horizonSize} later
	 * @param horizonSize - number of time points look ahead
	 * @param stopMargin - stop loss in percentage of current price
	 * @return bottoms -
	 */
	private double[] nBottom(double[] curPrice, double[] futurePrice, int horizonSize, double stopMargin)	{
		int timePointCnt = curPrice.length;
		double[] bottoms = new double[timePointCnt];
		
		for(int t = 0; t < timePointCnt; t++){
			bottoms[t] = 1;
			try {
				for(int j = 1; j < horizonSize; j++){
					if(curPrice[t] * (1 - stopMargin/100) > (futurePrice[t+j]))	{
						bottoms[t] = -1;
					}
				}
				if(curPrice[t] > (futurePrice[t+horizonSize]))	{
					bottoms[t] = -1;
				}
			} catch (ArrayIndexOutOfBoundsException e)	{
				bottoms[t] = 0;}
		}
		return bottoms;
	}
	
	/**
	 * After N time points, defined price arrives lower than the current price by certain percentage.
	 * Positive label represented by 1.0.
	 * Profit taking is safe for short and hold at the end of look ahead period, if low price as the future series.   
	 * 
	 * @param curPrice - asset price measure at current time point
	 * @param futurePrice - asset price measure at {@code horizonSize} later
	 * @param horizonSize - number of time points look ahead
	 * @param ror - rate of return desired
	 * @return -
	 */
	private double[] top(double[] curPrice, double[] futurePrice, int horizonSize, double ror)  {
        int timePointCnt = curPrice.length;
        double[] tops = new double[timePointCnt];
        
        for(int t = 0; t < timePointCnt; t++){
            try {
                if(curPrice[t] * (1-ror) > (futurePrice[t+horizonSize]))
                    tops[t] = 1;
                else
                    tops[t] = -1;
            } catch (ArrayIndexOutOfBoundsException e){
                tops[t] = 0;
            }
        }
        return tops;
	}
	
	/**
	 * Within N time points, defined future price does not shake higher than current price.
	 * Profit taking possible for short and hold within look ahead period
	 * 
	 * @param curPrice - asset price measure at current time point
	 * @param futurePrice - asset price measure at {@code horizonSize} later
	 * @param horizonSize - number of time points look ahead
	 * @param stopMargin - stop loss in percentage of current price
	 * @return tops -
	 */
	private double[] nTop(double[] curPrice, double[] futurePrice, int horizonSize, double stopMargin)	{
		int timePointCnt = curPrice.length;
		double[] tops = new double[timePointCnt];
		
		for(int t = 0; t < timePointCnt; t++){
			tops[t] = 1;
			try	{
				for(int j = 1; j < horizonSize; j++){
					if(curPrice[t] * (1 + stopMargin/100) < (futurePrice[t+j]))	{
						tops[t] = -1;
					}
				}
				if(curPrice[t] < (futurePrice[t+horizonSize]))	{
					tops[t] = -1;
				}
			} catch (ArrayIndexOutOfBoundsException e)	{
				tops[t] = 0;}	
		}
		return tops;
	}
	
	/**
	 * Extract long-and-hold bottoms of varied holding period.
	 * 
	 * @param inst - {@code weka.core.Instances} containing series to be extracted 
	 * @param index1 - attribute index of series to be extracted
	 * @param index2 - attribute index of look-ahead series to be extracted
	 * @param ror - rate of return
	 * @return - input instances merged with newly extracted instances
	 */
	public Instances holdBottoms(Instances inst, int index1, int index2, double ror)	{
		double[] close = inst.attributeToDoubleArray(index1);
		double[] low = inst.attributeToDoubleArray(index2);
		double[] nBottoms;
		for(int horizonSize = 1; horizonSize < 41; horizonSize++)	{
			nBottoms = bottom(close, low, horizonSize, ror);
			inst = Instances.mergeInstances(inst, doubleArrayToAttribute(horizonSize + "bottom", nBottoms));
		}
		return inst;
	}
	
	/**
	 * Extract long and stop-loss bottoms of varied horizon size.
	 * 
	 * @param inst - {@code weka.core.Instances} containing series to be extracted 
	 * @param index1 - attribute index of series to be extracted
	 * @param index2 - attribute index of look-ahead series to be extracted
	 * @param stopMargin - e.g. 0.5 means 0.5%, not 50%
	 * @return - input instances merged with newly extracted instances
	 * @throws Exception
	 */
	public Instances stopBottoms(Instances inst, int index1, int index2, double stopMargin) throws Exception	{
		double[] close = inst.attributeToDoubleArray(index1);
		double[] low = inst.attributeToDoubleArray(index2);
		double[] nBottoms;
		for(int horizonSize = 1; horizonSize < 41; horizonSize++)	{
			nBottoms = nBottom(close, low, horizonSize, stopMargin);
			inst = Instances.mergeInstances(inst, doubleArrayToAttribute(horizonSize + "bottom", nBottoms));
		}
		return inst;
	}
	
	/**
	 * Extract short-and-hold tops of varied holding period.
	 * 
	 * @param inst - {@code weka.core.Instances} containing series to be extracted 
	 * @param index1 - attribute index of series to be extracted
	 * @param index2 - attribute index of look-ahead series to be extracted
	 * @return - input instances merged with newly extracted instances
	 */
	public Instances holdTops(Instances inst, int index1, int index2, double ror)	{
		double[] close = inst.attributeToDoubleArray(index1);
		double[] high = inst.attributeToDoubleArray(index2);
		double[] nTops;
		for(int horizonSize = 1; horizonSize < 41; horizonSize++)	{
			nTops = top(close, high, horizonSize, ror);
			inst = Instances.mergeInstances(inst, doubleArrayToAttribute(horizonSize + "top", nTops));
		}
		return inst;
	}
	
	/**
	 * Extract short and stop-loss tops of varied horizon size.
	 * 
	 * @param inst - {@code weka.core.Instances} containing series to be extracted 
	 * @param index1 - attribute index of series to be extracted
	 * @param index2 - attribute index of look-ahead series to be extracted
	 * @param stopMargin - e.g. 0.5 means 0.5%, not 50%
	 * @return - input instances merged with newly extracted instances
	 * @throws Exception
	 */
	public Instances stopTops(Instances inst, int index1, int index2, double stopMargin) throws Exception	{
		double[] close = inst.attributeToDoubleArray(index1);
		double[] high = inst.attributeToDoubleArray(index2);
		double[] nTops;
		for(int horizonSize = 1; horizonSize < 41; horizonSize++)	{
			nTops = nTop(close, high, horizonSize, stopMargin);
			inst = Instances.mergeInstances(inst, doubleArrayToAttribute(horizonSize + "top", nTops));
		}
		return inst;
	}
	
	/**
	 * Extract percentage gap open.
	 * 
	 * @param inst
	 * @param O - open price
	 * @param C - close price
	 * @return
	 * @throws Exception
	 */
	public Instances gap(Instances inst, int O, int C) throws Exception	{
		int timePointCnt = inst.numInstances();
		fdm.etl.WekaFilter f = new WekaFilter();
		inst = f.fTimeSeriesTranslate(inst, Integer.toString(C+1), -1);
		double[] open = inst.attributeToDoubleArray(O);
		double[] yesterday_close = inst.attributeToDoubleArray(C);
		double[] gap = new double[timePointCnt];
		for(int t = 0; t < timePointCnt; t++)	{
			gap[t] = (open[t]-yesterday_close[t]) / yesterday_close[t]; 
		}
		return Instances.mergeInstances(inst, doubleArrayToAttribute("gap", gap));
	}
	
	/**
	 * Extract reversals.
	 * 1.0 represents trend reversal. -1.0 represents trend continuation. 0.0 represents undefined.
	 * 
	 * @param inst - {@code weka.core.Instances} containing bottom and top series to be extracted
	 * @param index1 - attribute index of bottom series to be extracted
	 * @param index2 - attribute index of top series to be extracted
	 * @return - input instances merged with newly extracted instances
	 * @throws Exception
	 */
	public Instances reversals(Instances inst, int index1, int index2) throws Exception	{
		double[] bottoms = inst.attributeToDoubleArray(index1);
		double[] tops = inst.attributeToDoubleArray(index2);
		int timePointCnt = bottoms.length;
		double[] reversals = new double[timePointCnt];
		for(int i = 0; i < timePointCnt; i++){
			if(bottoms[i] == -1 && tops[i] == -1)	reversals[i] = -1;
			if(bottoms[i] == 1 || tops[i] == 1)	reversals[i] = 1;
			if(bottoms[i] == 0 || tops[i] == 0)	reversals[i] = 0;
		}
		inst = Instances.mergeInstances(inst, doubleArrayToAttribute("reversal", reversals));
		return inst;
	}
	
	/**
	 * Extract SMA of varied smoothing period. 
	 * @param inst - {@code weka.core.Instances} containing series to be extracted 
	 * @param index - attribute index of series to be extracted
	 * @return - input instances merged with newly extracted instances
	 * @throws Exception
	 */
	public Instances SMA(Instances inst, int index) throws Exception   {
		double[] series = inst.attributeToDoubleArray(index);
		int timePointCnt = series.length;
		double[] smas = new double[timePointCnt];
	    Core TA = new Core();
	    MInteger outBegIdx = new MInteger();
	    MInteger outNBElement = new MInteger();
	    for (int optInTimePeriod = 2; optInTimePeriod < 26; optInTimePeriod++)   {
	        TA.sma(0, timePointCnt-1, series, optInTimePeriod, outBegIdx, outNBElement, smas);
	        for(int i = timePointCnt-1; i - outBegIdx.value > -1; i--)   {
	            smas[i] = smas[i - outBegIdx.value];
	        }
	        for(int i = outBegIdx.value - 1; i > -1; i--){
	            smas[i] = 0;
	        }
	        inst = Instances.mergeInstances(inst, doubleArrayToAttribute("sma" + optInTimePeriod, smas));
	    }
	    return inst;
	}
	
	/**
	 * Extract EMA of varied smoothing period. 
	 * @param inst - {@code weka.core.Instances} containing series to be extracted 
	 * @param index - attribute index of series to be extracted
	 * @return - input instances merged with newly extracted instances
	 * @throws Exception
	 */
	public Instances EMA(Instances inst, int index) throws Exception   {
		double[] series = inst.attributeToDoubleArray(index);
		int timePointCnt = series.length;
		double[] emas = new double[timePointCnt];
        Core TA = new Core();
        MInteger outBegIdx = new MInteger();
        MInteger outNBElement = new MInteger();
        for (int optInTimePeriod = 2; optInTimePeriod < 26; optInTimePeriod++)   {
            TA.ema(0, timePointCnt-1, series, optInTimePeriod, outBegIdx, outNBElement, emas);
            for(int i = timePointCnt-1; i - outBegIdx.value > -1; i--)   {
                emas[i] = emas[i - outBegIdx.value];
            }
            for(int i = outBegIdx.value - 1; i > -1; i--){
                emas[i] = 0;
            }            
            inst = Instances.mergeInstances(inst, doubleArrayToAttribute("ema" + optInTimePeriod, emas));
	    }
	    return inst;
    }
	
	/**
	 * Extract KAMA of varied smoothing period. 
	 * @param inst - {@code weka.core.Instances} containing series to be extracted 
	 * @param index - attribute index of series to be extracted
	 * @return - input instances merged with newly extracted instances
	 * @throws Exception
	 */
	public Instances KAMA(Instances inst, int index) throws Exception   {
		double[] series = inst.attributeToDoubleArray(index);
		int timePointCnt = series.length;
		double[] kamas = new double[timePointCnt];
        Core TA = new Core();
        MInteger outBegIdx = new MInteger();
        MInteger outNBElement = new MInteger();
        for (int optInTimePeriod = 2; optInTimePeriod < 26; optInTimePeriod++)   {
        	TA.kama(0, timePointCnt-1, series, optInTimePeriod, outBegIdx, outNBElement, kamas);
            for(int i = timePointCnt-1; i - outBegIdx.value > -1; i--)   {
            	kamas[i] = kamas[i - outBegIdx.value];
            }
            for(int i = outBegIdx.value - 1; i > -1; i--){
            	kamas[i] = 0;
            }            
            inst = Instances.mergeInstances(inst, doubleArrayToAttribute("kama" + optInTimePeriod, kamas));
	    }
	    return inst;
    }
	
	/**
	 * Extract slope of varied moving averages
	 * @param inst - {@code weka.core.Instances} containing series to be extracted 
	 * @param index - attribute index of MA series to be extracted
	 * @return - instances with date and newly extract MA slopes, original MAs deleted
	 * @throws Exception
	 */
	public Instances MAslope(Instances inst) throws Exception	{
		int inputNumAttributes = inst.numAttributes();
		int index = 0;
		for(int run = 0; run < inputNumAttributes; run++)	{
			if(inst.attribute(index).isDate())	{
				index++;
				continue;
			}
			double[] ma = inst.attributeToDoubleArray(index);
			int timePointCnt = ma.length;
			double[] maslope = new double[timePointCnt];
			int k = 1;
			for(int t = 0; t < timePointCnt; t++)	{
				try	{
					maslope[t] = (ma[t] - ma[t-k]) / k; 
				} catch (ArrayIndexOutOfBoundsException e)	{
					maslope[t] = 0;
				}
			}
			inst = Instances.mergeInstances(inst, doubleArrayToAttribute(inst.attribute(index).name() + "slope", maslope));
			inst.deleteAttributeAt(index);
		}
		return inst;
	}
	
	/**
	 * Extract percentage difference between series w.r.t. to its moving average.
	 * @param inst - {@code weka.core.Instances} containing series to be extracted 
	 * @param arg0 - attribute index of series to be extracted
	 * @return - instances with date and newly extracted MA deviations, original MAs deleted
	 * @throws Exception
	 */
	public Instances MAdev(Instances inst, int arg0) throws Exception	{
		int inputNumAttributes = inst.numAttributes();
		double[] series = inst.attributeToDoubleArray(arg0);
		int index = 0;
		for(int run = 0; run < inputNumAttributes; run++)	{
			if(inst.attribute(index).isDate())	{
				index++;
				continue;
			}
			if(index == arg0)	{
				index++;
				continue;
			}
			double[] ma = inst.attributeToDoubleArray(index);
			int timePointCnt = series.length;
			double[] madevs = new double[timePointCnt];
			for (int t = 0; t < timePointCnt; t++)	{
				if (ma[t] == 0)	{
					madevs[t] = 0;
					continue;
				}
				madevs[t] = (series[t] - ma[t]) / ma[t];
			}
			String colname = "dev" + inst.attribute(index).name();
			inst = Instances.mergeInstances(inst, doubleArrayToAttribute(colname, madevs));
			inst.deleteAttributeAt(index);
		}
		return inst;
		
	}
	
	/**
	 * Extract percentage return of varied lag. 
	 * @param inst - {@code weka.core.Instances} containing series to be extracted 
	 * @param index - attribute index of series to be extracted
	 * @return - input instances merged with newly extracted instances
	 * @throws Exception
	 */
	public Instances ROCP(Instances inst, int index) throws Exception	{
		double[] series = inst.attributeToDoubleArray(index);
		int timePointCnt = series.length;
		double[] rocp = new double[timePointCnt];
        Core TA = new Core();
        MInteger outBegIdx = new MInteger();
        MInteger outNBElement = new MInteger();
        for (int lag = 1; lag < 21; lag++)	{
        	TA.rocP(0, timePointCnt-1, series, lag, outBegIdx, outNBElement, rocp);
        	for(int i = timePointCnt-1; i - outBegIdx.value > -1; i--)   {
                rocp[i] = rocp[i - outBegIdx.value];
            }
            for(int i = outBegIdx.value - 1; i > -1; i--){
                rocp[i] = 0;
            }
            inst = Instances.mergeInstances(inst, doubleArrayToAttribute("rocp" + lag, rocp));
        }
        return inst;
	}
	
	/**
	 * Extract RSI of varied smoothing period. 
	 * @param inst - {@code weka.core.Instances} containing series to be extracted 
	 * @param index - attribute index of series to be extracted
	 * @return - input instances merged with newly extracted instances
	 * @throws Exception
	 */
	public Instances RSI(Instances inst, int index) throws Exception	{
		double[] series = inst.attributeToDoubleArray(index);
		int timePointCnt = series.length;
		double[] rsi = new double[timePointCnt];
        Core TA = new Core();   
        MInteger outBegIdx = new MInteger();
        MInteger outNBElement = new MInteger();
        for (int optInTimePeriod = 3; optInTimePeriod < 15; optInTimePeriod++)   {
            TA.rsi(0, timePointCnt-1, series, optInTimePeriod, outBegIdx, outNBElement, rsi);
            for(int i = timePointCnt-1; i - outBegIdx.value > -1; i--)   {
                rsi[i] = rsi[i - outBegIdx.value];
            }
            for(int i = outBegIdx.value - 1; i > -1; i--){
                rsi[i] = 0;
            }
            inst = Instances.mergeInstances(inst, doubleArrayToAttribute("rsi" + optInTimePeriod, rsi));
        }
        return inst;
	}
}
