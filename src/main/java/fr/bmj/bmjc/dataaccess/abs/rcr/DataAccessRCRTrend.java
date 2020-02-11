package fr.bmj.bmjc.dataaccess.abs.rcr;

import fr.bmj.bmjc.data.game.Tournament;
import fr.bmj.bmjc.data.stat.rcr.RCRDataPackageTrend;
import fr.bmj.bmjc.enums.EnumPeriodMode;

public interface DataAccessRCRTrend {

	public RCRDataPackageTrend getRCRDataPackageTrend(final Tournament tournament, final EnumPeriodMode periodMode, final int year, final int trimester,
		final int month, final int day);

}
