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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;

import fr.bmj.bmjc.data.game.Tournament;
import fr.bmj.bmjc.dataaccess.UpdateResult;
import fr.bmj.bmjc.dataaccess.rcr.DataAccessRCR;
import fr.bmj.bmjc.gui.UITabPanel;
import fr.bmj.bmjc.swing.JDialogWithProgress;
import fr.bmj.bmjc.swing.ProportionalGridLayout;
import fr.bmj.bmjc.swing.ProportionalGridLayoutConstraint;

public class UITabPanelRCRManage extends UITabPanel {
	private static final long serialVersionUID = -8908338011775965764L;

	private final DataAccessRCR dataAccess;

	private final JTextField textNewTournamentName;
	private final JButton buttonAddTournament;

	private final JComboBox<String> comboBoxTournament;
	private final ActionListener comboBoxTournamentActionListener;
	private final JTextField textModifyTournamentName;
	private final JButton buttonModifyTournament;
	private final JButton buttonDeleteTournament;

	private final JComboBox<String> comboBoxGameTournament;
	private final ActionListener comboBoxGameTournamentActionListener;
	private final JComboBox<Integer> comboBoxYear;
	private final JComboBox<String> comboBoxMonth;
	private final ActionListener comboBoxYearMonthActionListener;
	private final JComboBox<Integer> comboBoxDay;
	private final ActionListener comboBoxDayActionListener;
	private final JComboBox<Integer> comboBoxId;
	private final JButton buttonDeleteGame;

	private final List<Tournament> listTournament;

	public UITabPanelRCRManage(final DataAccessRCR dataAccess, final JDialogWithProgress waitingDialog) {
		this.dataAccess = dataAccess;

		final Dimension buttonMinSize = new Dimension(BUTTON_MIN_WIDTH, BUTTON_MIN_HEIGHT);
		final JPanel innerPanel = new JPanel();
		innerPanel.setLayout(new GridBagLayout());
		final GridBagConstraints innerC = new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(16, 8, 16, 8), 0, 0);
		{
			final JPanel tournamentPanel = new JPanel();
			tournamentPanel.setLayout(new GridBagLayout());
			tournamentPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(0, 0, 127)), "Tournoi"));
			final GridBagConstraints tournamentC = new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(8, 0, 8, 0), 0, 0);
			{
				final JPanel addTournamentPanel = new JPanel();
				addTournamentPanel.setLayout(new ProportionalGridLayout(1, 5, 2, 2));
				addTournamentPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Ajouter tournoi"));
				final ProportionalGridLayoutConstraint addTournamentC = new ProportionalGridLayoutConstraint(0, 1, 0, 1);

				addTournamentC.x = 0;
				addTournamentC.gridWidth = 1;
				addTournamentPanel.add(new JLabel("Nom: ", SwingConstants.RIGHT), addTournamentC);
				textNewTournamentName = new JTextField();
				addTournamentC.x = 1;
				addTournamentC.gridWidth = 3;
				addTournamentPanel.add(textNewTournamentName, addTournamentC);

				buttonAddTournament = new JButton("Ajouter");
				buttonAddTournament.setPreferredSize(buttonMinSize);
				addTournamentC.x = 4;
				addTournamentC.gridWidth = 1;
				addTournamentPanel.add(buttonAddTournament, addTournamentC);

				tournamentC.gridy = 0;
				tournamentPanel.add(addTournamentPanel, tournamentC);
			}

			{
				final JPanel modifyTournamentPanel = new JPanel();
				modifyTournamentPanel.setLayout(new ProportionalGridLayout(2, 5, 2, 2));
				modifyTournamentPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Modifier tournoi"));
				final ProportionalGridLayoutConstraint modifyTournamentC = new ProportionalGridLayoutConstraint(0, 1, 0, 1);

				modifyTournamentC.y = 0;
				modifyTournamentC.x = 0;
				modifyTournamentPanel.add(new JLabel("Tournoi: ", SwingConstants.RIGHT), modifyTournamentC);
				comboBoxTournament = new JComboBox<String>();
				modifyTournamentC.x = 1;
				modifyTournamentC.gridWidth = 3;
				modifyTournamentPanel.add(comboBoxTournament, modifyTournamentC);

				modifyTournamentC.x = 4;
				modifyTournamentC.gridWidth = 1;
				buttonDeleteTournament = new JButton("Supprimer");
				buttonDeleteTournament.setPreferredSize(buttonMinSize);
				modifyTournamentPanel.add(buttonDeleteTournament, modifyTournamentC);

				modifyTournamentC.y = 1;
				modifyTournamentC.x = 0;
				modifyTournamentC.gridWidth = 1;
				modifyTournamentPanel.add(new JLabel("Nom: ", SwingConstants.RIGHT), modifyTournamentC);
				textModifyTournamentName = new JTextField();
				modifyTournamentC.x = 1;
				modifyTournamentC.gridWidth = 3;
				modifyTournamentPanel.add(textModifyTournamentName, modifyTournamentC);

				modifyTournamentC.x = 4;
				modifyTournamentC.gridWidth = 1;
				buttonModifyTournament = new JButton("Modifier");
				buttonModifyTournament.setPreferredSize(buttonMinSize);
				modifyTournamentPanel.add(buttonModifyTournament, modifyTournamentC);

				tournamentC.gridy = 1;
				tournamentPanel.add(modifyTournamentPanel, tournamentC);
			}

			innerC.gridy = 0;
			innerPanel.add(tournamentPanel, innerC);
		}

		{
			final JPanel gamePanel = new JPanel();
			gamePanel.setLayout(new ProportionalGridLayout(3, 6, 2, 2));
			gamePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(0, 0, 127)), "Supprimer score"));
			final ProportionalGridLayoutConstraint deleteGameC = new ProportionalGridLayoutConstraint(0, 1, 0, 1);

			deleteGameC.y = 0;
			deleteGameC.x = 0;
			gamePanel.add(new JLabel("Tournoi: ", SwingConstants.RIGHT), deleteGameC);
			comboBoxGameTournament = new JComboBox<String>();
			deleteGameC.x = 1;
			deleteGameC.gridWidth = 5;
			gamePanel.add(comboBoxGameTournament, deleteGameC);

			deleteGameC.y = 1;
			deleteGameC.x = 0;
			deleteGameC.gridWidth = 1;
			gamePanel.add(new JLabel("Année: ", SwingConstants.RIGHT), deleteGameC);
			comboBoxYear = new JComboBox<Integer>();
			deleteGameC.x = 1;
			gamePanel.add(comboBoxYear, deleteGameC);

			deleteGameC.x = 2;
			gamePanel.add(new JLabel("Mois: ", SwingConstants.RIGHT), deleteGameC);
			comboBoxMonth = new JComboBox<String>();
			deleteGameC.x = 3;
			gamePanel.add(comboBoxMonth, deleteGameC);

			deleteGameC.x = 4;
			gamePanel.add(new JLabel("Jour: ", SwingConstants.RIGHT), deleteGameC);
			comboBoxDay = new JComboBox<Integer>();
			deleteGameC.x = 5;
			gamePanel.add(comboBoxDay, deleteGameC);

			deleteGameC.y = 2;
			deleteGameC.x = 0;
			gamePanel.add(new JLabel("ID: ", SwingConstants.RIGHT), deleteGameC);
			comboBoxId = new JComboBox<Integer>();
			deleteGameC.x = 1;
			deleteGameC.gridWidth = 3;
			gamePanel.add(comboBoxId, deleteGameC);

			buttonDeleteGame = new JButton("Supprimer");
			buttonDeleteGame.setPreferredSize(buttonMinSize);
			deleteGameC.x = 5;
			deleteGameC.gridWidth = 1;
			gamePanel.add(buttonDeleteGame, deleteGameC);

			innerC.gridy = 1;
			innerPanel.add(gamePanel, innerC);
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

		listTournament = new ArrayList<Tournament>();

		comboBoxTournamentActionListener = (final ActionEvent e) -> displayTournament();
		comboBoxTournament.addActionListener(comboBoxTournamentActionListener);
		buttonAddTournament.addActionListener((final ActionEvent e) -> addTournament());
		buttonModifyTournament.addActionListener((final ActionEvent e) -> modifyTournament());
		buttonDeleteTournament.addActionListener((final ActionEvent e) -> deleteTournament());

		comboBoxGameTournamentActionListener = (final ActionEvent e) -> getYear();
		comboBoxGameTournament.addActionListener(comboBoxGameTournamentActionListener);
		comboBoxYearMonthActionListener = (final ActionEvent e) -> getDay();
		comboBoxYear.addActionListener(comboBoxYearMonthActionListener);
		final String monthStrings[] = DateFormatSymbols.getInstance(Locale.FRANCE).getMonths();
		for (int monthIndex = 0; monthIndex < 12; monthIndex++) {
			comboBoxMonth.addItem(monthStrings[monthIndex]);
		}
		comboBoxMonth.addActionListener(comboBoxYearMonthActionListener);
		comboBoxDayActionListener = (final ActionEvent e) -> getId();
		comboBoxDay.addActionListener(comboBoxDayActionListener);
		buttonDeleteGame.addActionListener((final ActionEvent e) -> deleteGame());
	}

	@Override
	public String getTabName() {
		return "RCR Gestion";
	}

	@Override
	public void setDisplayFullName(final boolean displayFullName, final boolean toRefresh) {
	}

	@Override
	public void refresh() {
		refreshTournament();
	}

	private void refreshTournament() {
		comboBoxTournament.removeActionListener(comboBoxTournamentActionListener);
		comboBoxGameTournament.removeActionListener(comboBoxGameTournamentActionListener);
		comboBoxTournament.removeAllItems();
		comboBoxGameTournament.removeAllItems();

		listTournament.clear();
		listTournament.addAll(dataAccess.getRCRTournaments());
		for (int index = 0; index < listTournament.size(); index++) {
			final String name = listTournament.get(index).getName();
			comboBoxTournament.addItem(name);
			comboBoxGameTournament.addItem(name);
		}

		comboBoxTournament.addActionListener(comboBoxTournamentActionListener);
		comboBoxGameTournament.addActionListener(comboBoxGameTournamentActionListener);
		if (listTournament.size() > 0) {
			comboBoxTournament.setSelectedIndex(0);
			comboBoxGameTournament.setSelectedIndex(0);
		} else {
			comboBoxTournament.setSelectedIndex(-1);
			comboBoxGameTournament.setSelectedIndex(-1);
		}
	}

	private void addTournament() {
		final String tournamentName = textNewTournamentName.getText();
		if (tournamentName != null && tournamentName.length() > 0) {
			final UpdateResult result = dataAccess.addRCRTournament(tournamentName);
			if (result.getResult()) {
				JOptionPane.showMessageDialog(this, "Le tournoi a été ajouté", "Succès", JOptionPane.INFORMATION_MESSAGE);
				textNewTournamentName.setText("");
				refreshTournament();
			} else {
				JOptionPane.showMessageDialog(this, "Le nom est déjà utilisé", "Erreur", JOptionPane.ERROR_MESSAGE);
			}
		} else {
			JOptionPane.showMessageDialog(this, "Le nom ne peut pas être vide", "Erreur", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void displayTournament() {
		final int selectedTournamentIndex = comboBoxTournament.getSelectedIndex();
		if (selectedTournamentIndex != -1) {
			final Tournament tournament = listTournament.get(selectedTournamentIndex);
			textModifyTournamentName.setText(tournament.getName());
		} else {
			textModifyTournamentName.setText("");
		}
	}

	private void modifyTournament() {
		final int selectedTournamentIndex = comboBoxTournament.getSelectedIndex();
		if (selectedTournamentIndex != -1) {
			final Tournament tournament = listTournament.get(selectedTournamentIndex);
			final String tournamentName = textModifyTournamentName.getText();
			if (tournamentName != null && tournamentName.length() > 0) {
				final UpdateResult result = dataAccess.modifyRCRTournament(tournament.getId(), tournamentName);
				if (result.getResult()) {
					JOptionPane.showMessageDialog(this, "Le tournoi a été modifié", "Succès", JOptionPane.INFORMATION_MESSAGE);
					textModifyTournamentName.setText("");
					refreshTournament();
				} else {
					JOptionPane.showMessageDialog(this, "Le nom est déjà utilisé", "Erreur", JOptionPane.ERROR_MESSAGE);
				}
			} else {
				JOptionPane.showMessageDialog(this, "Le nom ne peut pas être vide", "Erreur", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private void deleteTournament() {
		final int selectedTournamentIndex = comboBoxTournament.getSelectedIndex();
		if (selectedTournamentIndex != -1) {
			final Tournament tournament = listTournament.get(selectedTournamentIndex);
			final UpdateResult result = dataAccess.deleteRCRTournament(tournament.getId());
			if (result.getResult()) {
				JOptionPane.showMessageDialog(this, "Le tournoi a été supprimé", "Succès", JOptionPane.INFORMATION_MESSAGE);
				refreshTournament();
			} else {
				JOptionPane.showMessageDialog(this, "Le tournoi n'a pas été supprimé", "Erreur", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private void getYear() {
		final int selectedTournamentIndex = comboBoxGameTournament.getSelectedIndex();
		if (selectedTournamentIndex != -1) {
			final Tournament tournament = listTournament.get(selectedTournamentIndex);
			comboBoxYear.removeActionListener(comboBoxYearMonthActionListener);
			comboBoxYear.removeAllItems();

			final List<Integer> years = new ArrayList<Integer>();
			years.addAll(dataAccess.getRCRYears(tournament));
			for (int yearIndex = 0; yearIndex < years.size(); yearIndex++) {
				comboBoxYear.addItem(years.get(yearIndex));
			}

			comboBoxYear.addActionListener(comboBoxYearMonthActionListener);
			if (years.size() > 0) {
				comboBoxYear.setSelectedIndex(0);
			} else {
				comboBoxYear.setSelectedIndex(-1);
			}
		}
	}

	private void getDay() {
		final int selectedTournamentIndex = comboBoxGameTournament.getSelectedIndex();
		final Integer selectedYear = (Integer) comboBoxYear.getSelectedItem();
		final int selectedMonth = comboBoxMonth.getSelectedIndex();

		comboBoxDay.removeActionListener(comboBoxDayActionListener);
		comboBoxDay.removeAllItems();
		final List<Integer> days = new ArrayList<Integer>();
		if (selectedTournamentIndex != -1 && selectedYear != null) {
			final Tournament tournament = listTournament.get(selectedTournamentIndex);

			days.addAll(dataAccess.getRCRGameDays(tournament, selectedYear, selectedMonth));
			for (int dayIndex = 0; dayIndex < days.size(); dayIndex++) {
				comboBoxDay.addItem(days.get(dayIndex));
			}
		}

		comboBoxDay.addActionListener(comboBoxDayActionListener);
		if (days.size() > 0) {
			comboBoxDay.setSelectedIndex(0);
		} else {
			comboBoxDay.setSelectedIndex(-1);
		}
	}

	private void getId() {
		final int selectedTournamentIndex = comboBoxGameTournament.getSelectedIndex();
		final Integer selectedYear = (Integer) comboBoxYear.getSelectedItem();
		final int selectedMonth = comboBoxMonth.getSelectedIndex();
		final Integer selectedDay = (Integer) comboBoxDay.getSelectedItem();

		comboBoxId.removeAllItems();
		if (selectedTournamentIndex != -1 && selectedYear != null && selectedDay != null) {
			final Tournament tournament = listTournament.get(selectedTournamentIndex);

			final List<Integer> ids = new ArrayList<Integer>();
			ids.addAll(dataAccess.getRCRGameIds(tournament, selectedYear, selectedMonth, selectedDay));
			for (int idIndex = 0; idIndex < ids.size(); idIndex++) {
				comboBoxId.addItem(ids.get(idIndex));
			}

			if (ids.size() > 0) {
				comboBoxId.setSelectedIndex(0);
			}
		}
	}

	private void deleteGame() {
		final Integer selectedId = (Integer) comboBoxId.getSelectedItem();
		if (selectedId != null) {
			final UpdateResult result = dataAccess.deleteRCRGame(selectedId);
			if (result.getResult()) {
				JOptionPane.showMessageDialog(this, "La partie a été supprimée", "Succès", JOptionPane.INFORMATION_MESSAGE);
				refreshTournament();
			} else {
				JOptionPane.showMessageDialog(this, "La partie n'a pas été supprimée", "Erreur", JOptionPane.ERROR_MESSAGE);
			}
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
