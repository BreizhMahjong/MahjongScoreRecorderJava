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
package fr.bmj.bmjc.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;

import fr.bmj.bmjc.dataaccess.abs.DataAccess;
import fr.bmj.bmjc.gui.player.UITabPanelManagePlayer;
import fr.bmj.bmjc.gui.rcr.UITabPanelRCRGameHistory;
import fr.bmj.bmjc.gui.rcr.UITabPanelRCRManage;
import fr.bmj.bmjc.gui.rcr.UITabPanelRCRNewGame;
import fr.bmj.bmjc.gui.rcr.UITabPanelRCRPersonalAnalyse;
import fr.bmj.bmjc.gui.rcr.UITabPanelRCRRanking;
import fr.bmj.bmjc.gui.rcr.UITabPanelRCRScoreAnalyze;
import fr.bmj.bmjc.gui.rcr.UITabPanelRCRTrend;

public class UIMainWindow extends JFrame implements WindowListener {
	private static final long serialVersionUID = 4639754313889847228L;

	private static final int NB_PANELS = 8;
	private static final int INDEX_PANEL_MANAGE_PLAYER = 0;
	private static final int INDEX_PANEL_RCR_MANAGE_GAME = 1;
	private static final int INDEX_PANEL_RCR_NEW_GAME = 2;
	private static final int INDEX_PANEL_RCR_RANKING = 3;
	private static final int INDEX_PANEL_RCR_TREND = 4;
	private static final int INDEX_PANEL_RCR_PERSONAL_ANALYZE = 5;
	private static final int INDEX_PANEL_RCR_SCORE_ANALYZE = 6;
	private static final int INDEX_PANEL_RCR_GAME_HISTORY = 7;

	private static final int WINDOW_HEIGHT = 800;
	private static final int WINDOW_WIDTH = 1280;
	private static final String MAIN_LOGO_URL = "fr/bmj/bmjc/image/logo.png";

	private static final boolean DISPLAY_REAL_NAME = false;
	private static final boolean USE_MIN_GAME = false;
	private static final boolean ONLY_REGULAR_PLAYERS = false;
	private static final boolean ONLY_FREQUENT_PLAYERS = true;

	private final DataAccess dataAccess;

	private final JMenuItem menuItemExport;
	private final JRadioButtonMenuItem menuSettingsFullName;
	private final JRadioButtonMenuItem menuSettingsDisplayName;
	private final JCheckBoxMenuItem menuSettingsUseMinGame;
	private final JCheckBoxMenuItem menuSettingsOnlyRegularPlayers;
	private final JCheckBoxMenuItem menuSettingsOnlyFrequentPlayers;

	private final UITabPanel tabPanels[];
	private final JTabbedPane tabbedPane;

	public UIMainWindow(final DataAccess dataAccess) {
		super(
			"Breizh Mahjong Recorder");

		this.dataAccess = dataAccess;
		dataAccess.initialize();
		dataAccess.getRCR().setUseMinimumGame(
			USE_MIN_GAME);
		dataAccess.getRCR().setOnlyRegularPlayers(
			ONLY_REGULAR_PLAYERS);
		dataAccess.getManagePlayer().setOnlyFrequentPlayers(
			ONLY_FREQUENT_PLAYERS);

		try {
			final URL icon = ClassLoader.getSystemResource(
				MAIN_LOGO_URL);
			setIconImage(
				ImageIO.read(
					icon));
		} catch (final Exception e) {
		}

		final Container mainPane = getContentPane();
		mainPane.setLayout(
			new BorderLayout());
		tabbedPane = new JTabbedPane(
			SwingConstants.TOP,
			JTabbedPane.SCROLL_TAB_LAYOUT);
		mainPane.add(
			tabbedPane,
			BorderLayout.CENTER);

		tabPanels = new UITabPanel[NB_PANELS];
		tabPanels[INDEX_PANEL_MANAGE_PLAYER] = new UITabPanelManagePlayer(
			dataAccess.getManagePlayer());
		tabPanels[INDEX_PANEL_RCR_MANAGE_GAME] = new UITabPanelRCRManage(
			dataAccess.getRCR());
		tabPanels[INDEX_PANEL_RCR_NEW_GAME] = new UITabPanelRCRNewGame(
			dataAccess.getRCR(),
			dataAccess.getManagePlayer());
		tabPanels[INDEX_PANEL_RCR_RANKING] = new UITabPanelRCRRanking(
			dataAccess.getRCR());
		tabPanels[INDEX_PANEL_RCR_TREND] = new UITabPanelRCRTrend(
			dataAccess.getRCR());
		tabPanels[INDEX_PANEL_RCR_PERSONAL_ANALYZE] = new UITabPanelRCRPersonalAnalyse(
			dataAccess.getRCR());
		tabPanels[INDEX_PANEL_RCR_SCORE_ANALYZE] = new UITabPanelRCRScoreAnalyze(
			dataAccess.getRCR());
		tabPanels[INDEX_PANEL_RCR_GAME_HISTORY] = new UITabPanelRCRGameHistory(
			dataAccess.getRCR());
		for (int indexTabPanel = 0; indexTabPanel < tabPanels.length; indexTabPanel++) {
			tabbedPane.addTab(
				tabPanels[indexTabPanel].getTabName(),
				tabPanels[indexTabPanel]);
		}
		tabbedPane.addChangeListener(
			(final ChangeEvent e) -> changeTab());

		final JMenuBar menuBar = new JMenuBar();
		setJMenuBar(
			menuBar);

		final JMenu menuFile = new JMenu(
			"Fichier");
		menuFile.setMnemonic(
			KeyEvent.VK_F);
		menuBar.add(
			menuFile);

		menuItemExport = new JMenuItem(
			"Exporter");
		menuItemExport.setMnemonic(
			KeyEvent.VK_E);
		menuItemExport.addActionListener(
			(final ActionEvent e) -> export());
		menuFile.add(
			menuItemExport);
		menuFile.addSeparator();

		final JMenuItem menuItemFileExit = new JMenuItem(
			"Quitter");
		menuItemFileExit.setMnemonic(
			KeyEvent.VK_X);
		menuItemFileExit.addActionListener(
			(final ActionEvent e) -> exit());
		menuFile.add(
			menuItemFileExit);

		final JMenu menuSettings = new JMenu(
			"Paramètres");
		menuSettings.setMnemonic(
			KeyEvent.VK_S);
		menuBar.add(
			menuSettings);

		final ButtonGroup nameModeMenuButtonGroup = new ButtonGroup();
		final ActionListener nameModeActionListener = (final ActionEvent e) -> setDisplayFullName();
		menuSettingsFullName = new JRadioButtonMenuItem(
			"Nom prénom");
		menuSettingsFullName.setMnemonic(
			KeyEvent.VK_F);
		menuSettingsFullName.setSelected(
			DISPLAY_REAL_NAME);
		menuSettingsFullName.addActionListener(
			nameModeActionListener);
		menuSettings.add(
			menuSettingsFullName);
		nameModeMenuButtonGroup.add(
			menuSettingsFullName);

		menuSettingsDisplayName = new JRadioButtonMenuItem(
			"Pseudo");
		menuSettingsDisplayName.setMnemonic(
			KeyEvent.VK_D);
		menuSettingsDisplayName.setSelected(
			!DISPLAY_REAL_NAME);
		menuSettingsDisplayName.addActionListener(
			nameModeActionListener);
		menuSettings.add(
			menuSettingsDisplayName);
		nameModeMenuButtonGroup.add(
			menuSettingsDisplayName);

		menuSettings.addSeparator();

		menuSettingsUseMinGame = new JCheckBoxMenuItem(
			"Min nb de parties");
		menuSettingsUseMinGame.setMnemonic(
			KeyEvent.VK_M);
		menuSettingsUseMinGame.setSelected(
			USE_MIN_GAME);
		menuSettingsUseMinGame.addActionListener(
			(final ActionEvent e) -> setUseMinGame());
		menuSettings.add(
			menuSettingsUseMinGame);

		menuSettingsOnlyRegularPlayers = new JCheckBoxMenuItem(
			"Joueurs réguliers");
		menuSettingsOnlyRegularPlayers.setMnemonic(
			KeyEvent.VK_R);
		menuSettingsOnlyRegularPlayers.setSelected(
			ONLY_REGULAR_PLAYERS);
		menuSettingsOnlyRegularPlayers.addActionListener(
			(final ActionEvent e) -> setOnlyRegularPlayers());
		menuSettings.add(
			menuSettingsOnlyRegularPlayers);

		menuSettingsOnlyFrequentPlayers = new JCheckBoxMenuItem(
			"Joueurs fréquents");
		menuSettingsOnlyFrequentPlayers.setMnemonic(
			KeyEvent.VK_F);
		menuSettingsOnlyFrequentPlayers.setSelected(
			ONLY_FREQUENT_PLAYERS);
		menuSettingsOnlyFrequentPlayers.addActionListener(
			(final ActionEvent e) -> setOnlyFrequentPlayers());
		menuSettings.add(
			menuSettingsOnlyFrequentPlayers);

		addWindowListener(
			this);
		setMinimumSize(
			new Dimension(
				WINDOW_WIDTH,
				WINDOW_HEIGHT));
		setPreferredSize(
			new Dimension(
				WINDOW_WIDTH,
				WINDOW_HEIGHT));
		setDefaultCloseOperation(
			WindowConstants.DO_NOTHING_ON_CLOSE);
		pack();
		setVisible(
			true);

		changeTab();
		setUseMinGame();
		setDisplayFullName();
	}

	private UITabPanel getCurrentTab() {
		final int selectedTabIndex = tabbedPane.getSelectedIndex();
		if (selectedTabIndex >= 0) {
			return tabPanels[selectedTabIndex];
		} else {
			return null;
		}
	}

	private void changeTab() {
		final UITabPanel tab = getCurrentTab();
		if (tab != null) {
			menuItemExport.setEnabled(
				tab.canExport());
			tab.refresh();
		}
	}

	private void export() {
		final UITabPanel tab = getCurrentTab();
		if (tab.canExport()) {
			tab.export();
		}
	}

	private void exit() {
		dataAccess.disconnect();
		dispose();
	}

	private void setDisplayFullName() {
		final boolean displayFullName = menuSettingsFullName.isSelected();
		final UITabPanel tab = getCurrentTab();
		for (int index = 0; index < tabPanels.length; index++) {
			tabPanels[index].setDisplayFullName(
				displayFullName,
				tabPanels[index] == tab);
		}
	}

	private void setUseMinGame() {
		dataAccess.getRCR().setUseMinimumGame(
			menuSettingsUseMinGame.isSelected());
		final UITabPanel tab = getCurrentTab();
		if (tab == tabPanels[INDEX_PANEL_RCR_RANKING]) {
			tab.refresh();
		}
	}

	private void setOnlyRegularPlayers() {
		dataAccess.getRCR().setOnlyRegularPlayers(
			menuSettingsOnlyRegularPlayers.isSelected());
		final UITabPanel tab = getCurrentTab();
		if (tab == tabPanels[INDEX_PANEL_RCR_RANKING] || tab == tabPanels[INDEX_PANEL_RCR_PERSONAL_ANALYZE] || tab == tabPanels[INDEX_PANEL_RCR_TREND]) {
			tab.refresh();
		}
	}

	private void setOnlyFrequentPlayers() {
		dataAccess.getManagePlayer().setOnlyFrequentPlayers(
			menuSettingsOnlyFrequentPlayers.isSelected());
		final UITabPanel tab = getCurrentTab();
		if (tab == tabPanels[INDEX_PANEL_RCR_NEW_GAME]) {
			tab.refresh();
		}
	}

	@Override
	public void windowOpened(final WindowEvent e) {
	}

	@Override
	public void windowClosing(final WindowEvent e) {
		exit();
	}

	@Override
	public void windowClosed(final WindowEvent e) {
	}

	@Override
	public void windowIconified(final WindowEvent e) {
	}

	@Override
	public void windowDeiconified(final WindowEvent e) {
	}

	@Override
	public void windowActivated(final WindowEvent e) {
	}

	@Override
	public void windowDeactivated(final WindowEvent e) {
	}

}
