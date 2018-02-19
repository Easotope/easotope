/*
 * Copyright © 2016-2018 by Devon Bowen.
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

package org.easotope.client.rawdata.batchimport.grouping;

import java.util.TreeSet;

import org.easotope.client.rawdata.batchimport.ImportedFile;
import org.easotope.shared.core.cache.CacheList;

public class Grouping {
	public static int groupingMenuDefaultInteger = 1;		// GroupingNone

	// order is important here - more restrictive groupings are later than less restrictive ones
	private static GroupingAlgorithm[] groupingAlgorithms = new GroupingAlgorithm[] {
		new GroupingNone(),
		new GroupingContiguousCommonType(),
		new GroupingCommonAssignment(),
		new GroupingManual()
	};

	@SuppressWarnings("serial")
	public static CacheList<GroupingAlgorithm> groupingMenu = new CacheList<GroupingAlgorithm>() {{
		for (int i=0; i<groupingAlgorithms.length; i++) {
			put(i+1, groupingAlgorithms[i]);
		}
	}};

	public static void applyGroupingAlgorithm(int selectedGroupingInteger, TreeSet<ImportedFile> importedFiles) {
		GroupingAlgorithm selectedGroupingAlgorithm = groupingMenu.get(selectedGroupingInteger);
		selectedGroupingAlgorithm.applyGroupingAlgorithm(importedFiles);
	}

	public static int getCurrentGrouping(int selectedGroupingInteger, TreeSet<ImportedFile> importedFiles) {
		GroupingAlgorithm selectedGroupingAlgorithm = groupingMenu.get(selectedGroupingInteger);

		if (selectedGroupingAlgorithm.isCurrentGrouping(importedFiles)) {
			return selectedGroupingInteger;
		}

		for (int i=0; i<groupingAlgorithms.length; i++) {
			GroupingAlgorithm groupingAlgorithm = groupingAlgorithms[i];

			if (groupingAlgorithm != selectedGroupingAlgorithm && groupingAlgorithm.isCurrentGrouping(importedFiles)) {
				return i+1;
			}
		}

		return groupingAlgorithms.length;
	}
}
