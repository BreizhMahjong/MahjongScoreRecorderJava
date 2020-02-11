package fr.bmj.bmjc.dataaccess.abs.rcr;

import java.util.List;

import fr.bmj.bmjc.data.game.Tournament;
import fr.bmj.bmjc.data.stat.rcr.RCRTotalScore;
import fr.bmj.bmjc.enums.EnumPeriodMode;
import fr.bmj.bmjc.enums.EnumRankingMode;
import fr.bmj.bmjc.enums.EnumSortingMode;

public interface DataAccessRCRRanking {

	public void setUseMinimumGame(boolean useMinimumGame);

	public void setOnlyRegularPlayers(boolean onlyRegularPlayers);

	public List<RCRTotalScore> getRCRDataPackageRanking(final Tournament tournament, final EnumRankingMode rankingMode, final EnumSortingMode sortingMode,
		final EnumPeriodMode periodMode, final int year, final int trimester, final int month, final int day);

}
