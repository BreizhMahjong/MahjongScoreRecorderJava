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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class RCRDataPackagePersonalAnalyze {

	private final List<Long> listGameID;
	private final List<Integer> listScore;
	private final List<Integer> listSum;
	private int numberOfGames;

	private int maxScore;
	private int minScore;

	private int positiveGames;
	private int positiveGamesPercent;

	private int negativeGames;
	private int negativeGamesPercent;

	private int scoreTotal;
	private int scoreMean;
	private int scoreStandardDeviation;

	private int positiveTotal;
	private int negativeTotal;

	private int numberOfFourPlayerGames;
	private final int fourPlayerGamePlaces[];
	private final int fourPlayerGamePlacePercent[];

	private int numberOfFivePlayerGames;
	private final int fivePlayerGamePlaces[];
	private final int fivePlayerGamePlacePercent[];

	public RCRDataPackagePersonalAnalyze() {
		listGameID = new ArrayList<>();
		listScore = new ArrayList<>();
		listSum = new ArrayList<>();
		numberOfGames = 0;
		maxScore = 0;
		minScore = 0;
		positiveGames = 0;
		positiveGamesPercent = 0;
		negativeGames = 0;
		negativeGamesPercent = 0;
		scoreTotal = 0;
		scoreMean = 0;
		scoreStandardDeviation = 0;
		positiveTotal = 0;
		negativeTotal = 0;
		numberOfFourPlayerGames = 0;
		fourPlayerGamePlaces = new int[4];
		fourPlayerGamePlacePercent = new int[4];
		numberOfFivePlayerGames = 0;
		fivePlayerGamePlaces = new int[5];
		fivePlayerGamePlacePercent = new int[5];
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

	public int getPositiveTotal() {
		return positiveTotal;
	}

	public void setPositiveTotal(final int positiveTotal) {
		this.positiveTotal = positiveTotal;
	}

	public int getNegativeTotal() {
		return negativeTotal;
	}

	public void setNegativeTotal(final int negativeTotal) {
		this.negativeTotal = negativeTotal;
	}

	public int getMaxScore() {
		return maxScore;
	}

	public void setMaxScore(final int maxScore) {
		this.maxScore = maxScore;
	}

	public int getMinScore() {
		return minScore;
	}

	public void setMinScore(final int minScore) {
		this.minScore = minScore;
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

	public int getNumberOfFourPlayerGames() {
		return numberOfFourPlayerGames;
	}

	public void setNumberOfFourPlayerGames(final int numberOfFourPlayerGames) {
		this.numberOfFourPlayerGames = numberOfFourPlayerGames;
	}

	public int getNumberOfFivePlayerGames() {
		return numberOfFivePlayerGames;
	}

	public void setNumberOfFivePlayerGames(final int numberOfFivePlayerGames) {
		this.numberOfFivePlayerGames = numberOfFivePlayerGames;
	}

	public void setLists(final List<Long> listGameID, final List<Integer> listScore, final List<Integer> listSum) {
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

	public List<Long> getListGameID() {
		return Collections.unmodifiableList(listGameID);
	}

	public List<Integer> getListScore() {
		return Collections.unmodifiableList(listScore);
	}

	public List<Integer> getListSum() {
		return Collections.unmodifiableList(listSum);
	}

	public void setFourPlayerGamePlaces(final int fourPlayerGamePlaces[]) {
		if (fourPlayerGamePlaces != null && fourPlayerGamePlaces.length == 4) {
			System.arraycopy(fourPlayerGamePlaces, 0, this.fourPlayerGamePlaces, 0, 4);
		}
	}

	public int[] getFourPlayerGamePlaces() {
		return Arrays.copyOf(fourPlayerGamePlaces, fourPlayerGamePlaces.length);
	}

	public void setFourPlayerGamePlacePercent(final int fourPlayerGamePlacePercent[]) {
		if (fourPlayerGamePlacePercent != null && fourPlayerGamePlacePercent.length == 4) {
			System.arraycopy(fourPlayerGamePlacePercent, 0, this.fourPlayerGamePlacePercent, 0, 4);
		}
	}

	public int[] getFourPlayerGamePlacePercent() {
		return Arrays.copyOf(fourPlayerGamePlacePercent, fourPlayerGamePlacePercent.length);
	}

	public void setFivePlayerGamePlaces(final int fivePlayerGamePlaces[]) {
		if (fivePlayerGamePlaces != null && fivePlayerGamePlaces.length == 5) {
			System.arraycopy(fivePlayerGamePlaces, 0, this.fivePlayerGamePlaces, 0, 5);
		}
	}

	public int[] getFivePlayerGamePlaces() {
		return Arrays.copyOf(fivePlayerGamePlaces, fivePlayerGamePlaces.length);
	}

	public void setFivePlayerGamePlacePercent(final int fivePlayerGamePlacePercent[]) {
		if (fivePlayerGamePlacePercent != null && fivePlayerGamePlacePercent.length == 5) {
			System.arraycopy(fivePlayerGamePlacePercent, 0, this.fivePlayerGamePlacePercent, 0, 5);
		}
	}

	public int[] getFivePlayerGamePlacePercent() {
		return Arrays.copyOf(fivePlayerGamePlacePercent, fivePlayerGamePlacePercent.length);
	}

}
