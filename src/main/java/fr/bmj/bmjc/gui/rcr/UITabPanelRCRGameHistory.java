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
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import fr.bmj.bmjc.data.game.ComparatorDescendingTournamentID;
import fr.bmj.bmjc.data.game.Tournament;
import fr.bmj.bmjc.data.game.rcr.RCRGame;
import fr.bmj.bmjc.data.game.rcr.RCRScore;
import fr.bmj.bmjc.dataaccess.abs.rcr.DataAccessRCR;
import fr.bmj.bmjc.gui.UITabPanel;

public class UITabPanelRCRGameHistory extends UITabPanel {
	private static final long serialVersionUID = -6883738413777372692L;

	private final int NUMBER_OF_PLAYERS = 5;
	private final int NUMBER_OF_COLUMNS = 5;
	private final int COLUMN_WIDTH[] = new int[] {
		96,
		144,
		96,
		96,
		96
	};
	private final int LABEL_HEIGHT = 18;

	private boolean displayFullName;
	private final DataAccessRCR dataAccess;

	private final JLabel labelDate;
	private final JLabel labelRounds;
	private final JLabel labelGameInfos[][];

	private final JComboBox<String> comboTournament;
	private final ActionListener tournamentComboBoxActionListener;
	private final JTree treeIds;
	private final DefaultTreeModel treeModel;
	private TreePath selectedPath;
	private Long selectedId;

	private final JSpinner spinnerInitialScore;
	private final SimpleDateFormat dateFormat;
	private final Calendar calendar;
	private final DecimalFormat normalDecimalFormat;
	private final DecimalFormat finalScoreDecimalFormat;

	private final List<Tournament> listTournament;

	public UITabPanelRCRGameHistory(final DataAccessRCR dataAccess) {
		this.dataAccess = dataAccess;
		displayFullName = false;

		dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		calendar = Calendar.getInstance();
		normalDecimalFormat = new DecimalFormat("#,###");
		final DecimalFormatSymbols normalFormatSymbols = normalDecimalFormat.getDecimalFormatSymbols();
		normalFormatSymbols.setGroupingSeparator(' ');
		normalDecimalFormat.setDecimalFormatSymbols(normalFormatSymbols);

		finalScoreDecimalFormat = new DecimalFormat("+#,###;-#,###");
		final DecimalFormatSymbols finalScoreFormatSymbols = finalScoreDecimalFormat.getDecimalFormatSymbols();
		finalScoreFormatSymbols.setGroupingSeparator(' ');
		finalScoreDecimalFormat.setDecimalFormatSymbols(finalScoreFormatSymbols);

		JPanel leftComponent;
		JPanel rightComponent;

		{
			leftComponent = new JPanel();
			leftComponent.setLayout(new GridBagLayout());

			final GridBagConstraints c = new GridBagConstraints();
			c.gridy = 0;
			c.gridx = 0;
			c.fill = GridBagConstraints.NONE;
			c.weighty = 0.0;
			c.weightx = 0.0;
			final JLabel labelTournament = new JLabel("Tournoi : ");
			leftComponent.add(labelTournament,
				c);

			tournamentComboBoxActionListener = (final ActionEvent e) -> refreshTree();
			comboTournament = new JComboBox<String>();
			comboTournament.setEditable(false);
			comboTournament.addActionListener(tournamentComboBoxActionListener);
			c.gridx = 1;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.weightx = 1.0;
			leftComponent.add(comboTournament,
				c);

			treeModel = new DefaultTreeModel(null);
			treeIds = new JTree(treeModel);
			treeIds.setRootVisible(true);
			treeIds.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
			treeIds.getSelectionModel().addTreeSelectionListener((final TreeSelectionEvent e) -> selectGame());
			final JScrollPane scrollList = new JScrollPane(treeIds,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
			c.gridy = 1;
			c.gridx = 0;
			c.gridwidth = 2;
			c.fill = GridBagConstraints.BOTH;
			c.weighty = 1.0;
			c.weightx = 1.0;
			leftComponent.add(scrollList,
				c);
		}

		{
			final Dimension labelSizes[] = new Dimension[NUMBER_OF_COLUMNS];

			final JPanel centerPanel = new JPanel(new GridBagLayout());
			final GridBagConstraints constraints = new GridBagConstraints(0,
				0,
				1,
				1,
				0.0,
				0.0,
				GridBagConstraints.CENTER,
				GridBagConstraints.BOTH,
				new Insets(0,
					0,
					0,
					0),
				8,
				8);

			for (int index = 0; index < NUMBER_OF_COLUMNS; index++) {
				labelSizes[index] = new Dimension(COLUMN_WIDTH[index],
					LABEL_HEIGHT);
			}

			{
				constraints.gridy = 0;
				constraints.gridx = 0;
				final JLabel labelTitleStack = new JLabel("Stack init.: ",
					SwingConstants.RIGHT);
				labelTitleStack.setPreferredSize(labelSizes[0]);
				centerPanel.add(labelTitleStack,
					constraints);

				constraints.gridx = 1;
				spinnerInitialScore = new JSpinner(new SpinnerNumberModel(30000,
					0,
					30000,
					1000));
				spinnerInitialScore.setEditor(new JSpinner.NumberEditor(spinnerInitialScore,
					"#"));
				spinnerInitialScore.setPreferredSize(labelSizes[1]);
				spinnerInitialScore.addChangeListener((final ChangeEvent e) -> displayGame());
				centerPanel.add(spinnerInitialScore,
					constraints);

				constraints.gridy = 2;
				constraints.gridx = 0;
				final JLabel labelTitleDate = new JLabel("Date : ",
					SwingConstants.RIGHT);
				labelTitleDate.setPreferredSize(labelSizes[0]);
				centerPanel.add(labelTitleDate,
					constraints);

				constraints.gridx = 1;
				constraints.gridwidth = 2;
				labelDate = new JLabel("",
					SwingConstants.LEFT);
				labelDate.setPreferredSize(new Dimension(COLUMN_WIDTH[1] + COLUMN_WIDTH[2],
					LABEL_HEIGHT));
				centerPanel.add(labelDate,
					constraints);

				constraints.gridx = 3;
				constraints.gridwidth = 1;
				final JLabel labelTitleRounds = new JLabel("Manche : ",
					SwingConstants.RIGHT);
				labelTitleRounds.setPreferredSize(labelSizes[3]);
				centerPanel.add(labelTitleRounds,
					constraints);

				constraints.gridx = 4;
				labelRounds = new JLabel("",
					SwingConstants.LEFT);
				labelRounds.setPreferredSize(labelSizes[4]);
				centerPanel.add(labelRounds,
					constraints);
			}

			{
				constraints.gridy = 3;
				constraints.gridx = 0;
				final JLabel labelTitleRanking = new JLabel("#",
					SwingConstants.CENTER);
				labelTitleRanking.setBorder(BorderFactory.createLineBorder(Color.BLACK));
				labelTitleRanking.setPreferredSize(labelSizes[0]);
				centerPanel.add(labelTitleRanking,
					constraints);

				constraints.gridx = 1;
				final JLabel labelTitleName = new JLabel("Nom du joueur",
					SwingConstants.CENTER);
				labelTitleName.setBorder(BorderFactory.createLineBorder(Color.BLACK));
				labelTitleName.setPreferredSize(labelSizes[1]);
				centerPanel.add(labelTitleName,
					constraints);

				constraints.gridx = 2;
				final JLabel labelTitleGameScore = new JLabel("Stack",
					SwingConstants.CENTER);
				labelTitleGameScore.setBorder(BorderFactory.createLineBorder(Color.BLACK));
				labelTitleGameScore.setPreferredSize(labelSizes[2]);
				centerPanel.add(labelTitleGameScore,
					constraints);

				constraints.gridx = 3;
				final JLabel labelTitleUma = new JLabel("UMA",
					SwingConstants.CENTER);
				labelTitleUma.setBorder(BorderFactory.createLineBorder(Color.BLACK));
				labelTitleUma.setPreferredSize(labelSizes[3]);
				centerPanel.add(labelTitleUma,
					constraints);

				constraints.gridx = 4;
				final JLabel labelTitleScore = new JLabel("Score",
					SwingConstants.CENTER);
				labelTitleScore.setBorder(BorderFactory.createLineBorder(Color.BLACK));
				labelTitleScore.setPreferredSize(labelSizes[4]);
				centerPanel.add(labelTitleScore,
					constraints);
			}

			{
				labelGameInfos = new JLabel[NUMBER_OF_PLAYERS][NUMBER_OF_COLUMNS];
				for (int playerIndex = 0; playerIndex < NUMBER_OF_PLAYERS; playerIndex++) {
					constraints.gridy = playerIndex + 4;
					for (int columnIndex = 0; columnIndex < NUMBER_OF_COLUMNS; columnIndex++) {
						constraints.gridx = columnIndex;
						labelGameInfos[playerIndex][columnIndex] = new JLabel("",
							SwingConstants.CENTER);
						labelGameInfos[playerIndex][columnIndex].setBorder(BorderFactory.createLineBorder(Color.BLACK));
						labelGameInfos[playerIndex][columnIndex].setPreferredSize(labelSizes[columnIndex]);
						centerPanel.add(labelGameInfos[playerIndex][columnIndex],
							constraints);
					}
				}
			}

			{
				rightComponent = new JPanel(new GridBagLayout());
				final GridBagConstraints c = new GridBagConstraints(0,
					0,
					1,
					1,
					1.0,
					1.0,
					GridBagConstraints.NORTH,
					GridBagConstraints.NONE,
					new Insets(0,
						0,
						0,
						0),
					0,
					0);
				rightComponent.setMinimumSize(new Dimension(600,
					600));
				rightComponent.add(centerPanel,
					c);
			}
		}

		setLayout(new BorderLayout());
		final JSplitPane centerPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
			leftComponent,
			rightComponent);
		centerPane.setResizeWeight(0.5);
		add(centerPane,
			BorderLayout.CENTER);

		listTournament = new ArrayList<Tournament>();
	}

	@Override
	public String getTabName() {
		return "RCR Historique";
	}

	@Override
	public void setDisplayFullName(final boolean displayFullName,
		final boolean toRefresh) {
		this.displayFullName = displayFullName;
		if (toRefresh) {
			displayGame();
		}
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
					Collections.sort(listTournament,
						new ComparatorDescendingTournamentID());

					comboTournament.removeActionListener(tournamentComboBoxActionListener);
					comboTournament.removeAllItems();
					for (int index = 0; index < listTournament.size(); index++) {
						final Tournament tournament = listTournament.get(index);
						comboTournament.addItem(tournament.getName());
					}

					comboTournament.addActionListener(tournamentComboBoxActionListener);
					if (listTournament.size() > 0) {
						comboTournament.setSelectedIndex(0);
					} else {
						comboTournament.setSelectedIndex(-1);
					}
				}
			} catch (final Exception e) {
				JOptionPane.showMessageDialog(this,
					e.getMessage(),
					"Erreur",
					JOptionPane.ERROR_MESSAGE);
			}
		}).start();
	}

	private void refreshTree() {
		new Thread(() -> {
			try {
				invalidate();
				final String monthStrings[] = DateFormatSymbols.getInstance(Locale.FRANCE).getMonths();
				final int selectedTournamentIndex = comboTournament.getSelectedIndex();
				if (listTournament.size() > 0 && selectedTournamentIndex >= 0) {
					final Tournament tournament = listTournament.get(selectedTournamentIndex);
					final List<Integer> yearList = new ArrayList<Integer>(dataAccess.getRCRYears(tournament));
					Collections.sort(yearList);

					final DefaultMutableTreeNode root = new DefaultMutableTreeNode(tournament.getName());
					for (int index = 0; index < yearList.size(); index++) {
						final int year = yearList.get(index);
						final DefaultMutableTreeNode nodeYear = new DefaultMutableTreeNode(year);
						root.add(nodeYear);

						for (int month = 0; month < 12; month++) {
							final List<Integer> days = dataAccess.getRCRGameDays(tournament,
								year,
								month);
							if (days.size() > 0) {
								Collections.sort(days);
								final DefaultMutableTreeNode nodeMonth = new DefaultMutableTreeNode(monthStrings[month]);
								nodeYear.add(nodeMonth);

								for (int dayIndex = 0; dayIndex < days.size(); dayIndex++) {
									final int day = days.get(dayIndex);
									final List<Long> idList = dataAccess.getRCRGameIds(tournament,
										year,
										month,
										day);
									Collections.sort(idList);
									final DefaultMutableTreeNode nodeDay = new DefaultMutableTreeNode(day);
									nodeMonth.add(nodeDay);

									for (int idIndex = 0; idIndex < idList.size(); idIndex++) {
										final DefaultMutableTreeNode nodeId = new DefaultMutableTreeNode(idList.get(idIndex));
										nodeDay.add(nodeId);
									}
								}
							}
						}
					}
					treeModel.setRoot(root);
				} else {
					treeModel.setRoot(null);
				}
				selectedPath = null;
				validate();
				repaint();
			} catch (final Exception e) {
				JOptionPane.showMessageDialog(this,
					e.getMessage(),
					"Erreur",
					JOptionPane.ERROR_MESSAGE);
			}
		}).start();
	}

	private void selectGame() {
		selectedPath = treeIds.getSelectionPath();
		if (selectedPath != null) {
			final DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) selectedPath.getLastPathComponent();
			if (selectedNode.isLeaf()) {
				selectedId = (Long) selectedNode.getUserObject();
			} else {
				selectedId = null;
			}
		} else {
			selectedId = null;
		}
		displayGame();
	}

	private void displayGame() {
		new Thread(() -> {
			try {
				if (selectedId != null) {
					final RCRGame game = dataAccess.getRCRGame(selectedId);
					if (game != null) {
						calendar.set(game.getYear(),
							game.getMonth(),
							game.getDay());
						labelDate.setText(dateFormat.format(calendar.getTime()));
						labelRounds.setText(normalDecimalFormat.format(game.getNbRounds()));
						final int initStack = ((Integer) spinnerInitialScore.getValue()).intValue();

						if (displayFullName) {
							for (int index = 0; index < game.getScores().size(); index++) {
								final RCRScore score = game.getScores().get(index);
								labelGameInfos[index][0].setText(normalDecimalFormat.format(score.getPlace()));
								labelGameInfos[index][1].setText(score.getPlayerName());
								labelGameInfos[index][2].setText(normalDecimalFormat.format(score.getGameScore() + initStack));
								labelGameInfos[index][3].setText(normalDecimalFormat.format(score.getUmaScore()));
								final int finalScore = score.getFinalScore();
								labelGameInfos[index][4].setText(finalScoreDecimalFormat.format(finalScore));
								if (finalScore >= 0) {
									labelGameInfos[index][4].setForeground(Color.BLACK);
								} else {
									labelGameInfos[index][4].setForeground(Color.RED);
								}
							}
						} else {
							for (int index = 0; index < game.getScores().size(); index++) {
								final RCRScore score = game.getScores().get(index);
								labelGameInfos[index][0].setText(normalDecimalFormat.format(score.getPlace()));
								labelGameInfos[index][1].setText(score.getDisplayName());
								labelGameInfos[index][2].setText(normalDecimalFormat.format(score.getGameScore() + initStack));
								labelGameInfos[index][3].setText(normalDecimalFormat.format(score.getUmaScore()));
								final int finalScore = score.getFinalScore();
								labelGameInfos[index][4].setText(finalScoreDecimalFormat.format(finalScore));
								if (finalScore >= 0) {
									labelGameInfos[index][4].setForeground(Color.BLACK);
								} else {
									labelGameInfos[index][4].setForeground(Color.RED);
								}
							}
						}

						for (int playerIndex = game.getScores().size(); playerIndex < NUMBER_OF_PLAYERS; playerIndex++) {
							for (int columnIndex = 0; columnIndex < NUMBER_OF_COLUMNS; columnIndex++) {
								labelGameInfos[playerIndex][columnIndex].setText("");
							}
						}
					} else {
						clearGame();
					}
				} else {
					clearGame();
				}
				repaint();
			} catch (final Exception e) {
				JOptionPane.showMessageDialog(this,
					e.getMessage(),
					"Erreur",
					JOptionPane.ERROR_MESSAGE);
			}
		}).start();
	}

	private void clearGame() {
		new Thread(() -> {
			try {
				labelDate.setText("");
				labelRounds.setText("");
				for (int playerIndex = 0; playerIndex < NUMBER_OF_PLAYERS; playerIndex++) {
					for (int columnIndex = 0; columnIndex < NUMBER_OF_COLUMNS; columnIndex++) {
						labelGameInfos[playerIndex][columnIndex].setText("");
					}
				}
			} catch (final Exception e) {
				JOptionPane.showMessageDialog(this,
					e.getMessage(),
					"Erreur",
					JOptionPane.ERROR_MESSAGE);
			}
		}).start();
	}
}
