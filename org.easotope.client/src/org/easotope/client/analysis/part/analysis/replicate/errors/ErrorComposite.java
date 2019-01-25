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

package org.easotope.client.analysis.part.analysis.replicate.errors;

import java.util.HashMap;
import java.util.List;

import org.easotope.client.Messages;
import org.easotope.client.analysis.part.analysis.replicate.ReplicateAnalysisPart;
import org.easotope.client.core.GuiConstants;
import org.easotope.client.core.adaptors.LoggingAdaptor;
import org.easotope.client.core.part.ChainedComposite;
import org.easotope.client.core.part.ChainedPart;
import org.easotope.framework.commands.Command;
import org.easotope.framework.dbcore.DatabaseConstants;
import org.easotope.framework.dbcore.tables.User;
import org.easotope.shared.admin.cache.user.UserCache;
import org.easotope.shared.admin.cache.user.userlist.UserCacheUserListGetListener;
import org.easotope.shared.admin.cache.user.userlist.UserList;
import org.easotope.shared.admin.cache.user.userlist.UserListItem;
import org.easotope.shared.analysis.cache.corrinterval.CorrIntervalCache;
import org.easotope.shared.analysis.cache.corrinterval.corrintervalcomp.CorrIntervalCacheCorrIntervalCompGetListener;
import org.easotope.shared.analysis.tables.CorrIntervalError;
import org.easotope.shared.analysis.tables.CorrIntervalScratchPad;
import org.easotope.shared.analysis.tables.RepStepParams;
import org.easotope.shared.core.DateFormat;
import org.easotope.shared.core.cache.logininfo.LoginInfoCache;
import org.easotope.shared.core.cache.logininfo.logininfo.LoginInfoCacheLoginInfoGetListener;
import org.easotope.shared.core.tables.Permissions;
import org.easotope.shared.core.tables.Preferences;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

public class ErrorComposite extends ChainedComposite implements CorrIntervalCacheCorrIntervalCompGetListener, UserCacheUserListGetListener, LoginInfoCacheLoginInfoGetListener {
	private Table table;

	private Integer corrIntervalId = DatabaseConstants.EMPTY_DB_ID;
	private Integer dataAnalysisTypeId = DatabaseConstants.EMPTY_DB_ID;
	private int waitingOnCommandId = Command.UNDEFINED_ID;

	private HashMap<Integer,String> userIdToName = new HashMap<Integer,String>();
	
	public ErrorComposite(ChainedPart chainedPart, Composite parent, int style) {
		super(chainedPart, parent, style);

		FormLayout formLayout = new FormLayout();
		formLayout.marginTop = GuiConstants.FORM_LAYOUT_MARGIN;
		formLayout.marginLeft = GuiConstants.FORM_LAYOUT_MARGIN;
		formLayout.marginRight = GuiConstants.FORM_LAYOUT_MARGIN;
		formLayout.marginBottom = GuiConstants.FORM_LAYOUT_MARGIN;
		setLayout(formLayout);

		Label label = new Label(this, SWT.NONE);
		FormData formData = new FormData();
		formData.top = new FormAttachment(0);
		formData.left = new FormAttachment(0);
		label.setLayoutData(formData);
		label.setText(Messages.errorComposite_title);

		table = new Table(this, SWT.BORDER);
		formData = new FormData();
		formData.top = new FormAttachment(label);
		formData.left = new FormAttachment(0);
		formData.right = new FormAttachment(100);
		formData.bottom = new FormAttachment(100);
		table.setLayoutData(formData);
		table.addListener(SWT.Selection, new LoggingAdaptor() {
			@Override
			public void loggingHandleEvent(Event event) {
				table.deselectAll();
			}
		});
		table.setLinesVisible(true);

		TableColumn column = new TableColumn(table, SWT.NONE);
		column.setText(Messages.errorComposite_column1);
		column.setWidth(75);

		column = new TableColumn(table, SWT.NONE);
		column.setText(Messages.errorComposite_column2);
		column.setWidth(75);

		column = new TableColumn(table, SWT.NONE);
		column.setText(Messages.errorComposite_column3);
		column.setWidth(75);

		column = new TableColumn(table, SWT.NONE);
		column.setText(Messages.errorComposite_column4);
		column.setWidth(1000);

		table.setHeaderVisible(true);
		setVisible(false);

		UserCache.getInstance().userListGet(this);

		CorrIntervalCache.getInstance().addListener(this);
		UserCache.getInstance().addListener(this);
		LoginInfoCache.getInstance().addListener(this);
	}

	@Override
	protected void handleDispose() {
		CorrIntervalCache.getInstance().removeListener(this);
		UserCache.getInstance().removeListener(this);
		LoginInfoCache.getInstance().removeListener(this);
	}

	@Override
	public boolean isWaiting() {
		return waitingOnCommandId != Command.UNDEFINED_ID;
	}

	@Override
	protected void setWidgetsEnabled() {
		// ignore
	}

	@Override
	protected void setWidgetsDisabled() {
		// ignore
	}

	@Override
	protected void receiveAddRequest() {
		// ignore
	}

	@Override
	protected void cancelAddRequest() {
		// ignore
	}

	@Override
	protected void receiveSelection() {
		HashMap<String,Object> selection = getChainedPart().getSelection();
		Integer corrIntervalId = (Integer) selection.get(ReplicateAnalysisPart.SELECTION_CORR_INTERVAL_ID);
		Integer dataAnalysisTypeId = (Integer) selection.get(ReplicateAnalysisPart.SELECTION_DATA_ANALYSIS_ID);

		if (this.corrIntervalId == corrIntervalId && this.dataAnalysisTypeId == dataAnalysisTypeId) {
			return;
		}

		if (corrIntervalId == null || corrIntervalId == DatabaseConstants.EMPTY_DB_ID || dataAnalysisTypeId == null || dataAnalysisTypeId == DatabaseConstants.EMPTY_DB_ID) {
			this.corrIntervalId = DatabaseConstants.EMPTY_DB_ID;
			this.dataAnalysisTypeId = DatabaseConstants.EMPTY_DB_ID;
			waitingOnCommandId = Command.UNDEFINED_ID;
			setVisible(false);
			return;
		}

		this.corrIntervalId = corrIntervalId;
		this.dataAnalysisTypeId = dataAnalysisTypeId;
		waitingOnCommandId = Command.UNDEFINED_ID;

		waitingOnCommandId = CorrIntervalCache.getInstance().corrIntervalCompGet(corrIntervalId, dataAnalysisTypeId, this);
		getChainedPart().setCursor();

		if (waitingOnCommandId != Command.UNDEFINED_ID) {
			setVisible(false);
		}
	}

	@Override
	public boolean stillCallabled() {
		return !isDisposed();
	}

	private void newUserList(UserList userList) {
		userIdToName.clear();

		for (Integer id : userList.keySet()) {
			UserListItem userListItem = userList.get(id);
			userIdToName.put(id, userListItem.getName());
		}
		
		refreshTableItems();
	}

	private void refreshTableItems() {
		for (TableItem item : table.getItems()) {
			CorrIntervalError error = (CorrIntervalError) item.getData();

			String timeZone = LoginInfoCache.getInstance().getPreferences().getTimeZoneId();
			boolean showTimeZone = LoginInfoCache.getInstance().getPreferences().getShowTimeZone();
			String replicateDate = (error.getReplicateDate() == 0) ? Messages.errorComposite_emptyReplicate : DateFormat.format(error.getReplicateDate(), timeZone, showTimeZone, false);
			String userName = (userIdToName.containsKey(error.getReplicateUserId())) ? userIdToName.get(error.getReplicateUserId()) : Messages.errorComposite_emptyUsername;

			item.setText(0, replicateDate);
			item.setText(1, userName);
		}
	}

	private void newErrors(List<CorrIntervalError> errors) {
		table.removeAll();

		for (CorrIntervalError error : errors) {
			TableItem tableItem = new TableItem(table, SWT.NONE);

			String timeZone = LoginInfoCache.getInstance().getPreferences().getTimeZoneId();
			boolean showTimeZone = LoginInfoCache.getInstance().getPreferences().getShowTimeZone();
			String replicateDate = (error.getReplicateDate() == 0) ? Messages.errorComposite_emptyReplicate : DateFormat.format(error.getReplicateDate(), timeZone, showTimeZone, false);
			String userName = (userIdToName.containsKey(error.getReplicateUserId())) ? userIdToName.get(error.getReplicateUserId()) : Messages.errorComposite_emptyUsername;
			String repStepName = (error.getRepStepName() == null || error.getRepStepName().trim().isEmpty()) ? Messages.errorComposite_emptyRepStepName : error.getRepStepName();

			tableItem.setText(0, replicateDate);
			tableItem.setText(1, userName);
			tableItem.setText(2, repStepName);
			tableItem.setText(3, error.getErrorMessage());
			
			tableItem.setData(error);
		}

		setVisible(true);
	}

	@Override
	public void corrIntervalCompGetCompleted(int commandId, int corrIntervalId, int dataAnalysisTypeId, List<RepStepParams> repStepParameters, CorrIntervalScratchPad corrIntervalScratchPad, List<CorrIntervalError> errors) {
		if (waitingOnCommandId != commandId) {
			return;
		}

		newErrors(errors);

		waitingOnCommandId = Command.UNDEFINED_ID;
		getChainedPart().setCursor();
	}

	@Override
	public void corrIntervalCompUpdated(int commandId, int corrIntervalId, int dataAnalysisTypeId, List<RepStepParams> repStepParameters, CorrIntervalScratchPad corrIntervalScratchPad, List<CorrIntervalError> errors) {
		HashMap<String,Object> selection = getChainedPart().getSelection();
		Integer selectedCorrIntervalId = (Integer) selection.get(ReplicateAnalysisPart.SELECTION_CORR_INTERVAL_ID);
		Integer selectedDataAnalysisTypeId = (Integer) selection.get(ReplicateAnalysisPart.SELECTION_DATA_ANALYSIS_ID);

		if (selectedDataAnalysisTypeId != null && selectedDataAnalysisTypeId == dataAnalysisTypeId && selectedCorrIntervalId != null && selectedCorrIntervalId == corrIntervalId) {
			newErrors(errors);
		}
	}

	@Override
	public void corrIntervalCompGetError(int commandId, String message) {
		if (waitingOnCommandId != commandId) {
			return;
		}

		waitingOnCommandId = Command.UNDEFINED_ID;
		getChainedPart().setCursor();

		getChainedPart().raiseError(message);
	}

	@Override
	public void userListGetCompleted(int commandId, UserList userList) {
		newUserList(userList);
	}

	@Override
	public void userListUpdated(int commandId, UserList userList) {
		newUserList(userList);
	}

	@Override
	public void userListGetError(int commandId, String message) {
		getChainedPart().raiseError(message);
	}

	@Override
	public void loginInfoUpdated(int commandId, User user, Permissions permissions, Preferences preferences) {
		refreshTableItems();
	}
}
