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

package org.easotope.shared.analysis.step;

public abstract class StepController {
	/**
	 * Returns the name of this step. These name should be descriptive
	 * and unique so that the user can tell them apart.
	 * 
	 * @return the name of the step
	 */
	public abstract String getStepName();

	/**
	 * Returns a short (approximately 2 sentence) description of what this
	 * step calculates. This should be a plain text description. For HTML,
	 * see the getDocumentation() method.
	 * 
	 * @return short step documentation
	 */
	public abstract String getShortDocumentation();

	/**
	 * Returns a path in the documentation to an HTML document that describes
	 * what this step does. This should be detailed enough for the user to
	 * understand the workings of the step so that the user could do the
	 * calculations themselves. The path must be separated by {0} which will
	 * later be filled in by the path separator of the operating system.
	 * 
	 * @return step documentation
	 */
	public abstract String getDocumentationPath();

	/**
	 * Returns a string with the full class name (ex "java.util.ArrayList") of
	 * the StepCalculator class that can do the calculations for this step.
	 * 
	 * @return full StepCalculator class name or null if none
	 */
	public abstract String getStepCalculatorClassName();

	/**
	 * Returns a string with the full class name (ex "java.util.ArrayList") of
	 * the ParameterComposite class that can be used to modify the parameters
	 * for this step.
	 * 
	 * @return full ParameterComposite class name or null if none
	 */
	public abstract String getParameterComposite();

	/**
	 * Returns a string with the full class name (ex "java.util.ArrayList") of
	 * the GraphicComposite class that can be used to display information about
	 * the inner workings of the step.
	 * 
	 * @return full GraphicComposite class name or null if none
	 */
	public abstract String getGraphicComposite();

	public abstract InputDescription[] getInputDescription();
	public abstract OutputDescription[] getOutputDescription();
}
