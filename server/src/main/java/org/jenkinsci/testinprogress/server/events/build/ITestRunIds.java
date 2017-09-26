package org.jenkinsci.testinprogress.server.events.build;

import java.io.Serializable;
import java.util.List;

/**
 * Run ids used for a build
 * 
 * @author Cedric Chabanois (cchabanois at gmail.com)
 *
 */
public interface ITestRunIds extends Serializable {

	public abstract List<String> getRunIds();

}