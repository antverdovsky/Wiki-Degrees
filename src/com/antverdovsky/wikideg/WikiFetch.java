package com.antverdovsky.wikideg;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.stream.Collectors;

public class WikiFetch {
	/**
	 * Downloads the data from the specified URL and places it into a String.
	 * @param urlLink The URL link from which to download the data.
	 * @return A String containing all of the data of the link.
	 * @throws IOException If there occurs an error reading the data from the
	 * 					   URL. 
	 */
	public static String getData(String urlLink) throws 
			IOException {
		URLConnection connection = null;    // Connection to the URL data
	    InputStreamReader iSR = null;       // Stream of the URL data
	    BufferedReader bR = null;           // Reader of URL data
	    URL url = null;                     // URL based on the specified link

	    // Open the connection to the URL web page
	    url = new URL(urlLink);
	    connection = url.openConnection();

	    // Initialize the Readers
	    iSR = new InputStreamReader(connection.getInputStream());
	    bR = new BufferedReader(iSR);

	    // Fetch all of the lines from the buffered reader and join them all
	    // together into a single string.
	    return bR.lines().collect(Collectors.joining("\n"));
	}
	
	/**
	 * Gets the URL of the Wiki Request Page containing the referenced links
	 * of the page with the specified name.
	 * @param name The name of the page for which the referenced links page is
	 *			   to be fetched.
	 * @return The URL link.
	 */
	public static String getLinksURL(String name) {
		// Construct the URL onto which the title of the page will be added.
		// This requests the links used on some page in JSON format.
		final String PRE_TITLE_URL = "https://en.wikipedia.org/w/api.php?" + 
				"action=query&format=json&prop=links&pllimit=max&titles=";
		
		return WikiFetch.appendURL(PRE_TITLE_URL, name);
	}
	
	/**
	 * Gets the URL of the Wiki Request Page containing the title which link
	 * reference the page of the specified name.
	 * @param name The name of the page for which the backlinks page is to be
	 * 			   fetched.
	 * @return The URL link.
	 */
	public static String getBacklinksURL(String name) {
		// Construct the URL onto which the title of the page will be added.
		// This requests the backlinks used on some page in JSON format,
		// excluding any redirect pages.
		final String PRE_TITLE_URL = "https://en.wikipedia.org/w/api.php?" + 
				"action=query&format=json&list=backlinks&bllimit=max&" + 
				"blfilterredir=nonredirects&bltitle=";
		
		return WikiFetch.appendURL(PRE_TITLE_URL, name);
	}
	
	/**
	 * Appends the name string onto a copy of the pre title string and returns
	 * the result.
	 * @param preTitle The pre title URL string.
	 * @param name The name of the page to be appended.
	 * @return The appended URL string.
	 */
	private static String appendURL(String preTitle, String name) {
		// Replace any spaces in the input string with underscores since URLs
		// do not have any spaces.
		name = name.replaceAll(" ", "_");
				
		// Build the String using the PRE_TITLE_URL + name, and return the
		// result.
		StringBuilder sB = new StringBuilder(preTitle);
		sB.append(name);
		return sB.toString();
	}
}
