package fdm.ods;
import java.io.*;
import java.net.*;
import java.util.Iterator;
import java.util.List;

import au.com.bytecode.opencsv.CSVReader;

/**
 * @description	Customised batch data download  
 * @date		
 * @author		Ying Ting Chung
 */
public class Crawler {	
	/*
	 * 1. path = dirName + fileName
	 * 2. fileName always include file extension
	 */

	/**
	 * Download single file from URL. Overwrite it to the named file in given directory.
	 * 
	 * @param urlName - exact URL where file is located
	 * @param dirName - directory to save into
	 * @param fileName - distinct file name
	 */
	public void download(String urlName, String dirName, String fileName)	{
		URL url;
		try {
			url = new URL(urlName);
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
			return;
		}
		BufferedReader br = null;
		BufferedWriter bw = null;
		String msg = null;
		try {
			URLConnection conn = url.openConnection();
			if (conn instanceof HttpURLConnection)	{
				HttpURLConnection httpConn = (HttpURLConnection) conn; 
				msg = httpConn.getResponseMessage();
			}
			br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			bw = new BufferedWriter(new FileWriter(dirName + fileName));
			String inputLine;
			while((inputLine = br.readLine()) != null)	{
				bw.write(inputLine);
				bw.newLine();
			}	
		} catch (IOException e1) {
			System.out.println("Downloading failed for " + fileName + ", HTTP response: " + msg);
			// e1.printStackTrace();
		} finally	{
			try {
				if (bw != null) bw.close();
			} catch (IOException e) {}
			try {
				if (br != null) br.close();
			} catch (IOException e) {}
		}
	}
	
	/*
	* Price data files named <0000_HK.csv> for convenient MySQL import
	*/
	/**
	* Iterate over Yahoo! Finance URL template.
	* Overwrite respectively named file in given directory.
	*
	* @param dirName - files directory to save into
	*/
	public void YahooBatchDownload(String dirName)	{
		StringBuilder s = new StringBuilder();
		String url;
		String fileName;
		for (int i = 1; i < 40; i++)	{
			s.delete(0, s.length());
			s.append(i);
			while (s.length() < 4)
				s.insert(0, "0");
			s.append(".HK");
					
			url = "http://ichart.yahoo.com/table.csv?s=" +
			s.toString() + "&d=11&e=23&f=2010&g=d&a=0&b=1&c=2003&ignore=.csv";
			
			fileName = s.toString().replace('.', '_').replace('^', '_').concat(".csv");
			this.download(url, dirName, fileName);
		}		
	}
	
	/*
	* To filter ETFs, find path to Listed ETFs.csv
	*/
	/**
	* Iterate over Yahoo! Finance URL template. Discard symbols in the stopList file.
	* Overwrite respectively named file in given directory.
	*
	* @param dirName - files directory to save into
	* @param stopList - path of file specifying symbols to discard
	* @return
	*/
	public void YahooBatchDownload(String dirName, String stopList)	{
		List<String[]> myEntries = null;
		try {
			CSVReader reader = new CSVReader(new FileReader(stopList));
			myEntries = reader.readAll();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		
		boolean stop;
		String[] nextLine;
		// String s;	// symbol
		StringBuilder s = new StringBuilder();
		String url;
		String fileName;
		
		for (int i = 1; i < 4000; i++)	{
			stop = false;
			Iterator<String[]> it = myEntries.iterator();
			while(it.hasNext())	{
				nextLine = (String[]) it.next();
				if (nextLine[0].equals(Integer.toString(i)))	{
					stop = true;
					break;
				}
			}
			if (stop) continue;	// skip stop items
			// if (!stop) continue;	// download stop items
			s.delete(0, s.length());
			s.append(i);
			while (s.length() < 4)
				s.insert(0, "0");
			s.append(".HK");
					
			url = "http://ichart.yahoo.com/table.csv?s=" +
			s.toString() + "&d=11&e=23&f=2010&g=d&a=0&b=1&c=2003&ignore=.csv";
			
			fileName = s.toString().replace('.', '_').replace('^', '_').concat(".csv");
			this.download(url, dirName, fileName);
		}
	}
}
