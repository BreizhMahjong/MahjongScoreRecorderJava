/*
 * This file is part of Breizh Mahjong Recorder.
 *
 * Breizh Mahjong Recorder is free software: you can redistribute it AND/or
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
package fr.bmj.bmjc.dataaccess.impl.db;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;

import fr.bmj.bmjc.dataaccess.abs.DataAccess;
import fr.bmj.bmjc.dataaccess.abs.player.DataAccessManagePlayer;
import fr.bmj.bmjc.dataaccess.abs.rcr.DataAccessRCR;
import fr.bmj.bmjc.dataaccess.impl.db.player.DataAccessDataBaseManagePlayer;
import fr.bmj.bmjc.dataaccess.impl.db.rcr.DataAccessDataBaseRCR;

public class DataAccessDataBase implements DataAccess {

	private static final String DATABASE_NAME = "DataBase";
	private Connection dataBaseConnection;

	private DataAccessManagePlayer dataAccessManagePlayer;
	private DataAccessRCR dataAccessRCR;

	public DataAccessDataBase() {
	}

	@Override
	public void initialize() {
		try {
			final File dataBaseFile = new File(DATABASE_NAME);
			Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
			if (dataBaseFile.exists() && dataBaseFile.isDirectory()) {
				dataBaseConnection = DriverManager.getConnection("jdbc:derby:" + dataBaseFile.getAbsolutePath());
				dataAccessManagePlayer = new DataAccessDataBaseManagePlayer(dataBaseConnection);
				dataAccessRCR = new DataAccessDataBaseRCR(dataBaseConnection);
			} else {
				dataBaseConnection = null;
			}
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean isConnected() {
		return dataBaseConnection != null;
	}

	@Override
	public void disconnect() {
		if (dataBaseConnection != null) {
			try {
				dataBaseConnection.close();
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public DataAccessManagePlayer getManagePlayer() {
		return dataAccessManagePlayer;
	}

	@Override
	public DataAccessRCR getRCR() {
		return dataAccessRCR;
	}

}
