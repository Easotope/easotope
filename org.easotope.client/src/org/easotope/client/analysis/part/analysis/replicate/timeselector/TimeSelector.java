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

package org.easotope.client.analysis.part.analysis.replicate.timeselector;

import java.util.HashMap;

import org.easotope.client.Messages;
import org.easotope.client.analysis.part.analysis.replicate.ReplicateAnalysisPart;
import org.easotope.client.core.ColorCache;
import org.easotope.client.core.GuiConstants;
import org.easotope.client.core.PointDesign;
import org.easotope.client.core.PointStyle;
import org.easotope.client.core.adaptors.LoggingAdaptor;
import org.easotope.client.core.adaptors.LoggingControlAdaptor;
import org.easotope.client.core.adaptors.LoggingGestureAdaptor;
import org.easotope.client.core.adaptors.LoggingMouseAdaptor;
import org.easotope.client.core.adaptors.LoggingMouseMoveAdaptor;
import org.easotope.client.core.adaptors.LoggingMouseTrackAdaptor;
import org.easotope.client.core.adaptors.LoggingPaintAdaptor;
import org.easotope.client.core.part.ChainedComposite;
import org.easotope.client.core.part.ChainedPart;
import org.easotope.client.core.widgets.SortedCombo;
import org.easotope.framework.commands.Command;
import org.easotope.framework.dbcore.DatabaseConstants;
import org.easotope.framework.dbcore.tables.User;
import org.easotope.shared.admin.cache.massspec.MassSpecCache;
import org.easotope.shared.admin.cache.massspec.massspeclist.MassSpecCacheMassSpecListGetListener;
import org.easotope.shared.admin.cache.massspec.massspeclist.MassSpecList;
import org.easotope.shared.admin.cache.standard.StandardCache;
import org.easotope.shared.admin.cache.standard.standard.StandardCacheStandardGetListener;
import org.easotope.shared.admin.cache.user.UserCache;
import org.easotope.shared.admin.cache.user.userlist.UserCacheUserListGetListener;
import org.easotope.shared.admin.cache.user.userlist.UserList;
import org.easotope.shared.admin.tables.Standard;
import org.easotope.shared.analysis.cache.corrinterval.CorrIntervalCache;
import org.easotope.shared.analysis.cache.corrinterval.corrintervallist.CorrIntervalCacheCorrIntervalListGetListener;
import org.easotope.shared.analysis.cache.corrinterval.corrintervallist.CorrIntervalList;
import org.easotope.shared.analysis.cache.corrinterval.corrintervallist.CorrIntervalListItem;
import org.easotope.shared.core.DateFormat;
import org.easotope.shared.core.cache.logininfo.LoginInfoCache;
import org.easotope.shared.core.cache.logininfo.logininfo.LoginInfoCacheLoginInfoGetListener;
import org.easotope.shared.core.tables.Permissions;
import org.easotope.shared.core.tables.Preferences;
import org.easotope.shared.rawdata.cache.input.InputCache;
import org.easotope.shared.rawdata.cache.input.replicatelist.InputCacheReplicateListGetListener;
import org.easotope.shared.rawdata.cache.input.replicatelist.ReplicateList;
import org.easotope.shared.rawdata.cache.input.replicatelist.ReplicateListItem;
import org.easotope.shared.rawdata.cache.input.scanlist.InputCacheScanListGetListener;
import org.easotope.shared.rawdata.cache.input.scanlist.ScanList;
import org.easotope.shared.rawdata.cache.input.scanlist.ScanListItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.GestureEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Slider;

public class TimeSelector extends ChainedComposite implements LoginInfoCacheLoginInfoGetListener, UserCacheUserListGetListener, MassSpecCacheMassSpecListGetListener, CorrIntervalCacheCorrIntervalListGetListener, InputCacheReplicateListGetListener, StandardCacheStandardGetListener, InputCacheScanListGetListener {
	private final double DEFAULT_BUTTON_MAGNIFICATION = 1.1d;
	private final int DEFAULT_NUM_OBJECTS_TO_SHOW = 30;

	private HashMap<Integer,PointDesign> pointDesignCache = new HashMap<Integer,PointDesign>();
	private PointDesign scanPointDesign = null;

	private int waitingForMassSpecList = Command.UNDEFINED_ID;
	private int waitingForSampleList = Command.UNDEFINED_ID;
	private int waitingForStandardList = Command.UNDEFINED_ID;
	private int waitingForScanList = Command.UNDEFINED_ID;
	private int waitingForCorrIntervalList = Command.UNDEFINED_ID;

	private CoordinateTransform coordinateTransform;
	private FindObject findObject;
	private Hover hover;
	private RenderableItems renderableItems;
	private Selection selection;
	private YAxisTypes yAxisTypes;

	private boolean alreadyDrawing = false;

	private SortedCombo massSpec;
	private Label selectionLabel;
	private FormAttachment selectionLabelFormAttachment;
	private Button magnifyButton;
	private Button reduceButton;
	private Canvas canvas;
	private Image imageCache;
	private Slider slider;

	public TimeSelector(ChainedPart chainedPart, Composite parent, int style) {
		super(chainedPart, parent, style);

		FormLayout formLayout = new FormLayout();
		formLayout.marginTop = GuiConstants.FORM_LAYOUT_MARGIN;
		formLayout.marginLeft = GuiConstants.FORM_LAYOUT_MARGIN;
		formLayout.marginRight = GuiConstants.FORM_LAYOUT_MARGIN;
		formLayout.marginBottom = GuiConstants.FORM_LAYOUT_MARGIN;
		setLayout(formLayout);

		coordinateTransform = new CoordinateTransform();
		renderableItems = new RenderableItems();
		yAxisTypes = new YAxisTypes();
		findObject = new FindObject(coordinateTransform, renderableItems, yAxisTypes);
		hover = new Hover(coordinateTransform, yAxisTypes, findObject);
		selection = new Selection(coordinateTransform, renderableItems, yAxisTypes);
		
		pointDesignCache.put(DatabaseConstants.EMPTY_DB_ID, new PointDesign(getDisplay(), ColorCache.getColor(getDisplay(), ColorCache.BLACK), PointStyle.X));
		scanPointDesign = new PointDesign(getDisplay(), ColorCache.getColor(getDisplay(), ColorCache.BLACK), PointStyle.S);

		Composite massSpecComposite = new Composite(this, SWT.NONE);
		FormData formData = new FormData();
		formData.top = new FormAttachment(0);
		formData.left = new FormAttachment(0);
		massSpecComposite.setLayoutData(formData);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		gridLayout.marginWidth = 0;
		massSpecComposite.setLayout(gridLayout);

		Label massSpecLabel = new Label(massSpecComposite, SWT.NONE);
		GridData gridData = new GridData();
		gridData.verticalAlignment = SWT.CENTER;
		massSpecLabel.setLayoutData(gridData);
		massSpecLabel.setText(Messages.correctionPart_massSpecLabel);

		massSpec = new SortedCombo(massSpecComposite, SWT.READ_ONLY);
		gridData = new GridData();
		gridData.verticalAlignment = SWT.CENTER;
		gridData.widthHint = GuiConstants.SHORT_COMBO_INPUT_WIDTH;
		massSpec.setLayoutData(gridData);
		massSpec.addListener(SWT.Selection, new LoggingAdaptor() {
			@Override
			public void loggingHandleEvent(Event event) {
				setMassSpec(massSpec.getSelectedInteger());
			}
		});

		Composite labelComposite = new Composite(this, SWT.NONE);
		formData = new FormData();
		formData.top = new FormAttachment(0);
		formData.left = selectionLabelFormAttachment = new FormAttachment(50, 0);
		formData.bottom = new FormAttachment(massSpecComposite, 0, SWT.BOTTOM);
		labelComposite.setLayoutData(formData);
		gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		gridLayout.marginWidth = 0;
		labelComposite.setLayout(gridLayout);
		
		selectionLabel = new Label(labelComposite, SWT.NONE);
		gridData = new GridData();
		gridData.verticalAlignment = SWT.CENTER;
		gridData.grabExcessVerticalSpace = true;
		selectionLabel.setLayoutData(gridData);

		Composite buttonComposite = new Composite(this, SWT.NONE);
		formData = new FormData();
		formData.top = new FormAttachment(0);
		formData.right = new FormAttachment(100);
		buttonComposite.setLayoutData(formData);
		FillLayout fillLayout = new FillLayout();
		fillLayout.type = SWT.HORIZONTAL;
		buttonComposite.setLayout(fillLayout);

		magnifyButton = new Button(buttonComposite, SWT.FLAT);
		magnifyButton.setText(Messages.timeSelector_magnify);
		magnifyButton.addListener(SWT.Selection, new LoggingAdaptor() {
			@Override
			public void loggingHandleEvent(Event event) {
				if (!coordinateTransform.isReady()) {
					return;
				}

				coordinateTransform.gestureBegin(canvas.getSize().x / 2);
				coordinateTransform.gestureMagnify(DEFAULT_BUTTON_MAGNIFICATION);
				coordinateTransform.gestureEnd();

				setSlider();
				clearAndRedraw();
			}
		});

		reduceButton = new Button(buttonComposite, SWT.FLAT);
		reduceButton.setText(Messages.timeSelector_shrink);
		reduceButton.addListener(SWT.Selection, new LoggingAdaptor() {
			@Override
			public void loggingHandleEvent(Event event) {
				if (!coordinateTransform.isReady()) {
					return;
				}

				coordinateTransform.gestureBegin(canvas.getSize().x / 2);
				coordinateTransform.gestureMagnify(1.0d / DEFAULT_BUTTON_MAGNIFICATION);
				coordinateTransform.gestureEnd();

				setSlider();
				clearAndRedraw();
			}
		});

		Composite displayComposite = new Composite(this, SWT.BORDER);
		formData = new FormData();
		formData.top = new FormAttachment(massSpecComposite);
		formData.left = new FormAttachment(0);
		formData.right = new FormAttachment(100);
		formData.bottom = new FormAttachment(100);
		displayComposite.setLayoutData(formData);
		displayComposite.setLayout(new FormLayout());

		slider = new Slider(displayComposite, SWT.HORIZONTAL);
		formData = new FormData();
		formData.left = new FormAttachment(0);
		formData.right = new FormAttachment(100);
		formData.bottom = new FormAttachment(100);
		slider.setLayoutData(formData);
		slider.addListener(SWT.Selection, new LoggingAdaptor() {
			@Override
			public void loggingHandleEvent(Event event) {
				if (coordinateTransform.isReady() && event.detail == SWT.DRAG) {
					coordinateTransform.updateVisibleDataRange(slider.getSelection(), slider.getSelection() + slider.getThumb());
					clearAndRedraw();
				}
			}
		});

		canvas = new Canvas(displayComposite, SWT.NO_BACKGROUND);
		formData = new FormData();
		formData.top = new FormAttachment(0);
		formData.left = new FormAttachment(0);
		formData.right = new FormAttachment(100);
		formData.bottom = new FormAttachment(slider);
		canvas.setLayoutData(formData);
		canvas.addControlListener(new LoggingControlAdaptor() {
			public void loggingControlResized(ControlEvent e) {
				if (coordinateTransform.isReady()) {
					coordinateTransform.updateCanvasSizeX(canvas.getSize().x);
					hover.reset();
					clearAndRedraw();
				}
			}
		});
		canvas.addPaintListener(new LoggingPaintAdaptor() {
			public void loggingPaintControl(PaintEvent e) {
				TimeSelector.this.paintControl(e);
			}
		});
		canvas.addGestureListener(new LoggingGestureAdaptor() {
			@Override
			public void loggingGesture(GestureEvent e) {
				if (!coordinateTransform.isReady()) {
					return;
				}

				if (e.detail == SWT.GESTURE_BEGIN) {
					coordinateTransform.gestureBegin(e.x);
					hover.reset();
					clearAndRedraw();

				} else if (e.detail == SWT.GESTURE_MAGNIFY) {
					coordinateTransform.gestureMagnify(e.magnification);
					hover.reset();
					setSlider();
					clearAndRedraw();

				} else if (e.detail == SWT.GESTURE_PAN) {
					coordinateTransform.gesturePan(e.xDirection);
					hover.reset();
					setSlider();
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
				hover.reset();
				clearAndRedraw();
			}
		});
		canvas.addMouseTrackListener(new LoggingMouseTrackAdaptor() {
			public void loggingMouseEnter(MouseEvent e) {
				hover.reset();
				clearAndRedraw();
			}

			public void loggingMouseExit(MouseEvent e) {
				hover.reset();
				clearAndRedraw();
			}

			public void loggingMouseHover(MouseEvent e) {
				if (coordinateTransform.isReady()) {
					findObject.setLocation(e.x, e.y);
					hover.setHoverObject();
					clearAndRedraw();
				}
			}
		});
		canvas.addMouseListener(new LoggingMouseAdaptor() {
			@Override
			public void loggingMouseDoubleClick(MouseEvent e) {
				mouseClick(e);
			}

			@Override
			public void loggingMouseDown(MouseEvent e) {
				mouseClick(e);
			}

			@Override
			public void loggingMouseUp(MouseEvent e) {

			}
		});

		UserCache.getInstance().userListGet(this);
		waitingForMassSpecList = MassSpecCache.getInstance().massSpecListGet(this);
		getChainedPart().setCursor();

		UserCache.getInstance().addListener(this);
		MassSpecCache.getInstance().addListener(this);
		CorrIntervalCache.getInstance().addListener(this);
		InputCache.getInstance().addListener(this);
	}

	@Override
	protected void handleDispose() {
		UserCache.getInstance().removeListener(this);
		MassSpecCache.getInstance().removeListener(this);
		CorrIntervalCache.getInstance().removeListener(this);
		InputCache.getInstance().removeListener(this);
	}

	private void mouseClick(MouseEvent e) {
		if (!renderableItems.isReady() || renderableItems.getSortedRenderableItems().length < 2) {
			return;
		}

		findObject.setLocation(e.x, e.y);
		selection.setSelection(findObject.getReplicateListItem());

		ReplicateListItem replicateListItem = selection.getReplicateListItem();
		CorrIntervalListItem corrIntervalListItem = selection.getCorrIntervalListItem();

		HashMap<String,Object> hashmap = new HashMap<String,Object>();
		hashmap.put(ReplicateAnalysisPart.SELECTION_CORR_INTERVAL_ID, renderableItems.getId(corrIntervalListItem));
		hashmap.put(ReplicateAnalysisPart.SELECTION_REPLICATE_ID, renderableItems.getId(replicateListItem));
		hashmap.put(ReplicateAnalysisPart.SELECTION_REPLICATE_IS_A_STANDARD, replicateListItem != null && replicateListItem.getStandardId() != DatabaseConstants.EMPTY_DB_ID);
		propogateSelection(hashmap);

		if (replicateListItem == null) {
			setSelectionLabel("");

		} else {
			String newLabel = "";

			for (String string : hover.getDisplayInfo(replicateListItem, null, corrIntervalListItem)) {
				newLabel += newLabel.isEmpty() ? string : "    " + string; 
			}

			setSelectionLabel(newLabel);
		}

		clearAndRedraw();
	}

	private void setSelectionLabel(String label) {
		selectionLabel.setText(label);
		selectionLabel.pack();
		selectionLabelFormAttachment.offset = -(selectionLabel.getSize().x / 2);
		layout();
	}

	private void setSlider() {
		if (!coordinateTransform.isReady() || renderableItems.getSortedRenderableItems().length == 0) {
			slider.setValues(0, 0, 99, 100, 0, 0);
			slider.setEnabled(false);
			return;
		}

		int timeInMinutesAtFirstPixel = coordinateTransform.pixelXToTimeInMinutes(0);
		int timeInMinutesAtLastPixel = coordinateTransform.pixelXToTimeInMinutes(canvas.getSize().x - 1);

		int visibleRange = timeInMinutesAtLastPixel - timeInMinutesAtFirstPixel + 1;

		slider.setValues(timeInMinutesAtFirstPixel, renderableItems.getStartTimeInMinutes(), renderableItems.getEndTimeInMinutes(), visibleRange, (int) (visibleRange / 100.0d), visibleRange);
		slider.setEnabled(true);	
	}

	private void clearAndRedraw() {
		if (alreadyDrawing) {
			return;
		}

		if (imageCache != null) {
			imageCache.dispose();
			imageCache = null;
		}

		canvas.redraw();
	}

	private void setTransform() {
		DateAndObject[] sortedRenderableItems = renderableItems.getSortedRenderableItems();

		if (sortedRenderableItems.length <= 1) {
			coordinateTransform.reset();
			magnifyButton.setEnabled(false);
			reduceButton.setEnabled(false);
			return;
		}

		int firstIndex = (sortedRenderableItems.length <= DEFAULT_NUM_OBJECTS_TO_SHOW) ? 0 : sortedRenderableItems.length - DEFAULT_NUM_OBJECTS_TO_SHOW;

		int timeInMinutesAtFirstPixel = sortedRenderableItems[firstIndex].getTimeInMinutes();
		int timeInMinutesAtLastPixel = sortedRenderableItems[sortedRenderableItems.length-1].getTimeInMinutes();

		coordinateTransform.initTransform(canvas.getSize().x, renderableItems.getStartTimeInMinutes(), renderableItems.getEndTimeInMinutes(), timeInMinutesAtFirstPixel, timeInMinutesAtLastPixel);
		magnifyButton.setEnabled(true);
		reduceButton.setEnabled(true);

		setSlider();
		clearAndRedraw();
	}

	private void paintControl(PaintEvent e) {
		if (imageCache == null) {
			alreadyDrawing = true;

			imageCache = new Image(getDisplay(), canvas.getSize().x, canvas.getSize().y);
			GC gc = new GC(imageCache);

			Font font = new Font(getDisplay(), "Arial", 12, SWT.NORMAL);
			gc.setFont(font);
			gc.setAntialias(SWT.ON);
			gc.setTextAntialias(SWT.ON);

			yAxisTypes.setImageParams(gc, canvas.getSize().y);

			if (coordinateTransform.isReady() && renderableItems.getSortedRenderableItems().length != 0) {
				magnifyButton.setEnabled(true);
				reduceButton.setEnabled(true);

				selection.renderSelection(gc, canvas.getSize().x);
				
				int textOffset = 2;
				String timeZone = LoginInfoCache.getInstance().getPreferences().getTimeZoneId();
				boolean showTimeZone = LoginInfoCache.getInstance().getPreferences().getShowTimeZone();
				gc.setForeground(ColorCache.getColor(getDisplay(), ColorCache.BLACK));
				gc.setBackground(ColorCache.getColor(getDisplay(), ColorCache.WHITE));

				long leftDate = timeInMinutesToDate(coordinateTransform.pixelXToTimeInMinutes(0));
				String timestamp = DateFormat.format(leftDate, timeZone, showTimeZone, false);
				Point textExtent = gc.textExtent(timestamp);
				int textX = textOffset;
				int textY = canvas.getSize().y - textExtent.y - textOffset;
				gc.drawText(timestamp, textX, textY);

				long rightDate = timeInMinutesToDate(coordinateTransform.pixelXToTimeInMinutes(canvas.getSize().x - 1));
				timestamp = DateFormat.format(rightDate, timeZone, showTimeZone, false);
				textExtent = gc.textExtent(timestamp);
				textX = canvas.getSize().x - textExtent.x - textOffset;
				gc.drawText(timestamp, textX, textY);

				long middleDate = rightDate / 2 + leftDate / 2 + (rightDate % 2 == 1 && leftDate % 2 == 1 ? 1 : 0);
				timestamp = DateFormat.format(middleDate, timeZone, showTimeZone, false);
				textExtent = gc.textExtent(timestamp);
				textX = canvas.getSize().x / 2 - textExtent.x / 2;
				gc.drawText(timestamp, textX, textY);

				// TODO reduce range with Arrays.binarySearch();

				for (DateAndObject dateAndObject : renderableItems.getSortedRenderableItems()) {
					Object item = dateAndObject.getObject();

					if (item instanceof CorrIntervalListItem) {
						CorrIntervalListItem correctionIntervalListItem = (CorrIntervalListItem) item;
						int timeInMinutes = dateToTimeInMinutes(correctionIntervalListItem.getDate());
						int x = coordinateTransform.timeInMinutesToPixelX(timeInMinutes);

						gc.setBackground(ColorCache.getColor(getDisplay(), ColorCache.BLACK));
						gc.fillPolygon(new int[] { x, yAxisTypes.getCorrIntervalTopY(), x+3, yAxisTypes.getCorrIntervalBaseY(), x-3, yAxisTypes.getCorrIntervalBaseY() });

					} else if (item instanceof ReplicateListItem) {
						ReplicateListItem replicateListItem = (ReplicateListItem) item;
						int timeInMinutes = dateToTimeInMinutes(replicateListItem.getDate());
						int x = coordinateTransform.timeInMinutesToPixelX(timeInMinutes);
						
						if (!pointDesignCache.containsKey(replicateListItem.getStandardId())) {
							pointDesignCache.put(replicateListItem.getStandardId(), null);
							StandardCache.getInstance().standardGet(replicateListItem.getStandardId(), this);
						}

						PointDesign pointDesign = pointDesignCache.get(replicateListItem.getStandardId());

						if (pointDesign != null) {
							if (replicateListItem.getStandardId() == DatabaseConstants.EMPTY_DB_ID) {
								pointDesign.draw(gc, x, yAxisTypes.getSamplesY(), false);
							} else {
								pointDesign.draw(gc, x, yAxisTypes.getStandardsY(), false);
							}
						}

					} else if (item instanceof ScanListItem) {
						ScanListItem scanListItem = (ScanListItem) item;
						int timeInMinutes = dateToTimeInMinutes(scanListItem.getDate());
						int x = coordinateTransform.timeInMinutesToPixelX(timeInMinutes);

						gc.setBackground(ColorCache.getColor(getDisplay(), ColorCache.BLACK));
						scanPointDesign.draw(gc, x, yAxisTypes.getScansY(), false);
					}
				}
			}

			hover.drawHoverInfo(gc, canvas.getSize().x);

			font.dispose();
			gc.dispose();

			alreadyDrawing = false;
		}

		e.gc.drawImage(imageCache, 0, 0);
	}

	private long timeInMinutesToDate(int timeInMinutes) {
		return ((long) timeInMinutes) * 1000 * 60;
	}

	private int dateToTimeInMinutes(long date) {
		return (int) (date / 1000 / 60);
	}

	private void setMassSpecList(MassSpecList massSpecList) {
		massSpec.setPossibilities(massSpecList);

		if (massSpec.getSelectedInteger() == -1) {
			massSpec.selectFirst();
			setMassSpec(massSpec.getSelectedInteger());
		}
	}

	private void setMassSpec(int massSpecId) {
		if (massSpecId == -1) {
			return;
		}

		coordinateTransform.reset();
		renderableItems.reset();
		hover.reset();
		selection.reset();

		HashMap<String,Object> selection = new HashMap<String,Object>();
		selection.put(ReplicateAnalysisPart.SELECTION_CORR_INTERVAL_ID, null);
		selection.put(ReplicateAnalysisPart.SELECTION_REPLICATE_ID, null);
		selection.put(ReplicateAnalysisPart.SELECTION_REPLICATE_IS_A_STANDARD, null);
		propogateSelection(selection);

		boolean canEditAllReplicates = LoginInfoCache.getInstance().getPermissions().isCanEditAllReplicates();
		int userId = canEditAllReplicates ? DatabaseConstants.EMPTY_DB_ID : LoginInfoCache.getInstance().getUser().getId();

		waitingForSampleList = InputCache.getInstance().replicateListGet(this, true, DatabaseConstants.EMPTY_DB_ID, massSpecId, userId);
		waitingForStandardList = InputCache.getInstance().replicateListGet(this, false, DatabaseConstants.EMPTY_DB_ID, massSpecId, DatabaseConstants.EMPTY_DB_ID);
		waitingForScanList = InputCache.getInstance().scanListGet(this, massSpecId);
		waitingForCorrIntervalList = CorrIntervalCache.getInstance().corrIntervalListGet(massSpecId, this);

		getChainedPart().setCursor();
		setSlider();
		clearAndRedraw();
	}

	@Override
	public void massSpecListGetCompleted(int commandId, MassSpecList massSpecList) {
		if (commandId == Command.UNDEFINED_ID || commandId == waitingForMassSpecList) {
			setMassSpecList(massSpecList);
			waitingForMassSpecList = Command.UNDEFINED_ID;
			getChainedPart().setCursor();
		}
	}

	@Override
	public void massSpecListUpdated(int commandId, MassSpecList massSpecList) {
		if (waitingForMassSpecList == Command.UNDEFINED_ID) {
			setMassSpecList(massSpecList);
		}
	}

	@Override
	public void massSpecListGetError(int commandId, String message) {
		if (commandId == Command.UNDEFINED_ID || commandId == waitingForMassSpecList) {
			getEasotopePart().raiseError(message);
			waitingForMassSpecList = Command.UNDEFINED_ID;
			getChainedPart().setCursor();
		}
	}

	@Override
	public void corrIntervalListGetCompleted(int commandId, CorrIntervalList corrIntervalList) {
		if (commandId == Command.UNDEFINED_ID || commandId == waitingForCorrIntervalList) {
			waitingForCorrIntervalList = Command.UNDEFINED_ID;
			getChainedPart().setCursor();

			renderableItems.setCorrIntervalList(corrIntervalList);
			setTransform();
		}
	}

	@Override
	public void corrIntervalListUpdated(int commandId, CorrIntervalList corrIntervalList) {
		if (waitingForCorrIntervalList == Command.UNDEFINED_ID) {
			renderableItems.setCorrIntervalList(corrIntervalList);

			if (coordinateTransform.isReady()) {
				coordinateTransform.updateDataRange(renderableItems.getStartTimeInMinutes(), renderableItems.getEndTimeInMinutes());

				selection.updateReplicateListItem();
				HashMap<String,Object> hashmap = new HashMap<String,Object>();
				hashmap.put(ReplicateAnalysisPart.SELECTION_CORR_INTERVAL_ID, renderableItems.getId(selection.getCorrIntervalListItem()));
				hashmap.put(ReplicateAnalysisPart.SELECTION_REPLICATE_ID, renderableItems.getId(selection.getReplicateListItem()));
				hashmap.put(ReplicateAnalysisPart.SELECTION_REPLICATE_IS_A_STANDARD, selection.getReplicateListItem() != null && selection.getReplicateListItem().getStandardId() != DatabaseConstants.EMPTY_DB_ID);
				propogateSelection(hashmap);

				hover.reset();
				setSlider();
				clearAndRedraw();
			}
		}
	}

	@Override
	public void corrIntervalListGetError(int commandId, String message) {
		if (commandId == Command.UNDEFINED_ID || commandId == waitingForCorrIntervalList) {
			waitingForCorrIntervalList = Command.UNDEFINED_ID;
			getChainedPart().setCursor();

			getEasotopePart().raiseError(message);
		}
	}

	@Override
	public void scanListGetCompleted(int commandId, ScanList scanList) {
		if (commandId == Command.UNDEFINED_ID || commandId == waitingForScanList) {
			waitingForScanList = Command.UNDEFINED_ID;
			getChainedPart().setCursor();

			renderableItems.setScanList(scanList);
			setTransform();
		}
	}

	@Override
	public void scanListUpdated(int commandId, ScanList scanList) {
		if (waitingForScanList == Command.UNDEFINED_ID) {
			renderableItems.setScanList(scanList);

			if (coordinateTransform.isReady()) {
				coordinateTransform.updateDataRange(renderableItems.getStartTimeInMinutes(), renderableItems.getEndTimeInMinutes());
				hover.reset();
				setSlider();
				clearAndRedraw();
			}
		}
	}

	@Override
	public void scanListGetError(int commandId, String message) {
		if (commandId == Command.UNDEFINED_ID || commandId == waitingForScanList) {
			waitingForScanList = Command.UNDEFINED_ID;
			getChainedPart().setCursor();
			
			getEasotopePart().raiseError(message);
		}
	}

	@Override
	public void replicateListGetCompleted(int commandId, ReplicateList replicateList) {
		if (replicateList.isGetSamples()) {
			if (commandId == Command.UNDEFINED_ID || commandId == waitingForSampleList) {
				renderableItems.setSampleList(replicateList);
				waitingForSampleList = Command.UNDEFINED_ID;
				setTransform();
			}
		} else {
			if (commandId == Command.UNDEFINED_ID || commandId == waitingForStandardList) {
				renderableItems.setStandardList(replicateList);
				waitingForStandardList = Command.UNDEFINED_ID;
				setTransform();
			}
		}

		getChainedPart().setCursor();
	}

	@Override
	public void replicateListUpdated(int commandId, ReplicateList replicateList) {
		int massSpecId = massSpec.getSelectedInteger();
		boolean canEditAllReplicates = LoginInfoCache.getInstance().getPermissions().isCanEditAllReplicates();
		int userId = canEditAllReplicates ? DatabaseConstants.EMPTY_DB_ID : LoginInfoCache.getInstance().getUser().getId();

		if (replicateList.isGetSamples() && replicateList.getSampleId() == DatabaseConstants.EMPTY_DB_ID && replicateList.getMassSpecId() == massSpecId && replicateList.getUserId() == userId) {
			renderableItems.setSampleList(replicateList);

			if (coordinateTransform.isReady()) {
				coordinateTransform.updateDataRange(renderableItems.getStartTimeInMinutes(), renderableItems.getEndTimeInMinutes());

				selection.updateReplicateListItem();
				HashMap<String,Object> hashmap = new HashMap<String,Object>();
				hashmap.put(ReplicateAnalysisPart.SELECTION_CORR_INTERVAL_ID, renderableItems.getId(selection.getCorrIntervalListItem()));
				hashmap.put(ReplicateAnalysisPart.SELECTION_REPLICATE_ID, renderableItems.getId(selection.getReplicateListItem()));
				hashmap.put(ReplicateAnalysisPart.SELECTION_REPLICATE_IS_A_STANDARD, selection.getReplicateListItem() != null && selection.getReplicateListItem().getStandardId() != DatabaseConstants.EMPTY_DB_ID);
				propogateSelection(hashmap);

				hover.reset();
				setSlider();
				clearAndRedraw();
			}
		}

		if (!replicateList.isGetSamples() && replicateList.getSampleId() == DatabaseConstants.EMPTY_DB_ID && replicateList.getMassSpecId() == massSpecId && replicateList.getUserId() == DatabaseConstants.EMPTY_DB_ID) {
			renderableItems.setStandardList(replicateList);

			if (coordinateTransform.isReady()) {
				coordinateTransform.updateDataRange(renderableItems.getStartTimeInMinutes(), renderableItems.getEndTimeInMinutes());

				selection.updateReplicateListItem();
				HashMap<String,Object> hashmap = new HashMap<String,Object>();
				hashmap.put(ReplicateAnalysisPart.SELECTION_CORR_INTERVAL_ID, renderableItems.getId(selection.getCorrIntervalListItem()));
				hashmap.put(ReplicateAnalysisPart.SELECTION_REPLICATE_ID, renderableItems.getId(selection.getReplicateListItem()));
				hashmap.put(ReplicateAnalysisPart.SELECTION_REPLICATE_IS_A_STANDARD, selection.getReplicateListItem() != null && selection.getReplicateListItem().getStandardId() != DatabaseConstants.EMPTY_DB_ID);
				propogateSelection(hashmap);

				hover.reset();
				setSlider();
				clearAndRedraw();
			}
		}
	}

	@Override
	public void replicateListGetError(int commandId, String message) {
		if (commandId == Command.UNDEFINED_ID || commandId == waitingForSampleList || commandId == waitingForStandardList) {
			getEasotopePart().raiseError(message);
		}

		if (commandId == waitingForSampleList) {
			waitingForSampleList = Command.UNDEFINED_ID;
		}

		if (commandId == waitingForStandardList) {
			waitingForStandardList = Command.UNDEFINED_ID;
		}
		
		getChainedPart().setCursor();
	}

	@Override
	public void standardGetCompleted(int commandId, final Standard standard) {
		pointDesignCache.put(standard.getId(), new PointDesign(getDisplay(), standard.getColorId(), PointStyle.values()[standard.getShapeId()]));
		hover.setStandardName(standard.getId(), standard.getName());
		clearAndRedraw();
	}

	@Override
	public void standardUpdated(int commandId, final Standard standard) {
		pointDesignCache.put(standard.getId(), new PointDesign(getDisplay(), standard.getColorId(), PointStyle.values()[standard.getShapeId()]));
		hover.setStandardName(standard.getId(), standard.getName());
		clearAndRedraw();
	}

	@Override
	public void standardGetError(int commandId, String message) {
		getEasotopePart().raiseError(message);
	}

	@Override
	public void userListGetCompleted(int commandId, UserList userList) {
		hover.setUserList(userList);
	}

	@Override
	public void userListUpdated(int commandId, UserList userList) {
		hover.setUserList(userList);
	}

	@Override
	public void userListGetError(int commandId, String message) {
		getEasotopePart().raiseError(message);
	}

	@Override
	public boolean stillCallabled() {
		return !isDisposed();
	}

	@Override
	public boolean isWaiting() {
		return waitingForMassSpecList != Command.UNDEFINED_ID
				|| waitingForSampleList != Command.UNDEFINED_ID
				|| waitingForStandardList != Command.UNDEFINED_ID
				|| waitingForCorrIntervalList != Command.UNDEFINED_ID;
	}

	@Override
	public void setWidgetsEnabled() {
		massSpec.setEnabled(true);
		magnifyButton.setEnabled(true);
		reduceButton.setEnabled(true);
		canvas.setEnabled(true);
		slider.setEnabled(true);
	}

	@Override
	public void setWidgetsDisabled() {
		massSpec.setEnabled(false);
		magnifyButton.setEnabled(false);
		reduceButton.setEnabled(false);
		canvas.setEnabled(false);
		slider.setEnabled(false);
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
		// ignore
	}

	@Override
	public void loginInfoUpdated(int commandId, User user, Permissions permissions, Preferences preferences) {
		//TODO what to do here?
	}
}
