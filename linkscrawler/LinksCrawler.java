package linkscrawler;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * Simple Java multithreaded crawler and links fetcher
 * 
 * @author Aleksandar Abu-Samra
 */
public class LinksCrawler {
	// params
	private static final int NUM_OF_CRAWLERS = 10;
	private static final int NUM_OF_LINKS = 1000;
	private static final String START_PAGE = "http://reddit.com";
	private static final String OUTPUT_FILE = "links.txt";

	
	/**
	 * Validates a link by testing connection
	 * 
	 * @param link
	 * @return
	 */
	public static boolean linkValid(String link) {
		try {
			URL url = new URL(link);
			URLConnection conn = url.openConnection();
			conn.connect();
		} catch (IOException e) {
			return false;
		}
		return true;
	}
	
	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		// Validate input
		if (!linkValid(START_PAGE)) {
			System.err.println("Link invalid or not responding. Try again.");
			return;
		}
		
		// init
		LinksDatabase linksDatabase = new LinksDatabase(NUM_OF_LINKS);
		Crawler [] crawlerList = new Crawler[NUM_OF_CRAWLERS];
		linksDatabase.putNextLink(START_PAGE);

		
		// Start crawlers
		for (int i=0; i<crawlerList.length; i++) {
			crawlerList[i] = new Crawler(linksDatabase);
			crawlerList[i].start();
		}
		
		// Wait for crawlers to finish
		for (int i=0; i<crawlerList.length; i++) {
			try {
				crawlerList[i].join();
			} catch (InterruptedException ex) {
				Logger.getLogger(LinksCrawler.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
		
		// Save result to file
		linksDatabase.saveLinksToFile(OUTPUT_FILE);
	}
}