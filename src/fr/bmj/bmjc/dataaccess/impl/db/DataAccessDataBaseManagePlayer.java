package fr.bmj.bmjc.dataaccess.impl.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import fr.bmj.bmjc.data.game.Player;
import fr.bmj.bmjc.dataaccess.DataAccessManagePlayer;
import fr.bmj.bmjc.dataaccess.UpdateResult;

public class DataAccessDataBaseManagePlayer extends DataAccessDataBaseCommon implements DataAccessManagePlayer {

	public DataAccessDataBaseManagePlayer(final Connection dataBaseConnection) {
		super(dataBaseConnection);
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

		int newId;
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
			final String query = "INSERT INTO player(id, name, display_name, hidden) VALUES(?, ?, ?, ?)";
			final PreparedStatement statement = dataBaseConnection.prepareStatement(query);
			statement.setInt(1, newId);
			statement.setString(2, name);
			statement.setString(3, displayName);
			statement.setBoolean(4, false);
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
				final ResultSet result = statement.executeQuery("SELECT id, name, display_name, hidden FROM player ORDER BY id");
				while (result.next()) {
					playerList.add(new Player(result.getInt(1), result.getString(2), result.getString(3), result.getBoolean(4)));
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
	public UpdateResult modifyPlayer(final int id, final String name, final String displayName, final boolean hidden) {
		if (!isConnected()) {
			return new UpdateResult(false, "Pas de connxion à la base de données");
		}
		if (name == null || displayName == null) {
			return new UpdateResult(false, "Le nom ne peut pas être vide");
		}

		boolean modified;
		try {
			final String query = "UPDATE player SET name=?, display_name=?, hidden=? WHERE id=?";
			final PreparedStatement statement = dataBaseConnection.prepareStatement(query);
			statement.setString(1, name);
			statement.setString(2, displayName);
			statement.setBoolean(3, hidden);
			statement.setInt(4, id);
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
	public UpdateResult deletePlayer(final int id) {
		if (!isConnected()) {
			return new UpdateResult(false, "Pas de connxion à la base de données");
		}

		boolean modified;
		try {
			final String query = "DELETE FROM player WHERE id=?";
			final PreparedStatement statement = dataBaseConnection.prepareStatement(query);
			statement.setInt(1, id);
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

}
