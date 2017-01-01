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

package org.easotope.client.util;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.DeviceData;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Path;
import org.eclipse.swt.graphics.Pattern;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Region;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class Sleak {
	private List list;
	private Canvas canvas;
	private Button start, stop, check;
	private Text text;
	private Label label;

	private Object[] oldObjects = new Object[0];
	//private Error[] oldErrors = new Error[0];
	private Object[] objects = new Object[0];
	private Error[] errors = new Error[0];

	public static void start(Display display) {
//		DeviceData data = new DeviceData();
//		data.tracking = true;
//		final Display display = new Display(data);
		Sleak sleak = new Sleak();
		Shell shell = new Shell(display);
		shell.setText("S-Leak");
		Point size = shell.getSize();
		shell.setSize(size.x / 2, size.y / 2);
		sleak.create(shell);
		shell.open();

		// Launch your application here
		// e.g.
		// shell = new Shell(display);
		// Button button1 = new Button(shell, SWT.PUSH);
		// button1.setBounds(10, 10, 100, 50);
		// button1.setText("Hello World");
		// Image image = new Image(display, 20, 20);
		// final Button button2 = new Button(shell, SWT.PUSH);
		// button2.setBounds(10, 70, 100, 50);
		// button2.setImage(image);
		// button2.addListener(SWT.Selection, new Listener() {
		// @Override
		// public void handleEvent(Event event) {
		// Image image = new Image(display, 20, 20);
		// button2.setImage(image);
		// }
		// });
		// shell.open();
		//
		// while (!shell.isDisposed ()) {
		// if (!display.readAndDispatch ()) display.sleep ();
		// }
		// display.dispose ();
	}

	public void create(Composite parent) {
		list = new List(parent, SWT.BORDER | SWT.V_SCROLL);
		list.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				refreshObject();
			}
		});
		text = new Text(parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		canvas = new Canvas(parent, SWT.BORDER);
		canvas.addListener(SWT.Paint, new Listener() {
			@Override
			public void handleEvent(Event event) {
				paintCanvas(event);
			}
		});
		check = new Button(parent, SWT.CHECK);
		check.setText("Stack");
		check.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event e) {
				toggleStackTrace();
			}
		});
		start = new Button(parent, SWT.PUSH);
		start.setText("Snap");
		start.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				refreshAll();
			}
		});
		stop = new Button(parent, SWT.PUSH);
		stop.setText("Diff");
		stop.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				refreshDifference();
			}
		});
		label = new Label(parent, SWT.BORDER);
		label.setText("0 object(s)");
		parent.addListener(SWT.Resize, new Listener() {
			@Override
			public void handleEvent(Event e) {
				layout();
			}
		});
		check.setSelection(false);
		text.setVisible(false);
		layout();
	}

	void refreshLabel() {
		int colors = 0, cursors = 0, fonts = 0, gcs = 0, images = 0;
		int paths = 0, patterns = 0, regions = 0, textLayouts = 0, transforms = 0;
		for (int i = 0; i < objects.length; i++) {
			Object object = objects[i];
			if (object instanceof Color)
				colors++;
			if (object instanceof Cursor)
				cursors++;
			if (object instanceof Font)
				fonts++;
			if (object instanceof GC)
				gcs++;
			if (object instanceof Image)
				images++;
			if (object instanceof Path)
				paths++;
			if (object instanceof Pattern)
				patterns++;
			if (object instanceof Region)
				regions++;
			if (object instanceof TextLayout)
				textLayouts++;
			if (object instanceof Transform)
				transforms++;
		}
		String string = "";
		if (colors != 0)
			string += colors + " Color(s)\n";
		if (cursors != 0)
			string += cursors + " Cursor(s)\n";
		if (fonts != 0)
			string += fonts + " Font(s)\n";
		if (gcs != 0)
			string += gcs + " GC(s)\n";
		if (images != 0)
			string += images + " Image(s)\n";
		if (paths != 0)
			string += paths + " Paths(s)\n";
		if (patterns != 0)
			string += patterns + " Pattern(s)\n";
		if (regions != 0)
			string += regions + " Region(s)\n";
		if (textLayouts != 0)
			string += textLayouts + " TextLayout(s)\n";
		if (transforms != 0)
			string += transforms + " Transform(s)\n";
		if (string.length() != 0) {
			string = string.substring(0, string.length() - 1);
		}
		label.setText(string);
	}

	void refreshDifference() {
		Display display = canvas.getDisplay();
		DeviceData info = display.getDeviceData();
		if (!info.tracking) {
			Shell shell = canvas.getShell();
			MessageBox dialog = new MessageBox(shell, SWT.ICON_WARNING | SWT.OK);
			dialog.setText(shell.getText());
			dialog.setMessage("Warning: Device is not tracking resource allocation");
			dialog.open();
		}
		Object[] newObjects = info.objects;
		Error[] newErrors = info.errors;
		Object[] diffObjects = new Object[newObjects.length];
		Error[] diffErrors = new Error[newErrors.length];
		int count = 0;
		for (int i = 0; i < newObjects.length; i++) {
			int index = 0;
			while (index < oldObjects.length) {
				if (newObjects[i] == oldObjects[index])
					break;
				index++;
			}
			if (index == oldObjects.length) {
				diffObjects[count] = newObjects[i];
				diffErrors[count] = newErrors[i];
				count++;
			}
		}
		objects = new Object[count];
		errors = new Error[count];
		System.arraycopy(diffObjects, 0, objects, 0, count);
		System.arraycopy(diffErrors, 0, errors, 0, count);
		list.removeAll();
		text.setText("");
		canvas.redraw();
		for (int i = 0; i < objects.length; i++) {
			list.add(objects[i].toString());
		}
		refreshLabel();
		layout();
	}

	void toggleStackTrace() {
		refreshObject();
		layout();
	}

	void paintCanvas(Event event) {
		canvas.setCursor(null);
		int index = list.getSelectionIndex();
		if (index == -1)
			return;
		GC gc = event.gc;
		Object object = objects[index];
		if (object instanceof Color) {
			if (((Color) object).isDisposed())
				return;
			gc.setBackground((Color) object);
			gc.fillRectangle(canvas.getClientArea());
			return;
		}
		if (object instanceof Cursor) {
			if (((Cursor) object).isDisposed())
				return;
			canvas.setCursor((Cursor) object);
			return;
		}
		if (object instanceof Font) {
			if (((Font) object).isDisposed())
				return;
			gc.setFont((Font) object);
			FontData[] array = gc.getFont().getFontData();
			String string = "";
			String lf = text.getLineDelimiter();
			for (int i = 0; i < array.length; i++) {
				FontData data = array[i];
				String style = "NORMAL";
				int bits = data.getStyle();
				if (bits != 0) {
					if ((bits & SWT.BOLD) != 0)
						style = "BOLD ";
					if ((bits & SWT.ITALIC) != 0)
						style += "ITALIC";
				}
				string += data.getName() + " " + data.getHeight() + " " + style
						+ lf;
			}
			gc.drawString(string, 0, 0);
			return;
		}
		// NOTHING TO DRAW FOR GC
		// if (object instanceof GC) {
		// return;
		// }
		if (object instanceof Image) {
			if (((Image) object).isDisposed())
				return;
			gc.drawImage((Image) object, 0, 0);
			return;
		}
		if (object instanceof Path) {
			if (((Path) object).isDisposed())
				return;
			gc.drawPath((Path) object);
			return;
		}
		if (object instanceof Pattern) {
			if (((Pattern) object).isDisposed())
				return;
			gc.setBackgroundPattern((Pattern) object);
			gc.fillRectangle(canvas.getClientArea());
			gc.setBackgroundPattern(null);
			return;
		}
		if (object instanceof Region) {
			if (((Region) object).isDisposed())
				return;
			String string = ((Region) object).getBounds().toString();
			gc.drawString(string, 0, 0);
			return;
		}
		if (object instanceof TextLayout) {
			if (((TextLayout) object).isDisposed())
				return;
			((TextLayout) object).draw(gc, 0, 0);
			return;
		}
		if (object instanceof Transform) {
			if (((Transform) object).isDisposed())
				return;
			String string = ((Transform) object).toString();
			gc.drawString(string, 0, 0);
			return;
		}
	}

	void refreshObject() {
		int index = list.getSelectionIndex();
		if (index == -1)
			return;
		if (check.getSelection()) {
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			PrintStream s = new PrintStream(stream);
			errors[index].printStackTrace(s);
			text.setText(stream.toString());
			text.setVisible(true);
			canvas.setVisible(false);
		} else {
			canvas.setVisible(true);
			text.setVisible(false);
			canvas.redraw();
		}
	}

	void refreshAll() {
		oldObjects = new Object[0];
		//oldErrors = new Error[0];
		refreshDifference();
		oldObjects = objects;
		//oldErrors = errors;
	}

	void layout() {
		Composite parent = canvas.getParent();
		Rectangle rect = parent.getClientArea();
		int width = 0;
		String[] items = list.getItems();
		GC gc = new GC(list);
		for (int i = 0; i < objects.length; i++) {
			width = Math.max(width, gc.stringExtent(items[i]).x);
		}
		gc.dispose();
		Point size1 = start.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		Point size2 = stop.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		Point size3 = check.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		Point size4 = label.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		width = Math.max(size1.x, Math.max(size2.x, Math.max(size3.x, width)));
		width = Math.max(64, Math.max(size4.x, list.computeSize(width, SWT.DEFAULT).x));
		start.setBounds(0, 0, width, size1.y);
		stop.setBounds(0, size1.y, width, size2.y);
		check.setBounds(0, size1.y + size2.y, width, size3.y);
		label.setBounds(0, rect.height - size4.y, width, size4.y);
		int height = size1.y + size2.y + size3.y;
		list.setBounds(0, height, width, rect.height - height - size4.y);
		text.setBounds(width, 0, rect.width - width, rect.height);
		canvas.setBounds(width, 0, rect.width - width, rect.height);
	}
}