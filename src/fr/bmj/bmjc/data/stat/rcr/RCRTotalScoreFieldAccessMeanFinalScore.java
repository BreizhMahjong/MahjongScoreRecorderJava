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
package fr.bmj.bmjc.data.stat.rcr;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public class RCRTotalScoreFieldAccessMeanFinalScore implements RCRTotalScoreFieldAccess {
	private final DecimalFormat meanScoreFormat;
	private final DecimalFormat stdDevFormat;

	public RCRTotalScoreFieldAccessMeanFinalScore() {
		meanScoreFormat = new DecimalFormat("+#,#00;-#,#00");
		DecimalFormatSymbols symbols = meanScoreFormat.getDecimalFormatSymbols();
		symbols.setGroupingSeparator(' ');
		meanScoreFormat.setDecimalFormatSymbols(symbols);

		stdDevFormat = new DecimalFormat("#,000");
		symbols = stdDevFormat.getDecimalFormatSymbols();
		symbols.setGroupingSeparator(' ');
		stdDevFormat.setDecimalFormatSymbols(symbols);
	}

	@Override
	public String getDataString(final RCRTotalScore data) {
		return meanScoreFormat.format(data.totalScore) + " (" + stdDevFormat.format(data.umaScore) + ")";
	}

}
