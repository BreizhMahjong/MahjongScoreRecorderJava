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
package fr.bmj.bmjc.data.stat.mcr;

import java.text.DateFormatSymbols;
import java.util.Locale;

public class MCRTotalScoreFieldAccessMonth implements MCRTotalScoreFieldAccess {

	private final String monthStrings[];

	public MCRTotalScoreFieldAccessMonth() {
		monthStrings = DateFormatSymbols.getInstance(Locale.FRANCE).getMonths();
	}

	@Override
	public String getDataString(final MCRTotalScore data) {
		return monthStrings[data.month] + " " + Integer.toString(data.year);
	}

}
