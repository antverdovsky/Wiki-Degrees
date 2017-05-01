package com.antverdovsky.wikideg;

import java.io.IOException;
import java.util.ArrayList;

import com.antverdovsky.wikideg.WikiParse.LinksResult;

public class Main {
	public static void main(String[] args) {
		ArrayList<ArrayList<String>> boLinks = 
				new ArrayList<ArrayList<String>>();
		
		try {
			 boLinks = WikiAPI.getAllLinks("Barack Obama");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		for (ArrayList<String> listN : boLinks) {
			System.out.println("List Size: " + listN.size());
			
			for (String s : listN) {
				System.out.println("\t" + s);
			}
			
			System.out.println("\n");
		}
	}
}
