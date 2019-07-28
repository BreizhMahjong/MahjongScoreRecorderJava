package fr.bmj.bmjc.dataaccess;

import fr.bmj.bmjc.dataaccess.rcr.DataAccessRCR;

public interface DataAccess extends DataAccessManagePlayer, DataAccessRCR {

	public void initialize();

	public boolean isConnected();

	public void disconnect();

}
