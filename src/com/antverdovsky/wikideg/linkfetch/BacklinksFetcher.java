package com.antverdovsky.wikideg.linkfetch;

import java.io.IOException;
import java.util.ArrayList;

import com.antverdovsky.wikideg.util.DataParse;
import com.antverdovsky.wikideg.util.URLFetch;

/**
 * Uses a Wiki API Request to fetch the JSON data containing the backlinks of
 * a Wikipedia article.
 */
public class BacklinksFetcher implements AbstractLinkFetcher {
	@Override
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
	public ArrayList<String> getLinks(String article, String target) 
			throws IOException {
		// Declare the set of all of the Backlinks
		ArrayList<String> allBacklinks = new ArrayList<String>();

		// Get the URL of the Wiki Request Page and fetch the first JSON file
		String url = URLFetch.getBacklinksURL(article);
		String json = URLFetch.getData(url);

		// Continue fetching JSON files until we reach a file which contains
		// no continue token.
		String continueToken = "0|0";
		while (!continueToken.isEmpty()) {
			// Get the JSON data of the new URL (with the continue token)
			url = URLFetch.getBacklinksURL(article, continueToken);
			json = URLFetch.getData(url);

			// Parse the JSON data from the URL and add it to the list.
			// Fetch the continue token so that we know if we have another
			// JSON file to read or if we're done.
			continueToken = DataParse.parseBacklinksJSON(json, allBacklinks, 
					target);
		}

		return allBacklinks; // Return all of the backlinks when done
	}
}
