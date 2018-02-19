/*
 * Copyright © 2016-2018 by Devon Bowen.
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

package org.easotope.shared.rawdata.parser;

import java.util.HashMap;
import java.util.Vector;

import org.easotope.shared.rawdata.InputParameter;

public class MapBuilder {
	private HashMap<InputParameter,Object> map = new HashMap<InputParameter,Object>();
	private HashMap<InputParameter,Class<?>> vectorTypes = new HashMap<InputParameter,Class<?>>();

	public void put(InputParameter parameter, Object obj) {
		Class<?> castClass = parameter.getType();
		
		Object value = null; 

		if (Double.class == castClass) {
			value = objectToDouble(obj);
		} else if (Long.class == castClass) {
			value = objectToLong(obj);
		} else if (String.class == castClass) {
			value = objectToString(obj);
		} else if (Integer.class == castClass) {
			value = objectToInteger(obj);
		}

		assert(!map.containsKey(parameter));
		assert(value != null);

		map.put(parameter, value);
	}

	public void put(InputParameter parameter, int index, Object obj) {
		Class<?> castClass = parameter.getType();

		Vector<Object> vector = null;
		Class<?> vectorClass = vectorTypes.get(parameter);

		if (vectorClass == null) {
			vector = new Vector<Object>();
			map.put(parameter, vector);
			vectorTypes.put(parameter, castClass);

		} else {
			assert(castClass == vectorClass);
			@SuppressWarnings("unchecked")
			Vector<Object> vectorTmp = (Vector<Object>) map.get(parameter);
			vector = vectorTmp;
		}

		Object value = null;

		if (Double.class == castClass) {
			value = objectToDouble(obj);
			
		} else if (Long.class == castClass) {
			value = objectToLong(obj);
			
		} else if (String.class == castClass) {
			value = objectToString(obj);

		} else if (Integer.class == castClass) {
			value = objectToInteger(obj);
		}

		if (index >= vector.size()) {
			vector.setSize(index + 1);
		}

		assert(vector.get(index) == null);
		vector.setElementAt(value, index);
	}
	
	public HashMap<InputParameter, Object> getMap() {
		return map;
	}

	private String objectToString(Object obj) {
		if (obj instanceof Double) {
			return String.valueOf((Double) obj);

		} if (obj instanceof String) {
			return (String) obj;

		} if (obj instanceof Long) {
			return String.valueOf((Long) obj);

		} if (obj instanceof Integer) {
			return String.valueOf((Integer) obj);
		}

		return null;
	}

	private Long objectToLong(Object obj) {
		assert(!(obj instanceof Double));

		if (obj instanceof String) {
			try {
				return Long.parseLong((String) obj);
			} catch (NumberFormatException e) {
				// do nothing
			}

		} if (obj instanceof Long) {
			return (Long) obj;

		} if (obj instanceof Integer) {
			return new Long((Integer) obj);
		}

		return null;
	}

	private Integer objectToInteger(Object obj) {
		assert(!(obj instanceof Double));

		if (obj instanceof String) {
			try {
				return Integer.parseInt((String) obj);
			} catch (NumberFormatException e) {
				// do nothing
			}

		} else if (obj instanceof Long) {
			return (Integer) obj;
			
		} else if (obj instanceof Integer) {
			return (Integer) obj;
		}

		return null;
	}

	private Double objectToDouble(Object obj) {
		if (obj instanceof Double) {
			return (Double) obj;

		} else if (obj instanceof Float) {
			double temp = (double) ((Float) obj);
			return (Double) temp;

		} else if (obj instanceof String) {
			try {
				return Double.parseDouble((String) obj);
			} catch (NumberFormatException e) {
				// do nothing
			}

		} else if (obj instanceof Long) {
			return new Double((Long) obj);
			
		} else if (obj instanceof Integer) {
			return new Double((Integer) obj);
		}

		return null;
	}
}
