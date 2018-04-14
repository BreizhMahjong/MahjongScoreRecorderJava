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
package fr.bmj.bmjc.data.game.mcr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MCRGame {
	private final int id;
	private final int tournamentId;
	private final int year;
	private final int month;
	private final int day;
	private final List<MCRScore> scores;

	public MCRGame(final int id, final int tournamentId, final int year, final int month, final int day, final List<MCRScore> scores) {
		super();
		this.id = id;
		this.tournamentId = tournamentId;
		this.year = year;
		this.month = month;
		this.day = day;
		final List<MCRScore> s = new ArrayList<MCRScore>(scores);
		Collections.sort(s, new ComparatorAscendingMCRScoreRanking());
		this.scores = Collections.unmodifiableList(s);
	}

	public int getId() {
		return id;
	}

	public int getTournamentId() {
		return tournamentId;
	}

	public int getYear() {
		return year;
	}

	public int getMonth() {
		return month;
	}

	public int getDay() {
		return day;
	}

	public List<MCRScore> getScores() {
		return scores;
	}

}
