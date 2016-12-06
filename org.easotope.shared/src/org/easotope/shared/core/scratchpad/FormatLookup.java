/*
 * Copyright © 2016 by Devon Bowen.
 *
 * This file is part of Easotope.
 *
 * Easotope is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * Additional permission under GNU GPL version 3 section 7:
 * If you modify this Program, or any covered work, by linking or combining
 * it with the Eclipse Rich Client Platform (or a modified version of that
 * library), containing parts covered by the terms of the Eclipse Public
 * License, the licensors of this Program grant you additional permission
 * to convey the resulting work. Corresponding Source for a non-source form
 * of such a combination shall include the source code for the parts of the
 * Eclipse Rich Client Platform used as well as that of the covered work.
 *
 * Easotope is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Easotope. If not, see <http://www.gnu.org/licenses/>.
 */

package org.easotope.shared.core.scratchpad;

import java.io.Serializable;
import java.util.HashMap;

import org.easotope.shared.core.scratchpad.AnalysisIdentifier.Level;

public class FormatLookup implements Serializable {
	private static final long serialVersionUID = 1L;

	private HashMap<AnalysisIdentifier,HashMap<String,String>> lookup = new HashMap<AnalysisIdentifier,HashMap<String,String>>();

	public FormatLookup() { }

	public FormatLookup(FormatLookup formatLookup) {
		add(formatLookup);
	}

	public void clear() {
		lookup.clear();
	}

	public void add(FormatLookup formatLookup) {
		for (AnalysisIdentifier analysisIdentifier : formatLookup.lookup.keySet()) {
			lookup.put(new AnalysisIdentifier(analysisIdentifier), new HashMap<String,String>(formatLookup.lookup.get(analysisIdentifier)));
		}
	}

	public void add(Level level, String analysisName, HashMap<String,String> formats) {
		lookup.put(new AnalysisIdentifier(level, analysisName), new HashMap<String,String>(formats));
	}

	public String getFormat(Level level, String analysisName, String columnName) {
		AnalysisIdentifier analysisIdentifier = new AnalysisIdentifier(level, analysisName);

		if (lookup.containsKey(analysisIdentifier)) {
			HashMap<String,String> formats = lookup.get(analysisIdentifier);

			if (formats.containsKey(columnName)) {
				return formats.get(columnName);
			}
		}

		return null;
	}
}
