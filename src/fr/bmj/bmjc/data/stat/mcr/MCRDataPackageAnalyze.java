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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MCRDataPackageAnalyze {

	private final List<Integer> listGameID;
	private final List<Integer> listScore;
	private final List<Integer> listSum;
	private int numberOfGames;

	private int scoreMax;
	private int scoreMin;

	private int positiveGames;
	private int positiveGamesPercent;

	private int negativeGames;
	private int negativeGamesPercent;

	private int scoreTotal;
	private int scoreMean;
	private int scoreStandardDeviation;

	private int totalMax;
	private int totalMin;

	private final int gamePlaces[];
	private final int gamePlacePercent[];

	public MCRDataPackageAnalyze() {
		listGameID = new ArrayList<>();
		listScore = new ArrayList<>();
		listSum = new ArrayList<>();
		numberOfGames = 0;
		scoreMax = 0;
		scoreMin = 0;
		positiveGames = 0;
		positiveGamesPercent = 0;
		negativeGames = 0;
		negativeGamesPercent = 0;
		scoreTotal = 0;
		scoreMean = 0;
		scoreStandardDeviation = 0;
		totalMax = 0;
		totalMin = 0;
		gamePlaces = new int[4];
		gamePlacePercent = new int[4];
	}

	public int getNumberOfGames() {
		return numberOfGames;
	}

	public void setNumberOfGames(final int numberOfGames) {
		this.numberOfGames = numberOfGames;
	}

	public int getScoreTotal() {
		return scoreTotal;
	}

	public void setScoreTotal(final int scoreTotal) {
		this.scoreTotal = scoreTotal;
	}

	public int getScoreMean() {
		return scoreMean;
	}

	public void setScoreMean(final int scoreMean) {
		this.scoreMean = scoreMean;
	}

	public int getScoreStandardDeviation() {
		return scoreStandardDeviation;
	}

	public void setScoreStandardDeviation(final int scoreStandardDeviation) {
		this.scoreStandardDeviation = scoreStandardDeviation;
	}

	public int getTotalMax() {
		return totalMax;
	}

	public void setTotalMax(final int totalMax) {
		this.totalMax = totalMax;
	}

	public int getTotalMin() {
		return totalMin;
	}

	public void setTotalMin(final int totalMin) {
		this.totalMin = totalMin;
	}

	public int getScoreMax() {
		return scoreMax;
	}

	public void setScoreMax(final int scoreMax) {
		this.scoreMax = scoreMax;
	}

	public int getScoreMin() {
		return scoreMin;
	}

	public void setScoreMin(final int scoreMin) {
		this.scoreMin = scoreMin;
	}

	public int getPositiveGames() {
		return positiveGames;
	}

	public void setPositiveGames(final int positiveGames) {
		this.positiveGames = positiveGames;
	}

	public int getPositiveGamesPercent() {
		return positiveGamesPercent;
	}

	public void setPositiveGamesPercent(final int positiveGamesPercent) {
		this.positiveGamesPercent = positiveGamesPercent;
	}

	public int getNegativeGames() {
		return negativeGames;
	}

	public void setNegativeGames(final int negativeGames) {
		this.negativeGames = negativeGames;
	}

	public int getNegativeGamesPercent() {
		return negativeGamesPercent;
	}

	public void setNegativeGamesPercent(final int negativeGamesPercent) {
		this.negativeGamesPercent = negativeGamesPercent;
	}

	public void setLists(final List<Integer> listGameID, final List<Integer> listScore, final List<Integer> listSum) {
		if (listGameID != null && listScore != null && listSum != null && listGameID.size() == listScore.size() && listScore.size() == listSum.size()) {
			this.listGameID.clear();
			this.listScore.clear();
			this.listSum.clear();
			for (int index = 0; index < listScore.size(); index++) {
				this.listGameID.add(index, listGameID.get(index));
				this.listScore.add(index, listScore.get(index));
				this.listSum.add(index, listSum.get(index));
			}
		}
	}

	public List<Integer> getListGameID() {
		return Collections.unmodifiableList(listGameID);
	}

	public List<Integer> getListScore() {
		return Collections.unmodifiableList(listScore);
	}

	public List<Integer> getListSum() {
		return Collections.unmodifiableList(listSum);
	}

	public void setGamePlaces(final int gamePlaces[]) {
		if (gamePlaces != null && gamePlaces.length == 4) {
			System.arraycopy(gamePlaces, 0, this.gamePlaces, 0, 4);
		}
	}

	public int[] getGamePlaces() {
		return Arrays.copyOf(gamePlaces, gamePlaces.length);
	}

	public void setGamePlacePercent(final int gamePlacePercent[]) {
		if (gamePlacePercent != null && gamePlacePercent.length == 4) {
			System.arraycopy(gamePlacePercent, 0, this.gamePlacePercent, 0, 4);
		}
	}

	public int[] getGamePlacePercent() {
		return Arrays.copyOf(gamePlacePercent, gamePlacePercent.length);
	}
}
