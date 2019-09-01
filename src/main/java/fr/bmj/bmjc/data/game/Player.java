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
package fr.bmj.bmjc.data.game;

public class Player {

	private final int playerID;
	private final String playerName;
	private final String displayName;
	private boolean frequent;
	private final boolean regular;

	public Player(final int playerID, final String playerName, final String displayName, final boolean frequent, final boolean regular) {
		this.playerID = playerID;
		this.playerName = playerName;
		this.displayName = displayName;
		this.frequent = frequent;
		this.regular = regular;
	}

	public int getPlayerID() {
		return playerID;
	}

	public String getPlayerName() {
		return playerName;
	}

	public String getDisplayName() {
		return displayName;
	}

	public boolean isFrequent() {
		return frequent;
	}

	public void setFrequent(final boolean frequent) {
		this.frequent = frequent;
	}

	public boolean isRegular() {
		return regular;
	}

}
