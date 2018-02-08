package org.werk.engine.sql.pulse;

import org.pulse.interfaces.ServerPulseListener;
import org.pulse.interfaces.ServerPulseRecord;

public class WerkPulseListener implements ServerPulseListener<Long> {
	@Override
	public void hbCreated(ServerPulseRecord<Long> server) {
		// TODO Auto-generated method stub
	}

	@Override
	public void hbUpdated(ServerPulseRecord<Long> server) {
		// TODO Auto-generated method stub
	}

	@Override
	public void hbLost(Exception e) {
		// TODO Auto-generated method stub
	}
}
