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
package fr.bmj.bmjc.gui.rcr;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.CustomXYToolTipGenerator;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import fr.bmj.bmjc.data.game.ComparatorAscendingPlayerDisplayName;
import fr.bmj.bmjc.data.game.ComparatorAscendingPlayerName;
import fr.bmj.bmjc.data.game.ComparatorDescendingTournamentID;
import fr.bmj.bmjc.data.game.Player;
import fr.bmj.bmjc.data.game.Tournament;
import fr.bmj.bmjc.data.stat.rcr.RCRDataPackageAnalyze;
import fr.bmj.bmjc.dataaccess.rcr.DataAccessRCR;
import fr.bmj.bmjc.enums.EnumPeriodMode;
import fr.bmj.bmjc.enums.EnumScoreMode;
import fr.bmj.bmjc.enums.EnumTrimester;
import fr.bmj.bmjc.gui.UITabPanel;
import fr.bmj.bmjc.swing.JDialogWithProgress;
import fr.bmj.bmjc.swing.ProportionalGridLayout;
import fr.bmj.bmjc.swing.ProportionalGridLayoutConstraint;

public class UITabPanelRCRPersonalAnalyse extends UITabPanel {
	private static final long serialVersionUID = -1239855677109744990L;

	private static final int MAX_NUMBER_OF_TICKS = 20;
	private static final int TICK_UNIT_MULTIPLE = 5;

	private static final String ZERO_STRING = "0";
	private static final String PERCENTAGE_STRING = "%";
	private static final String PLUS_MINUS = " ± ";

	private static final int COMBOBOX_NUMBER = 3;
	private static final int COMBOBOX_YEAR_INDEX = 0;
	private static final int COMBOBOX_TRIMESTER_INDEX = 1;
	private static final int COMBOBOX_MONTH_INDEX = 2;

	private boolean displayFullName;
	private final DataAccessRCR dataAccess;
	private final DecimalFormat format;

	private final ActionListener comboActionListener;
	private final JComboBox<String> comboPlayerNames;

	private final JComboBox<String> comboTournament;
	private final ActionListener tournamentComboBoxActionListener;
	private final EnumScoreMode scoreModes[];
	private final JComboBox<String> comboScoreMode;

	private final EnumPeriodMode periodModes[];
	private final JComboBox<String> comboPeriodMode;
	private final JComboBox<Integer> comboYear;
	private final JComboBox<String> comboTrimester;
	private final JComboBox<String> comboMonth;

	private final boolean comboBoxActivated[];

	private final JPanel panelBarChart;
	private final JPanel panelLineChart;

	private final JLabel labelNumberOfGames;
	private final JLabel labelTotalScore;
	private final JLabel labelMeanScore;
	private final JLabel labelMaxTotal;
	private final JLabel labelMinTotal;
	private final JLabel labelMaxScore;
	private final JLabel labelMinScore;

	private final JLabel labelPositiveGames;
	private final JLabel labelPositivePercentage;
	private final JLabel labelNegativeGames;
	private final JLabel labelNegativePercentage;

	private final JLabel labelNumberOfFourPlayersGames;
	private final JLabel labelFourPlayersGamePlaces[];
	private final JLabel labelFourPlayersGamePlacesPercent[];
	private final JLabel labelNumberOfFivePlayersGames;
	private final JLabel labelFivePlayersGamePlaces[];
	private final JLabel labelFivePlayersGamePlacesPercent[];

	private final List<Player> listPlayers;
	private final List<Tournament> listTournament;

	public UITabPanelRCRPersonalAnalyse(final DataAccessRCR dataAccess, final JDialogWithProgress waitingDialog) {
		this.dataAccess = dataAccess;

		format = new DecimalFormat("#,###");
		final DecimalFormatSymbols symbols = format.getDecimalFormatSymbols();
		symbols.setGroupingSeparator(' ');
		format.setDecimalFormatSymbols(symbols);

		setLayout(new BorderLayout());
		{
			final JPanel panelNorth = new JPanel();
			final ProportionalGridLayout northLayout = new ProportionalGridLayout(2, 9, 8, 2);
			northLayout.setWeightX(4, 5, 3, 5, 3, 5, 3, 5, 2);
			panelNorth.setLayout(northLayout);
			panelNorth.setBorder(BorderFactory.createLoweredBevelBorder());
			add(panelNorth, BorderLayout.NORTH);

			final ProportionalGridLayoutConstraint c = new ProportionalGridLayoutConstraint(0, 1, 0, 1);
			{
				c.y = 0;
				c.x = 0;
				panelNorth.add(new JLabel("Nom du joueur :", JLabel.RIGHT), c);
				comboPlayerNames = new JComboBox<String>();
				comboPlayerNames.setEditable(false);
				c.x = 1;
				panelNorth.add(comboPlayerNames, c);

				c.x = 2;
				panelNorth.add(new JLabel("Tournoi :", JLabel.RIGHT), c);
				comboTournament = new JComboBox<String>();
				comboTournament.setEditable(false);
				c.x = 3;
				c.gridWidth = 3;
				panelNorth.add(comboTournament, c);

				c.x = 6;
				c.gridWidth = 1;
				panelNorth.add(new JLabel("Score :", JLabel.RIGHT), c);
				scoreModes = new EnumScoreMode[] { EnumScoreMode.FINAL_SCORE, EnumScoreMode.GAME_SCORE };
				final String scoreModeStrings[] = new String[scoreModes.length];
				for (int index = 0; index < scoreModes.length; index++) {
					scoreModeStrings[index] = scoreModes[index].toString();
				}
				comboScoreMode = new JComboBox<String>(scoreModeStrings);
				comboScoreMode.setEditable(false);
				comboScoreMode.setSelectedIndex(0);
				c.x = 7;
				panelNorth.add(comboScoreMode, c);
			}

			{
				c.y = 1;
				c.x = 0;
				panelNorth.add(new JLabel("Période :", JLabel.RIGHT), c);
				periodModes = EnumPeriodMode.values();
				final String periodModeStrings[] = new String[periodModes.length];
				for (int index = 0; index < periodModes.length; index++) {
					periodModeStrings[index] = periodModes[index].toString();
				}
				comboPeriodMode = new JComboBox<String>(periodModeStrings);
				comboPeriodMode.setEditable(false);
				comboPeriodMode.setSelectedIndex(0);
				c.x = 1;
				panelNorth.add(comboPeriodMode, c);

				c.x = 2;
				panelNorth.add(new JLabel("Année :", JLabel.RIGHT), c);
				comboYear = new JComboBox<Integer>();
				comboYear.setEditable(false);
				c.x = 3;
				panelNorth.add(comboYear, c);

				c.x = 4;
				panelNorth.add(new JLabel("Trimestre :", JLabel.RIGHT), c);
				final String trimesters[] = { EnumTrimester.WINTER.toString(), EnumTrimester.SPRING.toString(), EnumTrimester.SUMMER.toString(), EnumTrimester.AUTUMN.toString() };
				comboTrimester = new JComboBox<String>(trimesters);
				comboTrimester.setEditable(false);
				comboTrimester.setSelectedIndex(0);
				c.x = 5;
				panelNorth.add(comboTrimester, c);

				c.x = 6;
				panelNorth.add(new JLabel("Mois :", JLabel.RIGHT), c);
				final String months[] = new String[12];
				System.arraycopy(DateFormatSymbols.getInstance(Locale.FRANCE).getMonths(), 0, months, 0, 12);
				comboMonth = new JComboBox<>(months);
				comboMonth.setEditable(false);
				comboMonth.setSelectedIndex(0);
				c.x = 7;
				panelNorth.add(comboMonth, c);
			}
		}

		{
			final JPanel panelCenter = new JPanel();
			panelCenter.setLayout(new GridLayout(2, 1, 0, 1));
			add(panelCenter, BorderLayout.CENTER);

			panelBarChart = new JPanel();
			panelBarChart.setLayout(new BorderLayout());
			panelBarChart.setBorder(BorderFactory.createLoweredBevelBorder());
			panelCenter.add(panelBarChart);

			panelLineChart = new JPanel();
			panelLineChart.setLayout(new BorderLayout());
			panelLineChart.setBorder(BorderFactory.createLoweredBevelBorder());
			panelCenter.add(panelLineChart);
		}

		{
			final JPanel panelSouth = new JPanel();
			panelSouth.setLayout(new GridLayout(3, 1));
			add(panelSouth, BorderLayout.SOUTH);

			{
				final JPanel panelBasicAnalyze = new JPanel();
				panelBasicAnalyze.setLayout(new GridLayout(4, 6));
				panelBasicAnalyze.setBorder(BorderFactory.createLoweredBevelBorder());
				panelSouth.add(panelBasicAnalyze);

				panelBasicAnalyze.add(new JLabel("Nombre de parties", JLabel.CENTER));
				panelBasicAnalyze.add(new JLabel("Score max", JLabel.CENTER));
				panelBasicAnalyze.add(new JLabel("Positif", JLabel.CENTER));
				panelBasicAnalyze.add(new JLabel("Négatif", JLabel.CENTER));
				panelBasicAnalyze.add(new JLabel("Score total", JLabel.CENTER));
				panelBasicAnalyze.add(new JLabel("Total max", JLabel.CENTER));

				labelNumberOfGames = new JLabel("", JLabel.CENTER);
				panelBasicAnalyze.add(labelNumberOfGames);
				labelMaxScore = new JLabel("", JLabel.CENTER);
				panelBasicAnalyze.add(labelMaxScore);
				labelPositiveGames = new JLabel("", JLabel.CENTER);
				panelBasicAnalyze.add(labelPositiveGames);
				labelNegativeGames = new JLabel("", JLabel.CENTER);
				panelBasicAnalyze.add(labelNegativeGames);
				labelTotalScore = new JLabel("", JLabel.CENTER);
				panelBasicAnalyze.add(labelTotalScore);
				labelMaxTotal = new JLabel("", JLabel.CENTER);
				panelBasicAnalyze.add(labelMaxTotal);

				panelBasicAnalyze.add(new JLabel("", JLabel.CENTER));
				panelBasicAnalyze.add(new JLabel("Score min", JLabel.CENTER));
				panelBasicAnalyze.add(new JLabel("Positif %", JLabel.CENTER));
				panelBasicAnalyze.add(new JLabel("Négatif %", JLabel.CENTER));
				panelBasicAnalyze.add(new JLabel("Moyenne", JLabel.CENTER));
				panelBasicAnalyze.add(new JLabel("Total min", JLabel.CENTER));

				panelBasicAnalyze.add(new JLabel("", JLabel.CENTER));
				labelMinScore = new JLabel("", JLabel.CENTER);
				panelBasicAnalyze.add(labelMinScore);
				labelPositivePercentage = new JLabel("", JLabel.CENTER);
				panelBasicAnalyze.add(labelPositivePercentage);
				labelNegativePercentage = new JLabel("", JLabel.CENTER);
				panelBasicAnalyze.add(labelNegativePercentage);
				labelMeanScore = new JLabel("", JLabel.CENTER);
				panelBasicAnalyze.add(labelMeanScore);
				labelMinTotal = new JLabel("", JLabel.CENTER);
				panelBasicAnalyze.add(labelMinTotal);
			}

			{
				final JPanel panelPlaceAnalyze = new JPanel();
				panelPlaceAnalyze.setBorder(BorderFactory.createLoweredBevelBorder());
				panelPlaceAnalyze.setLayout(new GridLayout(3, 6));
				panelSouth.add(panelPlaceAnalyze);

				panelPlaceAnalyze.add(new JLabel("Nombre de parties", JLabel.CENTER));
				panelPlaceAnalyze.add(new JLabel("1er", JLabel.CENTER));
				panelPlaceAnalyze.add(new JLabel("2ème", JLabel.CENTER));
				panelPlaceAnalyze.add(new JLabel("3ème", JLabel.CENTER));
				panelPlaceAnalyze.add(new JLabel("4ème", JLabel.CENTER));
				panelPlaceAnalyze.add(new JLabel("-", JLabel.CENTER));

				labelNumberOfFourPlayersGames = new JLabel("", JLabel.CENTER);
				panelPlaceAnalyze.add(labelNumberOfFourPlayersGames);
				labelFourPlayersGamePlaces = new JLabel[4];
				for (int labelIndex = 0; labelIndex < 4; labelIndex++) {
					labelFourPlayersGamePlaces[labelIndex] = new JLabel("", JLabel.CENTER);
					panelPlaceAnalyze.add(labelFourPlayersGamePlaces[labelIndex]);
				}
				panelPlaceAnalyze.add(new JLabel("-", JLabel.CENTER));

				panelPlaceAnalyze.add(new JLabel());
				labelFourPlayersGamePlacesPercent = new JLabel[5];
				for (int labelIndex = 0; labelIndex < 4; labelIndex++) {
					labelFourPlayersGamePlacesPercent[labelIndex] = new JLabel("", JLabel.CENTER);
					panelPlaceAnalyze.add(labelFourPlayersGamePlacesPercent[labelIndex]);
				}
				panelPlaceAnalyze.add(new JLabel("-", JLabel.CENTER));
			}

			{
				final JPanel panelPlaceAnalyze = new JPanel();
				panelPlaceAnalyze.setBorder(BorderFactory.createLoweredBevelBorder());
				panelPlaceAnalyze.setLayout(new GridLayout(3, 5));
				panelSouth.add(panelPlaceAnalyze);

				panelPlaceAnalyze.add(new JLabel("Nombre de parties", JLabel.CENTER));
				panelPlaceAnalyze.add(new JLabel("1er", JLabel.CENTER));
				panelPlaceAnalyze.add(new JLabel("2ème", JLabel.CENTER));
				panelPlaceAnalyze.add(new JLabel("3ème", JLabel.CENTER));
				panelPlaceAnalyze.add(new JLabel("4ème", JLabel.CENTER));
				panelPlaceAnalyze.add(new JLabel("5ème", JLabel.CENTER));

				labelNumberOfFivePlayersGames = new JLabel("", JLabel.CENTER);
				panelPlaceAnalyze.add(labelNumberOfFivePlayersGames);
				labelFivePlayersGamePlaces = new JLabel[5];
				for (int labelIndex = 0; labelIndex < 5; labelIndex++) {
					labelFivePlayersGamePlaces[labelIndex] = new JLabel("", JLabel.CENTER);
					panelPlaceAnalyze.add(labelFivePlayersGamePlaces[labelIndex]);
				}

				panelPlaceAnalyze.add(new JLabel());
				labelFivePlayersGamePlacesPercent = new JLabel[5];
				for (int labelIndex = 0; labelIndex < 5; labelIndex++) {
					labelFivePlayersGamePlacesPercent[labelIndex] = new JLabel("", JLabel.CENTER);
					panelPlaceAnalyze.add(labelFivePlayersGamePlacesPercent[labelIndex]);
				}
			}
		}

		listPlayers = new ArrayList<Player>();
		listTournament = new ArrayList<Tournament>();

		tournamentComboBoxActionListener = new TournamentComboActionListener();
		comboActionListener = new ComboBoxActionListener();
		comboPeriodMode.addActionListener(comboActionListener);
		comboScoreMode.addActionListener(comboActionListener);
		comboTrimester.addActionListener(comboActionListener);
		comboMonth.addActionListener(comboActionListener);

		comboBoxActivated = new boolean[COMBOBOX_NUMBER];
		changePeriodParameters();
	}

	@Override
	public String getTabName() {
		return "RCR Analyze";
	}

	@Override
	public void setDisplayFullName(final boolean displayFullName, final boolean toRefresh) {
		this.displayFullName = displayFullName;
		if (toRefresh) {
			refresh();
		}
	}

	@Override
	public void refresh() {
		comboPlayerNames.removeActionListener(comboActionListener);
		comboPlayerNames.removeAllItems();
		listPlayers.clear();
		listPlayers.addAll(dataAccess.getRCRPlayers());
		if (displayFullName) {
			Collections.sort(listPlayers, new ComparatorAscendingPlayerName());
			for (int index = 0; index < listPlayers.size(); index++) {
				comboPlayerNames.addItem(listPlayers.get(index).getPlayerName());
			}
		} else {
			Collections.sort(listPlayers, new ComparatorAscendingPlayerDisplayName());
			for (int index = 0; index < listPlayers.size(); index++) {
				comboPlayerNames.addItem(listPlayers.get(index).getDisplayName());
			}
		}
		if (listPlayers.size() > 0) {
			comboPlayerNames.setSelectedIndex(0);
		}
		comboPlayerNames.addActionListener(comboActionListener);

		listTournament.clear();
		listTournament.addAll(dataAccess.getRCRTournaments());
		if (listTournament.size() > 0) {
			Collections.sort(listTournament, new ComparatorDescendingTournamentID());

			comboTournament.removeActionListener(tournamentComboBoxActionListener);
			comboTournament.removeAllItems();
			for (int index = 0; index < listTournament.size(); index++) {
				final Tournament tournament = listTournament.get(index);
				comboTournament.addItem(tournament.getName());
			}
			comboTournament.addActionListener(tournamentComboBoxActionListener);
			if (listTournament.size() > 0) {
				comboTournament.setSelectedIndex(0);
			}
		}
	}

	private class TournamentComboActionListener implements ActionListener {
		@Override
		public void actionPerformed(final ActionEvent e) {
			refreshYear();
		}
	}

	private void refreshYear() {
		final int selectedTournamentIndex = comboTournament.getSelectedIndex();
		if (listTournament.size() > 0 && selectedTournamentIndex >= 0) {
			comboYear.removeActionListener(comboActionListener);
			comboYear.removeAllItems();

			final Tournament tournament = listTournament.get(selectedTournamentIndex);
			final List<Integer> years = new ArrayList<Integer>(dataAccess.getRCRYears(tournament));
			Collections.sort(years);
			Collections.reverse(years);
			for (int index = 0; index < years.size(); index++) {
				comboYear.addItem(years.get(index));
			}

			comboYear.addActionListener(comboActionListener);
			if (years.size() > 0) {
				comboYear.setSelectedIndex(0);
			} else {
				comboYear.setSelectedIndex(-1);
			}
		}
	}

	private class ComboBoxActionListener implements ActionListener {
		@Override
		public void actionPerformed(final ActionEvent e) {
			changePeriodParameters();
			display();
		}
	}

	private void changePeriodParameters() {
		final EnumPeriodMode periodMode = periodModes[comboPeriodMode.getSelectedIndex()];
		switch (periodMode) {
			case ALL:
				comboBoxActivated[COMBOBOX_YEAR_INDEX] = false;
				comboBoxActivated[COMBOBOX_TRIMESTER_INDEX] = false;
				comboBoxActivated[COMBOBOX_MONTH_INDEX] = false;
				break;
			case YEAR:
				comboBoxActivated[COMBOBOX_YEAR_INDEX] = true;
				comboBoxActivated[COMBOBOX_TRIMESTER_INDEX] = false;
				comboBoxActivated[COMBOBOX_MONTH_INDEX] = false;
				break;
			case TRIMESTER:
				comboBoxActivated[COMBOBOX_YEAR_INDEX] = true;
				comboBoxActivated[COMBOBOX_TRIMESTER_INDEX] = true;
				comboBoxActivated[COMBOBOX_MONTH_INDEX] = false;
				break;
			case MONTH:
				comboBoxActivated[COMBOBOX_YEAR_INDEX] = true;
				comboBoxActivated[COMBOBOX_TRIMESTER_INDEX] = false;
				comboBoxActivated[COMBOBOX_MONTH_INDEX] = true;
				break;
			default:
				break;
		}
		enableComboBoxes();
	}

	private void disableComboBoxes() {
		comboPlayerNames.setEnabled(false);
		comboTournament.setEnabled(false);
		comboScoreMode.setEnabled(false);
		comboPeriodMode.setEnabled(false);
		comboYear.setEnabled(false);
		comboTrimester.setEnabled(false);
		comboMonth.setEnabled(false);
	}

	private void enableComboBoxes() {
		comboPlayerNames.setEnabled(true);
		comboTournament.setEnabled(true);
		comboScoreMode.setEnabled(true);
		comboPeriodMode.setEnabled(true);
		comboYear.setEnabled(comboBoxActivated[COMBOBOX_YEAR_INDEX]);
		comboTrimester.setEnabled(comboBoxActivated[COMBOBOX_TRIMESTER_INDEX]);
		comboMonth.setEnabled(comboBoxActivated[COMBOBOX_MONTH_INDEX]);
	}

	private void display() {
		disableComboBoxes();
		panelBarChart.removeAll();
		panelLineChart.removeAll();
		validate();
		repaint();
		new DisplayThread().start();
	}

	private class DisplayThread extends Thread {
		@Override
		public void run() {
			final int selectedPlayerIndex = comboPlayerNames.getSelectedIndex();
			final int selectedTournamentIndex = comboTournament.getSelectedIndex();
			final int selectedYearIndex = comboYear.getSelectedIndex();

			if (selectedPlayerIndex != -1 && selectedTournamentIndex != -1 && selectedYearIndex != -1) {
				final Player player = listPlayers.get(selectedPlayerIndex);
				final Tournament tournament = listTournament.get(selectedTournamentIndex);
				final EnumPeriodMode periodMode = periodModes[comboPeriodMode.getSelectedIndex()];
				final EnumScoreMode scoreMode = scoreModes[comboScoreMode.getSelectedIndex()];
				final int year = (Integer) comboYear.getSelectedItem();
				final int trimester = comboTrimester.getSelectedIndex();
				final int month = comboMonth.getSelectedIndex();
				final RCRDataPackageAnalyze dataPackage = dataAccess.getRCRDataPackageAnalyze(tournament, player.getPlayerID(), scoreMode, periodMode, year, trimester, month);

				if (dataPackage != null && dataPackage.getNumberOfGames() > 0) {
					final int numberOfGames = dataPackage.getNumberOfGames();

					// Stats
					{
						labelNumberOfGames.setText(format.format(dataPackage.getNumberOfGames()));
						labelMaxScore.setText(format.format(dataPackage.getScoreMax()));
						labelMinScore.setText(format.format(dataPackage.getScoreMin()));
						labelPositiveGames.setText(format.format(dataPackage.getPositiveGames()));
						labelPositivePercentage.setText(format.format(dataPackage.getPositiveGamesPercent()) + PERCENTAGE_STRING);
						labelNegativeGames.setText(format.format(dataPackage.getNegativeGames()));
						labelNegativePercentage.setText(format.format(dataPackage.getNegativeGamesPercent()) + PERCENTAGE_STRING);
						labelTotalScore.setText(format.format(dataPackage.getScoreTotal()));
						labelMeanScore.setText(format.format(dataPackage.getScoreMean()) + PLUS_MINUS + format.format(dataPackage.getScoreStandardDeviation()));
						labelMaxTotal.setText(format.format(dataPackage.getTotalMax()));
						labelMinTotal.setText(format.format(dataPackage.getTotalMin()));

						labelNumberOfFourPlayersGames.setText(format.format(dataPackage.getNumberOfFourPlayerGames()));
						if (dataPackage.getNumberOfFourPlayerGames() > 0) {
							for (int index = 0; index < 4; index++) {
								labelFourPlayersGamePlaces[index].setText(format.format(dataPackage.getFourPlayerGamePlaces()[index]));
								labelFourPlayersGamePlacesPercent[index].setText(format.format(dataPackage.getFourPlayerGamePlacePercent()[index]) + PERCENTAGE_STRING);
							}
						} else {
							for (int index = 0; index < 4; index++) {
								labelFourPlayersGamePlaces[index].setText(ZERO_STRING);
								labelFourPlayersGamePlacesPercent[index].setText(ZERO_STRING + PERCENTAGE_STRING);
							}
						}

						labelNumberOfFivePlayersGames.setText(format.format(dataPackage.getNumberOfFivePlayerGames()));
						if (dataPackage.getNumberOfFivePlayerGames() > 0) {
							for (int index = 0; index < 5; index++) {
								labelFivePlayersGamePlaces[index].setText(format.format(dataPackage.getFivePlayerGamePlaces()[index]));
								labelFivePlayersGamePlacesPercent[index].setText(format.format(dataPackage.getFivePlayerGamePlacePercent()[index]) + PERCENTAGE_STRING);
							}
						} else {
							for (int index = 0; index < 5; index++) {
								labelFivePlayersGamePlaces[index].setText(ZERO_STRING);
								labelFivePlayersGamePlacesPercent[index].setText(ZERO_STRING + PERCENTAGE_STRING);
							}
						}
					}

					// Charts
					{
						int tickUnit;
						if (numberOfGames < MAX_NUMBER_OF_TICKS) {
							tickUnit = 1;
						} else {
							tickUnit = (numberOfGames / (MAX_NUMBER_OF_TICKS * TICK_UNIT_MULTIPLE) + 1) * TICK_UNIT_MULTIPLE;
						}
						// Bar Chart
						{
							final XYSeries scoreSeries = new XYSeries("Score");
							final List<Integer> listScore = dataPackage.getListScore();
							final List<Integer> listGameID = dataPackage.getListGameID();
							final List<String> listToolTipText = new ArrayList<String>();
							for (int index = 0; index < numberOfGames; index++) {
								scoreSeries.add(index + 1, listScore.get(index));
								listToolTipText.add(index, "<html>Score : " + Integer.toString(listScore.get(index)) + "<br>ID : " + Integer.toString(listGameID.get(index)) + "</html>");
							}
							final CustomXYToolTipGenerator toolTip = new CustomXYToolTipGenerator();
							toolTip.addToolTipSeries(listToolTipText);
							final IntervalXYDataset scoreDateSet = new XYSeriesCollection(scoreSeries);
							final NumberAxis scoreDomainAxis = new NumberAxis();
							scoreDomainAxis.setRange(0, numberOfGames + 1);
							scoreDomainAxis.setTickUnit(new NumberTickUnit(tickUnit));
							scoreDomainAxis.setLowerMargin(0.0);
							scoreDomainAxis.setUpperMargin(0.0);
							final ValueAxis scoreRangeAxis = new NumberAxis("Score");
							final XYBarRenderer scoreRender = new XYBarRenderer(0.5);
							scoreRender.setSeriesPaint(0, Color.BLUE);
							scoreRender.setSeriesToolTipGenerator(0, toolTip);
							scoreRender.setShadowVisible(false);
							final XYPlot scorePlot = new XYPlot(scoreDateSet, scoreDomainAxis, scoreRangeAxis, scoreRender);
							scorePlot.setBackgroundPaint(new Color(255, 255, 255, 0));
							scorePlot.setDomainGridlinePaint(Color.BLACK);
							scorePlot.setRangeGridlinePaint(Color.BLACK);
							final double makerValue = scoreMode == EnumScoreMode.FINAL_SCORE ? 0.0 : 30000.0;
							final ValueMarker marker = new ValueMarker(makerValue, Color.RED, new BasicStroke(1), null, null, 1.0f);
							scorePlot.addRangeMarker(marker);

							final ChartPanel chartPanel = new ChartPanel(new JFreeChart(scorePlot));
							chartPanel.setPopupMenu(null);
							chartPanel.setMouseZoomable(false);
							panelBarChart.add(chartPanel, BorderLayout.CENTER);
						}
						// Line Chart
						if (scoreMode == EnumScoreMode.FINAL_SCORE) {
							final XYSeries sumSeries = new XYSeries("Score total");
							sumSeries.add(0, 0);
							final List<Integer> listSum = dataPackage.getListSum();
							final List<String> listToolTipText = new ArrayList<String>();
							for (int index = 0; index < numberOfGames; index++) {
								sumSeries.add(index + 1, listSum.get(index));
								listToolTipText.add(index, Integer.toString(listSum.get(index)));
							}
							final CustomXYToolTipGenerator toolTip = new CustomXYToolTipGenerator();
							toolTip.addToolTipSeries(listToolTipText);
							final IntervalXYDataset sumDataSet = new XYSeriesCollection(sumSeries);
							final NumberAxis sumDomainAxis = new NumberAxis();
							sumDomainAxis.setRange(0, numberOfGames + 1);
							sumDomainAxis.setTickUnit(new NumberTickUnit(tickUnit));
							sumDomainAxis.setLowerMargin(0.0);
							sumDomainAxis.setUpperMargin(0.0);
							final NumberAxis sumRangeAxis = new NumberAxis("Score total");
							final XYLineAndShapeRenderer sumRender = new XYLineAndShapeRenderer();
							sumRender.setSeriesPaint(0, Color.BLUE);
							sumRender.setSeriesToolTipGenerator(0, toolTip);
							sumRender.setSeriesShapesVisible(0, false);
							final XYPlot sumPlot = new XYPlot(sumDataSet, sumDomainAxis, sumRangeAxis, sumRender);
							sumPlot.setBackgroundPaint(new Color(255, 255, 255, 0));
							sumPlot.setDomainGridlinePaint(Color.BLACK);
							sumPlot.setRangeGridlinePaint(Color.BLACK);
							final ValueMarker marker = new ValueMarker(0.0, Color.RED, new BasicStroke(1), null, null, 1.0f);
							sumPlot.addRangeMarker(marker);

							final ChartPanel chartPanel = new ChartPanel(new JFreeChart(sumPlot));
							chartPanel.setPopupMenu(null);
							chartPanel.setMouseZoomable(false);
							panelLineChart.add(chartPanel, BorderLayout.CENTER);
						}
					}
				} else {
					clearGameInfo();
				}
			} else {
				clearGameInfo();
			}
			validate();
			enableComboBoxes();
			repaint();
		}
	}

	private void clearGameInfo() {
		labelNumberOfGames.setText(ZERO_STRING);
		labelTotalScore.setText(ZERO_STRING);
		labelMinScore.setText(ZERO_STRING);
		labelPositiveGames.setText(ZERO_STRING);
		labelPositivePercentage.setText(ZERO_STRING + PERCENTAGE_STRING);
		labelNegativeGames.setText(ZERO_STRING);
		labelNegativePercentage.setText(ZERO_STRING + PERCENTAGE_STRING);
		labelMeanScore.setText(ZERO_STRING);
		labelMaxScore.setText(ZERO_STRING);
		labelMaxTotal.setText(ZERO_STRING);
		labelMinTotal.setText(ZERO_STRING);

		labelNumberOfFourPlayersGames.setText(ZERO_STRING);
		for (int index = 0; index < 4; index++) {
			labelFourPlayersGamePlaces[index].setText(ZERO_STRING);
			labelFourPlayersGamePlacesPercent[index].setText(ZERO_STRING + PERCENTAGE_STRING);
		}

		labelNumberOfFivePlayersGames.setText(ZERO_STRING);
		for (int index = 0; index < 5; index++) {
			labelFivePlayersGamePlaces[index].setText(ZERO_STRING);
			labelFivePlayersGamePlacesPercent[index].setText(ZERO_STRING + PERCENTAGE_STRING);
		}
	}

	@Override
	public boolean canExport() {
		return false;
	}

	@Override
	public void export() {
	}
}
