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
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FocusTraversalPolicy;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DateFormat;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComboBox.KeySelectionManager;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import fr.bmj.bmjc.data.game.ComparatorAscendingPlayerDisplayName;
import fr.bmj.bmjc.data.game.ComparatorAscendingPlayerName;
import fr.bmj.bmjc.data.game.ComparatorDescendingTournamentID;
import fr.bmj.bmjc.data.game.Player;
import fr.bmj.bmjc.data.game.Tournament;
import fr.bmj.bmjc.data.game.mcr.MCRGame;
import fr.bmj.bmjc.data.game.mcr.MCRScore;
import fr.bmj.bmjc.dataaccess.UpdateResult;
import fr.bmj.bmjc.dataaccess.mcr.DataAccessMCR;
import fr.bmj.bmjc.gui.UITabPanel;
import fr.bri.awt.ProportionalGridLayout;
import fr.bri.awt.ProportionalGridLayoutConstraint;
import fr.bri.swing.JDatePicker;
import fr.bri.swing.JDialogWithProgress;

public class UITabPanelMCRNewGame extends UITabPanel {
	private static final long serialVersionUID = -637957336947436609L;

	private static final String STRING_SPACE = " ";
	private static final String STRING_EMPTY = "";

	private static final int NUMBER_OF_PLAYERS = 4;

	private static final int FINAL_SCORES[] = {
		48, 24, 12, 0
	};

	private boolean displayFullName;
	private final DataAccessMCR dataAccess;

	private final JComboBox<String> comboBoxTournament;
	private final JLabel labelDate;

	private final List<JLabel> labelPlayerRankings;
	private final List<JComboBox<String>> comboBoxPlayers;
	private final List<ComboBoxModel<String>> comboBoxModels;
	private final List<JSpinner> spinnerPlayerGameScores;
	private final List<JLabel> labelPlayerFinalScores;

	private final JButton buttonCalculate;
	private final JButton buttonReset;
	private final JButton buttonSave;
	private final JLabel labelScoreError;

	private final Calendar calendar;
	private final DateFormat dateFormat;

	private final List<Player> players;
	private final List<String> normalizedPlayerNames;
	private final List<Tournament> listTournament;

	public UITabPanelMCRNewGame(final DataAccessMCR dataAccess, final JDialogWithProgress waitingDialog) {
		this.dataAccess = dataAccess;

		final Dimension buttonMinSize = new Dimension(BUTTON_MIN_WIDTH, BUTTON_MIN_HEIGHT);
		final JPanel innerPanel = new JPanel();
		innerPanel.setLayout(new BorderLayout(12, 12));
		innerPanel.setBorder(BorderFactory.createLineBorder(Color.CYAN));
		{
			final JPanel northPanel = new JPanel(new ProportionalGridLayout(1, 8, 8, 8));
			final ProportionalGridLayoutConstraint c = new ProportionalGridLayoutConstraint(0, 1, 0, 1);

			c.y = 0;
			c.x = 0;
			northPanel.add(new JLabel("Date: ", SwingConstants.RIGHT), c);
			labelDate = new JLabel(STRING_EMPTY);
			labelDate.setBorder(BorderFactory.createLineBorder(Color.BLACK));
			labelDate.addMouseListener(new LabelDateMouseListener());
			c.x = 1;
			northPanel.add(labelDate, c);

			c.x = 2;
			northPanel.add(new JLabel("Tournoi: ", SwingConstants.RIGHT), c);
			listTournament = new ArrayList<Tournament>();
			comboBoxTournament = new JComboBox<String>();
			c.x = 3;
			c.gridWidth = 5;
			northPanel.add(comboBoxTournament, c);

			innerPanel.add(northPanel, BorderLayout.NORTH);
		}

		{
			final JPanel centerPanel = new JPanel(new ProportionalGridLayout(6, 8, 4, 4));
			final ProportionalGridLayoutConstraint centerC = new ProportionalGridLayoutConstraint(0, 1, 0, 1);
			final Dimension labelSize = new Dimension(80, 25);

			centerC.y = 0;
			centerC.x = 0;
			centerC.gridWidth = 1;
			centerPanel.add(new JLabel("#", SwingConstants.CENTER), centerC);

			centerC.x = 1;
			centerC.gridWidth = 3;
			centerPanel.add(new JLabel("Joueur", SwingConstants.CENTER), centerC);

			centerC.x = 4;
			centerC.gridWidth = 2;
			centerPanel.add(new JLabel("Score", SwingConstants.CENTER), centerC);

			centerC.x = 6;
			centerPanel.add(new JLabel("Score final", SwingConstants.CENTER), centerC);

			labelPlayerRankings = new ArrayList<JLabel>(NUMBER_OF_PLAYERS);
			comboBoxPlayers = new ArrayList<JComboBox<String>>(NUMBER_OF_PLAYERS);
			comboBoxModels = new ArrayList<ComboBoxModel<String>>(NUMBER_OF_PLAYERS);
			spinnerPlayerGameScores = new ArrayList<JSpinner>(NUMBER_OF_PLAYERS);
			labelPlayerFinalScores = new ArrayList<JLabel>(NUMBER_OF_PLAYERS);
			final ChangeListener scoreSpinnerChangeListener = (final ChangeEvent e) -> disableSaveButton();

			for (int playerIndex = 0; playerIndex < NUMBER_OF_PLAYERS; playerIndex++) {
				centerC.y = 1 + playerIndex;

				final JLabel labelPlayerRanking = new JLabel(STRING_EMPTY, SwingConstants.CENTER);
				labelPlayerRanking.setBorder(BorderFactory.createLoweredSoftBevelBorder());
				labelPlayerRanking.setPreferredSize(labelSize);
				labelPlayerRankings.add(playerIndex, labelPlayerRanking);
				centerC.x = 0;
				centerC.gridWidth = 1;
				centerPanel.add(labelPlayerRanking, centerC);

				final JComboBox<String> comboBoxPlayer = new JComboBox<String>();
				comboBoxPlayer.setKeySelectionManager(new WebPageKeySelectionManager());
				comboBoxPlayers.add(playerIndex, comboBoxPlayer);
				comboBoxModels.add(playerIndex, comboBoxPlayer.getModel());
				centerC.x = 1;
				centerC.gridWidth = 3;
				centerPanel.add(comboBoxPlayer, centerC);

				final JSpinner spinnerPlayerGameScore = new JSpinner(new SpinnerNumberModel(0, -20000, 20000, 1));
				spinnerPlayerGameScores.add(playerIndex, spinnerPlayerGameScore);
				spinnerPlayerGameScore.addChangeListener(scoreSpinnerChangeListener);
				centerC.x = 4;
				centerC.gridWidth = 2;
				centerPanel.add(spinnerPlayerGameScore, centerC);

				final JLabel labelPlayerFinalScore = new JLabel(STRING_EMPTY);
				labelPlayerFinalScore.setBorder(BorderFactory.createLoweredSoftBevelBorder());
				labelPlayerFinalScore.setPreferredSize(labelSize);
				labelPlayerFinalScores.add(playerIndex, labelPlayerFinalScore);
				centerC.x = 6;
				centerPanel.add(labelPlayerFinalScore, centerC);
			}

			labelScoreError = new JLabel(STRING_SPACE, SwingConstants.RIGHT);
			labelScoreError.setBorder(BorderFactory.createLineBorder(Color.BLACK));
			labelScoreError.setForeground(Color.RED);
			centerC.y = 5;
			centerC.x = 4;
			centerC.gridWidth = 2;
			centerPanel.add(labelScoreError, centerC);

			innerPanel.add(centerPanel, BorderLayout.CENTER);
		}

		{
			final JPanel southPanel = new JPanel(new GridLayout(1, 7));

			southPanel.add(new JPanel());

			buttonCalculate = new JButton("Calculer");
			buttonCalculate.setPreferredSize(buttonMinSize);
			buttonCalculate.addActionListener((final ActionEvent e) -> calculateFinalScore());
			southPanel.add(buttonCalculate);

			southPanel.add(new JPanel());

			buttonSave = new JButton("Enregistrer");
			buttonSave.setPreferredSize(buttonMinSize);
			buttonSave.addActionListener((final ActionEvent e) -> saveScore());
			southPanel.add(buttonSave);

			southPanel.add(new JPanel());

			buttonReset = new JButton("Réinitialiser");
			buttonReset.setPreferredSize(buttonMinSize);
			buttonReset.addActionListener((final ActionEvent e) -> reset());
			southPanel.add(buttonReset);

			southPanel.add(new JPanel());

			innerPanel.add(southPanel, BorderLayout.SOUTH);
		}

		setLayout(new GridBagLayout());
		final GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.gridheight = 1;
		c.gridwidth = 1;
		c.weightx = 1.0;
		c.weighty = 1.0;
		c.anchor = GridBagConstraints.NORTH;
		c.fill = GridBagConstraints.NONE;
		add(innerPanel, c);

		final List<Component> focusOrder = new ArrayList<Component>();
		for (int index = 0; index < NUMBER_OF_PLAYERS; index++) {
			focusOrder.add(comboBoxPlayers.get(index));
			focusOrder.add(((JSpinner.NumberEditor) spinnerPlayerGameScores.get(index).getEditor()).getTextField());
		}
		focusOrder.add(buttonCalculate);
		focusOrder.add(buttonSave);
		focusOrder.add(buttonReset);
		setFocusCycleRoot(true);
		setFocusTraversalPolicy(new AddScoreFocusTransversalPolicy(focusOrder));

		calendar = Calendar.getInstance();
		dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.FRENCH);
		labelDate.setText(dateFormat.format(calendar.getTime()));

		players = new ArrayList<Player>();
		normalizedPlayerNames = new ArrayList<String>();
		playerScoreList = new ArrayList<PlayerScore>();
	}

	@Override
	public String getTabName() {
		return "MCR Nouvelle partie";
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
		try {
			comboBoxTournament.removeAllItems();
			listTournament.clear();
			listTournament.addAll(dataAccess.getMCRTournaments());
			Collections.sort(listTournament, new ComparatorDescendingTournamentID());
			for (int index = 0; index < listTournament.size(); index++) {
				comboBoxTournament.addItem(listTournament.get(index).getName());
			}

			players.clear();
			normalizedPlayerNames.clear();
			for (int comboBoxIndex = 0; comboBoxIndex < comboBoxPlayers.size(); comboBoxIndex++) {
				comboBoxPlayers.get(comboBoxIndex).removeAllItems();
			}
			players.addAll(dataAccess.getRegisteredPlayers());
			if (displayFullName) {
				Collections.sort(players, new ComparatorAscendingPlayerName());
			} else {
				Collections.sort(players, new ComparatorAscendingPlayerDisplayName());
			}
			for (int playerIndex = 0; playerIndex < players.size(); playerIndex++) {
				final Player player = players.get(playerIndex);
				String displayString = null;
				if (displayFullName) {
					displayString = player.getPlayerName() + " - " + Integer.toString(player.getPlayerID());
				} else {
					displayString = player.getDisplayName() + " - " + Integer.toString(player.getPlayerID());
				}
				final String normalizedDisplayString = Normalizer.normalize(displayString, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "").toLowerCase();
				normalizedPlayerNames.add(playerIndex, normalizedDisplayString);

				for (int comboBoxIndex = 0; comboBoxIndex < comboBoxPlayers.size(); comboBoxIndex++) {
					comboBoxPlayers.get(comboBoxIndex).addItem(displayString);
				}
			}
			reset();
		} catch (final Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "Erreur de base de donnée", "Erreur", JOptionPane.ERROR_MESSAGE);
		}
	}

	private class LabelDateMouseListener extends MouseAdapter {
		@Override
		public void mouseClicked(final MouseEvent e) {
			getDate();
		}
	}

	private void getDate() {
		final JDatePicker picker = new JDatePicker(SwingUtilities.getWindowAncestor(this), Calendar.MONDAY, calendar.getTime());
		final Rectangle bounds = labelDate.getGraphicsConfiguration().getBounds();
		final Point labelDateScreenLocation = labelDate.getLocationOnScreen();
		final int x = Math.min(labelDateScreenLocation.x, bounds.x + bounds.width - picker.getWidth());
		final int y = Math.min(labelDateScreenLocation.y, bounds.y + bounds.height - picker.getHeight());
		picker.setLocation(x, y);
		picker.setVisible(true);
		if (picker.dateSelected()) {
			final Date selectedDate = picker.getSelectedDate();
			calendar.setTime(selectedDate);
			labelDate.setText(dateFormat.format(selectedDate));
		}
	}

	private class WebPageKeySelectionManager implements KeySelectionManager {
		private static final long DEFAULT_DELAY = 500L;
		private final long delay;
		private long lastTime = 0L;
		private String typedString = "";

		public WebPageKeySelectionManager() {
			this(DEFAULT_DELAY);
		}

		public WebPageKeySelectionManager(final long delay) {
			if (delay < 0) {
				throw new IllegalArgumentException("Delay should be positive");
			}
			this.delay = delay;
		}

		@SuppressWarnings("rawtypes")
		@Override
		public int selectionForKey(final char aKey, final ComboBoxModel aModel) {
			final JComboBox<String> comboBoxPlayer = comboBoxPlayers.get(comboBoxModels.indexOf(aModel));

			final int size = aModel.getSize();
			final long now = System.currentTimeMillis();
			int selectedIndex = comboBoxPlayer.getSelectedIndex();

			final String typeKey = ("" + aKey).toLowerCase();
			if (now - lastTime < delay) {
				typedString += typeKey;
			} else {
				typedString = typeKey;
				selectedIndex++;
			}
			lastTime = now;

			for (int index = selectedIndex; index < size; index++) {
				final String s = normalizedPlayerNames.get(index);
				if (s.contains(typedString)) {
					return index;
				}
			}
			for (int index = 0; index < selectedIndex; index++) {
				final String s = normalizedPlayerNames.get(index);
				if (s.contains(typedString)) {
					return index;
				}
			}
			return -1;
		}
	}

	private class PlayerScore {
		public int index;
		public int place;
		public int gameScore;
		public int finalScore;
	}

	private final List<PlayerScore> playerScoreList;

	private void calculateFinalScore() {
		disableSaveButton();

		playerScoreList.clear();
		int totalScore = 0;
		for (int playerIndex = 0; playerIndex < NUMBER_OF_PLAYERS; playerIndex++) {
			final PlayerScore playerScore = new PlayerScore();
			playerScoreList.add(playerIndex, playerScore);
			playerScore.index = comboBoxPlayers.get(playerIndex).getSelectedIndex();

			// Check player id is specified.
			if (playerScore.index == -1) {
				JOptionPane.showMessageDialog(this, "Veuillez spécifier tous les joueurs", "Erreur", JOptionPane.WARNING_MESSAGE);
				return;
			}

			// Check player id is not present more than once
			for (int innerIndex = 0; innerIndex < playerIndex; innerIndex++) {
				if (playerScoreList.get(innerIndex).index == playerScore.index) {
					JOptionPane.showMessageDialog(this, "Un joueur ne peut être présent qu'une seule fois", "Erreur", JOptionPane.WARNING_MESSAGE);
					return;
				}
			}

			// Check game score
			playerScore.gameScore = (int) spinnerPlayerGameScores.get(playerIndex).getValue();
			totalScore += playerScore.gameScore;
		}

		// Check total score
		if (totalScore != 0) {
			JOptionPane.showMessageDialog(this, "Le total des scores n'est pas correct", "Erreur", JOptionPane.WARNING_MESSAGE);
			labelScoreError.setText(Integer.toString(totalScore));
			return;
		} else {
			labelScoreError.setText(STRING_SPACE);
		}

		// Sort score list
		Collections.sort(playerScoreList, (final PlayerScore o1, final PlayerScore o2) -> {
			return -Integer.compare(o1.gameScore, o2.gameScore);
		});
		int playerIndex = 0;
		while (playerIndex < NUMBER_OF_PLAYERS) {
			int equalityPlayerIndex = playerIndex;
			int totalFinalScore = 0;
			while (equalityPlayerIndex < NUMBER_OF_PLAYERS && playerScoreList.get(playerIndex).gameScore == playerScoreList.get(equalityPlayerIndex).gameScore) {
				totalFinalScore += FINAL_SCORES[equalityPlayerIndex];
				equalityPlayerIndex++;
			}

			totalFinalScore /= equalityPlayerIndex - playerIndex;
			for (int index = playerIndex; index < equalityPlayerIndex; index++) {
				final PlayerScore playerScore = playerScoreList.get(index);
				playerScore.finalScore = totalFinalScore;
				playerScore.place = playerIndex + 1;
			}
			playerIndex = equalityPlayerIndex;
		}

		// Update display
		for (int index = 0; index < NUMBER_OF_PLAYERS; index++) {
			final PlayerScore playerScore = playerScoreList.get(index);
			labelPlayerRankings.get(index).setText(Integer.toString(playerScore.place));
			comboBoxPlayers.get(index).setSelectedIndex(playerScore.index);
			spinnerPlayerGameScores.get(index).setValue(playerScore.gameScore);
			labelPlayerFinalScores.get(index).setText(Integer.toString(playerScore.finalScore));
		}
		buttonSave.setEnabled(true);
	}

	public void disableSaveButton() {
		buttonSave.setEnabled(false);
	}

	private void saveScore() {
		if (listTournament.size() > 0) {
			try {
				final Tournament tournament = listTournament.get(comboBoxTournament.getSelectedIndex());
				final int nbPlayers = playerScoreList.size();
				final List<MCRScore> scores = new ArrayList<MCRScore>(nbPlayers);
				for (int playerIndex = 0; playerIndex < nbPlayers; playerIndex++) {
					final PlayerScore p = playerScoreList.get(playerIndex);
					final Player player = players.get(p.index);
					final MCRScore score = new MCRScore(player.getPlayerID(), "", "", p.place, p.gameScore, p.finalScore);
					scores.add(score);
				}

				final MCRGame newGame = new MCRGame(0, tournament.getId(), calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), scores);
				final UpdateResult result = dataAccess.addMCRGame(newGame);
				if (result.getResult()) {
					JOptionPane.showMessageDialog(this, result.getMessage(), "Succès", JOptionPane.INFORMATION_MESSAGE);
					reset();
				} else {
					JOptionPane.showMessageDialog(this, result.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
				}
			} catch (final Exception e) {
				JOptionPane.showMessageDialog(this, "Erreur de base de données", "Erreur", JOptionPane.ERROR_MESSAGE);
			}
		} else {
			JOptionPane.showMessageDialog(this, "Veuillez créer d'abord un tournoi.", "Erreur", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void reset() {
		for (int playerIndex = 0; playerIndex < NUMBER_OF_PLAYERS; playerIndex++) {
			labelPlayerRankings.get(playerIndex).setText("?");
			comboBoxPlayers.get(playerIndex).setSelectedIndex(-1);
			spinnerPlayerGameScores.get(playerIndex).setValue(0);
			labelPlayerFinalScores.get(playerIndex).setText(STRING_EMPTY);
		}
		labelScoreError.setText(STRING_SPACE);
		disableSaveButton();
	}

	private class AddScoreFocusTransversalPolicy extends FocusTraversalPolicy {
		private final List<Component> focusOrder;

		public AddScoreFocusTransversalPolicy(final List<Component> order) {
			focusOrder = new ArrayList<Component>();
			for (int index = 0; index < order.size(); index++) {
				focusOrder.add(index, order.get(index));
			}
		}

		@Override
		public Component getComponentAfter(final Container aContainer, final Component aComponent) {
			int index = focusOrder.indexOf(aComponent);
			if (index >= 0) {
				Component nextComponent;
				do {
					index = (index + 1) % focusOrder.size();
					nextComponent = focusOrder.get(index);
				} while (!nextComponent.isEnabled());
				return nextComponent;
			} else {
				return null;
			}
		}

		@Override
		public Component getComponentBefore(final Container aContainer, final Component aComponent) {
			int index = focusOrder.indexOf(aComponent);
			if (index >= 0) {
				Component nextComponent;
				do {
					index = (index + focusOrder.size() - 1) % focusOrder.size();
					nextComponent = focusOrder.get(index);
				} while (!nextComponent.isEnabled());
				return nextComponent;
			} else {
				return null;
			}
		}

		@Override
		public Component getFirstComponent(final Container aContainer) {
			return focusOrder.get(0);
		}

		@Override
		public Component getLastComponent(final Container aContainer) {
			return focusOrder.get(focusOrder.size() - 1);
		}

		@Override
		public Component getDefaultComponent(final Container aContainer) {
			return focusOrder.get(0);
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
