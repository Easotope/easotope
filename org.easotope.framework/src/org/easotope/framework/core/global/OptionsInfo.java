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

package org.easotope.framework.core.global;

import java.util.concurrent.CountDownLatch;

import org.easotope.framework.commands.Command;
import org.easotope.framework.commands.OptionsGet;
import org.easotope.framework.dbcore.cmdprocessors.CommandListener;
import org.easotope.framework.dbcore.cmdprocessors.Event;
import org.easotope.framework.dbcore.cmdprocessors.EventListener;
import org.easotope.framework.dbcore.cmdprocessors.ProcessorManager;
import org.easotope.framework.dbcore.tables.Options;
import org.easotope.framework.events.OptionsUpdated;


public class OptionsInfo implements CommandListener, EventListener {
	private static OptionsInfo instance;

	private Options options = null;
	private CountDownLatch countdownLatch = new CountDownLatch(1);

	public OptionsInfo() {
		ProcessorManager.getInstance().getProcessor().addEventListener(this);
		OptionsGet optionsGet = new OptionsGet();
		ProcessorManager.getInstance().getProcessor().process(optionsGet, null, this);
	}

	public static OptionsInfo getInstance() {
		if (instance == null) {
			instance = new OptionsInfo();
		}

		try {
			instance.countdownLatch.await();
		} catch (InterruptedException e) {
			// do nothing
		}
		
		return instance;
	}

	public Options getOptions() {
		return options;
	}

	@Override
	public void commandExecuted(Command command) {
		OptionsGet optionsGet = (OptionsGet) command;
		options = optionsGet.getOptions();
		countdownLatch.countDown();
	}

	@Override
	public void eventReceived(Event event, Command command) {
		if (event instanceof OptionsUpdated) {
			OptionsUpdated optionsUpdated = (OptionsUpdated) event;
			options = optionsUpdated.getOptions();
		}
	}
}
