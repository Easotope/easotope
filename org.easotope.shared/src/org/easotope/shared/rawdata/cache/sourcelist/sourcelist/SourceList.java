/*
 * Copyright © 2016-2019 by Devon Bowen.
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

package org.easotope.shared.rawdata.cache.sourcelist.sourcelist;

import java.util.ArrayList;
import java.util.HashMap;

public class SourceList extends ArrayList<SourceListItem> {
	private static final long serialVersionUID = 1L;

	private transient HashMap<Integer,ArrayList<SourceListItem>> byUserId;
	private transient HashMap<Integer,ArrayList<SourceListItem>> byProjectId;
	private transient HashMap<Integer,ArrayList<SourceListItem>> bySampleId;
	private transient HashMap<Integer,ArrayList<SourceListItem>> byStandardId;

	public void buildIndices() {
		byUserId = new HashMap<Integer,ArrayList<SourceListItem>>();
		byProjectId = new HashMap<Integer,ArrayList<SourceListItem>>();
		bySampleId = new HashMap<Integer,ArrayList<SourceListItem>>();
		byStandardId = new HashMap<Integer,ArrayList<SourceListItem>>();

		for (SourceListItem sourceListItem : this) {
			if (!byUserId.containsKey(sourceListItem.getUserId())) {
				byUserId.put(sourceListItem.getUserId(), new ArrayList<SourceListItem>());
			}

			byUserId.get(sourceListItem.getUserId()).add(sourceListItem);
			
			if (!byProjectId.containsKey(sourceListItem.getProjectId())) {
				byProjectId.put(sourceListItem.getProjectId(), new ArrayList<SourceListItem>());
			}

			byProjectId.get(sourceListItem.getProjectId()).add(sourceListItem);

			if (!sourceListItem.isStandard()) {
				if (!bySampleId.containsKey(sourceListItem.getSampleId())) {
					bySampleId.put(sourceListItem.getSampleId(), new ArrayList<SourceListItem>());
				}
	
				bySampleId.get(sourceListItem.getSampleId()).add(sourceListItem);
			}

			if (sourceListItem.isStandard()) {
				if (!byStandardId.containsKey(sourceListItem.getStandardId())) {
					byStandardId.put(sourceListItem.getStandardId(), new ArrayList<SourceListItem>());
				}
	
				byStandardId.get(sourceListItem.getStandardId()).add(sourceListItem);
			}
		}
	}

	public ArrayList<SourceListItem> getByUserId(int userId) {
		return byUserId.get(userId);
	}

	public ArrayList<SourceListItem> getByProjectId(int projectId) {
		return byProjectId.get(projectId);
	}

	public ArrayList<SourceListItem> getBySampleId(int sampleId) {
		return bySampleId.get(sampleId);
	}

	public ArrayList<SourceListItem> getByStandardId(int standardId) {
		return byStandardId.get(standardId);
	}
}
