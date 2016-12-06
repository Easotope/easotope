/*
 * Copyright © 2016 by Devon Bowen.
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

package org.easotope.client.analysis.part.analysis.sample.samsteps;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;

import org.easotope.client.core.part.EasotopeComposite;
import org.easotope.client.core.part.EasotopePart;
import org.easotope.client.util.Documentation;
import org.easotope.framework.core.util.TopDir;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.LocationListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Composite;

public class TabDocsComposite extends EasotopeComposite {
	private Browser browser = null;

	TabDocsComposite(EasotopePart easotopePart, Composite parent, int style) {
		super(easotopePart, parent, style);
		setLayout(new FillLayout());

 		try {
 			browser = new Browser(this, SWT.NONE);
 
 			browser.addLocationListener(new LocationListener() {
				@Override
				public void changing(LocationEvent event) {
					if (event.location == null) {
						event.doit = false;
						return;
					}

					if (event.location.startsWith("http:")) {
						Program.launch(event.location);
						event.doit = false;
						return;
					}
				}

				@Override
				public void changed(LocationEvent event) {
					// TODO Auto-generated method stub
					
				}
 			});
 		} catch (Exception e) {
 			// ignore
 		}
	}

	@Override
	protected void handleDispose() {
		// TODO Auto-generated method stub
		
	}

	void setUrl(String string) {
		if (browser != null) {
			try {
				String path = Documentation.getPath(string);

				if (path == null) {
					throw new RuntimeException("deep path is " + TopDir.getDeepPath());

				} else {
					browser.setVisible(true);
					browser.setUrl(new File(path).toURI().toURL().toString());
				}

			} catch (Exception e) {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				e.printStackTrace(new PrintStream(baos));

				String s = baos.toString();
				s = "ERROR LOADING DOCUMENTATION. PLEASE REPORT THE FOLLOWING DEBUGGING INFORMATION:\n\n" + s; 
				s = s.replaceAll("\\n", "<\\br>\n");

				browser.setText(s, false);
			}
		}		
	}
}
