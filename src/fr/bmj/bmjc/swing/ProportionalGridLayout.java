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

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager2;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ProportionalGridLayout implements LayoutManager2 {

	private final int rowCount;
	private final double[] weightX;
	private double totalWeightX;
	private final int colCount;
	private final double[] weightY;
	private double totalWeightY;
	private final int hgap;
	private final int vgap;
	private final Map<Component, ProportionalGridLayoutConstraint> constraints;

	public ProportionalGridLayout() {
		this(1, 1, 0, 0);
	}

	public ProportionalGridLayout(final int rowCount, final int colCount, final int hgap, final int vgap) {
		if (rowCount <= 0 || colCount <= 0) {
			throw new IllegalArgumentException("Row Count and Column Count should be positive.");
		}
		if (hgap < 0 || vgap < 0) {
			throw new IllegalArgumentException("HGap and VGap should be non negative.");
		}
		this.rowCount = rowCount;
		this.colCount = colCount;
		weightX = new double[colCount];
		Arrays.fill(weightX, 1.0);
		totalWeightX = colCount;
		weightY = new double[rowCount];
		Arrays.fill(weightY, 1.0);
		totalWeightY = rowCount;
		this.hgap = hgap;
		this.vgap = vgap;
		constraints = new HashMap<Component, ProportionalGridLayoutConstraint>(10, 1f);
	}

	public void setWeightX(final double... weightX) {
		if (weightX == null || weightX.length != colCount) {
			throw new IllegalArgumentException("The number of weight values should be equal to column count");
		} else {
			totalWeightX = 0.0;
			for (int x = 0; x < colCount; x++) {
				if (weightX[x] <= 0.0) {
					throw new IllegalArgumentException("Weight value should be positive");
				}
				this.weightX[x] = weightX[x];
				totalWeightX += weightX[x];
			}
		}
	}

	public void setWeightY(final double... weightY) {
		if (weightY == null || weightY.length != rowCount) {
			throw new IllegalArgumentException("The number of weight values should be equal to row count");
		} else {
			totalWeightY = 0.0;
			for (int y = 0; y < rowCount; y++) {
				if (weightY[y] <= 0.0) {
					throw new IllegalArgumentException("Weight value should be positive");
				}
				this.weightY[y] = weightY[y];
				totalWeightY += weightY[y];
			}
		}
	}

	@Override
	public void addLayoutComponent(final String name, final Component comp) {
	}

	@Override
	public void addLayoutComponent(final Component comp, final Object constraints) {
		if (constraints instanceof ProportionalGridLayoutConstraint) {
			final ProportionalGridLayoutConstraint c = (ProportionalGridLayoutConstraint) constraints;
			if (c.y < 0 || c.gridHeight <= 0 || c.y + c.gridHeight > rowCount) {
				throw new IllegalArgumentException("Invalid Row and RowSpan value");
			} else if (c.x < 0 || c.gridWidth <= 0 || c.x + c.gridWidth > colCount) {
				throw new IllegalArgumentException("Invalid Col and ColSpan value");
			}
			this.constraints.put(comp, ((ProportionalGridLayoutConstraint) constraints).clone());
		} else if (constraints != null) {
			throw new IllegalArgumentException("Cannot add to layout: constraints must be a EqualSizeGridBagLayoutConstraint");
		}
	}

	@Override
	public void removeLayoutComponent(final Component comp) {
		constraints.remove(comp);
	}

	@Override
	public Dimension minimumLayoutSize(final Container parent) {
		synchronized (parent.getTreeLock()) {
			final Insets insets = parent.getInsets();
			double minWidth = 0;
			double minHeight = 0;
			for (final Component component : constraints.keySet()) {
				final ProportionalGridLayoutConstraint c = constraints.get(component);
				final Dimension minSize = component.getMinimumSize();

				double xParts = 0.0;
				for (int x = 0; x < c.gridWidth; x++) {
					xParts += weightX[c.x + x];
				}
				final double gridWidth = (int) ((minSize.width - hgap * (c.gridWidth - 1)) / (xParts * c.gridWidth / totalWeightX));

				double yParts = 0.0;
				for (int y = 0; y < c.gridHeight; y++) {
					yParts += weightY[c.y + y];
				}
				final double gridHeight = (int) ((minSize.height - vgap * (c.gridHeight - 1)) / (yParts * c.gridHeight / totalWeightY));

				if (gridWidth > minWidth) {
					minWidth = gridWidth;
				}
				if (gridHeight > minHeight) {
					minHeight = gridHeight;
				}
			}
			return new Dimension(insets.left + insets.right + (int) (minWidth * colCount) + hgap * (colCount - 1), insets.top + insets.bottom + (int) (minHeight * rowCount) + vgap * (rowCount - 1));
		}
	}

	@Override
	public Dimension maximumLayoutSize(final Container target) {
		return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
	}

	@Override
	public Dimension preferredLayoutSize(final Container parent) {
		synchronized (parent.getTreeLock()) {
			final Insets insets = parent.getInsets();
			double minWidth = 0;
			double minHeight = 0;
			for (final Component component : constraints.keySet()) {
				final ProportionalGridLayoutConstraint c = constraints.get(component);
				final Dimension minSize = component.getPreferredSize();

				double xParts = 0.0;
				for (int x = 0; x < c.gridWidth; x++) {
					xParts += weightX[c.x + x];
				}
				final double gridWidth = (minSize.width - hgap * (c.gridWidth - 1)) / (xParts * colCount / totalWeightX);

				double yParts = 0.0;
				for (int y = 0; y < c.gridHeight; y++) {
					yParts += weightY[c.y + y];
				}
				final double gridHeight = (minSize.height - vgap * (c.gridHeight - 1)) / (yParts * rowCount / totalWeightY);

				if (gridWidth > minWidth) {
					minWidth = gridWidth;
				}
				if (gridHeight > minHeight) {
					minHeight = gridHeight;
				}
			}
			return new Dimension(insets.left + insets.right + (int) (minWidth * colCount) + hgap * (colCount - 1), insets.top + insets.bottom + (int) (minHeight * rowCount) + vgap * (rowCount - 1));
		}
	}

	@Override
	public void layoutContainer(final Container parent) {
		synchronized (parent.getTreeLock()) {
			final Insets insets = parent.getInsets();
			final int usableWidth = parent.getWidth() - (insets.left + insets.right) - hgap * (colCount - 1);

			final int[] gridWidth = new int[colCount];
			int totalWidth = 0;
			for (int x = 0; x < colCount; x++) {
				gridWidth[x] = (int) (weightX[x] * usableWidth / totalWeightX);
				totalWidth += gridWidth[x];
			}
			final int extraWidth = (usableWidth - totalWidth) / 2;

			final int usableHeight = parent.getHeight() - (insets.top + insets.bottom) - vgap * (rowCount - 1);
			final int[] gridHeight = new int[rowCount];
			int totalHeight = 0;
			for (int y = 0; y < rowCount; y++) {
				gridHeight[y] = (int) (weightY[y] * usableHeight / totalWeightY);
				totalHeight += gridHeight[y];
			}
			final int extraHeight = (usableHeight - totalHeight) / 2;

			if (parent.getComponentOrientation().isLeftToRight()) {
				for (final Component component : constraints.keySet()) {
					final ProportionalGridLayoutConstraint constraint = constraints.get(component);

					int x = insets.left + extraWidth + hgap * constraint.x;
					for (int x1 = 0; x1 < constraint.x; x1++) {
						x += gridWidth[x1];
					}

					int y = insets.top + extraHeight + vgap * constraint.y;
					for (int y1 = 0; y1 < constraint.y; y1++) {
						y += gridHeight[y1];
					}

					int width = hgap * (constraint.gridWidth - 1);
					for (int x1 = 0; x1 < constraint.gridWidth; x1++) {
						width += gridWidth[constraint.x + x1];
					}

					int height = vgap * (constraint.gridHeight - 1);
					for (int y1 = 0; y1 < constraint.gridHeight; y1++) {
						height += gridHeight[constraint.y + y1];
					}

					component.setBounds(x, y, width, height);
				}
			} else {
				for (final Component component : constraints.keySet()) {
					final ProportionalGridLayoutConstraint constraint = constraints.get(component);

					int x = insets.left + extraWidth + hgap * (colCount - constraint.x - 1);
					for (int x1 = colCount - 1; x1 > constraint.x; x1--) {
						x += gridWidth[x1];
					}

					int y = insets.top + extraHeight + vgap * constraint.y;
					for (int y1 = 0; y1 < constraint.y; y1++) {
						y += gridHeight[y1];
					}

					int width = hgap * (constraint.gridWidth - 1);
					for (int x1 = 0; x1 < constraint.gridWidth; x1++) {
						width += gridWidth[constraint.x + x1];
					}

					int height = vgap * (constraint.gridHeight - 1);
					for (int y1 = 0; y1 < constraint.gridHeight; y1++) {
						height += gridHeight[constraint.y + y1];
					}

					component.setBounds(x, y, width, height);
				}
			}
		}
	}

	@Override
	public float getLayoutAlignmentX(final Container target) {
		return 0.5f;
	}

	@Override
	public float getLayoutAlignmentY(final Container target) {
		return 0.5f;
	}

	@Override
	public void invalidateLayout(final Container target) {
	}

}
