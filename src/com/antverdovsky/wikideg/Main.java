package com.antverdovsky.wikideg;

import java.io.IOException;
import java.util.Scanner;
import java.util.Stack;

import com.antverdovsky.wikideg.sep.Separation;
import com.antverdovsky.wikideg.util.Utilities;

/**
 * Main Class responsible for taking input and getting the separation from the
 * Separation class.
 */
public class Main {
	/**
	 * Main execution method.
	 * @param args The program arguments.
	 */
	public static void main(String[] args) {
		// Scanner for reading from stdin
		Scanner scanner = new Scanner(System.in);
		
		// Stores the titles of the starting and ending articles
		String start = "";
		String end = "";
		
		// Get the starting and ending article names
		System.out.print("Enter starting article name: ");
		if (scanner.hasNextLine()) start = scanner.nextLine();
		System.out.print("Enter ending article name: ");
		if (scanner.hasNextLine()) end = scanner.nextLine();
		scanner.close();
		
		// If the user used the "%r" title for either article, fetch a random
		// article to replace it.
		try {
			if (start.equals("%r")) start = Utilities.getRandomArticle();
			if (end.equals("%r")) end = Utilities.getRandomArticle();
		} catch (IOException e) {
			System.out.println("Unknown exception occured.");
			return;
		}

		// Print the starting and ending article titles
		System.out.println("Searching for path between " + 
				start + " and " + end);
		Separation separation = null;
		
		// Try to find the path between the start and end
		try { 
			separation = new Separation(start, end); 
		} catch (IOException e) {
			System.out.println("Unknown exception occured.");
			return;
		}
		
		if (separation == null || !separation.getPathExists()) {
			System.out.println("Unable to find a path from " + 
					start + " to " + end);
		} else {
			// Print out the degrees of separation between start and end
			System.out.println("Degrees of Separation: " + 
					separation.getNumDegrees());
			System.out.println("Path: ");
			
			Stack<String> path = separation.getPath();
			Stack<String> embedded = separation.getEmbeddedPath();
			for (int i = 0; i < path.size(); ++i) {
				// Get the current node in the standard and the embedded path
				String current = path.get(i);
				String currentEmbedded = (i > 0) ? embedded.get(i - 1) : current;
				
				// Print out the current node. If the embedded path node is
				// different from the standard path node, then also print out
				// the embedded path node.
				System.out.print("\t" + current);
				if (!current.equalsIgnoreCase(currentEmbedded))
					System.out.print(" [" + currentEmbedded + "]");
				
				// Print the arrow if this is not the last element of the path
				if (i < path.size() - 1) System.out.println(" -> ");
				else System.out.println("");
			}
		}
	}
}
