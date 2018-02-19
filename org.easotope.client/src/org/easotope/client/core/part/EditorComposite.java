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

package org.easotope.client.core.part;

import java.util.HashMap;

import org.easotope.client.core.BlockUntilNoOnePersisting;
import org.easotope.framework.commands.Command;
import org.easotope.framework.dbcore.tables.TableObjectWithIntegerId;
import org.easotope.framework.dbcore.tables.User;
import org.easotope.shared.core.cache.CacheListener;
import org.easotope.shared.core.cache.logininfo.LoginInfoCache;
import org.easotope.shared.core.cache.logininfo.logininfo.LoginInfoCacheLoginInfoGetListener;
import org.easotope.shared.core.tables.Permissions;
import org.easotope.shared.core.tables.Preferences;
import org.eclipse.swt.widgets.Composite;

public abstract class EditorComposite extends ChainedComposite implements CacheListener, LoginInfoCacheLoginInfoGetListener {
	/**
	 * Using the current object, set all widgets so that they contain
	 * the current data.
	 */
	abstract protected void setCurrentFieldValues();

	/**
	 * Set widgets so they have default values for a new object.
	 */
	abstract protected void setDefaultFieldValues();

	/**
	 * Enable all widgets in this composite that the user has permission
	 * to modify. If the user does not have permission, the widgets will
	 * instead be disabled. If these newly disabled widgets have been
	 * modified since their original default values were set, the widget
	 * might need to be reverted.
	 */
	abstract public void enableWidgets();

	/**
	 * Disable all widgets in this composite effectively freezing all input.
	 */
	abstract public void disableWidgets();

	/**
	 * This method is called when some change has been made to one of
	 * this composite's widgets. The method should return whether this
	 * widget is dirty (ie, can be reverted).
	 * 
	 * @return true if the composite is dirty (ie, can be reverted)
	 */
	abstract protected boolean isDirty();

	/**
	 * This method is called when some change has been made to one of
	 * this composite's widgets. The method should validate the fields,
	 * turn on any error indicators, and return whether the composite
	 * contains any errors or not. If any error indicators have been made
	 * visible a requestLayout() may also be needed to layout the composite.
	 * 
	 * @return true if the composite has errors
	 */
	abstract protected boolean hasError();

	/**
	 * A selection may contain many different keys and values but some may
	 * not be relevant to this editor. This method determines whether the
	 * selection has changed in a way that needs further processing. 
	 * 
	 * @param selection
	 * @return true if the selection needs further processing
	 */
	abstract protected boolean selectionHasChanged(HashMap<String,Object> selection);

	/**
	 * Whenever the selection changes, this method is called so that objects
	 * can be loaded. If a command could not complete immediately, the
	 * waitingFor() method should be called.
	 * 
	 * @param selection the selection set by the previous ChainedComposite
	 * @return true if parameters are sufficient to show this object when loaded
	 */
	abstract protected boolean processSelection(HashMap<String,Object> selection);

	/**
	 * Build and save the object to the database.
	 */
	abstract protected void requestSave(boolean isResend);

	/**
	 * This method is called to ask whether the user has permission to
	 * delete the currently edited object.
	 * 
	 * @return true if the user may delete this object
	 */
	abstract protected boolean canDelete();

	/**
	 * Send delete request to the database.
	 * @return TODO
	 */
	abstract protected boolean requestDelete();

	private Object currentObject = null;
	private boolean canRenderWhenNotWaiting = false;
	private boolean startingEditor = false;
	private HashMap<String,Integer> waitingForObjectCommandIds = new HashMap<String,Integer>();
	private boolean widgetsAreEnabled = false;
	private boolean fieldsCanBeReverted = false;
	private boolean fieldsAreStale = false;
	private boolean needsLayout = false;

	protected EditorComposite(ChainedPart chainedPart, Composite parent, int style) {
		super(chainedPart, parent, style);
		setVisible(false);
		LoginInfoCache.getInstance().addListener(this);
	}

	@Override
	protected void handleDispose() {
		super.handleDispose();
		LoginInfoCache.getInstance().removeListener(this);		
	}

	/**
	 * Called from above
	 */
	@Override
	public final boolean stillCallabled() {
		return !isDisposed();
	}

	/**
	 * Called from above
	 */
	@Override
	public final boolean isWaiting() {
		return waitingForObjectCommandIds.size() != 0;
	}

	/**
	 * Called from above
	 */
	@Override
	public final void receiveSelection() {
		if (!selectionHasChanged(getChainedPart().getSelection())) {
			setGuiFlags();
			return;
		}

		currentObject = null;
		canRenderWhenNotWaiting = processSelection(getChainedPart().getSelection());

		if (canRenderWhenNotWaiting && waitingForObjectCommandIds.size() == 0) {
			startUpEditor();
		} else {
			setVisible(false);
			setGuiFlags();
		}
	}

	/**
	 * Called from above
	 */
	@Override
	public final void setWidgetsEnabled() {
		widgetsAreEnabled = true;
		enableWidgets();
	}

	/**
	 * Called from above
	 */
	@Override
	public final void setWidgetsDisabled() {
		widgetsAreEnabled = false;
		disableWidgets();
	}

	/**
	 * Called from above
	 */
	@Override
	final void revert() {
		if (!getVisible()) {
			return;
		}

		if (currentObject == null) {
			setDefaultFieldValues();
		} else {
			setCurrentFieldValues();
		}

		layout();
		fieldsAreStale = false;
		setGuiFlags();
	}

	/**
	 * Called from above
	 */
	@Override
	public final void persist() {
		BlockUntilNoOnePersisting.getInstance().startPersisting();
		requestSave(false);
		widgetsAreEnabled = false;
		disableWidgets();
	}

	/**
	 * Called from above
	 */
	@Override
	final void delete() {
		if (requestDelete()) {
			BlockUntilNoOnePersisting.getInstance().startPersisting();
			widgetsAreEnabled = false;
			disableWidgets();
		}
	}
	
	/**
	 * Called from above
	 */
	@Override
	public final void receiveAddRequest() {
		currentObject = null;
		startUpEditor();
	}

	/**
	 * Called from above
	 */
	@Override
	protected void cancelAddRequest() {
		if (currentObject == null) {
			setDefaultFieldValues();
			layout();
			setVisible(false);
			setGuiFlags();
		}
	}

	/**
	 * Called from below
	 */
	public final void widgetStatusChanged() {
		setGuiFlags();
	}

	/**
	 * Called from below
	 */
	public final void waitingFor(String key, int commandId) {
		if (commandId != Command.UNDEFINED_ID) {
			waitingForObjectCommandIds.put(key, commandId);
			getChainedPart().setCursor();
		}
	}

	public final void cancelWaitingFor(String key) {
		waitingForObjectCommandIds.remove(key);
		getChainedPart().setCursor();
	}

	/**
	 * Called from below
	 */
	public final void doneWaitingFor(String key) {
		cancelWaitingFor(key);

		if (canRenderWhenNotWaiting && waitingForObjectCommandIds.size() == 0 && !getVisible()) {
			startUpEditor();
		}
	}

	/**
	 * Called from below
	 */
	public final int commandIdForKey(String key) {
		return waitingForObjectCommandIds.containsKey(key) ? waitingForObjectCommandIds.get(key) : Command.UNDEFINED_ID;
	}

	/**
	 * Called from below
	 */
	protected final void layoutNeeded() {
		needsLayout = true;
	}

	/**
	 * Called from below
	 */
	protected final Object getCurrentObject() {
		return currentObject;
	}

	/**
	 * Called from below
	 */
	protected final HashMap<String,Object> getSelection() {
		return getChainedPart().getSelection();
	}

	/**
	 * Called from below
	 */
	protected final void newObject(String key, Object object) {
		currentObject = object;
		doneWaitingFor(key);
	}

	/**
	 * Called from below
	 */
	protected final void saveComplete(String key, Object object) {
		BlockUntilNoOnePersisting.getInstance().stopPersisting(false);

		if (getChainedPart().closePartIfRequested()) {
			return;
		}

		currentObject = object;
		waitingForObjectCommandIds.remove(key);
		getChainedPart().setCursor();

		widgetsAreEnabled = true;
		enableWidgets();
		setCurrentFieldValues();
		layout();

		fieldsAreStale = false;
		setGuiFlags();
	}

	/**
	 * Called from below
	 */
	protected final void deleteComplete(String key) {
		BlockUntilNoOnePersisting.getInstance().stopPersisting(false);
		getChainedPart().closePart();
	}

	/**
	 * Called from below
	 */
	protected final void updateObject(Object object, String message) {
		if (!newIsReplacementForOld(currentObject, object)) {
			return;
		}

		currentObject = object;
		fieldsAreStale = true;

		if (!fieldsCanBeReverted) {
			setCurrentFieldValues();
			layout();
			fieldsAreStale = false;

		} else if (message != null) {
			getChainedPart().raiseInfo(message);
		}
		
		setGuiFlags();
	}

	/**
	 * Called from below
	 */
	public final void raiseGetError(String key, String message) {
		getChainedPart().raiseError(message);
		waitingForObjectCommandIds.remove(key);
		getChainedPart().setCursor();
	}

	/**
	 * Called from below
	 */
	protected final void raiseSaveOrDeleteError(String key, String message) {
		getChainedPart().raiseError(message);
		widgetsAreEnabled = true;
		enableWidgets();
		waitingForObjectCommandIds.remove(key);
		getChainedPart().setCursor();
		BlockUntilNoOnePersisting.getInstance().stopPersisting(true);
	}

	/**
	 * Called from below
	 */
	protected final void raiseResendRequest(String key, String message) {
		waitingForObjectCommandIds.remove(key);
		getChainedPart().setCursor();

		if (getChainedPart().raiseQuestion(message)) {
			requestSave(true);
			return;
		}

		widgetsAreEnabled = true;
		enableWidgets();
		BlockUntilNoOnePersisting.getInstance().stopPersisting(true);
	}

	private void startUpEditor() {
		if (startingEditor) {
			return;
		}

		startingEditor = true;

		if (currentObject == null) {
			setDefaultFieldValues();
		} else {
			setCurrentFieldValues();
		}

		layout();
		fieldsAreStale = false;
		widgetsAreEnabled = true;
		enableWidgets();
		setVisible(true);
		setGuiFlags();

		startingEditor = false;
	}

	private void setGuiFlags() {
		if (!getVisible()) {
			setCanRevert(false);
			setCanPersist(false);
			setCanDelete(false);
			return;
		}

		fieldsCanBeReverted = isDirty();

		if (!fieldsCanBeReverted && fieldsAreStale) {
			setCurrentFieldValues();
			needsLayout = true;
			fieldsAreStale = false;
		}

		setCanRevert(fieldsCanBeReverted);

		boolean fieldsHaveErrors = hasError();
		setCanPersist(fieldsCanBeReverted ? !fieldsHaveErrors : currentObject == null && !fieldsHaveErrors);

		setCanDelete(canDelete());

		if (needsLayout) {
			needsLayout = false;
			layout();
		}
	}

	protected boolean newIsReplacementForOld(Object oldObject, Object newObject) {
		if (oldObject == null || newObject == null) {
			return false;
		}

		TableObjectWithIntegerId oldTableObjectWithIntegerId;
		TableObjectWithIntegerId newTableObjectWithIntegerId;

		if (oldObject instanceof Object[]) {
			Object[] oldObjectArray = (Object[]) oldObject;
			oldTableObjectWithIntegerId = (TableObjectWithIntegerId) oldObjectArray[0];
	
			Object[] newObjectArray = (Object[]) newObject;
			newTableObjectWithIntegerId = (TableObjectWithIntegerId) newObjectArray[0];

		} else {
			oldTableObjectWithIntegerId = (TableObjectWithIntegerId) oldObject;
			newTableObjectWithIntegerId = (TableObjectWithIntegerId) newObject;
		}

		return newTableObjectWithIntegerId.getId() == oldTableObjectWithIntegerId.getId();
	}

	@Override
	public void loginInfoUpdated(int commandId, User user, Permissions permissions, Preferences preferences) {
		if (widgetsAreEnabled) {
			enableWidgets();
			setGuiFlags();
		}
	}
}
