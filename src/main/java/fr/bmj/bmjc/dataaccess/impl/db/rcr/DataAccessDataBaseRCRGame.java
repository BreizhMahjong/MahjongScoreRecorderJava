package fr.bmj.bmjc.dataaccess.impl.db.rcr;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import fr.bmj.bmjc.data.game.Player;
import fr.bmj.bmjc.data.game.Tournament;
import fr.bmj.bmjc.data.game.rcr.RCRGame;
import fr.bmj.bmjc.data.game.rcr.RCRScore;
import fr.bmj.bmjc.dataaccess.abs.UpdateResult;
import fr.bmj.bmjc.dataaccess.abs.rcr.DataAccessRCRGame;

public class DataAccessDataBaseRCRGame extends DataAccessDataBaseRCRCommon implements DataAccessRCRGame {

	private boolean onlyRegularPlayers;

	public DataAccessDataBaseRCRGame(final Connection dataBaseConnection) {
		super(dataBaseConnection);
	}

	@Override
	public UpdateResult addRCRGame(final RCRGame game) {
		if (!isConnected()) {
			return new UpdateResult(false, "Pas de connxion à la base de données");
		}
		if (game == null) {
			return new UpdateResult(false, "L'information du jeu ne peut pas être vide");
		}

		final Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.YEAR, game.getYear());
		calendar.set(Calendar.MONTH, game.getMonth());
		calendar.set(Calendar.DAY_OF_MONTH, game.getDay());
		final Date date = new Date(calendar.getTimeInMillis());
		long newId;
		try {
			final String query = "SELECT id FROM rcr_game_id WHERE date=? ORDER BY id";
			final PreparedStatement statement = dataBaseConnection.prepareStatement(query);
			statement.setDate(1, date);
			final ResultSet result = statement.executeQuery();
			newId = ((game.getYear() * 100 + game.getMonth() + 1) * 100 + game.getDay()) * 100 + 1;
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
			return new UpdateResult(false, "Erreur de connexion de base de données");
		}

		try {
			final String idTableQuery = "INSERT INTO rcr_game_id(id, rcr_tournament_id, date, nb_players, nb_rounds) VALUES(?, ?, ?, ?, ?)";
			final PreparedStatement idTableStatement = dataBaseConnection.prepareStatement(idTableQuery);
			idTableStatement.setLong(1, newId);
			idTableStatement.setShort(2, game.getTournamentId());
			idTableStatement.setDate(3, date);
			idTableStatement.setShort(4, game.getNbPlayers());
			idTableStatement.setShort(5, game.getNbRounds());
			idTableStatement.executeUpdate();
			idTableStatement.close();

			final String scoreTableQuery = "INSERT INTO rcr_game_score(rcr_game_id, player_id, ranking, game_score, uma_score, final_score) values(?, ?, ?, ?, ?, ?)";
			final PreparedStatement scoreTableStatement = dataBaseConnection.prepareStatement(scoreTableQuery);
			for (int playerIndex = 0; playerIndex < game.getScores().size(); playerIndex++) {
				final RCRScore score = game.getScores().get(playerIndex);
				scoreTableStatement.setLong(1, newId);
				scoreTableStatement.setShort(2, score.getPlayerId());
				scoreTableStatement.setShort(3, score.getPlace());
				scoreTableStatement.setInt(4, score.getGameScore());
				scoreTableStatement.setInt(5, score.getUmaScore());
				scoreTableStatement.setInt(6, score.getFinalScore());
				scoreTableStatement.execute();
				scoreTableStatement.clearParameters();
			}
			scoreTableStatement.close();
		} catch (final SQLException e) {
			e.printStackTrace();
			return new UpdateResult(false, "Erreur de connexion de base de données");
		}
		return new UpdateResult(true, "Scores enregistrés. ID " + Long.toString(newId) + ".");
	}

	@Override
	public List<Integer> getRCRYears(final Tournament tournament) {
		final List<Integer> yearList = new ArrayList<Integer>();
		if (isConnected()) {
			try {
				final PreparedStatement statement = dataBaseConnection
					.prepareStatement("SELECT DISTINCT YEAR(date) FROM rcr_game_id WHERE rcr_tournament_id=? ORDER BY YEAR(date) DESC");
				statement.setShort(1, tournament.getId());
				final ResultSet result = statement.executeQuery();
				while (result.next()) {
					yearList.add(result.getInt(1));
				}
				result.close();
				statement.close();
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}
		return yearList;
	}

	@Override
	public List<Integer> getRCRGameDays(final Tournament tournament, final int year, final int month) {
		final List<Integer> dayList = new ArrayList<Integer>();
		if (isConnected()) {
			try {
				final PreparedStatement statement = dataBaseConnection.prepareStatement(
					"SELECT DISTINCT day(date) FROM rcr_game_id WHERE rcr_tournament_id=? AND year(date)=? AND month(date)=? ORDER BY day(date)");
				statement.setShort(1, tournament.getId());
				statement.setInt(2, year);
				statement.setInt(3, month + 1);

				final ResultSet result = statement.executeQuery();
				while (result.next()) {
					dayList.add(result.getInt(1));
				}
				result.close();
				statement.close();
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}
		return dayList;
	}

	@Override
	public List<Long> getRCRGameIds(final Tournament tournament, final int year, final int month, final int day) {
		final List<Long> idList = new ArrayList<Long>();
		if (isConnected()) {
			try {
				final Calendar calendar = Calendar.getInstance();
				calendar.set(Calendar.YEAR, year);
				calendar.set(Calendar.MONTH, month);
				calendar.set(Calendar.DAY_OF_MONTH, day);
				final PreparedStatement statement = dataBaseConnection
					.prepareStatement("SELECT id FROM rcr_game_id WHERE rcr_tournament_id=? AND date=? ORDER BY id");
				statement.setShort(1, tournament.getId());
				statement.setDate(2, new Date(calendar.getTimeInMillis()));

				final ResultSet result = statement.executeQuery();
				while (result.next()) {
					idList.add(new Long(result.getLong(1)));
				}
				result.close();
				statement.close();
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}
		return idList;
	}

	@Override
	public RCRGame getRCRGame(final long id) {
		if (isConnected()) {
			try {
				final Calendar calendar = Calendar.getInstance();
				short nbPlayers = 0;
				short nbRounds = 0;
				short tournamentId = 0;
				PreparedStatement statement = dataBaseConnection
					.prepareStatement("SELECT rcr_tournament_id, date, nb_players, nb_rounds FROM rcr_game_id WHERE id=?");
				statement.setLong(1, id);
				ResultSet result = statement.executeQuery();
				if (result.next()) {
					tournamentId = result.getShort(1);
					calendar.setTimeInMillis(result.getDate(2).getTime());
					nbPlayers = result.getShort(3);
					nbRounds = result.getShort(4);
				}
				result.close();
				statement.close();

				if (nbPlayers > 0) {
					final List<RCRScore> scoreList = new ArrayList<RCRScore>(nbPlayers);
					statement = dataBaseConnection.prepareStatement(
						"SELECT player.id, player.name, player.display_name, rcr_game_score.ranking, rcr_game_score.game_score, rcr_game_score.uma_score, rcr_game_score.final_score FROM player, rcr_game_score WHERE player.id=rcr_game_score.player_id AND rcr_game_score.rcr_game_id=? ORDER BY rcr_game_score.ranking");
					statement.setLong(1, id);
					result = statement.executeQuery();
					while (result.next()) {
						scoreList.add(new RCRScore(result.getShort(1), result.getString(2), result.getString(3), result.getShort(4), result.getInt(5),
							result.getInt(6), result.getInt(7)));
					}
					result.close();
					statement.close();

					return new RCRGame(id, tournamentId, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH),
						nbRounds, nbPlayers, scoreList);
				} else {
					return null;
				}
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	@Override
	public UpdateResult deleteRCRGame(final long id) {
		if (!isConnected()) {
			return new UpdateResult(false, "Pas de connxion à la base de données");
		}

		boolean modified;
		try {
			final String query = "DELETE FROM rcr_game_id WHERE id=?";
			final PreparedStatement statement = dataBaseConnection.prepareStatement(query);
			statement.setLong(1, id);
			modified = statement.executeUpdate() == 1;
			statement.close();
		} catch (final SQLException e) {
			e.printStackTrace();
			return new UpdateResult(false, "Erreur de connexion de données");
		}

		if (modified) {
			return new UpdateResult(true, "OK");
		} else {
			return new UpdateResult(false, "Le tournoi n'a pas été supprimé");
		}
	}

	@Override
	public void setOnlyRegularPlayers(final boolean onlyRegularPlayers) {
		this.onlyRegularPlayers = onlyRegularPlayers;
	}

	@Override
	public List<Player> getRCRPlayers() {
		final List<Player> playerList = new ArrayList<Player>();
		if (isConnected()) {
			try {
				final String querySelectPart = "SELECT DISTINCT player.id, player.name, player.display_name FROM player, rcr_game_score";
				final String queryWherePart = " WHERE player.id=rcr_game_score.player_id";
				final String queryRegularPart = onlyRegularPlayers ? " AND player.regular=TRUE" : "";
				final String queryOrderPart = "  ORDER BY player.id";

				final Statement statement = dataBaseConnection.createStatement();
				final ResultSet result = statement.executeQuery(querySelectPart + queryWherePart + queryRegularPart + queryOrderPart);
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
