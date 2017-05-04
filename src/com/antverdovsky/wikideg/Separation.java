package com.antverdovsky.wikideg;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;

public class Separation {
	private int numDegrees;          // Degrees of Separation
	private Stack<String> path;      // The path from start to end
	
	/**
	 * Returns the number of degrees of separation between two articles.
	 * @return The degrees of separation.
	 */
	public int getNumDegrees() {
		return this.numDegrees;
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
	 *         the path from the starting article to the ending article.
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
		
		// Check if there is a two degree separation between start and end.
		// If so, return it! If not, we will get the list of all of the 
		// backlinks.
		HashSet<String> backlinks = new HashSet<String>();
		separation = Separation.getSeparation2(start, end, links, backlinks);
		if (separation != null) return separation;
		
		return null;
		
/*			TODO WIP
		// Special case if there is some article that is in both the starting
		// links and the ending backlinks ([start] -> [middle] -> [end]), so
		// we have two degrees of separation with a one hop chain.
		HashSet<String> common = new HashSet<String>(startLinks);
		common.retainAll(endLinks);
		if (!common.isEmpty()) {
			// Here, we can grab any element we want, for simplicity lets grab
			// the first element using the Iterator.
			path.push(common.iterator().next());
			path.push(end);
			
			return new Separation(degrees, path);
		}
		
		++degrees; // If above failed, we are at >= 3 degrees of separation
		
		// As we fetch links to find the midpoint, we are going to need to
		// store the predecessor for each link, or successor for each 
		// backlink.
		HashMap<String, String> predecessors = new HashMap<String, String>();
		HashMap<String, String> successors = new HashMap<String, String>();
		
		// Add each starting link and ending backlink to the predecessors hash 
		// and successors hash, respectively.
		for (String s : startLinks) predecessors.put(s, start);
		for (String s : endLinks) successors.put(s, end);
		
		// The main loop which will run until a middle node is found
		while (true) {
			// If we have less links from the starting article, lets build
			// the graph from the starting article.
			if (startLinks.size() < endLinks.size()) {
				// Stores the new start links
				HashSet<String> newStartLinks = new HashSet<String>();
				
				// Go through all of the start links and fetch their links.
				for (String s : startLinks) {
					// TODO: Optimize so we don't always have to copy over...
					HashSet<String> sStartLinks = WikiAPI.getAllLinks(s, end);
					newStartLinks.addAll(sStartLinks);
					
					// Set the predecessor of all of the start links
					for (String sS : sStartLinks) {
						if (!predecessors.containsKey(sS)) predecessors.put(sS, s);
					}
					
					// If the links we just fetched contain something in
					// common with the end backlinks, then we found a middle!
					common = new HashSet<String>(sStartLinks);
					common.retainAll(endLinks);
					if (!common.isEmpty()) break;
				}
				
				// Discard the old start links, replacing them with the new 
				// ones.
				startLinks = newStartLinks;
			} 
			// If we have less links from the ending article, lets build the
			// graph from the ending article.
			else {			// TODO: Some code duplication going on here...
				// Stores the new backlinks
				HashSet<String> newEndLinks = new HashSet<String>();
				
				// Go through all of the backlinks and fetch their backlinks.
				// Copy all of their backlinks into the new backlinks links.
				for (String s : endLinks) {
					// TODO: Optimize so we don't always have to copy over...
					HashSet<String> sEndLinks = WikiAPI.getAllBacklinks(s, start);
					newEndLinks.addAll(sEndLinks);
					
					// Set the successor of all of the start links
					for (String sS : sEndLinks) {
						if (!successors.containsKey(sS)) successors.put(sS, s);
					}
					
					// If the backlinks we just fetched contain something in
					// common with the start links, then we found a middle!
					common = new HashSet<String>(sEndLinks);
					common.retainAll(startLinks);
					if (!common.isEmpty()) break;
				}
				
				// Discard the old end links, replacing them with the new 
				// ones.
				endLinks = newEndLinks;
			}
			
			// Check if there is some element in common between the links and
			// backlinks list. If there is, we have found the middle!
			common = new HashSet<String>(startLinks);
			common.retainAll(endLinks);
			if (common.size() > 0) {
				// TODO: Return all elements, just one?
				// Get the middle element of the Graph
				String middle = common.iterator().next();
				
				// Will store the predecessors and successors of the middle
				// node element.
				String curPred = middle;
				String curSucc = middle;
				ArrayList<String> midPred = new ArrayList<String>();
				ArrayList<String> midSucc = new ArrayList<String>();
				
				// Get all of the predecessors until the starting element
				// is reached.
				while (!curPred.equals(start)) {
					curPred = predecessors.get(curPred);
					if (!curPred.equals(start)) midPred.add(curPred);
				}
				
				// Get all of the successors until the starting element
				// is reached.
				while (!curSucc.equals(end)) {
					curSucc = successors.get(curSucc);
					if (!curSucc.equals(end)) midSucc.add(curSucc);
				}
				
				// Form the link start -> { middle predecessors } -> middle ->
				// { middle successors } -> end.
				for (String s : midPred) {
					path.push(s);
				}
				path.push(middle);
				for (String s : midSucc) {
					path.push(s);
				}
				path.push(end);
				
				return new Separation(degrees, path);
			}
			
			++degrees; // For each iteration, the separation increases
		}
*/
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
		links.addAll(WikiAPI.getAllLinks(start, end));
		
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
	 * @param links The backlinks of the ending article. This hash set should 
	 *              be empty and will be filled using this method.
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
		HashSet<String> common = WikiAPI.getAllBacklinks(end, start);
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
}
