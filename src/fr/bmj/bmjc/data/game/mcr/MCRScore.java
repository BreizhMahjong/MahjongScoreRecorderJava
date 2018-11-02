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

public class MCRScore {

	private final int playerId;
	private final String playerName;
	private final String displayName;
	private final int place;
	private final int gameScore;
	private final int finalScore;

	public MCRScore(final int playerId, final String playerName, final String displayName, final int place, final int gameScore, final int finalScore) {
		this.playerId = playerId;
		this.playerName = playerName;
		this.displayName = displayName;
		this.place = place;
		this.gameScore = gameScore;
		this.finalScore = finalScore;
	}

	public int getPlayerId() {
		return playerId;
	}

	public String getPlayerName() {
		return playerName;
	}

	public String getDisplayName() {
		return displayName;
	}

	public int getPlace() {
		return place;
	}

	public int getGameScore() {
		return gameScore;
	}

	public int getFinalScore() {
		return finalScore;
	}

}
