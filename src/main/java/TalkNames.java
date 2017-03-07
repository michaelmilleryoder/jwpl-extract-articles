import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.FileReader;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.text.SimpleDateFormat;
//import com.opencsv.*;

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

/***
 * 
 * @author Michael Miller Yoder
 * 
 * This script prints names of talk pages in a JWPL database to a text file
 *
 */

public class TalkNames {
	
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

		Iterable<Page> pageNames = wiki.getPages();
		String outCsvName = "talk_pagenames.txt";
		String blacklistPath = "blacklist.txt";

		int pageCounter = 0;

		// Outfile for names
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(outCsvName), true)); 
		PrintWriter writer = new PrintWriter(bw, true); // true auto-flushes

		// File for blacklisted problem pages
		BufferedWriter blacklistBw = new BufferedWriter(new FileWriter(new File(blacklistPath), true)); 
		PrintWriter blacklistWriter = new PrintWriter(blacklistBw, true); // true auto-flushes

		// Load names already obtained
		List<String> already = read_file(outCsvName);

		// Try to get discussion pages
		for (Page page : pageNames) {
		
		// test out a page
//		Page page = wiki.getPage("Loch_Ness_Monster");
//		System.out.println("TITLE: " + page.getTitle().getPlainTitle());

//			pageCounter++;
//			if (pageCounter > 5) {
//				break;
//			}
			
			String pageTitle = page.getTitle().getPlainTitle();
			
			// blacklist of pages that fail
			if (already.contains(page.getTitle().getPlainTitle()) ||
					page.getTitle().getPlainTitle().equals("Abdülaziz") ||
					page.getTitle().getPlainTitle().equals("Bipolar disorder") ||
					page.getTitle().getPlainTitle().equals("Osama bin Laden") ||
					page.getTitle().getPlainTitle().equals("Propaganda") ||
					page.getTitle().getPlainTitle().equals("Pokémon") ||
					page.getTitle().getPlainTitle().equals("Prime minister") ||
					page.getTitle().getPlainTitle().equals("PlayStation 3") ||
					page.getTitle().getPlainTitle().equals("Rugby union") ||
					page.getTitle().getPlainTitle().equals("Historical revisionism (negationism)") ||
					page.getTitle().getPlainTitle().equals("StarCraft") ||
					page.getTitle().getPlainTitle().equals("The Doors") ||
					page.getTitle().getPlainTitle().equals("United Nations") ||
					page.getTitle().getPlainTitle().equals("X-Men") ||
					page.getTitle().getPlainTitle().equals("Axis of evil") ||
					page.getTitle().getPlainTitle().equals("Hugo Chávez") ||
					page.getTitle().getPlainTitle().equals("Wild boar") ||
					page.getTitle().getPlainTitle().equals("Sea lion") ||
					page.getTitle().getPlainTitle().equals("Noatun") ||
					page.getTitle().getPlainTitle().equals("Tāwhirimātea") ||
					page.getTitle().getPlainTitle().equals("Mārikoriko") ||
					page.getTitle().getPlainTitle().equals("Taranga (Māori mythology)") ||
					page.getTitle().getPlainTitle().equals("Ngā Mānawa") ||
					page.getTitle().getPlainTitle().equals("Yo Sé Que Mentía") ||
					page.getTitle().getPlainTitle().equals("Elizabeth Báthory") ||
					page.getTitle().getPlainTitle().equals("Loch Ness Monster"))
			{
				continue;
//				System.out.println("MATCH");
			}

			try {
				Page talkPage = wiki.getDiscussionPage(page); // might be printing

				String talkText = talkPage.getText();
				if (talkText != null) {

					// Just title
					String line;

					if (page.getTitle().getPlainTitle().startsWith("Discussion:")) {
						line = page.getTitle().getPlainTitle().substring(11);
					}
					else {
						line = page.getTitle().getPlainTitle();
					}

					writer.println(line);
					//					writer.write(line);
					//					writer.newLine();
				}
			}
			catch (WikiApiException e) {
				System.out.println("Couldn't find page");
			}
			catch (Exception e) { // probably won't work
				System.out.println(String.format("PROBLEM PAGE! %s", page.getTitle().getPlainTitle()));
				blacklistWriter.println(pageTitle);
			}

		} // end for

		//	 Write to the csv
		writer.close();
		System.out.println(String.format("Iterated through %d pages", pageCounter));
		System.out.println("Wrote csv at " + outCsvName);
	}
}