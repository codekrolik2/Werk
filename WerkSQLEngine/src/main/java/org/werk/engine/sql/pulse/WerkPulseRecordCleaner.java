package org.werk.engine.sql.pulse;

import org.pillar.db.interfaces.TransactionContext;
import org.pulse.interfaces.ServerPulseDAO;
import org.pulse.interfaces.ServerPulseRecordCleaner;
import org.werk.engine.sql.DAO.JobLoadDAO;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class WerkPulseRecordCleaner implements ServerPulseRecordCleaner<Long> {
	protected JobLoadDAO jobLoadDAO;
	protected ServerPulseDAO<Long> serverPulseDAO;
	
	@Override
	public void deleteServer(TransactionContext tc, Long serverId) throws Exception {
		jobLoadDAO.freeOwnedJobs(tc, serverId);
		serverPulseDAO.deleteServer(tc, serverId);
	}
}
