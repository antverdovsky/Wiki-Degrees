package com.antverdovsky.wikideg;

import java.io.IOException;
import java.util.ArrayList;

import com.antverdovsky.wikideg.WikiParse.LinksResult;

public class Main {
	public static void main(String[] args) {
		ArrayList<ArrayList<String>> boLinks = 
				new ArrayList<ArrayList<String>>();
		ArrayList<ArrayList<String>> boBacklinks = 
				new ArrayList<ArrayList<String>>();
		
		double a = System.currentTimeMillis();
		try {
			 boLinks = WikiAPI.getAllLinks("Barack Obama");
			 boBacklinks = WikiAPI.getAllBacklinks("Barack Obama");
		} catch (IOException e) {
			e.printStackTrace();
		}
		double b = System.currentTimeMillis();
		
		int totalBoLinks = 0;
		int totalBoBacklinks = 0;
		
		for (ArrayList<String> listN : boLinks) {
			System.out.println("List Size: " + listN.size());
			
			for (String s : listN) {
				totalBoLinks++;
				System.out.println("\t" + s);
			}
			
			System.out.println("\n");
		}
		
		for (ArrayList<String> listN : boBacklinks) {
			System.out.println("List Size: " + listN.size());
			
			for (String s : listN) {
				totalBoBacklinks++;
				System.out.println("\t" + s);
			}
			
			System.out.println("\n");
		}
		
		System.out.println("Total Links: " + totalBoLinks);
		System.out.println("Total Backlinks: " + totalBoBacklinks);
		System.out.println("Time taken: " + (b - a));
	}
}
