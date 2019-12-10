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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.text.DateFormatSymbols;
import java.time.LocalDate;
import java.time.temporal.ChronoField;
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
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;

import fr.bmj.bmjc.data.game.ComparatorDescendingTournamentID;
import fr.bmj.bmjc.data.game.Tournament;
import fr.bmj.bmjc.data.stat.rcr.RCRDataPackageScoreAnalyze;
import fr.bmj.bmjc.dataaccess.rcr.DataAccessRCR;
import fr.bmj.bmjc.enums.EnumPeriodMode;
import fr.bmj.bmjc.enums.EnumTrimester;
import fr.bmj.bmjc.gui.UITabPanel;
import fr.bri.awt.ProportionalGridLayout;
import fr.bri.awt.ProportionalGridLayoutConstraint;

public class UITabPanelRCRScoreAnalyze extends UITabPanel {
	private static final long serialVersionUID = -2831122708393360423L;

	private static final int COMBOBOX_NUMBER = 4;
	private static final int COMBOBOX_YEAR_INDEX = 0;
	private static final int COMBOBOX_TRIMESTER_INDEX = 1;
	private static final int COMBOBOX_MONTH_INDEX = 2;
	private static final int COMBOBOX_DAY_INDEX = 3;

	private static final int LABEL_WIDTH = 96;
	private static final int LABEL_HEIGHT = 24;
	private static final int SCROLL_WIDTH = 16;

	private boolean displayFullName;
	private final DataAccessRCR dataAccess;

	private final JComboBox<String> comboTournament;

	private final EnumPeriodMode periodModes[];
	private final JComboBox<String> comboPeriodMode;
	private final JComboBox<Integer> comboYear;
	private final JComboBox<String> comboTrimester;
	private final JComboBox<String> comboMonth;
	private final JComboBox<Integer> comboDay;

	private final ActionListener tournamentComboBoxActionListener;
	private final ActionListener periodParametersComboBoxHighLevelActionListener;
	private final ActionListener periodParametersComboBoxLowLevelActionListener;

	private final boolean comboBoxActivated[];

	private final JPanel panelScore;
	private final JScrollPane scrollScore;
	private final JPanel panelTitleUpper;
	private final JScrollPane scrollTitleUpper;
	private final JPanel panelTitleLeft;
	private final JScrollPane scrollTitleLeft;

	private final List<Tournament> listTournament;

	public UITabPanelRCRScoreAnalyze(final DataAccessRCR dataAccess) {
		this.dataAccess = dataAccess;

		setLayout(new BorderLayout());
		{
			final JPanel panelNorth = new JPanel();
			final ProportionalGridLayout northLayout = new ProportionalGridLayout(2, 11, 8, 2);
			northLayout.setWeightX(4, 5, 3, 5, 3, 5, 3, 5, 3, 5, 2);
			panelNorth.setLayout(northLayout);
			panelNorth.setBorder(BorderFactory.createLoweredBevelBorder());
			add(panelNorth, BorderLayout.NORTH);
			final ProportionalGridLayoutConstraint c = new ProportionalGridLayoutConstraint(0, 1, 0, 1);
			{
				c.y = 0;
				c.x = 2;
				panelNorth.add(new JLabel("Tournoi :", SwingConstants.RIGHT), c);
				comboTournament = new JComboBox<String>();
				comboTournament.setEditable(false);
				c.x = 3;
				c.gridWidth = 5;
				panelNorth.add(comboTournament, c);

				c.y = 1;
				c.x = 0;
				c.gridWidth = 1;
				panelNorth.add(new JLabel("Période :", SwingConstants.RIGHT), c);
				periodModes = new EnumPeriodMode[] {
					EnumPeriodMode.ALL, EnumPeriodMode.SEASON, EnumPeriodMode.YEAR, EnumPeriodMode.TRIMESTER, EnumPeriodMode.MONTH, EnumPeriodMode.DAY
				};
				final String periodModeStrings[] = new String[periodModes.length];
				for (int index = 0; index < periodModes.length; index++) {
					periodModeStrings[index] = periodModes[index].toString();
				}
				comboPeriodMode = new JComboBox<String>(periodModeStrings);
				comboPeriodMode.setEditable(false);
				comboPeriodMode.setSelectedIndex(3);
				c.x = 1;
				panelNorth.add(comboPeriodMode, c);

				c.x = 2;
				panelNorth.add(new JLabel("Année :", SwingConstants.RIGHT), c);
				comboYear = new JComboBox<Integer>();
				comboYear.setEditable(false);
				c.x = 3;
				panelNorth.add(comboYear, c);

				c.x = 4;
				panelNorth.add(new JLabel("Trimestre :", SwingConstants.RIGHT), c);
				final String trimesters[] = {
					EnumTrimester.TRIMESTER_1.toString(), EnumTrimester.TRIMESTER_2.toString(), EnumTrimester.TRIMESTER_3.toString(),
					EnumTrimester.TRIMESTER_4.toString()
				};
				comboTrimester = new JComboBox<String>(trimesters);
				comboTrimester.setEditable(false);
				comboTrimester.setSelectedIndex((LocalDate.now().get(ChronoField.MONTH_OF_YEAR) - 1) / 3);
				c.x = 5;
				panelNorth.add(comboTrimester, c);

				c.x = 6;
				panelNorth.add(new JLabel("Mois :", SwingConstants.RIGHT), c);
				final String months[] = new String[12];
				System.arraycopy(DateFormatSymbols.getInstance(Locale.FRANCE).getMonths(), 0, months, 0, 12);
				comboMonth = new JComboBox<String>(months);
				comboMonth.setEditable(false);
				comboMonth.setSelectedIndex(0);
				c.x = 7;
				panelNorth.add(comboMonth, c);

				c.x = 8;
				panelNorth.add(new JLabel("Jour :", SwingConstants.RIGHT), c);
				comboDay = new JComboBox<Integer>();
				comboDay.setEditable(false);
				comboDay.setSelectedIndex(-1);
				c.x = 9;
				panelNorth.add(comboDay, c);
			}
		}

		{
			final GridBagConstraints constraints = new GridBagConstraints();
			constraints.gridx = 0;
			constraints.gridy = 0;
			constraints.gridwidth = 1;
			constraints.gridheight = 1;
			constraints.weightx = 1.0;
			constraints.weighty = 1.0;
			constraints.fill = GridBagConstraints.NONE;

			final JPanel panelCenter = new JPanel();
			panelCenter.setLayout(new BorderLayout());

			{
				panelScore = new JPanel();
				final JPanel panelCenterSupportCenter = new JPanel();
				panelCenterSupportCenter.setLayout(new GridBagLayout());
				constraints.anchor = GridBagConstraints.NORTHWEST;
				panelCenterSupportCenter.add(panelScore, constraints);

				scrollScore = new JScrollPane(panelCenterSupportCenter, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
					ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
				scrollScore.getVerticalScrollBar().setUnitIncrement(16);
				panelCenter.add(scrollScore, BorderLayout.CENTER);
			}
			{
				final JPanel panelCenterNorth = new JPanel(new BorderLayout());

				final JLabel labelNorthWestSpacer = new JLabel();
				labelNorthWestSpacer.setPreferredSize(new Dimension(LABEL_WIDTH + 3, LABEL_HEIGHT));
				panelCenterNorth.add(labelNorthWestSpacer, BorderLayout.WEST);

				panelTitleUpper = new JPanel();
				final JPanel panelCenterSupportNorth = new JPanel();
				panelCenterSupportNorth.setLayout(new GridBagLayout());
				constraints.anchor = GridBagConstraints.WEST;
				panelCenterSupportNorth.add(panelTitleUpper, constraints);

				scrollTitleUpper = new JScrollPane(panelCenterSupportNorth, ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
					ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
				panelCenterNorth.add(scrollTitleUpper, BorderLayout.CENTER);

				final JLabel labelNorthEastSpacer = new JLabel();
				labelNorthEastSpacer.setPreferredSize(new Dimension(SCROLL_WIDTH, LABEL_HEIGHT));
				panelCenterNorth.add(labelNorthEastSpacer, BorderLayout.EAST);

				panelCenter.add(panelCenterNorth, BorderLayout.NORTH);
			}
			{
				final JPanel panelCenterWest = new JPanel(new BorderLayout());

				panelTitleLeft = new JPanel();
				final JPanel panelCenterSupportWest = new JPanel();
				panelCenterSupportWest.setLayout(new GridBagLayout());
				constraints.anchor = GridBagConstraints.NORTH;
				panelCenterSupportWest.add(panelTitleLeft, constraints);

				scrollTitleLeft = new JScrollPane(panelCenterSupportWest, ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
					ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
				panelCenterWest.add(scrollTitleLeft, BorderLayout.CENTER);

				final JLabel labelWestNorthSpacer = new JLabel();
				labelWestNorthSpacer.setPreferredSize(new Dimension(SCROLL_WIDTH, SCROLL_WIDTH));
				panelCenterWest.add(labelWestNorthSpacer, BorderLayout.SOUTH);

				panelCenter.add(panelCenterWest, BorderLayout.WEST);
			}

			add(panelCenter, BorderLayout.CENTER);
		}

		displayFullName = false;
		listTournament = new ArrayList<Tournament>();

		comboPeriodMode.addActionListener((final ActionEvent e) -> changePeriodParameters(true));
		tournamentComboBoxActionListener = (final ActionEvent e) -> refreshYear();

		periodParametersComboBoxHighLevelActionListener = (final ActionEvent e) -> refreshDay();
		periodParametersComboBoxLowLevelActionListener = (final ActionEvent e) -> display();

		comboTrimester.addActionListener(periodParametersComboBoxLowLevelActionListener);
		comboMonth.addActionListener(periodParametersComboBoxHighLevelActionListener);

		scrollScore.getVerticalScrollBar().addAdjustmentListener((final AdjustmentEvent e) -> synchronizedScrolls());
		scrollScore.getHorizontalScrollBar().addAdjustmentListener((final AdjustmentEvent e) -> synchronizedScrolls());

		comboBoxActivated = new boolean[COMBOBOX_NUMBER];
		changePeriodParameters(false);
	}

	@Override
	public String getTabName() {
		return "RCR Score Analyze";
	}

	@Override
	public void setDisplayFullName(final boolean displayFullName, final boolean toRefresh) {
		this.displayFullName = displayFullName;
		if (toRefresh) {
			display();
		}
	}

	private void changePeriodParameters(final boolean toRefresh) {
		final EnumPeriodMode periodMode = periodModes[comboPeriodMode.getSelectedIndex()];
		switch (periodMode) {
			case ALL:
				comboBoxActivated[COMBOBOX_YEAR_INDEX] = false;
				comboBoxActivated[COMBOBOX_TRIMESTER_INDEX] = false;
				comboBoxActivated[COMBOBOX_MONTH_INDEX] = false;
				comboBoxActivated[COMBOBOX_DAY_INDEX] = false;
				break;
			case SEASON:
				comboBoxActivated[COMBOBOX_YEAR_INDEX] = true;
				comboBoxActivated[COMBOBOX_TRIMESTER_INDEX] = false;
				comboBoxActivated[COMBOBOX_MONTH_INDEX] = false;
				comboBoxActivated[COMBOBOX_DAY_INDEX] = false;
				break;
			case YEAR:
				comboBoxActivated[COMBOBOX_YEAR_INDEX] = true;
				comboBoxActivated[COMBOBOX_TRIMESTER_INDEX] = false;
				comboBoxActivated[COMBOBOX_MONTH_INDEX] = false;
				comboBoxActivated[COMBOBOX_DAY_INDEX] = false;
				break;
			case TRIMESTER:
				comboBoxActivated[COMBOBOX_YEAR_INDEX] = true;
				comboBoxActivated[COMBOBOX_TRIMESTER_INDEX] = true;
				comboBoxActivated[COMBOBOX_MONTH_INDEX] = false;
				comboBoxActivated[COMBOBOX_DAY_INDEX] = false;
				break;
			case MONTH:
				comboBoxActivated[COMBOBOX_YEAR_INDEX] = true;
				comboBoxActivated[COMBOBOX_TRIMESTER_INDEX] = false;
				comboBoxActivated[COMBOBOX_MONTH_INDEX] = true;
				comboBoxActivated[COMBOBOX_DAY_INDEX] = false;
				break;
			case DAY:
				comboBoxActivated[COMBOBOX_YEAR_INDEX] = true;
				comboBoxActivated[COMBOBOX_TRIMESTER_INDEX] = false;
				comboBoxActivated[COMBOBOX_MONTH_INDEX] = true;
				comboBoxActivated[COMBOBOX_DAY_INDEX] = true;
				break;
			default:
				break;
		}
		enableComboBoxes();
		if (toRefresh) {
			display();
		}
	}

	private void disableComboBoxes() {
		comboTournament.setEnabled(false);
		comboPeriodMode.setEnabled(false);
		comboYear.setEnabled(false);
		comboTrimester.setEnabled(false);
		comboMonth.setEnabled(false);
		comboDay.setEnabled(false);
	}

	private void enableComboBoxes() {
		comboTournament.setEnabled(true);
		comboPeriodMode.setEnabled(true);
		comboYear.setEnabled(comboBoxActivated[COMBOBOX_YEAR_INDEX]);
		comboTrimester.setEnabled(comboBoxActivated[COMBOBOX_TRIMESTER_INDEX]);
		comboMonth.setEnabled(comboBoxActivated[COMBOBOX_MONTH_INDEX]);
		comboDay.setEnabled(comboBoxActivated[COMBOBOX_DAY_INDEX]);
	}

	@Override
	public void refresh() {
		refreshTournament();
	}

	private void refreshTournament() {
		new Thread(() -> {
			try {
				listTournament.clear();
				final List<Tournament> newTournaments = dataAccess.getRCRTournaments();
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
			} catch (final Exception e) {
				JOptionPane.showMessageDialog(this, e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
			}
		}).start();
	}

	private void refreshYear() {
		new Thread(() -> {
			try {
				final int selectedTournamentIndex = comboTournament.getSelectedIndex();
				if (listTournament.size() > 0 && selectedTournamentIndex >= 0) {
					comboYear.removeActionListener(periodParametersComboBoxHighLevelActionListener);
					comboYear.removeAllItems();

					final Tournament tournament = listTournament.get(selectedTournamentIndex);
					final List<Integer> years = new ArrayList<Integer>(dataAccess.getRCRYears(tournament));
					Collections.sort(years);
					Collections.reverse(years);
					for (int index = 0; index < years.size(); index++) {
						comboYear.addItem(years.get(index));
					}

					comboYear.addActionListener(periodParametersComboBoxHighLevelActionListener);
					if (years.size() > 0) {
						comboYear.setSelectedIndex(0);
					} else {
						comboYear.setSelectedIndex(-1);
					}
				}
			} catch (final Exception e) {
				JOptionPane.showMessageDialog(this, e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
			}
		}).start();
	}

	private void refreshDay() {
		new Thread(() -> {
			try {
				final int selectedTournamentIndex = comboTournament.getSelectedIndex();
				final int selectedYearIndex = comboYear.getSelectedIndex();
				if (selectedTournamentIndex != -1 && selectedYearIndex != -1) {
					comboDay.removeActionListener(periodParametersComboBoxLowLevelActionListener);
					comboDay.removeAllItems();

					final Tournament tournament = listTournament.get(selectedTournamentIndex);
					final int year = (Integer) comboYear.getSelectedItem();
					final int month = comboMonth.getSelectedIndex();
					final List<Integer> days = new ArrayList<Integer>(dataAccess.getRCRGameDays(tournament, year, month));
					Collections.sort(days);
					for (int index = 0; index < days.size(); index++) {
						comboDay.addItem(days.get(index));
					}

					comboDay.addActionListener(periodParametersComboBoxLowLevelActionListener);
					if (days.size() > 0) {
						comboDay.setSelectedIndex(0);
					} else {
						comboDay.setSelectedIndex(-1);
					}
				}
			} catch (final Exception e) {
				JOptionPane.showMessageDialog(this, e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
			}
		}).start();
	}

	private void synchronizedScrolls() {
		scrollTitleUpper.getHorizontalScrollBar().setValue(scrollScore.getHorizontalScrollBar().getValue());
		scrollTitleLeft.getVerticalScrollBar().setValue(scrollScore.getVerticalScrollBar().getValue());
		repaint();
	}

	private void display() {
		new Thread(() -> {
			try {
				disableComboBoxes();
				panelTitleUpper.removeAll();
				panelTitleLeft.removeAll();
				panelScore.removeAll();
				validate();
				repaint();

				final EnumPeriodMode periodMode = periodModes[comboPeriodMode.getSelectedIndex()];

				final int selectedTournamentIndex = comboTournament.getSelectedIndex();
				final int selectedYearIndex = comboYear.getSelectedIndex();
				final int selectedDayIndex = comboDay.getSelectedIndex();
				if (selectedTournamentIndex != -1 && selectedYearIndex != -1 && (periodMode != EnumPeriodMode.DAY || selectedDayIndex != -1)) {
					final Tournament tournament = listTournament.get(selectedTournamentIndex);
					final int year = (Integer) comboYear.getSelectedItem();
					final int trimester = comboTrimester.getSelectedIndex();
					final int month = comboMonth.getSelectedIndex();
					final int day = selectedDayIndex != -1 ? (Integer) comboDay.getSelectedItem() : 0;
					final RCRDataPackageScoreAnalyze score = dataAccess.getRCRDataPackageScoreAnalyze(tournament, periodMode, year, trimester, month, day);

					if (score != null) {
						final Dimension labelSize = new Dimension(LABEL_WIDTH, LABEL_HEIGHT);

						final int nbPlayers = score.playerNames.size();
						panelTitleUpper.setLayout(new ProportionalGridLayout(1, nbPlayers, 0, 0));
						panelTitleLeft.setLayout(new ProportionalGridLayout(nbPlayers + 1, 1, 0, 0));
						panelScore.setLayout(new ProportionalGridLayout(nbPlayers + 1, nbPlayers, 0, 0));
						final ProportionalGridLayoutConstraint constraintsScore = new ProportionalGridLayoutConstraint(0, 1, 0, 1);
						final ProportionalGridLayoutConstraint constraintsUpper = new ProportionalGridLayoutConstraint(0, 1, 0, 1);
						final ProportionalGridLayoutConstraint constraintsLeft = new ProportionalGridLayoutConstraint(0, 1, 0, 1);

						constraintsUpper.y = 0;
						constraintsLeft.x = 0;
						for (int index = 0; index < nbPlayers; index++) {
							final JLabel labelPlayerNameUpper = new JLabel(displayFullName ? score.playerNames.get(index) : score.displayNames.get(index),
								SwingConstants.CENTER);
							labelPlayerNameUpper.setPreferredSize(labelSize);
							labelPlayerNameUpper.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
							constraintsUpper.x = index;
							panelTitleUpper.add(labelPlayerNameUpper, constraintsUpper);

							final JLabel labelPlayerNameLeft = new JLabel(displayFullName ? score.playerNames.get(index) : score.displayNames.get(index),
								SwingConstants.CENTER);
							labelPlayerNameLeft.setPreferredSize(labelSize);
							labelPlayerNameLeft.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
							constraintsLeft.y = index;
							panelTitleLeft.add(labelPlayerNameLeft, constraintsLeft);
						}

						{
							final JLabel labelPlayerNameLeftSum = new JLabel("Somme", SwingConstants.CENTER);
							labelPlayerNameLeftSum.setPreferredSize(labelSize);
							labelPlayerNameLeftSum.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
							constraintsLeft.y = nbPlayers;
							panelTitleLeft.add(labelPlayerNameLeftSum, constraintsLeft);
						}

						for (int y = 0; y < nbPlayers; y++) {
							constraintsScore.y = y;
							for (int x = 0; x < nbPlayers; x++) {
								final JLabel labelScore = new JLabel("", SwingConstants.CENTER);
								labelScore.setPreferredSize(labelSize);
								labelScore.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
								if (x == y) {
									labelScore.setOpaque(true);
									labelScore.setBackground(Color.BLACK);
								} else {
									labelScore.setText(Long.toString(Math.round(score.scores[x][y])));
									if (score.scores[x][y] < 0) {
										labelScore.setForeground(Color.RED);
									}
									if (y % 2 == 0) {
										labelScore.setOpaque(true);
										labelScore.setBackground(Color.LIGHT_GRAY);
									}
								}
								constraintsScore.x = x;
								panelScore.add(labelScore, constraintsScore);
							}
						}

						{
							constraintsScore.y = nbPlayers;
							for (int x = 0; x < nbPlayers; x++) {
								final JLabel labelScore = new JLabel(Long.toString(Math.round(score.sums[x])), SwingConstants.CENTER);
								labelScore.setPreferredSize(labelSize);
								labelScore.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
								constraintsScore.x = x;
								panelScore.add(labelScore, constraintsScore);
							}
						}
					}
				}
				validate();
				enableComboBoxes();
				repaint();
			} catch (final Exception e) {
				JOptionPane.showMessageDialog(this, e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
			}
		}).start();
	}

	@Override
	public boolean canExport() {
		return true;
	}

	@Override
	public void export() {

	}
}
