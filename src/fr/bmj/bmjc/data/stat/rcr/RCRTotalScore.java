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

public class RCRTotalScore {

	public final String playerName;
	public final String displayName;
	public int totalScore;
	public int umaScore;
	public int numberOfGame;
	public final int year;
	public final int month;
	public final int day;

	public RCRTotalScore(final String playerName, final String displayName, final int year, final int month, final int day) {
		super();
		this.playerName = playerName;
		this.displayName = displayName;
		this.year = year;
		this.month = month;
		this.day = day;
		totalScore = 0;
		umaScore = 0;
		numberOfGame = 0;
	}

}
