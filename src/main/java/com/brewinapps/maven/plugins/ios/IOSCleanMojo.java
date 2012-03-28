package com.brewinapps.maven.plugins.ios;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;


/**
 * 
 * @author Brewin' Apps AS
 * @goal clean
 */
public class IOSCleanMojo extends AbstractMojo {
	
	/**
	* The maven project.
	* 
	* @parameter expression="${project}"
	* @required
	* @readonly
	*/
	protected MavenProject project;
	
	/**
	 * 
	 */
	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			ProjectBuilder.clean(project.getBasedir());
		} catch (IOSException e) {
			throw new MojoExecutionException(e.getMessage());
		}
	}

}
