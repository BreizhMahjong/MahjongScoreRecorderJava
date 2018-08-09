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

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
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

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

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
import fr.bmj.bmjc.data.stat.mcr.MCRDataPackageTrend;
import fr.bmj.bmjc.dataaccess.mcr.DataAccessMCR;
import fr.bmj.bmjc.enums.EnumPeriodMode;
import fr.bmj.bmjc.enums.EnumTrimester;
import fr.bmj.bmjc.gui.UITabPanel;
import fr.bmj.bmjc.swing.ComponentShownListener;
import fr.bmj.bmjc.swing.JDialogWithProgress;
import fr.bmj.bmjc.swing.ProportionalGridLayout;
import fr.bmj.bmjc.swing.ProportionalGridLayoutConstraint;

public class UITabPanelMCRTrend extends UITabPanel {
	private static final long serialVersionUID = -2511029206029779978L;

	private static final int MAX_NUMBER_OF_TICKS = 10;
	private static final int TICK_UNIT_MULTIPLE = 7;
	private static final long MILLISECONDS_PER_DAY = 1000l * 60l * 60l * 24l;

	private static final int COMBOBOX_NUMBER = 4;
	private static final int COMBOBOX_PERIOD = 0;
	private static final int COMBOBOX_YEAR_INDEX = 1;
	private static final int COMBOBOX_TRIMESTER_INDEX = 2;
	private static final int COMBOBOX_MONTH_INDEX = 3;

	private boolean displayFullName;
	private final DataAccessMCR dataAccess;
	private final JDialogWithProgress waitingDialog;
	private final ComponentShownListener waitingDialogRefreshData;
	private final ComponentShownListener waitingDialogDisplayData;

	private final JComboBox<String> comboTournament;

	private final EnumPeriodMode periodModes[];
	private final JComboBox<String> comboPeriodMode;
	private final JComboBox<Integer> comboYear;
	private final JComboBox<String> comboTrimester;
	private final JComboBox<String> comboMonth;
	private final boolean comboBoxActivated[];

	private final ActionListener tournamentComboBoxActionListener;
	private final ActionListener periodParametersComboBoxActionListener;

	private final JPanel panelPlayerSelect;
	private final JCheckBox checkBoxSelectAll;
	private final JButton buttonFilter;
	private final List<JCheckBox> listCheckBoxPlayerSelect;
	private final JPanel panelChart;

	private final ActionListener selectAllCheckBoxActionListener;
	private final ActionListener filterButtonActionListeneer;

	private final List<Tournament> listTournament;
	private final List<Player> listPlayers;

	public UITabPanelMCRTrend(final DataAccessMCR dataAccess, final JDialogWithProgress waitingDialog) {
		this.dataAccess = dataAccess;
		this.waitingDialog = waitingDialog;
		waitingDialogRefreshData = (final ComponentEvent e) -> new Thread(() -> refreshDataRun()).start();
		waitingDialogDisplayData = (final ComponentEvent e) -> new Thread(() -> displayDataRun()).start();

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
				c.x = 2;
				panelNorth.add(new JLabel("Tournoi :", JLabel.RIGHT), c);
				comboTournament = new JComboBox<String>();
				comboTournament.setEditable(false);
				c.x = 3;
				c.gridWidth = 3;
				panelNorth.add(comboTournament, c);

				c.y = 1;
				c.x = 0;
				c.gridWidth = 1;
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
				final String trimesters[] = {
					EnumTrimester.TRIMESTER_1.toString(), EnumTrimester.TRIMESTER_2.toString(), EnumTrimester.TRIMESTER_3.toString(), EnumTrimester.TRIMESTER_4.toString()
				};
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
			listCheckBoxPlayerSelect = new ArrayList<JCheckBox>();

			final JPanel panelCenter = new JPanel(new BorderLayout());
			add(panelCenter, BorderLayout.CENTER);
			{
				final JPanel panelCenterWest = new JPanel(new BorderLayout());
				panelCenterWest.setBorder(BorderFactory.createLoweredBevelBorder());
				panelCenter.add(panelCenterWest, BorderLayout.WEST);

				{
					final JPanel panelCenterWestNorth = new JPanel(new GridBagLayout());
					panelCenterWestNorth.setMinimumSize(new Dimension(160, 0));
					panelCenterWest.add(panelCenterWestNorth, BorderLayout.NORTH);
					final GridBagConstraints constraintsCenterWestNorth = new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0,
						0);

					buttonFilter = new JButton("Filtrer");
					buttonFilter.setPreferredSize(new Dimension(BUTTON_MIN_WIDTH, BUTTON_MIN_HEIGHT));
					panelCenterWestNorth.add(buttonFilter, constraintsCenterWestNorth);

					checkBoxSelectAll = new JCheckBox("Tous sélectionner", true);
					constraintsCenterWestNorth.gridy = 1;
					constraintsCenterWestNorth.fill = GridBagConstraints.HORIZONTAL;
					constraintsCenterWestNorth.weightx = 1.0;
					panelCenterWestNorth.add(checkBoxSelectAll, constraintsCenterWestNorth);
				}

				{
					panelPlayerSelect = new JPanel();
					panelPlayerSelect.setLayout(new BoxLayout(panelPlayerSelect, BoxLayout.Y_AXIS));
					final JPanel panelPlayerSelectSupport = new JPanel(new GridBagLayout());
					final GridBagConstraints constraintsSupport = new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0);
					panelPlayerSelectSupport.add(panelPlayerSelect, constraintsSupport);
					final JScrollPane scrollPlayerSelect = new JScrollPane(panelPlayerSelectSupport, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
					scrollPlayerSelect.getVerticalScrollBar().setUnitIncrement(16);
					panelCenterWest.add(scrollPlayerSelect, BorderLayout.CENTER);
				}
			}

			{
				panelChart = new JPanel(new BorderLayout());
				panelCenter.add(panelChart, BorderLayout.CENTER);
			}
		}

		displayFullName = false;
		listTournament = new ArrayList<Tournament>();
		listPlayers = new ArrayList<Player>();

		selectAllCheckBoxActionListener = (final ActionEvent e) -> selectAll();
		filterButtonActionListeneer = (final ActionEvent e) -> displayData();

		tournamentComboBoxActionListener = (final ActionEvent e) -> refreshYear();

		comboPeriodMode.addActionListener((final ActionEvent e) -> changePeriodParameters(true));
		periodParametersComboBoxActionListener = (final ActionEvent e) -> refreshData();
		comboTrimester.addActionListener(periodParametersComboBoxActionListener);
		comboMonth.addActionListener(periodParametersComboBoxActionListener);

		comboBoxActivated = new boolean[COMBOBOX_NUMBER];
		comboBoxActivated[COMBOBOX_PERIOD] = true;
		changePeriodParameters(false);
	}

	@Override
	public String getTabName() {
		return "MCR Tendance";
	}

	@Override
	public void setDisplayFullName(final boolean displayFullName, final boolean toRefresh) {
		this.displayFullName = displayFullName;
		if (toRefresh) {
			displayPlayerName();
			displayData();
		}
	}

	@Override
	public void refresh() {
		refreshPlayerName();
		refreshTournament();
	}

	private void refreshPlayerName() {
		listPlayers.clear();
		listPlayers.addAll(dataAccess.getMCRPlayers());
		if (listPlayers.size() > 0) {
			checkBoxSelectAll.removeActionListener(selectAllCheckBoxActionListener);
			buttonFilter.removeActionListener(filterButtonActionListeneer);

			panelPlayerSelect.removeAll();
			listCheckBoxPlayerSelect.clear();
			for (int index = 0; index < listPlayers.size(); index++) {
				final JCheckBox checkBox = new JCheckBox("", true);
				panelPlayerSelect.add(checkBox);
				listCheckBoxPlayerSelect.add(checkBox);
			}
			displayPlayerName();

			checkBoxSelectAll.addActionListener(selectAllCheckBoxActionListener);
			buttonFilter.addActionListener(filterButtonActionListeneer);
		}
	}

	private void displayPlayerName() {
		if (displayFullName) {
			Collections.sort(listPlayers, new ComparatorAscendingPlayerName());
			for (int index = 0; index < listPlayers.size(); index++) {
				listCheckBoxPlayerSelect.get(index).setText(listPlayers.get(index).getPlayerName());
			}
		} else {
			Collections.sort(listPlayers, new ComparatorAscendingPlayerDisplayName());
			for (int index = 0; index < listPlayers.size(); index++) {
				listCheckBoxPlayerSelect.get(index).setText(listPlayers.get(index).getDisplayName());
			}
		}
		repaint();
	}

	private void refreshTournament() {
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

	private void changePeriodParameters(final boolean refresh) {
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

		comboPeriodMode.setEnabled(comboBoxActivated[COMBOBOX_PERIOD]);
		comboYear.setEnabled(comboBoxActivated[COMBOBOX_YEAR_INDEX]);
		comboTrimester.setEnabled(comboBoxActivated[COMBOBOX_TRIMESTER_INDEX]);
		comboMonth.setEnabled(comboBoxActivated[COMBOBOX_MONTH_INDEX]);

		if (refresh) {
			refreshData();
		}
	}

	private void refreshData() {
		final Point location = getLocationOnScreen();
		final Dimension size = getSize();
		waitingDialog.setLocation(location.x + (size.width - waitingDialog.getWidth()) / 2, location.y + (size.height - waitingDialog.getHeight()) / 2);
		waitingDialog.setComponentShownListener(waitingDialogRefreshData);
		waitingDialog.setVisible(true);
	}

	private void refreshDataRun() {
		final EnumPeriodMode periodMode = periodModes[comboPeriodMode.getSelectedIndex()];

		final int selectedTournamentIndex = comboTournament.getSelectedIndex();
		final int selectedYearIndex = comboYear.getSelectedIndex();
		if (selectedTournamentIndex != -1 && selectedYearIndex != -1) {
			final Tournament tournament = listTournament.get(selectedTournamentIndex);
			final int year = (Integer) comboYear.getSelectedItem();
			final int trimester = comboTrimester.getSelectedIndex();
			final int month = comboMonth.getSelectedIndex();

			trend = dataAccess.getMCRDataPackageTrend(tournament, periodMode, year, trimester, month);
		}
		waitingDialog.removeComponentShownListener();
		waitingDialog.setVisible(false);

		displayData();
	}

	private void selectAll() {
		final boolean selected = checkBoxSelectAll.isSelected();
		for (int index = 0; index < listCheckBoxPlayerSelect.size(); index++) {
			listCheckBoxPlayerSelect.get(index).setSelected(selected);
		}
		repaint();
	}

	private void displayData() {
		panelChart.removeAll();
		validate();
		repaint();

		final Point location = getLocationOnScreen();
		final Dimension size = getSize();
		waitingDialog.setLocation(location.x + (size.width - waitingDialog.getWidth()) / 2, location.y + (size.height - waitingDialog.getHeight()) / 2);
		waitingDialog.setComponentShownListener(waitingDialogDisplayData);
		waitingDialog.setVisible(true);
	}

	private void displayDataRun() {
		if (trend.data.size() > 0) {
			final Set<String> selectedNames = new HashSet<String>();
			if (displayFullName) {
				for (int index = 0; index < listCheckBoxPlayerSelect.size(); index++) {
					if (listCheckBoxPlayerSelect.get(index).isSelected()) {
						selectedNames.add(listPlayers.get(index).getPlayerName());
					}
				}
			} else {
				for (int index = 0; index < listCheckBoxPlayerSelect.size(); index++) {
					if (listCheckBoxPlayerSelect.get(index).isSelected()) {
						selectedNames.add(listPlayers.get(index).getDisplayName());
					}
				}
			}

			final List<String> listName = displayFullName ? trend.playerNames : trend.displayNames;
			final List<List<Integer>> scores = trend.data;
			final List<Long> dates = trend.dates;

			final TimeSeriesCollection series = new TimeSeriesCollection();
			final XYItemRenderer sumRender = new XYLineAndShapeRenderer();
			for (int playerIndex = 0; playerIndex < listName.size(); playerIndex++) {
				final String playerName = listName.get(playerIndex);
				if (selectedNames.contains(playerName)) {
					final List<Integer> score = scores.get(playerIndex);
					final TimeSeries sumSeries = new TimeSeries(playerName);
					for (int index = 1; index < score.size(); index++) {
						sumSeries.add(new Day(new Date(dates.get(index))), score.get(index));
					}
					series.addSeries(sumSeries);
				}
			}

			int tickUnit;
			final int numberOfDays = (int) ((dates.get(dates.size() - 1) - dates.get(1)) / MILLISECONDS_PER_DAY);
			if (numberOfDays < MAX_NUMBER_OF_TICKS) {
				tickUnit = 1;
			} else {
				tickUnit = (numberOfDays / (MAX_NUMBER_OF_TICKS * TICK_UNIT_MULTIPLE) + 1) * TICK_UNIT_MULTIPLE;
			}

			final DateAxis sumDomainAxis = new DateAxis("Date");
			sumDomainAxis.setRange(new Date(dates.get(1) - MILLISECONDS_PER_DAY), new Date(dates.get(dates.size() - 1) + MILLISECONDS_PER_DAY));
			sumDomainAxis.setTickUnit(new DateTickUnit(DateTickUnitType.DAY, tickUnit));
			sumDomainAxis.setLowerMargin(0.0);
			sumDomainAxis.setUpperMargin(0.0);

			final NumberAxis sumRangeAxis = new NumberAxis("Score total");
			final XYPlot sumPlot = new XYPlot(series, sumDomainAxis, sumRangeAxis, sumRender);
			sumPlot.setBackgroundPaint(new Color(255, 255, 255, 0));
			sumPlot.setDomainGridlinePaint(Color.BLACK);
			sumPlot.setRangeGridlinePaint(Color.BLACK);
			final ValueMarker marker = new ValueMarker(0.0, Color.RED, new BasicStroke(1), null, null, 1.0f);
			sumPlot.addRangeMarker(marker);

			final ChartPanel chartPanel = new ChartPanel(new JFreeChart(sumPlot));
			chartPanel.setPopupMenu(null);
			chartPanel.setMouseZoomable(false);
			panelChart.add(chartPanel, BorderLayout.CENTER);
		}

		validate();
		repaint();

		waitingDialog.removeComponentShownListener();
		waitingDialog.setVisible(false);
	}

	@Override
	public boolean canExport() {
		return true;
	}

	private MCRDataPackageTrend trend;

	@Override
	public void export() {
		final EnumPeriodMode periodMode = periodModes[comboPeriodMode.getSelectedIndex()];

		final int selectedTournamentIndex = comboTournament.getSelectedIndex();
		final int selectedYearIndex = comboYear.getSelectedIndex();
		if (selectedTournamentIndex != -1 && selectedYearIndex != -1) {
			final Tournament tournament = listTournament.get(selectedTournamentIndex);
			final int year = (Integer) comboYear.getSelectedItem();

			if (trend != null && trend.data.size() > 0) {
				final StringBuffer proposedSaveFileName = new StringBuffer();
				proposedSaveFileName.append(tournament.getName());
				proposedSaveFileName.append("_tendance_");
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

						final List<String> listName = displayFullName ? trend.playerNames : trend.displayNames;
						final List<List<Integer>> scores = trend.data;
						final List<Long> dates = trend.dates;

						final DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.LONG, Locale.FRANCE);
						final Calendar calendar = Calendar.getInstance();

						for (int index = 0; index < listName.size(); index++) {
							writer.write(SEPARATOR);
							writer.write(listName.get(index));
						}
						writer.newLine();

						for (int dateIndex = 1; dateIndex < dates.size(); dateIndex++) {
							calendar.setTimeInMillis(dates.get(dateIndex));
							writer.write(dateFormat.format(calendar.getTime()));

							for (int playerIndex = 0; playerIndex < scores.size(); playerIndex++) {
								writer.write(SEPARATOR);
								writer.write(Integer.toString(scores.get(playerIndex).get(dateIndex)));
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
