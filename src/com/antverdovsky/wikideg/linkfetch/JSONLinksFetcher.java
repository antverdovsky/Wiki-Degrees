package com.antverdovsky.wikideg.linkfetch;

import java.io.IOException;
import java.util.ArrayList;

import com.antverdovsky.wikideg.util.DataParse;
import com.antverdovsky.wikideg.util.URLFetch;

/**
 * Uses a Wiki API Request to fetch the JSON data containing the links of a
 * Wikipedia article. This fetches every single link cited on a page and is
 * therefore guaranteed to always return the shortest path, though it may take
 * a lot longer to fetch links than the ExportLinksFetcher.
 */
public class JSONLinksFetcher implements AbstractLinkFetcher {
	@Override
	/**
	 * Returns all (or some, if the target is found) of the links (or 
	 * backlinks) of the article with the specified name.
	 * @param article The name of the article.
	 * @param targets If any link extracted from the starting article is equal
	 *                to any of the targets, the target is appended to the
	 *                list and the list is returned as is (short circuits the 
	 *                algorithm). 
	 * @return The set of all (or some) of the links or backlinks of the
	 *         Wikipedia article.
	 * @throws IOException If the article data could not be fetched.
	 */
	public ArrayList<String> getLinks(String article, 
			ArrayList<String> targets) throws IOException {
		// Declare the List of all of the links
		ArrayList<String> allLinks = new ArrayList<String>();

		// Get the URL of the Wiki Request Page and fetch the first JSON file
		String url = URLFetch.getLinksURL(article);
		String json = URLFetch.getData(url);

		// Continue fetching JSON files until we reach a file which contains
		// no continue token.
		String continueToken = "0|0|0";
		while (!continueToken.isEmpty()) {
			// Get the JSON data of the new URL (with the continue token)
			url = URLFetch.getLinksURL(article, continueToken);
			json = URLFetch.getData(url);

			// Parse the JSON data from the URL and add it to the list
			continueToken = DataParse.parseLinksJSON(json, allLinks, targets);
		}

		return allLinks; // Return all of the links when done
	}
}