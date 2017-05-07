package com.antverdovsky.wikideg;

import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class WikiParse {
	/**
	 * Finds the random article in the specified JSON data.
	 * @return The title of a random Wikipedia article.
	 */
	public static String parseRandomArticle(String json) {
		// Create a JSON Parser using GSON and parse the root of the JSON data
		JsonParser jParser = new JsonParser();
		JsonElement root = jParser.parse(json);
		
		// Navigate Root -> Query -> Random
		JsonElement jQuery = root.getAsJsonObject().get("query");
		JsonElement jRandom = jQuery.getAsJsonObject().get("random");
		
		// Fetch the random array in the JSON data and just get the first 
		// element.	
		JsonArray randomArray = jRandom.getAsJsonArray();
		JsonObject first = randomArray.get(0).getAsJsonObject();
		return first.get("title").getAsString();
	}
	
	/**
	 * Finds the embedded article name of the target article in the specified
	 * export data.
	 * @param data The export data.
	 * @param target The name of the article which should be embedded in the
	 *               export data.
	 * @return The embedded title of the target article.
	 */
	public static String parseEmbeddedArticle(String data, String target) {
		// Find where in the current article, the name of the next
		// article is embedded. Trim the data so that we have the string
		// containing the name of the next article as it is embedded,
		// along with all of the leftover information.
		String preEmbeddedName = "[[" + target + "|";
		int indexOfEmbeddedNameStart = data.indexOf(preEmbeddedName);
		indexOfEmbeddedNameStart += preEmbeddedName.length();
		data = data.substring(indexOfEmbeddedNameStart);

		// This will be true if the indexOfEmbeddedNameStart was less than
		// zero, period to the pre embedded name string length being added
		// on. In this case, the current article references the next
		// article, directly, so we can return the next article name.
		if (indexOfEmbeddedNameStart < preEmbeddedName.length()) 
			return target;

		// Now get the index of where the embedded name ends (on Wikipedia
		// this is signified by the double brackets) and trim so that we
		// only have the embedded name leftover.
		int indexOfEmbeddedNameEnd = data.indexOf("]]");
		data = data.substring(0, indexOfEmbeddedNameEnd);

		// Return the embedded name
		return data;
	}

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
	public static String parseBacklinksJSON(
			String json, ArrayList<String> backlinks, String target) {
		// Create a JSON Parser using GSON and parse the root of the JSON data
		JsonParser jParser = new JsonParser();
		JsonElement root = jParser.parse(json);

		// Navigate Root -> Query -> Backlinks
		JsonElement jQuery = root.getAsJsonObject().get("query");
		JsonElement jBacklinks = jQuery.getAsJsonObject().get("backlinks");
		if (jBacklinks == null) return "";
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
	 * Parses the specified export data, searching for links. All of the
	 * links fetched from the export data are then added to the links
	 * reference parameter. If the target string is not empty, for each 
	 * link fetched from the JSON data, if the target equals the link, the
	 * method will append the target to the links and halt its execution.
	 * @param export The export data String.
	 * @param links The links set into which the parsed links are to be 
	 *              appended.
	 * @param target The target String which is to be found in the JSON data.
	 */
	public static void parseLinksExport(
			String export, ArrayList<String> links, String target) {
		String pattern = "\\[\\[(.*?)\\]\\]";
		Pattern regex = Pattern.compile(pattern);
		Matcher matcher = regex.matcher(export);
		
		while (matcher.find()) {
			String match = matcher.group(0);
			
			int startIndex = 2; // For stripping off the "[["
			int endIndex = match.length() - 2; // For stripping off the "]]"
			
			// If the string contains a "|" then the article has a link but it
			// is referenced under a different name in this page. Either way,
			// we don't want the name of how it is referenced, so we will cut
			// off the string before the reference name.
			int split = match.indexOf('|');
			if (split >= 0) endIndex = split;
			
			// Get the article name and add it to the links list.
			// TODO: Need to not add non namespace zero links...
			match = match.substring(startIndex, endIndex);
			links.add(match);
			
			// If we found the target then return
			if (match.equals(target)) return;
		}
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
	public static String parseLinksJSON(
			String json, ArrayList<String> links, String target) {
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
		if (jLinks == null) return "";
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
