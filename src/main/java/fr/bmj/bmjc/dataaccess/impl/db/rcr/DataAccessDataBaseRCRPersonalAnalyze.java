package fr.bmj.bmjc.dataaccess.impl.db.rcr;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import fr.bmj.bmjc.data.game.Tournament;
import fr.bmj.bmjc.data.stat.rcr.RCRDataPackagePersonalAnalyze;
import fr.bmj.bmjc.dataaccess.abs.rcr.DataAccessRCRPersonalAnalyze;
import fr.bmj.bmjc.enums.EnumPeriodMode;
import fr.bmj.bmjc.enums.EnumScoreMode;

public class DataAccessDataBaseRCRPersonalAnalyze extends DataAccessDataBaseRCRCommon implements DataAccessRCRPersonalAnalyze {

	public DataAccessDataBaseRCRPersonalAnalyze(final Connection dataBaseConnection) {
		super(dataBaseConnection);
	}

	@Override
	public RCRDataPackagePersonalAnalyze getRCRDataPackagePersonalAnalyze(final Tournament tournament,
		final short playerId,
		final EnumScoreMode scoreMode,
		final EnumPeriodMode periodMode,
		final int year,
		final int trimester,
		final int month,
		final int day) {
		final Calendar calendarFrom = Calendar.getInstance();
		final Calendar calendarTo = Calendar.getInstance();
		switch (periodMode) {
			case ALL:
				break;
			case YEAR:
				calendarFrom.set(Calendar.YEAR,
					year);
				calendarFrom.set(Calendar.MONTH,
					Calendar.JANUARY);
				calendarFrom.set(Calendar.DAY_OF_MONTH,
					1);
				calendarTo.setTime(calendarFrom.getTime());
				calendarTo.add(Calendar.YEAR,
					1);
				break;
			case TRIMESTER:
				calendarFrom.set(Calendar.YEAR,
					year);
				calendarFrom.set(Calendar.MONTH,
					trimester * 3);
				calendarFrom.set(Calendar.DAY_OF_MONTH,
					1);
				calendarTo.setTime(calendarFrom.getTime());
				calendarTo.add(Calendar.MONTH,
					3);
				break;
			case MONTH:
				calendarFrom.set(Calendar.YEAR,
					year);
				calendarFrom.set(Calendar.MONTH,
					month);
				calendarFrom.set(Calendar.DAY_OF_MONTH,
					1);
				calendarTo.setTime(calendarFrom.getTime());
				calendarTo.add(Calendar.MONTH,
					1);
				break;
			case DAY:
				calendarFrom.set(Calendar.YEAR,
					year);
				calendarFrom.set(Calendar.MONTH,
					month);
				calendarFrom.set(Calendar.DAY_OF_MONTH,
					day);
				calendarTo.setTime(calendarFrom.getTime());
				calendarTo.add(Calendar.DAY_OF_MONTH,
					1);
				break;
			default:
				break;
		}

		final RCRDataPackagePersonalAnalyze dataPackage = new RCRDataPackagePersonalAnalyze();
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
				statement.setShort(1,
					playerId);
				statement.setShort(2,
					tournament.getId());
			} else {
				statement = dataBaseConnection.prepareStatement("SELECT rcr_game_id.id, rcr_game_id.nb_players, rcr_game_score.ranking, rcr_game_score."
					+ fieldString
					+ " FROM rcr_game_id, rcr_game_score WHERE rcr_game_id.id=rcr_game_score.rcr_game_id AND rcr_game_score.player_id=? AND rcr_game_id.rcr_tournament_id=? AND rcr_game_id.date>=? AND rcr_game_id.date<? ORDER BY rcr_game_id.id ASC");
				statement.setShort(1,
					playerId);
				statement.setShort(2,
					tournament.getId());
				statement.setDate(3,
					new Date(calendarFrom.getTimeInMillis()));
				statement.setDate(4,
					new Date(calendarTo.getTimeInMillis()));
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

				listGameID.add(numberOfGames,
					gameID);
				listScore.add(numberOfGames,
					score);

				if (score >= 0) {
					positiveGames++;
					positiveTotal += score;
				} else {
					negativeGames++;
					negativeTotal += score;
				}
				maxScore = Math.max(maxScore,
					score);
				minScore = Math.min(minScore,
					score);

				totalScore += score;
				listSum.add(numberOfGames,
					totalScore);

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
				deviation += Math.pow(listScore.get(index) - averageScore,
					2.0);
			}
			final long standardDeviation = numberOfGames <= 1
				? 0
				: Math.round(Math.sqrt(deviation / numberOfGames));

			for (int index = 0; index < 4; index++) {
				placeFourPlayersPercent[index] = Math.round(placeFourPlayers[index] * 100f / numberOfFourPlayersGames);
			}
			for (int index = 0; index < 5; index++) {
				placeFivePlayersPercent[index] = Math.round(placeFivePlayers[index] * 100f / numberOfFivePlayersGames);
			}

			dataPackage.setLists(listGameID,
				listScore,
				listSum);
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

}
