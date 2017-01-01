/*
 * Copyright © 2016-2017 by Devon Bowen.
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
import org.easotope.client.core.widgets.graph.MenuItemListener;
import org.easotope.client.core.widgets.graph.drawables.LineWithoutEnds;
import org.easotope.client.core.widgets.graph.drawables.Point;
import org.easotope.client.rawdata.navigator.PartManager;
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
import org.easotope.shared.core.DoubleTools;
import org.easotope.shared.core.NumericValue;
import org.easotope.shared.core.cache.logininfo.LoginInfoCache;
import org.easotope.shared.core.scratchpad.Accumulator;
import org.easotope.shared.core.scratchpad.Pad;
import org.easotope.shared.core.scratchpad.ReplicatePad;
import org.easotope.shared.core.scratchpad.ScratchPad;
import org.easotope.shared.core.tables.Preferences;
import org.easotope.shared.math.QQPlot;
import org.easotope.shared.math.Statistics;
import org.easotope.shared.rawdata.InputParameter;
import org.easotope.shared.rawdata.cache.input.InputCache;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

public class TabOffsetsComposite extends EasotopeComposite implements SciConstantCacheSciConstantGetListener, StandardCacheStandardListGetListener, StandardCacheStandardGetListener {
	private int RELATIVE_GRAPH = 0;
	private int ABSOLUTE_GRAPH = 1;
	private int QQPLOT_GRAPH = 2;

	private StackLayout stackLayout;

	private Combo column;
	private Combo graphType;
	private TimeRangeGraph graph;
	private Label mean;
	private Label stddev;
	private Label stderr;
	private Combo standard;
	private Combo run;

	private Composite waitingOnInput;
	private Composite calculationsReady;
	private Composite bottomComposite;

	private ScratchPad<ReplicatePad> corrIntervalScratchPad;
	private ArrayList<ComparableStandardOutput> comparableStandardOutputs = new ArrayList<ComparableStandardOutput>();
	private HashMap<Integer,Integer> indexToSourceId = new HashMap<Integer,Integer>();
	private HashMap<Integer,String> indexToRun = new HashMap<Integer,String>();

	private Double δ18O_VPDB_VSMOW;
	private StandardList standardList;
	private HashMap<Integer,Standard> standards = new HashMap<Integer,Standard>();
	private enum PointType { NORMAL, FADED, UNSELECTED };
	private HashMap<Integer,PointDesign> pointDesigns = new HashMap<Integer,PointDesign>();
	private HashMap<Integer,PointDesign> fadedPointDesigns = new HashMap<Integer,PointDesign>();
	private HashMap<Integer,PointDesign> unselectedPointDesigns = new HashMap<Integer,PointDesign>();

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

        graphType = new Combo(controls, SWT.READ_ONLY);
        gridData = new GridData();
        gridData.verticalAlignment = SWT.CENTER;
        graphType.setLayoutData(gridData);
        graphType.add(Messages.tabOffsetsComposite_relative);
        graphType.add(Messages.tabOffsetsComposite_absolute);
        graphType.add(Messages.tabOffsetsComposite_qqplot);
        graphType.select(0);
        graphType.addListener(SWT.Selection, new LoggingAdaptor() {
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

        bottomComposite = new Composite(calculationsReady, SWT.NONE);
        formData = new FormData();
        formData.left = new FormAttachment(0);
        formData.right = new FormAttachment(100);
        formData.bottom = new FormAttachment(100);
        bottomComposite.setLayoutData(formData);
        gridLayout = new GridLayout();
        gridLayout.numColumns = 11;
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        gridLayout.horizontalSpacing = 0;
        bottomComposite.setLayout(gridLayout);

        label = new Label(bottomComposite, SWT.NONE);
        label.setText(Messages.tabOffsetsComposite_average);
        gridData = new GridData();
        gridData.verticalAlignment = SWT.CENTER;
        label.setLayoutData(gridData);

        mean = new Label(bottomComposite, SWT.NONE);
        gridData = new GridData();
        gridData.verticalAlignment = SWT.CENTER;
        mean.setLayoutData(gridData);

        label = new Label(bottomComposite, SWT.NONE);
        label.setText(Messages.tabOffsetsComposite_stddev);
        gridData = new GridData();
        gridData.verticalAlignment = SWT.CENTER;
        gridData.horizontalIndent = GuiConstants.HORIZONTAL_LABEL_INDENT;
        label.setLayoutData(gridData);

        stddev = new Label(bottomComposite, SWT.NONE);
        gridData = new GridData();
        gridData.verticalAlignment = SWT.CENTER;
        stddev.setLayoutData(gridData);

        label = new Label(bottomComposite, SWT.NONE);
        label.setText(Messages.tabOffsetsComposite_stderr);
        gridData = new GridData();
        gridData.verticalAlignment = SWT.CENTER;
        gridData.horizontalIndent = GuiConstants.HORIZONTAL_LABEL_INDENT;
        label.setLayoutData(gridData);

        stderr = new Label(bottomComposite, SWT.NONE);
        gridData = new GridData();
        gridData.verticalAlignment = SWT.CENTER;
        stderr.setLayoutData(gridData);

        Label center = new Label(bottomComposite, SWT.NONE);
        gridData = new GridData();
        gridData.horizontalAlignment = SWT.FILL;
        gridData.grabExcessHorizontalSpace = true;
        center.setLayoutData(gridData);

        label = new Label(bottomComposite, SWT.NONE);
        gridData = new GridData();
        gridData.verticalAlignment = SWT.CENTER;
        label.setLayoutData(gridData);
        label.setText(Messages.tabOffsetsComposite_limitTo);

        standard = new Combo(bottomComposite, SWT.READ_ONLY);
        gridData = new GridData();
        gridData.verticalAlignment = SWT.CENTER;
        gridData.widthHint = GuiConstants.SHORT_COMBO_INPUT_WIDTH;
        standard.setLayoutData(gridData);
        standard.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				refreshGraph((graphType.getSelectionIndex() == QQPLOT_GRAPH) ? true : false);
			}
        });

        label = new Label(bottomComposite, SWT.NONE);
        label.setText(Messages.tabOffsetsComposite_run);
        gridData = new GridData();
        gridData.verticalAlignment = SWT.CENTER;
        gridData.horizontalIndent = GuiConstants.HORIZONTAL_LABEL_INDENT;
        label.setLayoutData(gridData);

        run = new Combo(bottomComposite, SWT.READ_ONLY);
        gridData = new GridData();
        gridData.verticalAlignment = SWT.CENTER;
        gridData.widthHint = GuiConstants.SHORT_COMBO_INPUT_WIDTH;
        run.setLayoutData(gridData);
        run.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				graph.setTimeRange(null, null);
				refreshGraph((graphType.getSelectionIndex() == QQPLOT_GRAPH) ? true : false);
			}
        });

        graph = new TimeRangeGraph(calculationsReady, SWT.NONE);
        formData = new FormData();
        formData.top = new FormAttachment(header);
        formData.left = new FormAttachment(0);
        formData.right = new FormAttachment(100);
        formData.bottom = new FormAttachment(bottomComposite, -GuiConstants.INTER_WIDGET_GAP);
        graph.setLayoutData(formData);
        graph.setVerticalAxisShowLabel(true);
        graph.setVerticalAxisShowValues(true);
        graph.setHorizontalAxisShowLabel(true);
        graph.setHorizontalAxisShowValues(false);
        graph.setXRangeEnabled(true);
        graph.addListener(new TimeRangeSelectorListener() {
			@Override
			public void timeRangeSelectionStarted() {
				run.select(0);
				refreshGraph(false);
			}

			@Override
			public void timeRangeSelectionComplete() {
				refreshGraph(false);
			}
        });

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

		Statistics statistics = new Statistics();
		QQPlot qqplot = new QQPlot();

		if (columnName != null && corrIntervalScratchPad != null) {
			double startTime = Double.isNaN(graph.getStartTime()) ? Double.NaN : graph.getStartTime() * 1000;
			double endTime = Double.isNaN(graph.getEndTime()) ? Double.NaN : graph.getEndTime() * 1000;

			for (ReplicatePad replicatePad : corrIntervalScratchPad.getChildren()) {
				double date = (double) (replicatePad.getDate() / 1000);

				Object value = replicatePad.getValue(columnName);
				Double doubleValue = null;
				Double statsValue = null;

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

				PointType pointType = PointType.NORMAL;

				Object disabled = replicatePad.getValue(Pad.DISABLED);
				boolean booleanDisabled = (disabled != null && ((Boolean) disabled) == true);
				pointType = booleanDisabled ? PointType.FADED : pointType;

				Integer selectedSourceId = indexToSourceId.get(standard.getSelectionIndex());
				pointType = (selectedSourceId != null && selectedSourceId != replicatePad.getSourceId()) ? PointType.UNSELECTED : pointType;  

				String selectedRun = indexToRun.get(run.getSelectionIndex());
				pointType = (selectedRun != null && !selectedRun.equals(replicatePad.getValue(InputParameter.Run.toString()))) ? PointType.UNSELECTED : pointType;  

				pointType = (!Double.isNaN(startTime) && (replicatePad.getDate() < startTime || replicatePad.getDate() > endTime)) ? PointType.UNSELECTED : pointType;

				PointDesign pointDesign = getPointDesign(replicatePad.getSourceId(), pointType);

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

				if (graphType.getSelectionIndex() == RELATIVE_GRAPH) {
					doubleValue = doubleValue - expected;
					statsValue = doubleValue;
					graph.setHorizontalAxisShowValues(false);
					graph.setXRangeEnabled(true);
					tooltipList.add(Messages.tabOffsetsComposite_difference + doubleValue);
			        graph.setHorizontalAxisLabel(Messages.tabOffsetsComposite_horizontalLabel);
			        graph.setVerticalAxisLabel(MessageFormat.format(Messages.tabOffsetsComposite_verticalRelativeLabel, standardParameter, standardParameter));

				} else if (graphType.getSelectionIndex() == ABSOLUTE_GRAPH) {
					statsValue = doubleValue - expected;
					graph.setHorizontalAxisShowValues(false);
					graph.setXRangeEnabled(true);

					if (!alreadyGraphedStandardExpectation.contains(standard.getId())) {
						graph.addDrawableObjectFirst(new LineWithoutEnds(0.0d, expected, Double.NaN, expected, ColorCache.getColorFromPalette(getDisplay(), standard.getColorId()), null));
						alreadyGraphedStandardExpectation.add(standard.getId());					
					}

					tooltipList.add(Messages.tabOffsetsComposite_difference + (doubleValue - expected));
			        graph.setHorizontalAxisLabel(Messages.tabOffsetsComposite_horizontalLabel);
			        graph.setVerticalAxisLabel(MessageFormat.format(Messages.tabOffsetsComposite_verticalAbsoluteLabel, standardParameter));

				} else {
					doubleValue = doubleValue - expected;
					statsValue = doubleValue;
					graph.setHorizontalAxisShowValues(true);
					graph.setXRangeEnabled(false);
			        graph.setHorizontalAxisLabel(MessageFormat.format(Messages.tabOffsetsComposite_horizontalQQLabel, standardParameter, standardParameter));
			        graph.setVerticalAxisLabel(MessageFormat.format(Messages.tabOffsetsComposite_verticalRelativeLabel, standardParameter, standardParameter));
				}
 
				Point point = new Point(date, doubleValue, pointDesign);
				point.setTooltip(tooltipList.toArray(new String[tooltipList.size()]));
				point.setInfluencesAutoscale(!booleanDisabled);
				replicatePad.getValue(Pad.ID);
				final int replicateId = replicatePad.getReplicateId();
				point.addMenuItem(new MenuItemListener() {
					@Override
					public void handleEvent(Event event) {
						PartManager.showRawDataPerspective(getEasotopePart());
						PartManager.openStandardReplicate(getEasotopePart(), replicateId);
					}

					@Override
					public String getName() {
						return Messages.tabOffsetsComposite_openReplicate;
					}
				});
				if (LoginInfoCache.getInstance().getPermissions().isCanEditAllReplicates()) {
					point.addMenuItem(new MenuItemListener() {
						@Override
						public void handleEvent(Event event) {
							InputCache.getInstance().replicateDisabledStatusUpdate(replicateId, !booleanDisabled);
						}
	
						@Override
						public String getName() {
							return booleanDisabled ? Messages.tabOffsetsComposite_enableReplicate : Messages.tabOffsetsComposite_disableReplicate;
						}
					});
				}

				if (graphType.getSelectionIndex() == QQPLOT_GRAPH) {
					if (pointType == PointType.NORMAL) {
						qqplot.addValue(doubleValue, point);
					}
				} else {
					graph.addDrawableObjectLast(point);
				}

				if (pointType == PointType.NORMAL) {
					statistics.addNumber(statsValue);
				}
			}
		}

		if (graphType.getSelectionIndex() == QQPLOT_GRAPH) {
			graph.addDrawableObjectFirst(new LineWithoutEnds(1.0, 0.0, ColorCache.getColor(getDisplay(), ColorCache.BLACK), null));

			for (org.easotope.shared.math.QQPlot.Point p : qqplot.getPoints()) {
				Point point = (Point) p.getObject();
				point.setX(p.getTheoreticalQuantile());
				graph.addDrawableObjectLast(point);
			}

		} else {
			graph.addDrawableObjectFirst(new LineWithoutEnds(0, 0, ColorCache.getColor(getDisplay(), ColorCache.BLACK), null));
		}

		Preferences preferences = LoginInfoCache.getInstance().getPreferences();
		String numberAsString = DoubleTools.format(statistics.getMean(), preferences.getLeadingExponent(), preferences.getForceExponent());
		mean.setText(numberAsString);

		numberAsString = DoubleTools.format(statistics.getStandardDeviationSample(), preferences.getLeadingExponent(), preferences.getForceExponent());
		stddev.setText(numberAsString);

		numberAsString = DoubleTools.format(statistics.getStandardErrorSample(), preferences.getLeadingExponent(), preferences.getForceExponent());
		stderr.setText(numberAsString);

		bottomComposite.layout();
		
		if (autoScale) {
			graph.autoScale();
		}

		graph.setRedraw(true);
	}

	private PointDesign getPointDesign(int sourceId, PointType pointType) {
		PointDesign pointDesign = null;

		switch (pointType) {
			case NORMAL:
				pointDesign = pointDesigns.get(sourceId);
				break;
			case FADED:
				pointDesign = fadedPointDesigns.get(sourceId);
				break;
			case UNSELECTED:
				pointDesign = unselectedPointDesigns.get(sourceId);
				break;
		}

		if (pointDesign == null) {
			if (StandardCache.getInstance().standardGet(sourceId, this) == Command.UNDEFINED_ID) {
				switch (pointType) {
					case NORMAL:
						pointDesign = pointDesigns.get(sourceId);
						break;
					case FADED:
						pointDesign = fadedPointDesigns.get(sourceId);
						break;
					case UNSELECTED:
						pointDesign = unselectedPointDesigns.get(sourceId);
						break;
				}
			}
		}

		return pointDesign;
	}

	public void setCorrIntervalInfo(AnalysisCompiled analysisCompiled, ScratchPad<ReplicatePad> corrIntervalScratchPad) {
		this.corrIntervalScratchPad = corrIntervalScratchPad;

		if (corrIntervalScratchPad != null) {
			TreeSet<NameAndId> namesAndIds = new TreeSet<NameAndId>();
			HashSet<String> seenRunNames = new HashSet<String>();
			ArrayList<String> runNames = new ArrayList<String>();

			for (ReplicatePad replicatePad : corrIntervalScratchPad.getChildren()) {
				if (standardList != null) {
					int sourceId = replicatePad.getSourceId();
					StandardListItem standardListItem = standardList.get(sourceId);
	
					if (standardListItem != null) {
						namesAndIds.add(new NameAndId(standardListItem.getName(), sourceId));
					}
				}

				String run = (String) replicatePad.getValue(InputParameter.Run.toString());

				if (run != null && !seenRunNames.contains(run)) {
					seenRunNames.add(run);
					runNames.add(run);
				}
			}

			int standardIndex = standard.getSelectionIndex();
			String previousStandard = (standard.isEnabled() && standardIndex != -1) ? standard.getItem(standardIndex) : null;

			indexToSourceId.clear();
			standard.removeAll();

			if (!namesAndIds.isEmpty()) {
				int newSelectionIndex = -1;
				standard.add("");

				for (NameAndId nameAndId : namesAndIds) {
					if (nameAndId.getName().equals(previousStandard)) {
						newSelectionIndex = standard.getItemCount();
					}
	
					indexToSourceId.put(standard.getItemCount(), nameAndId.getId());
					standard.add(nameAndId.getName());
				}
				
				if (newSelectionIndex != -1) {
					standard.select(newSelectionIndex);
				}
			}

			standard.setEnabled(!namesAndIds.isEmpty());

			int runIndex = run.getSelectionIndex();
			String previousRun = (run.isEnabled() && runIndex != -1) ? run.getItem(runIndex) : null;

			indexToRun.clear();
			run.removeAll();

			if (!runNames.isEmpty()) {
				int newSelectionIndex = -1;
				run.add("");

				for (String name : runNames) {
					if (name.equals(previousRun)) {
						newSelectionIndex = run.getItemCount();
					}

					indexToRun.put(run.getItemCount(), name);
					run.add(name);
				}

				if (newSelectionIndex != -1) {
					run.select(newSelectionIndex);
				}
			}

			run.setEnabled(!runNames.isEmpty());
		}

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

		color = ColorCache.getColor(getDisplay(), ColorCache.LIGHT_GREY);
		unselectedPointDesigns.put(standard.getId(), new PointDesign(getDisplay(), color, PointStyle.values()[standard.getShapeId()]));
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

	public class NameAndId implements Comparable<NameAndId> {
		private String name;
		private int id;
		
		public NameAndId(String name, int id) {
			this.name = name;
			this.id = id;
		}
		
		public String getName() {
			return name;
		}

		public int getId() {
			return id;
		}

		@Override
		public int compareTo(NameAndId that) {
			return this.name.compareTo(that.name);
		}
	}
}
