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

package org.easotope.client.analysis.repstep.co2.d48offsetpbl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.easotope.client.Messages;
import org.easotope.client.analysis.superclass.RepStepGraphicComposite;
import org.easotope.client.core.GuiConstants;
import org.easotope.client.core.part.EasotopePart;
import org.easotope.framework.dbcore.tables.User;
import org.easotope.shared.admin.cache.standard.StandardCache;
import org.easotope.shared.admin.cache.standard.standardlist.StandardCacheStandardListGetListener;
import org.easotope.shared.admin.cache.standard.standardlist.StandardList;
import org.easotope.shared.admin.cache.standard.standardlist.StandardListItem;
import org.easotope.shared.analysis.repstep.co2.d48offsetpbl.Calculator;
import org.easotope.shared.analysis.repstep.co2.d48offsetpbl.Calculator.D48OffsetPoint;
import org.easotope.shared.core.DateFormat;
import org.easotope.shared.core.cache.logininfo.LoginInfoCache;
import org.easotope.shared.core.cache.logininfo.logininfo.LoginInfoCacheLoginInfoGetListener;
import org.easotope.shared.core.scratchpad.ReplicatePad;
import org.easotope.shared.core.scratchpad.ScratchPad;
import org.easotope.shared.core.tables.Permissions;
import org.easotope.shared.core.tables.Preferences;
import org.easotope.shared.math.Statistics;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

public class GraphicComposite extends RepStepGraphicComposite implements StandardCacheStandardListGetListener, LoginInfoCacheLoginInfoGetListener {
	private Label average;
	private Label stddev;
	private Table table;

	private ArrayList<D48OffsetPoint> points;
	private StandardList standardList;

	public GraphicComposite(EasotopePart easotopePart, Composite parent, int style) {
		super(easotopePart, parent, style);
		FormLayout formLayout = new FormLayout();
		formLayout.marginTop = GuiConstants.FORM_LAYOUT_MARGIN;
		formLayout.marginLeft = GuiConstants.FORM_LAYOUT_MARGIN;
		formLayout.marginRight = GuiConstants.FORM_LAYOUT_MARGIN;
		formLayout.marginBottom = GuiConstants.FORM_LAYOUT_MARGIN;
		setLayout(formLayout);

		Label label2 = new Label(this, SWT.NONE);
		FormData formData = new FormData();
		formData.top = new FormAttachment(0);
		formData.left = new FormAttachment(0);
		label2.setLayoutData(formData);
		label2.setText(Messages.co2D48OffsetPblGraphicComposite_average);

		average = new Label(this, SWT.NONE);
		formData = new FormData();
		formData.top = new FormAttachment(0);
		formData.left = new FormAttachment(label2);
		average.setLayoutData(formData);

		Label label = new Label(this, SWT.NONE);
		formData = new FormData();
		formData.top = new FormAttachment(label2);
		formData.left = new FormAttachment(0);
		label.setLayoutData(formData);
		label.setText(Messages.co2D48OffsetPblGraphicComposite_stddev);

		stddev = new Label(this, SWT.NONE);
		formData = new FormData();
		formData.top = new FormAttachment(label2);
		formData.left = new FormAttachment(label);
		stddev.setLayoutData(formData);

		table = new Table(this, SWT.NONE);
		formData = new FormData();
		formData.top = new FormAttachment(label);
		formData.left = new FormAttachment(0);
		formData.right = new FormAttachment(100);
		formData.bottom = new FormAttachment(100);
		table.setLayoutData(formData);

		TableColumn tableColumn = new TableColumn(table, SWT.NONE);
		tableColumn.setText(Messages.co2D48OffsetPblGraphicComposite_date);
		tableColumn.setWidth(150);
		tableColumn = new TableColumn(table, SWT.NONE);
		tableColumn.setText(Messages.co2D48OffsetPblGraphicComposite_name);
		tableColumn.setWidth(150);
		tableColumn = new TableColumn(table, SWT.NONE);
		tableColumn.setText(Messages.co2D48OffsetPblGraphicComposite_D48);
		tableColumn.setWidth(150);
		tableColumn = new TableColumn(table, SWT.NONE);
		tableColumn.setText(Messages.co2D48OffsetPblGraphicComposite_disabled);
		tableColumn.setWidth(100);

		table.setHeaderVisible(true);
		
		StandardCache.getInstance().standardListGet(this);
		StandardCache.getInstance().addListener(this);
    }

	@Override
	protected void handleDispose() {
		StandardCache.getInstance().removeListener(this);
	}

	private void fillInTable() {
		table.removeAll();

		if (points != null) {
			for (D48OffsetPoint d48OffsetPoint : points) {
				TableItem tableItem = new TableItem(table, SWT.NONE);

				String timeZone = LoginInfoCache.getInstance().getPreferences().getTimeZoneId();
				boolean showTimeZone = LoginInfoCache.getInstance().getPreferences().getShowTimeZone();
				String timestamp = DateFormat.format(d48OffsetPoint.getDate(), timeZone, showTimeZone, false);
				String name = "";

				if (standardList != null) {
					StandardListItem standardListItem = standardList.get(d48OffsetPoint.getStandardId());

					if (standardListItem != null) {
						name = standardListItem.getName();
					}
				}

				tableItem.setText(0, timestamp);
				tableItem.setText(1, name);
				tableItem.setText(2, String.valueOf(d48OffsetPoint.getΔ48()));
				tableItem.setText(3, d48OffsetPoint.isDisabled() ? Messages.co2D48OffsetPblGraphicComposite_disabledLabel : "");
			}
		}
	}

	@Override
	public void newCalculation(ScratchPad<ReplicatePad> scratchPad) {
		ReplicatePad replicatePad = scratchPad.getChildren().get(0);

		@SuppressWarnings("unchecked")
		ArrayList<D48OffsetPoint> temp = (ArrayList<D48OffsetPoint>) replicatePad.getVolatileData(Calculator.getVolatileDataD48OffsetPointsKey());
		points = new ArrayList<D48OffsetPoint>(temp);
		Collections.sort(points, comparator);

		fillInTable();

		Statistics statistics = (Statistics) replicatePad.getVolatileData(Calculator.getVolatileDataStatisticsKey());
		average.setText(String.valueOf(statistics.getMean()));
		stddev.setText(String.valueOf(statistics.getStandardDeviationSample()));

		layout();
	}

	@Override
	public boolean stillCallabled() {
		return !isDisposed();
	}

	@Override
	public void loginInfoUpdated(int commandId, User user, Permissions permissions, Preferences preferences) {
		fillInTable();
	}

	@Override
	public void standardListGetCompleted(int commandId, StandardList standardList) {
		this.standardList = standardList;
		fillInTable();
	}

	@Override
	public void standardListUpdated(int commandId, StandardList standardList) {
		this.standardList = standardList;
		fillInTable();
	}

	@Override
	public void standardListGetError(int commandId, String message) {
		
	}

	public static Comparator<D48OffsetPoint> comparator = new Comparator<D48OffsetPoint>() {
		@Override
		public int compare(D48OffsetPoint arg0, D48OffsetPoint arg1) {
			if (arg0.getDate() == arg1.getDate()) {
				return 0;
			}

			return arg0.getDate() < arg1.getDate() ? 1 : -1;
		}
	};
}
