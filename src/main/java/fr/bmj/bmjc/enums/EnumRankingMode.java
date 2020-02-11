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
	WIN_RATE_4("Taux victoire (4j)"),
	WIN_RATE_5("Taux victoire (5j)"),
	POSITIVE_RATE_4("Taux positif (4j)"),
	POSITIVE_RATE_5("Taux positif (5j)"),
	MENSUAL_TOTAL_FINAL_SCORE("Score total mensuel"),
	TRIMESTRIAL_TOTAL_FINAL_SCORE("Score total trimestriel"),
	ANNUAL_TOTAL_FINAL_SCORE("Score total annuel"),
	MENSUAL_TOTAL_GAME_SCORE("Stack total mensuel"),
	TRIMESTRIAL_TOTAL_GAME_SCORE("Stack total trimestriel"),
	ANNUAL_TOTAL_GAME_SCORE("Stack total annuel");

	private final String display;

	private EnumRankingMode(final String display) {
		this.display = display;
	}

	@Override
	public String toString() {
		return display;
	}
}
