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

public class ProportionalGridLayoutConstraint {

	public int x;
	public int gridWidth;
	public int y;
	public int gridHeight;

	public ProportionalGridLayoutConstraint() {
		this(0, 1, 0, 1);
	}

	public ProportionalGridLayoutConstraint(final int x, final int gridWidth, final int y, final int gridHeight) {
		this.x = x;
		this.gridWidth = gridWidth;
		this.y = y;
		this.gridHeight = gridHeight;
	}

	@Override
	public ProportionalGridLayoutConstraint clone() {
		return new ProportionalGridLayoutConstraint(x, gridWidth, y, gridHeight);
	}

}
