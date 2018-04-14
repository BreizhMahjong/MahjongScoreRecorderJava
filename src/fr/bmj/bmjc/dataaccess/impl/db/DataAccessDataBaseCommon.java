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

	public DataAccessDataBaseCommon(final Connection dataBaseConnection) {
		this.dataBaseConnection = dataBaseConnection;
	}

	@Override
	public List<Player> getRegisteredPlayers() {
		final List<Player> playerList = new ArrayList<Player>();
		if (dataBaseConnection != null) {
			try {
				final Statement statement = dataBaseConnection.createStatement();
				final ResultSet result = statement.executeQuery("SELECT id, name, display_name FROM player ORDER BY id");
				while (result.next()) {
					playerList.add(new Player(result.getInt(1), result.getString(2), result.getString(3)));
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
