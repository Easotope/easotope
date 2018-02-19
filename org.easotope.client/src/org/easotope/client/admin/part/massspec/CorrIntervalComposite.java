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

package org.easotope.client.admin.part.massspec;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

import org.easotope.client.Messages;
import org.easotope.client.core.GuiConstants;
import org.easotope.client.core.Icons;
import org.easotope.client.core.adaptors.LoggingAdaptor;
import org.easotope.client.core.adaptors.LoggingPaintAdaptor;
import org.easotope.client.core.part.ChainedPart;
import org.easotope.client.core.part.EditorComposite;
import org.easotope.client.core.widgets.DateTimeLabel;
import org.easotope.client.core.widgets.VDateTime;
import org.easotope.client.core.widgets.VDateTimeListener;
import org.easotope.client.core.widgets.VSelectList;
import org.easotope.client.core.widgets.VSelectListListener;
import org.easotope.client.core.widgets.VText;
import org.easotope.framework.dbcore.DatabaseConstants;
import org.easotope.shared.admin.ChannelToMZX10;
import org.easotope.shared.analysis.cache.analysis.AnalysisCache;
import org.easotope.shared.analysis.cache.analysis.repanalysislist.AnalysisCacheRepAnalysisListGetListener;
import org.easotope.shared.analysis.cache.analysis.repanalysislist.RepAnalysisList;
import org.easotope.shared.analysis.cache.analysis.repanalysislist.RepAnalysisListItem;
import org.easotope.shared.analysis.cache.corrinterval.CorrIntervalCache;
import org.easotope.shared.analysis.cache.corrinterval.corrinterval.CorrIntervalCacheCorrIntervalGetListener;
import org.easotope.shared.analysis.cache.corrinterval.corrinterval.CorrIntervalCacheCorrIntervalSaveListener;
import org.easotope.shared.analysis.cache.corrinterval.corrintervallist.CorrIntervalList;
import org.easotope.shared.analysis.cache.corrinterval.corrintervallist.CorrIntervalListItem;
import org.easotope.shared.analysis.tables.CorrIntervalV1;
import org.easotope.shared.core.DateFormat;
import org.easotope.shared.core.cache.logininfo.LoginInfoCache;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;

public class CorrIntervalComposite extends EditorComposite implements AnalysisCacheRepAnalysisListGetListener, CorrIntervalCacheCorrIntervalGetListener, CorrIntervalCacheCorrIntervalSaveListener {
	private final String DATA_ANALYSIS_LIST_GET = "DATA_ANALYSIS_LIST_GET";
	private final String CORR_INTERVAL_GET = "CORR_INTERVAL_GET";
	private final String CORR_INTERVAL_SAVE = "CORR_INTERVAL_SAVE";

	private Label id;
	private VDateTime validFrom;
	private Canvas validFromError;
	private DateTimeLabel validTo;
	//private VSpinner batchDelimiter;
	private VText channelToMzX10;
	private Canvas channelToMzX10Error;
	private VText description;
	private VSelectList dataAnalysis;
	private Label dataAnalysisTip;

	private HashMap<Integer,String> dataAnalysisIdToName = new HashMap<Integer,String>();
	private CorrIntervalList lastCorrIntervalList = null;
	private HashSet<Long> corrIntervalDates = new HashSet<Long>();

	public CorrIntervalComposite(ChainedPart chainedPart, Composite parent, int style) {
		super(chainedPart, parent, style);

		final Image errorImage = Icons.getError(parent.getDisplay());

		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		setLayout(gridLayout);

		Label idLabel = new Label(this, SWT.NONE);
		idLabel.setText(Messages.massSpecCorrIntervalComposite_idLabel);

		id = new Label(this, SWT.NONE);

		Label validFromLabel = new Label(this, SWT.NONE);
		validFromLabel.setText(Messages.massSpecCorrIntervalComposite_validFromLabel);

		Composite validFromComposite = new Composite(this, SWT.NONE);

		gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		validFromComposite.setLayout(gridLayout);

		validFrom = new VDateTime(validFromComposite, SWT.CHECK);
		validFrom.addSelectionListener(new VDateTimeListener() {
			@Override
			public void widgetSelected() {
				widgetStatusChanged();
				
				if (getCurrentCorrInterval() == null) {
					setDataAnalysisFromCurrentTime();
				}
			}
		});

		validFromError = new Canvas(validFromComposite, SWT.NONE);
		GridData gridData = new GridData();
		gridData.widthHint = errorImage.getImageData().width;
		gridData.heightHint = errorImage.getImageData().height;
		validFromError.setLayoutData(gridData);
		validFromError.setVisible(false);
		validFromError.addPaintListener(new LoggingPaintAdaptor() {
			public void loggingPaintControl(PaintEvent e) {
				e.gc.setAntialias(SWT.ON);
				e.gc.drawImage(errorImage, 0, 0);
			}
		});

		Label validToLabel = new Label(this, SWT.NONE);
		validToLabel.setText(Messages.massSpecCorrIntervalComposite_validToLabel);

		validTo = new DateTimeLabel(this, SWT.NONE);
		validTo.setDefaultText(Messages.massSpecCorrIntervalComposite_newCorrIntervalValidTo);

//		Label batchDelimiterLabel = new Label(this, SWT.NONE);
//		batchDelimiterLabel.setText(Messages.corrIntervalComposite_batchDelimiterLabel);
//
//		batchDelimiter = new VSpinner(this, SWT.BORDER);
//		batchDelimiter.setMinimum(0);
//		batchDelimiter.setTextLimit(3);
//		batchDelimiter.setMaximum(999);

		Label channelToMzX10Label = new Label(this, SWT.NONE);
		channelToMzX10Label.setText(Messages.massSpecCorrIntervalComposite_channelToMzX10Label);

		Composite channelToMzX10Composite = new Composite(this, SWT.NONE);
		gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		channelToMzX10Composite.setLayout(gridLayout);

		channelToMzX10 = new VText(channelToMzX10Composite, SWT.BORDER);
		gridData = new GridData();
		gridData.widthHint = GuiConstants.SHORT_TEXT_INPUT_WIDTH;
		channelToMzX10.setLayoutData(gridData);
		channelToMzX10.setTextLimit(GuiConstants.LONG_TEXT_INPUT_LIMIT);
		channelToMzX10.addListener(SWT.KeyUp, new LoggingAdaptor() {
			@Override
			public void loggingHandleEvent(Event event) {
				widgetStatusChanged();
			}
		});

		channelToMzX10Error = new Canvas(channelToMzX10Composite, SWT.NONE);
		gridData = new GridData();
		gridData.widthHint = errorImage.getImageData().width;
		gridData.heightHint = errorImage.getImageData().height;
		channelToMzX10Error.setLayoutData(gridData);
		channelToMzX10Error.setVisible(false);
		channelToMzX10Error.addPaintListener(new LoggingPaintAdaptor() {
			public void loggingPaintControl(PaintEvent e) {
				e.gc.setAntialias(SWT.ON);
				e.gc.drawImage(errorImage, 0, 0);
			}
		});

		Label ignore1 = new Label(this, SWT.NONE);
		gridData = new GridData();
		gridData.verticalAlignment = SWT.TOP;
		ignore1.setLayoutData(gridData);

		Label channelToMzX10Tip = new Label(this, SWT.WRAP);
		gridData = new GridData();
		gridData.verticalAlignment = SWT.TOP;
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		channelToMzX10Tip.setLayoutData(gridData);
		channelToMzX10Tip.setText(Messages.massSpecCorrIntervalComposite_channelToMzX10Tip);

		Label descriptionLabel = new Label(this, SWT.NONE);
		gridData = new GridData();
		gridData.verticalAlignment = SWT.TOP;
		descriptionLabel.setLayoutData(gridData);
		descriptionLabel.setText(Messages.massSpecCorrIntervalComposite_descriptionLabel);

		description = new VText(this, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.heightHint = GuiConstants.MULTI_LINE_INPUT_HEIGHT;
		description.setLayoutData(gridData);
		description.setTextLimit(GuiConstants.LONG_TEXT_INPUT_LIMIT);
		description.addListener(SWT.KeyUp, new LoggingAdaptor() {
			@Override
			public void loggingHandleEvent(Event event) {
				widgetStatusChanged();
			}
		});

		Label dataAnalysisLabel = new Label(this, SWT.NONE);
		gridData = new GridData();
		gridData.verticalAlignment = SWT.TOP;
		dataAnalysisLabel.setLayoutData(gridData);
		dataAnalysisLabel.setText(Messages.massSpecCorrIntervalComposite_dataAnalysisLabel);

		dataAnalysis = new VSelectList(this, SWT.BORDER);
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.heightHint = GuiConstants.MULTI_LINE_INPUT_HEIGHT;
		dataAnalysis.setLayoutData(gridData);
		dataAnalysis.setSelectionEnabled(false);
		dataAnalysis.addListener(new VSelectListListener() {
			@Override
			public void checkBoxesChanged() {
				widgetStatusChanged();
			}

			@Override
			public void selectionChanged() {

			}
		});

		Label ignore2 = new Label(this, SWT.NONE);
		gridData = new GridData();
		gridData.verticalAlignment = SWT.TOP;
		ignore2.setLayoutData(gridData);

		dataAnalysisTip = new Label(this, SWT.WRAP);
		gridData = new GridData();
		gridData.verticalAlignment = SWT.TOP;
		gridData.horizontalAlignment = SWT.FILL;
		gridData.grabExcessHorizontalSpace = true;
		dataAnalysisTip.setLayoutData(gridData);
		dataAnalysisTip.setText(Messages.massSpecCorrIntervalComposite_dataAnalysisTip);

		int commandId = AnalysisCache.getInstance().repAnalysisListGet(this);
		waitingFor(DATA_ANALYSIS_LIST_GET, commandId);

		AnalysisCache.getInstance().addListener(this);
		CorrIntervalCache.getInstance().addListener(this);
	}

	@Override
	protected void handleDispose() {
		AnalysisCache.getInstance().removeListener(this);
		CorrIntervalCache.getInstance().removeListener(this);
		super.handleDispose();
	}

	private CorrIntervalV1 getCurrentCorrInterval() {
		return (CorrIntervalV1) getCurrentObject();
	}

	private void setDataAnalysisFromCurrentTime() {
		CorrIntervalListItem precedingCorrIntervalListItem = getPrecedingCorrIntervalListItem();
		ArrayList<Integer> checkboxSelection = new ArrayList<Integer>();

		if (precedingCorrIntervalListItem != null) {
//			batchDelimiter.setSelection(precedingCorrIntervalListItem.getBatchDelimiter());
			channelToMzX10.setText(new ChannelToMZX10(precedingCorrIntervalListItem.getChannelToMZX10()).toString());

			for (int i : precedingCorrIntervalListItem.getSelectedDataAnalysis()) {
				checkboxSelection.add(i);
			}
		}

		dataAnalysis.setCheckboxSelection(checkboxSelection);
	}

	private CorrIntervalListItem getPrecedingCorrIntervalListItem() {
		long date = validFrom.getDate();

		CorrIntervalListItem precedingCorrIntervalListItem = null;

		if (lastCorrIntervalList != null) {
			for (Integer id : lastCorrIntervalList.keySet()) {
				CorrIntervalListItem thisItem = lastCorrIntervalList.get(id);
				long thisDate = thisItem.getDate();

				if (thisDate < date && (precedingCorrIntervalListItem == null || thisDate > precedingCorrIntervalListItem.getDate())) {
					precedingCorrIntervalListItem = thisItem;
				}
			}
		}
		
		return precedingCorrIntervalListItem;
	}

	@Override
	public void setCurrentFieldValues() {
		CorrIntervalV1 currentCorrInterval = getCurrentCorrInterval();

		id.setText(String.valueOf(currentCorrInterval.getId()));
		validFrom.setDate(currentCorrInterval.getValidFrom());

		if (currentCorrInterval.getValidUntil() == DatabaseConstants.MAX_DATE) {
			validTo.unsetDate();
		} else {
			validTo.setDate(currentCorrInterval.getValidUntil());
		}

//		batchDelimiter.setSelection(currentCorrInterval.getBatchDelimiter());
		channelToMzX10.setText(new ChannelToMZX10(currentCorrInterval.getChannelToMzX10()).toString());
		description.setText(currentCorrInterval.getDescription());

		ArrayList<Integer> checkboxSelection = new ArrayList<Integer>();

		for (int id : currentCorrInterval.getDataAnalysis()) {
			checkboxSelection.add(id);
		}

		dataAnalysis.setCheckboxSelection(checkboxSelection);
		dataAnalysisTip.setVisible(false);
	}

	@Override
	public void setDefaultFieldValues() {
		id.setText(Messages.massSpecCorrIntervalComposite_newRefGasId);
		validFrom.setDate(new Date().getTime());
		validTo.unsetDate();
//		batchDelimiter.setSelection(0);
		channelToMzX10.setText("");
		description.setText("");
		setDataAnalysisFromCurrentTime();
		dataAnalysisTip.setVisible(true);
	}

	@Override
	public void enableWidgets() {
		boolean hasGeneralEditPermission = LoginInfoCache.getInstance().getPermissions().isCanEditCorrIntervals();

		validFrom.setEnabled(hasGeneralEditPermission);
//		batchDelimiter.setEnabled(hasGeneralEditPermission);
		channelToMzX10.setEnabled(hasGeneralEditPermission);
		description.setEnabled(hasGeneralEditPermission);
		dataAnalysis.setCheckboxesEnabled(hasGeneralEditPermission);

		if (!hasGeneralEditPermission) {
			validFrom.revert();
//			batchDelimiter.revert();
			channelToMzX10.revert();
			description.revert();
			dataAnalysis.revert();
		}
	}

	@Override
	public void disableWidgets() {
		validFrom.setEnabled(false);
//		batchDelimiter.setEnabled(false);
		channelToMzX10.setEnabled(false);
		description.setEnabled(false);
		dataAnalysis.setCheckboxesEnabled(false);
	}

	@Override
	public void requestSave(boolean isResend) {
		raiseInfoBoxes();

		CorrIntervalV1 oldCorrInterval = getCurrentCorrInterval();
		CorrIntervalV1 newCorrInterval = new CorrIntervalV1();

		if (oldCorrInterval != null) {
			newCorrInterval.id = oldCorrInterval.getId();
		}

		CorrIntervalList corrIntervalList = (CorrIntervalList) getSelection().get(MassSpecPart.SELECTION_CORR_INTERVAL_LIST);
		newCorrInterval.setMassSpecId(corrIntervalList.getMassSpecId());
		newCorrInterval.setValidFrom(validFrom.getDate());
		newCorrInterval.setDescription(description.getText());
		newCorrInterval.setBatchDelimiter(0); // batchDelimiter.getSelection());
		newCorrInterval.setChannelToMzX10(new ChannelToMZX10(channelToMzX10.getText()).getMZX10s());

		int[] selection = new int[dataAnalysis.getCheckboxSelection().size()];

		int index = 0;
		for (Integer i : dataAnalysis.getCheckboxSelection()) {
			selection[index] = i;
			index++;
		}

		newCorrInterval.setDataAnalysis(selection);

		int commandId = CorrIntervalCache.getInstance().corrIntervalSave(newCorrInterval, this);
		waitingFor(CORR_INTERVAL_SAVE, commandId);
	}

	@Override
	protected boolean canDelete() {
		return false;
	}

	@Override
	protected boolean requestDelete() {
		return false;
	}

	private void raiseInfoBoxes() {
		CorrIntervalV1 oldCorrInterval = getCurrentCorrInterval();
		CorrIntervalListItem precedingCorrIntervalListItem = getPrecedingCorrIntervalListItem();

		HashMap<Integer,String> defaultDataAnalysis = new HashMap<Integer,String>();
		HashMap<Integer,String> copyDataAnalysis = new HashMap<Integer,String>();

		for (Integer dataAnalysisId : dataAnalysis.getCheckboxSelection()) {
			if (oldCorrInterval == null || !arrayContainsInt(oldCorrInterval.getDataAnalysis(), dataAnalysisId)) {
				if (precedingCorrIntervalListItem != null && arrayContainsInt(precedingCorrIntervalListItem.getSelectedDataAnalysis(), dataAnalysisId)) {
					copyDataAnalysis.put(dataAnalysisId, dataAnalysisIdToName.get(dataAnalysisId));
				} else {
					defaultDataAnalysis.put(dataAnalysisId, dataAnalysisIdToName.get(dataAnalysisId));
				}
			}
		}

		if (copyDataAnalysis.size() != 0) {
			String plural = copyDataAnalysis.size() == 1 ? "analysis" : "analyses";
			String list = sortedAndFormatDataAnalyses(copyDataAnalysis);
			String timeZoneId = LoginInfoCache.getInstance().getPreferences().getTimeZoneId();
			boolean showTimeZone = LoginInfoCache.getInstance().getPreferences().getShowTimeZone();
			String date = DateFormat.format(precedingCorrIntervalListItem.getDate(), timeZoneId, showTimeZone, false);

			String message = MessageFormat.format(Messages.massSpecCorrIntervalComposite_copyDataAnalyses, plural, list, date);
			getChainedPart().raiseInfo(message);
		}

		if (defaultDataAnalysis.size() != 0) {
			String plural = defaultDataAnalysis.size() == 1 ? "analysis" : "analyses";
			String list = sortedAndFormatDataAnalyses(defaultDataAnalysis);

			String message = MessageFormat.format(Messages.massSpecCorrIntervalComposite_defaultDataAnalyses, plural, list);
			getChainedPart().raiseInfo(message);
		}
	}

	private String sortedAndFormatDataAnalyses(HashMap<Integer,String> copyDataAnalysis) {
		ArrayList<String> sorted = new ArrayList<String>();
		sorted.addAll(copyDataAnalysis.values());
		Collections.sort(sorted);

		String list = "";

		for (String sting : sorted) {
			list += list.isEmpty() ? sting : ", " + sting;
		}

		return list;
	}

	private boolean arrayContainsInt(int[] dataAnalysis, Integer dataAnalysisId) {
		if (dataAnalysisId == null) {
			return false;
		}

		for (int i : dataAnalysis) {
			if (i == dataAnalysisId) {
				return true;
			}
		}

		return false;
	}

	@Override
	protected boolean isDirty() {
		boolean isDirty = false;

		isDirty = isDirty || validFrom.hasChanged();
//		isDirty = isDirty || batchDelimiter.hasChanged();
		isDirty = isDirty || channelToMzX10.hasChanged();
		isDirty = isDirty || description.hasChanged();
		isDirty = isDirty || dataAnalysis.hasChanged();

		return isDirty;
	}

	@Override
	protected boolean hasError() {
		boolean dateErrorSet = false;
		boolean channelToMzX10ErrorSet = false;

		if (validFrom.hasChanged() && corrIntervalDates.contains(validFrom.getDate())) {
			validFromError.setToolTipText(Messages.massSPecCorrIntervalComposite_validFromDateNotUnique);
			dateErrorSet = true;
		}

		if (dateErrorSet != validFromError.getVisible()) {
			validFromError.setVisible(dateErrorSet);
			layoutNeeded();
		}

		if (channelToMzX10.hasChanged()) {
			ChannelToMZX10 parsed = new ChannelToMZX10(channelToMzX10.getText());

			if (!parsed.isValid()) {
				channelToMzX10Error.setToolTipText(parsed.getErrorMessage());
				channelToMzX10ErrorSet = true;
			}
		}

		if (channelToMzX10ErrorSet != channelToMzX10Error.getVisible()) {
			channelToMzX10Error.setVisible(channelToMzX10ErrorSet);
			layoutNeeded();
		}

		return dateErrorSet || channelToMzX10ErrorSet;
	}

	@Override
	protected boolean selectionHasChanged(HashMap<String,Object> selection) {
		CorrIntervalList currentCorrIntervalList = (CorrIntervalList) selection.get(MassSpecPart.SELECTION_CORR_INTERVAL_LIST);

		if (currentCorrIntervalList != lastCorrIntervalList) {
			lastCorrIntervalList = currentCorrIntervalList;
			corrIntervalDates.clear();

			if (currentCorrIntervalList != null) {
				for (Integer id : currentCorrIntervalList.keySet()) {
					CorrIntervalListItem corrIntervalListItem = currentCorrIntervalList.get(id);
					corrIntervalDates.add(corrIntervalListItem.getDate());
				}
			}
		}

		CorrIntervalV1 corrInterval = getCurrentCorrInterval();
		Integer selectedCorrIntervalId = (Integer) selection.get(MassSpecPart.SELECTION_CORR_INTERVAL_ID);

		if (corrInterval == null && selectedCorrIntervalId == null) {
			return false;
		}

		if ((corrInterval == null && selectedCorrIntervalId != null) || (corrInterval != null && selectedCorrIntervalId == null)) {
			return true;
		}

		return corrInterval.getId() != selectedCorrIntervalId;
	}

	@Override
	protected boolean processSelection(HashMap<String,Object> selection) {
		Integer corrIntervalId = (Integer) selection.get(MassSpecPart.SELECTION_CORR_INTERVAL_ID);

		if (corrIntervalId != null) {
			int commandId = CorrIntervalCache.getInstance().corrIntervalGet(corrIntervalId, this);
			waitingFor(CORR_INTERVAL_GET, commandId);
			return true;
		}

		return false;
	}

	private void newDataAnalysisList(RepAnalysisList dataAnalysisList) {
		HashMap<String,Integer> possibilities = new HashMap<String,Integer>();
		dataAnalysisIdToName.clear();

		for (int id : dataAnalysisList.keySet()) {
			RepAnalysisListItem item = dataAnalysisList.get(id);
			possibilities.put(item.getName(), id);
			dataAnalysisIdToName.put(id, item.getName());
		}

		dataAnalysis.setPossibilities(possibilities);
	}

	@Override
	public void repAnalysisListGetCompleted(int commandId, RepAnalysisList dataAnalysisList) {
		newDataAnalysisList(dataAnalysisList);
		doneWaitingFor(DATA_ANALYSIS_LIST_GET);
	}

	@Override
	public void repAnalysisListGetError(int commandId, String message) {
		raiseGetError(DATA_ANALYSIS_LIST_GET, message);
	}

	@Override
	public void corrIntervalGetCompleted(int commandId, CorrIntervalV1 corrInterval) {
		newObject(CORR_INTERVAL_GET, corrInterval);
	}

	@Override
	public void corrIntervalUpdated(int commandId, CorrIntervalV1 corrInterval) {
		updateObject(corrInterval, Messages.massSpec_corrIntervalHasBeenUpdated);
	}

	@Override
	public void corrIntervalGetError(int commandId, String message) {
		raiseGetError(CORR_INTERVAL_GET, message);		
	}

	@Override
	public void corrIntervalSaveCompleted(int commandId, CorrIntervalV1 corrInterval) {
		saveComplete(CORR_INTERVAL_SAVE, corrInterval);
	}

	@Override
	public void corrIntervalSaveError(int commandId, String message) {
		raiseSaveOrDeleteError(CORR_INTERVAL_SAVE, message);
	}
}
