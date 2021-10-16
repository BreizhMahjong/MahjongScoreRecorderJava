package fr.bmj.bmjc.data.stat.rcr;

import java.util.List;

public class RCRDataPackageScoreAnalyze {

	public final List<String> playerNames;
	public final List<String> displayNames;
	public final double[][] scores;
	public final double[] sums;

	public RCRDataPackageScoreAnalyze(final List<String> playerNames,
		final List<String> displayNames,
		final double[][] scores,
		final double[] sums) {
		this.playerNames = playerNames;
		this.displayNames = displayNames;
		this.scores = scores;
		this.sums = sums;
	}

}
