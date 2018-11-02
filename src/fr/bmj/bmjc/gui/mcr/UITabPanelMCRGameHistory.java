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
import java.awt.event.ComponentEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
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
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import fr.bmj.bmjc.data.game.ComparatorDescendingTournamentID;
import fr.bmj.bmjc.data.game.Tournament;
import fr.bmj.bmjc.data.game.mcr.MCRGame;
import fr.bmj.bmjc.data.game.mcr.MCRScore;
import fr.bmj.bmjc.dataaccess.mcr.DataAccessMCR;
import fr.bmj.bmjc.gui.UITabPanel;
import fr.bmj.bmjc.swing.ComponentShownListener;
import fr.bmj.bmjc.swing.JDialogWithProgress;

public class UITabPanelMCRGameHistory extends UITabPanel {
	private static final long serialVersionUID = 2864354782911474988L;

	private final int NUMBER_OF_PLAYERS = 4;
	private final int NUMBER_OF_COLUMNS = 4;
	private final int COLUMN_WIDTH[] = new int[] {
		96, 144, 96, 96
	};
	private final int LABEL_HEIGHT = 18;

	private boolean displayFullName;
	private final DataAccessMCR dataAccess;
	private final JDialogWithProgress waitingDialog;
	private final ComponentShownListener waitingDialogShowListener;

	private final JLabel labelDate;
	private final JLabel labelGameInfos[][];

	private final JComboBox<String> comboTournament;
	private final ActionListener tournamentComboBoxActionListener;
	private final JTree treeIds;
	private final DefaultTreeModel treeModel;
	private TreePath selectedPath;
	private Integer selectedId;

	private final DateFormat dateFormat;
	private final Calendar calendar;
	private final DecimalFormat decimalFormat;

	private final List<Tournament> listTournament;

	public UITabPanelMCRGameHistory(final DataAccessMCR dataAccess, final JDialogWithProgress waitingDialog) {
		this.dataAccess = dataAccess;
		this.waitingDialog = waitingDialog;
		waitingDialogShowListener = (final ComponentEvent e) -> new Thread(() -> refreshTreeRun()).start();
		displayFullName = false;

		dateFormat = DateFormat.getDateInstance(DateFormat.FULL, Locale.FRANCE);
		calendar = Calendar.getInstance();
		decimalFormat = new DecimalFormat("#,###");
		final DecimalFormatSymbols symbols = decimalFormat.getDecimalFormatSymbols();
		symbols.setGroupingSeparator(' ');
		decimalFormat.setDecimalFormatSymbols(symbols);

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
			leftComponent.add(labelTournament, c);

			tournamentComboBoxActionListener = (final ActionEvent e) -> refreshTree();
			comboTournament = new JComboBox<String>();
			comboTournament.setEditable(false);
			comboTournament.addActionListener(tournamentComboBoxActionListener);
			c.gridx = 1;
			c.fill = GridBagConstraints.HORIZONTAL;
			c.weightx = 1.0;
			leftComponent.add(comboTournament, c);

			treeModel = new DefaultTreeModel(null);
			treeIds = new JTree(treeModel);
			treeIds.setRootVisible(true);
			treeIds.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
			treeIds.getSelectionModel().addTreeSelectionListener((final TreeSelectionEvent e) -> selectGame());
			final JScrollPane scrollList = new JScrollPane(treeIds, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			c.gridy = 1;
			c.gridx = 0;
			c.gridwidth = 2;
			c.fill = GridBagConstraints.BOTH;
			c.weighty = 1.0;
			c.weightx = 1.0;
			leftComponent.add(scrollList, c);
		}

		{
			final Dimension labelSizes[] = new Dimension[NUMBER_OF_COLUMNS];

			final JPanel centerPanel = new JPanel(new GridBagLayout());
			final GridBagConstraints constraints = new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 8, 8);

			for (int index = 0; index < NUMBER_OF_COLUMNS; index++) {
				labelSizes[index] = new Dimension(COLUMN_WIDTH[index], LABEL_HEIGHT);
			}

			{
				constraints.gridy = 0;
				constraints.gridx = 0;
				final JLabel labelTitleDate = new JLabel("Date : ", JLabel.RIGHT);
				labelTitleDate.setPreferredSize(labelSizes[0]);
				centerPanel.add(labelTitleDate, constraints);

				constraints.gridx = 1;
				constraints.gridwidth = 2;
				labelDate = new JLabel("", JLabel.LEFT);
				labelDate.setPreferredSize(new Dimension(COLUMN_WIDTH[1] + COLUMN_WIDTH[2], LABEL_HEIGHT));
				centerPanel.add(labelDate, constraints);
			}

			{
				constraints.gridy = 1;
				constraints.gridx = 0;
				constraints.gridwidth = 1;
				final JLabel labelTitleRanking = new JLabel("#", JLabel.CENTER);
				labelTitleRanking.setBorder(BorderFactory.createLineBorder(Color.BLACK));
				labelTitleRanking.setPreferredSize(labelSizes[0]);
				centerPanel.add(labelTitleRanking, constraints);

				constraints.gridx = 1;
				final JLabel labelTitleName = new JLabel("Nom du joueur", JLabel.CENTER);
				labelTitleName.setBorder(BorderFactory.createLineBorder(Color.BLACK));
				labelTitleName.setPreferredSize(labelSizes[1]);
				centerPanel.add(labelTitleName, constraints);

				constraints.gridx = 2;
				final JLabel labelTitleGameScore = new JLabel("Score", JLabel.CENTER);
				labelTitleGameScore.setBorder(BorderFactory.createLineBorder(Color.BLACK));
				labelTitleGameScore.setPreferredSize(labelSizes[2]);
				centerPanel.add(labelTitleGameScore, constraints);

				constraints.gridx = 3;
				final JLabel labelTitleScore = new JLabel("Score final", JLabel.CENTER);
				labelTitleScore.setBorder(BorderFactory.createLineBorder(Color.BLACK));
				labelTitleScore.setPreferredSize(labelSizes[3]);
				centerPanel.add(labelTitleScore, constraints);
			}

			{
				labelGameInfos = new JLabel[NUMBER_OF_PLAYERS][NUMBER_OF_COLUMNS];
				for (int playerIndex = 0; playerIndex < NUMBER_OF_PLAYERS; playerIndex++) {
					constraints.gridy = playerIndex + 2;
					for (int columnIndex = 0; columnIndex < NUMBER_OF_COLUMNS; columnIndex++) {
						constraints.gridx = columnIndex;
						labelGameInfos[playerIndex][columnIndex] = new JLabel("", JLabel.CENTER);
						labelGameInfos[playerIndex][columnIndex].setBorder(BorderFactory.createLineBorder(Color.BLACK));
						labelGameInfos[playerIndex][columnIndex].setPreferredSize(labelSizes[columnIndex]);
						centerPanel.add(labelGameInfos[playerIndex][columnIndex], constraints);
					}
				}
			}

			{
				rightComponent = new JPanel(new GridBagLayout());
				final GridBagConstraints c = new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.NORTH, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0);
				rightComponent.setMinimumSize(new Dimension(600, 600));
				rightComponent.add(centerPanel, c);
			}
		}

		setLayout(new BorderLayout());
		final JSplitPane centerPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftComponent, rightComponent);
		centerPane.setResizeWeight(0.5);
		add(centerPane, BorderLayout.CENTER);

		listTournament = new ArrayList<Tournament>();
	}

	@Override
	public String getTabName() {
		return "MCR Historique";
	}

	@Override
	public void setDisplayFullName(final boolean displayFullName, final boolean toRefresh) {
		this.displayFullName = displayFullName;
		if (toRefresh) {
			displayGame();
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
			} else {
				comboTournament.setSelectedIndex(-1);
			}
		}
	}

	private void refreshTree() {
		final Point location = getLocationOnScreen();
		final Dimension size = getSize();
		waitingDialog.setLocation(location.x + (size.width - waitingDialog.getWidth()) / 2, location.y + (size.height - waitingDialog.getHeight()) / 2);
		waitingDialog.setComponentShownListener(waitingDialogShowListener);
		waitingDialog.setVisible(true);
	}

	private void refreshTreeRun() {
		invalidate();
		final String monthStrings[] = DateFormatSymbols.getInstance(Locale.FRANCE).getMonths();
		final int selectedTournamentIndex = comboTournament.getSelectedIndex();
		if (listTournament.size() > 0 && selectedTournamentIndex >= 0) {
			final Tournament tournament = listTournament.get(selectedTournamentIndex);
			final List<Integer> yearList = new ArrayList<Integer>(dataAccess.getMCRYears(tournament));
			Collections.sort(yearList);

			final int totalSize = yearList.size() * 12;
			final DefaultMutableTreeNode root = new DefaultMutableTreeNode(tournament.getName());
			for (int index = 0; index < yearList.size(); index++) {
				final int year = yearList.get(index);
				final DefaultMutableTreeNode nodeYear = new DefaultMutableTreeNode(year);
				root.add(nodeYear);

				for (int month = 0; month < 12; month++) {
					final List<Integer> days = dataAccess.getMCRGameDays(tournament, year, month);
					if (days.size() > 0) {
						Collections.sort(days);
						final DefaultMutableTreeNode nodeMonth = new DefaultMutableTreeNode(monthStrings[month]);
						nodeYear.add(nodeMonth);

						for (int dayIndex = 0; dayIndex < days.size(); dayIndex++) {
							final int day = days.get(dayIndex);
							final List<Integer> idList = dataAccess.getMCRGameIds(tournament, year, month, day);
							Collections.sort(idList);
							final DefaultMutableTreeNode nodeDay = new DefaultMutableTreeNode(day);
							nodeMonth.add(nodeDay);

							for (int idIndex = 0; idIndex < idList.size(); idIndex++) {
								final DefaultMutableTreeNode nodeId = new DefaultMutableTreeNode(idList.get(idIndex));
								nodeDay.add(nodeId);
							}
						}
					}
					waitingDialog.setProgress((index * 12 + month) * 100 / totalSize);
				}
			}
			treeModel.setRoot(root);
		} else {
			treeModel.setRoot(null);
		}
		selectedPath = null;
		validate();
		repaint();

		waitingDialog.removeComponentShownListener();
		waitingDialog.setVisible(false);
	}

	private void selectGame() {
		selectedPath = treeIds.getSelectionPath();
		if (selectedPath != null) {
			final DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) selectedPath.getLastPathComponent();
			if (selectedNode.isLeaf()) {
				selectedId = (Integer) selectedNode.getUserObject();
			} else {
				selectedId = null;
			}
		} else {
			selectedId = null;
		}
		displayGame();
	}

	private void displayGame() {
		if (selectedId != null) {
			final MCRGame game = dataAccess.getMCRGame(selectedId);
			if (game != null) {
				calendar.set(game.getYear(), game.getMonth(), game.getDay());
				labelDate.setText(dateFormat.format(calendar.getTime()));

				if (displayFullName) {
					for (int index = 0; index < game.getScores().size(); index++) {
						final MCRScore score = game.getScores().get(index);
						labelGameInfos[index][0].setText(decimalFormat.format(score.getPlace()));
						labelGameInfos[index][1].setText(score.getPlayerName());
						labelGameInfos[index][2].setText(decimalFormat.format(score.getGameScore()));
						labelGameInfos[index][3].setText(decimalFormat.format(score.getFinalScore()));
					}
				} else {
					for (int index = 0; index < game.getScores().size(); index++) {
						final MCRScore score = game.getScores().get(index);
						labelGameInfos[index][0].setText(decimalFormat.format(score.getPlace()));
						labelGameInfos[index][1].setText(score.getDisplayName());
						labelGameInfos[index][2].setText(decimalFormat.format(score.getGameScore()));
						labelGameInfos[index][3].setText(decimalFormat.format(score.getFinalScore()));
					}
				}
			} else {
				clearGame();
			}
		} else {
			clearGame();
		}
		repaint();
	}

	private void clearGame() {
		labelDate.setText("");
		for (int playerIndex = 0; playerIndex < NUMBER_OF_PLAYERS; playerIndex++) {
			for (int columnIndex = 0; columnIndex < NUMBER_OF_COLUMNS; columnIndex++) {
				labelGameInfos[playerIndex][columnIndex].setText("");
			}
		}
	}

	@Override
	public boolean canExport() {
		return true;
	}

	@Override
	public void export() {
		if (selectedPath != null) {
			final Object[] path = selectedPath.getPath();
			if (path != null && path.length >= 1) {
				final StringBuffer proposedSaveFileName = new StringBuffer();
				proposedSaveFileName.append(((DefaultMutableTreeNode) path[0]).getUserObject().toString());
				for (int index = 1; index < path.length; index++) {
					proposedSaveFileName.append("_");
					proposedSaveFileName.append(((DefaultMutableTreeNode) path[index]).getUserObject().toString());
				}
				proposedSaveFileName.append(".csv");
				final File fileSaveFile = askSaveFileName(proposedSaveFileName.toString());
				if (fileSaveFile != null) {
					BufferedWriter writer = null;
					try {
						writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileSaveFile), Charset.forName("UTF-8")));
						exportChildren((DefaultMutableTreeNode) selectedPath.getLastPathComponent(), writer);
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

	private void exportChildren(final DefaultMutableTreeNode node, final BufferedWriter writer) throws Exception {
		if (node != null && writer != null) {
			if (!node.isLeaf()) {
				for (int index = 0; index < node.getChildCount(); index++) {
					exportChildren((DefaultMutableTreeNode) node.getChildAt(index), writer);
				}
			} else {
				final Integer id = (Integer) node.getUserObject();
				final MCRGame game = dataAccess.getMCRGame(id);
				if (game != null) {
					calendar.set(game.getYear(), game.getMonth(), game.getDay());
					writer.write("Date");
					writer.write(SEPARATOR);
					writer.write(dateFormat.format(calendar.getTime()));
					writer.newLine();

					if (displayFullName) {
						for (int index = 0; index < game.getScores().size(); index++) {
							final MCRScore score = game.getScores().get(index);
							writer.write(Integer.toString(score.getPlace()));
							writer.write(SEPARATOR);
							writer.write(score.getPlayerName());
							writer.write(SEPARATOR);
							writer.write(Integer.toString(score.getGameScore()));
							writer.write(SEPARATOR);
							writer.write(Integer.toString(score.getFinalScore()));
							writer.newLine();
						}
					} else {
						for (int index = 0; index < game.getScores().size(); index++) {
							final MCRScore score = game.getScores().get(index);
							writer.write(Integer.toString(score.getPlace()));
							writer.write(SEPARATOR);
							writer.write(score.getDisplayName());
							writer.write(SEPARATOR);
							writer.write(Integer.toString(score.getGameScore()));
							writer.write(SEPARATOR);
							writer.write(Integer.toString(score.getFinalScore()));
							writer.newLine();
						}
					}
					writer.newLine();
				}
			}
		}
	}
}
