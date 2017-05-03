package com.antverdovsky.wikideg;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;

public class Separation {
	int numDegrees;          // Degrees of Separation
	Stack<String> path;      // The path from start to end
	
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
		// Create a Stack with the starting article which will be used to
		// store the path, and zero degrees of separation.
		int degrees = 0;
		Stack<String> path = new Stack<String>();
		path.push(start);
		
		// Special case if [start] == [end] -> Return 0 degrees of separation
		// and a path containing the starting article.
		if (start.equals(end)) return new Separation(degrees, path);
		
		++degrees; // If above failed, we are at >= 1 degree of separation
		
		// Fetch the starting list of links (note, we could also fetch the end
		// backlinks here, however, the start links are usually shorter than
		// the backlinks).
		HashSet<String> startLinks = WikiAPI.getAllLinks(start, end);
		
		// If the links fetched contain the the end article, then we have a 
		// special case where [start] -> [end], so we have one degree of
		// separation, with a direct path.
		if (startLinks.contains(end)) {
			path.push(end);
			return new Separation(degrees, path);
		}
		
		++degrees; // If above failed, we are at >= 2 degrees of separation
		
		// Otherwise, fetch the ending list of backlinks. 
		HashSet<String> endLinks = WikiAPI.getAllBacklinks(end, start);
		
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
}
