package fr.bmj.bmjc.dataaccess.impl.db.rcr;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import fr.bmj.bmjc.data.game.Tournament;
import fr.bmj.bmjc.data.stat.rcr.RCRDataPackageTrend;
import fr.bmj.bmjc.dataaccess.abs.rcr.DataAccessRCRTrend;
import fr.bmj.bmjc.enums.EnumPeriodMode;

public class DataAccessDataBaseRCRTrend extends DataAccessDataBaseRCRCommon implements DataAccessRCRTrend {

	public DataAccessDataBaseRCRTrend(final Connection dataBaseConnection) {
		super(
			dataBaseConnection);
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
					1);
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
			final List<Long> dates = new ArrayList<Long>();
			final SortedMap<String, List<Integer>> dataWithPlayerName = new TreeMap<String, List<Integer>>();
			final SortedMap<String, List<Integer>> dataWithDisplayName = new TreeMap<String, List<Integer>>();
			{
				final String querySelectPart = "SELECT DISTINCT player.name, player.display_name FROM player, rcr_game_id, rcr_game_score";
				final String queryWherePart =
					" WHERE player.id=rcr_game_score.player_id AND rcr_game_id.id=rcr_game_score.rcr_game_id AND rcr_game_id.rcr_tournament_id=?";
				final String queryPeriodPart = " AND rcr_game_id.date>=? AND rcr_game_id.date<?";
				PreparedStatement statement = null;
				if (periodMode == EnumPeriodMode.ALL) {
					statement = dataBaseConnection.prepareStatement(
						querySelectPart + queryWherePart);
					statement.setShort(
						1,
						tournament.getId());
				} else {
					statement = dataBaseConnection.prepareStatement(
						querySelectPart + queryWherePart + queryPeriodPart);
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
					final List<Integer> scoreListPlayerNames = new ArrayList<Integer>();
					scoreListPlayerNames.add(
						0);
					dataWithPlayerName.put(
						result.getString(
							1),
						scoreListPlayerNames);

					final List<Integer> scoreListDisplayNames = new ArrayList<Integer>();
					scoreListDisplayNames.add(
						0);
					dataWithDisplayName.put(
						result.getString(
							2),
						scoreListDisplayNames);
				}
				result.close();
				statement.close();
			}

			{
				final String querySelectPart =
					"SELECT rcr_game_id.date, player.name, player.display_name, SUM(rcr_game_score.final_score) FROM rcr_game_id, rcr_game_score, player";
				final String queryWherePart =
					" WHERE rcr_game_id.rcr_tournament_id=? AND rcr_game_id.id=rcr_game_score.rcr_game_id AND player.id=rcr_game_score.player_id";
				final String queryPeriodPart = " AND rcr_game_id.date>=? AND rcr_game_id.date<?";
				final String queryGroupPart = " GROUP BY rcr_game_id.date, player.name, player.display_name";
				final String queryOrderPart = " ORDER BY rcr_game_id.date";
				PreparedStatement statement = null;
				if (periodMode == EnumPeriodMode.ALL) {
					statement = dataBaseConnection.prepareStatement(
						querySelectPart + queryWherePart + queryGroupPart + queryOrderPart);
					statement.setShort(
						1,
						tournament.getId());
				} else {
					statement = dataBaseConnection.prepareStatement(
						querySelectPart + queryWherePart + queryPeriodPart + queryGroupPart + queryOrderPart);
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

				int dateIndex = 0;
				long lastDate = 0;
				long date;
				dates.add(
					dateIndex,
					lastDate);
				final ResultSet result = statement.executeQuery();
				while (result.next()) {
					date = result.getDate(
						1).getTime();
					if (date != lastDate) {
						for (final String playerName : dataWithPlayerName.keySet()) {
							final List<Integer> playerScore = dataWithPlayerName.get(
								playerName);
							playerScore.add(
								playerScore.get(
									dateIndex));
						}
						for (final String playerName : dataWithDisplayName.keySet()) {
							final List<Integer> playerScore = dataWithDisplayName.get(
								playerName);
							playerScore.add(
								playerScore.get(
									dateIndex));
						}
						lastDate = date;
						dateIndex++;
						dates.add(
							dateIndex,
							lastDate);
					}
					final int score = result.getInt(
						4);
					final List<Integer> scoreListPlayerNames = dataWithPlayerName.get(
						result.getString(
							2));
					scoreListPlayerNames.set(
						dateIndex,
						scoreListPlayerNames.get(
							dateIndex) + score);
					final List<Integer> scoreListDisplayNames = dataWithDisplayName.get(
						result.getString(
							3));
					scoreListDisplayNames.set(
						dateIndex,
						scoreListDisplayNames.get(
							dateIndex) + score);
				}
			}
			return new RCRDataPackageTrend(
				dates,
				dataWithPlayerName,
				dataWithDisplayName);
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
