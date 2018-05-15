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

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public abstract class UITabPanel extends JPanel {
	private static final long serialVersionUID = -784698626936944693L;

	protected static final int BUTTON_MIN_WIDTH = 96;
	protected static final int BUTTON_MIN_HEIGHT = 28;

	abstract public String getTabName();

	abstract public void refresh();

	abstract public void setDisplayFullName(boolean displayFullName, boolean toRefresh);

	abstract public boolean canExport();

	abstract public void export();

	protected static final String SEPARATOR = ";";

	private File lastSaveFilePath = null;

	protected File askSaveFileName(final String proposedSaveFileName) {
		final JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.setMultiSelectionEnabled(false);
		if (lastSaveFilePath != null) {
			fileChooser.setCurrentDirectory(lastSaveFilePath);
		}
		if (proposedSaveFileName != null) {
			fileChooser.setSelectedFile(new File(lastSaveFilePath, proposedSaveFileName));
		}

		while (true) {
			final int answer = fileChooser.showSaveDialog(this);
			if (answer == JFileChooser.APPROVE_OPTION) {
				final File selectedFile = fileChooser.getSelectedFile();
				if (selectedFile.exists()) {
					final int overwriteAnswer = JOptionPane.showConfirmDialog(this, "Le fichier existe déjà. Voulez-vous le remplacer ?", "Confirmer", JOptionPane.YES_NO_CANCEL_OPTION,
						JOptionPane.WARNING_MESSAGE);
					if (overwriteAnswer == JOptionPane.CANCEL_OPTION) {
						return null;
					} else if (overwriteAnswer == JOptionPane.YES_OPTION) {
						lastSaveFilePath = selectedFile.getParentFile();
						return selectedFile;
					}
				} else {
					lastSaveFilePath = selectedFile.getParentFile();
					return selectedFile;
				}
			} else {
				return null;
			}
		}
	}

}
