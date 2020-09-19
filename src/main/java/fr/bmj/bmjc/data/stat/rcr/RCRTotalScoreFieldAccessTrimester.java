/*
 * This file is part of Breizh Mahjong Recorder.
 *
 * Breizh Mahjong Recorder is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Breizh Mahjong Recorder is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Breizh Mahjong Recorder. If not, see <http://www.gnu.org/licenses/>.
 */
package fr.bmj.bmjc.data.stat.rcr;

import fr.bmj.bmjc.enums.EnumTrimester;

public class RCRTotalScoreFieldAccessTrimester implements RCRTotalScoreFieldAccess {

	private static final String TRIMESTER_STRINGS[] = {
		EnumTrimester.TRIMESTER_1.toString(),
		EnumTrimester.TRIMESTER_2.toString(),
		EnumTrimester.TRIMESTER_3.toString(),
		EnumTrimester.TRIMESTER_4.toString()
	};

	@Override
	public String getDataString(final RCRTotalScore data) {
		return Integer.toString(
			data.year) + " " + TRIMESTER_STRINGS[data.month];
	}

}
