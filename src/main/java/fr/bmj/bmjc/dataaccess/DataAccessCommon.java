package fr.bmj.bmjc.dataaccess;

import java.util.List;

import fr.bmj.bmjc.data.game.Player;

public interface DataAccessCommon {

	public void setOnlyFrequentPlayers(boolean onlyFrequentPlayers);

	public List<Player> getPlayers();

}
