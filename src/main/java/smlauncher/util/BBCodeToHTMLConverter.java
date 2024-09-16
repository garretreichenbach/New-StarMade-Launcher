package smlauncher.util;

import smlauncher.news.LauncherNewsPanel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class BBCodeToHTMLConverter {
	public static ArrayList<String> convert(String bbcode) {
		ArrayList<String> output = new ArrayList<>();
		for(String line : getLines(bbcode)) {
			// Regular stuff
			line = line.replace("[", "<").replace("]", ">");
			// Lists
			line = line.replace("<list>", "<ul>");
			line = line.replace("</list>", "</ul>");
			line = line.replace("<*>", "<li>");

			output.add(line);
		}
		return output;
	}

	public static ArrayList<String> getLines(String str) {
		return new ArrayList<>(Arrays.asList(str.split(Pattern.quote("\n"))));
	}

	public static ArrayList<String> insertColors(List<String> lines, String color) {
		ArrayList<String> nLines = new ArrayList<>();
		for(String line : lines) {
			int index = line.indexOf('>');
			String s;
			if(index == -1) {
				// No line found
				s = "<font face=\"Verdana\" color=\"" + color + "\">" + line;
				if(line.length() >= 3) {
					if(line.startsWith("[b]") || !line.startsWith("<")) {
						s = "<br>" + s + "<br>";
					}
				}
			} else s = insertString(line, "<font color=\"" + color + "\">", index);
			nLines.add(s);
		}
		return nLines;
	}

	public static String insertString(String originalString, String stringToBeInserted, int index) {

		// Create a new string
		String newString = "";

		for(int i = 0; i < originalString.length(); i++) {

			// Insert the original string character
			// into the new string
			newString += originalString.charAt(i);

			if(i == index) {

				// Insert the string to be inserted
				// into the new string
				newString += stringToBeInserted;
			}
		}

		// return the modified String
		return newString;
	}
}
