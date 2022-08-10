package fdm.ods;

import java.io.*;
import java.sql.*;

import weka.core.Instances;
import weka.core.converters.DatabaseSaver;
import weka.experiment.DatabaseUtils;
import weka.experiment.InstanceQuery;

import fdm.settings.Settings;
import fdm.utils.TextFile;

/**
 * @description	Agent that manage DBMS operations 
 * @date		
 * @author		Ying Ting Chung
 */
public class DB extends DatabaseUtils {
	
	private ResultSet rs = null;
	
	/**
	 * Reads my.properties and sets up the database drivers.
	 * 
	 * @throws Exception
	 */
	public DB() throws Exception	{
		setDatabaseURL(Settings.getProperty("DB.driver"));
		setDatabaseURL(Settings.getProperty("DB.url"));
		setUsername(Settings.getProperty("DB.username"));
		setPassword(Settings.getProperty("DB.password"));
		connectToDatabase();
	}
	
	/**
	 * Sets up the database drivers.
	 * 
	 * @param url - URL of database being accessed 
	 * @param user - database user name
	 * @param password - database password
	 * @throws Exception 
	 */
	public DB(String driver, String url, String username, String password) throws Exception	{
		setDatabaseURL(driver);
		setDatabaseURL(url);
		setUsername(username);
		setPassword(password);
		connectToDatabase();
	}
	
	/**
	 * return a line of comma-separated column labels of the result set
	 * 
	 * @param rs - result set
	 * @return {@code String} - single line comma-separated column labels
	 */
	private String getResultSetHeader(ResultSet rs) throws SQLException	{
		StringBuilder header = new StringBuilder();
		ResultSetMetaData metadata = rs.getMetaData();
		int colCnt = metadata.getColumnCount();
		int i;
		for (i = 0; i < colCnt - 1; i++) {
			header.append(metadata.getColumnLabel(i + 1)).append(",");
		}
		header.append(metadata.getColumnLabel(i + 1)).append("\n");
		return header.toString();
	}
	
	/**
	 * Gets the value of all instances in this table for a particular column.
	 * 
	 * @param tableName - table name
	 * @param columnLabel - column label
	 * @return {@code double[]}
	 */
	public double[] columnToDoubleArray(String tableName, String columnLabel)	{
		StringBuilder query = new StringBuilder("SELECT ");
		query.append(columnLabel).append(" FROM ").append(tableName).append(" ORDER BY Date");
		double[] result = null;
		try {
			rs = select(query.toString());
			rs.last();
			result = new double[rs.getRow()];
			rs.beforeFirst();
			for (int i = 0; rs.next(); i++)	{
				result[i] = rs.getDouble(columnLabel);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally	{
			close(rs);
		}
		return result;
	}
	
	/**
	 * Gets the value of all instances in this table for a particular column.
	 * 
	 * @param tableName - table name
	 * @param columnLabel - column label
	 * @return {@code String[]}
	 */
	public String[] columnToStringArray(String tableName, String columnLabel)	{
		StringBuilder query = new StringBuilder("SELECT ");
		query.append(columnLabel).append(" FROM ").append(tableName).append(" ORDER BY Date");
		String[] result = null;
		try {
			rs = select(query.toString());
			rs.last();
			result = new String[rs.getRow()];
			rs.beforeFirst();
			for (int i = 0; rs.next(); i++)	{
				result[i] = rs.getString(columnLabel);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally	{
			close(rs);
		}
		return result;
	}
	
	/**
	 * Add a new column to an existing table.
	 * 
	 * @param newCol - new column
	 * @param tableName - table name
	 * @param columnLabel - column label
	 * @return
	 */
	public boolean doubleArrayToColumn(double[] newCol, String tableName, String columnLabel){
		String query = "ALTER TABLE ".concat(tableName).concat(" ADD ").concat(columnLabel).concat(" REAL");
		try {
			update(query);
			rs = select("SELECT Date FROM ".concat(tableName));
			rs.last();
			query = "UPDATE ".concat(tableName) + " SET ".concat(columnLabel) + " = ";
			String suffix = " WHERE ".concat(columnLabel).concat(" IS NULL ORDER BY Date LIMIT 1");
			StringBuilder appended_query = new StringBuilder();
			for (int i = 0; i < rs.getRow(); i++)	{
				update(appended_query.append(query).append(newCol[i]).append(suffix).toString());
				appended_query.delete(0, appended_query.length());
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}  finally	{
			close(rs);
		}
		return true;
	}
	
	/**
	 * Add a new column to an existing table.
	 * 
	 * @param newCol - new column
	 * @param tableName - table name
	 * @param columnLabel - column label
	 * @return
	 */
	public boolean intArrayToColumn(int[] newCol, String tableName, String columnLabel){
		String query = "ALTER TABLE ".concat(tableName).concat(" ADD ").concat(columnLabel).concat(" INT");
		try {
			update(query);
			rs = select("SELECT Date FROM ".concat(tableName));
			rs.last();
			query = "UPDATE ".concat(tableName) + " SET ".concat(columnLabel) + " = ";
			String suffix = " WHERE ".concat(columnLabel).concat(" IS NULL ORDER BY Date LIMIT 1");
			StringBuilder appended_query = new StringBuilder();
			for (int i = 0; i < rs.getRow(); i++)	{
				update(appended_query.append(query).append(newCol[i]).append(suffix).toString());
				appended_query.delete(0, appended_query.length());
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}  finally	{
			close(rs);
		}
		return true;
	}
	
	/**
	 * Create table in DBMS with given table name. Load ARFF flat file into the table.
	 * 
	 * @param tableName - DBMS table name
	 * @param dirName - ARFF file directory
	 * @param fileName - ARFF file name
	 */
	public void importARFF(String tableName, String dirName, String fileName)	{
		Instances data = null;
		try {
			data = new Instances(new BufferedReader(new FileReader(dirName + fileName)));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		data.setClassIndex(data.numAttributes() - 1);
		try {
			DatabaseSaver save = new DatabaseSaver();
			save.setUrl(getDatabaseURL());
			save.setUser(getUsername());
			save.setPassword(getPassword());
			save.setInstances(data);
			save.setRelationForTableName(false);
			save.setTableName(tableName);
			save.connectToDatabase();
			save.writeBatch();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Create table in DBMS with given table name. Load / replace WEKA instances into the table.
	 * 
	 * @param tableName - DBMS table name
	 * @param data - WEKA instances
	 */
	public void importInstances(String tableName, Instances data)	{
		if (data.classIndex() == -1)	{
			data.setClassIndex(data.numAttributes() - 1);
		}
		try {
			DatabaseSaver save = new DatabaseSaver();
			save.setUrl(getDatabaseURL());
			save.setUser(getUsername());
			save.setPassword(getPassword());
			save.setInstances(data);
			save.setRelationForTableName(false);
			save.setTableName(tableName);
			save.connectToDatabase();
			if(tableExists(tableName))	{
		    	update("DROP TABLE " + tableName + ";");
		    }
			save.writeBatch();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Create suitable table schema in MySQL. Load / replace CSV data into the table. 
	 * 
	 * @param schema - MySQL table schema, e.g. "Date DATE,O REAL,H REAL,L REAL,C REAL,Vol BIGINT,Adj_C REAL"
	 * @param dirName - CSV file directory
	 * @param fileName - CSV file name
	 * @throws Exception 
	 */
	public void importCSV(String schema, String dirName, String fileName) throws Exception	{		
		StringBuilder query = new StringBuilder();
		
		String[] tokens = schema.split(",");
		int colCnt = tokens.length;
		String[] columnNames = new String[colCnt];
		
		String header = "";
		String[] tmp;
		
		for (int i = 0; i < colCnt - 1; i++)	{
			tmp = tokens[i].split(" ", 2);
			columnNames[i] = tmp[0];
			header = header.concat(columnNames[i]).concat(",");
		}
		tmp = tokens[colCnt - 1].split(" ", 2);
		columnNames[colCnt - 1] = tmp[0];
		header = header.concat(columnNames[colCnt - 1]);
		
		try {
		    if(tableExists(fileName.replaceFirst(".csv", "")))	{
		    	query.append("DROP TABLE ").append(fileName.replaceFirst(".csv", "")).append(";");
		    	update(query.toString());
		    	query.delete(0, query.length());
		    }
			// query to create table
		    query.append("CREATE TABLE ").append(fileName.replaceFirst(".csv", "")).append(" (").append(schema).append(");");
		    update(query.toString());
		    query.delete(0, query.length());
			// http://www.tech-recipes.com/rx/2345/import_csv_file_directly_into_mysql
		    // MySQL Windows path format
		    TextFile csv = new TextFile(dirName + fileName);
		    // Re-use string builder to import CSV 
		    query.append("LOAD DATA INFILE '").append(csv.getAbsolutePath().replaceAll("\\\\", "/")).append("' INTO TABLE ").append(fileName.replaceFirst(".csv", ""));
		    query.append(" FIELDS TERMINATED BY ',' ENCLOSED BY '\"' LINES TERMINATED BY '\\n' IGNORE 1 LINES (").append(header).append(");");
		    update(query.toString());
		} catch (SQLException ex){
		    // handle any errors
		    System.out.println("SQLException: " + ex.getMessage());
		    System.out.println("SQLState: " + ex.getSQLState());
		    System.out.println("VendorError: " + ex.getErrorCode());
		}
	}
	
	/**
	 * Define a proper schema for Yahoo! Finance CSV files. Assume file names contain no special symbol
	 * @param dirName - CSV file directory
	 * @param fileName - CSV file name
	 * @throws Exception 
	 */
	public void importYahooCSV(String dirName, String fileName) throws Exception{
		String schema = "Date DATE,O REAL,H REAL,L REAL,C REAL,Vol BIGINT,Adj_C REAL";
		importCSV(schema, dirName, fileName);
	}
	
	public void importDealCSV(String dirName, String fileName) throws Exception{
		String schema = "Date DATE,HKHalfDay INT,HKTurnover BIGINT,HKVol BIGINT,HKDeal INT";
		importCSV(schema, dirName, fileName);
	}	
		
	/**
	 * Execute query. Store the query result as WEKA instances.
	 * 
	 * @param query - query string
	 * @return weka.core.Instances
	 */
	public Instances exportResultSet(String query)	{
		InstanceQuery instanceQuery = null;
		Instances data = null;
		try {
			instanceQuery = new InstanceQuery();
			instanceQuery.setDatabaseURL(getDatabaseURL());
			instanceQuery.setUsername(getUsername());
			instanceQuery.setPassword(getPassword());
			instanceQuery.setQuery(query);
			data = instanceQuery.retrieveInstances();
		} catch (Exception e) {
			e.printStackTrace();
		} finally	{
			try {
				instanceQuery.disconnectFromDatabase();
			} catch (Exception e) {}
		}
		return data;
	}
	
	/**
	 * Execute query. Overwrite all column labels plus result set to given path. Assumes all referred tables in query exist.
	 *  
	 * @param query - MySQL query string
	 * @param dirName - CSV file directory
	 * @param fileName - file name including .csv extension
	 */
	public void exportResultSet(String query, String dirName, String fileName)	{
		dirName = dirName.replaceAll("\\\\", "//");
		StringBuilder appended_query = new StringBuilder(query.replaceAll(";", ""));
		appended_query.append(" INTO OUTFILE '").append(dirName).append(fileName).append("' FIELDS ESCAPED BY '\"' TERMINATED BY ',' LINES TERMINATED BY '\\r\\n';");
	
		try {
			execute(appended_query.toString());
			rs = select(query);
			dirName = dirName.replaceAll("//", "\\\\");
			fdm.utils.TextFile txtFile = new fdm.utils.TextFile(dirName, fileName); 
			txtFile.addHeader(getResultSetHeader(rs));
		} catch (SQLException e2) {
			e2.printStackTrace();
			return;
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Failed to add column labels");
		} finally {
            close(rs);
        }
	}
	
	public void exportWalkForwardSample(String query, String dirName, int windowSize, int horizonSize)   {
		int rowCount = windowSize + horizonSize, rsrowCount;
	    int skippedRow = 0;
	    StringBuilder appended_query = new StringBuilder();
	    StringBuilder fileName = new StringBuilder(); 
	    String tableName;
	    ResultSetMetaData metadata;
	    
        try {
            rs = select(query);
            metadata = rs.getMetaData();
            tableName = metadata.getTableName(1);
            rs.last();
            rsrowCount = rs.getRow();
            for(skippedRow = 0; skippedRow + rowCount <= rsrowCount; skippedRow++)    {
            	appended_query.append(query).append(" LIMIT ").append(skippedRow).append(",").append(rowCount);
                fileName.append(tableName).append(skippedRow).append(".csv");
                exportResultSet(appended_query.toString(), dirName, fileName.toString());
                fileName.delete(0, fileName.length());
                appended_query.delete(0, appended_query.length());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            close(rs);
        }
	}
}
