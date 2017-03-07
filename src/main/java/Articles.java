import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.FileReader;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.text.SimpleDateFormat;
import com.opencsv.*;

import de.tudarmstadt.ukp.wikipedia.api.DatabaseConfiguration;
import de.tudarmstadt.ukp.wikipedia.api.Page;
import de.tudarmstadt.ukp.wikipedia.api.WikiConstants.Language;
import de.tudarmstadt.ukp.wikipedia.api.Wikipedia;
import de.tudarmstadt.ukp.wikipedia.parser.ParsedPage;
import de.tudarmstadt.ukp.wikipedia.parser.mediawiki.FlushTemplates;
import de.tudarmstadt.ukp.wikipedia.parser.mediawiki.MediaWikiParser;
import de.tudarmstadt.ukp.wikipedia.parser.mediawiki.MediaWikiParserFactory;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.api.RevisionApi;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.api.Revision;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.api.RevisionIterator;

public class Articles {

	public static void main(String[] args) throws Exception{
		//setup db config
		DatabaseConfiguration dbconf = new DatabaseConfiguration();
		dbconf.setHost("erebor.lti.cs.cmu.edu");
		dbconf.setDatabase("enwiki_20140707_rev");
		// TODO: set username and password same method as TalkNames.java
		dbconf.setLanguage(Language.english);
		
		//connect to wiki db using JWPL API
		Wikipedia wiki = new Wikipedia(dbconf);
		RevisionApi revApi = new RevisionApi(dbconf);
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		// Get list of ideal dates
		int nMonths = 1;
		Timestamp startDate = new Timestamp(sdf.parse("2004-04-08 00:00:00").getTime());
		System.out.println("Start date " + startDate);
		Timestamp endDate = new Timestamp(sdf.parse("2014-07-07 00:00:00").getTime());
		// Every 2 weeks
		ArrayList<Timestamp> idealTsList = getDates(Calendar.DAY_OF_MONTH, 14, startDate, endDate);

		// Print ideal date list
		//System.out.println("Ideal date list:");
		//for (Timestamp idealTs : idealTsList) {
		//	System.out.println(sdf.format(idealTs));
		//}
		System.out.println("Number of ideal dates " + idealTsList.size());
		//System.out.println("First ideal date " + idealTsList.get(0));
		//System.out.println("Last ideal date " + idealTsList.get(idealTsList.size()-1));
					
		String ipcArticleNameCsv = "ipc_articles.tsv";
		
		//String[] pageNames = {"East Jerusalem"};
		String[] pageNames = getArticleNames(ipcArticleNameCsv);
		System.out.println("Number of article names " + pageNames.length);
		//String[] pageNames = {"Gaza Strip"};
		//String[] pageNames = {"Israeli-Palestinian Conflict"};
		//String[] pageNames = {"East Jerusalem", "Gaza Strip", "State of Palestine", "Temple Mount", "Israeli-Palestinian Conflict", "1948 Palestine War"};
		WriteCsv csvWriter = new WriteCsv();
		
		int numRevisions = idealTsList.size();
		int numCols = 3;
		//String[][] lines = new String[numRevisions][numCols];
			
		for (String pageName : pageNames) {
			String outCsvName = "out/ipc_sweep/" + pageName.replaceAll(" ", "_").replaceAll("/", "_").toLowerCase() + ".tsv";
			File pageFile = new File(outCsvName);
			
			// If isn't already there
			if (!pageFile.exists()) {
				List<String[]> lines = new ArrayList<String[]>();
				Page p = wiki.getPage(pageName);
				int articleId = p.getPageId();
		
				//Access revision using the JWPL RevisionApi
				List<Timestamp> tsList = revApi.getRevisionTimestamps(articleId); // is a LinkedList
			
				// Select revision timestamps
				List<Timestamp> actualTimestamps = new LinkedList<Timestamp>();

				// set actual Timestamps to all revisions (opt)
				//List<Timestamp> actualTimestamps = tsList;
			
				// Keep marker of oldest date to consider
				int idealPtr = 0;
				int revPtr = 0;
				Timestamp lastRevTs = tsList.get(revPtr);
				Timestamp targetIdeal = idealTsList.get(idealPtr);
				//if (lastRevTs.compareTo(targetIdeal) > 0) {
				//}

				// Loop through idealDateList starting at oldest and compare to revision timestamps
				while (idealPtr < idealTsList.size()) {
					Timestamp revTs;
					targetIdeal = idealTsList.get(idealPtr);
					if (revPtr < 0)
						System.out.println(revPtr);
					if (revPtr+1 < tsList.size()) {
						revTs = tsList.get(revPtr + 1);
					}
					else {
						revTs = lastRevTs;
						// Run out of revisions before ideal list ends
						if (revTs.compareTo(idealTsList.get(idealTsList.size()-1)) < 0)
							for (int i=idealPtr; i<idealTsList.size(); i++) {
								actualTimestamps.add(lastRevTs);
							}
						break;
					}
					
					if (revTs.compareTo(targetIdeal) < 0) { // revision is before the ideal
						revPtr++;
						lastRevTs = revTs;
					}
					
					else { // revision is after the ideal
						actualTimestamps.add(lastRevTs);
						idealPtr++;
					}
				}	
			
				// Print actual timestamps
				//System.out.println("Actual date list:");
				//for (Date actualDate : actualTimestamps) {
				//	System.out.println(sdf.format(actualDate));
				//}
				//System.out.println("Number of actual dates " + actualTimestamps.size());
				//System.out.println("First actual date " + actualTimestamps.get(0));
				//System.out.println("Last actual date " + actualTimestamps.get(actualTimestamps.size()-1));
			
				// Get and save revisions
				for (int i = 0; i < actualTimestamps.size(); i++) {
					Timestamp ts = actualTimestamps.get(i);
					//Timestamp stamp = tsList.get(tsList.size()-1);
					//Timestamp ts = (Timestamp) stamp;
					Timestamp firstPossibleTs = tsList.get(0);
					if (ts.compareTo(firstPossibleTs) > 0) {
						Revision rev = revApi.getRevision(articleId, ts);
						String revText = rev.getRevisionText();
						String revisionEditor = rev.getContributorName();
						String revisionComment = rev.getComment();
		
						//parse wiki markup
						MediaWikiParserFactory pf = new MediaWikiParserFactory();
						pf.setTemplateParserClass( FlushTemplates.class ); //optionally remove all templates from  markup
						MediaWikiParser parser = pf.createParser();
						ParsedPage pp = parser.parse(revText);
						String parsedText = pp.getText().replaceAll("\n", "").replaceAll("\t", "");
					
						Timestamp sampledTs = idealTsList.get(i);
						String date = sdf.format(sampledTs);
					
						// Add info to the array
						//ArrayList<String> line = new ArrayList();
						String[] line = {pageName, date, parsedText, revisionEditor, revisionComment};
						//String[] line = {date, revisionEditor, revisionComment};
				//		line.add(pageName);
				//		line.add(date);
				//		line.add(parsedText);	
						lines.add(line);
						//System.out.println(line);
						System.out.println("\tSample " + i + '/' + numRevisions + " added");
			
						// Write text to a file
//						String outFileName = "out/" + pageName.replaceAll(" ", "_").toLowerCase() + "_" + date + ".txt";
//						PrintWriter printWriter = new PrintWriter(outFileName);
//						printWriter.print(pp.getText());
//						printWriter.close();
//						System.out.println(outFileName + " written");
					}
		
				}
			
				// Write to the csv
				//String csvFileName = "out/east_jerusalem_all_revisions.tsv";
				//String csvFileName = "out/east_jerusalem_usernames.tsv";
				//String csvFileName = "out/gaza_strip_all_revisions.tsv";
				//String csvFileName = "out/gaza_strip_usernames.tsv";
				//String csvFileName = "out/israeli-palestinian_conflict_all_revisions.tsv";
				//String csvFileName = "out/east_jerusalem_2wk.tsv";
				//csvWriter.writeCsv(csvFileName, lines, '\t');
				CSVWriter writer = new CSVWriter(new FileWriter(outCsvName), '\t');
				writer.writeAll(lines);
				writer.close();
				System.out.println("Wrote csv at " + outCsvName);
			}
		}
	}
	
	public static ArrayList<Timestamp> getDates(int calendarUnit, int nUnits, Timestamp startDate, Timestamp endDate) throws Exception {
		/* 
		 * calendarUnit can be:
		 * 	Calendar.MONTH
		 * 	Calendar.DAY
		 */
		
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		
		Calendar c = Calendar.getInstance();
		c.setTime(startDate);
	
		ArrayList<Timestamp> dateList = new ArrayList();
		
		for (int i=0; c.getTime().compareTo(endDate) <= 0; i++) {
			dateList.add(new Timestamp(c.getTime().getTime()));
			c.add(calendarUnit, nUnits);
		}
		
		return dateList;
	}
	
	private static String[] getArticleNames(String infile) throws IOException {
		/** Expects separate name on separate line in infile **/
		String[] articles = {};
		try {
			CSVReader reader = new CSVReader(new FileReader(infile), '\t');
			List<String[]> lines = reader.readAll();
			int numLines = lines.size();
			articles = new String[numLines];
			for (int lineCount = 0; lineCount < numLines; lineCount++) {
				String[] line = lines.get(lineCount);
				if (line.length == 1)
		    		 articles[lineCount] = line[0];
		    	 else {
		    		 System.out.println("Error: more than one article name found on a line");
		    	 }
			reader.close();
		     }
	
		} catch (FileNotFoundException e) {
			System.out.println("Input CSV for article names not found.");
		}
		
		return articles;
		
	}
}