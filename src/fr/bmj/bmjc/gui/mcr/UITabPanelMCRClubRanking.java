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
package fr.bmj.bmjc.gui.mcr;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import fr.bmj.bmjc.data.game.ComparatorDescendingTournamentID;
import fr.bmj.bmjc.data.game.Tournament;
import fr.bmj.bmjc.data.stat.mcr.FieldAccessMCR;
import fr.bmj.bmjc.data.stat.mcr.FieldAccessMCRScoreTotalDay;
import fr.bmj.bmjc.data.stat.mcr.FieldAccessMCRScoreTotalDisplayName;
import fr.bmj.bmjc.data.stat.mcr.FieldAccessMCRScoreTotalFinalScore;
import fr.bmj.bmjc.data.stat.mcr.FieldAccessMCRScoreTotalMeanScore;
import fr.bmj.bmjc.data.stat.mcr.FieldAccessMCRScoreTotalMonth;
import fr.bmj.bmjc.data.stat.mcr.FieldAccessMCRScoreTotalNumberOfGames;
import fr.bmj.bmjc.data.stat.mcr.FieldAccessMCRScoreTotalPlayName;
import fr.bmj.bmjc.data.stat.mcr.FieldAccessMCRScoreTotalTotalScore;
import fr.bmj.bmjc.data.stat.mcr.FieldAccessMCRScoreTotalTrimester;
import fr.bmj.bmjc.data.stat.mcr.FieldAccessMCRScoreTotalYear;
import fr.bmj.bmjc.data.stat.mcr.MCRScoreTotal;
import fr.bmj.bmjc.dataaccess.mcr.DataAccessMCR;
import fr.bmj.bmjc.enums.EnumPeriodMode;
import fr.bmj.bmjc.enums.EnumRankingMode;
import fr.bmj.bmjc.enums.EnumSortingMode;
import fr.bmj.bmjc.enums.EnumTrimester;
import fr.bmj.bmjc.gui.UITabPanel;
import fr.bmj.bmjc.swing.JDialogWithProgress;
import fr.bmj.bmjc.swing.ProportionalGridLayout;
import fr.bmj.bmjc.swing.ProportionalGridLayoutConstraint;

public class UITabPanelMCRClubRanking extends UITabPanel {
	private static final long serialVersionUID = 862214639563775184L;

	private final int NB_COLUMNS = 4;
	private final int COLUMN_WIDTH[] = new int[] { 80, 144, 96, 112 };
	private final int LABEL_HEIGHT = 18;

	private static final int COMBOBOX_NUMBER = 4;
	private static final int COMBOBOX_PERIOD = 0;
	private static final int COMBOBOX_YEAR_INDEX = 1;
	private static final int COMBOBOX_TRIMESTER_INDEX = 2;
	private static final int COMBOBOX_MONTH_INDEX = 3;

	private boolean displayFullName;
	private final DataAccessMCR dataAccess;
	private final JDialogWithProgress waitingDialog;
	private final ComponentAdapter waitingDialogAdapter;

	private final EnumRankingMode rankingModes[];
	private final JComboBox<String> comboRankingMode;
	private final JComboBox<String> comboTournament;
	private final EnumSortingMode sortingModes[];
	private final JComboBox<String> comboSortingMode;

	private final EnumPeriodMode periodModes[];
	private final JComboBox<String> comboPeriodMode;
	private final JComboBox<Integer> comboYear;
	private final JComboBox<String> comboTrimester;
	private final JComboBox<String> comboMonth;

	private final boolean comboBoxActivated[];

	private final ActionListener tournamentComboBoxActionListener;
	private final ActionListener periodParametersComboBoxActionListener;

	private final JPanel panelRanking;
	private final JScrollPane scrollRanking;
	private final JLabel labelTitles[];
	private final Dimension labelSizes[];

	private final List<Tournament> listTournament;

	public UITabPanelMCRClubRanking(final DataAccessMCR dataAccess, final JDialogWithProgress waitingDialog) {
		this.dataAccess = dataAccess;
		this.waitingDialog = waitingDialog;
		waitingDialogAdapter = new WaitingDialogComponentListener();

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
				panelNorth.add(new JLabel("Classement :", JLabel.RIGHT), c);
				rankingModes = EnumRankingMode.values();
				final String rankingModeStrings[] = new String[rankingModes.length];
				for (int index = 0; index < rankingModes.length; index++) {
					rankingModeStrings[index] = rankingModes[index].toString();
				}
				comboRankingMode = new JComboBox<String>(rankingModeStrings);
				comboRankingMode.setEditable(false);
				comboRankingMode.setSelectedIndex(0);
				c.x = 1;
				panelNorth.add(comboRankingMode, c);

				c.x = 2;
				panelNorth.add(new JLabel("Tournoi :", JLabel.RIGHT), c);
				comboTournament = new JComboBox<String>();
				comboTournament.setEditable(false);
				c.x = 3;
				c.gridWidth = 3;
				panelNorth.add(comboTournament, c);

				c.x = 6;
				c.gridWidth = 1;
				panelNorth.add(new JLabel("Ordre :", JLabel.RIGHT), c);
				sortingModes = EnumSortingMode.values();
				final String sortingModeStrings[] = new String[sortingModes.length];
				for (int index = 0; index < sortingModes.length; index++) {
					sortingModeStrings[index] = sortingModes[index].toString();
				}
				comboSortingMode = new JComboBox<>(sortingModeStrings);
				comboSortingMode.setEditable(false);
				comboSortingMode.setSelectedIndex(0);
				c.x = 7;
				panelNorth.add(comboSortingMode, c);

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
			panelCenter.setLayout(new BorderLayout());

			{
				final JPanel panelCenterTitleSupport = new JPanel();
				panelCenterTitleSupport.setLayout(new BorderLayout());

				final JPanel panelCenterTitle = new JPanel();
				panelCenterTitle.setLayout(new GridBagLayout());
				final GridBagConstraints titleConstraints = new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 64, 2);

				labelTitles = new JLabel[NB_COLUMNS];
				labelSizes = new Dimension[NB_COLUMNS];
				for (int columnIndex = 0; columnIndex < NB_COLUMNS; columnIndex++) {
					titleConstraints.gridx = columnIndex;
					labelSizes[columnIndex] = new Dimension(COLUMN_WIDTH[columnIndex], LABEL_HEIGHT);
					labelTitles[columnIndex] = new JLabel("", JLabel.CENTER);
					labelTitles[columnIndex].setPreferredSize(labelSizes[columnIndex]);
					panelCenterTitle.add(labelTitles[columnIndex], titleConstraints);
				}
				panelCenterTitleSupport.add(panelCenterTitle, BorderLayout.CENTER);

				final JLabel spacer = new JLabel();
				spacer.setPreferredSize(new Dimension(16, LABEL_HEIGHT));
				panelCenterTitleSupport.add(spacer, BorderLayout.EAST);

				panelCenter.add(panelCenterTitleSupport, BorderLayout.NORTH);
			}

			{
				final JPanel panelCenterSupport = new JPanel();
				panelCenterSupport.setLayout(new GridBagLayout());

				final GridBagConstraints constraints = new GridBagConstraints();
				constraints.gridx = 0;
				constraints.gridy = 0;
				constraints.gridwidth = 1;
				constraints.gridheight = 1;
				constraints.weightx = 1.0;
				constraints.weighty = 1.0;
				constraints.anchor = GridBagConstraints.NORTH;
				constraints.fill = GridBagConstraints.HORIZONTAL;

				panelRanking = new JPanel();
				panelRanking.setLayout(new GridBagLayout());
				panelCenterSupport.add(panelRanking, constraints);

				scrollRanking = new JScrollPane(panelCenterSupport, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
				scrollRanking.getVerticalScrollBar().setUnitIncrement(LABEL_HEIGHT + 2);
				panelCenter.add(scrollRanking, BorderLayout.CENTER);
			}

			add(panelCenter, BorderLayout.CENTER);
		}

		listTournament = new ArrayList<Tournament>();
		displayFullName = false;

		tournamentComboBoxActionListener = new TournamentComboActionListener();
		comboPeriodMode.addActionListener(new PeriodModeComboBoxActionListener());
		periodParametersComboBoxActionListener = new PeriodParametersComboBoxActionListener();
		comboTrimester.addActionListener(periodParametersComboBoxActionListener);
		comboMonth.addActionListener(periodParametersComboBoxActionListener);

		final ActionListener rankingModeActionListener = new RankingModeComboBoxActionListener();
		comboSortingMode.addActionListener(rankingModeActionListener);
		comboRankingMode.addActionListener(rankingModeActionListener);
		comboBoxActivated = new boolean[COMBOBOX_NUMBER];
		togglePeriodMode();
	}

	@Override
	public String getTabName() {
		return "MCR Classement";
	}

	@Override
	public void setDisplayFullName(final boolean displayFullName, final boolean toRefresh) {
		this.displayFullName = displayFullName;
		if (toRefresh) {
			display();
		}
	}

	@Override
	public void refresh() {
		listTournament.clear();
		final List<Tournament> newTournaments = dataAccess.getMCRTournaments();
		if (newTournaments.size() > 0) {
			listTournament.addAll(newTournaments);
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
			comboYear.removeActionListener(periodParametersComboBoxActionListener);
			comboYear.removeAllItems();

			final Tournament tournament = listTournament.get(selectedTournamentIndex);
			final List<Integer> years = new ArrayList<Integer>(dataAccess.getMCRYears(tournament));
			Collections.sort(years);
			Collections.reverse(years);
			for (int index = 0; index < years.size(); index++) {
				comboYear.addItem(years.get(index));
			}

			comboYear.addActionListener(periodParametersComboBoxActionListener);
			if (years.size() > 0) {
				comboYear.setSelectedIndex(0);
			} else {
				comboYear.setSelectedIndex(-1);
			}
		}
	}

	private class RankingModeComboBoxActionListener implements ActionListener {
		@Override
		public void actionPerformed(final ActionEvent e) {
			togglePeriodMode();
			display();
		}
	}

	private void togglePeriodMode() {
		final EnumRankingMode rankingMode = rankingModes[comboRankingMode.getSelectedIndex()];
		switch (rankingMode) {
			case TOTAL_SCORE:
			case FINAL_SCORE:
			case MEAN_FINAL_SCORE:
			case GAME_SCORE:
			case MEAN_GAME_SCORE:
				comboBoxActivated[COMBOBOX_PERIOD] = true;
				comboPeriodMode.setEnabled(true);
				changePeriodParameters();
				break;
			case ANNUAL_SCORE:
			case TRIMESTRIAL_SCORE:
			case MENSUAL_SCORE:
				comboBoxActivated[COMBOBOX_PERIOD] = false;
				comboPeriodMode.setEnabled(false);
				changePeriodParameters();
				break;
			default:
				break;
		}
	}

	private class PeriodModeComboBoxActionListener implements ActionListener {
		@Override
		public void actionPerformed(final ActionEvent e) {
			changePeriodParameters();
			display();
		}
	}

	private void changePeriodParameters() {
		final EnumPeriodMode periodMode = comboBoxActivated[COMBOBOX_PERIOD] ? periodModes[comboPeriodMode.getSelectedIndex()] : EnumPeriodMode.ALL;
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
		comboRankingMode.setEnabled(false);
		comboTournament.setEnabled(false);
		comboSortingMode.setEnabled(false);
		comboPeriodMode.setEnabled(false);
		comboYear.setEnabled(false);
		comboTrimester.setEnabled(false);
		comboMonth.setEnabled(false);
	}

	private void enableComboBoxes() {
		comboRankingMode.setEnabled(true);
		comboTournament.setEnabled(true);
		comboSortingMode.setEnabled(true);
		comboPeriodMode.setEnabled(comboBoxActivated[COMBOBOX_PERIOD]);
		comboYear.setEnabled(comboBoxActivated[COMBOBOX_YEAR_INDEX]);
		comboTrimester.setEnabled(comboBoxActivated[COMBOBOX_TRIMESTER_INDEX]);
		comboMonth.setEnabled(comboBoxActivated[COMBOBOX_MONTH_INDEX]);
	}

	private class PeriodParametersComboBoxActionListener implements ActionListener {
		@Override
		public void actionPerformed(final ActionEvent e) {
			display();
		}
	}

	private void display() {
		disableComboBoxes();
		panelRanking.removeAll();
		validate();
		repaint();

		final Point location = getLocationOnScreen();
		final Dimension size = getSize();
		waitingDialog.setLocation(location.x + (size.width - waitingDialog.getWidth()) / 2, location.y + (size.height - waitingDialog.getHeight()) / 2);
		waitingDialog.addComponentListener(waitingDialogAdapter);
		waitingDialog.setVisible(true);
	}

	private class WaitingDialogComponentListener extends ComponentAdapter {
		@Override
		public void componentShown(final ComponentEvent e) {
			new DisplayThread().start();
		}
	}

	private class DisplayThread extends Thread {
		@Override
		public void run() {
			final EnumRankingMode rankingMode = rankingModes[comboRankingMode.getSelectedIndex()];
			final EnumSortingMode sortingMode = sortingModes[comboSortingMode.getSelectedIndex()];
			final EnumPeriodMode periodMode = periodModes[comboPeriodMode.getSelectedIndex()];

			final int selectedTournamentIndex = comboTournament.getSelectedIndex();
			final int selectedYearIndex = comboYear.getSelectedIndex();
			if (selectedTournamentIndex != -1 && selectedYearIndex != -1) {
				final Tournament tournament = listTournament.get(selectedTournamentIndex);
				final int year = (Integer) comboYear.getSelectedItem();
				final int trimestral = comboTrimester.getSelectedIndex();
				final int month = comboMonth.getSelectedIndex();
				final List<MCRScoreTotal> scoreList = dataAccess.getMCRDataPackageRanking(tournament, rankingMode, sortingMode, periodMode, year, trimestral, month);

				if (scoreList != null && scoreList.size() > 0) {
					labelTitles[0].setText("Classement");
					labelTitles[1].setText("Nom du joueur");
					final List<FieldAccessMCR> access = new ArrayList<>(3);
					access.add(0, null);
					if (displayFullName) {
						access.add(1, new FieldAccessMCRScoreTotalPlayName());
					} else {
						access.add(1, new FieldAccessMCRScoreTotalDisplayName());
					}
					switch (rankingMode) {
						case TOTAL_SCORE:
							labelTitles[2].setText(rankingMode.toString());
							labelTitles[3].setText("Nombre de parties");
							access.add(2, new FieldAccessMCRScoreTotalTotalScore());
							access.add(3, new FieldAccessMCRScoreTotalNumberOfGames());
							break;
						case FINAL_SCORE:
							labelTitles[2].setText(rankingMode.toString() + " (Uma)");
							labelTitles[3].setText("Date");
							access.add(2, new FieldAccessMCRScoreTotalFinalScore());
							access.add(3, new FieldAccessMCRScoreTotalDay());
							break;
						case MEAN_FINAL_SCORE:
							labelTitles[2].setText(rankingMode.toString() + " (Écart type)");
							labelTitles[3].setText("Nombre de parties");
							access.add(2, new FieldAccessMCRScoreTotalFinalScore());
							access.add(3, new FieldAccessMCRScoreTotalNumberOfGames());
							break;
						case GAME_SCORE:
							labelTitles[2].setText(rankingMode.toString());
							labelTitles[3].setText("Date");
							access.add(2, new FieldAccessMCRScoreTotalTotalScore());
							access.add(3, new FieldAccessMCRScoreTotalDay());
							break;
						case MEAN_GAME_SCORE:
							labelTitles[2].setText(rankingMode.toString() + " (Écart type)");
							labelTitles[3].setText("Nombre de parties");
							access.add(2, new FieldAccessMCRScoreTotalMeanScore());
							access.add(3, new FieldAccessMCRScoreTotalNumberOfGames());
							break;
						case ANNUAL_SCORE:
							labelTitles[2].setText(rankingMode.toString());
							labelTitles[3].setText("Date");
							access.add(2, new FieldAccessMCRScoreTotalTotalScore());
							access.add(3, new FieldAccessMCRScoreTotalYear());
							break;
						case TRIMESTRIAL_SCORE:
							labelTitles[2].setText(rankingMode.toString());
							labelTitles[3].setText("Date");
							access.add(2, new FieldAccessMCRScoreTotalTotalScore());
							access.add(3, new FieldAccessMCRScoreTotalTrimester());
							break;
						case MENSUAL_SCORE:
							labelTitles[2].setText(rankingMode.toString());
							labelTitles[3].setText("Date");
							access.add(2, new FieldAccessMCRScoreTotalTotalScore());
							access.add(3, new FieldAccessMCRScoreTotalMonth());
							break;
						default:
							break;
					}

					data = new String[scoreList.size()][NB_COLUMNS];
					final JLabel labels[] = new JLabel[NB_COLUMNS];
					final GridBagConstraints constraints = new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 64, 2);
					for (int index = 0; index < scoreList.size(); index++) {
						final MCRScoreTotal record = scoreList.get(index);
						data[index][0] = Integer.toString(index + 1);
						for (int labelIndex = 1; labelIndex < labels.length; labelIndex++) {
							data[index][labelIndex] = access.get(labelIndex).getDataString(record);
						}

						constraints.gridy = index;
						for (int labelIndex = 0; labelIndex < labels.length; labelIndex++) {
							labels[labelIndex] = new JLabel(data[index][labelIndex], labelIndex == 1 ? JLabel.LEADING : JLabel.CENTER);
							constraints.gridx = labelIndex;
							labels[labelIndex].setPreferredSize(labelSizes[labelIndex]);
							panelRanking.add(labels[labelIndex], constraints);
						}

						if (index % 2 == 0) {
							for (int labelIndex = 0; labelIndex < labels.length; labelIndex++) {
								labels[labelIndex].setOpaque(true);
								labels[labelIndex].setBackground(Color.LIGHT_GRAY);
							}
						}
					}
				} else {
					data = null;
				}
			}
			validate();
			scrollRanking.getVerticalScrollBar().setValue(0);
			enableComboBoxes();
			repaint();

			waitingDialog.removeComponentListener(waitingDialogAdapter);
			waitingDialog.setVisible(false);
		}
	}

	@Override
	public boolean canExport() {
		return true;
	}

	private String data[][];

	@Override
	public void export() {
		final EnumRankingMode rankingMode = rankingModes[comboRankingMode.getSelectedIndex()];
		final EnumSortingMode sortingMode = sortingModes[comboSortingMode.getSelectedIndex()];
		final EnumPeriodMode periodMode = periodModes[comboPeriodMode.getSelectedIndex()];

		final int selectedTournamentIndex = comboTournament.getSelectedIndex();
		final int selectedYearIndex = comboYear.getSelectedIndex();
		if (selectedTournamentIndex != -1 && selectedYearIndex != -1) {
			final Tournament tournament = listTournament.get(selectedTournamentIndex);
			final int year = (Integer) comboYear.getSelectedItem();

			if (data != null && data.length > 0) {
				final StringBuffer proposedSaveFileName = new StringBuffer();
				proposedSaveFileName.append(tournament.getName());
				proposedSaveFileName.append("_");
				proposedSaveFileName.append(rankingMode.toString());
				proposedSaveFileName.append("_");
				proposedSaveFileName.append(sortingMode.toString());
				proposedSaveFileName.append("_");
				proposedSaveFileName.append(periodMode.toString());
				proposedSaveFileName.append("_");
				switch (periodMode) {
					case ALL:
						break;
					case YEAR:
						proposedSaveFileName.append(Integer.toString(year));
						break;
					case TRIMESTER:
						proposedSaveFileName.append(Integer.toString(year));
						proposedSaveFileName.append("_");
						proposedSaveFileName.append(comboTrimester.getSelectedItem().toString());
						break;
					case MONTH:
						proposedSaveFileName.append(Integer.toString(year));
						proposedSaveFileName.append("_");
						proposedSaveFileName.append(comboMonth.getSelectedItem().toString());
						break;
					default:
						break;
				}
				proposedSaveFileName.append(".csv");
				final File fileSaveFile = askSaveFileName(proposedSaveFileName.toString());
				if (fileSaveFile != null) {
					BufferedWriter writer = null;
					try {
						writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileSaveFile), Charset.forName("UTF-8")));
						writer.write("Classement");
						writer.write(SEPARATOR);
						writer.write(rankingMode.toString());
						writer.newLine();

						writer.write("Tournois");
						writer.write(SEPARATOR);
						writer.write(tournament.getName());
						writer.newLine();

						writer.write("Ordre");
						writer.write(SEPARATOR);
						writer.write(sortingMode.toString());
						writer.newLine();

						writer.write("Période mode");
						writer.write(SEPARATOR);
						writer.write(periodMode.toString());
						writer.newLine();

						writer.write("Période");
						writer.write(SEPARATOR);
						switch (periodMode) {
							case ALL:
								writer.write("Tout");
								break;
							case YEAR:
								writer.write(Integer.toString(year));
								break;
							case TRIMESTER:
								writer.write(comboTrimester.getSelectedItem().toString());
								writer.write(" ");
								writer.write(Integer.toString(year));
								break;
							case MONTH:
								writer.write(comboMonth.getSelectedItem().toString());
								writer.write(" ");
								writer.write(Integer.toString(year));
								break;
							default:
								break;
						}
						writer.newLine();

						writer.write(labelTitles[0].getText());
						for (int fieldIndex = 1; fieldIndex < labelTitles.length; fieldIndex++) {
							writer.write(SEPARATOR);
							writer.write(labelTitles[fieldIndex].getText());
						}
						writer.newLine();

						for (int index = 0; index < data.length; index++) {
							writer.write(data[index][0]);
							for (int fieldIndex = 1; fieldIndex < data[index].length; fieldIndex++) {
								writer.write(SEPARATOR);
								writer.write(data[index][fieldIndex]);
							}
							writer.newLine();
						}
					} catch (final Exception e) {
						JOptionPane.showMessageDialog(this, "Une erreur est survenue lors de sauvegarde.", "Erreur", JOptionPane.ERROR_MESSAGE);
					} finally {
						if (writer != null) {
							try {
								writer.close();
							} catch (final Exception e) {
							}
						}
					}
				}
			}
		}
	}
}
