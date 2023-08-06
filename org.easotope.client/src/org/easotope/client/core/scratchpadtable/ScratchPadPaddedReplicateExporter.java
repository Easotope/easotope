package org.easotope.client.core.scratchpadtable;

import java.io.IOException;
import java.io.OutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.easotope.shared.core.cache.logininfo.LoginInfoCache;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.extension.poi.HSSFExcelExporter;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;

public class ScratchPadPaddedReplicateExporter extends HSSFExcelExporter {
	private final Pattern pattern = Pattern.compile("^([A-Z])(\\d+)$"); 

	private int exportPadding;
	private int currentRow;
	private boolean exportingReplicates = false;
	private int replicatesRemaining;

	@Override
	public void exportBegin(OutputStream outputStream) throws IOException {
		exportPadding = LoginInfoCache.getInstance().getPreferences().getExportPadding();
		exportingReplicates = false;
		super.exportBegin(outputStream);
	}

	@Override
	public void exportLayerBegin(OutputStream outputStream, String layerName) throws IOException {
		if (exportPadding == 0) {
			super.exportLayerBegin(outputStream, layerName);
			return;
		}

		currentRow = -1;		
		super.exportLayerBegin(outputStream, layerName);
	}

	@Override
	public void exportRowBegin(OutputStream outputStream, int rowPosition) throws IOException {
		if (exportPadding == 0) {
			super.exportRowBegin(outputStream, rowPosition);
			return;
		}

		currentRow++;
		super.exportRowBegin(outputStream, currentRow);
	}

	@Override
	public void exportRowEnd(OutputStream outputStream, int rowPosition) throws IOException {
		if (exportPadding == 0) {
			super.exportRowEnd(outputStream, rowPosition);
			return;
		}

		super.exportRowEnd(outputStream, currentRow);
	}

	@Override
	public void exportCell(OutputStream outputStream, Object exportDisplayValue, ILayerCell cell, IConfigRegistry configRegistry) throws IOException {
		if (exportPadding == 0) {
			super.exportCell(outputStream, exportDisplayValue, cell, configRegistry);
			return;
		}

		if (cell.getColumnPosition() == 0) {
			String type = "";

			if (exportDisplayValue != null) {
				Matcher matcher = pattern.matcher(exportDisplayValue.toString());

				if (matcher.matches()) {
					type = matcher.group(1);
				}
			}

			if (!type.equals("R") && exportingReplicates) {
				for (; replicatesRemaining>0; replicatesRemaining--) {
					currentRow++;
					this.xlRow = this.xlSheet.createRow(currentRow);
				}
			}

			switch (type) {
				case "R":
					replicatesRemaining--;
					break;

				case "S":
					exportingReplicates = true;
					replicatesRemaining = exportPadding;
					break;
					
				default:
					exportingReplicates = false;
					break;
			}
		}

		super.exportCell(outputStream, exportDisplayValue, cell, configRegistry);
	}
}
