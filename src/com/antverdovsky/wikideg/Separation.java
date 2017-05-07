package com.antverdovsky.wikideg;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

class ThreadedGraphGrower implements Runnable {
	static volatile boolean isDone = false;
	
	private List<String> writeTo;
	private List<String> task;
	private List<String> targets;
	private ConcurrentHashMap<String, String> map;
	private AbstractLinkFetcher linkFetcher;
	
	public ThreadedGraphGrower(List<String> writeTo, List<String> task, 
			AbstractLinkFetcher linkFetcher, List<String> targets,
			ConcurrentHashMap<String, String> map) {
		this.writeTo = writeTo;
		this.task = task;
		this.linkFetcher = linkFetcher;
		this.targets = targets;
		this.map = map;
	}
	
	public void run() {
		int index = 0;
		
		while (!ThreadedGraphGrower.isDone && index < this.task.size()) {
			String link = this.task.get(index);
			
			ArrayList<String> linksOf = new ArrayList<String>();
			try { linksOf = linkFetcher.getLinks(link, ""); } 
			catch (IOException e) { e.printStackTrace(); }

			for (String linkOf : linksOf) {
				this.map.putIfAbsent(linkOf, link);
				this.writeTo.add(linkOf);
			}

			linksOf.retainAll(this.targets);
			if (!linksOf.isEmpty()) ThreadedGraphGrower.isDone = true;
			
			++index;
		}
	}
}

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
	
	private List<String> links;           // Links built from start node
	private List<String> backlinks;       // Backlinks built from end node
	
	private ConcurrentHashMap<String, String> 
			predecessors; // Predecessors of each link in the links set
	private ConcurrentHashMap<String, String>        
			successors;   // Successors of each backlink in the backlinks set
	
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
		
		this.links = Collections.synchronizedList(
				new ArrayList<String>());
		this.backlinks = Collections.synchronizedList(
				new ArrayList<String>());
		this.predecessors = new ConcurrentHashMap<String, String>();
		this.successors = new ConcurrentHashMap<String, String>();
		
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
	 * Returns whether or not a path was found.
	 * @return True if a path was found. False otherwise.
	 */
	public boolean getPathExists() {
		return this.pathExists;
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
		ArrayList<String> newLinks = linksFetcher.getLinks(this.startArticle,
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
		ArrayList<String> common = backlinksFetcher.getLinks(this.endArticle, 
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
				
				if (links.isEmpty()) return false;
			} else {
				backlinks = this.getSeparation3GrowGraph(backlinksFetcher);
				
				if (backlinks.isEmpty()) return false;
			}
			
			// Check if there is some element in common between the links and
			// backlinks. If so, then we found a path! Otherwise, we must
			// repeat the loop though the number of degrees has now increased.
			ArrayList<String> common = new ArrayList<String>(this.links);
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
	private ArrayList<String> getSeparation3GrowGraph(
			AbstractLinkFetcher fetcher) throws IOException {
		List<String> newLinks = Collections.synchronizedList(
				new ArrayList<String>());
		
		// If the parameter was a links fetcher then we need to build the
		// graph from the starting node. Otherwise, set up the parameters to
		// build the graph from the ending node.
		boolean isStartSide = fetcher == Separation.linksFetcher;
		List<String> thisSide = isStartSide ? this.links : this.backlinks;
		List<String> otherSide = isStartSide ? this.backlinks : this.links;
		ConcurrentHashMap<String, String> map = isStartSide ? 
				this.predecessors : this.successors;

		final int NUM_PER_THREAD = 50;
		int numThreads = 1 + (thisSide.size() / NUM_PER_THREAD);
		
		ArrayList<Thread> threads = new ArrayList<Thread>();
		ThreadedGraphGrower.isDone = false;
		
		for (int i = 1; i <= numThreads; ++i) {
			int fromIndex = (i - 1) * NUM_PER_THREAD;
			int toIndex = fromIndex + NUM_PER_THREAD;
			
			if (i == numThreads) toIndex = thisSide.size();
			if (toIndex >= thisSide.size()) toIndex = thisSide.size();
			
			List<String> task = thisSide.subList(fromIndex, toIndex);
			
			Thread tgg = new Thread(new ThreadedGraphGrower(
					newLinks, task, fetcher, otherSide, map));
			threads.add(tgg);
			tgg.start();
		}
		
		for (Thread t : threads) {
			try {
				t.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} 
		}
		
		return new ArrayList<String>(newLinks);
	}
}
