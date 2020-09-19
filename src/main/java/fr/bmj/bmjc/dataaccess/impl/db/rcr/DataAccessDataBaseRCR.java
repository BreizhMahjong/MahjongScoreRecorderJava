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
package fr.bmj.bmjc.dataaccess.impl.db.rcr;

import java.sql.Connection;
import java.util.List;

import fr.bmj.bmjc.data.game.Player;
import fr.bmj.bmjc.data.game.Tournament;
import fr.bmj.bmjc.data.game.rcr.RCRGame;
import fr.bmj.bmjc.data.stat.rcr.RCRDataPackagePersonalAnalyze;
import fr.bmj.bmjc.data.stat.rcr.RCRDataPackageScoreAnalyze;
import fr.bmj.bmjc.data.stat.rcr.RCRDataPackageTrend;
import fr.bmj.bmjc.data.stat.rcr.RCRTotalScore;
import fr.bmj.bmjc.dataaccess.abs.UpdateResult;
import fr.bmj.bmjc.dataaccess.abs.rcr.DataAccessRCR;
import fr.bmj.bmjc.enums.EnumPeriodMode;
import fr.bmj.bmjc.enums.EnumRankingMode;
import fr.bmj.bmjc.enums.EnumScoreMode;
import fr.bmj.bmjc.enums.EnumSortingMode;

public class DataAccessDataBaseRCR implements DataAccessRCR {

	private final DataAccessDataBaseRCRTournament dataAccessRCRTournament;
	private final DataAccessDataBaseRCRGame dataAccessRCRGame;
	private final DataAccessDataBaseRCRPersonalAnalyze dataAccessRCRPeronalAnalyze;
	private final DataAccessDataBaseRCRScoreAnalyze dataAccessRCRScoreAnalyze;
	private final DataAccessDataBaseRCRRanking dataAccessRCRRanking;
	private final DataAccessDataBaseRCRTrend dataAccessRCRTrend;

	public DataAccessDataBaseRCR(final Connection connection) {
		dataAccessRCRTournament = new DataAccessDataBaseRCRTournament(
			connection);
		dataAccessRCRGame = new DataAccessDataBaseRCRGame(
			connection);
		dataAccessRCRPeronalAnalyze = new DataAccessDataBaseRCRPersonalAnalyze(
			connection);
		dataAccessRCRScoreAnalyze = new DataAccessDataBaseRCRScoreAnalyze(
			connection);
		dataAccessRCRRanking = new DataAccessDataBaseRCRRanking(
			connection);
		dataAccessRCRTrend = new DataAccessDataBaseRCRTrend(
			connection);
	}

	@Override
	public UpdateResult addRCRTournament(final String tournamentName) {
		return dataAccessRCRTournament.addRCRTournament(
			tournamentName);
	}

	@Override
	public UpdateResult modifyRCRTournament(final short tournamentId, final String tournamentName) {
		return dataAccessRCRTournament.modifyRCRTournament(
			tournamentId,
			tournamentName);
	}

	@Override
	public List<Tournament> getRCRTournaments() {
		return dataAccessRCRTournament.getRCRTournaments();
	}

	@Override
	public UpdateResult deleteRCRTournament(final short tournamentId) {
		return dataAccessRCRTournament.deleteRCRTournament(
			tournamentId);
	}

	@Override
	public UpdateResult addRCRGame(final RCRGame game) {
		return dataAccessRCRGame.addRCRGame(
			game);
	}

	@Override
	public void setOnlyRegularPlayers(final boolean onlyRegularPlayers) {
		dataAccessRCRGame.setOnlyRegularPlayers(
			onlyRegularPlayers);
		dataAccessRCRRanking.setOnlyRegularPlayers(
			onlyRegularPlayers);
	}

	@Override
	public List<Integer> getRCRYears(final Tournament tournament) {
		return dataAccessRCRGame.getRCRYears(
			tournament);
	}

	@Override
	public List<Integer> getRCRGameDays(final Tournament tournament, final int year, final int month) {
		return dataAccessRCRGame.getRCRGameDays(
			tournament,
			year,
			month);
	}

	@Override
	public List<Long> getRCRGameIds(final Tournament tournament, final int year, final int month, final int day) {
		return dataAccessRCRGame.getRCRGameIds(
			tournament,
			year,
			month,
			day);
	}

	@Override
	public RCRGame getRCRGame(final long id) {
		return dataAccessRCRGame.getRCRGame(
			id);
	}

	@Override
	public UpdateResult deleteRCRGame(final long id) {
		return dataAccessRCRGame.deleteRCRGame(
			id);
	}

	@Override
	public List<Player> getRCRPlayers() {
		return dataAccessRCRGame.getRCRPlayers();
	}

	@Override
	public void setUseMinimumGame(final boolean useMinimumGame) {
		dataAccessRCRRanking.setUseMinimumGame(
			useMinimumGame);
	}

	@Override
	public List<RCRTotalScore> getRCRDataPackageRanking(final Tournament tournament, final EnumRankingMode rankingMode, final EnumSortingMode sortingMode,
		final EnumPeriodMode periodMode, final int year, final int trimester, final int month, final int day) {
		return dataAccessRCRRanking.getRCRDataPackageRanking(
			tournament,
			rankingMode,
			sortingMode,
			periodMode,
			year,
			trimester,
			month,
			day);
	}

	@Override
	public RCRDataPackageTrend getRCRDataPackageTrend(final Tournament tournament, final EnumPeriodMode periodMode, final int year, final int trimester,
		final int month, final int day) {
		return dataAccessRCRTrend.getRCRDataPackageTrend(
			tournament,
			periodMode,
			year,
			trimester,
			month,
			day);
	}

	@Override
	public RCRDataPackagePersonalAnalyze getRCRDataPackagePersonalAnalyze(final Tournament tournament, final short playerId, final EnumScoreMode scoreMode,
		final EnumPeriodMode periodMode, final int year, final int trimester, final int month, final int day) {
		return dataAccessRCRPeronalAnalyze.getRCRDataPackagePersonalAnalyze(
			tournament,
			playerId,
			scoreMode,
			periodMode,
			year,
			trimester,
			month,
			day);
	}

	@Override
	public RCRDataPackageScoreAnalyze getRCRDataPackageScoreAnalyze(final Tournament tournament, final EnumPeriodMode periodMode, final int year,
		final int trimester, final int month, final int day) {
		return dataAccessRCRScoreAnalyze.getRCRDataPackageScoreAnalyze(
			tournament,
			periodMode,
			year,
			trimester,
			month,
			day);
	}

}
