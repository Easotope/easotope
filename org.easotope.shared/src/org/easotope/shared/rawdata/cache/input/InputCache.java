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

package org.easotope.shared.rawdata.cache.input;

import java.util.ArrayList;
import java.util.Hashtable;

import org.easotope.framework.dbcore.cmdprocessors.Processor;
import org.easotope.framework.dbcore.cmdprocessors.ProcessorManager;
import org.easotope.shared.commands.DisabledStatusUpdate;
import org.easotope.shared.core.cache.AbstractCache;
import org.easotope.shared.core.cache.CacheListener;
import org.easotope.shared.core.cache.logininfo.LoginInfoCache;
import org.easotope.shared.rawdata.Acquisition;
import org.easotope.shared.rawdata.ScanFile;
import org.easotope.shared.rawdata.cache.input.project.ProjectPlugin;
import org.easotope.shared.rawdata.cache.input.projectlist.ProjectListPlugin;
import org.easotope.shared.rawdata.cache.input.rawfile.RawFilePlugin;
import org.easotope.shared.rawdata.cache.input.replicate.ReplicatePlugin;
import org.easotope.shared.rawdata.cache.input.replicatelist.ReplicateListPlugin;
import org.easotope.shared.rawdata.cache.input.sample.SamplePlugin;
import org.easotope.shared.rawdata.cache.input.samplelist.SampleListPlugin;
import org.easotope.shared.rawdata.cache.input.scan.ScanPlugin;
import org.easotope.shared.rawdata.cache.input.scanlist.ScanListPlugin;
import org.easotope.shared.rawdata.tables.Project;
import org.easotope.shared.rawdata.tables.ReplicateV1;
import org.easotope.shared.rawdata.tables.Sample;
import org.easotope.shared.rawdata.tables.ScanV3;

public class InputCache extends AbstractCache {
	private ProjectListPlugin projectListPlugin = new ProjectListPlugin();
	private ProjectPlugin projectPlugin = new ProjectPlugin();
	private SampleListPlugin sampleListPlugin = new SampleListPlugin();
	private SamplePlugin samplePlugin = new SamplePlugin();
	private ReplicateListPlugin replicateListPlugin = new ReplicateListPlugin();
	private ReplicatePlugin replicatePlugin = new ReplicatePlugin();
	private RawFilePlugin rawFilePlugin = new RawFilePlugin();
	private ScanPlugin scanPlugin = new ScanPlugin();
	private ScanListPlugin scanListPlugin = new ScanListPlugin();

	public static InputCache getInstance() {
		return (InputCache) AbstractCache.getCacheInstanceForThisThread(InputCache.class);
	}

	public InputCache() {
		addPlugin(projectPlugin);
		addPlugin(projectListPlugin);
		addPlugin(samplePlugin);
		addPlugin(sampleListPlugin);
		addPlugin(replicateListPlugin);
		addPlugin(replicatePlugin);
		addPlugin(rawFilePlugin);
		addPlugin(scanPlugin);
		addPlugin(scanListPlugin);
	}

	public synchronized int projectListGet(CacheListener listener, int userId) {
		return getObject(projectListPlugin, listener, userId);
	}

	public synchronized int projectGet(CacheListener listener, int projectId) {
		return getObject(projectPlugin, listener, projectId);
	}

	public synchronized int projectSave(CacheListener listener, Project project) {
		return saveObject(projectPlugin, listener, project);
	}

	public synchronized int projectDelete(int projectId, CacheListener listener) {
		return deleteObject(projectPlugin, listener, projectId);
	}

	public synchronized int sampleListGet(CacheListener listener, int sampleId) {
		return getObject(sampleListPlugin, listener, sampleId);
	}

	public synchronized int sampleGet(CacheListener listener, int sampleId) {
		return getObject(samplePlugin, listener, sampleId);
	}

	public synchronized int sampleSave(CacheListener listener, Sample sample) {
		return saveObject(samplePlugin, listener, sample);
	}

	public synchronized int sampleDelete(int sampleId, CacheListener listener) {
		return deleteObject(samplePlugin, listener, sampleId);
	}

	public synchronized int replicateListGet(CacheListener listener, boolean getSamples, int sampleId, int massSpecId, int userId) {
		return getObject(replicateListPlugin, listener, getSamples, sampleId, massSpecId, userId);
	}

	public synchronized int replicateGet(int replicateId, CacheListener listener) {
		return getObject(replicatePlugin, listener, replicateId);
	}

	public synchronized int replicateSave(ReplicateV1 replicate, ArrayList<Acquisition> acquisitions, boolean explode, boolean allowDuplicates, CacheListener listener) {
		return saveObject(replicatePlugin, listener, replicate, acquisitions, explode, allowDuplicates);
	}

	public synchronized void replicateDisabledStatusUpdate(int replicateId, boolean disabled) {
		DisabledStatusUpdate disabledStatusUpdate = new DisabledStatusUpdate(replicateId, disabled);
		Hashtable<String,Object> authenticationObjects = LoginInfoCache.getInstance().getAuthenticationObjects();
		Processor processor = ProcessorManager.getInstance().getProcessor();
		processor.process(disabledStatusUpdate, authenticationObjects, null);
	}

	public synchronized int replicateDelete(int replicateId, CacheListener listener) {
		return deleteObject(replicatePlugin, listener, replicateId);
	}

	public synchronized int rawFileGet(int rawFileId, CacheListener listener) {
		return getObject(rawFilePlugin, listener, rawFileId);
	}

	public synchronized int scanListGet(CacheListener listener, int massSpecId) {
		return getObject(scanListPlugin, listener, massSpecId);
	}
	
	public synchronized int scanGet(int scanId, CacheListener listener) {
		return getObject(scanPlugin, listener, scanId);
	}

	public synchronized int scanSave(ScanV3 scan, ArrayList<ScanFile> scanFiles, CacheListener listener) {
		return saveObject(scanPlugin, listener, scan, scanFiles);
	}

	public synchronized int scanDelete(int scanId, CacheListener listener) {
		return deleteObject(scanPlugin, listener, scanId);
	}
}
