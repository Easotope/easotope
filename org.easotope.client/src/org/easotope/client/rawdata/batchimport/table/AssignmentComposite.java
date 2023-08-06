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

package org.easotope.client.rawdata.batchimport.table;

import java.util.ArrayList;
import java.util.HashMap;

import org.easotope.client.core.GuiConstants;
import org.easotope.client.core.PointDesign;
import org.easotope.client.core.PointStyle;
import org.easotope.client.core.adaptors.LoggingPaintAdaptor;
import org.easotope.client.core.adaptors.LoggingSelectionAdaptor;
import org.easotope.client.rawdata.batchimport.assignmentdialog.AssignmentDialog;
import org.easotope.shared.admin.cache.standard.StandardCache;
import org.easotope.shared.admin.cache.standard.standard.StandardCacheStandardGetListener;
import org.easotope.shared.admin.tables.Standard;
import org.easotope.shared.rawdata.cache.sourcelist.sourcelist.SourceListItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;

public class AssignmentComposite extends Composite implements StandardCacheStandardGetListener {
	private Canvas canvas;
	private Image blankImage;
	private HashMap<Integer,Image> standardIdToImage = new HashMap<Integer,Image>();
	private Combo combo;
	private HashMap<Integer,SourceListItem> indexToSourceListItem = new HashMap<Integer,SourceListItem>();
	private HashMap<String,SourceListItem> idToSourceListItem = new HashMap<String,SourceListItem>();
	private HashMap<String,Integer> idToIndex = new HashMap<String,Integer>();
	private Button button;

	public AssignmentComposite(Table table, int style) {
		super(table, style);
		GridLayout gridLayout = new GridLayout(3, false);
		gridLayout.marginLeft = 7;
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		setLayout(gridLayout);

	    Image src = new Image(getParent().getDisplay(), 9, 7);        
	    ImageData imageData = src.getImageData();
	    imageData.transparentPixel = imageData.getPixel(0, 0);
	    src.dispose();
	    blankImage = new Image(getParent().getDisplay(), imageData);

		canvas = new Canvas(this, SWT.NONE);
		GridData gridData = new GridData();
		gridData.widthHint = 9;
		gridData.heightHint = 7;
		canvas.setLayoutData(gridData);
		canvas.setVisible(true);
		canvas.addPaintListener(new LoggingPaintAdaptor() {
			public void loggingPaintControl(PaintEvent e) {
				e.gc.setAntialias(SWT.ON);

				SourceListItem sourceListItem = indexToSourceListItem.get(combo.getSelectionIndex());
				Image image = standardIdToImage.get(sourceListItem.getStandardId());

				e.gc.drawImage((image != null) ? image : blankImage, 0, 0);
			}
		});

		combo = new Combo(this, SWT.READ_ONLY);
		gridData = new GridData();
		gridData.widthHint = GuiConstants.LONG_COMBO_INPUT_WIDTH;
		gridData.grabExcessVerticalSpace = true;
		gridData.verticalAlignment = SWT.CENTER;
		combo.setLayoutData(gridData);
		combo.addSelectionListener(new LoggingSelectionAdaptor() {
			public void loggingWidgetSelected(SelectionEvent e) {
				int index = combo.getSelectionIndex();
				SourceListItem sourceListItem = indexToSourceListItem.get(index);

				if (sourceListItem.isStandard() && !standardIdToImage.containsKey(sourceListItem.getStandardId())) {
					StandardCache.getInstance().standardGet(sourceListItem.getStandardId(), AssignmentComposite.this);
				}

				canvas.redraw();
			}
		});

		button = new Button(this, SWT.NONE);
		gridData = new GridData();
		gridData.grabExcessVerticalSpace = true;
		gridData.verticalAlignment = SWT.CENTER;
		button.setLayoutData(gridData);
		button.addSelectionListener(new LoggingSelectionAdaptor() {
			public void loggingWidgetSelected(SelectionEvent e) {
				int index = combo.getSelectionIndex();
				SourceListItem sourceListItem = indexToSourceListItem.get(index);

				AssignmentDialog assignmentDialog = new AssignmentDialog(getShell());
				assignmentDialog.open(sourceListItem);

				SourceListItem newSourceListItem = assignmentDialog.getSourceListItem();

				if (newSourceListItem != null) {
					
				}
			}
		});
		button.setText("Other");

		pack();

		StandardCache.getInstance().addListener(this);
	}

	@Override
	public void dispose() {
		super.dispose();
		StandardCache.getInstance().removeListener(this);
	}

	public void setComboItems(ArrayList<SourceListItem> sourceList) {
		combo.removeAll();

		indexToSourceListItem.clear();
		idToSourceListItem.clear();
		idToIndex.clear();

		for (SourceListItem sourceListItem : sourceList) {
			indexToSourceListItem.put(combo.getItemCount(), sourceListItem);
			String id = sourceListItem.getSampleId() + " " + sourceListItem.getStandardId();
			idToSourceListItem.put(id, sourceListItem);
			idToIndex.put(id, combo.getItemCount());
			combo.add(sourceListItem.toString());
		}

		combo.deselectAll();
	}

	public void selectSampleIdStandardId(int sampleId, int standardId) {
		String id = sampleId + " " + standardId;

		int index = idToIndex.get(id);
		combo.select(index);

		SourceListItem sourceListItem = idToSourceListItem.get(id);

		if (sourceListItem.isStandard() && !standardIdToImage.containsKey(sourceListItem.getStandardId())) {
			StandardCache.getInstance().standardGet(sourceListItem.getStandardId(), this);
		}

		canvas.redraw();
	}

	@Override
	public boolean stillCallabled() {
		return !isDisposed();
	}

	private void addStandardImageToCache(Standard standard) {
	    Image src = new Image(getParent().getDisplay(), 9, 7);        
	    ImageData imageData = src.getImageData();
	    imageData.transparentPixel = imageData.getPixel(0, 0);
	    src.dispose();
	    Image image = new Image(getParent().getDisplay(), imageData);

	    standardIdToImage.put(standard.getId(), image);

		GC gc = new GC(image);
		new PointDesign(getParent().getDisplay(), standard.getColorId(), PointStyle.values()[standard.getShapeId()]).draw(gc, 3, 3, false);
		gc.dispose();

		canvas.redraw();
	}

	private boolean sourceListItemsAreTheSame(SourceListItem sourceListItem1, SourceListItem sourceListItem2) {
		if (sourceListItem1.getUserId() != sourceListItem2.getUserId()) {
			return false;
		}

		if (sourceListItem1.getProjectId() != sourceListItem2.getProjectId()) {
			return false;
		}

		if (sourceListItem1.getSampleId() != sourceListItem2.getSampleId()) {
			return false;
		}

		if (sourceListItem1.getStandardId() != sourceListItem2.getStandardId()) {
			return false;
		}

		return true;
	}

	@Override
	public void standardGetCompleted(int commandId, Standard standard) {
		addStandardImageToCache(standard);
	}

	@Override
	public void standardUpdated(int commandId, Standard standard) {
		addStandardImageToCache(standard);
	}

	@Override
	public void standardGetError(int commandId, String message) {
		// ignore
	}
}
