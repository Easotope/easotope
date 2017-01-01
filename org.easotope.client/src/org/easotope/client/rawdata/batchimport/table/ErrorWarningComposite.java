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

package org.easotope.client.rawdata.batchimport.table;

import org.easotope.client.core.Icons;
import org.easotope.client.core.adaptors.LoggingPaintAdaptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

public class ErrorWarningComposite extends Composite {
	private Canvas canvas;
	private Image currentIcon;
	private Image warningIcon;
	private Image errorIcon;
	private Image blankImage;

	public ErrorWarningComposite(Composite parent, int style) {
		super(parent, style);
		GridLayout gridLayout = new GridLayout(1, false);
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		setLayout(gridLayout);

		warningIcon = Icons.getWarning(getDisplay());
		errorIcon = Icons.getError(getDisplay());
		
	    Image src = new Image(getDisplay(), warningIcon.getImageData().width, warningIcon.getImageData().height);
	    ImageData imageData = src.getImageData();
	    imageData.transparentPixel = imageData.getPixel(0, 0);
	    src.dispose();
	    blankImage = new Image(getDisplay(), imageData);

		canvas = new Canvas(this, SWT.NONE);
		GridData gridData = new GridData();
		gridData.widthHint = warningIcon.getImageData().width;
		gridData.heightHint = warningIcon.getImageData().height;
		gridData.grabExcessVerticalSpace = true;
		gridData.verticalAlignment = SWT.CENTER;
		canvas.setLayoutData(gridData);
		canvas.addPaintListener(new LoggingPaintAdaptor() {
			public void loggingPaintControl(PaintEvent e) {
				e.gc.setAntialias(SWT.ON);
				e.gc.drawImage((currentIcon != null) ? currentIcon : blankImage, 0, 0);
			}
		});
	}

	public void setWarning(String message) {
		currentIcon = warningIcon;
		canvas.setToolTipText(message);
		canvas.redraw();
	}

	public void setError(String message) {
		currentIcon = errorIcon;
		canvas.setToolTipText(message);
		canvas.redraw();
	}

	public void reset() {
		currentIcon = null;
		canvas.setToolTipText(null);
		canvas.redraw();
	}
}
