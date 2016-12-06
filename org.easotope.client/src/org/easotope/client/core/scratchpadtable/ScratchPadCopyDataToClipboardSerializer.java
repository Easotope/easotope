/*******************************************************************************
 * Copyright (c) 2012 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.easotope.client.core.scratchpadtable;

import org.easotope.shared.core.DoubleTools;
import org.eclipse.nebula.widgets.nattable.copy.command.CopyDataToClipboardCommand;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.serializing.ISerializer;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;

public class ScratchPadCopyDataToClipboardSerializer implements ISerializer {

    private final ILayerCell[][] copiedCells;
    private final CopyDataToClipboardCommand command;

    public ScratchPadCopyDataToClipboardSerializer(ILayerCell[][] copiedCells,
            CopyDataToClipboardCommand command) {
        this.copiedCells = copiedCells;
        this.command = command;
    }

    @Override
    public void serialize() {
        final String cellDelimeter = this.command.getCellDelimeter();
        final String rowDelimeter = this.command.getRowDelimeter();

        final TextTransfer textTransfer = TextTransfer.getInstance();
        final StringBuilder textData = new StringBuilder();
        int currentRow = 0;
        for (ILayerCell[] cells : this.copiedCells) {
            int currentCell = 0;
            for (ILayerCell cell : cells) {
                final String delimeter = ++currentCell < cells.length ? cellDelimeter
                        : ""; //$NON-NLS-1$
                if (cell != null) {
                    textData.append(getTextForCell(cell) + delimeter);
                } else {
                    textData.append(delimeter);
                }
            }
            if (++currentRow < this.copiedCells.length) {
                textData.append(rowDelimeter);
            }
        }
        if (textData.length() > 0) {
            final Clipboard clipboard = new Clipboard(Display.getDefault());
            try {
                clipboard.setContents(new Object[] { textData.toString() },
                        new Transfer[] { textTransfer });
            } finally {
                clipboard.dispose();
            }
        }
    }

    protected String getTextForCell(ILayerCell cell) {
    		String string = String.valueOf(cell.getDataValue());
    		
    		if (string == null) {
    			return string;
    		}

    		string = DoubleTools.removeLeadingExponent(string);

		// special
    		string = string.replace('δ', 'd');
    		string = string.replace('Δ', 'D');

		// subscript
    		string = string.replace('₂', '2');

		// superscript
    		string = string.replace('¹', '1');
    		string = string.replace('³', '3');
    		string = string.replace('⁸', '8');
    		
    		return string;
    }

    final protected CopyDataToClipboardCommand getCommand() {
        return this.command;
    }
}
