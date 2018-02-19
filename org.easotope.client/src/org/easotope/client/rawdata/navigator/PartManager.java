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

package org.easotope.client.rawdata.navigator;

import java.util.HashMap;

import org.easotope.client.core.part.EasotopePart;
import org.easotope.client.rawdata.PluginConstants;
import org.easotope.client.rawdata.replicate.SampleReplicatePart;
import org.easotope.client.rawdata.replicate.StandardReplicatePart;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.basic.MBasicFactory;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;

public class PartManager {
	public static void showRawDataPerspective(EasotopePart easotopePart) {
		showRawDataPerspective(easotopePart.getApplication(), easotopePart.getPartService());
	}

	public static void showRawDataPerspective(MApplication app, EPartService partService) {
	    EModelService modelService = (EModelService) app.getContext().get(EModelService.class.getName());
	    MPerspective element = (MPerspective) modelService.find("org.easotope.rawdata.perspective", app);
	    partService.switchPerspective(element);
	}

	public static void openStandardReplicate(EasotopePart easotopePart, int replicateId) {
		HashMap<String,String> parameters = new HashMap<String,String>();
		parameters.put(SampleReplicatePart.INPUTURI_PARAM_REPLICATE, String.valueOf(replicateId));
		PartManager.openPart(easotopePart, SampleReplicatePart.ELEMENTID_BASE, StandardReplicatePart.class.getName(), parameters, true);
	}

	public static void openSampleReplicate(EasotopePart easotopePart, int replicateId) {
		HashMap<String,String> parameters = new HashMap<String,String>();
		parameters.put(SampleReplicatePart.INPUTURI_PARAM_REPLICATE, String.valueOf(replicateId));
		PartManager.openPart(easotopePart, SampleReplicatePart.ELEMENTID_BASE, SampleReplicatePart.class.getName(), parameters, true);
	}

	public static void openPart(EasotopePart easotopePart, String elementIdBase, String editorPartName, HashMap<String,String> parameters, boolean showPartIfExists) {
		openPart(easotopePart.getApplication(), easotopePart.getPartService(), elementIdBase, editorPartName, parameters, showPartIfExists);
	}

	public static void openPart(MApplication app, EPartService partService, String elementIdBase, String editorPartName, HashMap<String,String> parameters, boolean showPartIfExists) {
		String elementId = elementIdBase + ".";

		for (String key : parameters.keySet()) {
			elementId += key + "=" + parameters.get(key) + ".";
		}

		EModelService modelService = (EModelService) app.getContext().get(EModelService.class.getName());

	    if (showPartIfExists) {
	    		MPart part = (MPart) modelService.find(elementId, app);

	    		if (part != null) {
	    			partService.showPart(part, PartState.ACTIVATE);
	    			return;
	    		}
	    }

	    MPart part = MBasicFactory.INSTANCE.createPart();
	    part.setContributionURI(PluginConstants.BUNDLE_CLASS_PREFIX + editorPartName);
	    part.setElementId(elementId);
	    part.getPersistedState().putAll(parameters);
	    part.setCloseable(true);

		MPartStack stack = (MPartStack) modelService.find(PluginConstants.EDITOR_PARTSTACK, app);
	    stack.getChildren().add(part);

	    partService.showPart(part, PartState.ACTIVATE);
	}
}
