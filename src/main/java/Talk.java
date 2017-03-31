import java.io.PrintWriter;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.text.SimpleDateFormat;

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

/**
 * 
 * @author Michael Miller Yoder
 * 
 * This script gets full texts of talk pages.
 *
 */

public class Talk {

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
		
		//SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		// Get list of ideal dates
//					int nMonths = 3;
//					Date startDate = sdf.parse("2004-04-08 00:00:00");
//					Date endDate = sdf.parse("2014-07-07 00:00:00");
//					ArrayList<Date> idealDateList = getDates(Calendar.DAY_OF_MONTH, 14, startDate, endDate);
		
					// Print ideal date list
//					System.out.println("Ideal date list:");
//					for (Date idealDate : idealDateList) {
//						System.out.println(sdf.format(idealDate));
//					}
//					System.out.println("");
					
		
		//String[] pageNames = {"East Jerusalem"};
		//String[] pageNames = {"Gaza Strip"};
		String[] pageNames = {"Israeli-Palestinian Conflict"};
		//String[] pageNames = {"East Jerusalem", "Gaza Strip", "State of Palestine", "Temple Mount", "Israeli-Palestinian Conflict", "1948 Palestine War"};
		WriteCsv csvWriter = new WriteCsv();
		
		// to write to csv
		//ArrayList<ArrayList> lines = new ArrayList();
		int numRevisions = 0;
		
		// figure out how many revisions are printing
		for (String pageName : pageNames) {
			//Page p = wiki.getPage(pageName);
			Page p = wiki.getDiscussionPage(pageName);
			int currentTalkId = p.getPageId();
		
			//Access revision using the JWPL RevisionApi
			List<Timestamp> tsList = revApi.getRevisionTimestamps(currentTalkId); // is a LinkedList

			// Select revision timestamps
			//List<Timestamp> actualTimestamps = new LinkedList<Timestamp>();

			// set actual Timestamps to all revisions (opt)
			List<Timestamp> actualTimestamps = tsList;
			numRevisions += tsList.size();
		}
		
		// Build data structure to write to csv
		String[][] lines = new String[numRevisions][3];

		int revIdx = 0;
		for (String pageName : pageNames) {
			//Page p = wiki.getPage(pageName);
			Page p = wiki.getDiscussionPage(pageName);
			int currentTalkId = p.getPageId();
		
			//Access revision using the JWPL RevisionApi
			List<Timestamp> tsList = revApi.getRevisionTimestamps(currentTalkId); // is a LinkedList
			//int articleId = p.getPageId();
		
			// Select revision timestamps
			//List<Timestamp> actualTimestamps = new LinkedList<Timestamp>();

			// set actual Timestamps to all revisions (opt)
			List<Timestamp> actualTimestamps = tsList;
			
			// Keep marker of oldest date to consider
			int oldestDateIdx = 0;
			
			// Loop through idealDateList starting at oldest and compare to revision timestamps
			//for (Date idealDate : idealDateList) {
			//	
			//	// Initialize pointer in revision history date list
			//	Timestamp closestDate = tsList.get(oldestDateIdx);
			//			
			//	while (idealDate.compareTo(closestDate) > 0) { // stops when revision date is after an ideal date
			//		if (oldestDateIdx < tsList.size()) {
			//			closestDate = tsList.get(oldestDateIdx);
			//			oldestDateIdx++;
			//		}
			//		else {
			//			System.out.println("No revisions found that are after the ideal date " + idealDate);
			//			break;
			//		}
			//	}
			//	
			//	actualTimestamps.add(closestDate);
			//}
			
			// Print actual timestamps
			//System.out.println("Actual date list:");
			//for (Date actualDate : actualTimestamps) {
			//	System.out.println(sdf.format(actualDate));
			//}
			//System.out.println("");
			
			// Get and save revisions
			for (Timestamp ts : actualTimestamps) {
				//System.out.println(ts);
				//Timestamp stamp = tsList.get(tsList.size()-1);
				//Timestamp ts = (Timestamp) stamp;
				Revision rev = revApi.getRevision(currentTalkId, ts);
				String revText = rev.getRevisionText();
				String revisionEditor = rev.getContributorName();
				//String revisionComment = rev.getComment();
		
				//parse wiki markup
				MediaWikiParserFactory pf = new MediaWikiParserFactory();
				pf.setTemplateParserClass( FlushTemplates.class ); //optionally remove all templates from  markup
				MediaWikiParser parser = pf.createParser();
				ParsedPage pp = parser.parse(revText);
				String parsedText = pp.getText().replaceAll("\n", "").replaceAll("\t", "");
				
				String date = sdf.format(ts);
				
				// Add info to the array
				//ArrayList<String> line = new ArrayList();
				//String[] line = {pageName, date, parsedText};
				//String[] line = {date, revisionEditor, revisionComment};
				String[] line = {date, parsedText, revisionEditor};
			//	line.add(pageName);
			//	line.add(date);
			//	line.add(parsedText);	
				//lines.add(line);
				lines[revIdx] = line;
				//System.out.println(line);
				System.out.println("Revision " + revIdx + '/' + numRevisions + " added");
				revIdx++;
			
				// Write text to a file
//				String outFileName = "out/" + pageName.replaceAll(" ", "_").toLowerCase() + "_" + date + ".txt";
//				PrintWriter printWriter = new PrintWriter(outFileName);
//				printWriter.print(pp.getText());
//				printWriter.close();
//				System.out.println(outFileName + " written");
		
			}
		}
			
			// Write to the csv
			//String csvFileName = "out/east_jerusalem_all_revisions.tsv";
			//String csvFileName = "out/east_jerusalem_usernames.tsv";
			//String csvFileName = "out/gaza_strip_all_revisions.tsv";
			//String csvFileName = "out/gaza_strip_usernames.tsv";
			//String csvFileName = "out/israeli-palestinian_conflict_all_revisions.tsv";
			String csvFileName = "out/israeli-palestinian_conflict_talk.tsv";
			csvWriter.writeCsv(csvFileName, lines, '\t');
			System.out.println("Wrote csv at " + csvFileName);
	}
	
	public static ArrayList<Date> getDates(int calendarUnit, int nUnits, Date startDate, Date endDate) throws Exception {
		/* 
		 * calendarUnit can be:
		 * 	Calendar.MONTH
		 * 	Calendar.DAY
		 */
		
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		
		Calendar c = Calendar.getInstance();
		c.setTime(startDate);
	
		ArrayList dateList = new ArrayList();
		
		for (int i=0; c.getTime().compareTo(endDate) <= 0; i++) {
			dateList.add(c.getTime());
			c.add(calendarUnit, nUnits);
		}
		
		return dateList;
	}
}