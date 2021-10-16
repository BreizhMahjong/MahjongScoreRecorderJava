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
package fr.bmj.bmjc.gui.player;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;

import fr.bmj.bmjc.data.game.ComparatorAscendingPlayerDisplayName;
import fr.bmj.bmjc.data.game.Player;
import fr.bmj.bmjc.dataaccess.abs.UpdateResult;
import fr.bmj.bmjc.dataaccess.abs.player.DataAccessManagePlayer;
import fr.bmj.bmjc.gui.UITabPanel;
import fr.bri.awt.ProportionalGridLayout;
import fr.bri.awt.ProportionalGridLayoutConstraint;

public class UITabPanelManagePlayer extends UITabPanel {
	private static final long serialVersionUID = 7626428108727903118L;

	private final DataAccessManagePlayer dataAccess;

	private final JTextField textNewPlayerName;
	private final JTextField textNewPlayerDisplayName;
	private final JButton buttonAddPlayer;

	private final JComboBox<String> comboBoxPlayer;
	private final ActionListener comboBoxPlayerActionListener;
	private final JCheckBox checkBoxFrequent;
	private final JCheckBox checkBoxRegular;
	private final JTextField textModifyPlayerName;
	private final JTextField textModifyPlayerDisplayName;
	private final JTextField textModifyLicense;
	private final JButton buttonModifyPlayer;
	private final JButton buttonDeletePlayer;

	private final List<Player> listPlayer;

	public UITabPanelManagePlayer(final DataAccessManagePlayer dataAccess) {
		this.dataAccess = dataAccess;

		final Dimension buttonMinSize = new Dimension(BUTTON_MIN_WIDTH,
			BUTTON_MIN_HEIGHT);
		final JPanel innerPanel = new JPanel();
		innerPanel.setLayout(new GridBagLayout());
		final GridBagConstraints innerC = new GridBagConstraints(0,
			0,
			1,
			1,
			0.0,
			0.0,
			GridBagConstraints.CENTER,
			GridBagConstraints.HORIZONTAL,
			new Insets(16,
				8,
				16,
				8),
			0,
			0);
		{
			final JPanel playerPanel = new JPanel();
			playerPanel.setLayout(new GridBagLayout());
			playerPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(0,
				0,
				127)),
				"Joueur"));
			final GridBagConstraints playerC = new GridBagConstraints(0,
				0,
				1,
				1,
				1.0,
				0.0,
				GridBagConstraints.CENTER,
				GridBagConstraints.HORIZONTAL,
				new Insets(8,
					0,
					8,
					0),
				0,
				0);
			{
				final JPanel addPlayerPanel = new JPanel();
				final ProportionalGridLayout layout = new ProportionalGridLayout(1,
					7,
					2,
					2);
				addPlayerPanel.setLayout(layout);
				addPlayerPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
					"Ajouter joueur"));
				final ProportionalGridLayoutConstraint addPlayerC = new ProportionalGridLayoutConstraint(0,
					1,
					0,
					1);

				addPlayerC.x = 0;
				addPlayerC.gridWidth = 1;
				addPlayerPanel.add(new JLabel("Nom: ",
					SwingConstants.RIGHT),
					addPlayerC);
				textNewPlayerName = new JTextField();
				addPlayerC.x = 1;
				addPlayerC.gridWidth = 2;
				addPlayerPanel.add(textNewPlayerName,
					addPlayerC);

				addPlayerC.x = 3;
				addPlayerC.gridWidth = 1;
				addPlayerPanel.add(new JLabel("Pseudo: ",
					SwingConstants.RIGHT),
					addPlayerC);
				textNewPlayerDisplayName = new JTextField();
				addPlayerC.x = 4;
				addPlayerC.gridWidth = 2;
				addPlayerPanel.add(textNewPlayerDisplayName,
					addPlayerC);

				buttonAddPlayer = new JButton("Ajouter");
				buttonAddPlayer.setPreferredSize(buttonMinSize);
				addPlayerC.x = 6;
				addPlayerC.gridWidth = 1;
				addPlayerPanel.add(buttonAddPlayer,
					addPlayerC);

				playerC.gridy = 0;
				playerPanel.add(addPlayerPanel,
					playerC);
			}

			{
				final JPanel modifyPlayerPanel = new JPanel();
				final ProportionalGridLayout layout = new ProportionalGridLayout(4,
					7,
					2,
					2);
				modifyPlayerPanel.setLayout(layout);
				modifyPlayerPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
					"Modifier joueur"));
				final ProportionalGridLayoutConstraint modifyPlayerC = new ProportionalGridLayoutConstraint(0,
					1,
					0,
					1);

				modifyPlayerC.y = 0;
				modifyPlayerC.x = 0;
				modifyPlayerC.gridWidth = 1;
				modifyPlayerPanel.add(new JLabel("Joueur: ",
					SwingConstants.RIGHT),
					modifyPlayerC);
				comboBoxPlayer = new JComboBox<String>();
				modifyPlayerC.x = 1;
				modifyPlayerC.gridWidth = 5;
				modifyPlayerPanel.add(comboBoxPlayer,
					modifyPlayerC);

				modifyPlayerC.x = 6;
				modifyPlayerC.gridWidth = 1;
				buttonDeletePlayer = new JButton("Supprimer");
				buttonDeletePlayer.setPreferredSize(buttonMinSize);
				modifyPlayerPanel.add(buttonDeletePlayer,
					modifyPlayerC);

				modifyPlayerC.y = 1;
				modifyPlayerC.x = 0;
				modifyPlayerC.gridWidth = 1;
				modifyPlayerPanel.add(new JLabel("Nom: ",
					SwingConstants.RIGHT),
					modifyPlayerC);
				textModifyPlayerName = new JTextField();
				modifyPlayerC.x = 1;
				modifyPlayerC.gridWidth = 2;
				modifyPlayerPanel.add(textModifyPlayerName,
					modifyPlayerC);

				modifyPlayerC.x = 3;
				modifyPlayerC.gridWidth = 1;
				modifyPlayerPanel.add(new JLabel("Pseudo: ",
					SwingConstants.RIGHT),
					modifyPlayerC);
				textModifyPlayerDisplayName = new JTextField();
				modifyPlayerC.x = 4;
				modifyPlayerC.gridWidth = 2;
				modifyPlayerPanel.add(textModifyPlayerDisplayName,
					modifyPlayerC);

				modifyPlayerC.y = 2;
				modifyPlayerC.x = 0;
				modifyPlayerC.gridWidth = 1;
				modifyPlayerPanel.add(new JLabel("Licence: ",
					SwingConstants.RIGHT),
					modifyPlayerC);
				textModifyLicense = new JTextField();
				modifyPlayerC.x = 1;
				modifyPlayerC.gridWidth = 2;
				modifyPlayerPanel.add(textModifyLicense,
					modifyPlayerC);

				modifyPlayerC.y = 3;
				modifyPlayerC.x = 1;
				modifyPlayerC.gridWidth = 2;
				checkBoxFrequent = new JCheckBox("Fréquent");
				modifyPlayerPanel.add(checkBoxFrequent,
					modifyPlayerC);

				modifyPlayerC.x = 4;
				modifyPlayerC.gridWidth = 2;
				checkBoxRegular = new JCheckBox("Régulier");
				modifyPlayerPanel.add(checkBoxRegular,
					modifyPlayerC);

				modifyPlayerC.x = 6;
				modifyPlayerC.gridWidth = 1;
				buttonModifyPlayer = new JButton("Modifier");
				buttonModifyPlayer.setPreferredSize(buttonMinSize);
				modifyPlayerPanel.add(buttonModifyPlayer,
					modifyPlayerC);

				playerC.gridy = 1;
				playerPanel.add(modifyPlayerPanel,
					playerC);
			}
			innerC.gridy = 0;
			innerPanel.add(playerPanel,
				innerC);
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
		add(innerPanel,
			c);

		listPlayer = new ArrayList<Player>();

		comboBoxPlayerActionListener = (final ActionEvent e) -> displayPlayer();
		comboBoxPlayer.addActionListener(comboBoxPlayerActionListener);
		buttonAddPlayer.addActionListener((final ActionEvent e) -> addPlayer());
		buttonModifyPlayer.addActionListener((final ActionEvent e) -> modifyPlayer());
		buttonDeletePlayer.addActionListener((final ActionEvent e) -> deletePlayer());
	}

	@Override
	public String getTabName() {
		return "Gestion de joueur";
	}

	@Override
	public void setDisplayFullName(final boolean displayFullName,
		final boolean toRefresh) {
	}

	@Override
	public void refresh() {
		refreshPlayer();
	}

	private void refreshPlayer() {
		new Thread(() -> {
			try {
				comboBoxPlayer.removeActionListener(comboBoxPlayerActionListener);
				comboBoxPlayer.removeAllItems();

				listPlayer.clear();
				listPlayer.addAll(dataAccess.getAllPlayers());
				Collections.sort(listPlayer,
					new ComparatorAscendingPlayerDisplayName());
				for (int playerIndex = 0; playerIndex < listPlayer.size(); playerIndex++) {
					final Player player = listPlayer.get(playerIndex);
					final String displayString = Integer.toString(player.getPlayerID())
						+ " - "
						+ player.getPlayerName()
						+ " - "
						+ player.getDisplayName();
					comboBoxPlayer.addItem(displayString);
				}

				comboBoxPlayer.addActionListener(comboBoxPlayerActionListener);
				if (listPlayer.size() > 0) {
					comboBoxPlayer.setSelectedIndex(0);
				} else {
					comboBoxPlayer.setSelectedIndex(-1);
				}
			} catch (final Exception e) {
				JOptionPane.showMessageDialog(this,
					e.getMessage(),
					"Erreur",
					JOptionPane.ERROR_MESSAGE);
			}
		}).start();
	}

	private void addPlayer() {
		final String playerName = textNewPlayerName.getText();
		final String playerDisplayName = textNewPlayerDisplayName.getText();
		if (playerName != null && playerName.length() > 0 && playerDisplayName != null && playerDisplayName.length() > 0) {
			final UpdateResult result = dataAccess.addPlayer(playerName,
				playerDisplayName);
			if (result.getResult()) {
				JOptionPane.showMessageDialog(this,
					"Le joueur a été ajouté",
					"Succès",
					JOptionPane.INFORMATION_MESSAGE);
				textNewPlayerName.setText("");
				textNewPlayerDisplayName.setText("");
				refreshPlayer();
			} else {
				JOptionPane.showMessageDialog(this,
					"Le pseudo est déjà utilisé",
					"Erreur",
					JOptionPane.ERROR_MESSAGE);
			}
		} else {
			JOptionPane.showMessageDialog(this,
				"Le nom et le pseudo ne peuvent pas être vides",
				"Erreur",
				JOptionPane.ERROR_MESSAGE);
		}
	}

	private void displayPlayer() {
		final int selectedPlayerIndex = comboBoxPlayer.getSelectedIndex();
		if (selectedPlayerIndex != -1) {
			final Player player = listPlayer.get(selectedPlayerIndex);
			textModifyPlayerName.setText(player.getPlayerName());
			textModifyPlayerDisplayName.setText(player.getDisplayName());
			checkBoxFrequent.setSelected(player.isFrequent());
			checkBoxRegular.setSelected(player.isRegular());
			textModifyLicense.setText(player.getLicense());
		} else {
			textModifyPlayerName.setText("");
			textModifyPlayerDisplayName.setText("");
			checkBoxFrequent.setSelected(false);
			checkBoxRegular.setSelected(false);
			textModifyLicense.setText("");
		}
	}

	private void modifyPlayer() {
		final int selectedPlayerIndex = comboBoxPlayer.getSelectedIndex();
		if (selectedPlayerIndex != -1) {
			final Player player = listPlayer.get(selectedPlayerIndex);
			final String playerName = textModifyPlayerName.getText();
			final String playerDisplayName = textModifyPlayerDisplayName.getText();
			final boolean frequent = checkBoxFrequent.isSelected();
			final boolean regular = checkBoxRegular.isSelected();
			final String license = textModifyLicense.getText();
			if (playerName != null && playerName.length() > 0 && playerDisplayName != null && playerDisplayName.length() > 0) {
				final UpdateResult result = dataAccess.modifyPlayer(player.getPlayerID(),
					playerName,
					playerDisplayName,
					frequent,
					regular,
					license);
				if (result.getResult()) {
					JOptionPane.showMessageDialog(this,
						"Le joueur a été modifié",
						"Succès",
						JOptionPane.INFORMATION_MESSAGE);
					refreshPlayer();
				} else {
					JOptionPane.showMessageDialog(this,
						"Le pseudo est déjà utilisé",
						"Erreur",
						JOptionPane.ERROR_MESSAGE);
				}
			} else {
				JOptionPane.showMessageDialog(this,
					"Le nom et le pseudo ne peuvent pas être vides",
					"Erreur",
					JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private void deletePlayer() {
		final int selectedPlayerIndex = comboBoxPlayer.getSelectedIndex();
		if (selectedPlayerIndex != -1) {
			final Player player = listPlayer.get(selectedPlayerIndex);
			final UpdateResult result = dataAccess.deletePlayer(player.getPlayerID());
			if (result.getResult()) {
				JOptionPane.showMessageDialog(this,
					"Le joueur a été supprimé",
					"Succès",
					JOptionPane.INFORMATION_MESSAGE);
				refreshPlayer();
			} else {
				JOptionPane.showMessageDialog(this,
					"Le joueur n'a pas été supprimé",
					"Erreur",
					JOptionPane.ERROR_MESSAGE);
			}
		}
	}
}
