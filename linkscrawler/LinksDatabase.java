package linkscrawler;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * @author Aleksandar Abu-Samra
 */
public class LinksDatabase {
	private int maxNumOfLinks;
	
	// Predefined initialization
	private int nextLink = 0;
	private Semaphore linksAvailable = new Semaphore(0);
	private final Object lock = new Object();
	private ArrayList<String> linksList = new ArrayList<String>();
	

	/**
	 * Constructor
	 * 
	 * @param maxNumOfLinks
	 * @param outputFilePath
	 */
	public LinksDatabase(int maxNumOfLinks) {
		this.maxNumOfLinks = maxNumOfLinks;
	}	
	
	/**
	 *
	 * @param link
	 * @return
	 */
	public boolean putNextLink(String link) {
		if (null == link || link.isEmpty()) return false;
		
		synchronized (lock) {
			if (linksList.size() >= maxNumOfLinks) {
				return false;
			}
			
			// Check if link is already in database
			// [notice] Could be optimized with sorted list
			for (String iterator : linksList) {
				if (iterator.equals(link)) return false;
			}
			
			// Add link to database
			linksList.add(link);
			
			// Print as you go
			System.out.println(linksList.size() + ": " + link);
		}
		
		linksAvailable.release();
		return true;
	}
	

	
	/**
	 * [blocking]
	 * 
	 * @return
	 * @throws InterruptedException
	 */
	public String getNextLink() throws InterruptedException {
		linksAvailable.acquire();
		
		synchronized (lock) {
			if (linksList.size() >= maxNumOfLinks) {
				linksAvailable.release();
				return null;
			}

			return linksList.get(nextLink++);
		}
	}
	
	/**
	 * Prints all links to screen
	 */
	public void printLinks() {
		for (String link : linksList) {
			System.out.println(link);
		}
	}
	

	/**
	 *
	 * @param outputFilePath
	 * @return
	 */
	public boolean saveLinksToFile(String outputFilePath) {
		if (null == outputFilePath || outputFilePath.isEmpty()) return false;
		
		try {
			PrintWriter writer = new PrintWriter(outputFilePath, "UTF-8");

			for (String link : linksList) {
				writer.println(link);
			}

			writer.close();
			
			return true;
		} catch (FileNotFoundException ex) {
			Logger.getLogger(LinksDatabase.class.getName()).log(Level.SEVERE, null, ex);
		} catch (UnsupportedEncodingException ex) {
			Logger.getLogger(LinksDatabase.class.getName()).log(Level.SEVERE, null, ex);
		}
		
		return false;
	}
}
