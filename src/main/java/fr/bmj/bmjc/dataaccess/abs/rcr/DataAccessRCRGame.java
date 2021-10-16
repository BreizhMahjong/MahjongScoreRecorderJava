package fr.bmj.bmjc.dataaccess.abs.rcr;

import java.util.List;

import fr.bmj.bmjc.data.game.Player;
import fr.bmj.bmjc.data.game.Tournament;
import fr.bmj.bmjc.data.game.rcr.RCRGame;
import fr.bmj.bmjc.dataaccess.abs.UpdateResult;

public interface DataAccessRCRGame {

	public void setOnlyRegularPlayers(boolean onlyRegularPlayers);

	public UpdateResult addRCRGame(final RCRGame game);

	public List<Integer> getRCRYears(final Tournament tournament);

	public List<Integer> getRCRGameDays(final Tournament tournament,
		final int year,
		final int month);

	public List<Long> getRCRGameIds(final Tournament tournament,
		final int year,
		final int month,
		final int day);

	public RCRGame getRCRGame(final long id);

	public UpdateResult deleteRCRGame(final long id);

	public List<Player> getRCRPlayers();

}
