package org.jenkinsci.plugins.testinprogress;

import hudson.FilePath;
import hudson.Functions;
import hudson.model.TopLevelItem;
import hudson.model.FreeStyleProject;
import hudson.model.Node;
import hudson.tasks.Ant;
import hudson.tasks.Shell;

import java.io.File;
import java.util.Collections;
import java.util.List;

import hudson.tools.ToolProperty;
import hudson.util.jna.GNUCLibrary;
import jenkins.model.Jenkins;

import org.jvnet.hudson.test.ExtractResourceSCM;
import org.jvnet.hudson.test.JenkinsRule;

/**
 * 
 * @author Cedric Chabanois (cchabanois at gmail.com)
 * 
 */
public class JenkinsAntJobProjectBuilder {
	private final Jenkins jenkins;
	private final String name;
	private File resource;
	private Ant.AntInstallation antInstallation;
	private String targets = "";
	private Node node;
	private String properties = null;
	
	private JenkinsAntJobProjectBuilder(Jenkins jenkins, String name) {
		this.jenkins = jenkins;
		this.name = name;
	}

	public static JenkinsAntJobProjectBuilder aJenkinsAntJobProject(
			Jenkins jenkins, String name) {
		return new JenkinsAntJobProjectBuilder(jenkins, name);
	}

	public JenkinsAntJobProjectBuilder withProjectZipFile(File resource) {
		this.resource = resource;
		return this;
	}

	public JenkinsAntJobProjectBuilder withTargets(String targets) {
		this.targets = targets;
		return this;
	}
	
	public JenkinsAntJobProjectBuilder withAntInstallation(
			Ant.AntInstallation antInstallation) {
		this.antInstallation = antInstallation;
		return this;
	}

	public JenkinsAntJobProjectBuilder withAssignedNode(Node node) {
		this.node = node;
		return this;
	}

	public JenkinsAntJobProjectBuilder withProperties(String properties) {
		this.properties = properties;
		return this;
	}
	
	public JenkinsJob create() throws Exception {
		TopLevelItem item = jenkins.getItem(name);
		if (item != null) {
			item.delete();
		}
		FreeStyleProject job = jenkins.createProject(FreeStyleProject.class,
				name);
		Ant ant = new Ant(targets, antInstallation.getName(), null, "build.xml",
				properties);
		job.setScm(new ExtractResourceSCM(resource.toURI().toURL()));
		if (node != null) {
			job.setAssignedNode(node);
		}
		job.getBuildWrappersList().add(new TestInProgressBuildWrapper());
		job.getBuildersList().add(ant);
		job.save();
		return new JenkinsJob(job);
	}

	public static Ant.AntInstallation configureDefaultAnt(Jenkins jenkins,File antHome) throws Exception {
		final List<ToolProperty<?>> NO_PROPERTIES = Collections.EMPTY_LIST;

		Ant.AntInstallation antInstallation;
		if(System.getenv("ANT_HOME") != null) {
			antInstallation = new Ant.AntInstallation("default", System.getenv("ANT_HOME"), NO_PROPERTIES);
		} else {
			FilePath ant = jenkins.getRootPath().createTempFile("ant", "zip");
			ant.copyFrom(JenkinsRule.class.getClassLoader().getResource("apache-ant-1.8.1-bin.zip"));

			ant.unzip(new FilePath(antHome));
			if(!Functions.isWindows()) {
				GNUCLibrary.LIBC.chmod((new File(antHome, "apache-ant-1.8.1/bin/ant")).getPath(), 493);
			}

			antInstallation = new Ant.AntInstallation("default", (new File(antHome, "apache-ant-1.8.1")).getAbsolutePath(), NO_PROPERTIES);
		}

		((hudson.tasks.Ant.DescriptorImpl)jenkins.getDescriptorByType(hudson.tasks.Ant.DescriptorImpl.class)).setInstallations(new Ant.AntInstallation[]{antInstallation});
		return antInstallation;
	}
}
