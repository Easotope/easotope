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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.Vector;

import org.easotope.client.Messages;
import org.easotope.client.core.ColorCache;
import org.easotope.client.core.GuiConstants;
import org.easotope.client.core.Icons;
import org.easotope.client.core.adaptors.LoggingAdaptor;
import org.easotope.client.core.adaptors.LoggingPaintAdaptor;
import org.easotope.client.core.adaptors.LoggingTraverseAdaptor;
import org.easotope.client.core.widgets.DateTimeLabel;
import org.easotope.client.core.widgets.VText;
import org.easotope.client.core.widgets.graph.drawables.Curve;
import org.easotope.shared.rawdata.InputParameter;
import org.easotope.shared.rawdata.ScanFile;
import org.easotope.shared.rawdata.tables.ScanFileParsedV2;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabItem;

public class ByFileWidget extends Composite implements OnPeakSelectorListener {
	private TabItem tabItem = null;

	private Composite controlComposite = null;
	private Combo fileSelector = null;
	private VText onPeakX1 = null;
	private VText onPeakX2 = null;
	private Canvas onPeakWarning = null;
	private DateTimeLabel dateLabel = null;
	private boolean drawWithLines = true;
	private OnPeakSelector onPeakSelector = null;

	private ArrayList<InputParameter> inputParameters = null;
	private HashMap<Integer,ScanFile> indexToScanFile = new HashMap<Integer,ScanFile>();

	private Vector<ByFileWidgetListener> listeners = new Vector<ByFileWidgetListener>();

	public ByFileWidget(Composite parent, int style) {
		super(parent, style);
		FormLayout formLayout = new FormLayout();
		formLayout.marginHeight = GuiConstants.FORM_LAYOUT_MARGIN;
		formLayout.marginWidth = GuiConstants.FORM_LAYOUT_MARGIN;
		setLayout(formLayout);

		final Image warningImage = Icons.getWarning(parent.getDisplay());

		controlComposite = new Composite(this, SWT.NONE);
		FormData formData = new FormData();
		formData.top = new FormAttachment(0);
		formData.left = new FormAttachment(0);
		formData.right = new FormAttachment(100);
		controlComposite.setLayoutData(formData);
		GridLayout gridLayout = new GridLayout();
		gridLayout.marginWidth = 0;
		gridLayout.numColumns = 8;
		controlComposite.setLayout(gridLayout);

		Label fileLabel = new Label(controlComposite, SWT.NONE);
		GridData gridData = new GridData();
		gridData.verticalAlignment = SWT.CENTER;
		fileLabel.setLayoutData(gridData);
		fileLabel.setText(Messages.byFileWidget_fileLabel);

		fileSelector = new Combo(controlComposite, SWT.READ_ONLY);
		gridData = new GridData();
		gridData.verticalAlignment = SWT.CENTER;
		gridData.widthHint = GuiConstants.LONG_COMBO_INPUT_WIDTH;
		fileSelector.setLayoutData(gridData);
		fileSelector.addListener(SWT.Selection, new LoggingAdaptor() { 
			@Override
			public void loggingHandleEvent(Event event) {
				setScanFile(indexToScanFile.get(fileSelector.getSelectionIndex()), inputParameters);
			}
		});

		Label onPeakX1Label = new Label(controlComposite, SWT.NONE);
		gridData = new GridData();
		gridData.verticalAlignment = SWT.CENTER;
		onPeakX1Label.setLayoutData(gridData);
		onPeakX1Label.setText(Messages.byFileWidget_onPeakX1Label);

		onPeakX1 = new VText(controlComposite, SWT.BORDER);
		gridData = new GridData();
		gridData.verticalAlignment = SWT.CENTER;
		gridData.widthHint = GuiConstants.TINY_TEXT_INPUT_WIDTH;
		onPeakX1.setLayoutData(gridData);
		onPeakX1.setTextLimit(GuiConstants.SHORT_TEXT_INPUT_LIMIT);
		onPeakX1.addTraverseListener(new LoggingTraverseAdaptor() {
			@Override
			public void loggingKeyTraversed(TraverseEvent e) {
				if (e.character == SWT.CR) {
					((VText) e.widget).traverse(SWT.TRAVERSE_TAB_NEXT);
				}
			}
		});
		onPeakX1.addListener(SWT.Verify, new LoggingAdaptor() {
			public void loggingHandleEvent(Event e) {
				String string = e.text;
				char[] chars = new char[string.length()];
				string.getChars(0, chars.length, chars, 0);
				for (int i = 0; i < chars.length; i++) {
					if (!Character.isDigit(chars[i]) && chars[i] != '.' && chars[i] != '-') {
						e.doit = false;
						return;
					}
				}
			}
		});
		onPeakX1.addListener(SWT.FocusOut, new LoggingAdaptor() { 
			@Override
			public void loggingHandleEvent(Event event) {
				setOnPeakFromTextFields();
			}
		});

		Label onPeakX2Label = new Label(controlComposite, SWT.NONE);
		gridData = new GridData();
		gridData.verticalAlignment = SWT.CENTER;
		onPeakX2Label.setLayoutData(gridData);
		onPeakX2Label.setText(Messages.byFileWidget_onPeakX2Label);

		onPeakX2 = new VText(controlComposite, SWT.BORDER);
		gridData = new GridData();
		gridData.verticalAlignment = SWT.CENTER;
		gridData.widthHint = GuiConstants.TINY_TEXT_INPUT_WIDTH;
		onPeakX2.setLayoutData(gridData);
		onPeakX2.setTextLimit(GuiConstants.SHORT_TEXT_INPUT_LIMIT);
		onPeakX2.addTraverseListener(new LoggingTraverseAdaptor() {
			@Override
			public void loggingKeyTraversed(TraverseEvent e) {
				if (e.character == SWT.CR) {
					((VText) e.widget).traverse(SWT.TRAVERSE_TAB_NEXT);
				}
			}
		});
		onPeakX2.addListener(SWT.Verify, new LoggingAdaptor() {
			public void loggingHandleEvent(Event e) {
				String string = e.text;
				char[] chars = new char[string.length()];
				string.getChars(0, chars.length, chars, 0);
				for (int i = 0; i < chars.length; i++) {
					if (!Character.isDigit(chars[i]) && chars[i] != '.' && chars[i] != '-') {
						e.doit = false;
						return;
					}
				}
			}
		});
		onPeakX2.addListener(SWT.FocusOut, new LoggingAdaptor() { 
			@Override
			public void loggingHandleEvent(Event event) {
				setOnPeakFromTextFields();
			}
		});

		onPeakWarning = new Canvas(controlComposite, SWT.NONE);
		gridData = new GridData();
		gridData.widthHint = warningImage.getImageData().width;
		gridData.heightHint = warningImage.getImageData().height;
		onPeakWarning.setLayoutData(gridData);
		onPeakWarning.setVisible(false);
		onPeakWarning.addPaintListener(new LoggingPaintAdaptor() {
			public void loggingPaintControl(PaintEvent e) {
				e.gc.setAntialias(SWT.ON);
				e.gc.drawImage(warningImage, 0, 0);
			}
		});

		dateLabel = new DateTimeLabel(controlComposite, SWT.NONE);
		gridData = new GridData();
		gridData.verticalAlignment = SWT.CENTER;
		gridData.horizontalAlignment = SWT.RIGHT;
		gridData.grabExcessHorizontalSpace = true;
		dateLabel.setLayoutData(gridData);

		onPeakSelector = new OnPeakSelector(this, SWT.NONE);
		formData = new FormData();
		formData.top = new FormAttachment(controlComposite);
		formData.left = new FormAttachment(0);
		formData.right = new FormAttachment(100);
		formData.bottom = new FormAttachment(100);
		onPeakSelector.setLayoutData(formData);
		onPeakSelector.setVerticalAxisLabel(Messages.byFileWidget_verticalLabel);
		onPeakSelector.setVerticalAxisShowLabel(true);
		onPeakSelector.setVerticalAxisShowValues(true);
		onPeakSelector.setHorizontalAxisLabel(Messages.byFileWidget_horizontalLabel);
		onPeakSelector.setHorizontalAxisShowLabel(true);
		onPeakSelector.setHorizontalAxisShowValues(true);
		onPeakSelector.addListener(this);
	}

	public void setTabItem(TabItem tabItem) {
		this.tabItem = tabItem;
		setTextFieldsAndWarning();
	}

	public void setScanFiles(TreeSet<ScanFile> scanFiles, ArrayList<InputParameter> inputParameters) {
		this.inputParameters = inputParameters;
		ScanFile selectedScanFile = indexToScanFile.get(fileSelector.getSelectionIndex());
		Long selectedScanFileDate = selectedScanFile != null ? selectedScanFile.getScanFileParsed().getDate() : null;

		fileSelector.removeAll();
		Integer previouslySelectedIndex = null;

		for (ScanFile scanFile : scanFiles) {
			indexToScanFile.put(fileSelector.getItemCount(), scanFile);

			if (selectedScanFileDate != null && scanFile.getScanFileParsed().getDate() == selectedScanFileDate) {
				previouslySelectedIndex = fileSelector.getItemCount();
			}

			fileSelector.add(scanFile.getRawFile().getOriginalName());
		}

		if (previouslySelectedIndex != null) {
			fileSelector.select(previouslySelectedIndex);

		} else if (fileSelector.getItemCount() != 0) {
			fileSelector.select(0);
			setScanFile(indexToScanFile.get(0), inputParameters);
		}
	}

	private void setScanFile(ScanFile scanFile, ArrayList<InputParameter> inputParameters) {
		ScanFileParsedV2 scanFileParsed = scanFile.getScanFileParsed();
		HashMap<InputParameter,Double[]> measurements = scanFileParsed.getMeasurements();

		onPeakSelector.removeAllDrawableObjects();

		int count = 0;
		for (InputParameter param : inputParameters) {
			Double[] data = measurements.get(param);

			if (data == null) {
				return;
			}
			
			double[] x = new double[data.length];
			double[] y = new double[data.length];

			double m = (scanFileParsed.getToVoltage()-scanFileParsed.getFromVoltage()) / (data.length-1);
			double b = scanFileParsed.getFromVoltage();

			for (int i=0; i<data.length; i++) {
				x[i] = m * i + b;
				y[i] = data[i];
			}

			Curve curve = new Curve(x, y, ColorCache.getColorFromPalette(getDisplay(), count), new String[] { param.toString() });
			curve.setAddXToTooltip(true);
			curve.setAddYToTooltip(true);
			curve.setWithLines(drawWithLines);

			onPeakSelector.addDrawableObjectLast(curve);

			count++;
		}

		onPeakSelector.autoScale();

		dateLabel.setDate(indexToScanFile.get(fileSelector.getSelectionIndex()).getScanFileParsed().getDate());
		controlComposite.layout();
	}

	@Override
	public void setEnabled(boolean enabled) {
		onPeakX1.setEnabled(enabled);
		onPeakX2.setEnabled(enabled);
		onPeakSelector.setEnabled(enabled);
	}

	private void setWarningInTabLabel(String message) {
		tabItem.setImage(message == null ? Icons.getBlank(getDisplay()) : Icons.getWarning(getDisplay()));
		tabItem.setToolTipText(message);
		tabItem.getParent().redraw();
	}

	private void setWarningIcon(boolean setGraph) {
		if (onPeakX1.getText().length() == 0 && onPeakX2.getText().length() == 0) {
			if (setGraph) {
				onPeakSelector.getXRangeSelection().setX1(Double.NaN);
				onPeakSelector.getXRangeSelection().setX2(Double.NaN);
			}

			setWarningInTabLabel(Messages.byFileWidget_noRangeSelected);
			return;
		}

		if ((onPeakX1.getText().length() != 0 && onPeakX2.getText().length() == 0) || (onPeakX1.getText().length() == 0 && onPeakX2.getText().length() != 0)) {
			if (setGraph) {
				onPeakSelector.getXRangeSelection().setX1(Double.NaN);
				onPeakSelector.getXRangeSelection().setX2(Double.NaN);
			}

			onPeakWarning.setVisible(true);
			onPeakWarning.setToolTipText(Messages.byFileWidget_rangeNotComplete);

			setWarningInTabLabel(Messages.byFileWidget_noRangeSelected);
			return;
		}

		double onPeakX1Parsed;
		double onPeakX2Parsed;

		try {
			onPeakX1Parsed = Double.parseDouble(onPeakX1.getText().trim());
			onPeakX2Parsed = Double.parseDouble(onPeakX2.getText().trim());

		} catch (Exception e) {
			if (setGraph) {
				onPeakSelector.getXRangeSelection().setX1(Double.NaN);
				onPeakSelector.getXRangeSelection().setX2(Double.NaN);
			}

			onPeakWarning.setVisible(true);
			onPeakWarning.setToolTipText(Messages.byFileWidget_invalidNumber);

			setWarningInTabLabel(Messages.byFileWidget_noRangeSelected);
			return;
		}

		if (onPeakX1Parsed >= onPeakX2Parsed) {
			if (setGraph) {
				onPeakSelector.getXRangeSelection().setX1(Double.NaN);
				onPeakSelector.getXRangeSelection().setX2(Double.NaN);
			}

			onPeakWarning.setVisible(true);
			onPeakWarning.setToolTipText(Messages.byFileWidget_fromNotLessThanTo);

			setWarningInTabLabel(Messages.byFileWidget_noRangeSelected);
			return;
		}

		if (setGraph) {
			onPeakSelector.getXRangeSelection().setX1(onPeakX1Parsed);
			onPeakSelector.getXRangeSelection().setX2(onPeakX2Parsed);
		}

		onPeakWarning.setVisible(false);
		onPeakWarning.setToolTipText(null);
		setWarningInTabLabel(null);
	}

	private void setOnPeakFromTextFields() {
		setWarningIcon(true);
		onPeakSelector.clearAndRedraw();		
		broadcastRangesChanged();
	}

	private void setTextFieldsAndWarning() {
		double graphicalOnPeakX1 = onPeakSelector.getXRangeSelection().getX1();
		double graphicalOnPeakX2 = onPeakSelector.getXRangeSelection().getX2();

		if (Double.isNaN(graphicalOnPeakX1) || Double.isNaN(graphicalOnPeakX2)) {
			graphicalOnPeakX1 = Double.NaN;
			graphicalOnPeakX2 = Double.NaN;
		}

		if (graphicalOnPeakX1 >= graphicalOnPeakX2) {
			double temp = graphicalOnPeakX1;
			graphicalOnPeakX1 = graphicalOnPeakX2;
			graphicalOnPeakX2 = temp;
		}

		if (Double.isNaN(graphicalOnPeakX1)) {
			onPeakX1.setText("");
		} else {
			onPeakX1.setText(String.valueOf(graphicalOnPeakX1));
		}

		if (Double.isNaN(graphicalOnPeakX2)) {
			onPeakX2.setText("");
		} else {
			onPeakX2.setText(String.valueOf(graphicalOnPeakX2));
		}

		setWarningIcon(false);
	}

	@Override
	public void onPeakSelectionChanged() {
		setTextFieldsAndWarning();
		broadcastRangesChanged();
	}

	public double getOnPeakX1() {
		double graphicalOnPeakX1 = onPeakSelector.getXRangeSelection().getX1();
		double graphicalOnPeakX2 = onPeakSelector.getXRangeSelection().getX2();

		if (Double.isNaN(graphicalOnPeakX1) || Double.isNaN(graphicalOnPeakX2)) {
			return Double.NaN;
		}

		return (graphicalOnPeakX1 < graphicalOnPeakX2) ? graphicalOnPeakX1 : graphicalOnPeakX2;
	}

	public double getOnPeakX2() {
		double graphicalOnPeakX1 = onPeakSelector.getXRangeSelection().getX1();
		double graphicalOnPeakX2 = onPeakSelector.getXRangeSelection().getX2();

		if (Double.isNaN(graphicalOnPeakX1) || Double.isNaN(graphicalOnPeakX2)) {
			return Double.NaN;
		}

		return (graphicalOnPeakX1 > graphicalOnPeakX2) ? graphicalOnPeakX1 : graphicalOnPeakX2;
	}

	public void setOnPeakXs(double onPeakX1, double onPeakX2) {
		onPeakSelector.getXRangeSelection().setX1(onPeakX1);
		onPeakSelector.getXRangeSelection().setX2(onPeakX2);
		onPeakSelector.clearAndRedraw();
		setTextFieldsAndWarning();
	}

	@Override
	public void dispose() {
		listeners.clear();
		super.dispose();
	}

	private void broadcastRangesChanged() {
		for (ByFileWidgetListener listener : listeners) {
			listener.rangesChanged();
		}
	}

	public void addListener(ByFileWidgetListener listener) {
		listeners.add(listener);
	}
}
