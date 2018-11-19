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
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class JDatePicker extends JDialog {

	private static final long serialVersionUID = 1212732278398188351L;
	private static final Dimension DATE_CASE_SIZE = new Dimension(45, 30);

	private boolean dateSelected = false;
	private final Calendar calendar;
	private int year;
	private int month;
	private int day;

	private final JSpinner spinnerYear;
	private final JComboBox<String> comboMonth;
	private final JComboBox<Integer> comboDay;
	private final ActionListener comboDayActionListener;
	private final int firstDayOfWeek;
	private int firstDayInList;
	private final List<DateLabel> labels = new ArrayList<>(42);

	public JDatePicker(final Window owner) {
		this(owner, null, Calendar.SUNDAY, null);
	}

	public JDatePicker(final Window owner, final Locale locale) {
		this(owner, locale, Calendar.SUNDAY, null);
	}

	public JDatePicker(final Window owner, final int firstDayOfWeek) {
		this(owner, null, firstDayOfWeek, null);
	}

	public JDatePicker(final Window owner, final Locale locale, final int firstDayOfWeek) {
		this(owner, locale, firstDayOfWeek, null);
	}

	public JDatePicker(final Window owner, final Date currentDate) {
		this(owner, null, Calendar.SUNDAY, currentDate);
	}

	public JDatePicker(final Window owner, final Locale locale, final Date currentDate) {
		this(owner, locale, Calendar.SUNDAY, currentDate);
	}

	public JDatePicker(final Window owner, final int firstDayOfWeek, final Date currentDate) {
		this(owner, null, firstDayOfWeek, currentDate);
	}

	public JDatePicker(final Window owner, Locale locale, final int firstDayOfWeek, final Date currentDate) {
		super(owner, "Date", DEFAULT_MODALITY_TYPE);
		if (locale == null) {
			locale = Locale.getDefault();
		}
		calendar = Calendar.getInstance(locale);
		if (currentDate != null) {
			calendar.setTime(currentDate);
		}
		calendar.set(Calendar.HOUR, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		year = calendar.get(Calendar.YEAR);
		month = calendar.get(Calendar.MONTH);
		day = calendar.get(Calendar.DAY_OF_MONTH);

		if (firstDayOfWeek < Calendar.SUNDAY || firstDayOfWeek > Calendar.SATURDAY) {
			this.firstDayOfWeek = calendar.getFirstDayOfWeek();
		} else {
			this.firstDayOfWeek = firstDayOfWeek;
		}
		final DateFormatSymbols dfs = DateFormatSymbols.getInstance(locale);

		final Container pane = getContentPane();
		pane.setLayout(new BorderLayout());

		final JPanel northPanel = new JPanel(new GridLayout(1, 3, 4, 4));
		spinnerYear = new JSpinner(new SpinnerNumberModel(year, 1970, 9999, 1));
		((JSpinner.NumberEditor) spinnerYear.getEditor()).getFormat().setGroupingUsed(false);
		spinnerYear.addChangeListener(new YearSpinnerChangeListener());
		northPanel.add(spinnerYear);
		final String monthString[] = Arrays.copyOf(dfs.getMonths(), 12);
		comboMonth = new JComboBox<>(monthString);
		comboMonth.setSelectedIndex(month);
		comboMonth.setEditable(false);
		comboMonth.addActionListener(new MonthComboActionListener());
		comboMonth.addMouseWheelListener(new ComboBoxMouseWheelListener(comboMonth));
		northPanel.add(comboMonth);
		comboDay = new JComboBox<>(getDays(year, month));
		comboDay.setSelectedIndex(day - 1);
		comboDay.setEditable(false);
		comboDay.addMouseWheelListener(new ComboBoxMouseWheelListener(comboDay));
		comboDayActionListener = new DayComboActionListener();
		northPanel.add(comboDay);
		pane.add(northPanel, BorderLayout.NORTH);

		final JPanel centerPanel = new JPanel(new GridLayout(7, 7));
		final String weekStrings[] = Arrays.copyOfRange(dfs.getShortWeekdays(), 1, 8);
		for (int w = 0; w < 7; w++) {
			final int weekDay = (this.firstDayOfWeek + w - 1) % 7;
			final JLabel weekLabel = new JLabel(weekStrings[weekDay], JLabel.CENTER);
			weekLabel.setPreferredSize(DATE_CASE_SIZE);
			weekLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
			centerPanel.add(weekLabel);
		}
		for (int d = 0; d < 42; d++) {
			final DateLabel label = new DateLabel();
			label.setPreferredSize(DATE_CASE_SIZE);
			label.addMouseListener(new DateLabelMouseListener(label));
			label.setDate(0);
			labels.add(label);
			centerPanel.add(label);
		}
		pane.add(centerPanel, BorderLayout.CENTER);

		final Dimension buttonSize = new Dimension(80, 24);
		final JPanel southPanel = new JPanel();
		southPanel.setLayout(new GridBagLayout());
		final GridBagConstraints southC = new GridBagConstraints();
		southC.insets = new Insets(2, 2, 2, 2);
		southC.fill = GridBagConstraints.NONE;
		southC.gridwidth = 1;
		southC.gridheight = 1;
		southC.weightx = 1.0;
		southC.gridy = 0;

		final JButton selectButton = new JButton(UIManager.getString("OptionPane.okButtonText", locale));
		selectButton.addActionListener(new SelectButtonActionListener());
		selectButton.setPreferredSize(buttonSize);
		southC.gridx = 0;
		southC.anchor = GridBagConstraints.EAST;
		southPanel.add(selectButton, southC);

		southC.gridx = 1;
		southC.anchor = GridBagConstraints.CENTER;
		southPanel.add(new JPanel(), southC);

		final JButton cancelButton = new JButton(UIManager.getString("OptionPane.cancelButtonText", locale));
		cancelButton.addActionListener(new CancelButtonActionListener());
		cancelButton.setPreferredSize(buttonSize);
		southC.gridx = 2;
		southC.anchor = GridBagConstraints.WEST;
		southPanel.add(cancelButton, southC);

		pane.add(southPanel, BorderLayout.SOUTH);

		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		pack();
		setResizable(false);
		refresh();
	}

	private DateLabel lastSelectedLabel;
	private static final Color DISABLE_COLOR = new Color(223, 223, 223);
	private static final Color ENABLE_COLOR = new Color(255, 255, 255);
	private static final Color SELECTED_COLOR = new Color(255, 191, 191);

	private class DateLabel extends JLabel {
		private static final long serialVersionUID = 8569975519774021654L;
		private int date;

		public DateLabel() {
			super("", JLabel.CENTER);
			setOpaque(true);
			setBorder(BorderFactory.createLineBorder(Color.BLACK));
		}

		public void setDate(final int date) {
			this.date = date;
			if (date > 0) {
				setText("" + date);
				setBackground(ENABLE_COLOR);
			} else {
				setText("");
				setBackground(DISABLE_COLOR);
			}
			repaint();
		}

		public int getDate() {
			return date;
		}

		public void select() {
			setBackground(SELECTED_COLOR);
			repaint();
		}

		public void unselect() {
			setBackground(ENABLE_COLOR);
			repaint();
		}
	}

	private class YearSpinnerChangeListener implements ChangeListener {
		@Override
		public void stateChanged(final ChangeEvent e) {
			year = (int) spinnerYear.getValue();
			refresh();
		}
	}

	private class MonthComboActionListener implements ActionListener {
		@Override
		public void actionPerformed(final ActionEvent e) {
			month = comboMonth.getSelectedIndex();
			refresh();
		}
	}

	private class DayComboActionListener implements ActionListener {
		@Override
		public void actionPerformed(final ActionEvent e) {
			final int date = comboDay.getSelectedIndex();
			lastSelectedLabel.unselect();
			lastSelectedLabel = labels.get(firstDayInList + date);
			lastSelectedLabel.select();
			day = date + 1;
		}
	}

	private class DateLabelMouseListener extends MouseAdapter {
		private final DateLabel label;

		public DateLabelMouseListener(final DateLabel label) {
			this.label = label;
		}

		@Override
		public void mousePressed(final MouseEvent e) {
			final int date = label.getDate();
			if (date > 0) {
				lastSelectedLabel.unselect();
				lastSelectedLabel = label;
				lastSelectedLabel.select();
				comboDay.removeActionListener(comboDayActionListener);
				comboDay.setSelectedIndex(date - 1);
				comboDay.addActionListener(comboDayActionListener);
				day = date;
			}
		}
	}

	private class SelectButtonActionListener implements ActionListener {
		@Override
		public void actionPerformed(final ActionEvent e) {
			dateSelected = true;
			dispose();
		}
	}

	private class CancelButtonActionListener implements ActionListener {
		@Override
		public void actionPerformed(final ActionEvent e) {
			dispose();
		}
	}

	private class ComboBoxMouseWheelListener implements MouseWheelListener {
		private final JComboBox<?> combobox;

		public ComboBoxMouseWheelListener(final JComboBox<?> combobox) {
			this.combobox = combobox;
		}

		@Override
		public void mouseWheelMoved(final MouseWheelEvent e) {
			if (combobox != null) {
				if (e.getWheelRotation() > 0) {
					if (combobox.getSelectedIndex() < combobox.getItemCount() - 1) {
						combobox.setSelectedIndex(combobox.getSelectedIndex() + 1);
					}
				} else {
					if (combobox.getSelectedIndex() > 0) {
						combobox.setSelectedIndex(combobox.getSelectedIndex() - 1);
					}
				}
			}

		}
	}

	private void refresh() {
		calendar.set(Calendar.YEAR, year);
		calendar.set(Calendar.MONTH, month);

		final Integer dayString[] = getDays(year, month);
		if (day > dayString.length) {
			day = dayString.length;
		}

		calendar.set(Calendar.DAY_OF_MONTH, 1);
		firstDayInList = (calendar.get(Calendar.DAY_OF_WEEK) - firstDayOfWeek + 7) % 7;

		for (int d = 0; d < firstDayInList; d++) {
			labels.get(d).setDate(0);
		}
		for (int d = 0; d < dayString.length; d++) {
			labels.get(firstDayInList + d).setDate(d + 1);
			if (d + 1 == day) {
				labels.get(firstDayInList + d).select();
				lastSelectedLabel = labels.get(firstDayInList + d);
			}
		}
		for (int d = firstDayInList + dayString.length; d < 42; d++) {
			labels.get(d).setDate(0);
		}

		comboDay.removeActionListener(comboDayActionListener);
		comboDay.removeAllItems();
		for (final Integer i : dayString) {
			comboDay.addItem(i);
		}
		comboDay.addActionListener(comboDayActionListener);
		comboDay.setSelectedIndex(day - 1);
	}

	@Override
	public void setVisible(final boolean b) {
		dateSelected = false;
		super.setVisible(b);
	}

	/**
	 * If OK button is pressed, return true. If Cancel button or close button is pressed return false.
	 *
	 * @return
	 */
	public boolean dateSelected() {
		return dateSelected;
	}

	/**
	 * Return the selected date
	 *
	 * @return
	 */
	public Date getSelectedDate() {
		calendar.set(Calendar.YEAR, year);
		calendar.set(Calendar.MONTH, month);
		calendar.set(Calendar.DAY_OF_MONTH, day);
		return calendar.getTime();
	}

	private static final Integer[] DAY_STRING_31 = {
		1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31
	};
	private static final Integer[] DAY_STRING_30 = {
		1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30
	};
	private static final Integer[] DAY_STRING_29 = {
		1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29
	};
	private static final Integer[] DAY_STRING_28 = {
		1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28
	};

	private Integer[] getDays(final int year, final int month) {
		switch (month) {
			case Calendar.JANUARY:
			case Calendar.MARCH:
			case Calendar.MAY:
			case Calendar.JULY:
			case Calendar.AUGUST:
			case Calendar.OCTOBER:
			case Calendar.DECEMBER:
				return Arrays.copyOf(DAY_STRING_31, DAY_STRING_31.length);
			case Calendar.APRIL:
			case Calendar.JUNE:
			case Calendar.SEPTEMBER:
			case Calendar.NOVEMBER:
				return Arrays.copyOf(DAY_STRING_30, DAY_STRING_30.length);
			case Calendar.FEBRUARY:
				if (year % 400 == 0 || year % 4 == 0 && year % 100 != 0) {
					return Arrays.copyOf(DAY_STRING_29, DAY_STRING_29.length);
				} else {
					return Arrays.copyOf(DAY_STRING_28, DAY_STRING_28.length);
				}
			default:
				return null;
		}
	}

}
