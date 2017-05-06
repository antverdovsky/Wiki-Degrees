package com.antverdovsky.wikideg;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;

public class Separation {
	// Fetchers for Links and Backlinks.
	private static LinksFetcher linksFetcher = new LinksFetcher();
	private static BacklinksFetcher backlinksFetcher = new BacklinksFetcher();
	
	private int numDegrees;               // Degrees of Separation
	private Stack<String> path;           // Path by article names
	private Stack<String> embeddedPath;   // Path by embedded article names
	private boolean pathExists;           // Does a path exist?
	
	private String startArticle;          // The article where the path starts
	private String endArticle;            // The article where the path ends
	
	private HashSet<String> links;        // Links built from start node
	private HashSet<String> backlinks;    // Backlinks built from end node
	
	private HashMap<String, String>       // Predecessors of each link in the 
			predecessors;                 // links set.
	private HashMap<String, String>       // Successors of each backlink in 
			successors;                   // the backlinks set.
	
	/**
	 * Creates a new Separation class and computes the path from the starting
	 * article to the ending article.
	 * @param start The start article.
	 * @param end The end article.
	 * @throws IOException If there is an error fetching the links or
	 *                     backlinks for any articles.
	 */
	public Separation(String start, String end) throws IOException {
		this.startArticle = start;
		this.endArticle = end;
		
		this.numDegrees = 0;
		this.path = new Stack<String>();
		this.embeddedPath = new Stack<String>();
		this.pathExists = false;
		
		this.links = new HashSet<String>();
		this.backlinks = new HashSet<String>();
		this.predecessors = new HashMap<String, String>();
		this.successors = new HashMap<String, String>();
		
		// Try to find a zero degree of separation path
		this.pathExists = this.getSeparation0();
		if (this.pathExists) return;
		
		// Try to find a one degree of separation path
		this.pathExists = this.getSeparation1();
		if (this.pathExists) return;
		
		// If the starting article contains no embedded links, no path is
		// possible.
		if (this.links.isEmpty()) return;
		
		// Try to find a two degree of separation path
		this.pathExists = this.getSeparation2();
		if (this.pathExists) return;
		
		// If the ending article contains no backlinks that link to it, no
		// path is possible.
		if (this.backlinks.isEmpty()) return;
		
		// Try to find a three or more degree of separation path
		this.pathExists = this.getSeparation3();
	}
	
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
	 * degrees of separation. Regardless, the starting article is pushed
	 * onto the path stack.
	 * @param start The title of the start article.
	 * @param end The title of the end article.
	 * @return True if the degrees of separation is zero. False otherwise.
	 */
	private boolean getSeparation0() {
		this.path.push(this.startArticle);
		
		// If start == end -> 0 degrees of separation
		return this.startArticle.equals(this.endArticle);
	}
	
	/**
	 * Checks if the separation between the start and end articles is one
	 * degree of separation. This method will also fetch all of the links of
	 * the starting article. If the degrees of separation is one, the ending
	 * article is pushed onto the path stack.
	 * @return True if the degrees of separation is one.
	 * @throws IOException If the links could not be properly fetched from the
	 *                     starting article.
	 */
	private boolean getSeparation1() throws IOException {
		++(this.numDegrees);
		
		// Get the links of the starting article, and short circuit halt if
		// the end article is found.
		HashSet<String> newLinks = linksFetcher.getLinks(this.startArticle,
				this.endArticle);
		this.links.addAll(newLinks);
		
		// If the links contain the end article, then we have one degree of
		// separation.
		if (this.links.contains(this.endArticle)) {
			this.path.push(this.endArticle);
			return true;
		}
		
		return false;
	}
	
	/**
	 * Checks if the separation between the start and end articles is two
	 * degrees of separation. This method will also fetch all of the backlinks
	 * of the ending article. If the degrees of separation is two, the path is
	 * built by this method as well.
	 * @return True if a two degree path was found. False otherwise.
	 * @throws IOException If the links could not be properly fetched from the
	 *                     starting article.
	 */
	private boolean getSeparation2() throws IOException {
		++(this.numDegrees);
		
		// Get the backlinks of the ending article (short circuit halting will
		// never happen here since that would imply one degree of separation).
		HashSet<String> common = backlinksFetcher.getLinks(this.endArticle, 
				this.startArticle);
		backlinks.addAll(common);
		
		// Find any articles that backlinks has in common with the links. This
		// implies that there exists some middle article such that we can go
		// from start -> middle -> end.
		common.retainAll(this.links);
		
		// If no middle articles exist, return false
		if (common.isEmpty()) return false;
		
		// Fetch some random article from the common set and build a path with
		// it, returning a separation of two degrees.
		String middle = common.iterator().next();
		this.path.push(middle);
		this.path.push(this.endArticle);
		return true;
	}
	
	/**
	 * Checks if the separation between the start and end articles is three or
	 * more degrees of separation. If there exists a path, the path stack is
	 * built and true is returned.
	 * @return True if a three or more degree path was found. False otherwise.
	 * @throws IOException If the links could not be properly fetched from the
	 *                     starting article.
	 */
	private boolean getSeparation3() throws IOException {
		++(this.numDegrees);
		
		// Set the predecessor and successor for each link and backlink.
		for (String s : this.links) predecessors.put(s, this.startArticle);
		for (String s : this.backlinks) successors.put(s, this.endArticle);
		
		while (true) { // Until we have found a link
			// Build the graph from the perspective of the smaller data set.
			if (this.links.size() <= this.backlinks.size()) {
				links = this.getSeparation3GrowGraph(linksFetcher);
			} else {
				backlinks = this.getSeparation3GrowGraph(backlinksFetcher);
			}
			
			// Check if there is some element in common between the links and
			// backlinks. If so, then we found a path! Otherwise, we must
			// repeat the loop though the number of degrees has now increased.
			HashSet<String> common = new HashSet<String>(this.links);
			common.retainAll(this.backlinks);
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
				
				// Create a stack for backtracing the path
				Stack<String> backtrace = new Stack<String>();
				
				// Until we back trace all the way back to starting title
				while (!currentPredecessor.equals(this.startArticle)) {
					// Get the predecessor of the current predecessor and add
					// it to the backtrace queue.
					currentPredecessor = predecessors.get(currentPredecessor);
					backtrace.push(currentPredecessor);
				}
				
				// Pop all of the titles from the queue and onto the path with
				// the exception of the starting article since that is already
				// in the path.
				backtrace.pop();
				while (!backtrace.isEmpty()) {
					this.path.push(backtrace.pop());
				}
				
				// For the successors, we can just add all of the the
				// successors to the path stack, including the middle node and
				// the end article.
				while (!currentSuccessor.equals(this.endArticle)) {
					this.path.push(currentSuccessor);
					currentSuccessor = successors.get(currentSuccessor);
				}
				this.path.push(this.endArticle);
				
				return true;
			} else {
				++(this.numDegrees);
			}
		}
	}
	
	/**
	 * Grows the graph from one side in the separation three+ degrees 
	 * algorithm.
	 * @param fetcher The fetcher to be used when getting the links or 
	 *                backlinks. If this is a LinksFetcher, the graph will be
	 *                grown from the starting node side. Otherwise, the graph
	 *                will be grown from the ending node side.
	 * @return The new set of (back)links.
	 * @throws IOException If the links could not be properly fetched.
	 */
	private HashSet<String> getSeparation3GrowGraph(
			AbstractLinkFetcher fetcher) throws IOException {
		HashSet<String> newLinks = new HashSet<String>(); // New links set
		
		// If the parameter was a links fetcher then we need to build the
		// graph from the starting node. Otherwise, set up the parameters to
		// build the graph from the ending node.
		boolean isStartSide = fetcher == Separation.linksFetcher;	
		String target = isStartSide ? this.endArticle : this.startArticle;
		HashSet<String> thisSide = isStartSide ? this.links : this.backlinks;
		HashSet<String> otherSide = isStartSide ? this.backlinks : this.links;
		HashMap<String, String> map = isStartSide ? 
				this.predecessors : this.successors;

		for (String link : thisSide) { // Go through links
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
			common.retainAll(otherSide);
			if (!common.isEmpty()) return newLinks;
		}
		
		return newLinks;
	}
}
