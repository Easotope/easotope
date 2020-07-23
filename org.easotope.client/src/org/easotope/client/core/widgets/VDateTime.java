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

package org.easotope.client.core.widgets;

import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.util.Vector;

import org.easotope.client.core.adaptors.LoggingSelectionAdaptor;
import org.easotope.framework.core.logging.Log;
import org.easotope.framework.core.logging.Log.Level;
import org.easotope.framework.dbcore.tables.User;
import org.easotope.shared.core.PotentialGraphicsMethodsShared;
import org.easotope.shared.core.cache.logininfo.LoginInfoCache;
import org.easotope.shared.core.cache.logininfo.logininfo.LoginInfoCacheLoginInfoGetListener;
import org.easotope.shared.core.tables.Permissions;
import org.easotope.shared.core.tables.Preferences;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;

public class VDateTime extends Composite implements LoginInfoCacheLoginInfoGetListener {
	private static int MAX_HEIGHT = 23;
	
	private volatile String currentTimeZone;
	private long oldMillis;

	private DateTime date;
	private DateTime time;

	private Vector<VDateTimeListener> listeners = new Vector<VDateTimeListener>();
	
	public VDateTime(Composite parent, int style) {
		super(parent, style);
		
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		this.setLayout(gridLayout);

		date = new DateTime(this, SWT.DATE | SWT.BORDER);
		GridData gridData = new GridData();
		gridData.verticalAlignment = SWT.CENTER;
		date.setLayoutData(gridData);
		date.addSelectionListener(new LoggingSelectionAdaptor() {
			@Override
			public void loggingWidgetSelected(SelectionEvent e) {
				VDateTime.this.fireWidgetSelected();
			}
		});

		time = new DateTime(this, SWT.TIME | SWT.SHORT | SWT.BORDER);
		gridData = new GridData();
		gridData.verticalAlignment = SWT.CENTER;
		time.setLayoutData(gridData);
		time.addSelectionListener(new LoggingSelectionAdaptor() {
			@Override
			public void loggingWidgetSelected(SelectionEvent e) {
				VDateTime.this.fireWidgetSelected();
			}
		});

		currentTimeZone = LoginInfoCache.getInstance().getPreferences().getTimeZoneId();
		setDate(0);
		LoginInfoCache.getInstance().addListener(this);
	}
	
	private void fireWidgetSelected() {
		for (VDateTimeListener listener : listeners) {
			listener.widgetSelected();
		}
	}

	public synchronized void setDate(long newMillis) {
		GregorianCalendar gregorianCalendar = new GregorianCalendar(currentTimeZone == null ? TimeZone.getDefault() : TimeZone.getTimeZone(currentTimeZone));
		gregorianCalendar.setTimeInMillis(newMillis);

		date.setYear(gregorianCalendar.get(GregorianCalendar.YEAR));
		date.setMonth(gregorianCalendar.get(GregorianCalendar.MONTH));
		date.setDay(gregorianCalendar.get(GregorianCalendar.DAY_OF_MONTH));
		time.setHours(gregorianCalendar.get(GregorianCalendar.HOUR_OF_DAY));
		time.setMinutes(gregorianCalendar.get(GregorianCalendar.MINUTE));

		oldMillis = getDate();
	}

	public synchronized long getDate() {
		GregorianCalendar gregorianCalendar = new GregorianCalendar(currentTimeZone == null ? TimeZone.getDefault() : TimeZone.getTimeZone(currentTimeZone));
		gregorianCalendar.clear();
		gregorianCalendar.set(date.getYear(), date.getMonth(), date.getDay(), time.getHours(), time.getMinutes(), 0);
		return gregorianCalendar.getTimeInMillis();
	}

	private synchronized void timeZoneChanged(String newTimeZoneId) {
		long millis = getDate();
		currentTimeZone = newTimeZoneId;
		setDate(millis);
	}

	public synchronized void revert() {
		setDate(oldMillis);
	}

	public synchronized boolean hasChanged() {
		return getDate() != oldMillis;
	}

	public synchronized void addSelectionListener(VDateTimeListener listener) {
		listeners.add(listener);
	}

	public synchronized void removeSelectionListener(VDateTimeListener listener) {
		listeners.remove(listener);
	}

	@Override
	public boolean stillCallabled() {
		return !isDisposed();
	}

	@Override
	public synchronized void loginInfoUpdated(int commandId, User user, Permissions permissions, Preferences preferences) {
		if (preferences == null) {
			return;
		}

		final String newTimeZoneId = preferences.getTimeZoneId();

		if (currentTimeZone == null && newTimeZoneId == null) {
			return;
		}

		if ((currentTimeZone == null && newTimeZoneId != null)
				|| (currentTimeZone != null && newTimeZoneId == null)
				|| !currentTimeZone.equals(newTimeZoneId)) {

			getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					try {
						if (isDisposed()) {
							return;
						}
	
						timeZoneChanged(newTimeZoneId);

					} catch (Exception e) {
						Log.getInstance().log(Level.INFO, this, "unexpected exception", e);
						PotentialGraphicsMethodsShared.reportErrorToUser(VDateTime.this.getDisplay(), e);
					}
				}
			});
		}
	}

	@Override
	public synchronized void setEnabled(boolean enabled) {
		date.setEnabled(enabled);
		time.setEnabled(enabled);
	}

	@Override
	public synchronized void dispose() {
		LoginInfoCache.getInstance().removeListener(this);
		super.dispose();
	}

	@Override
	public Point computeSize(int wHint, int hHint, boolean changed) {
		return super.computeSize(wHint, MAX_HEIGHT, changed);
	}

	@Override
	public Point computeSize(int wHint, int hHint) {
		return super.computeSize(wHint, MAX_HEIGHT);
	}
}
