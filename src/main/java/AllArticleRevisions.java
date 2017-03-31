import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

public class AllArticleRevisions {
	
	public static List<String> read_file(String fpath) {
		/**
		 * Get database credentials
		 */
		
		Path p = Paths.get(fpath);
		
		List<String> lines = new ArrayList<String>();
		try {
			lines = Files.readAllLines(p);
		} catch (IOException e) {
			System.out.println("File read error");
		}
		
		return lines;
	}

	public static void main(String[] args) throws Exception{
		
		List<String> dbcred = read_file("db_cred.txt");
		
		//setup db config
		DatabaseConfiguration dbconf = new DatabaseConfiguration();
		dbconf.setHost("erebor.lti.cs.cmu.edu");
		dbconf.setDatabase("enwiki_20140707_rev");
		dbconf.setUser(dbcred.get(0));
		dbconf.setPassword(dbcred.get(1));
		dbconf.setLanguage(Language.english);
		
		//connect to wiki db using JWPL API
		Wikipedia wiki = new Wikipedia(dbconf);
		RevisionApi revApi = new RevisionApi(dbconf);
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		String articleNameCsv = "talk_pagenames_100k.txt";
		String outDirName = "enwiki_revisions/";
		
//		String[] pageNames = getArticleNames(articleNameCsv);
		List<String> pageNames = read_file(articleNameCsv);
		//System.out.println("Number of article names " + pageNames.length);
		//String[] pageNames = {"Gaza Strip"};
		//String[] pageNames = {"Israeli-Palestinian Conflict"};
		//String[] pageNames = {"East Jerusalem", "Gaza Strip", "State of Palestine", "Temple Mount", "Israeli-Palestinian Conflict", "1948 Palestine War"};
		WriteCsv csvWriter = new WriteCsv();
		
		for (String pageName : pageNames) {
			String outCsvName = outDirName + pageName.replaceAll(" ", "_").replaceAll("/", "_").toLowerCase() + ".tsv";
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
//						System.out.println("\tSample " + i + '/' + numRevisions + " added");
				
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
}