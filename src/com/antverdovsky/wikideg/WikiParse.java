package com.antverdovsky.wikideg;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class WikiParse {
	/**
	 * Parses the specified backlinks JSON data. All of the links fetched from
	 * the JSON data are then added to the backlinks reference parameter. If
	 * the JSON data contains a continue token, it is returned as a String,
	 * otherwise an empty String is returned instead.
	 * @param json The JSON data which is to be parsed.
	 * @param backlinks The backlinks list into which the JSON data is to be
	 *                  appended. 
	 * @return The continue token of the JSON data, or an empty String if the
	 *         JSON data contains no continue token.
	 */
	public static String parseBacklinks(String json, 
			ArrayList<String> backlinks) {
		// Create a JSON Parser using GSON and parse the root of the JSON data
		JsonParser jParser = new JsonParser();
		JsonElement root = jParser.parse(json);
		
		// Navigate Root -> Query -> Backlinks
		JsonElement jQuery = root.getAsJsonObject().get("query");
		JsonElement jBacklinks = jQuery.getAsJsonObject().get("backlinks");
		JsonArray jBacklinksArray = jBacklinks.getAsJsonArray();
		
		// Move all of the titles from the JsonArray into the List
		for (JsonElement e : jBacklinksArray) { // Foreach link
			JsonObject jLinkObj = e.getAsJsonObject();
			
			String title = jLinkObj.get("title").getAsString();
			backlinks.add(title);
		}
		
		// Navigate Root -> Continue. If unable, then there is no continue
		// token, so make the continue token empty.
		String blCont;
		JsonElement cont = root.getAsJsonObject().get("continue");
		if (cont != null) {
			// Fetch the blContinue entry. Strip the quotation marks from the 
			// continue entry.
			blCont = cont.getAsJsonObject().get("blcontinue").toString();
			blCont = blCont.replaceAll("\"", "");
		} else {
			blCont = "";
		}
		
		return blCont;
	}
	
	/**
	 * Parses the specified links JSON data. All of the links fetched from
	 * the JSON data are then added to the links reference parameter. If
	 * the JSON data contains a continue token, it is returned as a String,
	 * otherwise an empty String is returned instead.
	 * @param json The JSON data which is to be parsed.
	 * @param links The links list into which the JSON data is to be appended. 
	 * @return The continue token of the JSON data, or an empty String if the
	 *         JSON data contains no continue token.
	 */
	public static String parseLinks(String json, ArrayList<String> links) {
		// Create a JSON Parser using GSON and parse the root of the JSON data
		JsonParser jParser = new JsonParser();
		JsonElement root = jParser.parse(json);
		
		// Navigate Root -> Query -> Pages
		JsonElement jQuery = root.getAsJsonObject().get("query");
		JsonElement jPages = jQuery.getAsJsonObject().get("pages");
		
		// Get all of the Entries in the JSON file. Since we are only parsing
		// for one web article at a time, assert that there is only one entry.
		JsonObject pagesObj = jPages.getAsJsonObject();
		Set<Entry<String, JsonElement>> eSet = pagesObj.entrySet();
		if (eSet.size() != 1) return null;
		
		// Fetch the single entry from the entry set
		JsonElement entry = eSet.iterator().next().getValue();
		
		// Navigate Root -> Query -> Pages -> Entry -> Links
		JsonElement jLinks = entry.getAsJsonObject().get("links");
		JsonArray jLinksArray = jLinks.getAsJsonArray();
		
		// Move all of the titles from the JsonArray into the List 
		for (JsonElement e : jLinksArray) {
			JsonObject jLinkObj = e.getAsJsonObject();
			
			String title = jLinkObj.get("title").getAsString();
			links.add(title);
		}
		
		// Navigate Root -> Continue. If unable, then there is no continue
		// token, so make the continue token empty.
		String plCont;
		JsonElement cont = root.getAsJsonObject().get("continue");
		if (cont != null) {
			// Fetch the plContinue entry. Strip the quotation marks from the 
			// continue entry.
			plCont = cont.getAsJsonObject().get("plcontinue").toString();
			plCont = plCont.replaceAll("\"", "");
		} else {
			plCont = "";
		}
		
		return plCont;
	}
}
