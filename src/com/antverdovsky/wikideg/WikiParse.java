package com.antverdovsky.wikideg;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class WikiParse {
	static class LinksResult {
		private ArrayList<String> links;    // List of all links in JSON data
		private String lContinue;           // Continue entry in JSON data
		
		/**
		 * Initializes a new Links Result instance with the specified links
		 * and continue token.
		 * @param links The Links.
		 * @param cont The continue token.
		 */
		public LinksResult(ArrayList<String> links, String cont) {
			this.links = links;
			this.lContinue = cont;
		}
		
		/**
		 * Returns the Links of this LinkResult.
		 * @return The Links List.
		 */
		public ArrayList<String> getLinks() {
			return this.links;
		}
		
		/**
		 * Returns the Continue Token of this LinkResult.
		 * @return The lContinue Token.
		 */
		public String getLContinue() {
			return this.lContinue;
		}
	}
	
	/**
	 * Parses the specified JSON data and returns an instance of the 
	 * LinksResult class containing the list of all the backlinks in the JSON
	 * data, as well as the continue token, if applicable. If the JSON file
	 * has no continue entry, the continue token will be empty. If the JSON
	 * data is invalid, null is returned.
	 * @param json The JSON data which is to be parsed.
	 * @return The LinksResult instance containing the links and the continue
	 *         token, or null if the JSON data is invalid.
	 */
	public static LinksResult parseBacklinks(String json) {
		// Create an empty ArrayList to store the Links with a capacity of 500
		// links (maximum from one JSON data file).
		ArrayList<String> backlinksList = new ArrayList<String>(500);
		
		// Create a JSON Parser using GSON and parse the root of the JSON data
		JsonParser jParser = new JsonParser();
		JsonElement root = jParser.parse(json);
		
		// Navigate Root -> Query -> Backlinks
		JsonElement query = root.getAsJsonObject().get("query");
		JsonElement backlinks = query.getAsJsonObject().get("backlinks");
		JsonArray backlinksArray = backlinks.getAsJsonArray();
		
		// Move all of the titles from the JsonArray into the List
		for (JsonElement e : backlinksArray) { // Foreach link
			JsonObject linkObj = e.getAsJsonObject();
			
			String title = linkObj.get("title").getAsString();
			backlinksList.add(title);
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
		
		// Construct the Result and return
		LinksResult result = new LinksResult(backlinksList, blCont);
		return result;
	}
	
	/**
	 * Parses the specified JSON data and returns an instance of the 
	 * LinksResult class containing the list of all of the links in the JSON
	 * data, as well as the continue token, if applicable. If the JSON file
	 * has no continue entry, the continue token will be empty. If the JSON
	 * data is invalid, null is returned.
	 * @param json The JSON data which is to be parsed.
	 * @return The LinksResult instance containing the links and the continue
	 *         token, or null if the JSON data is invalid.
	 */
	public static LinksResult parseLinks(String json) {
		// Create an empty ArrayList to store the Links with a capacity of 500
		// links (maximum from one JSON data file).
		ArrayList<String> linksList = new ArrayList<String>(500);
		
		// Create a JSON Parser using GSON and parse the root of the JSON data
		JsonParser jParser = new JsonParser();
		JsonElement root = jParser.parse(json);
		
		// Navigate Root -> Query -> Pages
		JsonElement query = root.getAsJsonObject().get("query");
		JsonElement pages = query.getAsJsonObject().get("pages");
		
		// Get all of the Entries in the JSON file. Since we are only parsing
		// for one web article at a time, assert that there is only one entry.
		JsonObject pagesObj = pages.getAsJsonObject();
		Set<Entry<String, JsonElement>> eSet = pagesObj.entrySet();
		if (eSet.size() != 1) return null;
		
		// Fetch the single entry from the entry set
		JsonElement entry = eSet.iterator().next().getValue();
		
		// Navigate Root -> Query -> Pages -> Entry -> Links
		JsonElement links = entry.getAsJsonObject().get("links");
		JsonArray linksArray = links.getAsJsonArray();
		
		// Move all of the titles from the JsonArray into the List 
		for (JsonElement e : linksArray) {
			JsonObject linkObj = e.getAsJsonObject();
			
			String title = linkObj.get("title").getAsString();
			linksList.add(title);
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
		
		// Construct the Result and return
		LinksResult result = new LinksResult(linksList, plCont);
		return result;
	}
}
