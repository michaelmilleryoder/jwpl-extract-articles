

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

public class ArRevisions {

	public static void main(String[] args) throws Exception{
		//setup db config
		DatabaseConfiguration dbconf = new DatabaseConfiguration();
		dbconf.setHost("erebor.lti.cs.cmu.edu");
		dbconf.setDatabase("arwiki_20161001_rev");
		// TODO: set username and password same method as TalkNames.java
		dbconf.setLanguage(Language.arabic);
		
		//connect to wiki db using JWPL API
		Wikipedia wiki = new Wikipedia(dbconf);
		RevisionApi revApi = new RevisionApi(dbconf);
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

//		String articleNameCsv = "/home/michael/school/research/wp/ar/cs_pagenames.txt";
		String articleNameCsv = "cs_pagenames.txt";
		
		String[] pageNames = getArticleNames(articleNameCsv);
		//System.out.println("Number of article names " + pageNames.length);
//		String[] pageNames = {"عقوبة الإعدام"};
		//String[] pageNames = {"Israeli-Palestinian Conflict"};
		//String[] pageNames = {"East Jerusalem", "Gaza Strip", "State of Palestine", "Temple Mount", "Israeli-Palestinian Conflict", "1948 Palestine War"};
		WriteCsv csvWriter = new WriteCsv();
		
		int numCols = 3;
			
		for (String pageName : pageNames) {
			String outCsvName = "ar_articles/" + pageName.replaceAll(" ", "_").replaceAll("/", "_").toLowerCase() + ".tsv";
			File pageFile = new File(outCsvName);
			
			// If isn't already there
			if (!pageFile.exists()) {
				List<String[]> lines = new ArrayList<String[]>();
				try {
					Page p = wiki.getPage(pageName);
					int articleId = p.getPageId();
			
					//Access revision using the JWPL RevisionApi
					List<Timestamp> tsList = revApi.getRevisionTimestamps(articleId); // is a LinkedList
				
					// set actual Timestamps to all revisions (opt)
					List<Timestamp> actualTimestamps = tsList;
				
					int numRevisions = actualTimestamps.size();
					System.out.println(numRevisions);
				
					// Get and save revisions
					for (int i = 0; i < actualTimestamps.size(); i++) {
						Timestamp ts = actualTimestamps.get(i);
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
						
						String date = sdf.format(ts);
						
						// Add info to the array
						String[] line = {pageName, date, parsedText, revisionEditor, revisionComment};
						//String[] line = {date, revisionEditor, revisionComment};
						lines.add(line);
						//System.out.println(line);
						System.out.println("\tRevision " + i + '/' + numRevisions + " added");
				
					}
			
					// Write to the csv
					CSVWriter writer = new CSVWriter(new FileWriter(outCsvName), '\t');
					writer.writeAll(lines);
					writer.close();
					System.out.println("Wrote csv at " + outCsvName);
				}
				catch (Exception e) {
				}
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