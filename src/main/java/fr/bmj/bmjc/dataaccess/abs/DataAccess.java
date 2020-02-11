package fr.bmj.bmjc.dataaccess.abs;

import fr.bmj.bmjc.dataaccess.abs.player.DataAccessManagePlayer;
import fr.bmj.bmjc.dataaccess.abs.rcr.DataAccessRCR;

public interface DataAccess {

	public void initialize();

	public boolean isConnected();

	public void disconnect();

	public DataAccessManagePlayer getManagePlayer();

	public DataAccessRCR getRCR();

}
