package com.antverdovsky.wikideg.linkfetch;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Interface used by all link and backlink fetchers.
 */
public interface AbstractLinkFetcher {
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
			ArrayList<String> targets) throws IOException;
}