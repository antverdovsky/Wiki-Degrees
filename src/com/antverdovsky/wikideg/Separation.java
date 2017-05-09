package com.antverdovsky.wikideg;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

class ThreadedGraphGrower implements Runnable {
	// Flag indicating whether all instances should perform their task. This
	// will be set to true by any instance of this class once that instance
	// fetches a target link.
	static volatile boolean isDone = false;
	
	private List<String> writeTo;   // Where to add fetched links (concurrent)
	private List<String> task;      // List for which links are to be fetched
	private List<String> targets;   // The graph's other side nodes
	
	private ConcurrentHashMap<String, String> map; // Predecessor/Successor 
	private AbstractLinkFetcher linkFetcher;       // Fetcher to be used
	
	/**
	 * Creates a new thread graph grower instance.
	 * @param writeTo The concurrent list into which the graph grower is to
	 *                write all of the links it fetches.
	 * @param task The list of links for which we are going to fetch their
	 *             links.
	 * @param linkFetcher The Link Fetcher to be used when fetching all of the
	 *                    links.
	 * @param targets The other links list. If any fetched link is contained
	 *                in this list as well, the isDone flag will be set to
	 *                true since a common node has been found.
	 * @param map The predecessor or successor hash map.
	 */
	public ThreadedGraphGrower(List<String> writeTo, List<String> task, 
			AbstractLinkFetcher linkFetcher, List<String> targets,
			ConcurrentHashMap<String, String> map) {
		this.writeTo = writeTo;
		this.task = task;
		this.linkFetcher = linkFetcher;
		this.targets = targets;
		this.map = map;
	}
	
	/**
	 * Continually fetches the links of each link in the task list. As each
	 * new link is fetched, its predecessor or successor is added to the
	 * predecessor or successor hash map and the link is added to the
	 * concurrent write to list. If any of the links fetched are contained in
	 * the targets list, this method is halted for all instances of this.
	 */
	public void run() {
		Iterator<String> it = this.task.iterator();
		
		// While the isDone flag is not set to true and we have more elements
		// in the task list that need processing.
		while (!ThreadedGraphGrower.isDone && it.hasNext()) {
			String link = it.next(); // Fetch the next link from the task list
			
			// Get the (back)links of the link fetched 
			ArrayList<String> linksOf = new ArrayList<String>();
			try { linksOf = linkFetcher.getLinks(link, ""); } 
			catch (IOException e) { e.printStackTrace(); }

			// For each link fetched, add it to the predecessor/successor map
			// and write it to the write to list.
			for (String linkOf : linksOf) {
				this.map.putIfAbsent(linkOf.toLowerCase(), link);
				this.writeTo.add(linkOf);
			}

			// Check if any elements of targets were found. If so, halt 
			// execution for every instance of this class.
			List<String> common = Utilities.retainAllIgnoreCase(
					linksOf, this.targets);
			if (!common.isEmpty()) ThreadedGraphGrower.isDone = true;
		}
	}
}

public class Separation {
	// Fetchers for Links and Backlinks.
	private static ExportLinksFetcher linksFetcher = new ExportLinksFetcher();
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
	 * Computes the embedded path for this Separation instance. This should
	 * only be called once the standard path has been computed. An embedded
	 * path will contain the names of each node in the specified path as it is
	 * present in the predecessor node, since Wikipedia sometimes uses a 
	 * different article name when it is embedded in an article.
	 * @throws IOException If there is an error fetching the export for any
	 *                     of the articles in the path.
	 */
	public void computeEmbeddedPath() throws IOException {
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
			this.embeddedPath.push(WikiParse.parseEmbeddedArticle(data, next));
		}
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
		return this.startArticle.equalsIgnoreCase(this.endArticle);
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
		if (Utilities.containsIgnoreCase(this.links, this.endArticle)) {
			this.path.push(this.endArticle);
			
			this.computeEmbeddedPath();
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
		this.backlinks =  backlinksFetcher.getLinks(this.endArticle, 
				this.startArticle);
		
		// Find any articles that backlinks has in common with the links. This
		// implies that there exists some middle article such that we can go
		// from start -> middle -> end.
		List<String> common = Utilities.retainAllIgnoreCase(this.backlinks,
				this.links);
		
		// If no middle articles exist, return false
		if (common.isEmpty()) return false;
		
		// Fetch some random article from the common set and build a path with
		// it, returning a separation of two degrees.
		String middle = common.iterator().next();
		this.path.push(middle);
		this.path.push(this.endArticle);
		
		this.computeEmbeddedPath();
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
		for (String s : this.links) predecessors.put(s.toLowerCase(),
				this.startArticle);
		for (String s : this.backlinks) successors.put(s.toLowerCase(),
				this.endArticle);
		
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
			List<String> common = Utilities.retainAllIgnoreCase(
					this.links, this.backlinks);
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
				while (!currentPredecessor.equalsIgnoreCase(
						this.startArticle)) {
					// Get the predecessor of the current predecessor and add
					// it to the backtrace queue.
					currentPredecessor = predecessors.get(
							currentPredecessor.toLowerCase());
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
				while (!currentSuccessor.equalsIgnoreCase(this.endArticle)) {
					this.path.push(currentSuccessor);
					currentSuccessor = successors.get(
							currentSuccessor.toLowerCase());
				}
				this.path.push(this.endArticle);
				
				this.computeEmbeddedPath();
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
		// Create a new thread safe list into which each thread will write its
		// fetched links.
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

		// Since the biggest obstacle to performance for fetching links is
		// I/O (download speed), we can have a large number of threads here.
		final int MAX_NUM_THREADS = 128;     // Max Number of Threads at Once
		final int MIN_TO_MULTITHREAD = 32;   // Min list size to multithread
		final int MAX_IDEAL_PER_THREAD = 32; // Ideal task size per thread
		
		// By default, we use one thread with the whole list being processed
		// by the sole thread.
		int numThreads = 1;
		int numPerThread = thisSide.size();
		
		// If the list size is big enough to use multithreading
		if (thisSide.size() >= MIN_TO_MULTITHREAD) {
			// Calculate the number of threads to ideally split up the task
			numThreads = thisSide.size() / MAX_IDEAL_PER_THREAD + 1;
			
			// If the ideal will result in too many threads, limit the number
			// of threads to the maximum constant.
			if (numThreads > MAX_NUM_THREADS) 
				numThreads = MAX_NUM_THREADS;
			
			// Calculate the number of elements in the task per thread
			numPerThread = thisSide.size() / MAX_NUM_THREADS;
		}
		
		// Create a list of threads which will be used. Be sure to reset the
		// isDone flag every time we want to reuse the threads!
		ArrayList<Thread> threads = new ArrayList<Thread>();
		ThreadedGraphGrower.isDone = false;
		
		for (int i = 1; i <= numThreads; ++i) {
			// Get the indices of elements in the links list so that we may
			// evenly partition the links elements amongst all the threads.
			int fromIndex = (i - 1) * numPerThread;
			int toIndex = fromIndex + numPerThread;
			
			// Just In Case, limit the toIndex to the side of the links list.
			// Also, if this is the last thread, make sure it encapsulates the
			// entire remaining partition of the list.
			if (i == numThreads) toIndex = thisSide.size();
			if (toIndex >= thisSide.size()) toIndex = thisSide.size();
			
			// Create the partition of the list
			List<String> task = thisSide.subList(fromIndex, toIndex);
			
			// Create a new Thread with the partition as its assignment, add
			// it to the list and start its execution.
			Thread tgg = new Thread(new ThreadedGraphGrower(
					newLinks, task, fetcher, otherSide, map));
			threads.add(tgg);
			tgg.start();
		}
		
		// Wait for every single thread to fetch its data...
		for (Thread t : threads) {
			try { t.join(); } 
			catch (InterruptedException e) { e.printStackTrace(); } 
		}
		
		return new ArrayList<String>(newLinks); // Finished
	}
}
