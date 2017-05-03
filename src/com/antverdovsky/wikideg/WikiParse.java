package com.antverdovsky.wikideg;

import java.util.HashSet;
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
	 * otherwise an empty String is returned instead. If the target string
	 * is not empty, for each link fetched from the JSON data, if the target
	 * equals the link, the method will append the target to the links and
	 * halt its execution, returning an empty return string.
	 * @param json The JSON data which is to be parsed.
	 * @param backlinks The backlinks set into which the parsed backlinks are
	 *                  to be appended.
	 * @param target The target String which is to be found in the JSON data. 
	 * @return The continue token of the JSON data, or an empty String if the
	 *         JSON data contains no continue token, or if the target has been
	 *         found.
	 */
	public static String parseBacklinks(
			String json, HashSet<String> backlinks, String target) {
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
			
			// Fetch the title and add it to the backlinks set. If the title
			// is actually the target we are looking for, we do not need to
			// parse any more JSON data.
			String title = jLinkObj.get("title").getAsString();
			backlinks.add(title);
			if (title.equals(target)) return "";
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
	 * otherwise an empty String is returned instead. If the target string
	 * is not empty, for each link fetched from the JSON data, if the target
	 * equals the link, the method will append the target to the links and
	 * halt its execution, returning an empty return string.
	 * @param json The JSON data which is to be parsed.
	 * @param links The links set into which the parsed links are to be 
	 *              appended.
	 * @param target The target String which is to be found in the JSON data. 
	 * @return The continue token of the JSON data, or an empty String if the
	 *         JSON data contains no continue token, or if the target has been
	 *         found.
	 */
	public static String parseLinks(
			String json, HashSet<String> links, String target) {
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
			
			// Fetch the title and add it to the backlinks set. If the title
			// is actually the target we are looking for, we do not need to
			// parse any more JSON data.
			String title = jLinkObj.get("title").getAsString();
			links.add(title);
			if (title.equals(target)) return "";
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
