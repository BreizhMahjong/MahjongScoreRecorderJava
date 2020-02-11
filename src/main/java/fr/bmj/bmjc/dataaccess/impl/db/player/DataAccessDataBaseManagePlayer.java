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
package fr.bmj.bmjc.dataaccess.impl.db.player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import fr.bmj.bmjc.data.game.Player;
import fr.bmj.bmjc.dataaccess.abs.UpdateResult;
import fr.bmj.bmjc.dataaccess.abs.player.DataAccessManagePlayer;

public class DataAccessDataBaseManagePlayer implements DataAccessManagePlayer {

	protected final Connection dataBaseConnection;

	private boolean onlyFrequentPlayers;

	public DataAccessDataBaseManagePlayer(final Connection dataBaseConnection) {
		this.dataBaseConnection = dataBaseConnection;
	}

	private boolean isConnected() {
		return dataBaseConnection != null;
	}

	@Override
	public UpdateResult addPlayer(final String name, final String displayName) {
		if (!isConnected()) {
			return new UpdateResult(false, "Pas de connxion à la base de données");
		}
		if (name == null || displayName == null) {
			return new UpdateResult(false, "Le nom ne peut pas être vide");
		}

		short newId;
		boolean added;
		try {
			final String query = "SELECT id FROM player ORDER BY id";
			final Statement statement = dataBaseConnection.createStatement();
			final ResultSet result = statement.executeQuery(query);
			newId = 1;
			while (result.next()) {
				if (newId == result.getInt(1)) {
					newId++;
				} else {
					break;
				}
			}
			result.close();
			statement.close();
		} catch (final SQLException e) {
			e.printStackTrace();
			return new UpdateResult(false, "Erreur de connexion de données");
		}

		try {
			final String query = "INSERT INTO player(id, name, display_name, frequent, regular) VALUES(?, ?, ?, ?, ?)";
			final PreparedStatement statement = dataBaseConnection.prepareStatement(query);
			statement.setShort(1, newId);
			statement.setString(2, name);
			statement.setString(3, displayName);
			statement.setBoolean(4, true);
			statement.setBoolean(5, true);
			added = statement.executeUpdate() == 1;
			statement.close();
		} catch (final SQLException e) {
			e.printStackTrace();
			return new UpdateResult(false, "Erreur de connexion de données");
		}

		if (added) {
			return new UpdateResult(true, "OK");
		} else {
			return new UpdateResult(false, "Le nom est déjà utilisé");
		}
	}

	@Override
	public List<Player> getAllPlayers() {
		final List<Player> playerList = new ArrayList<Player>();
		if (dataBaseConnection != null) {
			try {
				final Statement statement = dataBaseConnection.createStatement();
				final ResultSet result = statement.executeQuery("SELECT id, name, display_name, frequent, regular, license FROM player ORDER BY id");
				while (result.next()) {
					playerList.add(new Player(result.getShort(1), result.getString(2), result.getString(3), result.getBoolean(4), result.getBoolean(5),
						result.getString(6)));
				}
				result.close();
				statement.close();
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}
		return playerList;
	}

	@Override
	public UpdateResult modifyPlayer(final short id, final String name, final String displayName, final boolean frequent, final boolean regular,
		final String license) {
		if (!isConnected()) {
			return new UpdateResult(false, "Pas de connxion à la base de données");
		}
		if (name == null || displayName == null) {
			return new UpdateResult(false, "Le nom ne peut pas être vide");
		}

		boolean modified;
		try {
			final String query = "UPDATE player SET name=?, display_name=?, frequent=?, regular=?, license=? WHERE id=?";
			final PreparedStatement statement = dataBaseConnection.prepareStatement(query);
			statement.setString(1, name);
			statement.setString(2, displayName);
			statement.setBoolean(3, frequent);
			statement.setBoolean(4, regular);
			if (license == null || license.length() == 0) {
				statement.setNull(5, Types.VARCHAR);
			} else {
				statement.setString(5, license);
			}
			statement.setShort(6, id);
			modified = statement.executeUpdate() == 1;
			statement.close();
		} catch (final SQLException e) {
			e.printStackTrace();
			return new UpdateResult(false, "Erreur de connexion de données");
		}

		if (modified) {
			return new UpdateResult(true, "OK");
		} else {
			return new UpdateResult(false, "Le nom est déjà utilisé");
		}
	}

	@Override
	public UpdateResult deletePlayer(final short id) {
		if (!isConnected()) {
			return new UpdateResult(false, "Pas de connxion à la base de données");
		}

		boolean modified;
		try {
			final String query = "DELETE FROM player WHERE id=?";
			final PreparedStatement statement = dataBaseConnection.prepareStatement(query);
			statement.setShort(1, id);
			modified = statement.executeUpdate() == 1;
			statement.close();
		} catch (final SQLException e) {
			e.printStackTrace();
			return new UpdateResult(false, "Erreur de connexion de données");
		}

		if (modified) {
			return new UpdateResult(true, "OK");
		} else {
			return new UpdateResult(false, "Le joueur n'a pas été supprimé");
		}
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
					playerList.add(new Player(result.getShort(1), result.getString(2), result.getString(3), false, true, ""));
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
