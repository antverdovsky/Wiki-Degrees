package com.antverdovsky.wikideg;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

public class Main {
	private static class Separation {
		int numDegrees;          // Degrees of Separation
		Stack<String> path;      // The path from start to end
		
		/**
		 * Creates a new instance of the Separation class with the specified
		 * number of degrees of separation and the specified path.
		 * @param numD The number of degrees of separation.
		 * @param path The path from the starting link to the ending link.
		 */
		public Separation(int numD, Stack<String> path) {
			this.numDegrees = numD;
			this.path = path;
		}
		
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
	}

	/**
	 * Finds the degrees of separation and path between the starting and 
	 * ending articles.
	 * @param start The starting article title.
	 * @param end The ending article title.
	 * @throws IOException If there occurs an error fetching the articles of
	 *                     either the starting or ending article. 
	 */
	private static Separation findConnection(String start, String end) 
			throws IOException {
		// Empty Stack to store the path between start and end
		Stack<String> path = new Stack<String>();
		path.push(start);
		
		// Special case if start == end -> Return 0 degrees of separation
		// and a path containing the starting article.
		if (start.equals(end)) return new Separation(0, path);
		
		// Fetch the starting and ending links
		ArrayList<String> startLinks = WikiAPI.getAllLinks(start);
		ArrayList<String> endLinks = WikiAPI.getAllBacklinks(end);
		
		// Special case if the end article is in the links of the starting
		// article, or the start article is in the backlinks of the end 
		// article (start -> end).
		if (startLinks.contains(end)) {
			path.push(end);
			return new Separation(1, path);
		}
		
		// Special case if there is some article that is in both the starting
		// links and the ending backlinks (start -> middle -> end).
		ArrayList<String> common = new ArrayList<String>(startLinks);
		common.retainAll(endLinks);
		if (common.size() > 0) {
			// TODO: Return all elements, just one?
			path.push(common.get(0));
			path.push(end);
			
			return new Separation(2, path);
		}
		
		// The main loop. If we have reached this far, we know we have >= 3
		// degrees of separation so we must try to find some midpoint between
		// the starting and ending articles.
		int degSeparation = 2;
		HashMap<String, String> predecessors = new HashMap<String, String>();
		HashMap<String, String> successors = new HashMap<String, String>();
		for (String s : startLinks) predecessors.put(s, start);
		for (String s : endLinks) successors.put(s, end);
		while (true) {
			// For each iteration, the degrees of separation increases by one
			// since the graph's Final Frontier nodes get additional adjacent 
			// neighbors.
			++degSeparation;
			
			// If we have less links from the starting article, lets build
			// the graph from the starting article.
			if (startLinks.size() < endLinks.size()) {
				System.out.println(startLinks.size());
				
				// Stores the new start links
				ArrayList<String> newStartLinks = new ArrayList<String>();
				
				// Go through all of the start links and fetch their links.
				// Copy all of their links into the new start links.
				for (String s : startLinks) {
					// TODO: Optimize so we don't always have to copy over...
					ArrayList<String> sStartLinks = WikiAPI.getAllLinks(s);
					newStartLinks.addAll(sStartLinks);
					
					// Set the predecessor of all of the start links
					for (String sS : sStartLinks) {
						if (!predecessors.containsKey(sS)) predecessors.put(sS, s);
					}
				}
				
				// Discard the old start links, replacing them with the new 
				// ones.
				startLinks = newStartLinks;
			} 
			// If we have less links from the ending article, lets build the
			// graph from the ending article.
			else {			// TODO: Some code duplication going on here...
				System.out.println(endLinks.size());
				
				// Stores the new backlinks
				ArrayList<String> newEndLinks = new ArrayList<String>();
				
				// Go through all of the backlinks and fetch their backlinks.
				// Copy all of their backlinks into the new backlinks links.
				for (String s : endLinks) {
					// TODO: Optimize so we don't always have to copy over...
					ArrayList<String> sEndLinks = WikiAPI.getAllBacklinks(s);
					newEndLinks.addAll(sEndLinks);
					
					// Set the successor of all of the start links
					for (String sS : sEndLinks) {
						if (!successors.containsKey(sS)) successors.put(sS, s);
					}
				}
				
				// Discard the old end links, replacing them with the new 
				// ones.
				endLinks = newEndLinks;
			}
			
			// Check if there is some element in common between the links and
			// backlinks list. If there is, we have found the middle!
			common = new ArrayList<String>(startLinks);
			common.retainAll(endLinks);
			if (common.size() > 0) {
				// TODO: Return all elements, just one?
				// Get the middle element of the Graph
				String middle = common.get(0);
				
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
					midPred.add(curPred);
				}
				
				// Get all of the successors until the starting element
				// is reached.
				while (!curSucc.equals(end)) {
					curSucc = successors.get(curSucc);
					midSucc.add(curSucc);
				}
				
				// Form the link start -> { middle predecessors } -> middle ->
				// { middle successors } -> end.
				for (String s : midPred) {
					path.push(s);
				}
				for (String s : midSucc) {
					path.push(s);
				}
				
				return new Separation(degSeparation, path);
			}
		}
	}

	public static void main(String[] args) {

		try {
/*
			Separation s0 = findConnection("Fruit", "Fruit");
			System.out.println(s0.getNumDegrees());
			System.out.println(s0.getPath());
			
			Separation s1 = findConnection("Barack Obama", "Donald Trump");
			System.out.println(s1.getNumDegrees());
			System.out.println(s1.getPath());

			Separation s2 = findConnection("Apple", "Orange (fruit)");
			System.out.println(s2.getNumDegrees());
			System.out.println(s2.getPath());
*/
			Separation s3 = findConnection("Lingua Franca Nova", "United Airlines");
			System.out.println(s3.getNumDegrees());
			System.out.println(s3.getPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
