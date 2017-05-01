package com.antverdovsky.wikideg;

import java.io.IOException;
import java.util.ArrayList;

import com.antverdovsky.wikideg.WikiParse.LinksResult;

public class Main {
	public static void main(String[] args) {
		String boLinkForward = WikiFetch.getLinksURL("Barack Obama", "534366|0|Elie_Wiesel");
		String boLinkBack = WikiFetch.getBacklinksURL("Barack Obama", "0|49260");
		
		String boLData = "empty";
		String boBLData = "empty";
		try {
			 boLData = WikiFetch.getData(boLinkForward);
			 boBLData = WikiFetch.getData(boLinkBack);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		LinksResult boLinksResult = WikiParse.parseLinks(boLData);
		ArrayList<String> boLinks = boLinksResult.getLinks();
		String boPlContinue = boLinksResult.getLContinue();

		System.out.println("LINKS");
		System.out.println("PL CONTINUE: " + boPlContinue);
		for (String s : boLinks) {
			System.out.println(s);
		}
		
		LinksResult boBacklinksResult = WikiParse.parseBacklinks(boBLData);
		ArrayList<String> boBacklinks = boBacklinksResult.getLinks();
		String boBlContinue = boBacklinksResult.getLContinue();
		
		System.out.println("BACKLINKS");
		System.out.println("BL CONTINUE: " + boBlContinue);
		for (String s : boBacklinks) {
			System.out.println(s);
		}
	}
}
