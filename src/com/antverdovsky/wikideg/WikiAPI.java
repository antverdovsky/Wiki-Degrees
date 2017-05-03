package com.antverdovsky.wikideg;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

public class WikiAPI {
	/**
	 * Gets all (or some, if the target is found) of the backlinks on a 
	 * Wikipedia article of the specified name.
	 * @param article The name of the article.
	 * @param target If any link extracted from the starting article is equal
	 *               to the target, the target is appended to the list and the
	 *               list is returned as is (short circuits the algorithm).
	 * @return The set of all (or some, if target is found) of the backlinks of
	 *         the Wikipedia article.
	 * @throws IOException If the article data could not be fetched.
	 */
	public static HashSet<String> getAllBacklinks(String article, 
			String target) throws IOException {
		// Declare the set of all of the Backlinks
		HashSet<String> allBacklinks = new HashSet<String>();

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
			continueToken = WikiParse.parseBacklinks(json, allBacklinks, 
					target);
		}

		return allBacklinks; // Return all of the backlinks when done
	}
	
	/**
	 * Gets all (or some, if the target is found) of the links on a Wikipedia
	 * article of the specified name.
	 * @param article The name of the article.
	 * @param target If any link extracted from the starting article is equal
	 *               to the target, the target is appended to the list and the
	 *               list is returned as is (short circuits the algorithm).
	 * @return The set of all (or some, if the target is found) of the links
	 *         of the Wikipedia article.  
	 * @throws IOException If the article data could not be fetched. 
	 */
	public static HashSet<String> getAllLinks(String article, String target) 
			throws IOException {
		// Declare the List of all of the links
		HashSet<String> allLinks = new HashSet<String>();
		
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
			continueToken = WikiParse.parseLinks(json, allLinks, target);
		}
		
		return allLinks; // Return all of the links when done
	}
}
