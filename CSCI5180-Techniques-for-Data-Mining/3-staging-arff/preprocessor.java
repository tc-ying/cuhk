
import java.io.*;

public class preprocessor {
	
	/* Removing all lines in the file specified by path "inFile",
	 * which matches the specified "line",
	 * and output a new pre-processed file to path "outFile"
	*/
	public void removeLines(String inFile, String outFile, String line) throws IOException {

		BufferedReader br = new BufferedReader(new FileReader(inFile));
		BufferedWriter bw = new BufferedWriter(new FileWriter(outFile));
		
		String inputLine;
		
		while((inputLine = br.readLine()) != null)	{
			if(!inputLine.trim().equalsIgnoreCase(line))	{
				bw.write(inputLine);
				bw.newLine();
			}
		}
		
		br.close();
		bw.close();
	}
}