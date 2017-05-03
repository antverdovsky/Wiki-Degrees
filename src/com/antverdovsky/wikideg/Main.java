package com.antverdovsky.wikideg;

import java.io.IOException;
import java.util.ArrayList;
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
		
		// main loop TODO
		
		return new Separation(0, new Stack<String>());
	}

	public static void main(String[] args) {

		try {
			Separation s1 = findConnection("Apple", "Orange (fruit)");
			Separation s2 = findConnection("Barack Obama", "Donald Trump");
			
			System.out.println(s1.getNumDegrees());
			System.out.println(s1.getPath());
			
			System.out.println(s2.getNumDegrees());
			System.out.println(s2.getPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
