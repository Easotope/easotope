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

package org.easotope.client.dialog;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.easotope.client.Messages;
import org.easotope.client.core.GuiConstants;
import org.easotope.framework.core.logging.Log;
import org.easotope.framework.core.logging.Log.Level;
import org.easotope.framework.core.util.Reflection;
import org.easotope.framework.dbcore.DatabaseConstants;
import org.easotope.framework.dbcore.cmdprocessors.FolderProcessor;
import org.easotope.framework.dbcore.cmdprocessors.ProcessorManager;
import org.easotope.framework.dbcore.tables.RawFile;
import org.easotope.framework.dbcore.tables.User;
import org.easotope.framework.dbcore.util.RawFileManager;
import org.easotope.shared.admin.tables.AcidTemp;
import org.easotope.shared.admin.tables.MassSpec;
import org.easotope.shared.admin.tables.RefGas;
import org.easotope.shared.admin.tables.SampleType;
import org.easotope.shared.admin.tables.Standard;
import org.easotope.shared.analysis.tables.CalcRepToCalcSamp;
import org.easotope.shared.analysis.tables.CalcReplicateCache;
import org.easotope.shared.analysis.tables.CalcSampleCache;
import org.easotope.shared.analysis.tables.CorrIntervalError;
import org.easotope.shared.analysis.tables.CorrIntervalScratchPad;
import org.easotope.shared.analysis.tables.CorrIntervalV1;
import org.easotope.shared.analysis.tables.RepAnalysisChoice;
import org.easotope.shared.analysis.tables.RepStep;
import org.easotope.shared.analysis.tables.RepStepParams;
import org.easotope.shared.analysis.tables.SamStepParams;
import org.easotope.shared.core.tables.Permissions;
import org.easotope.shared.core.tables.Preferences;
import org.easotope.shared.core.tables.TableLayout;
import org.easotope.shared.rawdata.common.DeleteReplicate;
import org.easotope.shared.rawdata.common.DeleteScan;
import org.easotope.shared.rawdata.tables.Project;
import org.easotope.shared.rawdata.tables.ReplicateV1;
import org.easotope.shared.rawdata.tables.Sample;
import org.easotope.shared.rawdata.tables.ScanV3;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.ConnectionSource;

public class TrimDialog extends Dialog {
	private Shell shell;

	private Composite areYouSureComposite;
	private Composite shuttingDownCommandProcessorComposite;
	private Composite scanningDatabaseComposite;
	private Composite trimmingDatabaseComposite;
	private Composite finishedComposite;
	private Composite errorBeforeComposite;
	private Label errorBeforeException;
	private Composite errorAfterComposite;
	private Label errorAfterException;

	private RawFileManager rawFileManager;
	private ConnectionSource connectionSource;

	private HashSet<Integer> acidTempIds = new HashSet<Integer>();
	private HashSet<Integer> corrIntervalIds = new HashSet<Integer>();
	private HashSet<Integer> massSpecIds = new HashSet<Integer>();
	private HashSet<Integer> projectIds = new HashSet<Integer>();
	private HashSet<Integer> refGasIds = new HashSet<Integer>();
	private HashSet<Integer> repAnalysisChoiceIds = new HashSet<Integer>();
	private HashSet<Integer> replicateIds = new HashSet<Integer>();
	private HashSet<Integer> repStepParamsIds = new HashSet<Integer>();
	private HashSet<Integer> sampleTypeIds = new HashSet<Integer>();
	private HashSet<Integer> sampleIds = new HashSet<Integer>();
	private HashSet<Integer> samStepParamsIds = new HashSet<Integer>();
	private HashSet<Integer> scanIds = new HashSet<Integer>();
	private HashSet<Integer> standardIds = new HashSet<Integer>();

	public TrimDialog(Shell shell, int style) {
		super(shell, style);
		setText(Messages.trimDialog_title);
	}

	public TrimDialog(Shell shell) {
		this(shell, SWT.NONE);
	}

	public void open() {
		Display display = getParent().getDisplay();

		shell = new Shell(display, SWT.TITLE | SWT.BORDER | SWT.ON_TOP | SWT.APPLICATION_MODAL);
		shell.setLayout(new StackLayout());
		shell.setText(Messages.trimDialog_title);

		areYouSureComposite = createAreYouSureComposite(shell);
		shuttingDownCommandProcessorComposite = createShuttingDownCommandProcessorComposite(shell);
		scanningDatabaseComposite = createScanningDatabaseComposite(shell);
		trimmingDatabaseComposite = createTrimmingDatabaseComposite(shell);
		finishedComposite = createFinishedComposite(shell);
		errorBeforeComposite = createErrorBeforeComposite(shell);
		errorAfterComposite = createErrorAfterComposite(shell);

		((StackLayout) shell.getLayout()).topControl = areYouSureComposite;
		shell.layout();

		shell.setSize(shell.computeSize(500, SWT.DEFAULT));
		Rectangle bounds = display.getBounds();
		Point size = shell.getSize();
		shell.setLocation((bounds.width - size.x) / 2, (bounds.height - size.y) / 2);
		
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
	}

	private Composite createAreYouSureComposite(Shell shell) {
		Composite composite = new Composite(shell, SWT.NONE);
		FormLayout formLayout = new FormLayout();
		formLayout.marginTop = GuiConstants.FORM_LAYOUT_MARGIN;
		formLayout.marginLeft = GuiConstants.FORM_LAYOUT_MARGIN;
		formLayout.marginRight = GuiConstants.FORM_LAYOUT_MARGIN;
		formLayout.marginBottom = GuiConstants.FORM_LAYOUT_MARGIN;
		composite.setLayout(formLayout);

		Label label = new Label(composite, SWT.WRAP);
		FormData formData = new FormData();
		formData.top = new FormAttachment(0);
		formData.left = new FormAttachment(0);
		formData.right = new FormAttachment(100);
		label.setLayoutData(formData);
		label.setText(Messages.trimDialog_areYouSure);

		Button cancel = new Button(composite, SWT.PUSH);
		formData = new FormData();
		formData.top = new FormAttachment(label);
		formData.right = new FormAttachment(100);
		cancel.setLayoutData(formData);
		cancel.setText(Messages.trimDialog_no);
		cancel.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				shell.dispose();
			}
		});

		Button ok = new Button(composite, SWT.PUSH);
		formData = new FormData();
		formData.top = new FormAttachment(label);
		formData.right = new FormAttachment(cancel, 0, SWT.LEFT);
		ok.setLayoutData(formData);
		ok.setText(Messages.trimDialog_yes);
		ok.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				new TrimThread(shell.getDisplay()).start();
			}
		});

		return composite;
	}

	private Composite createShuttingDownCommandProcessorComposite(Shell shell) {
		Composite composite = new Composite(shell, SWT.NONE);
		composite.setLayout(new GridLayout());

		Label label = new Label(composite, SWT.NONE);
		GridData gridData = new GridData();
		gridData.grabExcessVerticalSpace = true;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = SWT.CENTER;
		gridData.verticalAlignment = SWT.CENTER;
		label.setLayoutData(gridData);
		label.setText(Messages.trimDialog_shuttingDown);

		return composite;
	}

	private Composite createScanningDatabaseComposite(Shell shell) {
		Composite composite = new Composite(shell, SWT.NONE);
		composite.setLayout(new GridLayout());

		Label label = new Label(composite, SWT.NONE);
		GridData gridData = new GridData();
		gridData.grabExcessVerticalSpace = true;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = SWT.CENTER;
		gridData.verticalAlignment = SWT.CENTER;
		label.setLayoutData(gridData);
		label.setText(Messages.trimDialog_scanning);

		return composite;
	}

	private Composite createTrimmingDatabaseComposite(Shell shell) {
		Composite composite = new Composite(shell, SWT.NONE);
		composite.setLayout(new GridLayout());

		Label label = new Label(composite, SWT.NONE);
		GridData gridData = new GridData();
		gridData.grabExcessVerticalSpace = true;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = SWT.CENTER;
		gridData.verticalAlignment = SWT.CENTER;
		label.setLayoutData(gridData);
		label.setText(Messages.trimDialog_trimming);

		return composite;
	}

	private Composite createFinishedComposite(Shell shell) {
		Composite composite = new Composite(shell, SWT.NONE);
		FormLayout formLayout = new FormLayout();
		formLayout.marginTop = GuiConstants.FORM_LAYOUT_MARGIN;
		formLayout.marginLeft = GuiConstants.FORM_LAYOUT_MARGIN;
		formLayout.marginRight = GuiConstants.FORM_LAYOUT_MARGIN;
		formLayout.marginBottom = GuiConstants.FORM_LAYOUT_MARGIN;
		composite.setLayout(formLayout);

		Label label = new Label(composite, SWT.WRAP);
		FormData formData = new FormData();
		formData.top = new FormAttachment(0);
		formData.left = new FormAttachment(0);
		formData.right = new FormAttachment(100);
		label.setLayoutData(formData);
		label.setText(Messages.trimDialog_finished);

		Button ok = new Button(composite, SWT.PUSH);
		formData = new FormData();
		formData.top = new FormAttachment(label);
		formData.right = new FormAttachment(100);
		ok.setLayoutData(formData);
		ok.setText(Messages.trimDialog_ok);
		ok.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				shell.dispose();
				System.exit(0);
			}
		});

		return composite;
	}

	private Composite createErrorBeforeComposite(Shell shell) {
		Composite composite = new Composite(shell, SWT.NONE);
		FormLayout formLayout = new FormLayout();
		formLayout.marginTop = GuiConstants.FORM_LAYOUT_MARGIN;
		formLayout.marginLeft = GuiConstants.FORM_LAYOUT_MARGIN;
		formLayout.marginRight = GuiConstants.FORM_LAYOUT_MARGIN;
		formLayout.marginBottom = GuiConstants.FORM_LAYOUT_MARGIN;
		composite.setLayout(formLayout);

		Label label = new Label(composite, SWT.WRAP);
		FormData formData = new FormData();
		formData.top = new FormAttachment(0);
		formData.left = new FormAttachment(0);
		formData.right = new FormAttachment(100);
		label.setLayoutData(formData);
		label.setText(Messages.trimDialog_errorBefore);

		errorBeforeException = new Label(composite, SWT.WRAP);
		formData = new FormData();
		formData.top = new FormAttachment(label, GuiConstants.INTER_WIDGET_GAP);
		formData.left = new FormAttachment(0);
		formData.right = new FormAttachment(100);
		errorBeforeException.setLayoutData(formData);
		errorBeforeException.setText(Messages.trimDialog_dummyException);

		Button ok = new Button(composite, SWT.PUSH);
		formData = new FormData();
		formData.top = new FormAttachment(errorBeforeException, GuiConstants.INTER_WIDGET_GAP);
		formData.right = new FormAttachment(100);
		ok.setLayoutData(formData);
		ok.setText(Messages.trimDialog_ok);
		ok.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				shell.dispose();
				System.exit(0);
			}
		});

		return composite;
	}

	private Composite createErrorAfterComposite(Shell shell) {
		Composite composite = new Composite(shell, SWT.NONE);
		FormLayout formLayout = new FormLayout();
		formLayout.marginTop = GuiConstants.FORM_LAYOUT_MARGIN;
		formLayout.marginLeft = GuiConstants.FORM_LAYOUT_MARGIN;
		formLayout.marginRight = GuiConstants.FORM_LAYOUT_MARGIN;
		formLayout.marginBottom = GuiConstants.FORM_LAYOUT_MARGIN;
		composite.setLayout(formLayout);

		Label label = new Label(composite, SWT.WRAP);
		FormData formData = new FormData();
		formData.top = new FormAttachment(0);
		formData.left = new FormAttachment(0);
		formData.right = new FormAttachment(100);
		label.setLayoutData(formData);
		label.setText(Messages.trimDialog_errorAfter);

		errorAfterException = new Label(composite, SWT.WRAP);
		formData = new FormData();
		formData.top = new FormAttachment(label, GuiConstants.INTER_WIDGET_GAP);
		formData.left = new FormAttachment(0);
		formData.right = new FormAttachment(100);
		errorAfterException.setLayoutData(formData);
		errorAfterException.setText(Messages.trimDialog_dummyException);

		Button ok = new Button(composite, SWT.PUSH);
		formData = new FormData();
		formData.top = new FormAttachment(errorAfterException, GuiConstants.INTER_WIDGET_GAP);
		formData.right = new FormAttachment(100);
		ok.setLayoutData(formData);
		ok.setText(Messages.trimDialog_ok);
		ok.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent(Event event) {
				shell.dispose();
				System.exit(0);
			}
		});

		return composite;
	}

	class TrimThread extends Thread {
		private Display display;

		TrimThread(Display display) {
			this.display = display;
		}

        public void run() {
        	display.asyncExec(new Runnable() {
				@Override
				public void run() {
		    		((StackLayout) shell.getLayout()).topControl = shuttingDownCommandProcessorComposite;
		    		shell.layout();
				}
        	});

    		FolderProcessor folderProcessor = (FolderProcessor) ProcessorManager.getInstance().getProcessor();

    		String source = folderProcessor.getSource();
    		String jdbcUrl = folderProcessor.getJdbcUrl();

    		folderProcessor.requestStop();

    		while (folderProcessor.isConnected()) {
    			try {
    				Thread.sleep(1000);
    			} catch (InterruptedException e) {
    				// ignore
    			}
    		}

    		try {
    			Thread.sleep(1000);
    		} catch (InterruptedException e1) {
    			// ignore
    		}

    		try {
    			rawFileManager = new RawFileManager(source);
    			connectionSource = FolderProcessor.createConnectionSource(jdbcUrl);

    		} catch (Exception e) {
    			Log.getInstance().log(Level.INFO, this, "error while trimming database", e);

	        	display.asyncExec(new Runnable() {
					@Override
					public void run() {
		    			errorBeforeException.setText(e.getMessage());
			    		((StackLayout) shell.getLayout()).topControl = errorBeforeComposite;
			    		shell.layout();
					}
	        	});

				return;
    		}

        	display.asyncExec(new Runnable() {
				@Override
				public void run() {
		    		((StackLayout) shell.getLayout()).topControl = scanningDatabaseComposite;
		    		shell.layout();
				}
        	});

        	try {
				markAll();

			} catch (Exception e) {
    			Log.getInstance().log(Level.INFO, this, "error while trimming database", e);

	        	display.asyncExec(new Runnable() {
					@Override
					public void run() {
		    			errorBeforeException.setText(e.getMessage());
			    		((StackLayout) shell.getLayout()).topControl = errorBeforeComposite;
			    		shell.layout();
					}
	        	});

				return;
			}
 
        	display.asyncExec(new Runnable() {
				@Override
				public void run() {
		    		((StackLayout) shell.getLayout()).topControl = trimmingDatabaseComposite;
		    		shell.layout();
				}
        	});

    		try {
				removeFromTables();

			} catch (Exception e) {
    			Log.getInstance().log(Level.INFO, this, "error while trimming database", e);
				
	        	display.asyncExec(new Runnable() {
					@Override
					public void run() {
		    			errorAfterException.setText(e.getMessage());
			    		((StackLayout) shell.getLayout()).topControl = errorAfterComposite;
			    		shell.layout();
					}
	        	});

				return;
			}
 
    		try {
    			connectionSource.close();
 
    		} catch (Exception e) {
    			Log.getInstance().log(Level.INFO, this, "error while trimming database", e);

	        	display.asyncExec(new Runnable() {
					@Override
					public void run() {
		    			errorAfterException.setText(e.getMessage());
			    		((StackLayout) shell.getLayout()).topControl = errorAfterComposite;
			    		shell.layout();
					}
	        	});

				return;
    		}

    		try {
				copyDatabase(source, jdbcUrl);

			} catch (Exception e) {
    			Log.getInstance().log(Level.INFO, this, "error while trimming database", e);

	        	display.asyncExec(new Runnable() {
					@Override
					public void run() {
		    			errorAfterException.setText(e.getMessage());
			    		((StackLayout) shell.getLayout()).topControl = errorAfterComposite;
			    		shell.layout();
					}
	        	});

				return;
			}
 
        	display.asyncExec(new Runnable() {
				@Override
				public void run() {
		    		((StackLayout) shell.getLayout()).topControl = finishedComposite;
		    		shell.layout();
				}
        	});
        }

		private void markAll() throws SQLException {
			Dao<Project,Integer> projectDao = DaoManager.createDao(connectionSource, Project.class);
			Dao<Sample,Integer> sampleDao = DaoManager.createDao(connectionSource, Sample.class);
			Dao<ReplicateV1,Integer> replicateDao = DaoManager.createDao(connectionSource, ReplicateV1.class);
			Dao<RepAnalysisChoice,Integer> repAnalysisChoiceDao = DaoManager.createDao(connectionSource, RepAnalysisChoice.class);
			Dao<SamStepParams,Integer> samStepParamsDao = DaoManager.createDao(connectionSource, SamStepParams.class);

			for (Project project : projectDao.queryForEq(Project.USER_ID_FIELD_NAME, DatabaseConstants.ADMIN_USER_ID)) {
				projectIds.add(project.getId());

				for (Sample sample : sampleDao.queryForEq(Sample.PROJECT_ID_FIELD_NAME, project.getId())) {
					sampleIds.add(sample.getId());
					sampleTypeIds.add(sample.getSampleTypeId());

					for (ReplicateV1 replicate : replicateDao.queryForEq(ReplicateV1.SAMPLEID_FIELD_NAME, sample.getId())) {
						replicateIds.add(replicate.getId());
						markAllForSampleReplicate(replicate);
					}

					for (RepAnalysisChoice repAnalysisChoice : repAnalysisChoiceDao.queryForEq(RepAnalysisChoice.SAMPLE_ID_FIELD_NAME, sample.getId())) {
						repAnalysisChoiceIds.add(repAnalysisChoice.getId());
					}

					for (SamStepParams samStepParams : samStepParamsDao.queryForEq(SamStepParams.SAMPLE_ID_FIELD_NAME, sample.getId())) {
						samStepParamsIds.add(samStepParams.getId());
					}
				}
			}

			for (int corrIntervalId : corrIntervalIds) {
				markAllForCorrIntervalId(corrIntervalId);
			}
		}

		private void markAllForSampleReplicate(ReplicateV1 replicate) throws SQLException {
			Dao<CorrIntervalV1,Integer> corrIntervalDao = DaoManager.createDao(connectionSource, CorrIntervalV1.class);

			massSpecIds.add(replicate.getMassSpecId());

			if (replicate.getAcidTempId() != DatabaseConstants.EMPTY_DB_ID) {
				acidTempIds.add(replicate.getAcidTempId());
			}

			QueryBuilder<CorrIntervalV1, Integer> queryBuilderCorrInterval = corrIntervalDao.queryBuilder();
			queryBuilderCorrInterval.where()
				.eq(CorrIntervalV1.MASSSPECID_FIELD_NAME, replicate.getMassSpecId())
				.and()
				.le(CorrIntervalV1.VALIDFROM_FIELD_NAME, replicate.getDate())
				.and()
				.gt(CorrIntervalV1.VALIDUNTIL_FIELD_NAME, replicate.getDate());
			PreparedQuery<CorrIntervalV1> preparedQueryCorrInterval = queryBuilderCorrInterval.prepare();

			List<CorrIntervalV1> corrIntervalList = corrIntervalDao.query(preparedQueryCorrInterval);

			for (CorrIntervalV1 corrInterval : corrIntervalList) {
				corrIntervalIds.add(corrInterval.getId());
			}
		}

		private void markAllForCorrIntervalId(int corrIntervalId) throws SQLException {
			Dao<CorrIntervalV1,Integer> corrIntervalDao = DaoManager.createDao(connectionSource, CorrIntervalV1.class);
			Dao<ReplicateV1,Integer> replicateDao = DaoManager.createDao(connectionSource, ReplicateV1.class);
			Dao<Standard,Integer> standardDao = DaoManager.createDao(connectionSource, Standard.class);
			Dao<ScanV3,Integer> scanDao = DaoManager.createDao(connectionSource, ScanV3.class);
			Dao<RepStepParams,Integer> repStepParamsDao = DaoManager.createDao(connectionSource, RepStepParams.class);
			Dao<RefGas,Integer> refGasDao = DaoManager.createDao(connectionSource, RefGas.class);

			CorrIntervalV1 corrInterval = corrIntervalDao.queryForId(corrIntervalId);

			QueryBuilder<ReplicateV1,Integer> queryBuilderReplicate = replicateDao.queryBuilder();
			Where<ReplicateV1,Integer> whereReplicate = queryBuilderReplicate.where();
			whereReplicate
				.eq(ReplicateV1.MASSSPECID_FIELD_NAME, corrInterval.getMassSpecId())
				.and()
				.ne(ReplicateV1.STANDARDID_FIELD_NAME, DatabaseConstants.EMPTY_DB_ID)
				.and()
				.ge(ReplicateV1.DATE_FIELD_NAME, corrInterval.getValidFrom());

			if (corrInterval.getValidUntil() != DatabaseConstants.MAX_DATE) {
				whereReplicate
					.and()
					.lt(ReplicateV1.DATE_FIELD_NAME, corrInterval.getValidUntil());
			}

			PreparedQuery<ReplicateV1> preparedQueryReplicate = queryBuilderReplicate.prepare();

			for (ReplicateV1 replicate : replicateDao.query(preparedQueryReplicate)) {
				replicateIds.add(replicate.getId());
				massSpecIds.add(replicate.getMassSpecId());

				if (!standardIds.contains(replicate.getStandardId())) {
					standardIds.add(replicate.getStandardId());
					Standard standard = standardDao.queryForId(replicate.getStandardId());
					sampleTypeIds.add(standard.getSampleTypeId());
				}

				if (replicate.getAcidTempId() != DatabaseConstants.EMPTY_DB_ID) {
					acidTempIds.add(replicate.getAcidTempId());
				}
			}

			QueryBuilder<ScanV3,Integer> queryBuilderScan = scanDao.queryBuilder();
			Where<ScanV3,Integer> whereScan = queryBuilderScan.where();
			whereScan
				.eq(ScanV3.MASSSPECID_FIELD_NAME, corrInterval.getMassSpecId())
				.and()
				.ge(ScanV3.DATE_FIELD_NAME, corrInterval.getValidFrom());

			if (corrInterval.getValidUntil() != DatabaseConstants.MAX_DATE) {
				whereScan
					.and()
					.lt(ReplicateV1.DATE_FIELD_NAME, corrInterval.getValidUntil());
			}

			PreparedQuery<ScanV3> preparedQueryScan = queryBuilderScan.prepare();

			for (ScanV3 scan : scanDao.query(preparedQueryScan)) {
				scanIds.add(scan.getId());
			}

			for (RepStepParams repStepParams : repStepParamsDao.queryForEq(RepStepParams.CORR_INTERVAL_ID_FIELD_NAME, corrInterval.getId())) {
				repStepParamsIds.add(repStepParams.getId());
			}

			for (RefGas refGas : refGasDao.queryForEq(RefGas.MASSSPECID_FIELD_NAME, corrInterval.getMassSpecId())) {
				if (refGas.getValidUntil() < corrInterval.getValidFrom()) {
					continue;
				}

				if (refGas.getValidFrom() >= corrInterval.getValidUntil()) {
					continue;
				}

				refGasIds.add(refGas.getId());
			}
		}

		private void removeFromTables() throws SQLException {
			// ACIDTEMP_V0					remove unneeded

			Dao<AcidTemp,Integer> acidTempDao = DaoManager.createDao(connectionSource, AcidTemp.class);

			for (AcidTemp acidTemp : acidTempDao.queryForAll()) {
				if (!acidTempIds.contains(acidTemp.getId())) {
					acidTempDao.delete(acidTemp);
				}
			}

			// ACQUISITIONINPUT_V0			deleted with replicate
			// ACQUISITIONPARSED_V2			deleted with replicate
			// CALCREPLICATECACHE_V0		remove all

			Dao<CalcReplicateCache,Integer> calcReplicateCacheDao = DaoManager.createDao(connectionSource, CalcReplicateCache.class);

			for (CalcReplicateCache calcReplicateCache : calcReplicateCacheDao.queryForAll()) {
				calcReplicateCacheDao.delete(calcReplicateCache);
			}
			
			// CALCREPTOCALCSAMP_V0			remove all

			Dao<CalcRepToCalcSamp,Integer> calcRepToCalcSampDao = DaoManager.createDao(connectionSource, CalcRepToCalcSamp.class);

			for (CalcRepToCalcSamp calcRepToCalcSamp : calcRepToCalcSampDao.queryForAll()) {
				calcRepToCalcSampDao.delete(calcRepToCalcSamp);
			}
			
			// CALCSAMPLECACHE_V0			remove all
			
			Dao<CalcSampleCache,Integer> calcSampleCacheDao = DaoManager.createDao(connectionSource, CalcSampleCache.class);

			for (CalcSampleCache calcSampleCache : calcSampleCacheDao.queryForAll()) {
				calcSampleCacheDao.delete(calcSampleCache);
			}

			// CORRINTERVALERROR_V0			remove all
			
			Dao<CorrIntervalError,Integer> corrIntervalErrorDao = DaoManager.createDao(connectionSource, CorrIntervalError.class);

			for (CorrIntervalError corrIntervalError : corrIntervalErrorDao.queryForAll()) {
				corrIntervalErrorDao.delete(corrIntervalError);
			}

			// CORRINTERVALSCRATCHPAD_V0	remove all

			Dao<CorrIntervalScratchPad,Integer> corrIntervalScratchPadDao = DaoManager.createDao(connectionSource, CorrIntervalScratchPad.class);

			for (CorrIntervalScratchPad corrIntervalScratchPad : corrIntervalScratchPadDao.queryForAll()) {
				corrIntervalScratchPadDao.delete(corrIntervalScratchPad);
			}

			// CORRINTERVAL_V1				remove unneeded

			Dao<CorrIntervalV1,Integer> corrIntervalV1Dao = DaoManager.createDao(connectionSource, CorrIntervalV1.class);

			for (CorrIntervalV1 corrInterval : corrIntervalV1Dao.queryForAll()) {
				if (!corrIntervalIds.contains(corrInterval.getId())) {
					corrIntervalV1Dao.delete(corrInterval);
				}
			}

			// MASSSPEC_V0					remove unneeded

			Dao<MassSpec,Integer> massSpecDao = DaoManager.createDao(connectionSource, MassSpec.class);

			for (MassSpec massSpec : massSpecDao.queryForAll()) {
				if (!massSpecIds.contains(massSpec.getId())) {
					massSpecDao.delete(massSpec);
				}
			}

			// PERMISSIONS_V0				remove all but admin

			Dao<Permissions,Integer> permissionsDao = DaoManager.createDao(connectionSource, Permissions.class);

			for (Permissions permissions : permissionsDao.queryForAll()) {
				if (permissions.getUserId() != DatabaseConstants.ADMIN_USER_ID) {
					permissionsDao.delete(permissions);
				}
			}
			
			// PREFERENCES_V0				remove all but admin

			Dao<Preferences,Integer> preferencesDao = DaoManager.createDao(connectionSource, Preferences.class);

			for (Preferences preferences : preferencesDao.queryForAll()) {
				if (preferences.getUserId() != DatabaseConstants.ADMIN_USER_ID) {
					preferencesDao.delete(preferences);
				}
			}

			// PROJECT_V0					remove unneeded

			Dao<Project,Integer> projectDao = DaoManager.createDao(connectionSource, Project.class);

			for (Project project : projectDao.queryForAll()) {
				if (!projectIds.contains(project.getId())) {
					projectDao.delete(project);
				} else {
					project.setUserId(DatabaseConstants.ADMIN_USER_ID);
					projectDao.update(project);
				}
			}

			// REFERENCEGAS_V0				remove unneeded

			Dao<RefGas,Integer> refGasDao = DaoManager.createDao(connectionSource, RefGas.class);

			for (RefGas refGas : refGasDao.queryForAll()) {
				if (!refGasIds.contains(refGas.getId())) {
					refGasDao.delete(refGas);
				}
			}

			// REPANALYSISCHOICE_V0			remove unneeded

			Dao<RepAnalysisChoice,Integer> repAnalysisChoiceDao = DaoManager.createDao(connectionSource, RepAnalysisChoice.class);

			for (RepAnalysisChoice repAnalysisChoice : repAnalysisChoiceDao.queryForAll()) {
				if (!repAnalysisChoiceIds.contains(repAnalysisChoice.getId())) {
					repAnalysisChoiceDao.delete(repAnalysisChoice);
				}
			}

			// REPANALYSIS_V0				no change
			// REPLICATE_V1					remove unneeded

			Dao<ReplicateV1,Integer> replicateDao = DaoManager.createDao(connectionSource, ReplicateV1.class);

			for (ReplicateV1 replicate : replicateDao.queryForAll()) {
				if (!replicateIds.contains(replicate.getId())) {
					DeleteReplicate.deleteReplicate(connectionSource, rawFileManager, replicate);
				} else {
					replicate.setUserId(DatabaseConstants.ADMIN_USER_ID);
					replicateDao.update(replicate);
				}
			}

			// REPSTEPPARAMS_V0				remove unneeded

			Dao<RepStepParams,Integer> repStepParamsDao = DaoManager.createDao(connectionSource, RepStepParams.class);
			Dao<RepStep,Integer> repStepDao = DaoManager.createDao(connectionSource, RepStep.class);

			for (RepStepParams repStepParams : repStepParamsDao.queryForAll()) {
				if (!repStepParamsIds.contains(repStepParams.getId())) {
					repStepParamsDao.delete(repStepParams);

				} else {
					int analysisId = repStepParams.getAnalysisId();
					int position = repStepParams.getPosition();

					HashMap<String,Object> fieldValues = new HashMap<String,Object>();
					fieldValues.put(RepStep.ANALYSIS_ID_FIELD_NAME, analysisId);
					fieldValues.put(RepStep.POSITION_FIELD_NAME, position);

					List<RepStep> repSteps = repStepDao.queryForFieldValues(fieldValues);

					if (repSteps.size() == 1) {
						String clazz = repSteps.get(0).getClazz();
						Object object = null;

						try {
							object = Reflection.createObject(clazz);
						} catch (Exception e) {
							// ignore
						}

						if (object != null) {
							Reflection.callMethod(object, "removeStandardIds", repStepParams, standardIds);
						}

						repStepParamsDao.update(repStepParams);
					}
				}
			}

			// REPSTEP_V0					no change
			// SAMANALYSIS_V0				no change
			// SAMPLETYPE_V0				remove unneeded
			
			Dao<SampleType,Integer> sampleTypeDao = DaoManager.createDao(connectionSource, SampleType.class);

			for (SampleType sampleType : sampleTypeDao.queryForAll()) {
				if (!sampleTypeIds.contains(sampleType.getId())) {
					sampleTypeDao.delete(sampleType);
				}
			}

			// SAMPLE_V0					remove unneeded
			
			Dao<Sample,Integer> sampleDao = DaoManager.createDao(connectionSource, Sample.class);

			for (Sample sample : sampleDao.queryForAll()) {
				if (!sampleIds.contains(sample.getId())) {
					sampleDao.delete(sample);
				} else {
					sample.setUserId(DatabaseConstants.ADMIN_USER_ID);
					sampleDao.update(sample);
				}
			}

			// SAMSTEPPARAMS_V0				remove unneeded

			Dao<SamStepParams,Integer> samStepParamsDao = DaoManager.createDao(connectionSource, SamStepParams.class);

			for (SamStepParams samStepParams : samStepParamsDao.queryForAll()) {
				if (!samStepParamsIds.contains(samStepParams.getId())) {
					samStepParamsDao.delete(samStepParams);
				} else {
					samStepParams.setUserId(DatabaseConstants.ADMIN_USER_ID);
					samStepParamsDao.update(samStepParams);
				}
			}

			// SAMSTEP_V0					no change
			// SCANFILEINPUT_V0				deleted with scan
			// SCANFILEPARSED_V2			deleted with scan
			// SCAN_V2						remove unneeded

			Dao<ScanV3,Integer> scanDao = DaoManager.createDao(connectionSource, ScanV3.class);

			for (ScanV3 scan : scanDao.queryForAll()) {
				if (!scanIds.contains(scan.getId())) {
					DeleteScan.deleteScan(connectionSource, rawFileManager, scan.getId());
				} else {
					scan.setUserId(DatabaseConstants.ADMIN_USER_ID);
					scanDao.update(scan);
				}
			}

			// SCICONSTANTS_V0				no change
			// STANDARD_V0					remove unneeded
			
			Dao<Standard,Integer> standardDao = DaoManager.createDao(connectionSource, Standard.class);

			for (Standard standard : standardDao.queryForAll()) {
				if (!standardIds.contains(standard.getId())) {
					standardDao.delete(standard);
				}
			}

			// TABLELAYOUT_V0				remove all but admin

			Dao<TableLayout,Integer> tableLayoutDao = DaoManager.createDao(connectionSource, TableLayout.class);

			for (TableLayout tableLayout : tableLayoutDao.queryForAll()) {
				if (tableLayout.getUserId() != DatabaseConstants.ADMIN_USER_ID) {
					tableLayoutDao.delete(tableLayout);
				}
			}

			// USER_V0						remove all but admin - reset password to "admin"

			Dao<User,Integer> userDao = DaoManager.createDao(connectionSource, User.class);

			for (User user : userDao.queryForAll()) {
				if (user.getId() == DatabaseConstants.ADMIN_USER_ID) {
					user.setPassword("admin");
					userDao.update(user);

				} else {
					userDao.delete(user);
				}
			}

			// VERSION						no change
			// RAWFILE_V0					deleted with replicate/scan

			Dao<RawFile,Integer> rawFileDao = DaoManager.createDao(connectionSource, RawFile.class);

			for (RawFile rawFile : rawFileDao.queryForAll()) {
				rawFile.setUserId(DatabaseConstants.ADMIN_USER_ID);
				rawFileDao.update(rawFile);
			}
		}

		private void copyDatabase(String source, String jdbcUrl) throws ClassNotFoundException, SQLException {
			Class.forName("org.h2.Driver");

			Connection conn = DriverManager.getConnection(jdbcUrl, "admin", "admin");
			conn.createStatement().execute("SCRIPT TO '" + source + "/database.txt'");
			conn.close();

			for (File file : new File(source).listFiles()) {
				if (file.getName().startsWith("easotope")) {
					file.delete();
				}
			}

			if (jdbcUrl.endsWith(";IFEXISTS=TRUE")) {
				jdbcUrl = jdbcUrl.substring(0, jdbcUrl.length() - ";IFEXISTS=TRUE".length());
			}

			conn = DriverManager.getConnection(jdbcUrl, "admin", "admin");
			conn.createStatement().execute("RUNSCRIPT FROM '" + source + "/database.txt'");
			conn.close();

			new File(source + "/database.txt").delete();
		}
	}
}
