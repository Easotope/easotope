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

package org.easotope.client.core.widgets;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

public class VDouble extends Text {
	private String oldString = null;
	private Double oldNumber = null;

	private String lastParsedString = null;
	private Double lastParsedResult = null;

	public VDouble(Composite parent, int style) {
		super(parent, style);
	}

	@Override
	public void setText(String string) {
		// if caller is swt, then don't do our special stuff
		if (new Throwable().getStackTrace()[1].getClassName().startsWith("org.eclipse.swt")) {
			super.setText(string);
			return;
		}

		throw new RuntimeException("don't call setText() on VDouble");
	}

	@Override
	public String getText() {
		throw new RuntimeException("don't call getText() on VDouble");
	}

	public void setNumber(Double number) {
		if (number == null) {
			oldString = null;
			oldNumber = null;
			super.setText("");
		} else {
			oldString = String.valueOf(number);
			oldNumber = number;
			super.setText(oldString);
		}
	}

	public void setNumberButLeaveRevertValue(Double number) {
		if (number == null) {
			super.setText("");
		} else {
			super.setText(String.valueOf(number));
		}
	}
	
	public Double getNumber() {
		String text = super.getText().trim();

		if (text.equals(lastParsedString)) {
			return lastParsedResult;
		}

		lastParsedString = text;

		if (lastParsedString.length() == 0) {
			lastParsedResult = null;

		} else {
			lastParsedResult = Double.NaN;
	
			try {
				lastParsedResult = Double.valueOf(text);
			} catch (NumberFormatException e) {
				// ignore
			}
		}

		return lastParsedResult;
	}

	public Double getOldNumber() {
		return oldNumber;
	}

	public boolean hasChanged() {
		Double number = getNumber();

		if (number == null) {
			return (oldString != null && oldString.length() != 0);
		}

		if (Double.isNaN(number)) {
			return oldNumber == null || !Double.isNaN(oldNumber);
		}

		if (Double.isInfinite(number)) {
			return oldNumber == null || !Double.isInfinite(oldNumber) || (number < 0 && oldNumber > 0) || (number > 0 && oldNumber < 0);
		}

		return !String.valueOf(number).equals(oldString);
	}

	public void revert() {
		super.setText(oldString == null ? "" : oldString);
	}

	@Override
	protected void checkSubclass() {
		// remove check to allow subclassing
	}
}
