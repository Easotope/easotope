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

package org.easotope.shared.rawdata.parser.thermo;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;

import org.easotope.shared.rawdata.InputParameter;
import org.easotope.shared.rawdata.Vendor;
import org.easotope.shared.rawdata.parser.Parser;

public class ThermoParser extends Parser {
	private int maxFreeSpace = 100;
	private RollingBuffer buffer = null;
	private StateMachine stateMachine = null;

	private boolean fileInputStreamEmpty = false;
	private PrintStream printStream = null;
	private ArrayList<Integer> junk = null;
	
	private Integer normalString[] = {
			0xff, 0xff, null, 0x00, null, 0x00
	};
	
	private Integer spreadString[] = {
			0xff, 0xfe, 0xff, null
	};

	public ThermoParser(int maxBufferSize, int maxFreeSpace, StateMachine stateMachine, boolean historicMode, String assumedTimeZone) {
		super(historicMode, assumedTimeZone);
		buffer = new RollingBuffer(maxBufferSize);
		this.maxFreeSpace = maxFreeSpace;
		this.stateMachine = stateMachine;
	}

	public ThermoParser(int maxBufferSize, int maxFreeSpace, StateMachine stateMachine, boolean historicMode, String assumedTimeZone, PrintStream printStream) {
		super(historicMode, assumedTimeZone);
		buffer = new RollingBuffer(maxBufferSize);
		this.maxFreeSpace = maxFreeSpace;
		this.stateMachine = stateMachine;
		this.printStream = printStream;
		junk = new ArrayList<Integer>();
	}

	protected boolean bufferContainsNormalString(RollingBuffer rollingBuffer) {
		return rollingBuffer.startsWith(normalString);
	}

	protected boolean bufferContainsSpreadString(RollingBuffer rollingBuffer) {
		return rollingBuffer.startsWith(spreadString);
	}

	public void parseFile(ByteArrayInputStream inputStream) {
		try {
			fillBuffer(inputStream);

			while (buffer.getContains() != 0) {
				if (buffer.startsWith(normalString)) {
					String string = buildNormalString();
					
					if (printStream != null) {
						printJunk();
						printStream.println(string);
					}

					buffer.deleteFromStart(spreadString.length + string.length());

					if (stateMachine != null) {
						stateMachine.foundString(string, buffer);
					}
	
				} else if (buffer.startsWith(spreadString)) {
					String string = buildSpreadString();
	
					if (printStream != null) {
						printJunk();
						printStream.println(string);
					}
					
					buffer.deleteFromStart(spreadString.length + string.length() * 2);
					
					if (stateMachine != null) {
						stateMachine.foundString(string, buffer);
					}
	
				} else {
					if (printStream != null) {
						junk.add(buffer.get(0));
					}
	
					buffer.deleteFromStart(1);
				}
				
				fillBuffer(inputStream);
			}

			if (printStream != null) {
				printJunk();
			}

		} catch (Exception e) {
			e.printStackTrace(System.err);
			stateMachine = null;
			return;
		}
	}

	private void printJunk() {
		if (junk.size() == 0) {
			return;
		}

		printStream.print("[");

		for (int i : junk) {
			String hex = Integer.toHexString(i);
			
			if (hex.length() == 1) {
				hex = "0" + hex;
			}
			
			printStream.print(hex);
		}
		
		printStream.println("]");

		junk.clear();
	}

	private String buildNormalString() {
		StringBuffer stringBuffer = new StringBuffer();

		for (int i=0; i<buffer.get(4); i++) {
			stringBuffer.append((char) buffer.get(i + normalString.length));
		}

		return stringBuffer.toString();
	}

	private String buildSpreadString() {
		StringBuffer stringBuffer = new StringBuffer();

		for (int i=0; i<buffer.get(3); i++) {
			stringBuffer.append((char) buffer.get(i*2 + spreadString.length));
		}

		return stringBuffer.toString();
	}

	public void fillBuffer(InputStream inputStream) {
		int freeSpace = buffer.getSize() - buffer.getContains();

		if (freeSpace > maxFreeSpace && !fileInputStreamEmpty) {
			byte[] bytes = new byte[freeSpace];

			try {
				int numRead = inputStream.read(bytes, 0, freeSpace);
				
				if (numRead == -1) {
					fileInputStreamEmpty = true;
					return;
				}
				
				for (int i=0; i<numRead; i++) {
					buffer.addToEnd(Util.toInt(bytes[i]));
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public ArrayList<HashMap<InputParameter,Object>> getResultsList() {
		if (stateMachine == null) {
			return null;
		}

		ArrayList<HashMap<InputParameter,Object>> result = new ArrayList<HashMap<InputParameter,Object>>();
		result.add(stateMachine.getMap());

		return result;
	}

	@Override
	public String getAssumedTimeZone() {
		return null;
	}

	public String getDescription() {
		return "Thermo Fisher Generic";
	}

	@Override
	public Vendor getVendor() {
		return Vendor.Thermo;
	}
}
