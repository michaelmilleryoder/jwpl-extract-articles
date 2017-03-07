import java.sql.Timestamp;
import java.util.List;

import de.tudarmstadt.ukp.wikipedia.api.DatabaseConfiguration;
import de.tudarmstadt.ukp.wikipedia.api.Page;
import de.tudarmstadt.ukp.wikipedia.api.WikiConstants.Language;
import de.tudarmstadt.ukp.wikipedia.api.Wikipedia;
import de.tudarmstadt.ukp.wikipedia.parser.ParsedPage;
import de.tudarmstadt.ukp.wikipedia.parser.mediawiki.MediaWikiParser;
import de.tudarmstadt.ukp.wikipedia.parser.mediawiki.MediaWikiParserFactory;
import de.tudarmstadt.ukp.wikipedia.revisionmachine.api.RevisionApi;

public class WikipediaTest {

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
		
		Page p = wiki.getPage("Palestine");
		int articleId = p.getPageId();
		
		//Access revision using the JWPL RevisionApi
		List<Timestamp> tsList = revApi.getRevisionTimestamps(articleId);
		String revText = revApi.getRevision(articleId, tsList.get(0)).getRevisionText(); 
		
		//parse wiki markup
		MediaWikiParserFactory pf = new MediaWikiParserFactory();
		MediaWikiParser parser = pf.createParser();
//		pf.setTemplateParserClass( FlushTemplates.class ); //optionally remove all templates from  markup
		ParsedPage pp = parser.parse(revText);
		
		System.out.println(pp.getText());
	}

}

