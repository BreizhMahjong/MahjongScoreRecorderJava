package fr.bmj.bmjc.dataaccess.impl.db.rcr;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import fr.bmj.bmjc.data.game.Tournament;
import fr.bmj.bmjc.dataaccess.abs.UpdateResult;
import fr.bmj.bmjc.dataaccess.abs.rcr.DataAccessRCRTournament;

public class DataAccessDataBaseRCRTournament extends DataAccessDataBaseRCRCommon implements DataAccessRCRTournament {

	public DataAccessDataBaseRCRTournament(final Connection dataBaseConnection) {
		super(dataBaseConnection);
	}

	@Override
	public UpdateResult addRCRTournament(final String tournamentName) {
		if (!isConnected()) {
			return new UpdateResult(false, "Pas de connxion à la base de données");
		}
		if (tournamentName == null) {
			return new UpdateResult(false, "Le nom ne peut pas être vide");
		}
		short newId;
		boolean added;
		try {
			final String query = "SELECT id FROM rcr_tournament ORDER BY id";
			final Statement statement = dataBaseConnection.createStatement();
			final ResultSet result = statement.executeQuery(query);
			newId = 1;
			while (result.next()) {
				if (newId == result.getShort(1)) {
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
			final String query = "INSERT INTO rcr_tournament(id, name) VALUES(?, ?)";
			final PreparedStatement statement = dataBaseConnection.prepareStatement(query);
			statement.setShort(1, newId);
			statement.setString(2, tournamentName);
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
	public UpdateResult modifyRCRTournament(final short tournamentId, final String tournamentName) {
		if (!isConnected()) {
			return new UpdateResult(false, "Pas de connxion à la base de données");
		}
		if (tournamentName == null) {
			return new UpdateResult(false, "Le nom ne peut pas être vide");
		}

		boolean modified;
		try {
			final String query = "UPDATE rcr_tournament SET name=? WHERE id=?";
			final PreparedStatement statement = dataBaseConnection.prepareStatement(query);
			statement.setString(1, tournamentName);
			statement.setShort(2, tournamentId);
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
	public List<Tournament> getRCRTournaments() {
		final List<Tournament> tournamentList = new ArrayList<Tournament>();
		if (isConnected()) {
			try {
				final Statement statement = dataBaseConnection.createStatement();
				final ResultSet result = statement.executeQuery("SELECT id, name FROM rcr_tournament ORDER BY id DESC");
				while (result.next()) {
					tournamentList.add(new Tournament(result.getShort(1), result.getString(2)));
				}
				result.close();
				statement.close();
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}
		return tournamentList;
	}

	@Override
	public UpdateResult deleteRCRTournament(final short tournamentId) {
		if (!isConnected()) {
			return new UpdateResult(false, "Pas de connxion à la base de données");
		}

		boolean modified;
		try {
			final String query = "DELETE FROM rcr_tournament WHERE id=?";
			final PreparedStatement statement = dataBaseConnection.prepareStatement(query);
			statement.setShort(1, tournamentId);
			modified = statement.executeUpdate() == 1;
			statement.close();
		} catch (final SQLException e) {
			e.printStackTrace();
			return new UpdateResult(false, "Erreur de connexion de base de données");
		}

		if (modified) {
			return new UpdateResult(true, "OK");
		} else {
			return new UpdateResult(false, "Le tournoi n'a pas été supprimé");
		}
	}

}
