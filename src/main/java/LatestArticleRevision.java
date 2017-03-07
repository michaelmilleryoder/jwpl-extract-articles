import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

/**
 * 
 * @author Michael Miller Yoder
 * 
 * Output latest article revisions of Wikipedia article names read from a file
 *
 */

public class LatestArticleRevision {
	
	public static List<String> readFile(String fpath) {
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
		//setup db config
		List<String> dbcred = readFile("db_cred.txt");
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

//		String articleNamesCsv = "/home/michael/school/research/wp/wikipedia/data/talk_pagenames_10k.txt";
		String articleNamesCsv = "talk_pagenames_10k.txt";
		String outpath = "latest_revisions.csv";
		File outfile = new File(outpath);
		CSVWriter writer = new CSVWriter(new FileWriter(outpath, true), ',');

		List<String> pageNames = readFile(articleNamesCsv);
		System.out.println("Number of article names " + pageNames.size());
//		String[] pageNames = {"McDonald's"};
		WriteCsv csvWriter = new WriteCsv();

		List<String[]> lines = new ArrayList<String[]>();
		
		int counter = 0;
		
		for (String pageName : pageNames) {
			
//			counter++;
//			if (counter > 5) {
//				break;
//			}

			try {
				Page p = wiki.getPage(pageName);
				int articleId = p.getPageId();

				//Access revision using the JWPL RevisionApi
				List<Timestamp> tsList = revApi.getRevisionTimestamps(articleId); // is a LinkedList

				// set actual Timestamps to all revisions (opt)
				List<Timestamp> actualTimestamps = tsList;

				int numRevisions = actualTimestamps.size();

				// Get and save latest revision
				Timestamp ts = actualTimestamps.get(numRevisions - 1);
				Revision rev = revApi.getRevision(articleId, ts);
				String revText = rev.getRevisionText();
				String revisionEditor = rev.getContributorName();
				String revisionComment = rev.getComment();

				//parse wiki markup
				MediaWikiParserFactory pf = new MediaWikiParserFactory();
				pf.setTemplateParserClass( FlushTemplates.class ); //optionally remove all templates from  markup
				MediaWikiParser parser = pf.createParser();
				ParsedPage pp = parser.parse(revText);
//				String parsedText = pp.getText().replaceAll("\n", " ").replaceAll("\t", " ");
				
				// compress whitespace
				String parsedText = pp.getText().replaceAll("\\s+", " ").trim();

				String date = sdf.format(ts);

				// Add info to the array
				String[] line = {pageName, date, parsedText, revisionEditor, revisionComment};
				//						lines.add(line);
				writer.writeNext(line);
				System.out.println(String.format("\tArticle %s added", pageName));


				// Write to the csv
				//					writer.writeAll(lines);
				//					writer.close();
				//					System.out.println("Wrote csv at " + outpath);
			}
			catch (Exception e) {
				System.out.println(String.format("Problem getting article", pageName));
				continue;
			}
		}
		
		writer.close();
		System.out.println(String.format("Finished: check %s", outpath));
	}
	
}