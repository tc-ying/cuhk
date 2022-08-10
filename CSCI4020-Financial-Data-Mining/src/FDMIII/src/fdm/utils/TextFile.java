package fdm.utils;

import java.io.*;

/**
 * @description	Represent a text file. Contain text file editing functions.
 * @date		
 * @author		Ying Ting Chung
 */
public class TextFile extends File {
	private BufferedReader br = null;
	private BufferedWriter bw = null;
	
	/**
	 * Creates a new File instance by converting the given pathname string into an abstract pathname.
	 *  
	 * @param pathname - A pathname string 
	 * @throws NullPointerException - If the pathname argument is null
	 */
	public TextFile(String pathname)	{
		super(pathname);
	}
	
	/**
	 * Creates a new File instance from a parent pathname string and a child pathname string.
	 * 
	 * @param parent - The parent pathname string
	 * @param child - The child pathname string 
	 * @throws NullPointerException - If child is null
	 */
	public TextFile(String parent, String child)	{
		super(parent, child);
	}
	
	// public static void IntArrToCSV(int[] arr, String columnLabel, String dirname, String filename)	{}
	// public static void StringArrToCSV(String[] arr, String columnLabel, String dirname, String filename)	{}
	
	/**
	 * Return the first line of text file. First line removed, file overwritten.
	 * @return {@code String} - first line of text file
	 */
	public String removeHeader() throws IOException	{
		File original = getAbsoluteFile();
		File tmp = File.createTempFile("removing_", ".tmp");
		br = new BufferedReader(new FileReader(getPath()));
		bw = new BufferedWriter(new FileWriter(tmp));
		
		String inputLine;	
		String header = null;
		if((inputLine = br.readLine()) != null)
			header = inputLine;
			
		while((inputLine = br.readLine()) != null)	{
			bw.write(inputLine);
			bw.newLine();
		}
		bw.flush();
		bw.close();
		br.close();
		original.delete();
		tmp.renameTo(new File(getPath()));
		
		return header;
	}
	
	/**
	 * Insert text to the beginning of text file. File overwritten.
	 * @param header - text 
	 */
    public void addHeader(String header) throws IOException {
        File original = getAbsoluteFile();
        File tmp = File.createTempFile("adding_", ".tmp");
        br = new BufferedReader(new FileReader(getPath()));
        bw = new BufferedWriter(new FileWriter(tmp));
        
        bw.write(header);
        bw.newLine();
        String inputLine;
        while((inputLine = br.readLine()) != null)	{
        	bw.write(inputLine);
        	bw.newLine();
        }
        bw.flush();
        bw.close();
        br.close();
        original.delete();
        tmp.renameTo(new File(getPath()));
    }
    
    /*
	public String removeHeaderArff() throws IOException	{
		br = new BufferedReader(new FileReader(dirName + fileName));
		
		File original = new File(dirName + fileName);
		File edit = File.createTempFile("removing_", ".tmp");
		
		bw = new BufferedWriter(new FileWriter(edit));
		
		String inputLine;
		
		String header = null;
		
		if((inputLine = br.readLine()) != null)
			header = inputLine;
			
		while((inputLine = br.readLine()) != null)
			bw.write(inputLine + "\n");
		
		bw.flush();
		bw.close();
		br.close();
		
		fileName = fileName.replace(".csv", ".arff");
		edit.renameTo(new File(dirName + fileName));
		
		return fileName;
	}
	
	public void csv2arff (String dirname) throws IOException	{
		File directory = new File(dirname);
		File files[] = directory.listFiles();
        String newHeader = "@relation hsi_test\n"
            + "@attribute Date date \"yyyy-mm-dd\"\n"
            + "@attribute O real\n"
            + "@attribute H real\n"
            + "@attribute L real\n"
            + "@attribute C real\n"
            + "@attribute Vol integer\n"
            + "@attribute Adj_C real\n"
            + "@data\n";
        
		for (File f : files) {
	        System.out.print(f.getName());
	        String name = removeHeaderArff(dirname,f.getName());
	        System.out.println(name);
	        addHeader(dirname, name ,newHeader);
		}	
	}
	*/
}
