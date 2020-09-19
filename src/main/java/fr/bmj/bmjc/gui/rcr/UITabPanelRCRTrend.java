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
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.SortedMap;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.DateTickUnit;
import org.jfree.chart.axis.DateTickUnitType;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import fr.bmj.bmjc.data.game.ComparatorAscendingPlayerDisplayName;
import fr.bmj.bmjc.data.game.ComparatorAscendingPlayerName;
import fr.bmj.bmjc.data.game.ComparatorDescendingTournamentID;
import fr.bmj.bmjc.data.game.Player;
import fr.bmj.bmjc.data.game.Tournament;
import fr.bmj.bmjc.data.stat.rcr.RCRDataPackageTrend;
import fr.bmj.bmjc.dataaccess.abs.rcr.DataAccessRCR;
import fr.bmj.bmjc.enums.EnumPeriodMode;
import fr.bmj.bmjc.enums.EnumTrimester;
import fr.bmj.bmjc.gui.UITabPanel;
import fr.bri.awt.ProportionalGridLayout;
import fr.bri.awt.ProportionalGridLayoutConstraint;

public class UITabPanelRCRTrend extends UITabPanel {
	private static final long serialVersionUID = 6352466319081554030L;

	private static final int MAX_NUMBER_OF_TICKS = 10;
	private static final int TICK_UNIT_MULTIPLE = 7;
	private static final long MILLISECONDS_PER_DAY = 1000l * 60l * 60l * 24l;

	private static final int COMBOBOX_NUMBER = 4;
	private static final int COMBOBOX_YEAR_INDEX = 0;
	private static final int COMBOBOX_TRIMESTER_INDEX = 1;
	private static final int COMBOBOX_MONTH_INDEX = 2;
	private static final int COMBOBOX_DAY_INDEX = 3;

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

	private final JPanel panelPlayerSelect;
	private final JCheckBox checkBoxSelectAll;
	private final JButton buttonFilter;
	private final List<JCheckBox> listCheckBoxPlayerSelect;
	private final JPanel panelChart;

	private final ActionListener selectAllCheckBoxActionListener;
	private final ActionListener filterButtonActionListeneer;

	private final List<Tournament> listTournament;
	private final List<Player> listPlayers;

	public UITabPanelRCRTrend(final DataAccessRCR dataAccess) {
		this.dataAccess = dataAccess;

		setLayout(
			new BorderLayout());
		{
			final JPanel panelNorth = new JPanel();
			final ProportionalGridLayout northLayout = new ProportionalGridLayout(
				2,
				11,
				8,
				2);
			northLayout.setWeightX(
				4,
				5,
				3,
				5,
				3,
				5,
				3,
				5,
				3,
				5,
				2);
			panelNorth.setLayout(
				northLayout);
			panelNorth.setBorder(
				BorderFactory.createLoweredBevelBorder());
			add(
				panelNorth,
				BorderLayout.NORTH);
			final ProportionalGridLayoutConstraint c = new ProportionalGridLayoutConstraint(
				0,
				1,
				0,
				1);

			{
				c.y = 0;
				c.x = 2;
				panelNorth.add(
					new JLabel(
						"Tournoi :",
						SwingConstants.RIGHT),
					c);
				comboTournament = new JComboBox<String>();
				comboTournament.setEditable(
					false);
				c.x = 3;
				c.gridWidth = 5;
				panelNorth.add(
					comboTournament,
					c);

				c.y = 1;
				c.x = 0;
				c.gridWidth = 1;
				panelNorth.add(
					new JLabel(
						"Période :",
						SwingConstants.RIGHT),
					c);
				periodModes = new EnumPeriodMode[] {
					EnumPeriodMode.ALL,
					EnumPeriodMode.YEAR,
					EnumPeriodMode.TRIMESTER,
					EnumPeriodMode.MONTH
				};
				final String periodModeStrings[] = new String[periodModes.length];
				for (int index = 0; index < periodModes.length; index++) {
					periodModeStrings[index] = periodModes[index].toString();
				}
				comboPeriodMode = new JComboBox<String>(
					periodModeStrings);
				comboPeriodMode.setEditable(
					false);
				comboPeriodMode.setSelectedIndex(
					2);
				c.x = 1;
				panelNorth.add(
					comboPeriodMode,
					c);

				c.x = 2;
				panelNorth.add(
					new JLabel(
						"Année :",
						SwingConstants.RIGHT),
					c);
				comboYear = new JComboBox<Integer>();
				comboYear.setEditable(
					false);
				c.x = 3;
				panelNorth.add(
					comboYear,
					c);

				c.x = 4;
				panelNorth.add(
					new JLabel(
						"Trimestre :",
						SwingConstants.RIGHT),
					c);
				final String trimesters[] = {
					EnumTrimester.TRIMESTER_1.toString(),
					EnumTrimester.TRIMESTER_2.toString(),
					EnumTrimester.TRIMESTER_3.toString(),
					EnumTrimester.TRIMESTER_4.toString()
				};
				comboTrimester = new JComboBox<String>(
					trimesters);
				comboTrimester.setEditable(
					false);
				comboTrimester.setSelectedIndex(
					0);
				c.x = 5;
				panelNorth.add(
					comboTrimester,
					c);

				c.x = 6;
				panelNorth.add(
					new JLabel(
						"Mois :",
						SwingConstants.RIGHT),
					c);
				final String months[] = new String[12];
				System.arraycopy(
					DateFormatSymbols.getInstance(
						Locale.FRANCE).getMonths(),
					0,
					months,
					0,
					12);
				comboMonth = new JComboBox<>(
					months);
				comboMonth.setEditable(
					false);
				comboMonth.setSelectedIndex(
					0);
				c.x = 7;
				panelNorth.add(
					comboMonth,
					c);

				c.x = 8;
				panelNorth.add(
					new JLabel(
						"Jour :",
						SwingConstants.RIGHT),
					c);
				comboDay = new JComboBox<Integer>();
				comboDay.setEditable(
					false);
				comboDay.setSelectedIndex(
					-1);
				c.x = 9;
				panelNorth.add(
					comboDay,
					c);
			}
		}

		{
			listCheckBoxPlayerSelect = new ArrayList<JCheckBox>();

			final JPanel panelCenter = new JPanel(
				new BorderLayout());
			add(
				panelCenter,
				BorderLayout.CENTER);
			{
				final JPanel panelCenterWest = new JPanel(
					new BorderLayout());
				panelCenterWest.setBorder(
					BorderFactory.createLoweredBevelBorder());
				panelCenter.add(
					panelCenterWest,
					BorderLayout.WEST);

				{
					final JPanel panelCenterWestNorth = new JPanel(
						new GridBagLayout());
					panelCenterWestNorth.setMinimumSize(
						new Dimension(
							160,
							0));
					panelCenterWest.add(
						panelCenterWestNorth,
						BorderLayout.NORTH);
					final GridBagConstraints constraintsCenterWestNorth = new GridBagConstraints(
						0,
						0,
						1,
						1,
						0.0,
						0.0,
						GridBagConstraints.CENTER,
						GridBagConstraints.NONE,
						new Insets(
							0,
							0,
							0,
							0),
						0,
						0);

					buttonFilter = new JButton(
						"Filtrer");
					buttonFilter.setPreferredSize(
						new Dimension(
							BUTTON_MIN_WIDTH,
							BUTTON_MIN_HEIGHT));
					panelCenterWestNorth.add(
						buttonFilter,
						constraintsCenterWestNorth);

					checkBoxSelectAll = new JCheckBox(
						"Tous sélectionner",
						true);
					constraintsCenterWestNorth.gridy = 1;
					constraintsCenterWestNorth.fill = GridBagConstraints.HORIZONTAL;
					constraintsCenterWestNorth.weightx = 1.0;
					panelCenterWestNorth.add(
						checkBoxSelectAll,
						constraintsCenterWestNorth);
				}

				{
					panelPlayerSelect = new JPanel();
					panelPlayerSelect.setLayout(
						new BoxLayout(
							panelPlayerSelect,
							BoxLayout.Y_AXIS));
					final JPanel panelPlayerSelectSupport = new JPanel(
						new GridBagLayout());
					final GridBagConstraints constraintsSupport = new GridBagConstraints(
						0,
						0,
						1,
						1,
						1.0,
						1.0,
						GridBagConstraints.NORTH,
						GridBagConstraints.HORIZONTAL,
						new Insets(
							0,
							0,
							0,
							0),
						0,
						0);
					panelPlayerSelectSupport.add(
						panelPlayerSelect,
						constraintsSupport);
					final JScrollPane scrollPlayerSelect = new JScrollPane(
						panelPlayerSelectSupport,
						ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
						ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
					scrollPlayerSelect.getVerticalScrollBar().setUnitIncrement(
						16);
					panelCenterWest.add(
						scrollPlayerSelect,
						BorderLayout.CENTER);
				}
			}

			{
				panelChart = new JPanel(
					new BorderLayout());
				panelCenter.add(
					panelChart,
					BorderLayout.CENTER);
			}
		}

		displayFullName = false;
		listTournament = new ArrayList<Tournament>();
		listPlayers = new ArrayList<Player>();

		selectAllCheckBoxActionListener = (final ActionEvent e) -> selectAll();
		filterButtonActionListeneer = (final ActionEvent e) -> displayData();

		comboPeriodMode.addActionListener(
			(final ActionEvent e) -> changePeriodParameters(
				true));
		tournamentComboBoxActionListener = (final ActionEvent e) -> refreshYear();

		periodParametersComboBoxHighLevelActionListener = (final ActionEvent e) -> refreshDay();
		periodParametersComboBoxLowLevelActionListener = (final ActionEvent e) -> refreshData();

		comboTrimester.addActionListener(
			periodParametersComboBoxLowLevelActionListener);
		comboMonth.addActionListener(
			periodParametersComboBoxHighLevelActionListener);

		comboBoxActivated = new boolean[COMBOBOX_NUMBER];
		changePeriodParameters(
			false);
	}

	@Override
	public String getTabName() {
		return "RCR Tendance";
	}

	@Override
	public void setDisplayFullName(final boolean displayFullName, final boolean toRefresh) {
		this.displayFullName = displayFullName;
		if (toRefresh) {
			displayPlayerName();
			displayData();
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
			refreshData();
		}
	}

	private void disableComboBoxes() {
		comboTournament.setEnabled(
			false);
		comboPeriodMode.setEnabled(
			false);
		comboYear.setEnabled(
			false);
		comboTrimester.setEnabled(
			false);
		comboMonth.setEnabled(
			false);
		comboDay.setEnabled(
			false);
	}

	private void enableComboBoxes() {
		comboTournament.setEnabled(
			true);
		comboPeriodMode.setEnabled(
			true);
		comboYear.setEnabled(
			comboBoxActivated[COMBOBOX_YEAR_INDEX]);
		comboTrimester.setEnabled(
			comboBoxActivated[COMBOBOX_TRIMESTER_INDEX]);
		comboMonth.setEnabled(
			comboBoxActivated[COMBOBOX_MONTH_INDEX]);
		comboDay.setEnabled(
			comboBoxActivated[COMBOBOX_DAY_INDEX]);
	}

	@Override
	public void refresh() {
		new Thread(
			() -> {
				try {
					refreshPlayerName();
					refreshTournament();
				} catch (final Exception e) {
					JOptionPane.showMessageDialog(
						this,
						e.getMessage(),
						"Erreur",
						JOptionPane.ERROR_MESSAGE);
				}
			}).start();
	}

	private void refreshPlayerName() {
		listPlayers.clear();
		listPlayers.addAll(
			dataAccess.getRCRPlayers());
		if (listPlayers.size() > 0) {
			checkBoxSelectAll.removeActionListener(
				selectAllCheckBoxActionListener);
			buttonFilter.removeActionListener(
				filterButtonActionListeneer);

			panelPlayerSelect.removeAll();
			listCheckBoxPlayerSelect.clear();
			for (int index = 0; index < listPlayers.size(); index++) {
				final JCheckBox checkBox = new JCheckBox(
					"",
					true);
				panelPlayerSelect.add(
					checkBox);
				listCheckBoxPlayerSelect.add(
					checkBox);
			}
			displayPlayerName();

			checkBoxSelectAll.addActionListener(
				selectAllCheckBoxActionListener);
			buttonFilter.addActionListener(
				filterButtonActionListeneer);
		}
	}

	private void displayPlayerName() {
		if (displayFullName) {
			Collections.sort(
				listPlayers,
				new ComparatorAscendingPlayerName());
			for (int index = 0; index < listPlayers.size(); index++) {
				listCheckBoxPlayerSelect.get(
					index).setText(
						listPlayers.get(
							index).getPlayerName());
			}
		} else {
			Collections.sort(
				listPlayers,
				new ComparatorAscendingPlayerDisplayName());
			for (int index = 0; index < listPlayers.size(); index++) {
				listCheckBoxPlayerSelect.get(
					index).setText(
						listPlayers.get(
							index).getDisplayName());
			}
		}
		repaint();
	}

	private void refreshTournament() {
		listTournament.clear();
		final List<Tournament> newTournaments = dataAccess.getRCRTournaments();
		if (newTournaments.size() > 0) {
			listTournament.addAll(
				newTournaments);
			Collections.sort(
				listTournament,
				new ComparatorDescendingTournamentID());

			comboTournament.removeActionListener(
				tournamentComboBoxActionListener);
			comboTournament.removeAllItems();
			for (int index = 0; index < listTournament.size(); index++) {
				final Tournament tournament = listTournament.get(
					index);
				comboTournament.addItem(
					tournament.getName());
			}
			comboTournament.addActionListener(
				tournamentComboBoxActionListener);
			if (listTournament.size() > 0) {
				comboTournament.setSelectedIndex(
					0);
			}
		}
	}

	private void refreshYear() {
		new Thread(
			() -> {
				try {
					final int selectedTournamentIndex = comboTournament.getSelectedIndex();
					if (listTournament.size() > 0 && selectedTournamentIndex >= 0) {
						comboYear.removeActionListener(
							periodParametersComboBoxHighLevelActionListener);
						comboYear.removeAllItems();

						final Tournament tournament = listTournament.get(
							selectedTournamentIndex);
						final List<Integer> years = new ArrayList<Integer>(
							dataAccess.getRCRYears(
								tournament));
						Collections.sort(
							years);
						Collections.reverse(
							years);
						for (int index = 0; index < years.size(); index++) {
							comboYear.addItem(
								years.get(
									index));
						}

						comboYear.addActionListener(
							periodParametersComboBoxHighLevelActionListener);
						if (years.size() > 0) {
							comboYear.setSelectedIndex(
								0);
						} else {
							comboYear.setSelectedIndex(
								-1);
						}
					}
				} catch (final Exception e) {
					JOptionPane.showMessageDialog(
						this,
						e.getMessage(),
						"Erreur",
						JOptionPane.ERROR_MESSAGE);
				}
			}).start();
	}

	private void refreshDay() {
		new Thread(
			() -> {
				try {
					final int selectedTournamentIndex = comboTournament.getSelectedIndex();
					final int selectedYearIndex = comboYear.getSelectedIndex();
					if (selectedTournamentIndex != -1 && selectedYearIndex != -1) {
						comboDay.removeActionListener(
							periodParametersComboBoxLowLevelActionListener);
						comboDay.removeAllItems();

						final Tournament tournament = listTournament.get(
							selectedTournamentIndex);
						final int year = (Integer) comboYear.getSelectedItem();
						final int month = comboMonth.getSelectedIndex();
						final List<Integer> days = new ArrayList<Integer>(
							dataAccess.getRCRGameDays(
								tournament,
								year,
								month));
						Collections.sort(
							days);
						for (int index = 0; index < days.size(); index++) {
							comboDay.addItem(
								days.get(
									index));
						}

						comboDay.addActionListener(
							periodParametersComboBoxLowLevelActionListener);
						if (days.size() > 0) {
							comboDay.setSelectedIndex(
								0);
						} else {
							comboDay.setSelectedIndex(
								-1);
						}
					}
				} catch (final Exception e) {
					JOptionPane.showMessageDialog(
						this,
						e.getMessage(),
						"Erreur",
						JOptionPane.ERROR_MESSAGE);
				}
			}).start();
	}

	private void refreshData() {
		new Thread(
			() -> {
				try {
					final EnumPeriodMode periodMode = periodModes[comboPeriodMode.getSelectedIndex()];

					final int selectedTournamentIndex = comboTournament.getSelectedIndex();
					final int selectedYearIndex = comboYear.getSelectedIndex();
					final int selectedDayIndex = comboDay.getSelectedIndex();
					if (selectedTournamentIndex != -1 && selectedYearIndex != -1) {
						final Tournament tournament = listTournament.get(
							selectedTournamentIndex);
						final int year = (Integer) comboYear.getSelectedItem();
						final int trimester = comboTrimester.getSelectedIndex();
						final int month = comboMonth.getSelectedIndex();
						final int day = selectedDayIndex != -1 ? (Integer) comboDay.getSelectedItem() : 0;
						trend = dataAccess.getRCRDataPackageTrend(
							tournament,
							periodMode,
							year,
							trimester,
							month,
							day);
					}

					displayData();
				} catch (final Exception e) {
					JOptionPane.showMessageDialog(
						this,
						e.getMessage(),
						"Erreur",
						JOptionPane.ERROR_MESSAGE);
				}
			}).start();
	}

	private void selectAll() {
		final boolean selected = checkBoxSelectAll.isSelected();
		for (int index = 0; index < listCheckBoxPlayerSelect.size(); index++) {
			listCheckBoxPlayerSelect.get(
				index).setSelected(
					selected);
		}
		repaint();
	}

	private void displayData() {
		new Thread(
			() -> {
				try {
					disableComboBoxes();
					panelChart.removeAll();
					validate();
					repaint();

					if (trend.dates.size() > 0) {
						final Set<String> selectedNames = new HashSet<String>();
						if (displayFullName) {
							for (int index = 0; index < listCheckBoxPlayerSelect.size(); index++) {
								if (listCheckBoxPlayerSelect.get(
									index).isSelected()) {
									selectedNames.add(
										listPlayers.get(
											index).getPlayerName());
								}
							}
						} else {
							for (int index = 0; index < listCheckBoxPlayerSelect.size(); index++) {
								if (listCheckBoxPlayerSelect.get(
									index).isSelected()) {
									selectedNames.add(
										listPlayers.get(
											index).getDisplayName());
								}
							}
						}

						final SortedMap<String, List<Integer>> data = displayFullName ? trend.dataWithPlayerName : trend.dataWithDisplayName;
						final List<Long> dates = trend.dates;

						final TimeSeriesCollection series = new TimeSeriesCollection();
						final XYItemRenderer sumRender = new XYLineAndShapeRenderer();
						for (final String playerName : data.keySet()) {
							if (selectedNames.contains(
								playerName)) {
								final List<Integer> score = data.get(
									playerName);
								final TimeSeries sumSeries = new TimeSeries(
									playerName);
								for (int index = 1; index < score.size(); index++) {
									sumSeries.add(
										new Day(
											new Date(
												dates.get(
													index))),
										score.get(
											index));
								}
								series.addSeries(
									sumSeries);
							}
						}

						int tickUnit;
						final int numberOfDays = (int) ((dates.get(
							dates.size() - 1)
							- dates.get(
								1))
							/ MILLISECONDS_PER_DAY);
						if (numberOfDays < MAX_NUMBER_OF_TICKS) {
							tickUnit = 1;
						} else {
							tickUnit = (numberOfDays / (MAX_NUMBER_OF_TICKS * TICK_UNIT_MULTIPLE) + 1) * TICK_UNIT_MULTIPLE;
						}

						final DateAxis sumDomainAxis = new DateAxis(
							"Date");
						sumDomainAxis.setRange(
							new Date(
								dates.get(
									1) - MILLISECONDS_PER_DAY),
							new Date(
								dates.get(
									dates.size() - 1) + MILLISECONDS_PER_DAY));
						sumDomainAxis.setTickUnit(
							new DateTickUnit(
								DateTickUnitType.DAY,
								tickUnit));
						sumDomainAxis.setLowerMargin(
							0.0);
						sumDomainAxis.setUpperMargin(
							0.0);

						final NumberAxis sumRangeAxis = new NumberAxis(
							"Score total");
						final XYPlot sumPlot = new XYPlot(
							series,
							sumDomainAxis,
							sumRangeAxis,
							sumRender);
						sumPlot.setBackgroundPaint(
							new Color(
								255,
								255,
								255,
								0));
						sumPlot.setDomainGridlinePaint(
							Color.BLACK);
						sumPlot.setRangeGridlinePaint(
							Color.BLACK);
						final ValueMarker marker = new ValueMarker(
							0.0,
							Color.RED,
							new BasicStroke(
								1),
							null,
							null,
							1.0f);
						sumPlot.addRangeMarker(
							marker);

						final ChartPanel chartPanel = new ChartPanel(
							new JFreeChart(
								sumPlot));
						chartPanel.setPopupMenu(
							null);
						chartPanel.setMouseZoomable(
							false);
						panelChart.add(
							chartPanel,
							BorderLayout.CENTER);
					}

					validate();
					enableComboBoxes();
					repaint();
				} catch (final Exception e) {
					JOptionPane.showMessageDialog(
						this,
						e.getMessage(),
						"Erreur",
						JOptionPane.ERROR_MESSAGE);
				}
			}).start();
	}

	@Override
	public boolean canExport() {
		return true;
	}

	private RCRDataPackageTrend trend;

	@Override
	public void export() {
		new Thread(
			() -> {
				try {
					final EnumPeriodMode periodMode = periodModes[comboPeriodMode.getSelectedIndex()];

					final int selectedTournamentIndex = comboTournament.getSelectedIndex();
					final int selectedYearIndex = comboYear.getSelectedIndex();
					if (selectedTournamentIndex != -1 && selectedYearIndex != -1) {
						final Tournament tournament = listTournament.get(
							selectedTournamentIndex);
						final int year = (Integer) comboYear.getSelectedItem();

						if (trend != null && trend.dates.size() > 0) {
							final StringBuffer proposedSaveFileName = new StringBuffer();
							proposedSaveFileName.append(
								tournament.getName());
							proposedSaveFileName.append(
								"_tendance_");
							proposedSaveFileName.append(
								periodMode.toString());
							proposedSaveFileName.append(
								"_");
							switch (periodMode) {
								case ALL:
									break;
								case YEAR:
									proposedSaveFileName.append(
										Integer.toString(
											year));
									break;
								case TRIMESTER:
									proposedSaveFileName.append(
										Integer.toString(
											year));
									proposedSaveFileName.append(
										"_");
									proposedSaveFileName.append(
										comboTrimester.getSelectedItem().toString());
									break;
								case MONTH:
									proposedSaveFileName.append(
										Integer.toString(
											year));
									proposedSaveFileName.append(
										"_");
									proposedSaveFileName.append(
										comboMonth.getSelectedItem().toString());
									break;
								default:
									break;
							}
							proposedSaveFileName.append(
								".csv");
							final File fileSaveFile = askSaveFileName(
								proposedSaveFileName.toString());
							if (fileSaveFile != null) {
								BufferedWriter writer = null;
								try {
									writer = new BufferedWriter(
										new OutputStreamWriter(
											new FileOutputStream(
												fileSaveFile),
											Charset.forName(
												"UTF-8")));

									final SortedMap<String, List<Integer>> data = displayFullName ? trend.dataWithPlayerName : trend.dataWithDisplayName;
									final List<Long> dates = trend.dates;

									final DateFormat dateFormat = DateFormat.getDateInstance(
										DateFormat.LONG,
										Locale.FRANCE);
									final Calendar calendar = Calendar.getInstance();

									for (int index = 0; index < dates.size(); index++) {
										writer.write(
											SEPARATOR);
										calendar.setTimeInMillis(
											dates.get(
												index));
										writer.write(
											dateFormat.format(
												calendar.getTime()));
									}
									writer.newLine();

									for (final String playerName : data.keySet()) {
										writer.write(
											playerName);
										final List<Integer> score = data.get(
											playerName);
										for (int dateIndex = 0; dateIndex < score.size(); dateIndex++) {
											writer.write(
												SEPARATOR);
											writer.write(
												Integer.toString(
													score.get(
														dateIndex)));
										}
										writer.newLine();
									}
								} catch (final Exception e) {
									JOptionPane.showMessageDialog(
										this,
										"Une erreur est survenue lors de sauvegarde.",
										"Erreur",
										JOptionPane.ERROR_MESSAGE);
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
				} catch (final Exception e) {
					JOptionPane.showMessageDialog(
						this,
						e.getMessage(),
						"Erreur",
						JOptionPane.ERROR_MESSAGE);
				}
			}).start();
	}

}
