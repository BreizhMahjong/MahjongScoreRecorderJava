package fr.bmj.bmjc.dataaccess.abs.rcr;

import fr.bmj.bmjc.data.game.Tournament;
import fr.bmj.bmjc.data.stat.rcr.RCRDataPackageScoreAnalyze;
import fr.bmj.bmjc.enums.EnumPeriodMode;

public interface DataAccessRCRScoreAnalyze {

	public RCRDataPackageScoreAnalyze getRCRDataPackageScoreAnalyze(final Tournament tournament, final EnumPeriodMode periodMode, final int year,
		final int trimester, final int month, final int day);

}
