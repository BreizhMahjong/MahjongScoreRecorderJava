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
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import fr.bmj.bmjc.dataaccess.DataAccess;
import fr.bmj.bmjc.gui.mcr.UITabPanelMCRClubRanking;
import fr.bmj.bmjc.gui.mcr.UITabPanelMCRGameHistory;
import fr.bmj.bmjc.gui.mcr.UITabPanelMCRManage;
import fr.bmj.bmjc.gui.mcr.UITabPanelMCRNewGame;
import fr.bmj.bmjc.gui.mcr.UITabPanelMCRPersonalAnalyse;
import fr.bmj.bmjc.gui.rcr.UITabPanelRCRClubRanking;
import fr.bmj.bmjc.gui.rcr.UITabPanelRCRGameHistory;
import fr.bmj.bmjc.gui.rcr.UITabPanelRCRManage;
import fr.bmj.bmjc.gui.rcr.UITabPanelRCRNewGame;
import fr.bmj.bmjc.gui.rcr.UITabPanelRCRPersonalAnalyse;
import fr.bmj.bmjc.swing.JDialogWithProgress;

public class UIMainWindow extends JFrame implements WindowListener {
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

	private static final int NB_RCR_TABS = 5;
	private static final int NB_MCR_TABS = 5;
	private static final int TAB_RCR_INDEX = 2;
	private static final int TAB_MCR_INDEX = 3;

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
		tabbedPane = new JTabbedPane(SwingConstants.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
		mainPane.add(tabbedPane, BorderLayout.CENTER);

		tabsMainManagePlayer = new UITabPanelManagePlayer(dataAccess, waitingDialog);
		tabbedPane.addTab(tabsMainManagePlayer.getTabName(), tabsMainManagePlayer);

		tabRCR = new JPanel();
		tabbedPane.addTab("RCR", tabRCR);

		tabsRCR = new UITabPanel[NB_RCR_TABS];
		tabsRCR[0] = new UITabPanelRCRManage(dataAccess, waitingDialog);
		tabsRCR[1] = new UITabPanelRCRNewGame(dataAccess, waitingDialog);
		tabsRCR[2] = new UITabPanelRCRClubRanking(dataAccess, waitingDialog);
		// tabsRCR[3] = new UITabPanelRCRTrend(dataAccess, waitingDialog);
		tabsRCR[3] = new UITabPanelRCRPersonalAnalyse(dataAccess, waitingDialog);
		tabsRCR[4] = new UITabPanelRCRGameHistory(dataAccess, waitingDialog);

		tabMCR = new JPanel();
		tabbedPane.addTab("MCR", tabMCR);

		tabsMCR = new UITabPanel[NB_MCR_TABS];
		tabsMCR[0] = new UITabPanelMCRManage(dataAccess, waitingDialog);
		tabsMCR[1] = new UITabPanelMCRNewGame(dataAccess, waitingDialog);
		tabsMCR[2] = new UITabPanelMCRClubRanking(dataAccess, waitingDialog);
		// tabsMCR[3] = new UITabPanelMCRTrend(dataAccess, waitingDialog);
		tabsMCR[3] = new UITabPanelMCRPersonalAnalyse(dataAccess, waitingDialog);
		tabsMCR[4] = new UITabPanelMCRGameHistory(dataAccess, waitingDialog);

		tabPaneChangeListener = (final ChangeEvent e) -> changeTab();
		tabbedPane.addChangeListener(tabPaneChangeListener);

		final JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		final JMenu menuFile = new JMenu("Fichier");
		menuFile.setMnemonic(KeyEvent.VK_F);
		menuBar.add(menuFile);

		menuItemExport = new JMenuItem("Exporter");
		menuItemExport.setMnemonic(KeyEvent.VK_E);
		menuItemExport.addActionListener((final ActionEvent e) -> export());
		menuFile.add(menuItemExport);
		menuFile.addSeparator();

		final JMenuItem menuItemFileExit = new JMenuItem("Quitter");
		menuItemFileExit.setMnemonic(KeyEvent.VK_X);
		menuItemFileExit.addActionListener((final ActionEvent e) -> exit());
		menuFile.add(menuItemFileExit);

		final JMenu menuSettings = new JMenu("Paramètres");
		menuSettings.setMnemonic(KeyEvent.VK_S);
		menuBar.add(menuSettings);

		final ButtonGroup nameModeMenuButtonGroup = new ButtonGroup();
		final ActionListener nameModeActionListener = (final ActionEvent e) -> setDisplayFullName();
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
		menuSettingsUseMinGame.addActionListener((final ActionEvent e) -> setUseMinGame());
		menuSettings.add(menuSettingsUseMinGame);

		addWindowListener(this);
		setMinimumSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
		setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		pack();
		setVisible(true);

		changeTab();
		setUseMinGame();
		setDisplayFullName();
	}

	private void changeTab() {
		tabbedPane.removeChangeListener(tabPaneChangeListener);
		final Component selectedComponent = tabbedPane.getSelectedComponent();
		if (selectedComponent == tabsMainManagePlayer) {
			if (currentSubTab == tabMCR) {
				removeTabs(TAB_MCR_INDEX, TAB_MCR_INDEX + NB_MCR_TABS - 1);
			} else if (currentSubTab == tabRCR) {
				removeTabs(TAB_RCR_INDEX, TAB_RCR_INDEX + NB_RCR_TABS - 1);
			}
			currentSubTab = tabsMainManagePlayer;
		} else if (selectedComponent == tabRCR) {
			if (currentSubTab == tabMCR) {
				removeTabs(TAB_MCR_INDEX, TAB_MCR_INDEX + NB_MCR_TABS - 1);
			}
			if (currentSubTab != tabRCR) {
				addTabs(TAB_RCR_INDEX, tabsRCR);
			}
			currentSubTab = tabRCR;
			tabbedPane.setSelectedIndex(TAB_RCR_INDEX);
		} else if (selectedComponent == tabMCR) {
			if (currentSubTab == tabRCR) {
				removeTabs(TAB_RCR_INDEX, TAB_RCR_INDEX + NB_RCR_TABS - 1);
			}
			if (currentSubTab != tabMCR) {
				addTabs(TAB_MCR_INDEX, tabsMCR);
			}
			currentSubTab = tabMCR;
			tabbedPane.setSelectedIndex(TAB_MCR_INDEX);
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

	private void export() {
		final UITabPanel tab = getCurrentTab();
		if (tab != null && tab.canExport()) {
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
		tabsMainManagePlayer.setDisplayFullName(displayFullName, tab == tabsMainManagePlayer);
		for (int index = 0; index < tabsRCR.length; index++) {
			tabsRCR[index].setDisplayFullName(displayFullName, tabsRCR[index] == tab);
		}
		for (int index = 0; index < tabsMCR.length; index++) {
			tabsMCR[index].setDisplayFullName(displayFullName, tabsMCR[index] == tab);
		}
	}

	private void setUseMinGame() {
		dataAccess.setRCRUseMinimumGame(menuSettingsUseMinGame.isSelected());
		dataAccess.setMCRUseMinimumGame(menuSettingsUseMinGame.isSelected());
		final UITabPanel tab = getCurrentTab();
		tab.refresh();
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
