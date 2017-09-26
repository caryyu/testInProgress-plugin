package org.jenkinsci.testinprogress.server.events.build;

import java.io.Serializable;

/**
 * 
 * @author Cedric Chabanois (cchabanois at gmail.com)
 *
 */
public interface IBuildTestEventListener extends Serializable {

	public void event(BuildTestEvent buildTestEvent);
	
}
