package com.antverdovsky.wikideg;

import java.io.IOException;
import java.util.Scanner;
import java.util.Stack;

import com.antverdovsky.wikideg.sep.Separation;
import com.antverdovsky.wikideg.util.Logger;
import com.antverdovsky.wikideg.util.Utilities;

/**
 * Main Class responsible for taking input and getting the separation from the
 * Separation class.
 */
public class Main {
	// Command line arguments booleans
	private static boolean doDisplayTimeArg = false;
	private static boolean doDisplayDebugArg = false;
	private static boolean doDisplayHelp = false;
	
	/**
	 * Prints out the help information.
	 */
	private static void printHelp() {
		System.out.println("Command Line Arguments: ");
		System.out.println("\t-h : Prints out these help instructions.");
		System.out.println("\t-t : Prints out time taken to compute a path");
		System.out.println("\t-d : Prints out debug information");
		
		System.out.println("\nUsage: ");
		System.out.println("\tEnter any two Wikipedia article titles or " +
				"use \"%r\" to fetch a random Wikipedia article title.");
		System.out.println("\tOnce the path is computed, each title in the " +
				"path will be outputted. Sometimes article titles are ");
		System.out.println("\tembedded under different names in the " +
				"article, in which case the title of the article as it");
		System.out.println("\tappears in an article will be displayed and " +
				"surrounded by brackets. Sometimes, paths cannot be ");
		System.out.println("\tcomputed due to the starting article having " +
				"no links in it, or the ending article having no links");
		System.out.println("\twhich lead to it. Use the debug mode to find " +
				"why a path could not be calculated.");
		
		System.out.println("\n\n");
	}
	
	/**
	 * Main execution method.
	 * @param args The program arguments.
	 */
	public static void main(String[] args) {
		// Check for command line arguments
		for (String s : args) {
			if (s.equalsIgnoreCase("-d")) Main.doDisplayDebugArg = true;
			if (s.equalsIgnoreCase("-t")) Main.doDisplayTimeArg = true;
			if (s.equalsIgnoreCase("-h")) Main.doDisplayHelp = true;
		}
		
		// Print out the help, if applicable
		if (Main.doDisplayHelp) Main.printHelp();
		
		// Initialize the Debug Logger
		Logger.setIsEnabled(Main.doDisplayDebugArg);
		
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
		System.out.println("Searching for path between \"" + 
				start + "\" and \"" + end + "\"");
		Separation separation = null;
		
		// Try to find the path between the start and end
		double startTime = System.currentTimeMillis(); 
		try { 
			separation = new Separation(start, end); 
		} catch (IOException e) {
			System.out.println("Unknown exception occured.");
			return;
		}
		double endTime = System.currentTimeMillis();
		
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
		
		// Print out the time taken, if applicable
		int deltaTime = (int)(endTime - startTime);
		if (Main.doDisplayTimeArg)
			System.out.println("Time taken: " + deltaTime + "ms.");
	}
}
