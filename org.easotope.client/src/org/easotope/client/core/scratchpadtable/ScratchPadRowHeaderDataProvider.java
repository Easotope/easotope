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

package org.easotope.client.core.scratchpadtable;

import org.easotope.shared.core.scratchpad.AcquisitionPad;
import org.easotope.shared.core.scratchpad.CyclePad;
import org.easotope.shared.core.scratchpad.Pad;
import org.easotope.shared.core.scratchpad.ProjectPad;
import org.easotope.shared.core.scratchpad.ReplicatePad;
import org.easotope.shared.core.scratchpad.SamplePad;
import org.easotope.shared.core.scratchpad.UserPad;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;


class ScratchPadRowHeaderDataProvider implements IDataProvider {
	private String[] header = new String[0];

	void setPads(Pad[] pads) {
		header = new String[pads.length];

		int totalU = 0;
		int totalP = 0;
		int totalS = 0;
		int totalR = 0;
		int totalA = 0;
		int totalC = 0;

		for (int i=0; i<pads.length; i++) {
			if (pads[i] instanceof UserPad) {
				header[i] = "U" + (++totalU);
				totalP = 0;
				totalS = 0;
				totalR = 0;
				totalA = 0;
				totalC = 0;

			} else if (pads[i] instanceof ProjectPad) {
				header[i] = "P" + (++totalP);
				totalS = 0;
				totalR = 0;
				totalA = 0;
				totalC = 0;

			} else if (pads[i] instanceof SamplePad) {
				header[i] = "S" + (++totalS);
				totalR = 0;
				totalA = 0;
				totalC = 0;

			} else if (pads[i] instanceof ReplicatePad) {
				header[i] = "R" + (++totalR);
				totalA = 0;
				totalC = 0;

			} else if (pads[i] instanceof AcquisitionPad) {
				header[i] = "A" + (++totalA);
				totalC = 0;

			} else if (pads[i] instanceof CyclePad) {
				header[i] = "C" + (++totalC);
			}
		}
	}

	@Override
	public int getRowCount() {
		return header.length;
	}

	@Override
	public int getColumnCount() {
		return 1;
	}

	@Override
	public Object getDataValue(int columnIndex, int rowIndex) {
		// this is unfortunately called with -1 when the column count is 0
		// is that true for rows, too?
		return rowIndex >= 0 && rowIndex < header.length ? header[rowIndex] : null;
	}

	@Override
	public void setDataValue(int columnIndex, int rowIndex, Object newValue) {
		throw new UnsupportedOperationException();
	}
}
