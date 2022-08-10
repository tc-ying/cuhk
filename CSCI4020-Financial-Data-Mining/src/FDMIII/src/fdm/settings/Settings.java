package fdm.settings;

import java.io.*;
import java.util.Properties;

/**
 * @description	Wrapper class storing the .properties file. Load and save settings.  
 * @date		
 * @author		Adapted by Ying Ting Chung, from Sfolcini A., http://sfljtse.sourceforge.net/
 */
public class Settings {
	
    /** holds the properties */
	private static Properties defaultProps;
 	private static String filePath = "\\..\\..\\FYP\\FDMIII\\my.properties";
    
 	/** loads the properties at start */
	static {
		load(filePath);
	}
	
	/**
	 * Read .properties file, initialise variables to default
	 * @param filePath - full path of .properties file
	 */
	public static void load(String filePath) {
		BufferedReader br = null;
		defaultProps = new Properties();
		try {
			br = new BufferedReader(new FileReader(filePath));
			defaultProps.load(br);
		} catch (FileNotFoundException e) {
			System.out.println("*.Properties file not found.");
		} catch (IOException e)	{
			e.printStackTrace();
		} finally	{
			try {
				br.close();
			} catch (IOException e) {}
		}
	}
	
	/** 
	 * Save .properties file
	 * @param pathName - full path of .properties file
	 */
	public static void save(String pathName) {
        try {
        	BufferedOutputStream buffer = new BufferedOutputStream(new FileOutputStream(new File(pathName)));
        	defaultProps.store(buffer, null);
        } catch (Exception e) {
        	System.out.println("Error while saving properties");
        }
	}
	
	/** 
	 * get a property
	 * @param name - name of the key
	 * @return - value
	 */
	public static String getProperty(String name) {
		return defaultProps.getProperty(name);
	}	

	/** 
	 * set a property
	 * @param name - name of the key
	 * @param value - the new value
	 */
	public static void setProperty(String name, String value) {
		defaultProps.setProperty(name, value);
	}
}
