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

package org.easotope.client.analysis.part.analysis.replicate.repsteps;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;

import org.easotope.client.Messages;
import org.easotope.client.core.ColorCache;
import org.easotope.client.core.GuiConstants;
import org.easotope.client.core.PointDesign;
import org.easotope.client.core.PointStyle;
import org.easotope.client.core.adaptors.LoggingAdaptor;
import org.easotope.client.core.part.EasotopeComposite;
import org.easotope.client.core.part.EasotopePart;
import org.easotope.client.core.widgets.graph.Graph;
import org.easotope.client.core.widgets.graph.drawables.LineWithoutEnds;
import org.easotope.client.core.widgets.graph.drawables.Point;
import org.easotope.framework.commands.Command;
import org.easotope.shared.admin.IsotopicScale;
import org.easotope.shared.admin.SciConstantNames;
import org.easotope.shared.admin.StandardParameter;
import org.easotope.shared.admin.cache.sciconstant.SciConstantCache;
import org.easotope.shared.admin.cache.sciconstant.sciconstant.SciConstantCacheSciConstantGetListener;
import org.easotope.shared.admin.cache.standard.StandardCache;
import org.easotope.shared.admin.cache.standard.standard.StandardCacheStandardGetListener;
import org.easotope.shared.admin.cache.standard.standardlist.StandardCacheStandardListGetListener;
import org.easotope.shared.admin.cache.standard.standardlist.StandardList;
import org.easotope.shared.admin.cache.standard.standardlist.StandardListItem;
import org.easotope.shared.admin.cache.user.UserCache;
import org.easotope.shared.admin.tables.SciConstant;
import org.easotope.shared.admin.tables.Standard;
import org.easotope.shared.analysis.execute.AnalysisCompiled;
import org.easotope.shared.analysis.execute.ComparableStandardOutputs;
import org.easotope.shared.analysis.execute.ComparableStandardOutputs.ComparableStandardOutput;
import org.easotope.shared.core.DateFormat;
import org.easotope.shared.core.NumericValue;
import org.easotope.shared.core.cache.logininfo.LoginInfoCache;
import org.easotope.shared.core.scratchpad.Accumulator;
import org.easotope.shared.core.scratchpad.Pad;
import org.easotope.shared.core.scratchpad.ReplicatePad;
import org.easotope.shared.core.scratchpad.ScratchPad;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;

public class TabOffsetsComposite extends EasotopeComposite implements SciConstantCacheSciConstantGetListener, StandardCacheStandardListGetListener, StandardCacheStandardGetListener {
	private StackLayout stackLayout;

	private Combo column;
	private Button absolute;
	private Graph graph;

	private Composite waitingOnInput;
	private Composite calculationsReady;

	private ScratchPad<ReplicatePad> corrIntervalScratchPad;
	private ArrayList<ComparableStandardOutput> comparableStandardOutputs = new ArrayList<ComparableStandardOutput>();

	private Double δ18O_VPDB_VSMOW;
	private StandardList standardList;
	private HashMap<Integer,Standard> standards = new HashMap<Integer,Standard>();
	private HashMap<Integer,PointDesign> pointDesigns = new HashMap<Integer,PointDesign>();
	private HashMap<Integer,PointDesign> fadedPointDesigns = new HashMap<Integer,PointDesign>();

	TabOffsetsComposite(EasotopePart easotopePart, Composite parent, int style) {
		super(easotopePart, parent, style);
		
		stackLayout = new StackLayout();
        setLayout(stackLayout);

        waitingOnInput = new Composite(this, SWT.NONE);
        waitingOnInput.setLayout(new GridLayout());

        Label label = new Label(waitingOnInput, SWT.NONE);
        GridData gridData = new GridData();
        gridData.grabExcessHorizontalSpace = true;
        gridData.grabExcessVerticalSpace = true;
        gridData.horizontalAlignment = SWT.CENTER;
        gridData.verticalAlignment = SWT.CENTER;
        label.setLayoutData(gridData);
        label.setText(Messages.tabOffsetsComposite_waitingOnInput);

        calculationsReady = new Composite(this, SWT.NONE);
        FormLayout formLayout = new FormLayout();
        formLayout.marginTop = GuiConstants.FORM_LAYOUT_MARGIN;
        formLayout.marginLeft = GuiConstants.FORM_LAYOUT_MARGIN;
        formLayout.marginRight = GuiConstants.FORM_LAYOUT_MARGIN;
        formLayout.marginBottom = GuiConstants.FORM_LAYOUT_MARGIN;
        calculationsReady.setLayout(formLayout);

        Composite header = new Composite(calculationsReady, SWT.NONE);
        header.setLayout(new FormLayout());
        FormData formData = new FormData();
        formData.top = new FormAttachment(0);
        formData.left = new FormAttachment(0);
        formData.right = new FormAttachment(100);
        header.setLayoutData(formData);

        Composite controls = new Composite(header, SWT.NONE);
        formData = new FormData();
        formData.top = new FormAttachment(0);
        formData.right = new FormAttachment(100);
        controls.setLayoutData(formData);
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        controls.setLayout(gridLayout);

        column = new Combo(controls, SWT.READ_ONLY);
        gridData = new GridData();
        gridData.widthHint = GuiConstants.MEDIUM_COMBO_INPUT_WIDTH;
        gridData.verticalAlignment = SWT.CENTER;
        column.setLayoutData(gridData);
        column.addListener(SWT.Selection, new LoggingAdaptor() {
        		public void loggingHandleEvent(Event event) {
        			refreshGraph(true);
        		}
        });

        absolute = new Button(controls, SWT.TOGGLE);
        gridData = new GridData();
        gridData.verticalAlignment = SWT.CENTER;
        absolute.setLayoutData(gridData);
        absolute.setText(Messages.tabOffsetsComposite_absolute);
        absolute.addListener(SWT.Selection, new LoggingAdaptor() {
	        	public void loggingHandleEvent(Event event) {
	        		refreshGraph(true);
	        	}
        });

        label = new Label(header, SWT.WRAP);
        formData = new FormData();
        formData.top = new FormAttachment(0);
        formData.left = new FormAttachment(0);
        formData.right = new FormAttachment(controls);
        label.setLayoutData(formData);
        label.setText(Messages.tabOffsetsComposite_description);

        graph = new Graph(calculationsReady, SWT.NONE);
        formData = new FormData();
        formData.top = new FormAttachment(header);
        formData.left = new FormAttachment(0);
        formData.right = new FormAttachment(100);
        formData.bottom = new FormAttachment(100);
        graph.setLayoutData(formData);
        graph.setVerticalAxisShowLabel(true);
        graph.setVerticalAxisShowValues(true);
        graph.setHorizontalAxisLabel(Messages.tabOffsetsComposite_horizontalLabel);
        graph.setHorizontalAxisShowLabel(true);
        graph.setHorizontalAxisShowValues(false);

		stackLayout.topControl = waitingOnInput;
		layout();

		UserCache.getInstance().userListGet(this);
		StandardCache.getInstance().standardListGet(this);
		SciConstantCache.getInstance().sciConstantGet(SciConstantNames.δ18O_VPDB_VSMOW, this);

		StandardCache.getInstance().addListener(this);
		SciConstantCache.getInstance().addListener(this);
	}

	@Override
	protected void handleDispose() {
		UserCache.getInstance().removeListener(this);
		StandardCache.getInstance().removeListener(this);
		SciConstantCache.getInstance().removeListener(this);
	}

	private void refreshGraph(boolean autoScale) {
		graph.setRedraw(false);

		graph.removeAllDrawableObjects();
		graph.addDrawableObjectFirst(new LineWithoutEnds(0, 0, ColorCache.getColor(getDisplay(), ColorCache.BLACK), null));

		HashSet<Integer> alreadyGraphedStandardExpectation = new HashSet<Integer>();

		String columnName = null;
		StandardParameter standardParameter = null;
		IsotopicScale isotopicScale = null;

		int selectionIndex = column.getSelectionIndex();

		if (selectionIndex != -1) {
			columnName = comparableStandardOutputs.get(selectionIndex).getColumnName();
			standardParameter = comparableStandardOutputs.get(selectionIndex).getComparableStandardParameter();
			isotopicScale = comparableStandardOutputs.get(selectionIndex).getIsotopicScale();
		}

		if (columnName != null && corrIntervalScratchPad != null) {
			for (ReplicatePad replicatePad : corrIntervalScratchPad.getChildren()) {
				double date = (double) (replicatePad.getDate() / 1000);

				Object value = replicatePad.getValue(columnName);
				Double doubleValue = null;

				if (value instanceof Accumulator) {
					doubleValue = ((Accumulator) value).getMeanStdDevSampleAndStdError()[0];

				} else if (value instanceof Integer) {
					doubleValue = ((Integer) value).doubleValue();

				} else if (value instanceof Double) {
					doubleValue = (Double) value;
				}

				if (doubleValue == null) {
					continue;
				}

				Object disabled = replicatePad.getValue(Pad.DISABLED);
				boolean booleanDisabled = disabled != null && ((Boolean) disabled) == true;

				PointDesign pointDesign = getPointDesign(replicatePad.getSourceId(), booleanDisabled);

				if (pointDesign == null) {
					continue;
				}

				ArrayList<String> tooltipList = new ArrayList<String>();
				StandardListItem standardListItem = standardList.get(replicatePad.getSourceId());
				
				if (standardListItem != null) {
					String name = standardListItem.getName();

					if (booleanDisabled) {
						name += " " + Messages.tabOffsetsComposite_disabled;
					}

					tooltipList.add(name);
				}

				String timeZone = LoginInfoCache.getInstance().getPreferences().getTimeZoneId();
				boolean showTimeZone = LoginInfoCache.getInstance().getPreferences().getShowTimeZone();
				tooltipList.add(DateFormat.format(replicatePad.getDate(), timeZone, showTimeZone, false));

				Standard standard = standards.get(replicatePad.getSourceId());

				if (standard == null) {
					continue;
				}

				NumericValue numericValue = standard.getValues().get(standardParameter.ordinal());

				if (numericValue == null || δ18O_VPDB_VSMOW == null) {
					continue;
				}

				IsotopicScale currentIsotopicScale = IsotopicScale.values()[numericValue.getDescription()];
				Double expected = currentIsotopicScale.convert(numericValue.getValue(), isotopicScale, δ18O_VPDB_VSMOW);

				if (expected == null) {
					continue;
				}

				tooltipList.add(Messages.tabOffsetsComposite_measured + doubleValue.toString());
				tooltipList.add(Messages.tabOffsetsComposite_expected + expected.toString());

				if (!absolute.getSelection()) {
					doubleValue = doubleValue - expected;
					tooltipList.add(Messages.tabOffsetsComposite_difference + doubleValue);
			        graph.setVerticalAxisLabel(MessageFormat.format(Messages.tabOffsetsComposite_verticalRelativeLabel, standardParameter, standardParameter));

				} else {
					if (!alreadyGraphedStandardExpectation.contains(standard.getId())) {
						graph.addDrawableObjectFirst(new LineWithoutEnds(0.0d, expected, Double.NaN, expected, ColorCache.getColorFromPalette(getDisplay(), standard.getColorId()), null));
						alreadyGraphedStandardExpectation.add(standard.getId());					
					}

					tooltipList.add(Messages.tabOffsetsComposite_difference + (doubleValue - expected));
			        graph.setVerticalAxisLabel(MessageFormat.format(Messages.tabOffsetsComposite_verticalAbsoluteLabel, standardParameter));
				}

				Point point = new Point(date, doubleValue, pointDesign);
				point.setTooltip(tooltipList.toArray(new String[tooltipList.size()]));
				point.setInfluencesAutoscale(!booleanDisabled);

				graph.addDrawableObjectLast(point);
			}
		}

		if (autoScale) {
			graph.autoScale();
		}

		graph.setRedraw(true);
	}

	private PointDesign getPointDesign(int sourceId, boolean faded) {
		PointDesign pointDesign = faded ? fadedPointDesigns.get(sourceId) : pointDesigns.get(sourceId);

		if (pointDesign == null) {
			if (StandardCache.getInstance().standardGet(sourceId, this) == Command.UNDEFINED_ID) {
				pointDesign = faded ? fadedPointDesigns.get(sourceId) : pointDesigns.get(sourceId);
			}
		}

		return pointDesign;
	}

	public void setCorrIntervalInfo(AnalysisCompiled analysisCompiled, ScratchPad<ReplicatePad> corrIntervalScratchPad) {
		this.corrIntervalScratchPad = corrIntervalScratchPad;

		if (analysisCompiled != null) {
			int currentlySelectedIndex = -1;
			String currentlySelectedColumnName = column.getSelectionIndex() == -1 ? null : column.getItem(column.getSelectionIndex());
			comparableStandardOutputs.clear();
			column.removeAll();
			TreeSet<String> availableColumns = corrIntervalScratchPad.getAllColumns();

			for (ComparableStandardOutput comparableStandardOutput : new ComparableStandardOutputs(analysisCompiled).getComparableStandardOutputs()) {
				if (availableColumns.contains(comparableStandardOutput.getColumnName())) {					
					if (comparableStandardOutput.getColumnName().equals(currentlySelectedColumnName)) {
						currentlySelectedIndex = comparableStandardOutputs.size();
					}

					column.add(comparableStandardOutput.getColumnName());
					comparableStandardOutputs.add(comparableStandardOutput);
				}
			}

			if (currentlySelectedIndex != -1) {
				column.select(currentlySelectedIndex);
			} else {
				column.select(0);
			}
		}

		if (analysisCompiled == null || corrIntervalScratchPad == null) {
			stackLayout.topControl = waitingOnInput;
			layout();
			return;
		}

		refreshGraph(true);

		stackLayout.topControl = calculationsReady;
		layout();
	}

	@Override
	public boolean stillCallabled() {
		return !isDisposed();
	}

	@Override
	public void sciConstantGetCompleted(int commandId, SciConstant sciConstant) {
		if (sciConstant.getEnumeration() == SciConstantNames.δ18O_VPDB_VSMOW) {
			δ18O_VPDB_VSMOW = sciConstant.getValue();
			refreshGraph(false);
		}
	}

	@Override
	public void sciConstantUpdated(int commandId, SciConstant sciConstant) {
		if (sciConstant.getEnumeration() == SciConstantNames.δ18O_VPDB_VSMOW) {
			δ18O_VPDB_VSMOW = sciConstant.getValue();
			refreshGraph(false);
		}
	}

	@Override
	public void sciConstantGetError(int commandId, String message) {
		getEasotopePart().raiseError(message);
	}

	@Override
	public void standardListGetCompleted(int commandId, StandardList standardList) {
		this.standardList = standardList;
		refreshGraph(false);
	}

	@Override
	public void standardListUpdated(int commandId, StandardList standardList) {
		this.standardList = standardList;
		refreshGraph(false);
	}

	@Override
	public void standardListGetError(int commandId, String message) {
		getEasotopePart().raiseError(message);
	}

	private void newStandard(Standard standard) {
		standards.put(standard.getId(), standard);

		Color color = ColorCache.getColorFromPalette(getDisplay(), standard.getColorId());
		pointDesigns.put(standard.getId(), new PointDesign(getDisplay(), color, PointStyle.values()[standard.getShapeId()]));

		color = ColorCache.getFadedColorFromPalette(getDisplay(), standard.getColorId());
		fadedPointDesigns.put(standard.getId(), new PointDesign(getDisplay(), color, PointStyle.values()[standard.getShapeId()]));
	}

	@Override
	public void standardGetCompleted(int commandId, Standard standard) {
		newStandard(standard);

		if (commandId != Command.UNDEFINED_ID) {
			refreshGraph(false);
		}
	}

	@Override
	public void standardUpdated(int commandId, Standard standard) {
		newStandard(standard);
		refreshGraph(false);
	}

	@Override
	public void standardGetError(int commandId, String message) {
		getEasotopePart().raiseError(message);
	}
}
