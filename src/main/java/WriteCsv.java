import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import com.opencsv.*;

public class WriteCsv
{
   public static void main(String [] args)
   {
	   String fName = "out/java_test.csv";
	   ArrayList<String> line0 = new ArrayList();
	   line0.add("line0item0");
	   line0.add("line0item1");
	   
	   ArrayList<String> line1 = new ArrayList();
	   line1.add("line1item0");
	   line1.add("line1item1");
	   
	   ArrayList<ArrayList> sampleData = new ArrayList();
	   sampleData.add(line0);
	   sampleData.add(line1);
	   
	   //writeCsv(fName, sampleData); 
   }
   
   public void writeCsv(String fName, String[][] data, char delimiter)
   {
	   for (String[] line : data) {
		   //writer.writeAll(line);
	   }
//	try
//	{
//	    FileWriter writer = new FileWriter(sFileName, false);
//	    
//	    int counter = 0;
//	    for (String[] line : data) {
//	    	
//	    	for (String item : line) {
//		 
//			    writer.append(item);
//			    writer.append(delimiter);
//	    	}
//			
//	    	writer.append('\n');
//	    	//System.out.println("Line " + counter + "/" + data.length + "printed");
//	    }
//	    
//	    writer.flush();
//	    writer.close();
//	}
//	catch(IOException e)
//	{
//	     e.printStackTrace();
//	} 
    }
}