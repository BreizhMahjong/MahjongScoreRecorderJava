package fr.bmj.bmjc.dataaccess.abs.rcr;

import fr.bmj.bmjc.data.game.Tournament;
import fr.bmj.bmjc.data.stat.rcr.RCRDataPackagePersonalAnalyze;
import fr.bmj.bmjc.enums.EnumPeriodMode;
import fr.bmj.bmjc.enums.EnumScoreMode;

public interface DataAccessRCRPersonalAnalyze {

	public RCRDataPackagePersonalAnalyze getRCRDataPackagePersonalAnalyze(final Tournament tournament,
		final short playerId,
		final EnumScoreMode scoreMode,
		final EnumPeriodMode periodMode,
		final int year,
		final int trimester,
		final int month,
		final int day);

}
