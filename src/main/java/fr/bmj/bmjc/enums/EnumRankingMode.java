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
package fr.bmj.bmjc.enums;

public enum EnumRankingMode {
	TOTAL_FINAL_SCORE("Score Total"),
	MEAN_FINAL_SCORE("Score moyen"),
	BEST_FINAL_SCORE("Meilleur Score"),
	TOTAL_GAME_SCORE("Stack Total"),
	MEAN_GAME_SCORE("Stack moyen"),
	BEST_GAME_SCORE("Meilleur Stack"),
	WIN_RATE("Taux victoire"),
	POSITIVE_RATE("Taux positif"),
	MENSUAL_SCORE("Total mensuel"),
	TRIMESTRIAL_SCORE("Total trimestriel"),
	ANNUAL_SCORE("Total annuel");

	private final String display;

	private EnumRankingMode(final String display) {
		this.display = display;
	}

	@Override
	public String toString() {
		return display;
	}
}
