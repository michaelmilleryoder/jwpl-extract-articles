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
import de.tudarmstadt.ukp.wikipedia.api.exception.WikiApiException;

public class ArTalkNames {

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
		
		Iterable<Page> pageNames = wiki.getPages();
//		String outCsvName = "ar/artalk_pagenames";
		String outCsvName = "ar/artalk_pages";
//		String outCsvName = "artalk_pages_test";
		
		int pageCounter = 0;
			
		CSVWriter writer = new CSVWriter(new FileWriter(outCsvName + ".csv", true), ',');
		String[] header = {"article_name", "discussion_text"};
		writer.writeNext(header);
		for (Page page : pageNames) {

			pageCounter++;
//			if (pageCounter > 100) {
//				break;
//			}

			// Try to get discussion page
			try {
				Page talkPage = wiki.getDiscussionPage(page); // might be printing

				String talkText = talkPage.getText();
				if (talkText != null) {

					// Optionally add in text
					//parse wiki markup
					MediaWikiParserFactory pf = new MediaWikiParserFactory();
					pf.setTemplateParserClass( FlushTemplates.class ); //optionally remove all templates from  markup
					MediaWikiParser parser = pf.createParser();
					ParsedPage pp = parser.parse(talkText);
					if (pp != null) {
						String parsedText = pp.getText();

						// Add info to the array
						System.out.println(page.getTitle().getPlainTitle());
						String[] line = {page.getTitle().getPlainTitle(), parsedText};
						writer.writeNext(line);
						//						lines.add(line); // might be printing sthg to stdout
					}

					// Write to CSV
					//				writer.writeAll(lines);
					//				writer.close();
					//				lines = new ArrayList<String[]>();

				}
			}
			catch (WikiApiException e) {
			}

		}
		
		// Write to the csv
//		CSVWriter writer = new CSVWriter(new FileWriter(outCsvName + pageCounter + ".csv"), ',');
//		writer.writeAll(lines);
		writer.close();
		System.out.println("Wrote csv at " + outCsvName);
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