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

package org.easotope.client.core.widgets.graph;

import java.util.ArrayList;

import org.easotope.client.Messages;
import org.easotope.client.core.ColorCache;
import org.easotope.client.core.adaptors.LoggingAdaptor;
import org.easotope.client.core.adaptors.LoggingControlAdaptor;
import org.easotope.client.core.adaptors.LoggingDisposeAdaptor;
import org.easotope.client.core.adaptors.LoggingGestureAdaptor;
import org.easotope.client.core.adaptors.LoggingMouseAdaptor;
import org.easotope.client.core.adaptors.LoggingMouseMoveAdaptor;
import org.easotope.client.core.adaptors.LoggingMouseTrackAdaptor;
import org.easotope.client.core.adaptors.LoggingPaintAdaptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.GestureEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;


public class Graph extends Composite {
	private final double GRAPH_MAGNIFY_FRACTION = 1.1d;
	private final double GRAPH_SHIFT_FRACTION = 0.02d;

	private ArrayList<DrawableObject> drawableObjects = new ArrayList<DrawableObject>();

	private GraphSettings graphSettings;
	private CoordinateTransform coordinateTransform;
	private HorizontalAxis horizontalAxis;
	private VerticalAxis verticalAxis;
	private MenuManager menuManager;
	private Hover hover;
	private Canvas canvas;
	private Image cacheImage;
	private boolean autoScaleOnPaint;
	private boolean backgroundIsWhite;

	public Graph(Composite parent, int style) {
		super(parent, style | SWT.BORDER);
		setLayout(new FillLayout());

		graphSettings = new GraphSettings(getDisplay());
		coordinateTransform = new CoordinateTransform();
		horizontalAxis = new HorizontalAxis(graphSettings);
		verticalAxis = new VerticalAxis(graphSettings);
		menuManager = new MenuManager(getDisplay(), getShell());
		hover = new Hover();
		autoScaleOnPaint = false;

		setBackground(ColorCache.getColor(getDisplay(), ColorCache.WHITE));
		backgroundIsWhite = true;

		addDisposeListener(new LoggingDisposeAdaptor() {
			@Override
			public void loggingWidgetDisposed(DisposeEvent e) {
				graphSettings.dispose();

				if (cacheImage != null) {
					cacheImage.dispose();
				}

				cacheImage = null;
			}
		});

		menuManager.addDefaultMenuItem(new MenuItemListener() {
			@Override
			public void handleEvent(Event event) {
				autoScale();
			}

			@Override
			public String getName() {
				return Messages.graph_autoscaleMenuItem;
			}
		});

		menuManager.addDefaultMenuItem(new MenuItemListener() {
			@Override
			public void handleEvent(Event event) {
				toggleBackground();
			}

			@Override
			public String getName() {
				return Messages.graph_toggleBackgroundMenuItem;
			}
		});

		menuManager.addDefaultMenuItem(new MenuItemListener() {
			@Override
			public void handleEvent(Event event) {
				graphSettings.setHorizontalAxisShowLabel(!graphSettings.getHorizontalAxisShowLabel());
				clearAndRedraw();
			}

			@Override
			public String getName() {
				return graphSettings.getHorizontalAxisShowLabel() ? Messages.graph_horizontalAxisHideLabel : Messages.graph_horizontalAxisShowLabel;
			}

			@Override
			public boolean isVisible() {
				return graphSettings.getHorizontalAxisLabel() != null;
			}
		});

		menuManager.addDefaultMenuItem(new MenuItemListener() {
			@Override
			public void handleEvent(Event event) {
				graphSettings.setHorizontalAxisShowScale(!graphSettings.getHorizontalAxisShowScale());
				clearAndRedraw();
			}

			@Override
			public String getName() {
				return graphSettings.getHorizontalAxisShowScale() ? Messages.graph_horizontalAxisHideScale : Messages.graph_horizontalAxisShowScale;
			}
		});

		menuManager.addDefaultMenuItem(new MenuItemListener() {
			@Override
			public void handleEvent(Event event) {
				graphSettings.setVerticalAxisShowLabel(!graphSettings.getVerticalAxisShowLabel());
				clearAndRedraw();
			}

			@Override
			public String getName() {
				return graphSettings.getVerticalAxisShowLabel() ? Messages.graph_verticalAxisHideLabel : Messages.graph_verticalAxisShowLabel;
			}

			@Override
			public boolean isVisible() {
				return graphSettings.getVerticalAxisLabel() != null;
			}
		});

		menuManager.addDefaultMenuItem(new MenuItemListener() {
			@Override
			public void handleEvent(Event event) {
				graphSettings.setVerticalAxisShowScale(!graphSettings.getVerticalAxisShowScale());
				clearAndRedraw();
			}

			@Override
			public String getName() {
				return graphSettings.getVerticalAxisShowScale() ? Messages.graph_verticalAxisHideScale : Messages.graph_verticalAxisShowScale;
			}
		});

		canvas = new Canvas(this, SWT.NO_BACKGROUND);
		canvas.addControlListener(new LoggingControlAdaptor() {
			public void loggingControlResized(ControlEvent e) {
				coordinateTransform.updateCanvasSize(canvas.getSize().x, canvas.getSize().y);
				hover.reset();
				clearAndRedraw();
			}
		});
		canvas.addPaintListener(new LoggingPaintAdaptor() {
			public void loggingPaintControl(PaintEvent e) {
				Graph.this.paintControl(e);
			}
		});
		canvas.addGestureListener(new LoggingGestureAdaptor() {
			@Override
			public void loggingGesture(GestureEvent e) {
				if (!coordinateTransform.isReady()) {
					return;
				}

				if (e.detail == SWT.GESTURE_BEGIN) {
					coordinateTransform.gestureBegin(e.x, e.y);
					hover.reset();
					clearAndRedraw();

				} else if (e.detail == SWT.GESTURE_MAGNIFY) {
					coordinateTransform.gestureMagnify(e.magnification);
					hover.reset();
					clearAndRedraw();

				} else if (e.detail == SWT.GESTURE_PAN) {
					coordinateTransform.gesturePan(e.xDirection, e.yDirection);
					hover.reset();
					clearAndRedraw();

				} else if (e.detail == SWT.GESTURE_END) {
					coordinateTransform.gestureEnd();
					hover.reset();
					clearAndRedraw();
				}
			}
		});
		canvas.addMouseMoveListener(new LoggingMouseMoveAdaptor() {
			public void loggingMouseMove(MouseEvent e) {
				canvas.forceFocus();
				hover.reset();
				mouseMoved(e);
				redraw();
			}
		});
		canvas.addMouseListener(new LoggingMouseAdaptor() {
			@Override
			public void loggingMouseDoubleClick(MouseEvent e) {

			}

			@Override
			public void loggingMouseDown(MouseEvent e) {
				if ((e.stateMask & SWT.MOD4) != 0 || e.button == 3) {
					// right click
					int displayX = canvas.toDisplay(e.x, e.y).x;
					int displayY = canvas.toDisplay(e.x, e.y).y;
					menuManager.raiseMenu(e.x, e.y, displayX, displayY, drawableObjects);

				} else if (e.button == 2) {
					// middle click

				} else if (e.button == 1) {
					// left click
					leftMouseDown(e);
				}
			}

			@Override
			public void loggingMouseUp(MouseEvent e) {
				leftMouseUp(e);
			}
		});
		canvas.addMouseTrackListener(new LoggingMouseTrackAdaptor() {
			public void loggingMouseEnter(MouseEvent e) {
				canvas.forceFocus();
				hover.reset();
				clearAndRedraw();
			}

			public void loggingMouseExit(MouseEvent e) {
				hover.reset();
				clearAndRedraw();
			}

			public void loggingMouseHover(MouseEvent e) {
				hover.set(e.x, e.y, drawableObjects);
				clearAndRedraw();
			}
		});
		canvas.addListener(SWT.KeyDown, new LoggingAdaptor() {
			public void loggingHandleEvent(Event e) {
				Point cursorDisplayLocation = getDisplay().getCursorLocation();
				Point cursorControlLocation = canvas.toControl(cursorDisplayLocation);

				if (cursorControlLocation.x < 0 || cursorControlLocation.y < 0) {
					return;
				}

				if (cursorControlLocation.x >= canvas.getSize().x || cursorControlLocation.y >= canvas.getSize().y) {
					return;
				}

				hover.reset();

				if (e.character == '+') {
					coordinateTransform.gestureBegin(cursorControlLocation.x, cursorControlLocation.y);
					coordinateTransform.gestureMagnify(GRAPH_MAGNIFY_FRACTION);
					coordinateTransform.gestureEnd();
					clearAndRedraw();

				} else if (e.character == '-') {
					coordinateTransform.gestureBegin(cursorControlLocation.x, cursorControlLocation.y);
					coordinateTransform.gestureMagnify(1/GRAPH_MAGNIFY_FRACTION);
					coordinateTransform.gestureEnd();
					clearAndRedraw();

				} else if (e.keyCode == SWT.ARROW_UP) {
					coordinateTransform.gestureBegin(cursorControlLocation.x, cursorControlLocation.y);
					coordinateTransform.gesturePan(0, (int) Math.round(canvas.getSize().y * GRAPH_SHIFT_FRACTION));
					coordinateTransform.gestureEnd();
					clearAndRedraw();

				} else if (e.keyCode == SWT.ARROW_DOWN) {
					coordinateTransform.gestureBegin(cursorControlLocation.x, cursorControlLocation.y);
					coordinateTransform.gesturePan(0, (int) -Math.round(canvas.getSize().y * GRAPH_SHIFT_FRACTION));
					coordinateTransform.gestureEnd();
					clearAndRedraw();

				} else if (e.keyCode == SWT.ARROW_LEFT) {
					coordinateTransform.gestureBegin(cursorControlLocation.x, cursorControlLocation.y);
					coordinateTransform.gesturePan((int) Math.round(canvas.getSize().x * GRAPH_SHIFT_FRACTION), 0);
					coordinateTransform.gestureEnd();
					clearAndRedraw();

				} else if (e.keyCode == SWT.ARROW_RIGHT) {
					coordinateTransform.gestureBegin(cursorControlLocation.x, cursorControlLocation.y);
					coordinateTransform.gesturePan((int) -Math.round(canvas.getSize().x * GRAPH_SHIFT_FRACTION), 0);
					coordinateTransform.gestureEnd();
					clearAndRedraw();
				}
			}
		});
	}

	public boolean getUnitsAreEqual() {
		return graphSettings.getUnitsAreEqual();
	}

	public void setUnitsAreEqual(boolean unitsAreEqual) {
		graphSettings.setUnitsAreEqual(unitsAreEqual);
	}

	public String getVerticalAxisLabel() {
		return graphSettings.getVerticalAxisLabel();
	}

	public void setVerticalAxisLabel(String verticalAxisLabel) {
		graphSettings.setVerticalAxisLabel(verticalAxisLabel);
	}

	public boolean getVerticalAxisShowLabel() {
		return graphSettings.getVerticalAxisShowLabel();
	}

	public void setVerticalAxisShowLabel(boolean verticalAxisShowLabel) {
		graphSettings.setVerticalAxisShowLabel(verticalAxisShowLabel);
	}

	public boolean getVerticalAxisShowValues() {
		return graphSettings.getVerticalAxisShowScale();
	}

	public void setVerticalAxisShowValues(boolean verticalAxisShowValues) {
		graphSettings.setVerticalAxisShowScale(verticalAxisShowValues);
	}

	public String getHorizontalAxisLabel() {
		return graphSettings.getHorizontalAxisLabel();
	}

	public void setHorizontalAxisLabel(String horizontalAxisLabel) {
		graphSettings.setHorizontalAxisLabel(horizontalAxisLabel);
	}

	public boolean getHorizontalAxisShowLabel() {
		return graphSettings.getHorizontalAxisShowLabel();
	}

	public void setHorizontalAxisShowLabel(boolean horizontalAxisShowLabel) {
		graphSettings.setHorizontalAxisShowLabel(horizontalAxisShowLabel);
	}

	public boolean getHorizontalAxisShowValues() {
		return graphSettings.getHorizontalAxisShowScale();
	}

	public void setHorizontalAxisShowValues(boolean horizontalAxisShowValues) {
		graphSettings.setHorizontalAxisShowScale(horizontalAxisShowValues);
	}

	protected void leftMouseDown(MouseEvent e) {
		
	}

	protected void mouseMoved(MouseEvent e) {
		
	}

	protected void leftMouseUp(MouseEvent e) {
		
	}

	protected CoordinateTransform getCoordinateTransform() {
		return coordinateTransform;
	}

	public void addMenuItem(MenuItemListener listener) {
		menuManager.addGraphMenuItem(listener);
	}

	public void addDrawableObjectFirst(DrawableObject drawableObject) {
		drawableObjects.add(0, drawableObject);
		clearAndRedraw();
	}

	public void addDrawableObjectLast(DrawableObject drawableObject) {
		drawableObjects.add(drawableObject);
		clearAndRedraw();
	}

	public ArrayList<DrawableObject> getDrawableObjects() {
		return drawableObjects;
	}

	public void removeDrawableObject(DrawableObject drawableObject) {
		drawableObjects.remove(drawableObject);
		clearAndRedraw();
	}

	public void removeAllDrawableObjects() {
		drawableObjects.clear();
		clearAndRedraw();
	}

	public void autoScale() {
		if (canvas.getSize().x == 0 || canvas.getSize().y == 0) {
			autoScaleOnPaint = true;
			return;
		}

		int canvasSizeX = canvas.getSize().x;
		int canvasSizeY = canvas.getSize().y;
		
		if (drawableObjects.size() == 0) {
			coordinateTransform.initY(canvasSizeY, 0, canvasSizeY-1, 100.0d, 0.0d);
			coordinateTransform.initX(canvasSizeX, 0, canvasSizeX-1, 0.0d, 100.0d);
			clearAndRedraw();
			return;
		}

		double minX = Double.POSITIVE_INFINITY;
		double maxX = Double.NEGATIVE_INFINITY;
		double minY = Double.POSITIVE_INFINITY;
		double maxY = Double.NEGATIVE_INFINITY;

		for (DrawableObject drawableObject : drawableObjects) {
			if (drawableObject.isInfluencesAutoscale()) {
				double drawableMinX = drawableObject.getMinX();
	
				if (!Double.isNaN(drawableMinX) && drawableMinX < minX) {
					minX = drawableMinX;
				}
	
				double drawableMaxX = drawableObject.getMaxX();
	
				if (!Double.isNaN(drawableMaxX) && drawableMaxX > maxX) {
					maxX = drawableMaxX;
				}
	
				double drawableMinY = drawableObject.getMinY();
	
				if (!Double.isNaN(drawableMinY) && drawableMinY < minY) {
					minY = drawableMinY;
				}
	
				double drawableMaxY = drawableObject.getMaxY();
	
				if (!Double.isNaN(drawableMaxY) && drawableMaxY > maxY) {
					maxY = drawableMaxY;
				}
			}
		}

		if (minX == maxX) {
			minX -= 100;
			maxX += 100;
		}

		if (minY == maxY) {
			minY -= 100;
			maxY += 100;
		}

		Image tempImage = null;
		GC tempGc = null;

		if (cacheImage == null) {
			tempImage = new Image(getDisplay(), 1, 1);
			tempGc = new GC(tempImage);
		} else {
			tempGc = new GC(cacheImage);
		}

		int horizontalHeight = horizontalAxis.getHeight(tempGc);
		coordinateTransform.initY(canvasSizeY, 0, canvasSizeY - horizontalHeight - 1, maxY, minY);

		verticalAxis.setParams(tempGc, canvasSizeX, canvasSizeY, canvasSizeY - horizontalHeight - 1, coordinateTransform);
		coordinateTransform.initX(canvasSizeX, verticalAxis.getWidth(), canvasSizeX - 1, minX, maxX);

		coordinateTransform.gestureBegin((verticalAxis.getWidth() + (canvasSizeX-1)) / 2, (canvasSizeY-horizontalHeight-1) /2);
		coordinateTransform.gestureMagnify(0.9);
		coordinateTransform.gestureEnd();

		tempGc.dispose();

		if (tempImage != null) {
			tempImage.dispose();
		}

		clearAndRedraw();
	}

	private void toggleBackground() {
		if (backgroundIsWhite) {
			setBackground(ColorCache.getColor(getDisplay(), ColorCache.BLACK));
		} else {
			setBackground(ColorCache.getColor(getDisplay(), ColorCache.WHITE));
		}

		backgroundIsWhite = !backgroundIsWhite;
		clearAndRedraw();
	}

	public void clearAndRedraw() {
		if (cacheImage != null) {
			cacheImage.dispose();
		}

		cacheImage = null;
		canvas.redraw();
	}

	protected void paintControl(PaintEvent e) {
		if (autoScaleOnPaint) {
			autoScaleOnPaint = false;
			autoScale();
		}

		if (!coordinateTransform.isReady()) {
			e.gc.fillRectangle(0, 0, canvas.getSize().x, canvas.getSize().y);
			return;
		}

		if (cacheImage == null) {
			Point canvasSize = canvas.getSize();
			cacheImage = new Image(getDisplay(), canvasSize.x, canvasSize.y);
			GC gc = new GC(cacheImage);

			gc.setBackground(getBackground());
			gc.fillRectangle(0, 0, canvasSize.x, canvasSize.y);

			int horizontalAxisHeight = horizontalAxis.getHeight(gc);
			verticalAxis.setParams(gc, canvasSize.x, canvasSize.y, canvasSize.y-horizontalAxisHeight-1, coordinateTransform);
			int verticalAxisWidth = verticalAxis.getWidth();

			horizontalAxis.draw(gc, canvas.getSize().x, canvas.getSize().y, verticalAxisWidth, coordinateTransform);
			verticalAxis.draw(gc);

			gc.setClipping(verticalAxisWidth, 0, canvas.getSize().x-verticalAxisWidth, canvas.getSize().y-horizontalAxisHeight);

			for (DrawableObject drawableObject : drawableObjects) {
				drawableObject.draw(gc, canvas.getSize().x, canvas.getSize().y, coordinateTransform);
			}

			gc.dispose();
		}

		e.gc.drawImage(cacheImage, 0, 0);
		hover.drawHover(e.gc, canvas.getSize().x, canvas.getSize().y, graphSettings);
	}
}
