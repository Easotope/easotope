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

package org.easotope.client.rawdata.scan.widget.scanfile;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.Vector;

import org.easotope.client.Messages;
import org.easotope.client.core.GuiConstants;
import org.easotope.client.core.Icons;
import org.easotope.client.core.adaptors.LoggingAdaptor;
import org.easotope.client.core.widgets.VCombo;
import org.easotope.shared.rawdata.InputParameter;
import org.easotope.shared.rawdata.ScanFile;
import org.easotope.shared.rawdata.tables.ScanFileParsedV2;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabItem;

public class ByMassWidget extends Composite implements BackgroundSelectorListener {
	private TabItem tabItem = null;
	private InputParameter inputParameter = null;
	private TreeSet<ScanFile> scanFiles = null;

	private VCombo referenceChannel;
	private HashMap<Integer,Integer> referenceChannelToIndex = new HashMap<Integer,Integer>();
	private HashMap<Integer,Integer> indexToReferenceChannel = new HashMap<Integer,Integer>();
	private HashMap<Integer,String> indexToMzString = new HashMap<Integer,String>();
	private VCombo degreeOfFit;
	private Button showRegression = null;
	private Composite graphComposite = null;
	private BackgroundSelector backgroundSelector = null;
	private PblRegression pblRegression = null;

	private Vector<ByMassWidgetListener> listeners = new Vector<ByMassWidgetListener>();

	public ByMassWidget(Composite parent, int style) {
		super(parent, style);
		FormLayout formLayout = new FormLayout();
		formLayout.marginHeight = GuiConstants.FORM_LAYOUT_MARGIN;
		formLayout.marginWidth = GuiConstants.FORM_LAYOUT_MARGIN;
		setLayout(formLayout);

		Composite controlComposite = new Composite(this, SWT.NONE);
		FormData formData = new FormData();
		formData.top = new FormAttachment(0);
		formData.left = new FormAttachment(0);
		formData.right = new FormAttachment(100);
		controlComposite.setLayoutData(formData);
		GridLayout gridLayout = new GridLayout();
		gridLayout.marginWidth = 0;
		gridLayout.numColumns = 5;
		controlComposite.setLayout(gridLayout);

		Label label = new Label(controlComposite, style);
		GridData gridData = new GridData();
		gridData.verticalAlignment = SWT.CENTER;
		label.setLayoutData(gridData);
		label.setText(Messages.byMassWidget_referenceChannelLabel);

		referenceChannel = new VCombo(controlComposite, SWT.READ_ONLY);
		gridData = new GridData();
		gridData.verticalAlignment = SWT.CENTER;
		gridData.widthHint = GuiConstants.MEDIUM_COMBO_INPUT_WIDTH;
		referenceChannel.setLayoutData(gridData);
		referenceChannel.addListener(SWT.Selection, new LoggingAdaptor() {
			@Override
			public void loggingHandleEvent(Event event) {
				pblRegression.setReferenceChannel(indexToReferenceChannel.get(referenceChannel.getSelectionIndex()), indexToMzString.get(referenceChannel.getSelectionIndex()));
				
				if (!pblRegression.isValid() && showRegression.getSelection()) {
					showRegression.setSelection(false);

					((StackLayout) graphComposite.getLayout()).topControl = backgroundSelector;
					graphComposite.layout();
				}

				showRegression.setEnabled(pblRegression.isValid());

				tabItem.setImage(pblRegression.isValid() ? Icons.getBlank(getDisplay()) : Icons.getWarning(getDisplay()));
				tabItem.setToolTipText(pblRegression.isValid() ? null : Messages.byMassWidget_warningToolTip);
				tabItem.getParent().redraw();

				broadcastRangesChanged();
			}
		});

		label = new Label(controlComposite, style);
		gridData = new GridData();
		gridData.verticalAlignment = SWT.CENTER;
		label.setLayoutData(gridData);
		label.setText(Messages.byMassWidget_degreeOfFitLabel);

		degreeOfFit = new VCombo(controlComposite, SWT.READ_ONLY);
		gridData = new GridData();
		gridData.verticalAlignment = SWT.CENTER;
		gridData.widthHint = GuiConstants.MEDIUM_COMBO_INPUT_WIDTH;
		degreeOfFit.setLayoutData(gridData);
		degreeOfFit.addListener(SWT.Selection, new LoggingAdaptor() {
			@Override
			public void loggingHandleEvent(Event event) {
				pblRegression.setDegreeOfFit(degreeOfFit.getSelectionIndex() + 1);
				
				if (!pblRegression.isValid() && showRegression.getSelection()) {
					showRegression.setSelection(false);

					((StackLayout) graphComposite.getLayout()).topControl = backgroundSelector;
					graphComposite.layout();
				}

				showRegression.setEnabled(pblRegression.isValid());

				tabItem.setImage(pblRegression.isValid() ? Icons.getBlank(getDisplay()) : Icons.getWarning(getDisplay()));
				tabItem.setToolTipText(pblRegression.isValid() ? null : Messages.byMassWidget_warningToolTip);
				tabItem.getParent().redraw();

				broadcastRangesChanged();
			}
		});
		degreeOfFit.add(Messages.byMassWidget_degreeOne);
		degreeOfFit.add(Messages.byMassWidget_degreeTwo);

		showRegression = new Button(controlComposite, SWT.TOGGLE);
		gridData = new GridData();
		gridData.verticalAlignment = SWT.CENTER;
		gridData.horizontalAlignment = SWT.RIGHT;
		gridData.grabExcessHorizontalSpace = true;
		showRegression.setLayoutData(gridData);
		showRegression.setText(Messages.byMassWidget_showRegression);
		showRegression.addListener(SWT.Selection, new LoggingAdaptor() {
			@Override
			public void loggingHandleEvent(Event event) {
				((StackLayout) graphComposite.getLayout()).topControl = showRegression.getSelection() ? pblRegression : backgroundSelector;
				graphComposite.layout();
			}
		});

		graphComposite = new Composite(this, SWT.NONE);
		formData = new FormData();
		formData.top = new FormAttachment(controlComposite);
		formData.left = new FormAttachment(0);
		formData.right = new FormAttachment(100);
		formData.bottom = new FormAttachment(100);
		graphComposite.setLayoutData(formData);
		graphComposite.setLayout(new StackLayout());

		backgroundSelector = new BackgroundSelector(graphComposite, SWT.NONE);
		backgroundSelector.setVerticalAxisShowLabel(true);
		backgroundSelector.setVerticalAxisShowValues(true);
		backgroundSelector.setHorizontalAxisLabel(Messages.byMassWidget_horizontalLabel);
		backgroundSelector.setHorizontalAxisShowLabel(true);
		backgroundSelector.setHorizontalAxisShowValues(true);
		backgroundSelector.addListener(this);

		pblRegression = new PblRegression(graphComposite, SWT.NONE);

		((StackLayout) graphComposite.getLayout()).topControl = backgroundSelector;
		graphComposite.layout();
	}

	public void setInputParameter(InputParameter inputParameter, String label) {
		this.inputParameter = inputParameter;
		backgroundSelector.setVerticalAxisLabel(MessageFormat.format(Messages.byMassWidget_verticalLabel, label));
		backgroundSelector.setInputParameter(inputParameter);
		pblRegression.setGraphLabel(label);
	}

	public void setTabItem(TabItem tabItem) {
		this.tabItem = tabItem;
	}

	public void setInitialValues(TreeSet<ScanFile> scanFiles, Integer[] channelToMZX10, HashMap<Integer,double[]> allReferenceChannelOnPeakValues) {
		this.scanFiles = scanFiles;
		backgroundSelector.setScanFiles(scanFiles);

		int indexToSelect = -1;
		String oldMz = indexToMzString.get(referenceChannel.getSelectionIndex());
		referenceChannel.deselectAll();
		referenceChannel.removeAll();

		referenceChannelToIndex.clear();
		indexToReferenceChannel.clear();
		indexToMzString.clear();

		for (int channel=0; channel<channelToMZX10.length; channel++) {
			if (channelToMZX10[channel] == null) {
				continue;
			}

			String mz = (channelToMZX10[channel] % 10 == 0) ? String.valueOf(channelToMZX10[channel] / 10) : String.valueOf(channelToMZX10[channel] / 10.0d);

			referenceChannelToIndex.put(channel, referenceChannel.getItemCount());
			indexToReferenceChannel.put(referenceChannel.getItemCount(), channel);
			indexToMzString.put(referenceChannel.getItemCount(), mz);

			if (mz.equals(oldMz)) {
				indexToSelect = referenceChannel.getItemCount();
			}

			referenceChannel.add(mz);
		}

		if (indexToSelect == -1) {
			pblRegression.setReferenceChannel(null, "");
		} else {
			pblRegression.setReferenceChannel(indexToReferenceChannel.get(indexToSelect), indexToMzString.get(indexToSelect));
			referenceChannel.select(indexToSelect);
		}

		pblRegression.setAllReferenceChannelOnPeakValues(allReferenceChannelOnPeakValues);

		calculateRegression();
	}

	public void setAllReferenceChannelOnPeakValues(HashMap<Integer,double[]> allReferenceChannelOnPeakValues) {
		pblRegression.setAllReferenceChannelOnPeakValues(allReferenceChannelOnPeakValues);
		calculateRegression();
	}

	@Override
	public void setEnabled(boolean enabled) {
		backgroundSelector.setEnabled(enabled);
	}

	void setValues(Integer referenceChannel, int degreeOfFit, double leftX1, double leftX2, double rightX1, double rightX2) {
		if (referenceChannel == null) {
			this.referenceChannel.deselectAll();

		} else {
			Integer index = referenceChannelToIndex.get(referenceChannel);

			if (index == null) {
				this.referenceChannel.deselectAll();

			} else {
				this.referenceChannel.select(index);
				pblRegression.setReferenceChannel(referenceChannel, indexToMzString.get(index));
			}
		}

		if (degreeOfFit < 1) {
			this.degreeOfFit.deselectAll();
		} else {
			this.degreeOfFit.select(degreeOfFit-1);
		}
		pblRegression.setDegreeOfFit(degreeOfFit);

		backgroundSelector.getLeftXRangeSelection().setX1(leftX1);
		backgroundSelector.getLeftXRangeSelection().setX2(leftX2);
		backgroundSelector.getRightXRangeSelection().setX1(rightX1);
		backgroundSelector.getRightXRangeSelection().setX2(rightX2);
		backgroundSelector.clearAndRedraw();

		calculateRegression();
	}

	private void calculateRegression() {
		double leftX1 = backgroundSelector.getLeftXRangeSelection().getX1();
		double leftX2 = backgroundSelector.getLeftXRangeSelection().getX2();
		boolean leftXValid = !Double.isNaN(leftX1) && !Double.isNaN(leftX2) && leftX1 != leftX2;
		double minLeftX = Math.min(leftX1,leftX2);
		double maxLeftX = Math.max(leftX1,leftX2);

		double rightX1 = backgroundSelector.getRightXRangeSelection().getX1();
		double rightX2 = backgroundSelector.getRightXRangeSelection().getX2();
		boolean rightXValid = !Double.isNaN(rightX1) && !Double.isNaN(rightX2) && rightX1 != rightX2;
		double minRightX = Math.min(rightX1,rightX2);
		double maxRightX = Math.max(rightX1,rightX2);

		int intensity = 0;
		double[] intensityAverages = new double[scanFiles.size()];

		for (ScanFile scanFile : scanFiles) {
			double intensityCount = 0.0d;
			ScanFileParsedV2 scanFileParsed = scanFile.getScanFileParsed();
			Double[] values = scanFileParsed.getMeasurements().get(inputParameter);

			if (values == null) {
				continue;
			}
			
			double m = (scanFileParsed.getToVoltage()-scanFileParsed.getFromVoltage()) / (values.length-1);
			double b = scanFileParsed.getFromVoltage();

			for (int i=0; i<values.length; i++) {
				double x = m * i + b;

				if ((leftXValid && x > minLeftX && x < maxLeftX) || (rightXValid && x > minRightX && x < maxRightX)) {
					intensityAverages[intensity] += values[i];
					intensityCount += 1.0d;
				}
			}
			
			intensityAverages[intensity] /= intensityCount;
			intensity++;
		}

		pblRegression.setIntensityAverages(intensityAverages);

		if (!pblRegression.isValid() && showRegression.getSelection()) {
			showRegression.setSelection(false);

			((StackLayout) graphComposite.getLayout()).topControl = backgroundSelector;
			graphComposite.layout();
		}

		showRegression.setEnabled(pblRegression.isValid());

		tabItem.setImage(pblRegression.isValid() ? Icons.getBlank(getDisplay()) : Icons.getWarning(getDisplay()));
		tabItem.setToolTipText(pblRegression.isValid() ? null : Messages.byMassWidget_warningToolTip);
		tabItem.getParent().redraw();
	}

	@Override
	public void backgroundSelectionChanged() {
		calculateRegression();
		broadcastRangesChanged();
	}

	public BackgroundSelector getBackgroundSelector() {
		return backgroundSelector;
	}

	public PblRegression getPblRegression() {
		return pblRegression;
	}

	InputParameter getInputParameter() {
		return inputParameter;
	}

	public Integer getReferenceChannel() {
		int index = referenceChannel.getSelectionIndex();

		if (index == -1) {
			return null;
		}

		return indexToReferenceChannel.get(index);
	}

	public int getDegreeOfFit() {
		return degreeOfFit.getSelectionIndex() + 1;
	}

	@Override
	public void dispose() {
		listeners.clear();
		super.dispose();
	}

	private void broadcastRangesChanged() {
		for (ByMassWidgetListener listener : listeners) {
			listener.rangesChanged();
		}
	}

	public void addListener(ByMassWidgetListener listener) {
		listeners.add(listener);
	}
}
