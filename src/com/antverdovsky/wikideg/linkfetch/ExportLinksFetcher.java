package com.antverdovsky.wikideg.linkfetch;

import java.io.IOException;
import java.util.ArrayList;

import com.antverdovsky.wikideg.util.DataParse;
import com.antverdovsky.wikideg.util.URLFetch;

/**
 * Uses the export data of a Wikipedia article to fetch the links contained on
 * the article page. This usually fails to fetch template and external links
 * so it may not always result in the shortest path being found, however, it
 * is a lot faster than the JSONLinksFetcher.
 */
public class ExportLinksFetcher implements AbstractLinkFetcher {
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

		// Get the URL of the Wiki Request Page and fetch the export data
		String url = URLFetch.getExportURL(article);
		String export = URLFetch.getData(url);

		DataParse.parseLinksExport(export, allLinks, targets);

		return allLinks; // Return all of the links when done
	}
}
