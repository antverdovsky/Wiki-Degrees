package com.antverdovsky.wikideg;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Utilities {
	/**
	 * Checks if the specified list of strings contains the target string,
	 * ignoring the case of the strings.
	 * @param list The list of strings to be checked.
	 * @param str The string which we are to search for in the List.
	 * @return True if the list contains the string. False otherwise.
	 */
	public static boolean containsIgnoreCase(List<String> list, String str) {
		for (String element : list)
			if (str.equalsIgnoreCase(element))
				return true;
		
		return false;
	}
	
	/**
	 * Returns a list containing the intersection of list A and B. An element
	 * will be in the intersection if it is in both A and B (string case is
	 * ignored).
	 * @param a List A.
	 * @param b List B.
	 * @return The list of the intersection of A and B.
	 */
	public static List<String> retainAllIgnoreCase(
			List<String> a, List<String> b) {
		Stream<String> stream = a.stream().filter(
				s -> containsIgnoreCase(b, s));
		
		return stream.collect(Collectors.toList());
	}
}
