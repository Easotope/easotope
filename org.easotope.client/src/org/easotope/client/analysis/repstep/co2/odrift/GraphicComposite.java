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

package org.easotope.client.analysis.repstep.co2.odrift;

import org.easotope.client.Messages;
import org.easotope.client.core.part.EasotopePart;
import org.easotope.shared.analysis.repstep.co2.odrift.Calculator;
import org.easotope.shared.analysis.repstep.superclass.drift.VolatileKeys;
import org.eclipse.swt.widgets.Composite;


public class GraphicComposite extends org.easotope.client.analysis.repstep.superclass.drift.GraphicComposite {
	public GraphicComposite(EasotopePart easotopePart, Composite parent, int style) {
		super(easotopePart, parent, style);
	}

	@Override
	public VolatileKeys getVolatileKeys() {
		return new VolatileKeys(Calculator.class.getName());
	}

	@Override
	public String getHorizontalLabel() {
		return Messages.co2ODriftGraphicComposite_horizontalLabel;
	}

	@Override
	public String getVerticalLabel() {
		return Messages.co2ODriftGraphicComposite_verticalLabel;
	}
}
