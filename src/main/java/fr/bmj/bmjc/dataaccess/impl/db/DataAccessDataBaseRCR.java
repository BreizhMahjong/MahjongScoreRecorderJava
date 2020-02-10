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
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import fr.bmj.bmjc.data.game.Player;
import fr.bmj.bmjc.data.game.Tournament;
import fr.bmj.bmjc.data.game.rcr.RCRGame;
import fr.bmj.bmjc.data.game.rcr.RCRScore;
import fr.bmj.bmjc.data.stat.rcr.RCRDataPackageAnalyze;
import fr.bmj.bmjc.data.stat.rcr.RCRDataPackageScoreAnalyze;
import fr.bmj.bmjc.data.stat.rcr.RCRDataPackageTrend;
import fr.bmj.bmjc.data.stat.rcr.RCRTotalScore;
import fr.bmj.bmjc.dataaccess.UpdateResult;
import fr.bmj.bmjc.dataaccess.rcr.DataAccessRCR;
import fr.bmj.bmjc.enums.EnumPeriodMode;
import fr.bmj.bmjc.enums.EnumRankingMode;
import fr.bmj.bmjc.enums.EnumScoreMode;
import fr.bmj.bmjc.enums.EnumSortingMode;

public class DataAccessDataBaseRCR extends DataAccessDataBaseCommon implements DataAccessRCR {

	private static final int NUMBER_TOP = 30;
	private static final int MINIMUM_GAME_MONTH = 4;
	private static final int MINIMUM_GAME_TRIMESTER = 8;
	private static final int MINIMUM_GAME_YEAR = 32;

	private boolean useMinimumGame;
	private boolean onlyRegularPlayers;

	public DataAccessDataBaseRCR(final Connection dataBaseConnection) {
		super(dataBaseConnection);
	}

	private boolean isConnected() {
		try {
			return dataBaseConnection != null && !dataBaseConnection.isClosed();
		} catch (final SQLException e) {
			return false;
		}
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
	public void setRCRUseMinimumGame(final boolean useMinimumGame) {
		this.useMinimumGame = useMinimumGame;
	}

	@Override
	public void setRCROnlyRegularPlayers(final boolean onlyRegularPlayers) {
		this.onlyRegularPlayers = onlyRegularPlayers;
	}

	@Override
	public RCRDataPackageAnalyze getRCRDataPackageAnalyze(final Tournament tournament, final short playerId, final EnumScoreMode scoreMode,
		final EnumPeriodMode periodMode, final int year, final int trimester, final int month, final int day) {
		final Calendar calendarFrom = Calendar.getInstance();
		final Calendar calendarTo = Calendar.getInstance();
		switch (periodMode) {
			case ALL:
				break;
			case YEAR:
				calendarFrom.set(Calendar.YEAR, year);
				calendarFrom.set(Calendar.MONTH, Calendar.JANUARY);
				calendarFrom.set(Calendar.DAY_OF_MONTH, 1);
				calendarTo.setTime(calendarFrom.getTime());
				calendarTo.add(Calendar.YEAR, 1);
				break;
			case TRIMESTER:
				calendarFrom.set(Calendar.YEAR, year);
				calendarFrom.set(Calendar.MONTH, trimester * 3);
				calendarFrom.set(Calendar.DAY_OF_MONTH, 1);
				calendarTo.setTime(calendarFrom.getTime());
				calendarTo.add(Calendar.MONTH, 3);
				break;
			case MONTH:
				calendarFrom.set(Calendar.YEAR, year);
				calendarFrom.set(Calendar.MONTH, month);
				calendarFrom.set(Calendar.DAY_OF_MONTH, 1);
				calendarTo.setTime(calendarFrom.getTime());
				calendarTo.add(Calendar.MONTH, 1);
				break;
			case DAY:
				calendarFrom.set(Calendar.YEAR, year);
				calendarFrom.set(Calendar.MONTH, month);
				calendarFrom.set(Calendar.DAY_OF_MONTH, day);
				calendarTo.setTime(calendarFrom.getTime());
				calendarTo.add(Calendar.DAY_OF_MONTH, 1);
				break;
			default:
				break;
		}

		final RCRDataPackageAnalyze dataPackage = new RCRDataPackageAnalyze();
		try {
			String fieldString = null;
			switch (scoreMode) {
				case FINAL_SCORE:
					fieldString = "final_score";
					break;
				case GAME_SCORE:
					fieldString = "game_score";
					break;
				default:
					fieldString = "";
					break;
			}

			PreparedStatement statement = null;
			if (periodMode == EnumPeriodMode.ALL) {
				statement = dataBaseConnection.prepareStatement("SELECT rcr_game_id.id, rcr_game_id.nb_players, rcr_game_score.ranking, rcr_game_score."
					+ fieldString
					+ " FROM rcr_game_id, rcr_game_score WHERE rcr_game_id.id=rcr_game_score.rcr_game_id AND rcr_game_score.player_id=? AND rcr_game_id.rcr_tournament_id=? ORDER BY rcr_game_id.id ASC");
				statement.setShort(1, playerId);
				statement.setShort(2, tournament.getId());
			} else {
				statement = dataBaseConnection.prepareStatement("SELECT rcr_game_id.id, rcr_game_id.nb_players, rcr_game_score.ranking, rcr_game_score."
					+ fieldString
					+ " FROM rcr_game_id, rcr_game_score WHERE rcr_game_id.id=rcr_game_score.rcr_game_id AND rcr_game_score.player_id=? AND rcr_game_id.rcr_tournament_id=? AND rcr_game_id.date>=? AND rcr_game_id.date<? ORDER BY rcr_game_id.id ASC");
				statement.setShort(1, playerId);
				statement.setShort(2, tournament.getId());
				statement.setDate(3, new Date(calendarFrom.getTimeInMillis()));
				statement.setDate(4, new Date(calendarTo.getTimeInMillis()));
			}
			final ResultSet result = statement.executeQuery();

			int numberOfGames = 0;
			final List<Long> listGameID = new ArrayList<Long>();
			final List<Integer> listScore = new ArrayList<Integer>();
			final List<Integer> listSum = new ArrayList<Integer>();

			int numberOfFourPlayersGames = 0;
			int numberOfFivePlayersGames = 0;
			int totalScore = 0;
			int positiveGames = 0;
			int negativeGames = 0;
			final int placeFourPlayers[] = new int[4];
			final int placeFourPlayersPercent[] = new int[4];
			final int placeFivePlayers[] = new int[5];
			final int placeFivePlayersPercent[] = new int[5];
			int maxScore = Integer.MIN_VALUE;
			int minScore = Integer.MAX_VALUE;
			int positiveTotal = 0;
			int negativeTotal = 0;

			while (result.next()) {
				final long gameID = result.getLong(1);
				final short nbPlayers = result.getShort(2);
				final short ranking = result.getShort(3);
				final int score = result.getInt(4);

				listGameID.add(numberOfGames, gameID);
				listScore.add(numberOfGames, score);

				if (score >= 0) {
					positiveGames++;
					positiveTotal += score;
				} else {
					negativeGames++;
					negativeTotal += score;
				}
				maxScore = Math.max(maxScore, score);
				minScore = Math.min(minScore, score);

				totalScore += score;
				listSum.add(numberOfGames, totalScore);

				if (nbPlayers == 4) {
					placeFourPlayers[ranking - 1]++;
					numberOfFourPlayersGames++;
				} else if (nbPlayers == 5) {
					placeFivePlayers[ranking - 1]++;
					numberOfFivePlayersGames++;
				}
				numberOfGames++;
			}
			final double averageScore = (double) totalScore / numberOfGames;
			double deviation = 0.0;
			for (int index = 0; index < numberOfGames; index++) {
				deviation += Math.pow(listScore.get(index) - averageScore, 2.0);
			}
			final long standardDeviation = numberOfGames <= 1 ? 0 : Math.round(Math.sqrt(deviation / numberOfGames));

			for (int index = 0; index < 4; index++) {
				placeFourPlayersPercent[index] = Math.round(placeFourPlayers[index] * 100f / numberOfFourPlayersGames);
			}
			for (int index = 0; index < 5; index++) {
				placeFivePlayersPercent[index] = Math.round(placeFivePlayers[index] * 100f / numberOfFivePlayersGames);
			}

			dataPackage.setLists(listGameID, listScore, listSum);
			dataPackage.setNumberOfGames(numberOfGames);

			dataPackage.setMaxScore(maxScore);
			dataPackage.setMinScore(minScore);

			dataPackage.setPositiveGames(positiveGames);
			dataPackage.setPositiveGamesPercent(Math.round(positiveGames * 100f / numberOfGames));

			dataPackage.setNegativeGames(negativeGames);
			dataPackage.setNegativeGamesPercent(Math.round(negativeGames * 100f / numberOfGames));

			dataPackage.setScoreTotal(totalScore);
			dataPackage.setScoreMean((int) Math.round(averageScore));
			dataPackage.setScoreStandardDeviation((int) standardDeviation);

			dataPackage.setPositiveTotal(positiveTotal);
			dataPackage.setNegativeTotal(negativeTotal);

			dataPackage.setNumberOfFourPlayerGames(numberOfFourPlayersGames);
			dataPackage.setFourPlayerGamePlaces(placeFourPlayers);
			dataPackage.setFourPlayerGamePlacePercent(placeFourPlayersPercent);

			dataPackage.setNumberOfFivePlayerGames(numberOfFivePlayersGames);
			dataPackage.setFivePlayerGamePlaces(placeFivePlayers);
			dataPackage.setFivePlayerGamePlacePercent(placeFivePlayersPercent);

			result.close();
			statement.close();
		} catch (final Exception e) {
			dataPackage.setNumberOfGames(0);
			e.printStackTrace();
		}
		return dataPackage;
	}

	private static final double MILLISECONDS_PER_YEAR = 31557600000.0f;

	private float getNumberOfYearOfAllGamePeriod(final Tournament tournament) {
		Date firstDate = null;
		Date lastDate = null;
		try {
			final PreparedStatement statement = dataBaseConnection.prepareStatement("SELECT MIN(date), MAX(date) FROM rcr_game_id WHERE rcr_tournament_id=?");
			statement.setShort(1, tournament.getId());
			final ResultSet result = statement.executeQuery();
			if (result.next()) {
				firstDate = result.getDate(1);
				lastDate = result.getDate(2);
			}
			result.close();
			statement.close();
		} catch (final Exception e) {
			e.printStackTrace();
		}

		if (firstDate != null && lastDate != null) {
			return (float) ((lastDate.getTime() - firstDate.getTime()) / MILLISECONDS_PER_YEAR);
		} else {
			return 0.0f;
		}
	}

	private float getProportionalPeriod(final long from, final long to) {
		if (from < to) {
			final Calendar calendar = Calendar.getInstance();
			final long today = calendar.getTimeInMillis();
			if (today >= from && to >= today) {
				return (float) ((double) (today - from) / (double) (to - from));
			} else {
				return 1.0f;
			}
		} else {
			return 0.0f;
		}
	}

	@Override
	public List<RCRTotalScore> getRCRDataPackageRanking(final Tournament tournament, final EnumRankingMode rankingMode, final EnumSortingMode sortingMode,
		final EnumPeriodMode periodMode, final int year, final int trimester, final int month, final int day) {
		final Calendar calendarFrom = Calendar.getInstance();
		final Calendar calendarTo = Calendar.getInstance();
		int minimumGames = 0;
		switch (periodMode) {
			case ALL:
				if (useMinimumGame) {
					minimumGames = Math.round(MINIMUM_GAME_YEAR * getNumberOfYearOfAllGamePeriod(tournament));
				}
				break;
			case YEAR:
				calendarFrom.set(Calendar.YEAR, year);
				calendarFrom.set(Calendar.MONTH, Calendar.JANUARY);
				calendarFrom.set(Calendar.DAY_OF_MONTH, 1);
				calendarTo.setTime(calendarFrom.getTime());
				calendarTo.add(Calendar.YEAR, 1);
				minimumGames = Math.round(getProportionalPeriod(calendarFrom.getTimeInMillis(), calendarTo.getTimeInMillis()) * MINIMUM_GAME_YEAR);
				break;
			case TRIMESTER:
				calendarFrom.set(Calendar.YEAR, year);
				calendarFrom.set(Calendar.MONTH, trimester * 3);
				calendarFrom.set(Calendar.DAY_OF_MONTH, 1);
				calendarTo.setTime(calendarFrom.getTime());
				calendarTo.add(Calendar.MONTH, 3);
				minimumGames = Math.round(getProportionalPeriod(calendarFrom.getTimeInMillis(), calendarTo.getTimeInMillis()) * MINIMUM_GAME_TRIMESTER);
				break;
			case MONTH:
				calendarFrom.set(Calendar.YEAR, year);
				calendarFrom.set(Calendar.MONTH, month);
				calendarFrom.set(Calendar.DAY_OF_MONTH, 1);
				calendarTo.setTime(calendarFrom.getTime());
				calendarTo.add(Calendar.MONTH, 1);
				minimumGames = Math.round(getProportionalPeriod(calendarFrom.getTimeInMillis(), calendarTo.getTimeInMillis()) * MINIMUM_GAME_MONTH);
				break;
			case DAY:
				calendarFrom.set(Calendar.YEAR, year);
				calendarFrom.set(Calendar.MONTH, month);
				calendarFrom.set(Calendar.DAY_OF_MONTH, day);
				calendarTo.setTime(calendarFrom.getTime());
				calendarTo.add(Calendar.DAY_OF_MONTH, 1);
				minimumGames = 0;
				break;
			default:
				break;
		}

		final List<RCRTotalScore> rankingScores = new ArrayList<>();
		try {
			switch (rankingMode) {
				case TOTAL_FINAL_SCORE: {
					final String querySelectPart = "SELECT player.name, player.display_name, SUM(rcr_game_score.final_score) AS total, COUNT(*) AS nb_games FROM player, rcr_game_id, rcr_game_score";
					final String queryWherePart = " WHERE player.id=rcr_game_score.player_id AND rcr_game_id.id=rcr_game_score.rcr_game_id AND rcr_game_id.rcr_tournament_id=?";
					final String queryRegularPart = onlyRegularPlayers ? " AND player.regular=TRUE" : "";
					final String queryPeriodPart = " AND rcr_game_id.date>=? AND rcr_game_id.date<?";
					final String queryGroupPart = " GROUP BY player.name, player.display_name";
					final String queryHavingPart = useMinimumGame ? " HAVING COUNT(*)>=" + Integer.toString(minimumGames) : "";
					final String queryOrderPart = sortingMode == EnumSortingMode.DESCENDING ? " ORDER BY total DESC" : " ORDER BY total ASC";
					PreparedStatement statement = null;
					if (periodMode == EnumPeriodMode.ALL) {
						statement = dataBaseConnection
							.prepareStatement(querySelectPart + queryWherePart + queryRegularPart + queryGroupPart + queryHavingPart + queryOrderPart);
						statement.setShort(1, tournament.getId());
					} else {
						statement = dataBaseConnection.prepareStatement(
							querySelectPart + queryWherePart + queryRegularPart + queryPeriodPart + queryGroupPart + queryHavingPart + queryOrderPart);
						statement.setShort(1, tournament.getId());
						statement.setDate(2, new Date(calendarFrom.getTimeInMillis()));
						statement.setDate(3, new Date(calendarTo.getTimeInMillis()));
					}

					final ResultSet result = statement.executeQuery();
					while (result.next()) {
						final RCRTotalScore total = new RCRTotalScore(result.getString(1), result.getString(2), 0, 0, 0);
						total.totalScore = new Integer(result.getInt(3));
						total.numberOfGame = new Integer(result.getInt(4));
						rankingScores.add(total);
					}
					result.close();
					statement.close();
				}
					break;
				case MEAN_FINAL_SCORE: {
					final String querySelectPart = "SELECT player.name, player.display_name, AVG(rcr_game_score.final_score) AS mean, STDDEV_POP(rcr_game_score.final_score) as stddev, COUNT(*) AS nb_games FROM player, rcr_game_id, rcr_game_score";
					final String queryWherePart = " WHERE player.id=rcr_game_score.player_id AND rcr_game_id.id=rcr_game_score.rcr_game_id AND rcr_game_id.rcr_tournament_id=?";
					final String queryRegularPart = onlyRegularPlayers ? " AND player.regular=TRUE" : "";
					final String queryPeriodPart = " AND rcr_game_id.date>=? AND rcr_game_id.date<?";
					final String queryGroupPart = " GROUP BY player.name, player.display_name";
					final String queryHavingPart = useMinimumGame ? " HAVING COUNT(*)>=" + Integer.toString(minimumGames) : "";
					final String queryOrderPart = sortingMode == EnumSortingMode.DESCENDING ? " ORDER BY mean DESC" : " ORDER BY mean ASC";
					PreparedStatement statement = null;
					if (periodMode == EnumPeriodMode.ALL) {
						statement = dataBaseConnection
							.prepareStatement(querySelectPart + queryWherePart + queryRegularPart + queryGroupPart + queryHavingPart + queryOrderPart);
						statement.setShort(1, tournament.getId());
					} else {
						statement = dataBaseConnection.prepareStatement(
							querySelectPart + queryWherePart + queryRegularPart + queryPeriodPart + queryGroupPart + queryHavingPart + queryOrderPart);
						statement.setShort(1, tournament.getId());
						statement.setDate(2, new Date(calendarFrom.getTimeInMillis()));
						statement.setDate(3, new Date(calendarTo.getTimeInMillis()));
					}

					final ResultSet result = statement.executeQuery();
					while (result.next()) {
						final RCRTotalScore total = new RCRTotalScore(result.getString(1), result.getString(2), 0, 0, 0);
						total.totalScore = result.getInt(3);
						total.umaScore = new Long(Math.round(result.getDouble(4)));
						total.numberOfGame = new Integer(result.getInt(5));
						rankingScores.add(total);
					}
					result.close();
					statement.close();
				}
					break;
				case BEST_FINAL_SCORE: {
					final String querySelectPart = " SELECT player.name, player.display_name, YEAR(rcr_game_id.date), MONTH(rcr_game_id.date)-1, DAY(rcr_game_id.date), rcr_game_score.final_score, rcr_game_score.uma_score FROM player, rcr_game_id, rcr_game_score";
					final String queryWherePart = " WHERE player.id=rcr_game_score.player_id AND rcr_game_id.id=rcr_game_score.rcr_game_id AND rcr_game_id.rcr_tournament_id=?";
					final String queryRegularPart = onlyRegularPlayers ? " AND player.regular=TRUE" : "";
					final String queryPeriodPart = " AND rcr_game_id.date>=? AND rcr_game_id.date<?";
					final String queryOrderPart = sortingMode == EnumSortingMode.DESCENDING ? " ORDER BY rcr_game_score.final_score DESC"
						: " ORDER BY rcr_game_score.final_score ASC";
					final String queryFetchPart = " FETCH FIRST " + Integer.toString(NUMBER_TOP) + " ROWS ONLY";
					PreparedStatement statement = null;
					if (periodMode == EnumPeriodMode.ALL) {
						statement = dataBaseConnection.prepareStatement(querySelectPart + queryWherePart + queryRegularPart + queryOrderPart + queryFetchPart);
						statement.setShort(1, tournament.getId());
					} else {
						statement = dataBaseConnection
							.prepareStatement(querySelectPart + queryWherePart + queryRegularPart + queryPeriodPart + queryOrderPart + queryFetchPart);
						statement.setShort(1, tournament.getId());
						statement.setDate(2, new Date(calendarFrom.getTimeInMillis()));
						statement.setDate(3, new Date(calendarTo.getTimeInMillis()));
					}

					final ResultSet result = statement.executeQuery();
					while (result.next()) {
						final RCRTotalScore total = new RCRTotalScore(result.getString(1), result.getString(2), result.getInt(3), result.getInt(4),
							result.getInt(5));
						total.totalScore = new Integer(result.getInt(6));
						total.umaScore = new Integer(result.getInt(7));
						rankingScores.add(total);
					}
					result.close();
					statement.close();
				}
					break;
				case TOTAL_GAME_SCORE: {
					final String querySelectPart = "SELECT player.name, player.display_name, SUM(rcr_game_score.game_score) AS total, COUNT(*) AS nb_games FROM player, rcr_game_id, rcr_game_score";
					final String queryWherePart = " WHERE player.id=rcr_game_score.player_id AND rcr_game_id.id=rcr_game_score.rcr_game_id AND rcr_game_id.rcr_tournament_id=?";
					final String queryRegularPart = onlyRegularPlayers ? " AND player.regular=TRUE" : "";
					final String queryPeriodPart = " AND rcr_game_id.date>=? AND rcr_game_id.date<?";
					final String queryGroupPart = " GROUP BY player.name, player.display_name";
					final String queryHavingPart = useMinimumGame ? " HAVING COUNT(*)>=" + Integer.toString(minimumGames) : "";
					final String queryOrderPart = sortingMode == EnumSortingMode.DESCENDING ? " ORDER BY total DESC" : " ORDER BY total ASC";
					PreparedStatement statement = null;
					if (periodMode == EnumPeriodMode.ALL) {
						statement = dataBaseConnection
							.prepareStatement(querySelectPart + queryWherePart + queryRegularPart + queryGroupPart + queryHavingPart + queryOrderPart);
						statement.setShort(1, tournament.getId());
					} else {
						statement = dataBaseConnection.prepareStatement(
							querySelectPart + queryWherePart + queryRegularPart + queryPeriodPart + queryGroupPart + queryHavingPart + queryOrderPart);
						statement.setShort(1, tournament.getId());
						statement.setDate(2, new Date(calendarFrom.getTimeInMillis()));
						statement.setDate(3, new Date(calendarTo.getTimeInMillis()));
					}

					final ResultSet result = statement.executeQuery();
					while (result.next()) {
						final RCRTotalScore total = new RCRTotalScore(result.getString(1), result.getString(2), 0, 0, 0);
						total.totalScore = new Integer(result.getInt(3));
						total.numberOfGame = new Integer(result.getInt(4));
						rankingScores.add(total);
					}
					result.close();
					statement.close();
				}
					break;
				case MEAN_GAME_SCORE: {
					final String querySelectPart = " SELECT player.name, player.display_name, AVG(rcr_game_score.game_score) AS mean, STDDEV_POP(rcr_game_score.game_score) as stddev, COUNT(*) AS nb_games FROM player, rcr_game_id, rcr_game_score";
					final String queryWherePart = " WHERE player.id=rcr_game_score.player_id AND rcr_game_id.id=rcr_game_score.rcr_game_id AND rcr_game_id.rcr_tournament_id=?";
					final String queryRegularPart = onlyRegularPlayers ? " AND player.regular=TRUE" : "";
					final String queryPeriodPart = " AND rcr_game_id.date>=? AND rcr_game_id.date<?";
					final String queryGroupPart = " GROUP BY player.name, player.display_name";
					final String queryHavingPart = useMinimumGame ? " HAVING COUNT(*)>=" + Integer.toString(minimumGames) : "";
					final String queryOrderPart = sortingMode == EnumSortingMode.DESCENDING ? " ORDER BY mean DESC" : " ORDER BY mean ASC";
					PreparedStatement statement = null;
					if (periodMode == EnumPeriodMode.ALL) {
						statement = dataBaseConnection
							.prepareStatement(querySelectPart + queryWherePart + queryRegularPart + queryGroupPart + queryHavingPart + queryOrderPart);
						statement.setShort(1, tournament.getId());
					} else {
						statement = dataBaseConnection.prepareStatement(
							querySelectPart + queryWherePart + queryRegularPart + queryPeriodPart + queryGroupPart + queryHavingPart + queryOrderPart);
						statement.setShort(1, tournament.getId());
						statement.setDate(2, new Date(calendarFrom.getTimeInMillis()));
						statement.setDate(3, new Date(calendarTo.getTimeInMillis()));
					}

					final ResultSet result = statement.executeQuery();
					while (result.next()) {
						final RCRTotalScore total = new RCRTotalScore(result.getString(1), result.getString(2), 0, 0, 0);
						total.totalScore = result.getInt(3);
						total.umaScore = new Long(Math.round(result.getDouble(4)));
						total.numberOfGame = new Integer(result.getInt(5));
						rankingScores.add(total);
					}
					result.close();
					statement.close();
				}
					break;
				case BEST_GAME_SCORE: {
					final String querySelectPart = "SELECT player.name, player.display_name, YEAR(rcr_game_id.date), MONTH(rcr_game_id.date)-1, DAY(rcr_game_id.date), rcr_game_score.game_score FROM player, rcr_game_id, rcr_game_score";
					final String queryWherePart = " WHERE player.id=rcr_game_score.player_id AND rcr_game_id.id=rcr_game_score.rcr_game_id AND rcr_game_id.rcr_tournament_id=?";
					final String queryRegularPart = onlyRegularPlayers ? " AND player.regular=TRUE" : "";
					final String queryPeriodPart = " AND rcr_game_id.date>=? AND rcr_game_id.date<?";
					final String queryOrderPart = sortingMode == EnumSortingMode.DESCENDING ? " ORDER BY rcr_game_score.game_score DESC"
						: " ORDER BY rcr_game_score.game_score ASC";
					final String queryFetchPart = " FETCH FIRST " + Integer.toString(NUMBER_TOP) + " ROWS ONLY";
					PreparedStatement statement = null;
					if (periodMode == EnumPeriodMode.ALL) {
						statement = dataBaseConnection.prepareStatement(querySelectPart + queryWherePart + queryRegularPart + queryOrderPart + queryFetchPart);
						statement.setShort(1, tournament.getId());
					} else {
						statement = dataBaseConnection
							.prepareStatement(querySelectPart + queryWherePart + queryRegularPart + queryPeriodPart + queryOrderPart + queryFetchPart);
						statement.setShort(1, tournament.getId());
						statement.setDate(2, new Date(calendarFrom.getTimeInMillis()));
						statement.setDate(3, new Date(calendarTo.getTimeInMillis()));
					}

					final ResultSet result = statement.executeQuery();
					while (result.next()) {
						final RCRTotalScore total = new RCRTotalScore(result.getString(1), result.getString(2), result.getInt(3), result.getInt(4),
							result.getInt(5));
						total.totalScore = new Integer(result.getInt(6));
						rankingScores.add(total);
					}
					result.close();
					statement.close();
				}
					break;
				case WIN_RATE_4: {
					final Map<String, RCRTotalScore> mapNameScore = new HashMap<String, RCRTotalScore>();
					{
						final String querySelectPart = "SELECT player.name, player.display_name, COUNT(*) AS nb_games FROM player, rcr_game_id, rcr_game_score";
						final String queryWherePart = " WHERE player.id=rcr_game_score.player_id AND rcr_game_id.id=rcr_game_score.rcr_game_id AND rcr_game_id.rcr_tournament_id=? AND rcr_game_id.nb_players=4";
						final String queryRegularPart = onlyRegularPlayers ? " AND player.regular=TRUE" : "";
						final String queryPeriodPart = " AND rcr_game_id.date>=? AND rcr_game_id.date<?";
						final String queryGroupPart = " GROUP BY player.name, player.display_name";
						final String queryHavingPart = useMinimumGame ? " HAVING COUNT(*)>=" + Integer.toString(minimumGames) : "";
						PreparedStatement statement = null;
						if (periodMode == EnumPeriodMode.ALL) {
							statement = dataBaseConnection
								.prepareStatement(querySelectPart + queryWherePart + queryRegularPart + queryGroupPart + queryHavingPart);
							statement.setShort(1, tournament.getId());
						} else {
							statement = dataBaseConnection
								.prepareStatement(querySelectPart + queryWherePart + queryRegularPart + queryPeriodPart + queryGroupPart + queryHavingPart);
							statement.setShort(1, tournament.getId());
							statement.setDate(2, new Date(calendarFrom.getTimeInMillis()));
							statement.setDate(3, new Date(calendarTo.getTimeInMillis()));
						}

						final ResultSet result = statement.executeQuery();
						while (result.next()) {
							final RCRTotalScore total = new RCRTotalScore(result.getString(1), result.getString(2), 0, 0, 0);
							total.numberOfGame = new Integer(result.getInt(3));
							mapNameScore.put(total.displayName, total);
						}
						result.close();
						statement.close();
					}
					{
						final String querySelectPart = "SELECT player.display_name, COUNT(*) AS nb_games FROM player, rcr_game_id, rcr_game_score";
						final String queryWherePart = " WHERE player.id=rcr_game_score.player_id AND rcr_game_id.id=rcr_game_score.rcr_game_id AND rcr_game_id.rcr_tournament_id=? AND rcr_game_id.nb_players=4 AND rcr_game_score.ranking=1";
						final String queryRegularPart = onlyRegularPlayers ? " AND player.regular=TRUE" : "";
						final String queryPeriodPart = " AND rcr_game_id.date>=? AND rcr_game_id.date<?";
						final String queryGroupPart = " GROUP BY player.display_name";
						PreparedStatement statement = null;
						if (periodMode == EnumPeriodMode.ALL) {
							statement = dataBaseConnection.prepareStatement(querySelectPart + queryWherePart + queryRegularPart + queryGroupPart);
							statement.setShort(1, tournament.getId());
						} else {
							statement = dataBaseConnection
								.prepareStatement(querySelectPart + queryWherePart + queryRegularPart + queryPeriodPart + queryGroupPart);
							statement.setShort(1, tournament.getId());
							statement.setDate(2, new Date(calendarFrom.getTimeInMillis()));
							statement.setDate(3, new Date(calendarTo.getTimeInMillis()));
						}

						final ResultSet result = statement.executeQuery();
						while (result.next()) {
							final String name = result.getString(1);
							final RCRTotalScore total = mapNameScore.get(name);
							if (total != null) {
								total.umaScore = new Integer(result.getInt(2));
								total.totalScore = new Double(total.umaScore.doubleValue() * 100.0 / total.numberOfGame.doubleValue());
							}
						}
						result.close();
						statement.close();
					}
					rankingScores.addAll(mapNameScore.values());
					switch (sortingMode) {
						case DESCENDING:
							Collections.sort(rankingScores,
								(final RCRTotalScore o1, final RCRTotalScore o2) -> -Double.compare(o1.totalScore.doubleValue(), o2.totalScore.doubleValue()));
							break;
						case ASCENDING:
							Collections.sort(rankingScores,
								(final RCRTotalScore o1, final RCRTotalScore o2) -> Double.compare(o1.totalScore.doubleValue(), o2.totalScore.doubleValue()));
							break;
						default:
							break;
					}
				}
					break;
				case WIN_RATE_5: {
					final Map<String, RCRTotalScore> mapNameScore = new HashMap<String, RCRTotalScore>();
					{
						final String querySelectPart = "SELECT player.name, player.display_name, COUNT(*) AS nb_games FROM player, rcr_game_id, rcr_game_score";
						final String queryWherePart = " WHERE player.id=rcr_game_score.player_id AND rcr_game_id.id=rcr_game_score.rcr_game_id AND rcr_game_id.rcr_tournament_id=? AND rcr_game_id.nb_players=5";
						final String queryRegularPart = onlyRegularPlayers ? " AND player.regular=TRUE" : "";
						final String queryPeriodPart = " AND rcr_game_id.date>=? AND rcr_game_id.date<?";
						final String queryGroupPart = " GROUP BY player.name, player.display_name";
						final String queryHavingPart = useMinimumGame ? " HAVING COUNT(*)>=" + Integer.toString(minimumGames / 4) : "";
						PreparedStatement statement = null;
						if (periodMode == EnumPeriodMode.ALL) {
							statement = dataBaseConnection
								.prepareStatement(querySelectPart + queryWherePart + queryRegularPart + queryGroupPart + queryHavingPart);
							statement.setShort(1, tournament.getId());
						} else {
							statement = dataBaseConnection
								.prepareStatement(querySelectPart + queryWherePart + queryRegularPart + queryPeriodPart + queryGroupPart + queryHavingPart);
							statement.setShort(1, tournament.getId());
							statement.setDate(2, new Date(calendarFrom.getTimeInMillis()));
							statement.setDate(3, new Date(calendarTo.getTimeInMillis()));
						}

						final ResultSet result = statement.executeQuery();
						while (result.next()) {
							final RCRTotalScore total = new RCRTotalScore(result.getString(1), result.getString(2), 0, 0, 0);
							total.numberOfGame = new Integer(result.getInt(3));
							mapNameScore.put(total.displayName, total);
						}
						result.close();
						statement.close();
					}
					{
						final String querySelectPart = "SELECT player.display_name, COUNT(*) AS nb_games FROM player, rcr_game_id, rcr_game_score";
						final String queryWherePart = " WHERE player.id=rcr_game_score.player_id AND rcr_game_id.id=rcr_game_score.rcr_game_id AND rcr_game_id.rcr_tournament_id=? AND rcr_game_id.nb_players=5 AND rcr_game_score.ranking=1";
						final String queryRegularPart = onlyRegularPlayers ? " AND player.regular=TRUE" : "";
						final String queryPeriodPart = " AND rcr_game_id.date>=? AND rcr_game_id.date<?";
						final String queryGroupPart = " GROUP BY player.display_name";
						PreparedStatement statement = null;
						if (periodMode == EnumPeriodMode.ALL) {
							statement = dataBaseConnection.prepareStatement(querySelectPart + queryWherePart + queryRegularPart + queryGroupPart);
							statement.setShort(1, tournament.getId());
						} else {
							statement = dataBaseConnection
								.prepareStatement(querySelectPart + queryWherePart + queryRegularPart + queryPeriodPart + queryGroupPart);
							statement.setShort(1, tournament.getId());
							statement.setDate(2, new Date(calendarFrom.getTimeInMillis()));
							statement.setDate(3, new Date(calendarTo.getTimeInMillis()));
						}

						final ResultSet result = statement.executeQuery();
						while (result.next()) {
							final String name = result.getString(1);
							final RCRTotalScore total = mapNameScore.get(name);
							if (total != null) {
								total.umaScore = result.getInt(2);
								total.totalScore = new Double(total.umaScore.doubleValue() * 100.0 / total.numberOfGame.doubleValue());
							}
						}
						result.close();
						statement.close();
					}
					rankingScores.addAll(mapNameScore.values());
					switch (sortingMode) {
						case DESCENDING:
							Collections.sort(rankingScores,
								(final RCRTotalScore o1, final RCRTotalScore o2) -> -Double.compare(o1.totalScore.doubleValue(), o2.totalScore.doubleValue()));
							break;
						case ASCENDING:
							Collections.sort(rankingScores,
								(final RCRTotalScore o1, final RCRTotalScore o2) -> Double.compare(o1.totalScore.doubleValue(), o2.totalScore.doubleValue()));
							break;
						default:
							break;
					}
				}
					break;
				case POSITIVE_RATE_4: {
					final Map<String, RCRTotalScore> mapNameScore = new HashMap<String, RCRTotalScore>();
					{
						final String querySelectPart = "SELECT player.name, player.display_name, COUNT(*) AS nb_games FROM player, rcr_game_id, rcr_game_score";
						final String queryWherePart = " WHERE player.id=rcr_game_score.player_id AND rcr_game_id.id=rcr_game_score.rcr_game_id AND rcr_game_id.rcr_tournament_id=? AND rcr_game_id.nb_players=4";
						final String queryRegularPart = onlyRegularPlayers ? " AND player.regular=TRUE" : "";
						final String queryPeriodPart = " AND rcr_game_id.date>=? AND rcr_game_id.date<?";
						final String queryGroupPart = " GROUP BY player.name, player.display_name";
						final String queryHavingPart = useMinimumGame ? " HAVING COUNT(*)>=" + Integer.toString(minimumGames) : "";
						PreparedStatement statement = null;
						if (periodMode == EnumPeriodMode.ALL) {
							statement = dataBaseConnection
								.prepareStatement(querySelectPart + queryWherePart + queryRegularPart + queryGroupPart + queryHavingPart);
							statement.setShort(1, tournament.getId());
						} else {
							statement = dataBaseConnection
								.prepareStatement(querySelectPart + queryWherePart + queryRegularPart + queryPeriodPart + queryGroupPart + queryHavingPart);
							statement.setShort(1, tournament.getId());
							statement.setDate(2, new Date(calendarFrom.getTimeInMillis()));
							statement.setDate(3, new Date(calendarTo.getTimeInMillis()));
						}

						final ResultSet result = statement.executeQuery();
						while (result.next()) {
							final RCRTotalScore total = new RCRTotalScore(result.getString(1), result.getString(2), 0, 0, 0);
							total.numberOfGame = new Integer(result.getInt(3));
							mapNameScore.put(total.displayName, total);
						}
						result.close();
						statement.close();
					}
					{
						final String querySelectPart = "SELECT player.display_name, COUNT(*) AS nb_games FROM player, rcr_game_id, rcr_game_score";
						final String queryWherePart = " WHERE player.id=rcr_game_score.player_id AND rcr_game_id.id=rcr_game_score.rcr_game_id AND rcr_game_id.rcr_tournament_id=? AND rcr_game_id.nb_players=4 AND rcr_game_score.final_score>0";
						final String queryRegularPart = onlyRegularPlayers ? " AND player.regular=TRUE" : "";
						final String queryPeriodPart = " AND rcr_game_id.date>=? AND rcr_game_id.date<?";
						final String queryGroupPart = " GROUP BY player.display_name";
						PreparedStatement statement = null;
						if (periodMode == EnumPeriodMode.ALL) {
							statement = dataBaseConnection.prepareStatement(querySelectPart + queryWherePart + queryRegularPart + queryGroupPart);
							statement.setShort(1, tournament.getId());
						} else {
							statement = dataBaseConnection
								.prepareStatement(querySelectPart + queryWherePart + queryRegularPart + queryPeriodPart + queryGroupPart);
							statement.setShort(1, tournament.getId());
							statement.setDate(2, new Date(calendarFrom.getTimeInMillis()));
							statement.setDate(3, new Date(calendarTo.getTimeInMillis()));
						}

						final ResultSet result = statement.executeQuery();
						while (result.next()) {
							final String name = result.getString(1);
							final RCRTotalScore total = mapNameScore.get(name);
							if (total != null) {
								total.umaScore = result.getInt(2);
								total.totalScore = new Double(total.umaScore.doubleValue() * 100.0 / total.numberOfGame.doubleValue());
							}
						}
						result.close();
						statement.close();
					}
					rankingScores.addAll(mapNameScore.values());
					switch (sortingMode) {
						case DESCENDING:
							Collections.sort(rankingScores,
								(final RCRTotalScore o1, final RCRTotalScore o2) -> -Double.compare(o1.totalScore.doubleValue(), o2.totalScore.doubleValue()));
							break;
						case ASCENDING:
							Collections.sort(rankingScores,
								(final RCRTotalScore o1, final RCRTotalScore o2) -> Double.compare(o1.totalScore.doubleValue(), o2.totalScore.doubleValue()));
							break;
						default:
							break;
					}
				}
					break;
				case POSITIVE_RATE_5: {
					final Map<String, RCRTotalScore> mapNameScore = new HashMap<String, RCRTotalScore>();
					{
						final String querySelectPart = "SELECT player.name, player.display_name, COUNT(*) AS nb_games FROM player, rcr_game_id, rcr_game_score";
						final String queryWherePart = " WHERE player.id=rcr_game_score.player_id AND rcr_game_id.id=rcr_game_score.rcr_game_id AND rcr_game_id.rcr_tournament_id=? AND rcr_game_id.nb_players=5";
						final String queryRegularPart = onlyRegularPlayers ? " AND player.regular=TRUE" : "";
						final String queryPeriodPart = " AND rcr_game_id.date>=? AND rcr_game_id.date<?";
						final String queryGroupPart = " GROUP BY player.name, player.display_name";
						final String queryHavingPart = useMinimumGame ? " HAVING COUNT(*)>=" + Integer.toString(minimumGames / 4) : "";
						PreparedStatement statement = null;
						if (periodMode == EnumPeriodMode.ALL) {
							statement = dataBaseConnection
								.prepareStatement(querySelectPart + queryWherePart + queryRegularPart + queryGroupPart + queryHavingPart);
							statement.setShort(1, tournament.getId());
						} else {
							statement = dataBaseConnection
								.prepareStatement(querySelectPart + queryWherePart + queryRegularPart + queryPeriodPart + queryGroupPart + queryHavingPart);
							statement.setShort(1, tournament.getId());
							statement.setDate(2, new Date(calendarFrom.getTimeInMillis()));
							statement.setDate(3, new Date(calendarTo.getTimeInMillis()));
						}

						final ResultSet result = statement.executeQuery();
						while (result.next()) {
							final RCRTotalScore total = new RCRTotalScore(result.getString(1), result.getString(2), 0, 0, 0);
							total.numberOfGame = new Integer(result.getInt(3));
							mapNameScore.put(total.displayName, total);
						}
						result.close();
						statement.close();
					}
					{
						final String querySelectPart = "SELECT player.display_name, COUNT(*) AS nb_games FROM player, rcr_game_id, rcr_game_score";
						final String queryWherePart = " WHERE player.id=rcr_game_score.player_id AND rcr_game_id.id=rcr_game_score.rcr_game_id AND rcr_game_id.rcr_tournament_id=? AND rcr_game_id.nb_players=5 AND rcr_game_score.final_score>0";
						final String queryRegularPart = onlyRegularPlayers ? " AND player.regular=TRUE" : "";
						final String queryPeriodPart = " AND rcr_game_id.date>=? AND rcr_game_id.date<?";
						final String queryGroupPart = " GROUP BY player.display_name";
						PreparedStatement statement = null;
						if (periodMode == EnumPeriodMode.ALL) {
							statement = dataBaseConnection.prepareStatement(querySelectPart + queryWherePart + queryRegularPart + queryGroupPart);
							statement.setShort(1, tournament.getId());
						} else {
							statement = dataBaseConnection
								.prepareStatement(querySelectPart + queryWherePart + queryRegularPart + queryPeriodPart + queryGroupPart);
							statement.setShort(1, tournament.getId());
							statement.setDate(2, new Date(calendarFrom.getTimeInMillis()));
							statement.setDate(3, new Date(calendarTo.getTimeInMillis()));
						}

						final ResultSet result = statement.executeQuery();
						while (result.next()) {
							final String name = result.getString(1);
							final RCRTotalScore total = mapNameScore.get(name);
							if (total != null) {
								total.umaScore = result.getInt(2);
								total.totalScore = new Double(total.umaScore.doubleValue() * 100.0 / total.numberOfGame.doubleValue());
							}
						}
						result.close();
						statement.close();
					}
					rankingScores.addAll(mapNameScore.values());
					switch (sortingMode) {
						case DESCENDING:
							Collections.sort(rankingScores,
								(final RCRTotalScore o1, final RCRTotalScore o2) -> -Double.compare(o1.totalScore.doubleValue(), o2.totalScore.doubleValue()));
							break;
						case ASCENDING:
							Collections.sort(rankingScores,
								(final RCRTotalScore o1, final RCRTotalScore o2) -> Double.compare(o1.totalScore.doubleValue(), o2.totalScore.doubleValue()));
							break;
						default:
							break;
					}
				}
					break;
				case ANNUAL_SCORE: {
					final String querySelectPart = "SELECT name, display_name, y, SUM(final_score) AS total, COUNT(*) AS nb_games FROM";
					final String querySubSelectPart = " (SELECT player.name, player.display_name, YEAR(rcr_game_id.date) as y, rcr_game_score.final_score FROM player, rcr_game_id, rcr_game_score";
					final String querySubWherePart = " WHERE player.id=rcr_game_score.player_id AND rcr_game_id.id=rcr_game_score.rcr_game_id AND rcr_game_id.rcr_tournament_id=?";
					final String querySubRegularPart = onlyRegularPlayers ? " AND player.regular=TRUE" : "";
					final String querySubFinalPart = ") AS year_score";
					final String queryGroupPart = " GROUP BY name, display_name, y";
					final String queryHavingPart = " HAVING COUNT(*)>=" + Integer.toString(MINIMUM_GAME_YEAR);
					final String queryOrderPart = sortingMode == EnumSortingMode.DESCENDING ? " ORDER BY total DESC" : " ORDER BY total ASC";
					final String queryFetchPart = " FETCH FIRST " + Integer.toString(NUMBER_TOP) + " ROWS ONLY";
					final PreparedStatement statement = dataBaseConnection.prepareStatement(querySelectPart + querySubSelectPart + querySubWherePart
						+ querySubRegularPart + querySubFinalPart + queryGroupPart + queryHavingPart + queryOrderPart + queryFetchPart);
					statement.setShort(1, tournament.getId());

					final ResultSet result = statement.executeQuery();
					while (result.next()) {
						final RCRTotalScore total = new RCRTotalScore(result.getString(1), result.getString(2), result.getInt(3), 0, 0);
						total.totalScore = new Integer(result.getInt(4));
						total.numberOfGame = new Integer(result.getInt(5));
						rankingScores.add(total);
					}
					result.close();
					statement.close();
				}
					break;
				case TRIMESTRIAL_SCORE: {
					final String querySelectPart = "SELECT name, display_name, y, t, SUM(final_score) AS total, COUNT(*) AS nb_games FROM";
					final String querySubSelectPart = " (SELECT player.name, player.display_name, YEAR(rcr_game_id.date) as y, (MONTH(rcr_game_id.date)-1)/3 as t, rcr_game_score.final_score FROM player, rcr_game_id, rcr_game_score";
					final String querySubWherePart = " WHERE player.id=rcr_game_score.player_id AND rcr_game_id.id=rcr_game_score.rcr_game_id AND rcr_game_id.rcr_tournament_id=?";
					final String querySubRegularPart = onlyRegularPlayers ? " AND player.regular=TRUE" : "";
					final String querySubFinalPart = ") AS year_score";
					final String queryGroupPart = " GROUP BY name, display_name, y, t";
					final String queryHavingPart = " HAVING COUNT(*)>=" + Integer.toString(MINIMUM_GAME_TRIMESTER);
					final String queryOrderPart = sortingMode == EnumSortingMode.DESCENDING ? " ORDER BY total DESC" : " ORDER BY total ASC";
					final String queryFetchPart = " FETCH FIRST " + Integer.toString(NUMBER_TOP) + " ROWS ONLY";
					final PreparedStatement statement = dataBaseConnection.prepareStatement(querySelectPart + querySubSelectPart + querySubWherePart
						+ querySubRegularPart + querySubFinalPart + queryGroupPart + queryHavingPart + queryOrderPart + queryFetchPart);
					statement.setShort(1, tournament.getId());

					final ResultSet result = statement.executeQuery();
					while (result.next()) {
						final RCRTotalScore total = new RCRTotalScore(result.getString(1), result.getString(2), result.getInt(3), result.getInt(4), 0);
						total.totalScore = new Integer(result.getInt(5));
						total.numberOfGame = new Integer(result.getInt(6));
						rankingScores.add(total);
					}
					result.close();
					statement.close();
				}
					break;
				case MENSUAL_SCORE: {
					final String querySelectPart = "SELECT name, display_name, y, m, SUM(final_score) AS total, COUNT(*) AS nb_games FROM";
					final String querySubSelectPart = " (SELECT player.name, player.display_name, YEAR(rcr_game_id.date) as y, MONTH(rcr_game_id.date)-1 as m, rcr_game_score.final_score FROM player, rcr_game_id, rcr_game_score";
					final String querySubWherePart = " WHERE player.id=rcr_game_score.player_id AND rcr_game_id.id=rcr_game_score.rcr_game_id AND rcr_game_id.rcr_tournament_id=?";
					final String querySubRegularPart = onlyRegularPlayers ? " AND player.regular=TRUE" : "";
					final String querySubFinalPart = ") AS year_score";
					final String queryGroupPart = " GROUP BY name, display_name, y, m";
					final String queryHavingPart = " HAVING COUNT(*)>=" + Integer.toString(MINIMUM_GAME_MONTH);
					final String queryOrderPart = sortingMode == EnumSortingMode.DESCENDING ? " ORDER BY total DESC" : " ORDER BY total ASC";
					final String queryFetchPart = " FETCH FIRST " + Integer.toString(NUMBER_TOP) + " ROWS ONLY";
					final PreparedStatement statement = dataBaseConnection.prepareStatement(querySelectPart + querySubSelectPart + querySubWherePart
						+ querySubRegularPart + querySubFinalPart + queryGroupPart + queryHavingPart + queryOrderPart + queryFetchPart);
					statement.setShort(1, tournament.getId());

					final ResultSet result = statement.executeQuery();
					while (result.next()) {
						final RCRTotalScore total = new RCRTotalScore(result.getString(1), result.getString(2), result.getInt(3), result.getInt(4), 0);
						total.totalScore = new Integer(result.getInt(5));
						total.numberOfGame = new Integer(result.getInt(6));
						rankingScores.add(total);
					}
					result.close();
					statement.close();
				}
					break;
				default:
					break;
			}
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return rankingScores;
	}

	@Override
	public RCRDataPackageTrend getRCRDataPackageTrend(final Tournament tournament, final EnumPeriodMode periodMode, final int year, final int trimester,
		final int month, final int day) {
		final Calendar calendarFrom = Calendar.getInstance();
		final Calendar calendarTo = Calendar.getInstance();
		switch (periodMode) {
			case ALL:
				break;
			case YEAR:
				calendarFrom.set(Calendar.YEAR, year);
				calendarFrom.set(Calendar.MONTH, Calendar.JANUARY);
				calendarFrom.set(Calendar.DAY_OF_MONTH, 1);
				calendarTo.setTime(calendarFrom.getTime());
				calendarTo.add(Calendar.YEAR, 1);
				break;
			case TRIMESTER:
				calendarFrom.set(Calendar.YEAR, year);
				calendarFrom.set(Calendar.MONTH, trimester * 3);
				calendarFrom.set(Calendar.DAY_OF_MONTH, 1);
				calendarTo.setTime(calendarFrom.getTime());
				calendarTo.add(Calendar.MONTH, 3);
				break;
			case MONTH:
				calendarFrom.set(Calendar.YEAR, year);
				calendarFrom.set(Calendar.MONTH, month);
				calendarFrom.set(Calendar.DAY_OF_MONTH, 1);
				calendarTo.setTime(calendarFrom.getTime());
				calendarTo.add(Calendar.MONTH, 1);
				break;
			case DAY:
				calendarFrom.set(Calendar.YEAR, year);
				calendarFrom.set(Calendar.MONTH, month);
				calendarFrom.set(Calendar.DAY_OF_MONTH, 1);
				calendarTo.setTime(calendarFrom.getTime());
				calendarTo.add(Calendar.DAY_OF_MONTH, 1);
				break;
			default:
				break;
		}

		try {
			final List<Long> dates = new ArrayList<Long>();
			final SortedMap<String, List<Integer>> dataWithPlayerName = new TreeMap<String, List<Integer>>();
			final SortedMap<String, List<Integer>> dataWithDisplayName = new TreeMap<String, List<Integer>>();
			{
				final String querySelectPart = "SELECT DISTINCT player.name, player.display_name FROM player, rcr_game_id, rcr_game_score";
				final String queryWherePart = " WHERE player.id=rcr_game_score.player_id AND rcr_game_id.id=rcr_game_score.rcr_game_id AND rcr_game_id.rcr_tournament_id=?";
				final String queryPeriodPart = " AND rcr_game_id.date>=? AND rcr_game_id.date<?";
				PreparedStatement statement = null;
				if (periodMode == EnumPeriodMode.ALL) {
					statement = dataBaseConnection.prepareStatement(querySelectPart + queryWherePart);
					statement.setShort(1, tournament.getId());
				} else {
					statement = dataBaseConnection.prepareStatement(querySelectPart + queryWherePart + queryPeriodPart);
					statement.setShort(1, tournament.getId());
					statement.setDate(2, new Date(calendarFrom.getTimeInMillis()));
					statement.setDate(3, new Date(calendarTo.getTimeInMillis()));
				}
				final ResultSet result = statement.executeQuery();
				while (result.next()) {
					final List<Integer> scoreListPlayerNames = new ArrayList<Integer>();
					scoreListPlayerNames.add(0);
					dataWithPlayerName.put(result.getString(1), scoreListPlayerNames);

					final List<Integer> scoreListDisplayNames = new ArrayList<Integer>();
					scoreListDisplayNames.add(0);
					dataWithDisplayName.put(result.getString(2), scoreListDisplayNames);
				}
				result.close();
				statement.close();
			}

			{
				final String querySelectPart = "SELECT rcr_game_id.date, player.name, player.display_name, SUM(rcr_game_score.final_score) FROM rcr_game_id, rcr_game_score, player";
				final String queryWherePart = " WHERE rcr_game_id.rcr_tournament_id=? AND rcr_game_id.id=rcr_game_score.rcr_game_id AND player.id=rcr_game_score.player_id";
				final String queryPeriodPart = " AND rcr_game_id.date>=? AND rcr_game_id.date<?";
				final String queryGroupPart = " GROUP BY rcr_game_id.date, player.name, player.display_name";
				final String queryOrderPart = " ORDER BY rcr_game_id.date";
				PreparedStatement statement = null;
				if (periodMode == EnumPeriodMode.ALL) {
					statement = dataBaseConnection.prepareStatement(querySelectPart + queryWherePart + queryGroupPart + queryOrderPart);
					statement.setShort(1, tournament.getId());
				} else {
					statement = dataBaseConnection.prepareStatement(querySelectPart + queryWherePart + queryPeriodPart + queryGroupPart + queryOrderPart);
					statement.setShort(1, tournament.getId());
					statement.setDate(2, new Date(calendarFrom.getTimeInMillis()));
					statement.setDate(3, new Date(calendarTo.getTimeInMillis()));
				}

				int dateIndex = 0;
				long lastDate = 0;
				long date;
				dates.add(dateIndex, lastDate);
				final ResultSet result = statement.executeQuery();
				while (result.next()) {
					date = result.getDate(1).getTime();
					if (date != lastDate) {
						for (final String playerName : dataWithPlayerName.keySet()) {
							final List<Integer> playerScore = dataWithPlayerName.get(playerName);
							playerScore.add(playerScore.get(dateIndex));
						}
						for (final String playerName : dataWithDisplayName.keySet()) {
							final List<Integer> playerScore = dataWithDisplayName.get(playerName);
							playerScore.add(playerScore.get(dateIndex));
						}
						lastDate = date;
						dateIndex++;
						dates.add(dateIndex, lastDate);
					}
					final int score = result.getInt(4);
					final List<Integer> scoreListPlayerNames = dataWithPlayerName.get(result.getString(2));
					scoreListPlayerNames.set(dateIndex, scoreListPlayerNames.get(dateIndex) + score);
					final List<Integer> scoreListDisplayNames = dataWithDisplayName.get(result.getString(3));
					scoreListDisplayNames.set(dateIndex, scoreListDisplayNames.get(dateIndex) + score);
				}
			}
			return new RCRDataPackageTrend(dates, dataWithPlayerName, dataWithDisplayName);
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public RCRDataPackageScoreAnalyze getRCRDataPackageScoreAnalyze(final Tournament tournament, final EnumPeriodMode periodMode, final int year,
		final int trimester, final int month, final int day) {
		if (isConnected()) {
			final Calendar calendarFrom = Calendar.getInstance();
			final Calendar calendarTo = Calendar.getInstance();
			switch (periodMode) {
				case ALL:
					break;
				case YEAR:
					calendarFrom.set(Calendar.YEAR, year);
					calendarFrom.set(Calendar.MONTH, Calendar.JANUARY);
					calendarFrom.set(Calendar.DAY_OF_MONTH, 1);
					calendarTo.setTime(calendarFrom.getTime());
					calendarTo.add(Calendar.YEAR, 1);
					break;
				case TRIMESTER:
					calendarFrom.set(Calendar.YEAR, year);
					calendarFrom.set(Calendar.MONTH, trimester * 3);
					calendarFrom.set(Calendar.DAY_OF_MONTH, 1);
					calendarTo.setTime(calendarFrom.getTime());
					calendarTo.add(Calendar.MONTH, 3);
					break;
				case MONTH:
					calendarFrom.set(Calendar.YEAR, year);
					calendarFrom.set(Calendar.MONTH, month);
					calendarFrom.set(Calendar.DAY_OF_MONTH, 1);
					calendarTo.setTime(calendarFrom.getTime());
					calendarTo.add(Calendar.MONTH, 1);
					break;
				case DAY:
					calendarFrom.set(Calendar.YEAR, year);
					calendarFrom.set(Calendar.MONTH, month);
					calendarFrom.set(Calendar.DAY_OF_MONTH, day);
					calendarTo.setTime(calendarFrom.getTime());
					calendarTo.add(Calendar.DAY_OF_MONTH, 1);
					break;
				default:
					break;
			}

			try {
				final List<Short> playerIDs = new ArrayList<Short>();
				final List<String> playerNames = new ArrayList<String>();
				final List<String> displayNames = new ArrayList<String>();
				final Map<Short, Integer> mapId2Index = new HashMap<Short, Integer>();
				{
					final String querySelectPart = "SELECT DISTINCT player.id, player.name, player.display_name FROM player, rcr_game_id, rcr_game_score";
					final String queryWherePart = " WHERE player.id=rcr_game_score.player_id AND rcr_game_id.id=rcr_game_score.rcr_game_id AND rcr_game_id.rcr_tournament_id=?";
					final String queryPeriodPart = " AND rcr_game_id.date>=? AND rcr_game_id.date<?";
					final String queryOrderPart = " ORDER BY player.id";
					PreparedStatement statement = null;
					if (periodMode == EnumPeriodMode.ALL) {
						statement = dataBaseConnection.prepareStatement(querySelectPart + queryWherePart + queryOrderPart);
						statement.setShort(1, tournament.getId());
					} else {
						statement = dataBaseConnection.prepareStatement(querySelectPart + queryWherePart + queryPeriodPart + queryOrderPart);
						statement.setShort(1, tournament.getId());
						statement.setDate(2, new Date(calendarFrom.getTimeInMillis()));
						statement.setDate(3, new Date(calendarTo.getTimeInMillis()));
					}
					final ResultSet result = statement.executeQuery();
					int index = 0;
					while (result.next()) {
						final short id = result.getShort(1);
						playerIDs.add(index, id);
						playerNames.add(index, result.getString(2));
						displayNames.add(index, result.getString(3));
						mapId2Index.put(id, index);
						index++;
					}
					result.close();
					statement.close();
				}

				final List<Integer> gameIDs = new ArrayList<Integer>();
				{
					final String querySelectPart = "SELECT rcr_game_id.id FROM rcr_game_id";
					final String queryWherePart = " WHERE rcr_game_id.rcr_tournament_id=?";
					final String queryPeriodPart = " AND rcr_game_id.date>=? AND rcr_game_id.date<?";
					final String queryOrderPart = " ORDER BY rcr_game_id.id";
					PreparedStatement statement = null;
					if (periodMode == EnumPeriodMode.ALL) {
						statement = dataBaseConnection.prepareStatement(querySelectPart + queryWherePart + queryOrderPart);
						statement.setShort(1, tournament.getId());
					} else {
						statement = dataBaseConnection.prepareStatement(querySelectPart + queryWherePart + queryPeriodPart + queryOrderPart);
						statement.setShort(1, tournament.getId());
						statement.setDate(2, new Date(calendarFrom.getTimeInMillis()));
						statement.setDate(3, new Date(calendarTo.getTimeInMillis()));
					}
					final ResultSet result = statement.executeQuery();
					while (result.next()) {
						gameIDs.add(result.getInt(1));
					}
					result.close();
					statement.close();
				}

				final double[][] scores = new double[playerIDs.size()][playerIDs.size()];
				final double[] sums = new double[playerIDs.size()];
				{
					final short[] playerIDGame = new short[5];
					final long[] playerScoreGame = new long[5];
					int scoreIndex;
					int nbPositives;
					double totalPositive;

					final String querySelectPart = "SELECT rcr_game_score.player_id, rcr_game_score.game_score FROM rcr_game_score";
					final String queryWherePart = " WHERE rcr_game_score.rcr_game_id=?";
					final String queryOrderPart = " ORDER BY rcr_game_score.game_score DESC";
					final PreparedStatement statement = dataBaseConnection.prepareStatement(querySelectPart + queryWherePart + queryOrderPart);
					for (int idIndex = 0; idIndex < gameIDs.size(); idIndex++) {
						statement.setInt(1, gameIDs.get(idIndex));
						final ResultSet result = statement.executeQuery();
						int nbPlayers = 0;
						while (result.next()) {
							playerIDGame[nbPlayers] = result.getShort(1);
							playerScoreGame[nbPlayers] = result.getLong(2);
							nbPlayers++;
						}
						result.close();
						statement.clearParameters();

						scoreIndex = 0;
						totalPositive = 0;
						while (scoreIndex < nbPlayers && playerScoreGame[scoreIndex] > 0) {
							totalPositive += playerScoreGame[scoreIndex];
							scoreIndex++;
						}
						if (totalPositive != 0) {
							nbPositives = scoreIndex;
							while (scoreIndex < nbPlayers && playerScoreGame[scoreIndex] == 0) {
								scoreIndex++;
							}
							while (scoreIndex < nbPlayers) {
								final int playerNegativeIndex = mapId2Index.get(playerIDGame[scoreIndex]);
								for (int positiveIndex = 0; positiveIndex < nbPositives; positiveIndex++) {
									final double scorePart = -playerScoreGame[scoreIndex] * playerScoreGame[positiveIndex] / totalPositive;
									final int playerPositiveIndex = mapId2Index.get(playerIDGame[positiveIndex]);
									scores[playerPositiveIndex][playerNegativeIndex] += scorePart;
									scores[playerNegativeIndex][playerPositiveIndex] -= scorePart;
									sums[playerPositiveIndex] += scorePart;
									sums[playerNegativeIndex] -= scorePart;
								}
								scoreIndex++;
							}
						}
					}
					statement.close();
				}
				return new RCRDataPackageScoreAnalyze(playerNames, displayNames, scores, sums);
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

}
