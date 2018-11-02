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

import java.util.List;

public class RCRDataPackageTrend {

	public final List<Long> dates;
	public final List<String> playerNames;
	public final List<String> displayNames;
	public final List<List<Integer>> data;

	public RCRDataPackageTrend(final List<Long> dates, final List<String> playerNames, final List<String> displayNames, final List<List<Integer>> data) {
		this.dates = dates;
		this.playerNames = playerNames;
		this.displayNames = displayNames;
		this.data = data;
	}

}
