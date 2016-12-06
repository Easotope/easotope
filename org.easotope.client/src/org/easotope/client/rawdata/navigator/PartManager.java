package org.easotope.client.rawdata.navigator;

import java.util.HashMap;

import org.easotope.client.core.part.EasotopePart;
import org.easotope.client.rawdata.PluginConstants;
import org.eclipse.e4.ui.model.application.ui.basic.MBasicFactory;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;

public class PartManager {
	public static void openPart(EasotopePart easotopePart, String elementIdBase, String editorPartName, HashMap<String,String> parameters, boolean showPartIfExists) {
		String elementId = elementIdBase + ".";

		for (String key : parameters.keySet()) {
			elementId += key + "=" + parameters.get(key) + ".";
		}

		EModelService modelService = (EModelService) easotopePart.getApplication().getContext().get(EModelService.class.getName());

	    if (showPartIfExists) {
	    		MPart part = (MPart) modelService.find(elementId, easotopePart.getApplication());

	    		if (part != null) {
	    			easotopePart.getPartService().showPart(part, PartState.ACTIVATE);
	    			return;
	    		}
	    }

	    MPart part = MBasicFactory.INSTANCE.createPart();
	    part.setContributionURI(PluginConstants.BUNDLE_CLASS_PREFIX + editorPartName);
	    part.setElementId(elementId);
	    part.getPersistedState().putAll(parameters);
	    part.setCloseable(true);

		MPartStack stack = (MPartStack) modelService.find(PluginConstants.EDITOR_PARTSTACK, easotopePart.getApplication());
	    stack.getChildren().add(part);

	    easotopePart.getPartService().showPart(part, PartState.ACTIVATE);
	}
}
