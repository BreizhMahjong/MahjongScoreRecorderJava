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
package fr.bmj.bmjc.dataaccess.rcr;

import java.util.List;

import fr.bmj.bmjc.data.game.Player;
import fr.bmj.bmjc.data.game.Tournament;
import fr.bmj.bmjc.data.game.rcr.RCRGame;
import fr.bmj.bmjc.data.stat.rcr.RCRDataPackageAnalyze;
import fr.bmj.bmjc.data.stat.rcr.RCRDataPackageScoreAnalyze;
import fr.bmj.bmjc.data.stat.rcr.RCRDataPackageTrend;
import fr.bmj.bmjc.data.stat.rcr.RCRTotalScore;
import fr.bmj.bmjc.dataaccess.DataAccessCommon;
import fr.bmj.bmjc.dataaccess.UpdateResult;
import fr.bmj.bmjc.enums.EnumPeriodMode;
import fr.bmj.bmjc.enums.EnumRankingMode;
import fr.bmj.bmjc.enums.EnumScoreMode;
import fr.bmj.bmjc.enums.EnumSortingMode;

public interface DataAccessRCR extends DataAccessCommon {

	public List<Player> getRCRPlayers();

	public UpdateResult addRCRTournament(final String tournamentName);

	public UpdateResult modifyRCRTournament(final int tournamentId, final String tournamentName);

	public List<Tournament> getRCRTournaments();

	public UpdateResult deleteRCRTournament(final int tournamentId);

	public UpdateResult addRCRGame(final RCRGame game);

	public List<Integer> getRCRYears(final Tournament tournament);

	public List<Integer> getRCRGameDays(final Tournament tournament, final int year, final int month);

	public List<Integer> getRCRGameIds(final Tournament tournament, final int year, final int month, final int day);

	public RCRGame getRCRGame(final int id);

	public UpdateResult deleteRCRGame(final int id);

	public void setRCRUseMinimumGame(boolean useMinimumGame);

	public void setRCROnlyRegularPlayers(boolean onlyRegularPlayers);

	public RCRDataPackageAnalyze getRCRDataPackageAnalyze(final Tournament tournament, final int playerId, final EnumScoreMode scoreMode,
		final EnumPeriodMode periodMode, final int year, final int trimester, final int month, final int day);

	public List<RCRTotalScore> getRCRDataPackageRanking(final Tournament tournament, final EnumRankingMode rankingMode, final EnumSortingMode sortingMode,
		final EnumPeriodMode periodMode, final int year, final int trimester, final int month, final int day);

	public RCRDataPackageTrend getRCRDataPackageTrend(final Tournament tournament, final EnumPeriodMode periodMode, final int year, final int trimester,
		final int month, final int day);

	public RCRDataPackageScoreAnalyze getRCRDataPackageScoreAnalyze(final Tournament tournament, final EnumPeriodMode periodMode, final int year, final int trimester,
		final int month, final int day);

}
