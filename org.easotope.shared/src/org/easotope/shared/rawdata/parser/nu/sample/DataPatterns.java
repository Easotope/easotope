package org.easotope.shared.rawdata.parser.nu.sample;

import java.util.ArrayList;
import java.util.regex.Pattern;

public class DataPatterns {
	ArrayList<Pattern> refPatterns = new ArrayList<Pattern>();
	ArrayList<Pattern> samPatterns = new ArrayList<Pattern>();
	ArrayList<Pattern> endPatterns = new ArrayList<Pattern>();

	public DataPatterns(boolean isLidi2) {
		if (!isLidi2) {
			refPatterns.add(Pattern.compile("^Gas\\s+Ref\\s*$"));
			refPatterns.add(Pattern.compile("^Gas\\s+Ref\\s+Block\\s+\\d+\\s+Cycle\\s+\\d+.*$"));
			
			samPatterns.add(Pattern.compile("^Gas\\s+Sam\\s*$"));
			samPatterns.add(Pattern.compile("^Gas\\s+Sam\\s+Block\\s+\\d+\\s+Cycle\\s+\\d+.*$"));
			
			endPatterns.add(Pattern.compile("^\\s*Gas\\s*$"));
			endPatterns.add(Pattern.compile("^\\s*Gas\\s+Block\\s+\\d+.*$"));

		} else {
			refPatterns.add(Pattern.compile("^\\s*Gas\\s+Ref\\s+Block\\s+\\d+\\s+LidiRefCycle\\s+\\d+\\s+.*$"));

			samPatterns.add(Pattern.compile("^Gas\\s+Sam\\s*$"));
			samPatterns.add(Pattern.compile("^Gas\\s+Sam\\s+Block\\s+\\d+\\s+Cycle\\s+\\d+.*$"));
			
			endPatterns.add(Pattern.compile("^End of Analysis$"));
		}
	}

	public boolean matchesRefPattern(String line) {
		for (Pattern pattern : refPatterns) {
			if (pattern.matcher(line).matches()) {
				return true;
			}
		}
		
		return false;
	}
	
	public boolean matchesSamPattern(String line) {
		for (Pattern pattern : samPatterns) {
			if (pattern.matcher(line).matches()) {
				return true;
			}
		}
		
		return false;
	}

	public boolean matchesEndPattern(String line) {
		for (Pattern pattern : endPatterns) {
			if (pattern.matcher(line).matches()) {
				return true;
			}
		}
		
		return false;
	}
}
