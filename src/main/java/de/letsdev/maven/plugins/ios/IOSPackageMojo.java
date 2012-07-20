/**
 * Maven iOS Plugin
 * 
 * User: sbott
 * Date: 19.07.2012
 * Time: 19:54:44
 *
 * This code is copyright (c) 2012 let's dev.
 * URL: http://www.letsdev.de
 * e-Mail: contact@letsdev.de
 */

package de.letsdev.maven.plugins.ios;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;


/**
 * 
 * @author let's dev
 * @goal package
 * @phase package
 */
public class IOSPackageMojo extends AbstractMojo {
	
	/**
	 * iOS app name
	 * @parameter
	 * 		expression="${ios.appName}"
	 * @required
	 */
	private String appName;
	
	/**
	 * iOS configuration
	 * @parameter
	 * 		expression="${ios.configuration}"
	 * 		default-value="Release"
	 */
	private String configuration;
	
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
			final String targetDir = project.getBuild().getDirectory();			
			final String appDir = targetDir + "/" + configuration + "-iphoneos/";			
			final String artifactName = appName + ".zip";
			
			ProcessBuilder processBuilder = new ProcessBuilder(
					"zip",
					"-r", 
					artifactName, 
					appName + ".app.dSYM",
					appName + ".ipa");
			processBuilder.directory(new File(appDir));
			CommandHelper.performCommand(processBuilder);

			project.getArtifact().setFile(new File(appDir + "/" + artifactName));
		} catch (IOSException e) {
			throw new MojoExecutionException(e.getMessage());
		}
	}

}
