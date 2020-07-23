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

package org.easotope.client.handler;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;

import javax.inject.Named;

import org.easotope.client.core.annotations.CanDelete;
import org.easotope.client.core.annotations.Delete;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.MContribution;
import org.eclipse.e4.ui.services.IServiceConstants;

public class DeleteHandler {
	@CanExecute
	public boolean canExecute(IEclipseContext context, @Named(IServiceConstants.ACTIVE_PART) final MContribution contribution) throws InvocationTargetException, InterruptedException {
		Object result = null;

		if (contribution != null) {
			final IEclipseContext pmContext = context.createChild();
			Object clientObject = contribution.getObject();
			result = ContextInjectionFactory.invoke(clientObject, (Class<? extends Annotation>) CanDelete.class, pmContext, null);
			pmContext.dispose();
		}

		if (result != null && result instanceof Boolean) {
			return ((Boolean) result).booleanValue();
		}

		return false;
	}

	@Execute
	public void execute(IEclipseContext context, @Named(IServiceConstants.ACTIVE_PART) final MContribution contribution) throws InvocationTargetException, InterruptedException {
		if (contribution != null) {
			final IEclipseContext pmContext = context.createChild();
			Object clientObject = contribution.getObject();
			ContextInjectionFactory.invoke(clientObject, (Class<? extends Annotation>) Delete.class, pmContext, null);
			pmContext.dispose();
		}
	}
}
