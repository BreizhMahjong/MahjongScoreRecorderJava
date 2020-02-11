package fr.bmj.bmjc.dataaccess.impl.db.rcr;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.bmj.bmjc.data.game.Tournament;
import fr.bmj.bmjc.data.stat.rcr.RCRTotalScore;
import fr.bmj.bmjc.dataaccess.abs.rcr.DataAccessRCRRanking;
import fr.bmj.bmjc.enums.EnumPeriodMode;
import fr.bmj.bmjc.enums.EnumRankingMode;
import fr.bmj.bmjc.enums.EnumSortingMode;

public class DataAccessDataBaseRCRRanking extends DataAccessDataBaseRCRCommon implements DataAccessRCRRanking {

	private static final int NUMBER_TOP = 32;
	private static final int MINIMUM_GAME_MONTH = 4;
	private static final int MINIMUM_GAME_TRIMESTER = 8;
	private static final int MINIMUM_GAME_YEAR = 32;

	private boolean useMinimumGame;
	private boolean onlyRegularPlayers;

	public DataAccessDataBaseRCRRanking(final Connection dataBaseConnection) {
		super(dataBaseConnection);
	}

	@Override
	public void setUseMinimumGame(final boolean useMinimumGame) {
		this.useMinimumGame = useMinimumGame;
	}

	@Override
	public void setOnlyRegularPlayers(final boolean onlyRegularPlayers) {
		this.onlyRegularPlayers = onlyRegularPlayers;
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
				case ANNUAL_TOTAL_FINAL_SCORE: {
					final String querySelectPart = "SELECT name, display_name, y, SUM(final_score) AS total, COUNT(*) AS nb_games FROM";
					final String querySubSelectPart = " (SELECT player.name, player.display_name, YEAR(rcr_game_id.date) as y, rcr_game_score.final_score FROM player, rcr_game_id, rcr_game_score";
					final String querySubWherePart = " WHERE player.id=rcr_game_score.player_id AND rcr_game_id.id=rcr_game_score.rcr_game_id AND rcr_game_id.rcr_tournament_id=?";
					final String querySubRegularPart = onlyRegularPlayers ? " AND player.regular=TRUE" : "";
					final String querySubFinalPart = ") AS year_score";
					final String queryGroupPart = " GROUP BY name, display_name, y";
					final String queryHavingPart = useMinimumGame ? " HAVING COUNT(*)>=" + Integer.toString(MINIMUM_GAME_YEAR) : "";
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
				case TRIMESTRIAL_TOTAL_FINAL_SCORE: {
					final String querySelectPart = "SELECT name, display_name, y, t, SUM(final_score) AS total, COUNT(*) AS nb_games FROM";
					final String querySubSelectPart = " (SELECT player.name, player.display_name, YEAR(rcr_game_id.date) as y, (MONTH(rcr_game_id.date)-1)/3 as t, rcr_game_score.final_score FROM player, rcr_game_id, rcr_game_score";
					final String querySubWherePart = " WHERE player.id=rcr_game_score.player_id AND rcr_game_id.id=rcr_game_score.rcr_game_id AND rcr_game_id.rcr_tournament_id=?";
					final String querySubRegularPart = onlyRegularPlayers ? " AND player.regular=TRUE" : "";
					final String querySubFinalPart = ") AS year_score";
					final String queryGroupPart = " GROUP BY name, display_name, y, t";
					final String queryHavingPart = useMinimumGame ? " HAVING COUNT(*)>=" + Integer.toString(MINIMUM_GAME_TRIMESTER) : "";
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
				case MENSUAL_TOTAL_FINAL_SCORE: {
					final String querySelectPart = "SELECT name, display_name, y, m, SUM(final_score) AS total, COUNT(*) AS nb_games FROM";
					final String querySubSelectPart = " (SELECT player.name, player.display_name, YEAR(rcr_game_id.date) as y, MONTH(rcr_game_id.date)-1 as m, rcr_game_score.final_score FROM player, rcr_game_id, rcr_game_score";
					final String querySubWherePart = " WHERE player.id=rcr_game_score.player_id AND rcr_game_id.id=rcr_game_score.rcr_game_id AND rcr_game_id.rcr_tournament_id=?";
					final String querySubRegularPart = onlyRegularPlayers ? " AND player.regular=TRUE" : "";
					final String querySubFinalPart = ") AS year_score";
					final String queryGroupPart = " GROUP BY name, display_name, y, m";
					final String queryHavingPart = useMinimumGame ? " HAVING COUNT(*)>=" + Integer.toString(MINIMUM_GAME_MONTH) : "";
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
				case ANNUAL_TOTAL_GAME_SCORE: {
					final String querySelectPart = "SELECT name, display_name, y, SUM(game_score) AS total, COUNT(*) AS nb_games FROM";
					final String querySubSelectPart = " (SELECT player.name, player.display_name, YEAR(rcr_game_id.date) as y, rcr_game_score.game_score FROM player, rcr_game_id, rcr_game_score";
					final String querySubWherePart = " WHERE player.id=rcr_game_score.player_id AND rcr_game_id.id=rcr_game_score.rcr_game_id AND rcr_game_id.rcr_tournament_id=?";
					final String querySubRegularPart = onlyRegularPlayers ? " AND player.regular=TRUE" : "";
					final String querySubFinalPart = ") AS year_score";
					final String queryGroupPart = " GROUP BY name, display_name, y";
					final String queryHavingPart = useMinimumGame ? " HAVING COUNT(*)>=" + Integer.toString(MINIMUM_GAME_YEAR) : "";
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
				case TRIMESTRIAL_TOTAL_GAME_SCORE: {
					final String querySelectPart = "SELECT name, display_name, y, t, SUM(game_score) AS total, COUNT(*) AS nb_games FROM";
					final String querySubSelectPart = " (SELECT player.name, player.display_name, YEAR(rcr_game_id.date) as y, (MONTH(rcr_game_id.date)-1)/3 as t, rcr_game_score.game_score FROM player, rcr_game_id, rcr_game_score";
					final String querySubWherePart = " WHERE player.id=rcr_game_score.player_id AND rcr_game_id.id=rcr_game_score.rcr_game_id AND rcr_game_id.rcr_tournament_id=?";
					final String querySubRegularPart = onlyRegularPlayers ? " AND player.regular=TRUE" : "";
					final String querySubFinalPart = ") AS year_score";
					final String queryGroupPart = " GROUP BY name, display_name, y, t";
					final String queryHavingPart = useMinimumGame ? " HAVING COUNT(*)>=" + Integer.toString(MINIMUM_GAME_TRIMESTER) : "";
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
				case MENSUAL_TOTAL_GAME_SCORE: {
					final String querySelectPart = "SELECT name, display_name, y, m, SUM(game_score) AS total, COUNT(*) AS nb_games FROM";
					final String querySubSelectPart = " (SELECT player.name, player.display_name, YEAR(rcr_game_id.date) as y, MONTH(rcr_game_id.date)-1 as m, rcr_game_score.game_score FROM player, rcr_game_id, rcr_game_score";
					final String querySubWherePart = " WHERE player.id=rcr_game_score.player_id AND rcr_game_id.id=rcr_game_score.rcr_game_id AND rcr_game_id.rcr_tournament_id=?";
					final String querySubRegularPart = onlyRegularPlayers ? " AND player.regular=TRUE" : "";
					final String querySubFinalPart = ") AS year_score";
					final String queryGroupPart = " GROUP BY name, display_name, y, m";
					final String queryHavingPart = useMinimumGame ? " HAVING COUNT(*)>=" + Integer.toString(MINIMUM_GAME_MONTH) : "";
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

}
