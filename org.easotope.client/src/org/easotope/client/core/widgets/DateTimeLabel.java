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

import org.easotope.client.core.adaptors.LoggingDisposeAdaptor;
import org.easotope.framework.core.logging.Log;
import org.easotope.framework.core.logging.Log.Level;
import org.easotope.framework.dbcore.tables.User;
import org.easotope.shared.core.DateFormat;
import org.easotope.shared.core.PotentialGraphicsMethodsShared;
import org.easotope.shared.core.cache.logininfo.LoginInfoCache;
import org.easotope.shared.core.cache.logininfo.logininfo.LoginInfoCacheLoginInfoGetListener;
import org.easotope.shared.core.tables.Permissions;
import org.easotope.shared.core.tables.Preferences;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class DateTimeLabel extends Label implements LoginInfoCacheLoginInfoGetListener {
	private volatile String currentTimeZone;
	private volatile boolean currentShowTimeZone;
	private volatile long millis;
	private volatile String defaultText = "";
	private volatile boolean withSeconds = false;
	private volatile boolean unset = true;

	public DateTimeLabel(Composite parent, int style) {
		super(parent, style);

		addDisposeListener(new LoggingDisposeAdaptor() {
			@Override
			public void loggingWidgetDisposed(DisposeEvent e) {
				LoginInfoCache.getInstance().removeListener(DateTimeLabel.this);
			}
		});

		currentTimeZone = LoginInfoCache.getInstance().getPreferences().getTimeZoneId();
		currentShowTimeZone = LoginInfoCache.getInstance().getPreferences().getShowTimeZone();
		
		LoginInfoCache.getInstance().addListener(this);
	}

	public void setWithSeconds(boolean withSeconds) {
		this.withSeconds = withSeconds;
	}

	public void setDefaultText(String defaultText) {
		this.defaultText = defaultText;

		if (unset) {
			setText(defaultText);
			getParent().layout();
		}
	}

	public void setDate(long millis) {
		if (millis == Long.MAX_VALUE) {
			unsetDate();
			return;
		}

		this.millis = millis;
		setText(DateFormat.format(millis, currentTimeZone, currentShowTimeZone, withSeconds));
		unset = false;
		getParent().layout();
	}

	public void unsetDate() {
		setText(defaultText);
		unset = true;
		getParent().layout();
	}

	@Override
	public boolean stillCallabled() {
		return !isDisposed();
	}

	@Override
	public void loginInfoUpdated(int commandId, User user, Permissions permissions, final Preferences preferences) {
		getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				try {
					if (isDisposed()) {
						return;
					}

					if (preferences == null) {
						return;
					}

					String newTimeZone = preferences.getTimeZoneId();
					boolean newShowTimeZone = preferences.getShowTimeZone();
	
					if (currentTimeZone == null && newTimeZone == null) {
						return;
					}

					if (currentShowTimeZone != newShowTimeZone || (currentTimeZone == null && newTimeZone != null) || (currentTimeZone != null && newTimeZone == null) || !currentTimeZone.equals(newTimeZone)) {
						currentTimeZone = newTimeZone;
						currentShowTimeZone = newShowTimeZone;

						if (!unset) {
							setText(DateFormat.format(millis, currentTimeZone, currentShowTimeZone, withSeconds));
						}
					}

				} catch (Exception e) {
					Log.getInstance().log(Level.INFO, this, "unexpected exception", e);
					PotentialGraphicsMethodsShared.reportErrorToUser(DateTimeLabel.this.getDisplay(), e);
				}
			}
		});
	}

	@Override
	protected void checkSubclass() {
		// remove check to allow subclassing
	}
}
