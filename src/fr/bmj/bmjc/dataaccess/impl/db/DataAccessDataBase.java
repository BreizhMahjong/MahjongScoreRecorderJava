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
import fr.bmj.bmjc.data.game.mcr.MCRGame;
import fr.bmj.bmjc.data.game.rcr.RCRGame;
import fr.bmj.bmjc.data.stat.mcr.MCRDataPackageAnalyze;
import fr.bmj.bmjc.data.stat.mcr.MCRDataPackageTrend;
import fr.bmj.bmjc.data.stat.mcr.MCRScoreTotal;
import fr.bmj.bmjc.data.stat.rcr.RCRDataPackageAnalyze;
import fr.bmj.bmjc.data.stat.rcr.RCRDataPackageTrend;
import fr.bmj.bmjc.data.stat.rcr.RCRScoreTotal;
import fr.bmj.bmjc.dataaccess.DataAccess;
import fr.bmj.bmjc.dataaccess.DataAccessManagePlayer;
import fr.bmj.bmjc.dataaccess.UpdateResult;
import fr.bmj.bmjc.dataaccess.mcr.DataAccessMCR;
import fr.bmj.bmjc.dataaccess.rcr.DataAccessRCR;
import fr.bmj.bmjc.enums.EnumPeriodMode;
import fr.bmj.bmjc.enums.EnumRankingMode;
import fr.bmj.bmjc.enums.EnumScoreMode;
import fr.bmj.bmjc.enums.EnumSortingMode;

public class DataAccessDataBase implements DataAccess {

	private final File dataBaseFile;
	private Connection dataBaseConnection;

	private DataAccessManagePlayer dataAccessManagePlayer;
	private DataAccessRCR dataAccessRCR;
	private DataAccessMCR dataAccessMCR;

	public DataAccessDataBase(final File dataBaseFile) {
		this.dataBaseFile = dataBaseFile;
	}

	@Override
	public void initialize() {
		try {
			Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
			if (dataBaseFile.exists() && dataBaseFile.isDirectory()) {
				dataBaseConnection = DriverManager.getConnection("jdbc:derby:" + dataBaseFile.getAbsolutePath());
				dataAccessManagePlayer = new DataAccessDataBaseManagePlayer(dataBaseConnection);
				dataAccessRCR = new DataAccessDataBaseRCR(dataBaseConnection);
				dataAccessMCR = new DataAccessDataBaseMCR(dataBaseConnection);
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
	public List<Player> getAllPlayers() {
		return dataAccessManagePlayer.getAllPlayers();
	}

	@Override
	public UpdateResult modifyPlayer(final int id, final String name, final String displayName) {
		return dataAccessManagePlayer.modifyPlayer(id, name, displayName);
	}

	@Override
	public UpdateResult hidePlayer(final int id, final boolean hidden) {
		return dataAccessManagePlayer.hidePlayer(id, hidden);
	}

	@Override
	public List<Player> getRegisteredPlayers() {
		return dataAccessManagePlayer.getRegisteredPlayers();
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
	public RCRDataPackageAnalyze getRCRDataPackageAnalyze(final Tournament tournament, final int playerId, final EnumScoreMode scoreMode, final EnumPeriodMode periodMode, final int year,
		final int trimester, final int month) {
		return dataAccessRCR.getRCRDataPackageAnalyze(tournament, playerId, scoreMode, periodMode, year, trimester, month);
	}

	@Override
	public List<RCRScoreTotal> getRCRDataPackageRanking(final Tournament tournament, final EnumRankingMode rankingMode, final EnumSortingMode sortingMode, final EnumPeriodMode periodMode,
		final int year, final int trimester, final int month) {
		return dataAccessRCR.getRCRDataPackageRanking(tournament, rankingMode, sortingMode, periodMode, year, trimester, month);
	}

	@Override
	public RCRDataPackageTrend getRCRDataPackageTrend(final Tournament tournament, final EnumPeriodMode periodMode, final int year, final int trimester, final int month) {
		return dataAccessRCR.getRCRDataPackageTrend(tournament, periodMode, year, trimester, month);
	}

	@Override
	public List<Player> getMCRPlayers() {
		return dataAccessMCR.getMCRPlayers();
	}

	@Override
	public UpdateResult addMCRTournament(final String tournamentName) {
		return dataAccessMCR.addMCRTournament(tournamentName);
	}

	@Override
	public UpdateResult modifyMCRTournament(final int tournamentId, final String tournamentName) {
		return dataAccessMCR.modifyMCRTournament(tournamentId, tournamentName);
	}

	@Override
	public List<Tournament> getMCRTournaments() {
		return dataAccessMCR.getMCRTournaments();
	}

	@Override
	public UpdateResult deleteMCRTournament(final int tournamentId) {
		return dataAccessMCR.deleteMCRTournament(tournamentId);
	}

	@Override
	public UpdateResult addMCRGame(final MCRGame game) {
		return dataAccessMCR.addMCRGame(game);
	}

	@Override
	public List<Integer> getMCRGameDays(final Tournament tournament, final int year, final int month) {
		return dataAccessMCR.getMCRGameDays(tournament, year, month);
	}

	@Override
	public List<Integer> getMCRGameIds(final Tournament tournament, final int year, final int month, final int day) {
		return dataAccessMCR.getMCRGameIds(tournament, year, month, day);
	}

	@Override
	public MCRGame getMCRGame(final int id) {
		return dataAccessMCR.getMCRGame(id);
	}

	@Override
	public List<Integer> getMCRYears(final Tournament tournament) {
		return dataAccessMCR.getMCRYears(tournament);
	}

	@Override
	public UpdateResult deleteMCRGame(final int id) {
		return dataAccessMCR.deleteMCRGame(id);
	}

	@Override
	public void setMCRUseMinimumGame(final boolean useMinimumGame) {
		dataAccessMCR.setMCRUseMinimumGame(useMinimumGame);
	}

	@Override
	public MCRDataPackageAnalyze getMCRDataPackageAnalyze(final Tournament tournament, final int playerId, final EnumScoreMode scoreMode, final EnumPeriodMode periodMode, final int year,
		final int trimester, final int month) {
		return dataAccessMCR.getMCRDataPackageAnalyze(tournament, playerId, scoreMode, periodMode, year, trimester, month);
	}

	@Override
	public List<MCRScoreTotal> getMCRDataPackageRanking(final Tournament tournament, final EnumRankingMode rankingMode, final EnumSortingMode sortingMode, final EnumPeriodMode periodMode,
		final int year, final int trimester, final int month) {
		return dataAccessMCR.getMCRDataPackageRanking(tournament, rankingMode, sortingMode, periodMode, year, trimester, month);
	}

	@Override
	public MCRDataPackageTrend getMCRDataPackageTrend(final Tournament tournament, final EnumPeriodMode periodMode, final int year, final int trimester, final int month) {
		return dataAccessMCR.getMCRDataPackageTrend(tournament, periodMode, year, trimester, month);
	}

}
