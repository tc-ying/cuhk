package fdm.etl;

import javax.xml.crypto.Data;

import weka.core.Attribute;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.*;

/**
 * @description	Helper class to apply WEKA built-in filters  
 * @date		
 * @author		Ying Ting Chung
 */
public class WekaFilter {
	
	/**
	 * 
	 * @param IDIndex - index of the attribute used
	 * @param AttributeName - new attribute's name
	 * @param data - instances for filtering
	 * @return
	 * @throws Exception
	 */
	public Instances fAddID(String IDIndex, String AttributeName, Instances data) throws Exception	{
		AddID addid = new AddID();
		addid.setIDIndex(IDIndex);
		addid.setAttributeName(AttributeName);
		addid.setInputFormat(data);
		return Filter.useFilter(data, addid);
	}
	
	/**
	 * 
	 * @param expr - expression to apply
	 * @param instanceInfo - format of the input instances
	 * @return
	 * @throws Exception
	 */
	public Instances fAddExpression(String expr, Instances instanceInfo) throws Exception	{
		AddExpression addexp = new AddExpression();
		addexp.setExpression(expr);
		addexp.setInputFormat(instanceInfo);
		return Filter.useFilter(instanceInfo, addexp);
	}
	
	/**
	 * 
	 * @param index - which attributes are to be "nominalised" (only numeric attributes among the selection will be transformed)
	 * @param data - instances for filtering
	 * @return
	 * @throws Exception
	 */
	public Instances fNumericToNominal(String index, Instances data) throws Exception	{
		NumericToNominal nom = new NumericToNominal();
		nom.setAttributeIndices(index);
		nom.setInputFormat(data);
		return Filter.useFilter(data, nom);
	}
	
	/**
	 * An instance filter that assumes instances form time-series data and replaces attribute values in the current instance with the equivalent attribute values of some previous (or future) instance.
	 * For instances where the desired value is unknown either the instance may be dropped, or missing values used.
	 * Skips the class attribute if it is set.
	 * @param rangeList - list of columns to translate in time
	 * @param data - input instances
	 * @param newInstanceRange - number of instances forward to translate values between.
	 * 			A negative number indicates taking values from a past instance.
	 * @return
	 * @throws Exception
	 */
	public Instances fTimeSeriesTranslate(Instances data, String rangeList, int newInstanceRange) throws Exception	{
		TimeSeriesTranslate lag = new TimeSeriesTranslate();
		lag.setAttributeIndices(rangeList);
		lag.setInstanceRange(newInstanceRange);
		lag.setInputFormat(data);
		return Filter.useFilter(data, lag);
	}
}
