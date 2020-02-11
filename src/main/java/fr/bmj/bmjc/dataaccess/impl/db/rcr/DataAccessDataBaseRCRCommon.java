package fr.bmj.bmjc.dataaccess.impl.db.rcr;

import java.sql.Connection;
import java.sql.SQLException;

public abstract class DataAccessDataBaseRCRCommon {

	protected final Connection dataBaseConnection;

	public DataAccessDataBaseRCRCommon(final Connection dataBaseConnection) {
		this.dataBaseConnection = dataBaseConnection;
	}

	protected boolean isConnected() {
		try {
			return dataBaseConnection != null && !dataBaseConnection.isClosed();
		} catch (final SQLException e) {
			return false;
		}
	}

}
