package com.antverdovsky.wikideg;

import java.io.IOException;
import java.util.ArrayList;

public class WikiAPI {
	/**
	 * Gets all of the backlinks on a Wikipedia article of the specified name.
	 * @param article The name of the article.
	 * @return The list of all of the backlinks of the Wikipedia article.
	 * @throws IOException If the article data could not be fetched.
	 */
	public static ArrayList<String> getAllBacklinks(String article)
			throws IOException {
		// Declare the List of all of the Backlinks
		ArrayList<String> allBacklinks = new ArrayList<String>();

		// Get the URL of the Wiki Request Page and fetch the first JSON file
		String url = WikiFetch.getBacklinksURL(article);
		String json = WikiFetch.getData(url);

		// Continue fetching JSON files until we reach a file which contains
		// no continue token.
		String continueToken = "0|0";
		while (!continueToken.isEmpty()) {
			// Get the JSON data of the new URL (with the continue token)
			url = WikiFetch.getBacklinksURL(article, continueToken);
			json = WikiFetch.getData(url);

			// Parse the JSON data from the URL and add it to the list.
			// Fetch the continue token so that we know if we have another
			// JSON file to read or if we're done.
			continueToken = WikiParse.parseBacklinks(json, allBacklinks);
		}

		return allBacklinks; // Return all of the backlinks when done
	}
	
	/**
	 * Gets all of the forward links on a Wikipedia article of the specified
	 * name.
	 * @param article The name of the article.
	 * @return The list of all of the links of the Wikipedia article.  
	 * @throws IOException If the article data could not be fetched. 
	 */
	public static ArrayList<String> getAllLinks(String article) 
			throws IOException {
		// Declare the List of all of the links
		ArrayList<String> allLinks = new ArrayList<String>();
		
		// Get the URL of the Wiki Request Page and fetch the first JSON file
		String url = WikiFetch.getLinksURL(article);
		String json = WikiFetch.getData(url);
		
		// Continue fetching JSON files until we reach a file which contains
		// no continue token.
		String continueToken = "0|0|0";
		while (!continueToken.isEmpty()) {
			// Get the JSON data of the new URL (with the continue token)
			url = WikiFetch.getLinksURL(article, continueToken);
			json = WikiFetch.getData(url);
			
			// Parse the JSON data from the URL and add it to the list
			continueToken = WikiParse.parseLinks(json, allLinks);
		}
		
		return allLinks; // Return all of the links when done
	}
}
