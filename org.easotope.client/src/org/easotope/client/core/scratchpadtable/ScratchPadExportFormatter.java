/*
 * Copyright © 2016-2020 by Devon Bowen.
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

import org.easotope.shared.core.DoubleTools;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.export.excel.DefaultExportFormatter;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;

class ScratchPadExportFormatter extends DefaultExportFormatter {
	@Override
	public Object formatForExport(ILayerCell cell, IConfigRegistry configRegistry) {
		Object obj = super.formatForExport(cell, configRegistry);

		if (obj instanceof String) {
			if ("true".equals(obj.toString())) {
				obj = true;
			} else if ("false".equals(obj.toString().trim())) {
				obj = false;
			}
		}

		if (obj instanceof String) {
			try {
				obj = Integer.valueOf(obj.toString().trim());
			} catch (NumberFormatException e) {
				// ignore
			}
		}

		if (obj instanceof String) {
			try {
				String withoutLeadingExponent = DoubleTools.removeLeadingExponent(obj.toString().trim());
				obj = Double.valueOf(withoutLeadingExponent);
			} catch (NumberFormatException e) {
				// ignore
			}
		}

//		if (obj instanceof String) {
//			try {
//				String timeZone = LoginInfoCache.getInstance().getPreferences().getTimeZoneId();
//				obj = DateFormat.parse(obj.toString().trim(), timeZone, false);
//			} catch (ParseException e) {
//				// ignore
//			}
//		}

		if (obj instanceof String) {
			String value = obj.toString();

			// special
			value = value.replace('δ', 'd');
			value = value.replace('Δ', 'D');
			
			// subscript
			value = value.replace('₂', '2');
			
			// superscript
			value = value.replace('¹', '1');
			value = value.replace('³', '3');
			value = value.replace('⁸', '8');

			obj = value;
		}

		return obj;
	}
}
