package fr.bmj.bmjc.dataaccess.abs.rcr;

import java.util.List;

import fr.bmj.bmjc.data.game.Tournament;
import fr.bmj.bmjc.dataaccess.abs.UpdateResult;

public interface DataAccessRCRTournament {

	public UpdateResult addRCRTournament(final String tournamentName);

	public UpdateResult modifyRCRTournament(final short tournamentId, final String tournamentName);

	public List<Tournament> getRCRTournaments();

	public UpdateResult deleteRCRTournament(final short tournamentId);

}
