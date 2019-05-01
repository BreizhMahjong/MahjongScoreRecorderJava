/*
 * This file is part of Breizh Mahjong Recorder.
 *
 * Breizh Mahjong Recorder is free software: you can redistribute it and/or
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
package fr.bmj.bmjc.dataaccess.mcr;

import java.util.List;

import fr.bmj.bmjc.data.game.Player;
import fr.bmj.bmjc.data.game.Tournament;
import fr.bmj.bmjc.data.game.mcr.MCRGame;
import fr.bmj.bmjc.data.stat.mcr.MCRDataPackageAnalyze;
import fr.bmj.bmjc.data.stat.mcr.MCRDataPackageTrend;
import fr.bmj.bmjc.data.stat.mcr.MCRTotalScore;
import fr.bmj.bmjc.dataaccess.DataAccessCommon;
import fr.bmj.bmjc.dataaccess.UpdateResult;
import fr.bmj.bmjc.enums.EnumPeriodMode;
import fr.bmj.bmjc.enums.EnumRankingMode;
import fr.bmj.bmjc.enums.EnumScoreMode;
import fr.bmj.bmjc.enums.EnumSortingMode;

public interface DataAccessMCR extends DataAccessCommon {

	public List<Player> getMCRPlayers();

	public UpdateResult addMCRTournament(final String tournamentName);

	public UpdateResult modifyMCRTournament(final int tournamentId, final String tournamentName);

	public List<Tournament> getMCRTournaments();

	public UpdateResult deleteMCRTournament(final int tournamentId);

	public UpdateResult addMCRGame(final MCRGame game);

	public List<Integer> getMCRYears(final Tournament tournament);

	public List<Integer> getMCRGameDays(final Tournament tournament, final int year, final int month);

	public List<Integer> getMCRGameIds(final Tournament tournament, final int year, final int month, final int day);

	public MCRGame getMCRGame(final int id);

	public UpdateResult deleteMCRGame(final int id);

	public void setMCRUseMinimumGame(boolean useMinimumGame);

	public void setMCROnlyRegularPlayers(boolean onlyRegularPlayers);

	public MCRDataPackageAnalyze getMCRDataPackageAnalyze(final Tournament tournament, final int playerId, final EnumScoreMode scoreMode, final EnumPeriodMode periodMode, final int year,
		final int trimester, final int month);

	public List<MCRTotalScore> getMCRDataPackageRanking(final Tournament tournament, final EnumRankingMode rankingMode, final EnumSortingMode sortingMode, final EnumPeriodMode periodMode,
		final int year, final int trimester, final int month);

	public MCRDataPackageTrend getMCRDataPackageTrend(final Tournament tournament, final EnumPeriodMode periodMode, final int year, final int trimester, final int month);

}
