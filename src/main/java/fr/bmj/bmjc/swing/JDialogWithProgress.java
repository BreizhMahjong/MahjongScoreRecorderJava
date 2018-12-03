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
package fr.bmj.bmjc.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.Window;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;

public class JDialogWithProgress extends JDialog implements ComponentListener {

	private static final long serialVersionUID = 4668215293557400804L;

	public JDialogWithProgress() {
		this((Frame) null, null, false, null);
	}

	public JDialogWithProgress(final Dialog owner, final String title, final boolean modal, final GraphicsConfiguration gc) {
		super(owner, title, modal, gc);
		super.getContentPane().setLayout(new BorderLayout());
		textLabel = new JLabel("Reading. Please wait...", SwingConstants.CENTER);
		textLabel.setOpaque(true);
		textLabel.setBackground(Color.WHITE);
		textLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		progressBar = new JProgressBar(SwingConstants.HORIZONTAL, 0, 100);
		progressBar.setStringPainted(true);
		progressBar.setOpaque(true);
		super.getContentPane().add(textLabel, BorderLayout.CENTER);
		super.getContentPane().add(progressBar, BorderLayout.SOUTH);

		addComponentListener(this);
		setUndecorated(true);
		pack();
	}

	public JDialogWithProgress(final Dialog owner, final String title, final boolean modal) {
		this(owner, title, modal, null);
	}

	public JDialogWithProgress(final Dialog owner, final boolean modal) {
		this(owner, null, modal, null);
	}

	public JDialogWithProgress(final Dialog owner, final String title) {
		this(owner, title, false, null);
	}

	public JDialogWithProgress(final Dialog owner) {
		this(owner, null, false, null);
	}

	public JDialogWithProgress(final Frame owner, final String title, final boolean modal, final GraphicsConfiguration gc) {
		super(owner, title, modal, gc);
		super.getContentPane().setLayout(new BorderLayout());
		textLabel = new JLabel("Reading. Please wait...", SwingConstants.CENTER);
		textLabel.setOpaque(true);
		textLabel.setBackground(Color.WHITE);
		textLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		progressBar = new JProgressBar(SwingConstants.HORIZONTAL, 0, 100);
		progressBar.setStringPainted(true);
		progressBar.setOpaque(true);
		super.getContentPane().add(textLabel, BorderLayout.CENTER);
		super.getContentPane().add(progressBar, BorderLayout.SOUTH);

		addComponentListener(this);
		setUndecorated(true);
		pack();
	}

	public JDialogWithProgress(final Frame owner, final String title, final boolean modal) {
		this(owner, title, modal, null);
	}

	public JDialogWithProgress(final Frame owner, final boolean modal) {
		this(owner, null, modal, null);
	}

	public JDialogWithProgress(final Frame owner, final String title) {
		this(owner, title, false, null);
	}

	public JDialogWithProgress(final Frame owner) {
		this(owner, null, false, null);
	}

	public JDialogWithProgress(final Window owner, final String title, final ModalityType modal, final GraphicsConfiguration gc) {
		super(owner, title, modal, gc);
		super.getContentPane().setLayout(new BorderLayout());
		textLabel = new JLabel("Reading. Please wait...", SwingConstants.CENTER);
		textLabel.setOpaque(true);
		textLabel.setBackground(Color.WHITE);
		textLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		progressBar = new JProgressBar(SwingConstants.HORIZONTAL, 0, 100);
		progressBar.setStringPainted(true);
		progressBar.setOpaque(true);
		super.getContentPane().add(textLabel, BorderLayout.CENTER);
		super.getContentPane().add(progressBar, BorderLayout.SOUTH);

		addComponentListener(this);
		setUndecorated(true);
		pack();
	}

	public JDialogWithProgress(final Window owner, final String title, final ModalityType modal) {
		this(owner, title, modal, null);
	}

	public JDialogWithProgress(final Window owner, final ModalityType modal) {
		this(owner, null, modal, null);
	}

	public JDialogWithProgress(final Window owner, final String title) {
		this(owner, title, ModalityType.MODELESS, null);
	}

	public JDialogWithProgress(final Window owner) {
		this(owner, null, ModalityType.MODELESS, null);
	}

	private final JLabel textLabel;
	private final JProgressBar progressBar;

	@Override
	public Container getContentPane() {
		return null;
	}

	public void setText(final String text) {
		textLabel.setText(text);
	}

	public void setProgress(final int progress) {
		if (progress >= 0 && progress <= 100) {
			progressBar.setValue(progress);
			progressBar.setString(progress + "%");
			repaint();
		}
	}

	private ComponentShownListener listener;

	public void setComponentShownListener(final ComponentShownListener l) {
		if (l != null) {
			listener = l;
		}
	}

	public void removeComponentShownListener() {
		listener = null;
	}

	@Override
	public void componentResized(final ComponentEvent e) {
	}

	@Override
	public void componentMoved(final ComponentEvent e) {
	}

	@Override
	public void componentShown(final ComponentEvent e) {
		if (listener != null) {
			listener.componentShown(e);
		}
	}

	@Override
	public void componentHidden(final ComponentEvent e) {
	}

}
