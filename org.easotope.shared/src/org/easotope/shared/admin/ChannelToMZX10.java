/*
 * Copyright © 2016-2019 by Devon Bowen.
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

import java.util.ArrayList;
import java.util.HashSet;

import org.easotope.shared.rawdata.InputParameter;

public class ChannelToMZX10 {
	private static HashSet<Integer> knownMZX10s = new HashSet<Integer>();

	private ArrayList<Integer> channelToMZX10 = new ArrayList<Integer>();
	private String errorMessage;

	public ChannelToMZX10(Integer[] channelToMZX10) {
		if (channelToMZX10 != null) {
			for (Integer mzX10 : channelToMZX10) {
				this.channelToMZX10.add(mzX10);
			}
		}
	}

	public ChannelToMZX10(String channelToMZX10) {
		String[] split = channelToMZX10.split(",");

		if (split.length == 1 && split[0].trim().length() == 0) {
			return;
		}

		for (int i=0; i<split.length; i++) {
			split[i] = split[i].trim();

			if (split[i].length() == 0) {
				this.channelToMZX10.add(null);
				continue;
			}

			int decimalIndex = -1;

			for (int j=0; j<split[i].length(); j++) {
				char ch = split[i].charAt(j);

				if ((ch < '0' || ch > '9') && ch != '.') {
					errorMessage = "Malformed input";
					return;
				}

				if (ch == '.') {
					if (decimalIndex != -1) {
						errorMessage = "Malformed input";
						return;

					} else {
						decimalIndex = j;
					}
				}
			}

			if (split[i].charAt(0) == '.') {
				split[i] = "0" + split[i];
				decimalIndex = (decimalIndex == -1) ? -1 : decimalIndex + 1;
			}

			if (decimalIndex != -1 && split[i].charAt(split[i].length()-1) == '0') {
				split[i] = split[i].substring(0, split[i].length()-1);
			}

			if (split[i].charAt(split[i].length()-1) == '.') {
				split[i] = split[i].substring(0, split[i].length()-1);
				decimalIndex = -1;
			}

			String beforeDecimal = (decimalIndex == -1) ? split[i] : split[i].substring(0, decimalIndex);
			String afterDecimal = (decimalIndex == -1) ? "0" : split[i].substring(decimalIndex + 1);

			if (beforeDecimal.length() == 0) {
				beforeDecimal = "0";
			}

			if (afterDecimal.length() == 0) {
				afterDecimal = "0";
			}

			int mzX10 = Integer.parseInt(beforeDecimal) * 10 + Integer.parseInt(afterDecimal);

			if (!knownMZX10s.contains(mzX10)) {
				errorMessage = "MZ value " + beforeDecimal + "." + afterDecimal + " is unknown";
				return;
			}

			this.channelToMZX10.add(mzX10);
		}
	}

	public boolean isValid() {
		return errorMessage == null;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public Integer[] getMZX10s() {
		return channelToMZX10.toArray(new Integer[channelToMZX10.size()]);
	}

	@Override
	public String toString() {
		if (errorMessage != null) {
			return errorMessage;
		}

		if (channelToMZX10.size() == 0) {
			return "";
		}

		boolean leadingNull = false;
		StringBuffer buffer = new StringBuffer();

		for (Integer mzX10 : channelToMZX10) {
			if (leadingNull || buffer.length() != 0) {
				buffer.append(", ");
			}

			if (mzX10 == null) {
				if (buffer.length() == 0) {
					leadingNull = true;
				}

				continue;
			}

			String asString = String.valueOf(mzX10);
			char lastCharacter = asString.charAt(asString.length()-1);
			int howManyBeforeDecimal = asString.length() - 1;

			if (howManyBeforeDecimal == 0) {
				buffer.append('0');
			} else {
				buffer.append(asString.substring(0, howManyBeforeDecimal));
			}

			if (lastCharacter != '0') {
				buffer.append('.');
				buffer.append(lastCharacter);
			}
		}

		return buffer.toString();
	}

	static {
		for (InputParameter inputParameter : InputParameter.values()) {
			Integer mzX10 = inputParameter.getMzX10();

			if (mzX10 != null) {
				knownMZX10s.add(mzX10);
			}
		}
	}
}
