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

package org.easotope.framework.core.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class Reflection {
	public static Object createObject(String className, Object... parameters) {
		try {
			Class<?> clazz = Class.forName(className);
			Constructor<?> foundConstructor = null;

			if (parameters == null) {
				parameters = new Object[0];
			}

			for (Constructor<?> constructor : clazz.getDeclaredConstructors()){
				Class<?>[] types = constructor.getParameterTypes();

				if (typesMatchParameters(types, parameters)) {
					foundConstructor = constructor;
					break;
				}
		    }

			if (foundConstructor == null) {
				throw new RuntimeException("constructor " + className + "() not found");
			}

	    	return foundConstructor.newInstance(parameters);

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static Object callMethod(Object object, String methodName, Object... parameters) {
		try {
			Method foundMethod = null;

			for (Method method : object.getClass().getMethods()) {
				if (!methodName.equals(method.getName())) {
					continue;
				}

				Class<?>[] types = method.getParameterTypes();

				if (typesMatchParameters(types, parameters)) {
					foundMethod = method;
					break;
				}
			}

			if (foundMethod == null) {
				throw new RuntimeException("method " + methodName + "() not found");
			}

	    	return foundMethod.invoke(object, parameters);

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static Object callGetter(Object object, String name) {
		try {
			Method method = object.getClass().getMethod("get" + name, new Class[] { });
	    	return method.invoke(object);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
    }

	public static void callSetter(Object object, String name, Object value) {
		try {
			Method method = object.getClass().getMethod("set" + name, new Class[] { value.getClass() });
	    	method.invoke(object, value);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
    }
	
	private static boolean typesMatchParameters(Class<?>[] types, Object[] parameters) {
		if (types.length != parameters.length) {
			return false;
		}

        boolean match = true;
        for (int i=0; i<types.length; i++) {
        		Class<?> need = types[i];

        		if (need.isPrimitive() || parameters[i] != null) {
	        		Class<?> got = parameters[i].getClass();

	        		if (!need.isAssignableFrom(got)) {
	        			if (need.isPrimitive()) {
	        				match = (int.class.equals(need) && Integer.class.equals(got))
	        						|| (long.class.equals(need) && Long.class.equals(got))
	        						|| (char.class.equals(need) && Character.class.equals(got))
	        						|| (short.class.equals(need) && Short.class.equals(got))
	        						|| (boolean.class.equals(need) && Boolean.class.equals(got))
	        						|| (byte.class.equals(need) && Byte.class.equals(got))
	        						|| (float.class.equals(need) && Float.class.equals(got))
	        						|| (double.class.equals(need) && Double.class.equals(got));
	        			} else {
	        				match = false;
	        			}
	        		}
        		}

            if (!match) {
                break;
            }
        }

        return match;
	}
}
