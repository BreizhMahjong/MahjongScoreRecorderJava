package fr.bmj.bmjc.dataaccess.impl.db.rcr;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.bmj.bmjc.data.game.Tournament;
import fr.bmj.bmjc.data.stat.rcr.RCRDataPackageScoreAnalyze;
import fr.bmj.bmjc.dataaccess.abs.rcr.DataAccessRCRScoreAnalyze;
import fr.bmj.bmjc.enums.EnumPeriodMode;

public class DataAccessDataBaseRCRScoreAnalyze extends DataAccessDataBaseRCRCommon implements DataAccessRCRScoreAnalyze {

	public DataAccessDataBaseRCRScoreAnalyze(final Connection dataBaseConnection) {
		super(
			dataBaseConnection);
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
					calendarFrom.set(
						Calendar.YEAR,
						year);
					calendarFrom.set(
						Calendar.MONTH,
						Calendar.JANUARY);
					calendarFrom.set(
						Calendar.DAY_OF_MONTH,
						1);
					calendarTo.setTime(
						calendarFrom.getTime());
					calendarTo.add(
						Calendar.YEAR,
						1);
					break;
				case TRIMESTER:
					calendarFrom.set(
						Calendar.YEAR,
						year);
					calendarFrom.set(
						Calendar.MONTH,
						trimester * 3);
					calendarFrom.set(
						Calendar.DAY_OF_MONTH,
						1);
					calendarTo.setTime(
						calendarFrom.getTime());
					calendarTo.add(
						Calendar.MONTH,
						3);
					break;
				case MONTH:
					calendarFrom.set(
						Calendar.YEAR,
						year);
					calendarFrom.set(
						Calendar.MONTH,
						month);
					calendarFrom.set(
						Calendar.DAY_OF_MONTH,
						1);
					calendarTo.setTime(
						calendarFrom.getTime());
					calendarTo.add(
						Calendar.MONTH,
						1);
					break;
				case DAY:
					calendarFrom.set(
						Calendar.YEAR,
						year);
					calendarFrom.set(
						Calendar.MONTH,
						month);
					calendarFrom.set(
						Calendar.DAY_OF_MONTH,
						day);
					calendarTo.setTime(
						calendarFrom.getTime());
					calendarTo.add(
						Calendar.DAY_OF_MONTH,
						1);
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
					final String queryWherePart =
						" WHERE player.id=rcr_game_score.player_id AND rcr_game_id.id=rcr_game_score.rcr_game_id AND rcr_game_id.rcr_tournament_id=?";
					final String queryPeriodPart = " AND rcr_game_id.date>=? AND rcr_game_id.date<?";
					final String queryOrderPart = " ORDER BY player.id";
					PreparedStatement statement = null;
					if (periodMode == EnumPeriodMode.ALL) {
						statement = dataBaseConnection.prepareStatement(
							querySelectPart + queryWherePart + queryOrderPart);
						statement.setShort(
							1,
							tournament.getId());
					} else {
						statement = dataBaseConnection.prepareStatement(
							querySelectPart + queryWherePart + queryPeriodPart + queryOrderPart);
						statement.setShort(
							1,
							tournament.getId());
						statement.setDate(
							2,
							new Date(
								calendarFrom.getTimeInMillis()));
						statement.setDate(
							3,
							new Date(
								calendarTo.getTimeInMillis()));
					}
					final ResultSet result = statement.executeQuery();
					int index = 0;
					while (result.next()) {
						final short id = result.getShort(
							1);
						playerIDs.add(
							index,
							id);
						playerNames.add(
							index,
							result.getString(
								2));
						displayNames.add(
							index,
							result.getString(
								3));
						mapId2Index.put(
							id,
							index);
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
						statement = dataBaseConnection.prepareStatement(
							querySelectPart + queryWherePart + queryOrderPart);
						statement.setShort(
							1,
							tournament.getId());
					} else {
						statement = dataBaseConnection.prepareStatement(
							querySelectPart + queryWherePart + queryPeriodPart + queryOrderPart);
						statement.setShort(
							1,
							tournament.getId());
						statement.setDate(
							2,
							new Date(
								calendarFrom.getTimeInMillis()));
						statement.setDate(
							3,
							new Date(
								calendarTo.getTimeInMillis()));
					}
					final ResultSet result = statement.executeQuery();
					while (result.next()) {
						gameIDs.add(
							result.getInt(
								1));
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
					final PreparedStatement statement = dataBaseConnection.prepareStatement(
						querySelectPart + queryWherePart + queryOrderPart);
					for (int idIndex = 0; idIndex < gameIDs.size(); idIndex++) {
						statement.setInt(
							1,
							gameIDs.get(
								idIndex));
						final ResultSet result = statement.executeQuery();
						int nbPlayers = 0;
						while (result.next()) {
							playerIDGame[nbPlayers] = result.getShort(
								1);
							playerScoreGame[nbPlayers] = result.getLong(
								2);
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
								final int playerNegativeIndex = mapId2Index.get(
									playerIDGame[scoreIndex]);
								for (int positiveIndex = 0; positiveIndex < nbPositives; positiveIndex++) {
									final double scorePart = -playerScoreGame[scoreIndex] * playerScoreGame[positiveIndex] / totalPositive;
									final int playerPositiveIndex = mapId2Index.get(
										playerIDGame[positiveIndex]);
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
				return new RCRDataPackageScoreAnalyze(
					playerNames,
					displayNames,
					scores,
					sums);
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

}
