package com.antverdovsky.wikideg;

import java.io.IOException;

public class Main {
	public static void main(String[] args) {
		String boLinkForward = WikiFetch.getLinksURL("Barack Obama");
		String boLinkBack = WikiFetch.getBacklinksURL("Barack Obama");
		
		String boData = "empty";
		try {
			 boData = WikiFetch.getData(boLinkForward);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println(boLinkForward);
		System.out.println(boLinkBack);
		System.out.println(boData);
	}
}
