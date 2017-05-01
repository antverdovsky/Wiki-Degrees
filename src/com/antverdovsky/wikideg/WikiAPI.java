package com.antverdovsky.wikideg;

import java.io.IOException;
import java.util.ArrayList;

import com.antverdovsky.wikideg.WikiParse.LinksResult;

public class WikiAPI {
	/**
	 * Gets all of the forward links on a Wikipedia article of the specified
	 * name.
	 * @param article The name of the article.
	 * @return A 2D List of all of the links. Each row in the list contains a
	 *         list of article titles, with at most 500 articles per list.  
	 * @throws IOException If the article data could not be fetched. 
	 */
	public static ArrayList<ArrayList<String>> getAllLinks(String article) 
			throws IOException {
		// Declare the 2D List of all of the links
		ArrayList<ArrayList<String>> allLinks = 
				new ArrayList<ArrayList<String>>();
		
		// Get the URL of the Wiki Request Page and fetch the first JSON file
		String url = WikiFetch.getLinksURL(article);
		String json = WikiFetch.getData(url);
		
		// Set the result to a null list and a continue token of zero (first
		// page).
		LinksResult result = new LinksResult(null, "0|0|0");
		
		// Continue fetching JSON files until we reach a file which contains
		// no continue token. For each JSON file parsed, add its links to the
		// all links 2D list.
		while (!result.getLContinue().isEmpty()) {
			// Get the JSON data of the new URL (with the continue token)
			url = WikiFetch.getLinksURL(article, result.getLContinue());
			json = WikiFetch.getData(url);
			
			// Parse the JSON data from the URL and add it to the list
			result = WikiParse.parseLinks(json);
			allLinks.add(result.getLinks());
		}
		
		return allLinks; // Return all of the links when done
	}
}
