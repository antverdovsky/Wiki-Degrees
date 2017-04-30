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
		private String plContinue;          // Continue entry in JSON data
		
		/**
		 * Initializes a new Links Result instance with the specified links
		 * and continue token.
		 * @param links The Links.
		 * @param cont The continue token.
		 */
		public LinksResult(ArrayList<String> links, String cont) {
			this.links = links;
			this.plContinue = cont;
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
		 * @return The PlContinue Token.
		 */
		public String getPlContinue() {
			return this.plContinue;
		}
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
		/********************************
		 * TODO: Handle Continue Token. *
		 ********************************/
		
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
		
		for (JsonElement e : linksArray) { // Foreach link in the links array
			JsonObject linkObj = e.getAsJsonObject();
			
			// Fetch the namespace and title of the link
			int namespace = linkObj.get("ns").getAsInt();
			String title = linkObj.get("title").getAsString();
			
			// If the namespace is zero then this is a valid Wikipedia Article
			// link, so add it to the links list.
			if (namespace == 0) linksList.add(title);
		}
		
		// Navigate Root -> Continue and fetch the plContinue entry
		JsonElement cont = root.getAsJsonObject().get("continue"); 
		String plCont = cont.getAsJsonObject().get("plcontinue").toString();
		
		// Construct the Result and return.
		LinksResult result = new LinksResult(linksList, plCont);
		return result;
	}
}
