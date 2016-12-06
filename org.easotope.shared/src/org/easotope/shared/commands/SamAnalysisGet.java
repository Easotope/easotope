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

package org.easotope.shared.commands;

import java.util.Hashtable;
import java.util.List;

import org.easotope.framework.commands.Command;
import org.easotope.framework.dbcore.util.RawFileManager;
import org.easotope.shared.analysis.server.GetFromDb;
import org.easotope.shared.analysis.tables.SamAnalysis;
import org.easotope.shared.analysis.tables.SamStep;

import com.j256.ormlite.support.ConnectionSource;

public class SamAnalysisGet extends Command {
	private static final long serialVersionUID = 1L;

	private int samAnalysisId;
	private SamAnalysis samAnalysis = null;
	private List<SamStep> samSteps = null;

	@Override
	public boolean authenticate(ConnectionSource connectionSource, RawFileManager rawFileManager, Hashtable<String, Object> authenticationObjects) throws Exception {
		return true;
	}

	public void execute(ConnectionSource connectionSource, RawFileManager rawFileManager, Hashtable<String,Object> authenticationObjects) throws Exception {
		samAnalysis = GetFromDb.getSamAnalysis(connectionSource, samAnalysisId);
		samSteps = GetFromDb.getSamSteps(connectionSource, samAnalysisId);
	}

	public void setSamAnalysisId(int samAnalysisId) {
		this.samAnalysisId = samAnalysisId;
	}

	public SamAnalysis getSamAnalysis() {
		return samAnalysis;
	}

	public List<SamStep> getSamSteps() {
		return samSteps;
	}
}
