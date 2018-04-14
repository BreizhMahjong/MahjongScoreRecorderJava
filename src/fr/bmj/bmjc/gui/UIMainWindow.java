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
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import fr.bmj.bmjc.dataaccess.DataAccess;
import fr.bmj.bmjc.gui.mcr.UITabPanelMCRClubRanking;
import fr.bmj.bmjc.gui.mcr.UITabPanelMCRGameHistory;
import fr.bmj.bmjc.gui.mcr.UITabPanelMCRManage;
import fr.bmj.bmjc.gui.mcr.UITabPanelMCRNewGame;
import fr.bmj.bmjc.gui.mcr.UITabPanelMCRPersonalAnalyse;
import fr.bmj.bmjc.gui.mcr.UITabPanelMCRTrend;
import fr.bmj.bmjc.gui.rcr.UITabPanelRCRClubRanking;
import fr.bmj.bmjc.gui.rcr.UITabPanelRCRGameHistory;
import fr.bmj.bmjc.gui.rcr.UITabPanelRCRManage;
import fr.bmj.bmjc.gui.rcr.UITabPanelRCRNewGame;
import fr.bmj.bmjc.gui.rcr.UITabPanelRCRPersonalAnalyse;
import fr.bmj.bmjc.gui.rcr.UITabPanelRCRTrend;
import fr.bmj.bmjc.swing.JDialogWithProgress;

public class UIMainWindow extends JFrame {
	private static final long serialVersionUID = 4639754313889847228L;

	private static final int WINDOW_HEIGHT = 768;
	private static final int WINDOW_WIDTH = 1024;
	private static final String MAIN_LOGO_URL = "fr/bmj/bmjc/image/logo.png";

	private static final boolean USE_MIN_GAME = false;
	private static final boolean DISPLAY_REAL_NAME = false;

	private final DataAccess dataAccess;

	private final JMenuItem menuItemExport;
	private final JRadioButtonMenuItem menuSettingsFullName;
	private final JRadioButtonMenuItem menuSettingsDisplayName;
	private final JCheckBoxMenuItem menuSettingsUseMinGame;

	private final JTabbedPane tabbedPane;
	private final UITabPanel tabsMainManagePlayer;
	private final JPanel tabRCR;
	private final JPanel tabMCR;
	private JPanel currentSubTab;
	private final UITabPanel[] tabsRCR;
	private final UITabPanel[] tabsMCR;
	private final ChangeListener tabPaneChangeListener;

	private static final int WAITING_DIALOG_WIDTH = 240;
	private static final int WAITING_DIALOG_HEIGHT = 60;
	private final JDialogWithProgress waitingDialog;

	public UIMainWindow(final String databaseName, final DataAccess dataAccess) {
		super("Breizh Mahjong Score: " + databaseName);

		this.dataAccess = dataAccess;
		dataAccess.initialize();

		try {
			final URL icon = ClassLoader.getSystemResource(MAIN_LOGO_URL);
			setIconImage(ImageIO.read(icon));
		} catch (final Exception e) {
		}

		waitingDialog = new JDialogWithProgress(this, true);
		waitingDialog.setText("Chargement. Veuillez patienter...");
		waitingDialog.setPreferredSize(new Dimension(WAITING_DIALOG_WIDTH, WAITING_DIALOG_HEIGHT));
		waitingDialog.pack();

		final Container mainPane = getContentPane();
		mainPane.setLayout(new BorderLayout());
		tabbedPane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
		mainPane.add(tabbedPane, BorderLayout.CENTER);

		tabsMainManagePlayer = new UITabPanelManagePlayer(dataAccess, waitingDialog);
		tabbedPane.addTab(tabsMainManagePlayer.getTabName(), tabsMainManagePlayer);

		tabRCR = new JPanel();
		tabbedPane.addTab("RCR", tabRCR);

		tabsRCR = new UITabPanel[6];
		tabsRCR[0] = new UITabPanelRCRManage(dataAccess, waitingDialog);
		tabsRCR[1] = new UITabPanelRCRNewGame(dataAccess, waitingDialog);
		tabsRCR[2] = new UITabPanelRCRClubRanking(dataAccess, waitingDialog);
		tabsRCR[3] = new UITabPanelRCRTrend(dataAccess, waitingDialog);
		tabsRCR[4] = new UITabPanelRCRPersonalAnalyse(dataAccess, waitingDialog);
		tabsRCR[5] = new UITabPanelRCRGameHistory(dataAccess, waitingDialog);

		tabMCR = new JPanel();
		tabbedPane.addTab("MCR", tabMCR);

		tabsMCR = new UITabPanel[6];
		tabsMCR[0] = new UITabPanelMCRManage(dataAccess, waitingDialog);
		tabsMCR[1] = new UITabPanelMCRNewGame(dataAccess, waitingDialog);
		tabsMCR[2] = new UITabPanelMCRClubRanking(dataAccess, waitingDialog);
		tabsMCR[3] = new UITabPanelMCRTrend(dataAccess, waitingDialog);
		tabsMCR[4] = new UITabPanelMCRPersonalAnalyse(dataAccess, waitingDialog);
		tabsMCR[5] = new UITabPanelMCRGameHistory(dataAccess, waitingDialog);

		tabPaneChangeListener = new TabPaneChangeListener();
		tabbedPane.addChangeListener(tabPaneChangeListener);

		final JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		final JMenu menuFile = new JMenu("Fichier");
		menuFile.setMnemonic(KeyEvent.VK_F);
		menuBar.add(menuFile);

		menuItemExport = new JMenuItem("Exporter");
		menuItemExport.setMnemonic(KeyEvent.VK_E);
		menuItemExport.addActionListener(new MenuFileExportActionListener());
		menuFile.add(menuItemExport);
		menuFile.addSeparator();

		final JMenuItem menuItemFileExit = new JMenuItem("Quitter");
		menuItemFileExit.setMnemonic(KeyEvent.VK_X);
		menuItemFileExit.addActionListener(new MenuFileExitActionListener());
		menuFile.add(menuItemFileExit);

		final JMenu menuSettings = new JMenu("Paramètres");
		menuSettings.setMnemonic(KeyEvent.VK_S);
		menuBar.add(menuSettings);

		final ButtonGroup nameModeMenuButtonGroup = new ButtonGroup();
		final MenuSettingsNameActionListener nameModeActionListener = new MenuSettingsNameActionListener();
		menuSettingsFullName = new JRadioButtonMenuItem("Nom prénom");
		menuSettingsFullName.setMnemonic(KeyEvent.VK_F);
		menuSettingsFullName.setSelected(DISPLAY_REAL_NAME);
		menuSettingsFullName.addActionListener(nameModeActionListener);
		menuSettings.add(menuSettingsFullName);
		nameModeMenuButtonGroup.add(menuSettingsFullName);

		menuSettingsDisplayName = new JRadioButtonMenuItem("Pseudo");
		menuSettingsDisplayName.setMnemonic(KeyEvent.VK_D);
		menuSettingsDisplayName.setSelected(!DISPLAY_REAL_NAME);
		menuSettingsDisplayName.addActionListener(nameModeActionListener);
		menuSettings.add(menuSettingsDisplayName);
		nameModeMenuButtonGroup.add(menuSettingsDisplayName);

		menuSettings.addSeparator();

		menuSettingsUseMinGame = new JCheckBoxMenuItem("Min nb de parties");
		menuSettingsUseMinGame.setMnemonic(KeyEvent.VK_M);
		menuSettingsUseMinGame.setSelected(USE_MIN_GAME);
		menuSettingsUseMinGame.addActionListener(new MenuSettingsMinGameActionListener());
		menuSettings.add(menuSettingsUseMinGame);

		addWindowListener(new MainWindowListener());
		setMinimumSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
		setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		pack();
		setVisible(true);

		changeTab();
		setUseMinGame();
		setDisplayFullName();
	}

	private class TabPaneChangeListener implements ChangeListener {
		@Override
		public void stateChanged(final ChangeEvent e) {
			changeTab();
		}
	}

	private void changeTab() {
		tabbedPane.removeChangeListener(tabPaneChangeListener);
		final Component selectedComponent = tabbedPane.getSelectedComponent();
		if (selectedComponent == tabsMainManagePlayer) {
			if (currentSubTab == tabMCR) {
				removeTabs(3, 8);
			} else if (currentSubTab == tabRCR) {
				removeTabs(2, 7);
			}
			currentSubTab = tabsMainManagePlayer;
		} else if (selectedComponent == tabRCR) {
			if (currentSubTab == tabMCR) {
				removeTabs(3, 8);
			}
			if (currentSubTab != tabRCR) {
				addTabs(2, tabsRCR);
			}
			currentSubTab = tabRCR;
			tabbedPane.setSelectedIndex(2);
		} else if (selectedComponent == tabMCR) {
			if (currentSubTab == tabRCR) {
				removeTabs(2, 7);
			}
			if (currentSubTab != tabMCR) {
				addTabs(3, tabsMCR);
			}
			currentSubTab = tabMCR;
			tabbedPane.setSelectedIndex(3);
		}

		final UITabPanel tab = getCurrentTab();
		if (tab != null) {
			tab.refresh();
			menuItemExport.setEnabled(tab.canExport());
		}

		tabbedPane.addChangeListener(tabPaneChangeListener);
	}

	private void removeTabs(final int startIndex, final int endIndex) {
		for (int index = endIndex; index >= startIndex; index--) {
			tabbedPane.removeTabAt(index);
		}
	}

	private void addTabs(final int startIndex, final UITabPanel[] tabs) {
		for (int index = 0; index < tabs.length; index++) {
			tabbedPane.insertTab(tabs[index].getTabName(), null, tabs[index], null, startIndex + index);
		}
	}

	private UITabPanel getCurrentTab() {
		if (currentSubTab == tabsMainManagePlayer) {
			return tabsMainManagePlayer;
		} else if (currentSubTab == tabRCR) {
			return tabsRCR[tabbedPane.getSelectedIndex() - 2];
		} else if (currentSubTab == tabMCR) {
			return tabsMCR[tabbedPane.getSelectedIndex() - 3];
		} else {
			return null;
		}
	}

	private class MenuFileExportActionListener implements ActionListener {
		@Override
		public void actionPerformed(final ActionEvent e) {
			final UITabPanel tab = getCurrentTab();
			if (tab != null && tab.canExport()) {
				tab.export();
			}
		}
	}

	private class MenuFileExitActionListener implements ActionListener {
		@Override
		public void actionPerformed(final ActionEvent e) {
			exit();
		}
	}

	private class MainWindowListener extends WindowAdapter {
		@Override
		public void windowClosing(final WindowEvent e) {
			exit();
		}
	}

	private void exit() {
		dataAccess.disconnect();
		dispose();
	}

	private class MenuSettingsNameActionListener implements ActionListener {
		@Override
		public void actionPerformed(final ActionEvent e) {
			setDisplayFullName();
		}
	}

	private void setDisplayFullName() {
		final boolean displayFullName = menuSettingsFullName.isSelected();
		final UITabPanel tab = getCurrentTab();
		tabsMainManagePlayer.setDisplayFullName(displayFullName, tab == tabsMainManagePlayer);
		for (int index = 0; index < tabsRCR.length; index++) {
			tabsRCR[index].setDisplayFullName(displayFullName, tabsRCR[index] == tab);
		}
		for (int index = 0; index < tabsMCR.length; index++) {
			tabsMCR[index].setDisplayFullName(displayFullName, tabsMCR[index] == tab);
		}
	}

	private class MenuSettingsMinGameActionListener implements ActionListener {
		@Override
		public void actionPerformed(final ActionEvent e) {
			setUseMinGame();
		}
	}

	private void setUseMinGame() {
		dataAccess.setRCRUseMinimumGame(menuSettingsUseMinGame.isSelected());
		dataAccess.setMCRUseMinimumGame(menuSettingsUseMinGame.isSelected());
		final UITabPanel tab = getCurrentTab();
		tab.refresh();
	}

}
