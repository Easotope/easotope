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

package org.easotope.shared.admin;

public enum SciConstantNames {
	λ("Terrestrial mass-dependent fractionation parameter between 17O and 18O", 0.5164, "Gonfianti et al, 1993 (not verified)"),
    R13_VPDB("Abundance ratio of 13C/12C for VPDB", 0.0112372, "Craig H. Geochim. Cosmochim. Acta 1957; 12: 133. (not verified)"),
    R17_VSMOW("Abundance ratio of 17O/16O for VSMOW", 0.0003799, "Gonfianti et al, 1993 (not verified)"),
    R18_VSMOW("Abundance ratio of 18O/16O for VSMOW", 0.0020052, "Gonfianti et al, 1993 (not verified)"),
    δ18O_VPDB_VSMOW("Factor for converting d18O VPDB to VSMOW", 30.92, "NIST 1992 (not verified)"),
	KELVIN_CELCIUS("Offset for converting Kelvin to Celcius", 273.15, "Wikipedia. http://en.wikipedia.org/wiki/Kelvin");

	private String description;
	private double defaultValue;
	private String defaultReference;

	SciConstantNames(String description, double defaultValue, String defaultReference) {
		this.description = description;
		this.defaultValue = defaultValue;
		this.defaultReference = defaultReference;
	}

	public String getDescription() {
		return description;
	}

	public double getDefaultValue() {
		return defaultValue;
	}

	public String getDefaultReference() {
		return defaultReference;
	}
}
