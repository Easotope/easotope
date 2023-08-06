/*
 * Copyright © 2016-2023 by Devon Bowen.
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

package org.easotope.client.core.widgets;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.easotope.client.Messages;
import org.easotope.client.core.ColorCache;
import org.easotope.client.core.Icons;
import org.easotope.client.core.adaptors.LoggingAdaptor;
import org.easotope.client.core.adaptors.LoggingControlAdaptor;
import org.easotope.client.core.adaptors.LoggingPaintAdaptor;
import org.easotope.shared.core.NumericValue;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Text;


public class NumericValueList extends ScrolledComposite {
	private Composite contentComposite;

	private String valueName;
	private String descriptionName;
	private boolean withReferences = false;
	private boolean requireAtLeastOne = true;
	private HashMap<Integer,String> labelPossibilities = new HashMap<Integer,String>();
	private HashMap<Integer,HashMap<Integer,String>> descriptionPossibilities;
	private Map<Integer,NumericValue> originalValues = null;

	private boolean isEnabled = true;
	private Boolean hasChanged = null;
	private boolean hasError = false;
	private Vector<OneLineOfWidgets> allLinesOfWidgets = new Vector<OneLineOfWidgets>();
	private HashMap<Integer,NumericValue> currentValues = new HashMap<Integer,NumericValue>();

	private Vector<NumericValueListListener> listeners = new Vector<NumericValueListListener>();

	public NumericValueList(Composite parent, int style) {
		super(parent, style | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		
        contentComposite = new Composite(this, SWT.NONE);
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 1;
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        gridLayout.verticalSpacing = 1;
        contentComposite.setLayout(gridLayout);
        contentComposite.setBackground(ColorCache.getColor(getDisplay(), ColorCache.WHITE));
 
        setContent(contentComposite);
        setExpandHorizontal(true);
        setExpandVertical(true);
        addControlListener(new LoggingControlAdaptor() {
        		@Override
        		public void loggingControlResized(ControlEvent e) {
        			NumericValueList.this.layout(true, true);
        			NumericValueList.this.setMinSize(contentComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
        			getParent().notifyListeners(SWT.Resize, new Event());
        		}
        });
	}

	public void setValueName(String valueName) {
		this.valueName = valueName;
	}

	public void setDescriptionName(String descriptionName) {
		this.descriptionName = descriptionName;
	}

	public boolean isWithReferences() {
		return withReferences;
	}

	public void setWithReferences(boolean withReferences) {
		this.withReferences = withReferences;
	}

	public boolean isRequireAtLeastOne() {
		return requireAtLeastOne;
	}

	public void setRequireAtLeastOne(boolean requireAtLeastOne) {
		this.requireAtLeastOne = requireAtLeastOne;
	}

	public void setLabelPossibilities(HashMap<Integer,String> labelPossibilities) {
		this.labelPossibilities = labelPossibilities;
		resetLabelPossibilities();
	}

	public void setDescriptionPossibilities(HashMap<Integer,HashMap<Integer,String>> descriptionPossibilities) {
		this.descriptionPossibilities = descriptionPossibilities;
	}

	private void resetLabelPossibilities() {
		HashMap<Integer,String> remainingLabelPossibilities = new HashMap<Integer,String>(labelPossibilities);

		for (OneLineOfWidgets oneLineOfWidgets : allLinesOfWidgets) {
			int selectedInteger = oneLineOfWidgets.getSelectedInteger();
			remainingLabelPossibilities.remove(selectedInteger);
		}

		for (OneLineOfWidgets oneLineOfWidgets : allLinesOfWidgets) {
			HashMap<Integer,String> thisOnesLabelPossibilities = new HashMap<Integer,String>(remainingLabelPossibilities);

			int selectedInteger = oneLineOfWidgets.getSelectedInteger();

			if (labelPossibilities.containsKey(selectedInteger)) {
				thisOnesLabelPossibilities.put(selectedInteger, labelPossibilities.get(selectedInteger));
			}

			oneLineOfWidgets.setLabelPossibilities(thisOnesLabelPossibilities);
		}
	}

	public HashMap<Integer, NumericValue> getValues() {
		return currentValues;
	}

	public void setValues(Map<Integer,NumericValue> numericValues) {
		originalValues = numericValues;

		for (OneLineOfWidgets oneLineOfWidgets : allLinesOfWidgets) {
			oneLineOfWidgets.dispose();
		}

		allLinesOfWidgets.clear();

		if (numericValues != null) {
			for (int integer : numericValues.keySet()) {
				NumericValue numericValue = numericValues.get(integer);

				OneLineOfWidgets oneLineOfWidgets = new OneLineOfWidgets(contentComposite, descriptionPossibilities, withReferences, isEnabled);
				oneLineOfWidgets.setNumericValue(integer, numericValue);

				allLinesOfWidgets.add(oneLineOfWidgets);
			}
		}

		somethingChanged(true, false);
	}

	public void revert() {
		setValues(originalValues);
	}

	public boolean hasError() {
		return hasError;
	}

	public boolean hasChanged() {
		if (hasChanged != null) {
			return hasChanged;
		}

		if (originalValues == null) {
			hasChanged = (currentValues.size() != 0);
			return hasChanged;
		}

		if (originalValues.size() != currentValues.size()) {
			hasChanged = true;
			return hasChanged;
		}

		for (Integer integer : originalValues.keySet()) {
			if (!currentValues.containsKey(integer)) {
				hasChanged = true;
				return hasChanged;
			}

			NumericValue originalNumericValue = originalValues.get(integer);

			if (originalNumericValue == null || !originalNumericValue.equals(currentValues.get(integer))) {
				hasChanged = true;
				return hasChanged;
			}
		}

		hasChanged = false;
		return hasChanged;
	}

	private void deleteLine(OneLineOfWidgets oneLineOfWidgets) {
		oneLineOfWidgets.dispose();
		allLinesOfWidgets.remove(oneLineOfWidgets);
		somethingChanged(true, true);
	}

	private void somethingChanged(boolean forceLayout, boolean externalEvent) {
		boolean hasAnEmpty = false;
		hasError = false;

		for (OneLineOfWidgets oneLineOfWidgets : allLinesOfWidgets) {
			if (oneLineOfWidgets.hasError()) {
				hasError = true;
			}

			if (oneLineOfWidgets.isEmpty()) {
				hasAnEmpty = true;
			}
		}

		if (!hasAnEmpty && allLinesOfWidgets.size() != labelPossibilities.size()) {
			allLinesOfWidgets.add(new OneLineOfWidgets(contentComposite, descriptionPossibilities, withReferences, isEnabled));
		}

		resetLabelPossibilities();

		if (requireAtLeastOne && allLinesOfWidgets.size() == 1 && allLinesOfWidgets.get(0).isEmpty()) {
			hasError = true;
			String message = MessageFormat.format(Messages.numericList_allLeastOne, new Object[] { valueName });
			allLinesOfWidgets.get(0).forceErrorMessage(message);
		}

		if (forceLayout || !hasAnEmpty) {
			contentComposite.layout(true,true);
			this.layout(true, true);
			NumericValueList.this.setMinSize(contentComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
			getParent().notifyListeners(SWT.Resize, new Event());
		}

		hasChanged = null;
		currentValues.clear();

		for (OneLineOfWidgets oneLineOfWidgets : allLinesOfWidgets) {
			int integer = oneLineOfWidgets.getSelectedInteger();
			
			if (integer != -1) {
				NumericValue numericValue = oneLineOfWidgets.getNumericValue();
				currentValues.put(integer, numericValue);
			}
		}

		if (externalEvent) {
			for (NumericValueListListener listener : listeners) {
				listener.widgetSelected();
			}
		}
	}

	@Override
	public void setEnabled(boolean enabled) {
		for (OneLineOfWidgets oneLineOfWidgets : allLinesOfWidgets) {
			oneLineOfWidgets.setEnabled(enabled);
		}

		isEnabled = enabled;
	}

	public synchronized void addSelectionListener(NumericValueListListener listener) {
		listeners.add(listener);
	}

	public synchronized void removeSelectionListener(NumericValueListListener listener) {
		listeners.remove(listener);
	}

	private class OneLineOfWidgets extends Composite {
		private int selectedInteger = -1;
		private NumericValue numericValue = null;
		private HashMap<Integer,HashMap<Integer,String>> descriptionPossibilities = null;

		private Button deleteButton;
		private Canvas errorIcon;
		private SortedCombo labelPossibilitiesCombo;
		private SortedCombo descriptionPossibilitiesCombo;
		private Text valueField;
		private Text referenceField;

		public OneLineOfWidgets(Composite composite, HashMap<Integer,HashMap<Integer,String>> descriptionPossibilities, boolean withReferences, boolean initiallyEnabled) {
			super(composite, SWT.NONE);
			this.descriptionPossibilities = descriptionPossibilities;

			setBackground(ColorCache.getColor(getDisplay(), ColorCache.WHITE));

			GridLayout gridLayout = new GridLayout();
			gridLayout.numColumns = 4;
			gridLayout.numColumns = descriptionPossibilities != null ? gridLayout.numColumns + 1 : gridLayout.numColumns;
			gridLayout.numColumns = withReferences ? gridLayout.numColumns + 1 : gridLayout.numColumns;
			setLayout(gridLayout);

			deleteButton = new Button(this, SWT.CHECK);
			GridData gridData = new GridData();
			gridData.verticalAlignment = SWT.CENTER;
			deleteButton.setLayoutData(gridData);
			deleteButton.setVisible(false);
			deleteButton.setEnabled(initiallyEnabled);
			deleteButton.addListener(SWT.Selection, new LoggingAdaptor() {
				@Override
				public void loggingHandleEvent(Event event) {
					NumericValueList.this.deleteLine(OneLineOfWidgets.this);
				}
			});

			final Image errorImage = Icons.getError(getParent().getDisplay());

			errorIcon = new Canvas(this, SWT.NONE);
			gridData = new GridData();
			gridData.verticalAlignment = SWT.CENTER;
			gridData.widthHint = errorImage.getImageData().width;
			gridData.heightHint = errorImage.getImageData().height;
			errorIcon.setLayoutData(gridData);
			errorIcon.setVisible(false);
			errorIcon.setBackground(ColorCache.getColor(getDisplay(), ColorCache.WHITE));
			errorIcon.addPaintListener(new LoggingPaintAdaptor() {
				public void loggingPaintControl(PaintEvent e) {
					e.gc.setAntialias(SWT.ON);
					e.gc.drawImage(errorImage, 0, 0);
				}
			});

			labelPossibilitiesCombo = new SortedCombo(this, SWT.READ_ONLY);
			gridData = new GridData();
			gridData.widthHint = 100;
			gridData.verticalAlignment = SWT.CENTER;
			labelPossibilitiesCombo.setLayoutData(gridData);
			labelPossibilitiesCombo.setPossibilities(labelPossibilities);
			labelPossibilitiesCombo.setEnabled(initiallyEnabled);
			labelPossibilitiesCombo.addListener(SWT.Selection, new LoggingAdaptor() {
				@Override
				public void loggingHandleEvent(Event event) {
					selectedInteger = labelPossibilitiesCombo.getSelectedInteger();
					checkFieldsForChange(true);
					somethingChanged(false, true);
				}
			});

			valueField = new Text(this, SWT.BORDER);
			gridData = new GridData();
			gridData.widthHint = 100;
			gridData.verticalAlignment = SWT.CENTER;
			valueField.setLayoutData(gridData);
			valueField.setVisible(false);
			valueField.setEnabled(initiallyEnabled);
			valueField.addListener(SWT.KeyUp, new LoggingAdaptor() {
				@Override
				public void loggingHandleEvent(Event event) {
					checkFieldsForChange(false);
					somethingChanged(false, true);
				}
			});

			if (descriptionPossibilities != null) {
				descriptionPossibilitiesCombo = new SortedCombo(this, SWT.READ_ONLY);
				gridData = new GridData();
				gridData.widthHint = 100;
				gridData.verticalAlignment = SWT.CENTER;
				descriptionPossibilitiesCombo.setLayoutData(gridData);
				descriptionPossibilitiesCombo.setVisible(false);
				descriptionPossibilitiesCombo.setEnabled(initiallyEnabled);
				descriptionPossibilitiesCombo.addListener(SWT.Selection, new LoggingAdaptor() {
					@Override
					public void loggingHandleEvent(Event event) {
						checkFieldsForChange(false);
						somethingChanged(false, true);
					}
				});
			}

			if (withReferences) {
				referenceField = new Text(this, SWT.BORDER);
				gridData = new GridData();
				gridData.widthHint = 200;
				gridData.verticalAlignment = SWT.CENTER;
				referenceField.setLayoutData(gridData);
				referenceField.setVisible(false);
				referenceField.setEnabled(initiallyEnabled);
				referenceField.addListener(SWT.KeyUp, new LoggingAdaptor() {
					@Override
					public void loggingHandleEvent(Event event) {
						checkFieldsForChange(false);
						somethingChanged(false, true);
					}
				});
			}
		}

		public void setLabelPossibilities(HashMap<Integer,String> possibilities) {
			labelPossibilitiesCombo.setPossibilities(possibilities);
			labelPossibilitiesCombo.selectInteger(selectedInteger);

			checkFieldsForChange(false);
		}

		public void setNumericValue(int integer, NumericValue numericValue) {
			selectedInteger = integer;
			labelPossibilitiesCombo.selectInteger(integer);

			valueField.setText(String.valueOf(numericValue.getValue()));
			valueField.setVisible(true);

			if (descriptionPossibilitiesCombo != null) {
				HashMap<Integer,String> descriptions = descriptionPossibilities.get(labelPossibilitiesCombo.getSelectedInteger());
				descriptionPossibilitiesCombo.setPossibilities(descriptions);
				descriptionPossibilitiesCombo.setVisible(true);
				descriptionPossibilitiesCombo.selectInteger(numericValue.getDescription());
			}

			if (referenceField != null) {
				referenceField.setText(numericValue.getReference().trim());
				referenceField.setVisible(true);
			}

			checkFieldsForChange(true);
		}

		public void forceErrorMessage(String message) {
			errorIcon.setToolTipText(message);
			errorIcon.setVisible(true);
		}

		public int getSelectedInteger() {
			return selectedInteger;
		}

		public NumericValue getNumericValue() {
			return numericValue;
		}

		public boolean isEmpty() {
			String value = valueField.getText().trim();
			int description = descriptionPossibilitiesCombo == null ? -1 : descriptionPossibilitiesCombo.getSelectedInteger();
			String reference = referenceField == null ? "" : referenceField.getText().trim();

			return selectedInteger == -1 && value.isEmpty() && description == -1 && reference.isEmpty(); 
		}

		public boolean hasError() {
			return errorIcon.isVisible();
		}

		private void checkFieldsForChange(boolean labelChanged) {
			if (isEmpty()) {
				numericValue = null;
				errorIcon.setVisible(false);
				return;
			}

			if (selectedInteger != -1 && labelPossibilitiesCombo.getSelectedInteger() == -1) {
				errorIcon.setToolTipText(Messages.numericList_selectionDoesNotExist);
				errorIcon.setVisible(true);
				numericValue = null;
				return;
			}

			if (labelChanged && descriptionPossibilities != null) {
				HashMap<Integer,String> descriptions = descriptionPossibilities.get(labelPossibilitiesCombo.getSelectedInteger());
				descriptionPossibilitiesCombo.setPossibilities(descriptions);

				if (descriptions.size() == 1) {
					Integer key = descriptions.keySet().iterator().next();
					descriptionPossibilitiesCombo.selectInteger(key);
				}

				descriptionPossibilitiesCombo.setVisible(true);
			}

			deleteButton.setVisible(true);
			valueField.setVisible(true);

			if (referenceField != null) {
				referenceField.setVisible(true);
			}

			String value = valueField.getText().trim();

			if (value.isEmpty()) {
				String message = MessageFormat.format(Messages.numericList_valueIsRequired, new Object[] { valueName });
				errorIcon.setToolTipText(message);
				errorIcon.setVisible(true);
				numericValue = null;
				return;
			}

			double parsedValue = 0;

			try {
				parsedValue = Double.valueOf(value);

			} catch (NumberFormatException e) {
				String message = MessageFormat.format(Messages.numericList_valueIsRequired, new Object[] { valueName });
				errorIcon.setToolTipText(message);
				errorIcon.setVisible(true);
				numericValue = null;
				return;
			}

			if (descriptionPossibilitiesCombo != null && descriptionPossibilitiesCombo.getSelectionIndex() == -1) {
				String message = MessageFormat.format(Messages.numericList_descriptionIsRequired, new Object[] { descriptionName });
				errorIcon.setToolTipText(message);
				errorIcon.setVisible(true);
				numericValue = null;
				return;
			}

			errorIcon.setVisible(false);

			if (numericValue == null) {
				numericValue = new NumericValue(parsedValue, descriptionPossibilitiesCombo != null ? descriptionPossibilitiesCombo.getSelectedInteger() : 0, withReferences ? referenceField.getText().trim() : null);
			} else {
				numericValue.setValue(parsedValue);
				numericValue.setDescription(descriptionPossibilitiesCombo != null ? descriptionPossibilitiesCombo.getSelectedInteger() : 0);
				numericValue.setReference(referenceField != null ? referenceField.getText().trim() : null);
			}
		}

		@Override
		public void setEnabled(boolean enabled) {
			deleteButton.setEnabled(enabled);
			labelPossibilitiesCombo.setEnabled(enabled);

			if (descriptionPossibilitiesCombo != null) {
				descriptionPossibilitiesCombo.setEnabled(enabled);
			}

			valueField.setEnabled(enabled);

			if (referenceField != null) {
				referenceField.setEnabled(enabled);
			}
		}

		@Override
		public void dispose() {
			deleteButton.dispose();
			errorIcon.dispose();
			labelPossibilitiesCombo.dispose();

			if (descriptionPossibilitiesCombo != null) {
				descriptionPossibilitiesCombo.dispose();
			}

			valueField.dispose();
			
			if (referenceField != null) { 
				referenceField.dispose();
			}

			super.dispose();
		}
	}
}
