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
package fr.bmj.bmjc.dataaccess.abs.player;

import java.util.List;

import fr.bmj.bmjc.data.game.Player;
import fr.bmj.bmjc.dataaccess.abs.UpdateResult;

public interface DataAccessManagePlayer {

	public UpdateResult addPlayer(String name, final String displayName);

	public List<Player> getAllPlayers();

	public UpdateResult modifyPlayer(final short id, String name, final String displayName, boolean frequent, boolean regular, String license);

	public UpdateResult deletePlayer(final short id);

	public void setOnlyFrequentPlayers(boolean onlyFrequentPlayers);

	public List<Player> getPlayers();

}
