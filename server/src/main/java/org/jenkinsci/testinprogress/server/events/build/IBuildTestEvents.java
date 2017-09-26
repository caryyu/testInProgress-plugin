package org.jenkinsci.testinprogress.server.events.build;

import java.io.Serializable;
import java.util.List;

/**
 * Test events for a build
 * 
 * @author Cedric Chabanois (cchabanois at gmail.com)
 *
 */
public interface IBuildTestEvents extends Serializable {

	public abstract List<BuildTestEvent> getEvents();

}