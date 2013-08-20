package linkscrawler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 * @author Aleksandar Abu-Samra
 */
public class Crawler extends Thread {
	private LinksDatabase linksDatabase;

	/**
	 * Constructor
	 * 
	 * @param linksDatabase
	 */
	public Crawler(LinksDatabase linksDatabase) {
		this.linksDatabase = linksDatabase;
	}
	
	
	/**
	 * Downloads a web page source code into String
	 * 
	 * @param url	URL of page to be crawled 
	 * @return		HTML source
	 * @throws IOException 
	 */
	private String downloadPage(URL url) throws IOException {
		String html = "";
		
		BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
		
		String line = null;
		while (null != (line = br.readLine())) {
			html += line;
		}		
		
		return html;
	}
	
	/**
	 * Makes a full page path out of URL and relative link
	 * 
	 * @param url	URL of page crawled
	 * @param link	relative link
	 * @return 
	 */
	private String getAbsoluteUrl(URL url, String link) {
		// Link not okay
		if (link == null || link.isEmpty()) return null;
		if (link.matches("javascript:.*|mailto:.*")) return null;
		
		// Link okay
		if (link.matches("https?://.*")) return link;
		
		// Fix
		String root;
		if ('#' == link.charAt(0) || '?' == link.charAt(0)) {
			root = url.toString();
		}
		else if ('/' == link.charAt(0)) {
			root = url.getProtocol() + "://" + url.getHost();
		}
		else {
			root = url.toString().substring(0, url.toString().lastIndexOf("/"));
		}
		
		return root + link;
	}
	
	/**
	 * Web page parser
	 * 
	 * @param pageString 
	 */
	private void parseLinks(URL url, String html) {
		// Patterns
		Pattern tagPattern = Pattern.compile("<a\\b[^>]*href=[\"'][^>]*>(.*?)</a>");
		Pattern linkPattern = Pattern.compile("href=[\"'][^>\"']*"); // Does not include last quotation mark
		
		Matcher tagMatcher = tagPattern.matcher(html);
		
		while (tagMatcher.find()) {
			String tag = tagMatcher.group();
			Matcher linkMatcher = linkPattern.matcher(tag);
			
			// If there is actually a link
			if (linkMatcher.find()) {
				String link = linkMatcher.group().replaceFirst("href=[\"']", "");
				
				link = getAbsoluteUrl(url, link);
				
				// Save to database
				linksDatabase.putNextLink(link);
			}
		}
		
	}
	
	@Override
	public void run() {
		while (true) {
			try {
				// [blocking] Get link from database
				String link = linksDatabase.getNextLink();
				if (null == link || link.isEmpty()) break;

				// Form URL and download page content
				URL url = new URL(link);
				String html = downloadPage(url);

				// Extract links
				parseLinks(url, html);
				
			} catch (IOException ex) {
				Logger.getLogger(Crawler.class.getName()).log(Level.SEVERE, null, ex);
			} catch (InterruptedException ex) {
				Logger.getLogger(Crawler.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}
	
}
