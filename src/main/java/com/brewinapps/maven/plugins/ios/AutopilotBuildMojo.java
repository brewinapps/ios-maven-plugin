package com.brewinapps.maven.plugins.ios;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;


/**
 * 
 * @author Brewin' Apps AS
 * @goal build
 */
public class AutopilotBuildMojo extends AbstractMojo {
	
	/**
	 * iOS Source Directory
	 * @parameter
	 * 		expression="${ios.sourceDir}"
	 * 		default-value=""
	 */
	private String sourceDir;
	
	/**
	 * iOS Target Directory
	 * @parameter
	 * 		expression="${ios.targetDir}"
	 * 		default-value=""
	 */
	private String targetDir;	
	
	/**
	* The maven project.
	* 
	* @parameter expression="${project}"
	* @required
	* @readonly
	*/
	protected MavenProject project;
	
	/**
	 * Code sign identity
	 * @parameter
	 *		expression="${ios.codeSignIdentity}"
	 *		default-value=""
	 */
	protected String codeSignIdentity;
	
	/**
	 * 
	 */
	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			ProjectBuilder.build(project.getBasedir(), sourceDir, targetDir, codeSignIdentity);
		} catch (AutopilotException e) {
			throw new MojoExecutionException(e.getMessage());
		}
	}

}
