/*
 * This file is part of Breizh Mahjong Recorder.
 *
 * Breizh Mahjong Recorder is free software: you can redistribute it AND/or
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
package fr.bmj.bmjc.dataaccess.impl.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import fr.bmj.bmjc.data.game.Player;
import fr.bmj.bmjc.dataaccess.DataAccessCommon;

public class DataAccessDataBaseCommon implements DataAccessCommon {

	protected final Connection dataBaseConnection;

	private boolean onlyFrequentPlayers;

	public DataAccessDataBaseCommon(final Connection dataBaseConnection) {
		this.dataBaseConnection = dataBaseConnection;
	}

	@Override
	public void setOnlyFrequentPlayers(final boolean onlyFrequentPlayers) {
		this.onlyFrequentPlayers = onlyFrequentPlayers;
	}

	@Override
	public List<Player> getPlayers() {
		final List<Player> playerList = new ArrayList<Player>();
		if (dataBaseConnection != null) {
			try {
				String query = "SELECT id, name, display_name FROM player ";
				if (onlyFrequentPlayers) {
					query = query + "WHERE frequent=true ";
				}
				query = query + "ORDER BY id";
				final Statement statement = dataBaseConnection.createStatement();
				final ResultSet result = statement.executeQuery(query);
				while (result.next()) {
					playerList.add(new Player(result.getInt(1), result.getString(2), result.getString(3), false, true, ""));
				}
				result.close();
				statement.close();
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}
		return playerList;
	}

}
