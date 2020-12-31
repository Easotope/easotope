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

package org.easotope.shared.rawdata;

// !!!!!!
// Any time modifications are made to this list, you need to set the
// variables rebuildScanFileParsed and rebuildAcquisitionsParsed to
// true during in the upgrade process. This removes and rebuilds tables
// in the database that serialize this class.
//!!!!!!

public enum InputParameter {
	Java_Date(Long.class, InputParameterType.Timestamp, null),

	Disabled(Boolean.class, InputParameterType.Flag, null),
	Off_Peak(Boolean.class, InputParameterType.Flag, null),

	// THERE IS ANOTHER META FIELD AT THE BOTTOM OF THE LIST. IT SHOULD
	// BE MOVED UP NEXT TIME A REPARSING OF THE INPUT FILES IS NECESSARY.

	Identifier_1(String.class, InputParameterType.Meta, null),			// Thermo specific
	Identifier_2(String.class, InputParameterType.Meta, null),			// Thermo specific
	Run(String.class, InputParameterType.Meta, null),					// Thermo specific
	Sample_Name(String.class, InputParameterType.Meta, null),			// Nu specific
	Sample_Weight(Double.class, InputParameterType.Meta, null),

	V43_5_Background(Double.class, InputParameterType.Background, 435),
	V44_Background(Double.class, InputParameterType.Background, 440),
	V44_5_Background(Double.class, InputParameterType.Background, 445),
	V45_Background(Double.class, InputParameterType.Background, 450),
	V45_5_Background(Double.class, InputParameterType.Background, 455),
	V46_Background(Double.class, InputParameterType.Background, 460),
	V46_5_Background(Double.class, InputParameterType.Background, 465),
	V47_Background(Double.class, InputParameterType.Background, 470),
	V47_5_Background(Double.class, InputParameterType.Background, 475),
	V48_Background(Double.class, InputParameterType.Background, 480),
	V48_5_Background(Double.class, InputParameterType.Background, 485),
	V49_Background(Double.class, InputParameterType.Background, 490),
	V49_5_Background(Double.class, InputParameterType.Background, 495),

	V43_5_Sample(Double.class, InputParameterType.SampleMeasurement, 435),
	V44_Sample(Double.class, InputParameterType.SampleMeasurement, 440),
	V44_5_Sample(Double.class, InputParameterType.SampleMeasurement, 445),
	V45_Sample(Double.class, InputParameterType.SampleMeasurement, 450),
	V45_5_Sample(Double.class, InputParameterType.SampleMeasurement, 455),
	V46_Sample(Double.class, InputParameterType.SampleMeasurement, 460),
	V46_5_Sample(Double.class, InputParameterType.SampleMeasurement, 465),
	V47_Sample(Double.class, InputParameterType.SampleMeasurement, 470),
	V47_5_Sample(Double.class, InputParameterType.SampleMeasurement, 475),
	V48_Sample(Double.class, InputParameterType.SampleMeasurement, 480),
	V48_5_Sample(Double.class, InputParameterType.SampleMeasurement, 485),
	V49_Sample(Double.class, InputParameterType.SampleMeasurement, 490),
	V49_5_Sample(Double.class, InputParameterType.SampleMeasurement, 495),

	V43_5_Ref(Double.class, InputParameterType.RefMeasurement, 435),
	V44_Ref(Double.class, InputParameterType.RefMeasurement, 440),
	V44_5_Ref(Double.class, InputParameterType.RefMeasurement, 445),
	V45_Ref(Double.class, InputParameterType.RefMeasurement, 450),
	V45_5_Ref(Double.class, InputParameterType.RefMeasurement, 455),
	V46_Ref(Double.class, InputParameterType.RefMeasurement, 460),
	V46_5_Ref(Double.class, InputParameterType.RefMeasurement, 465),
	V47_Ref(Double.class, InputParameterType.RefMeasurement, 470),
	V47_5_Ref(Double.class, InputParameterType.RefMeasurement, 475),
	V48_Ref(Double.class, InputParameterType.RefMeasurement, 480),
	V48_5_Ref(Double.class, InputParameterType.RefMeasurement, 485),
	V49_Ref(Double.class, InputParameterType.RefMeasurement, 490),
	V49_5_Ref(Double.class, InputParameterType.RefMeasurement, 495),

	V43_5_Scan(Double.class, InputParameterType.Scan, 435),
	V44_Scan(Double.class, InputParameterType.Scan, 440),
	V44_5_Scan(Double.class, InputParameterType.Scan, 445),
	V45_Scan(Double.class, InputParameterType.Scan, 450),
	V45_5_Scan(Double.class, InputParameterType.Scan, 455),
	V46_Scan(Double.class, InputParameterType.Scan, 460),
	V46_5_Scan(Double.class, InputParameterType.Scan, 465),
	V47_Scan(Double.class, InputParameterType.Scan, 470),
	V47_5_Scan(Double.class, InputParameterType.Scan, 475),
	V48_Scan(Double.class, InputParameterType.Scan, 480),
	V48_5_Scan(Double.class, InputParameterType.Scan, 485),
	V49_Scan(Double.class, InputParameterType.Scan, 490),
	V49_5_Scan(Double.class, InputParameterType.Scan, 495),

	ScanFromVoltage(Double.class, InputParameterType.Meta, null),
	ScanToVoltage(Double.class, InputParameterType.Meta, null),

	//The order here is important - don't put anything between these
	Channel0_Background(Double.class, InputParameterType.Background, null),
	Channel1_Background(Double.class, InputParameterType.Background, null),
	Channel2_Background(Double.class, InputParameterType.Background, null),
	Channel3_Background(Double.class, InputParameterType.Background, null),
	Channel4_Background(Double.class, InputParameterType.Background, null),
	Channel5_Background(Double.class, InputParameterType.Background, null),
	Channel6_Background(Double.class, InputParameterType.Background, null),
	Channel7_Background(Double.class, InputParameterType.Background, null),
	Channel8_Background(Double.class, InputParameterType.Background, null),
	Channel9_Background(Double.class, InputParameterType.Background, null),
	Channel10_Background(Double.class, InputParameterType.Background, null),

	//The order here is important - don't put anything between these
	Channel0_Sample(Double.class, InputParameterType.SampleMeasurement, null),
	Channel1_Sample(Double.class, InputParameterType.SampleMeasurement, null),
	Channel2_Sample(Double.class, InputParameterType.SampleMeasurement, null),
	Channel3_Sample(Double.class, InputParameterType.SampleMeasurement, null),
	Channel4_Sample(Double.class, InputParameterType.SampleMeasurement, null),
	Channel5_Sample(Double.class, InputParameterType.SampleMeasurement, null),
	Channel6_Sample(Double.class, InputParameterType.SampleMeasurement, null),
	Channel7_Sample(Double.class, InputParameterType.SampleMeasurement, null),
	Channel8_Sample(Double.class, InputParameterType.SampleMeasurement, null),
	Channel9_Sample(Double.class, InputParameterType.SampleMeasurement, null),
	Channel10_Sample(Double.class, InputParameterType.SampleMeasurement, null),

	//The order here is important - don't put anything between these
	Channel0_Ref(Double.class, InputParameterType.RefMeasurement, null),
	Channel1_Ref(Double.class, InputParameterType.RefMeasurement, null),
	Channel2_Ref(Double.class, InputParameterType.RefMeasurement, null),
	Channel3_Ref(Double.class, InputParameterType.RefMeasurement, null),
	Channel4_Ref(Double.class, InputParameterType.RefMeasurement, null),
	Channel5_Ref(Double.class, InputParameterType.RefMeasurement, null),
	Channel6_Ref(Double.class, InputParameterType.RefMeasurement, null),
	Channel7_Ref(Double.class, InputParameterType.RefMeasurement, null),
	Channel8_Ref(Double.class, InputParameterType.RefMeasurement, null),
	Channel9_Ref(Double.class, InputParameterType.RefMeasurement, null),
	Channel10_Ref(Double.class, InputParameterType.RefMeasurement, null),

	//The order here is important - don't put anything between these
	Channel0_Scan(Double.class, InputParameterType.Scan, null),
	Channel1_Scan(Double.class, InputParameterType.Scan, null),
	Channel2_Scan(Double.class, InputParameterType.Scan, null),
	Channel3_Scan(Double.class, InputParameterType.Scan, null),
	Channel4_Scan(Double.class, InputParameterType.Scan, null),
	Channel5_Scan(Double.class, InputParameterType.Scan, null),
	Channel6_Scan(Double.class, InputParameterType.Scan, null),
	Channel7_Scan(Double.class, InputParameterType.Scan, null),
	Channel8_Scan(Double.class, InputParameterType.Scan, null),
	Channel9_Scan(Double.class, InputParameterType.Scan, null),
	Channel10_Scan(Double.class, InputParameterType.Scan, null),

	ChannelToMZX10(Integer.class, InputParameterType.ChannelData, null),

	V43_5_Scan_Algorithm(Double.class, InputParameterType.Algorithm, 435),
	V44_Scan_Algorithm(Double.class, InputParameterType.Algorithm, 440),
	V44_5_Scan_Algorithm(Double.class, InputParameterType.Algorithm, 445),
	V45_Scan_Algorithm(Double.class, InputParameterType.Algorithm, 450),
	V45_5_Scan_Algorithm(Double.class, InputParameterType.Algorithm, 455),
	V46_Scan_Algorithm(Double.class, InputParameterType.Algorithm, 460),
	V46_5_Scan_Algorithm(Double.class, InputParameterType.Algorithm, 465),
	V47_Scan_Algorithm(Double.class, InputParameterType.Algorithm, 470),
	V47_5_Scan_Algorithm(Double.class, InputParameterType.Algorithm, 475),
	V48_Scan_Algorithm(Double.class, InputParameterType.Algorithm, 480),
	V48_5_Scan_Algorithm(Double.class, InputParameterType.Algorithm, 485),
	V49_Scan_Algorithm(Double.class, InputParameterType.Algorithm, 490),
	V49_5_Scan_Algorithm(Double.class, InputParameterType.Algorithm, 495),

	V43_5_Scan_Slope(Double.class, InputParameterType.X1Coeff, 435),
	V44_Scan_Slope(Double.class, InputParameterType.X1Coeff, 440),
	V44_5_Scan_Slope(Double.class, InputParameterType.X1Coeff, 445),
	V45_Scan_Slope(Double.class, InputParameterType.X1Coeff, 450),
	V45_5_Scan_Slope(Double.class, InputParameterType.X1Coeff, 455),
	V46_Scan_Slope(Double.class, InputParameterType.X1Coeff, 460),
	V46_5_Scan_Slope(Double.class, InputParameterType.X1Coeff, 465),
	V47_Scan_Slope(Double.class, InputParameterType.X1Coeff, 470),
	V47_5_Scan_Slope(Double.class, InputParameterType.X1Coeff, 475),
	V48_Scan_Slope(Double.class, InputParameterType.X1Coeff, 480),
	V48_5_Scan_Slope(Double.class, InputParameterType.X1Coeff, 485),
	V49_Scan_Slope(Double.class, InputParameterType.X1Coeff, 490),
	V49_5_Scan_Slope(Double.class, InputParameterType.X1Coeff, 495),

	V43_5_Scan_Intercept(Double.class, InputParameterType.X0Coeff, 435),
	V44_Scan_Intercept(Double.class, InputParameterType.X0Coeff, 440),
	V44_5_Scan_Intercept(Double.class, InputParameterType.X0Coeff, 445),
	V45_Scan_Intercept(Double.class, InputParameterType.X0Coeff, 450),
	V45_5_Scan_Intercept(Double.class, InputParameterType.X0Coeff, 455),
	V46_Scan_Intercept(Double.class, InputParameterType.X0Coeff, 460),
	V46_5_Scan_Intercept(Double.class, InputParameterType.X0Coeff, 465),
	V47_Scan_Intercept(Double.class, InputParameterType.X0Coeff, 470),
	V47_5_Scan_Intercept(Double.class, InputParameterType.X0Coeff, 475),
	V48_Scan_Intercept(Double.class, InputParameterType.X0Coeff, 480),
	V48_5_Scan_Intercept(Double.class, InputParameterType.X0Coeff, 485),
	V49_Scan_Intercept(Double.class, InputParameterType.X0Coeff, 490),
	V49_5_Scan_Intercept(Double.class, InputParameterType.X0Coeff, 495),

	V43_5_Scan_X2Coeff(Double.class, InputParameterType.X2Coeff, 435),
	V44_Scan_X2Coeff(Double.class, InputParameterType.X2Coeff, 440),
	V44_5_Scan_X2Coeff(Double.class, InputParameterType.X2Coeff, 445),
	V45_Scan_X2Coeff(Double.class, InputParameterType.X2Coeff, 450),
	V45_5_Scan_X2Coeff(Double.class, InputParameterType.X2Coeff, 455),
	V46_Scan_X2Coeff(Double.class, InputParameterType.X2Coeff, 460),
	V46_5_Scan_X2Coeff(Double.class, InputParameterType.X2Coeff, 465),
	V47_Scan_X2Coeff(Double.class, InputParameterType.X2Coeff, 470),
	V47_5_Scan_X2Coeff(Double.class, InputParameterType.X2Coeff, 475),
	V48_Scan_X2Coeff(Double.class, InputParameterType.X2Coeff, 480),
	V48_5_Scan_X2Coeff(Double.class, InputParameterType.X2Coeff, 485),
	V49_Scan_X2Coeff(Double.class, InputParameterType.X2Coeff, 490),
	V49_5_Scan_X2Coeff(Double.class, InputParameterType.X2Coeff, 495),

	V43_5_Scan_RefMzX10(Double.class, InputParameterType.ReferenceChannel2, 435),
	V44_Scan_RefMzX10(Double.class, InputParameterType.ReferenceChannel2, 440),
	V44_5_Scan_RefMzX10(Double.class, InputParameterType.ReferenceChannel2, 445),
	V45_Scan_RefMzX10(Double.class, InputParameterType.ReferenceChannel2, 450),
	V45_5_Scan_RefMzX10(Double.class, InputParameterType.ReferenceChannel2, 455),
	V46_Scan_RefMzX10(Double.class, InputParameterType.ReferenceChannel2, 460),
	V46_5_Scan_RefMzX10(Double.class, InputParameterType.ReferenceChannel2, 465),
	V47_Scan_RefMzX10(Double.class, InputParameterType.ReferenceChannel2, 470),
	V47_5_Scan_RefMzX10(Double.class, InputParameterType.ReferenceChannel2, 475),
	V48_Scan_RefMzX10(Double.class, InputParameterType.ReferenceChannel2, 480),
	V48_5_Scan_RefMzX10(Double.class, InputParameterType.ReferenceChannel2, 485),
	V49_Scan_RefMzX10(Double.class, InputParameterType.ReferenceChannel2, 490),
	V49_5_Scan_RefMzX10(Double.class, InputParameterType.ReferenceChannel2, 495),

	V43_5_Scan_Factor(Double.class, InputParameterType.Factor2, 435),
	V44_Scan_Factor(Double.class, InputParameterType.Factor2, 440),
	V44_5_Scan_Factor(Double.class, InputParameterType.Factor2, 445),
	V45_Scan_Factor(Double.class, InputParameterType.Factor2, 450),
	V45_5_Scan_Factor(Double.class, InputParameterType.Factor2, 455),
	V46_Scan_Factor(Double.class, InputParameterType.Factor2, 460),
	V46_5_Scan_Factor(Double.class, InputParameterType.Factor2, 465),
	V47_Scan_Factor(Double.class, InputParameterType.Factor2, 470),
	V47_5_Scan_Factor(Double.class, InputParameterType.Factor2, 475),
	V48_Scan_Factor(Double.class, InputParameterType.Factor2, 480),
	V48_5_Scan_Factor(Double.class, InputParameterType.Factor2, 485),
	V49_Scan_Factor(Double.class, InputParameterType.Factor2, 490),
	V49_5_Scan_Factor(Double.class, InputParameterType.Factor2, 495),

	// MOVE THIS UP TO OTHER META DATA NEXT TIME A REPARSING IS NECESSARY
	Init_Intensity(String.class, InputParameterType.Meta, null);	// Thermo specific

	private final Class<?> clazz;
	private final InputParameterType inputParameterType;
	private final Integer mzX10;

	InputParameter(Class<?> clazz, InputParameterType inputParameterType, Integer mzX10) {
        this.clazz = clazz;
        this.inputParameterType = inputParameterType;
        this.mzX10 = mzX10;
    }

	public InputParameterType getInputParameterType() {
		return inputParameterType;
	}

	public Class<?> getType() {
		return clazz;
	}

	public Integer getMzX10() {
		return mzX10;
	}

	public boolean getIsMeasurements() {
		return inputParameterType == InputParameterType.SampleMeasurement || inputParameterType == InputParameterType.RefMeasurement;
	}

	public boolean getIsBackground() {
		return inputParameterType == InputParameterType.Background;
	}
}
