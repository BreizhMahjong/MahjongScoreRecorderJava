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

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;

import fr.bmj.bmjc.data.game.Player;
import fr.bmj.bmjc.data.game.Tournament;
import fr.bmj.bmjc.data.game.rcr.RCRGame;
import fr.bmj.bmjc.data.stat.rcr.RCRDataPackageAnalyze;
import fr.bmj.bmjc.data.stat.rcr.RCRDataPackageTrend;
import fr.bmj.bmjc.data.stat.rcr.RCRTotalScore;
import fr.bmj.bmjc.dataaccess.DataAccess;
import fr.bmj.bmjc.dataaccess.DataAccessManagePlayer;
import fr.bmj.bmjc.dataaccess.UpdateResult;
import fr.bmj.bmjc.dataaccess.rcr.DataAccessRCR;
import fr.bmj.bmjc.enums.EnumPeriodMode;
import fr.bmj.bmjc.enums.EnumRankingMode;
import fr.bmj.bmjc.enums.EnumScoreMode;
import fr.bmj.bmjc.enums.EnumSortingMode;

public class DataAccessDataBase implements DataAccess {

	private static final String DATABASE_NAME = "DataBase";
	private Connection dataBaseConnection;

	private DataAccessManagePlayer dataAccessManagePlayer;
	private DataAccessRCR dataAccessRCR;

	public DataAccessDataBase() {
	}

	@Override
	public void initialize() {
		try {
			final File dataBaseFile = new File(DATABASE_NAME);
			Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
			if (dataBaseFile.exists() && dataBaseFile.isDirectory()) {
				dataBaseConnection = DriverManager.getConnection("jdbc:derby:" + dataBaseFile.getAbsolutePath());
				dataAccessManagePlayer = new DataAccessDataBaseManagePlayer(dataBaseConnection);
				dataAccessRCR = new DataAccessDataBaseRCR(dataBaseConnection);
			} else {
				dataBaseConnection = null;
			}
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean isConnected() {
		return dataBaseConnection != null;
	}

	@Override
	public void disconnect() {
		if (dataBaseConnection != null) {
			try {
				dataBaseConnection.close();
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public UpdateResult addPlayer(final String name, final String displayName) {
		return dataAccessManagePlayer.addPlayer(name, displayName);
	}

	@Override
	public void setOnlyFrequentPlayers(final boolean onlyFrequentPlayers) {
		dataAccessManagePlayer.setOnlyFrequentPlayers(onlyFrequentPlayers);
	}

	@Override
	public List<Player> getAllPlayers() {
		return dataAccessManagePlayer.getAllPlayers();
	}

	@Override
	public UpdateResult modifyPlayer(final int id, final String name, final String displayName, final boolean hidden, final boolean regular) {
		return dataAccessManagePlayer.modifyPlayer(id, name, displayName, hidden, regular);
	}

	@Override
	public List<Player> getPlayers() {
		return dataAccessManagePlayer.getPlayers();
	}

	@Override
	public UpdateResult deletePlayer(final int id) {
		return dataAccessManagePlayer.deletePlayer(id);
	}

	@Override
	public List<Player> getRCRPlayers() {
		return dataAccessRCR.getRCRPlayers();
	}

	@Override
	public UpdateResult addRCRTournament(final String tournamentName) {
		return dataAccessRCR.addRCRTournament(tournamentName);
	}

	@Override
	public UpdateResult modifyRCRTournament(final int tournamentId, final String tournamentName) {
		return dataAccessRCR.modifyRCRTournament(tournamentId, tournamentName);
	}

	@Override
	public List<Tournament> getRCRTournaments() {
		return dataAccessRCR.getRCRTournaments();
	}

	@Override
	public UpdateResult deleteRCRTournament(final int tournamentId) {
		return dataAccessRCR.deleteRCRTournament(tournamentId);
	}

	@Override
	public UpdateResult addRCRGame(final RCRGame game) {
		return dataAccessRCR.addRCRGame(game);
	}

	@Override
	public List<Integer> getRCRYears(final Tournament tournament) {
		return dataAccessRCR.getRCRYears(tournament);
	}

	@Override
	public List<Integer> getRCRGameDays(final Tournament tournament, final int year, final int month) {
		return dataAccessRCR.getRCRGameDays(tournament, year, month);
	}

	@Override
	public List<Integer> getRCRGameIds(final Tournament tournament, final int year, final int month, final int day) {
		return dataAccessRCR.getRCRGameIds(tournament, year, month, day);
	}

	@Override
	public RCRGame getRCRGame(final int id) {
		return dataAccessRCR.getRCRGame(id);
	}

	@Override
	public UpdateResult deleteRCRGame(final int id) {
		return dataAccessRCR.deleteRCRGame(id);
	}

	@Override
	public void setRCRUseMinimumGame(final boolean useMinimumGame) {
		dataAccessRCR.setRCRUseMinimumGame(useMinimumGame);
	}

	@Override
	public void setRCROnlyRegularPlayers(final boolean onlyRegularPlayers) {
		dataAccessRCR.setRCROnlyRegularPlayers(onlyRegularPlayers);
	}

	@Override
	public RCRDataPackageAnalyze getRCRDataPackageAnalyze(final Tournament tournament, final int playerId, final EnumScoreMode scoreMode, final EnumPeriodMode periodMode, final int year,
		final int trimester, final int month, final int day) {
		return dataAccessRCR.getRCRDataPackageAnalyze(tournament, playerId, scoreMode, periodMode, year, trimester, month, day);
	}

	@Override
	public List<RCRTotalScore> getRCRDataPackageRanking(final Tournament tournament, final EnumRankingMode rankingMode, final EnumSortingMode sortingMode, final EnumPeriodMode periodMode,
		final int year, final int trimester, final int month, final int day) {
		return dataAccessRCR.getRCRDataPackageRanking(tournament, rankingMode, sortingMode, periodMode, year, trimester, month, day);
	}

	@Override
	public RCRDataPackageTrend getRCRDataPackageTrend(final Tournament tournament, final EnumPeriodMode periodMode, final int year, final int trimester, final int month, final int day) {
		return dataAccessRCR.getRCRDataPackageTrend(tournament, periodMode, year, trimester, month, day);
	}

}
