package com.antverdovsky.wikideg;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;

public class Separation {
	private int numDegrees;               // Degrees of Separation
	private Stack<String> path;           // Path by article names
	private Stack<String> embeddedPath;   // Path by embedded article names
	
	private static LinksFetcher linksFetcher = new LinksFetcher();
	private static BacklinksFetcher backlinksFetcher = new BacklinksFetcher();
	
	/**
	 * Returns the number of degrees of separation between two articles.
	 * @return The degrees of separation.
	 */
	public int getNumDegrees() {
		return this.numDegrees;
	}
	
	/**
	 * Returns the embedded path between two articles.
	 * @return The embedded path.
	 */
	public Stack<String> getEmbeddedPath() {
		return this.embeddedPath;
	}
	
	/**
	 * Returns the path between two articles.
	 * @return The path.
	 */
	public Stack<String> getPath() {
		return this.path;
	}
	
	/**
	 * Finds a separation between the specified start and end articles. The
	 * path is guaranteed to be the shortest path, though there may be other
	 * paths of the same length as the one returned.
	 * @param start The starting article title.
	 * @param end The ending article title.
	 * @return A separation instance containing the degrees of separation and
	 *         the path from the starting article to the ending article. If no
	 *         path exists between the start and end article, null will be
	 *         returned.
	 * @throws IOException If there occurs an error fetching the articles of
	 *                     either the starting or ending article. 
	 */
	public static Separation getSeparation(String start, String end)
				throws IOException {
		Separation separation = null;
		
		// Check if there is a zero degree separation between start and end.
		// If so, return it!
		separation = Separation.getSeparation0(start, end);
		if (separation != null) return separation;
		
		// Check if there is a one degree separation between start and end.
		// If so, return it! If not, at least we get the list of all of the
		// links to which the starting article links to.
		HashSet<String> links = new HashSet<String>();
		separation = Separation.getSeparation1(start, end, links);
		if (separation != null) return separation;
		
		// No path possible because the start contains no links!
		if (links.isEmpty()) return null;
		
		// Check if there is a two degree separation between start and end.
		// If so, return it! If not, we will get the list of all of the 
		// backlinks.
		HashSet<String> backlinks = new HashSet<String>();
		separation = Separation.getSeparation2(start, end, links, backlinks);
		if (separation != null) return separation;
		
		// No path possible because the end contains no backlinks!
		if (backlinks.isEmpty()) return null;
		
		// Last chance is checking for 3+ degrees of separation. If that
		// returns null also then this path cannot be constructed.
		separation = Separation.getSeparation3(start, end, links, backlinks);
		if (separation == null) return null; 
		
		separation.embeddedPath = Separation.getEmbeddedPath(separation.path);
		return separation;
	}
	
	/**
	 * Creates a new instance of the Separation class with the specified
	 * number of degrees of separation and the specified path.
	 * @param numD The number of degrees of separation.
	 * @param path The path from the starting link to the ending link.
	 */
	private Separation(int numD, Stack<String> path) {
		this.numDegrees = numD;
		this.path = path;
	}
	
	/**
	 * Converts the specified path to an embedded path and returns it. An
	 * embedded path will contain the names of each node in the specified
	 * path as it is present in the predecessor node, since Wikipedia
	 * sometimes uses a different article name when it is embedded in an
	 * article.
	 * @param path The standard path.
	 * @return The embedded path.
	 * @throws IOException If there is an error fetching the export for any
	 *                     of the articles in the path.
	 */
	public static Stack<String> getEmbeddedPath(Stack<String> path) 
			throws IOException {
		// Create a stack to store the embedded path.
		Stack<String> embeddedPath = new Stack<String>();
		
		// Foreach article in the standard path
		for (int i = 0; i < path.size() - 1; ++i) {
			// Get the current article title and the next article title
			String current = path.get(i);
			String next = path.get(i + 1);
			
			// Get the full export data of the current article
			String url = WikiFetch.getExportURL(current);
			String data = WikiFetch.getData(url);
			
			// Get the name of the next article as it appears in the current
			// article and push it onto the stack.
			embeddedPath.push(WikiParse.parseEmbeddedArticle(data, next));
		}
		
		// Return the embedded path
		return embeddedPath;
	}
	
	/**
	 * Checks if the separation between the start and end articles is zero
	 * degrees of separation. If so, an instance of the separation class is
	 * returned. If the separation between the start and end articles is not
	 * zero, null is returned. A zero degree separation is only possible if
	 * the starting article's title is equal to the end article's title.
	 * @param start The title of the start article.
	 * @param end The title of the end article.
	 * @return An instance of separation containing the path between start
	 *         and end, or null if no one degree separation exists.
	 */
	private static Separation getSeparation0(String start, String end) {
		Stack<String> path = new Stack<String>();
		path.push(start);
		
		// Return 0 degrees of separation if the start article equals the
		// end article, null otherwise.
		return start.equals(end) ? new Separation(0, path) : null;
	}
	
	/**
	 * Checks if the separation between the start and end articles is one
	 * degree of separation. If so, an instance of the separation class is
	 * returned. If the separation between the start and end articles is not
	 * one, null is returned. A one degree separation is only possible if the
	 * links of the start article contain the end article. Regardless of 
	 * whether the degrees of separation are one, the links hash set will be
	 * filled with all of the links of the starting article.
	 * @param start The title of the start article.
	 * @param end The title of the end article.
	 * @param links The links of the starting article. This hash set should be
	 *              empty and will be filled using this method.
	 * @return An instance of separation containing the path between start
	 *         and end, or null if no one degree separation exists.
	 * @throws IOException If the links could not be properly fetched from the
	 *                     starting article.
	 */
	private static Separation getSeparation1(String start, String end,
			HashSet<String> links) throws IOException {
		Stack<String> path = new Stack<String>();
		path.push(start);
		path.push(end); // Assuming we will find a one degree separation
		
		// Get the links of the starting article, and short circuit halt if
		// the end article is found.
		links.addAll(linksFetcher.getLinks(start, end));
		
		// If we find a one degree separation, return the separation instance,
		// or null otherwise.
		return links.contains(end) ? new Separation(1, path) : null;
	}
	
	/**
	 * Checks if the separation between the start and end articles is two
	 * degrees of separation. If so, an instance of the separation class is
	 * returned. If the separation between the start and end articles is not
	 * two, null is returned. A two degree separation is only possible if the
	 * links of the starting article and the backlinks of the ending article
	 * contain some common middle article. Regardless of whether the degrees 
	 * of separation are two, the backlinks hash set will be filled with all
	 * of the backlinks of the ending article. 
	 * @param start The title of the start article.
	 * @param end The title of the end article.
	 * @param links The links of the starting article.
	 * @param backlinks The backlinks of the ending article. This hash set 
	 *                  should be empty and will be filled using this method. 
	 * @return An instance of separation containing the path between start
	 *         and end, or null if no two degree separation exists.
	 * @throws IOException If the links could not be properly fetched from the
	 *                     starting article.
	 */
	private static Separation getSeparation2(String start, String end,
			HashSet<String> links, HashSet<String> backlinks) 
			throws IOException {
		Stack<String> path = new Stack<String>();
		path.push(start);
		
		// Get the backlinks of the ending article (short circuit halting will
		// never happen here since that would imply one degree of separation).
		HashSet<String> common = backlinksFetcher.getLinks(end, start);
		backlinks.addAll(common);
		
		// Find any articles that backlinks has in common with the links. This
		// implies that there exists some middle article such that we can go
		// from start -> middle -> end.
		common.retainAll(links);
		
		// If no middle articles exist, return null
		if (common.isEmpty()) return null;
		
		// Fetch some random article from the common set and build a path with
		// it, returning a separation of two degrees.
		String middle = common.iterator().next();
		path.push(middle);
		path.push(end);
		return new Separation(2, path);
	}
	
	/**
	 * Checks if the separation between the start and end articles is three or
	 * more degrees of separation. If so, an instance of the separation class 
	 * is returned. If the separation between the start and end articles is 
	 * not two, null is returned. A three or more degree separation is only 
	 * possible if we were unable to find a zero, one, or two degree 
	 * separation between the start and end articles.  
	 * @param start The title of the start article.
	 * @param end The title of the end article.
	 * @param links The links of the starting article.
	 * @param backlinks The backlinks of the ending article.
	 * @return An instance of separation containing the path between start
	 *         and end, or null if no three+ degree separation exists.
	 * @throws IOException If the links could not be properly fetched from the
	 *                     starting article.
	 */
	private static Separation getSeparation3(String start, String end,
			HashSet<String> links, HashSet<String> backlinks) 
			throws IOException {
		// As we fetch links to find the midpoint, we are going to need to
		// store the predecessor for each link, or successor for each 
		// backlink.
		HashMap<String, String> predecessors = new HashMap<String, String>();
		HashMap<String, String> successors = new HashMap<String, String>();
		
		// Set the predecessor and successor for each link and backlink.
		for (String s : links) predecessors.put(s, start);
		for (String s : backlinks) successors.put(s, end);
		
		// The degrees of separation (since this method applies to 3+ degrees
		// of separation, we can set this to a default value of 3).
		int degrees = 3;
		
		while (true) { // Until we have found a link
			// Build the graph from the perspective of the smaller data set.
			if (links.size() <= backlinks.size()) {
				links = Separation.getSeparation3GrowGraph(links,
						backlinks, end, predecessors, linksFetcher);
			} else {
				backlinks = Separation.getSeparation3GrowGraph(backlinks,
						links, start, successors, backlinksFetcher);
			}
			
			// Check if there is some element in common between the links and
			// backlinks. If so, then we found a path! Otherwise, we must
			// repeat the loop though the number of degrees has now increased.
			HashSet<String> common = new HashSet<String>(links);
			common.retainAll(backlinks);
			if (!common.isEmpty()) {
				// Get some random element from the common list and mark it
				// as the middle node. Now we need to backtrace through the
				// predecessors and successors to find the full path from
				// start to end.
				String middle = common.iterator().next();
				
				// Set the current predecessor / successor of the middle node
				// to itself, for now.
				String currentPredecessor = middle;
				String currentSuccessor = middle;
				
				// Create a path stack for storing the path and a queue for
				// backtracing through all of the predecessors.
				Stack<String> path = new Stack<String>();
				Stack<String> backtrace = new Stack<String>();
				
				// Until we back trace all the way back to starting title
				while (!currentPredecessor.equals(start)) {
					// Get the predecessor of the current predecessor and add
					// it to the backtrace queue.
					currentPredecessor = predecessors.get(currentPredecessor);
					backtrace.push(currentPredecessor);
				}
				
				// Pop all of the titles from the queue and onto the path
				while (!backtrace.isEmpty()) {
					path.push(backtrace.pop());
				}
				
				// For the successors, we can just add all of the the
				// successors to the path stack, including the middle node and
				// the end article.
				while (!currentSuccessor.equals(end)) {
					path.push(currentSuccessor);
					currentSuccessor = successors.get(currentSuccessor);
				}
				path.push(end);
				
				// Return the full path with the number of degrees.
				return new Separation(degrees, path);
			} else {
				++degrees;
			}
		}
	}
	
	/**
	 * Grows the graph from one side in the separation three+ degrees 
	 * algorithm.
	 * @param links The links for which we are to grow the graph. For each
	 *              link in the set, this will fetch the (back)links of the
	 *              link and add them to a set which will be returned at the
	 *              end of the method.
	 * @param otherLinks The hash set of the other side of the graph (i.e. if
	 *                   the links set is backlinks, this is the links). 
	 * @param target The target link. If this is found when fetching new links
	 *               the method will halt early.
	 * @param map The predecessor map (for links) or the successor map (for
	 *            backlinks).
	 * @param fetcher The fetcher to be used when getting the links or 
	 *                backlinks.
	 * @return The set of (back)links.
	 * @throws IOException If the links could not be properly fetched.
	 */
	private static HashSet<String> getSeparation3GrowGraph(
			HashSet<String> links, HashSet<String> otherLinks, String target,
			HashMap<String, String> map, AbstractLinkFetcher fetcher) 
			throws IOException {
		HashSet<String> newLinks = new HashSet<String>(); // New links set
		
		for (String link : links) { // Go through links
			// Get all of the (back)links of this link
			HashSet<String> linksOf = fetcher.getLinks(link, target);
			newLinks.addAll(linksOf);
			
			// Set this link as the predecessor or successor of all of the 
			// (back)links of this link and add the link to the newLinks list,
			// only if we have not yet seen this link!
			for (String linkOf : linksOf)
				if (!map.containsKey(linkOf)) 
					map.put(linkOf, link);
				
			// Check if any links of this link are contained in the
			// backlinks. If so, then this link leads to a backlink
			// and so it is a middle link and we have found a path!
			HashSet<String> common = new HashSet<String>(linksOf);
			common.retainAll(otherLinks);
			if (!common.isEmpty()) return newLinks;
		}
		
		return newLinks;
	}
}
