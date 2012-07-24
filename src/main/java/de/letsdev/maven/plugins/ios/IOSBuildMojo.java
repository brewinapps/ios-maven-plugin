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

import java.util.HashMap;
import java.util.Map;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;


/**
 * 
 * @author let's dev
 * @goal build
 * @phase compile
 */
public class IOSBuildMojo extends AbstractMojo {
	
	/**
	 * iOS Source Directory
	 * @parameter
	 * 		expression="${ios.sourceDir}"
	 * 		default-value="src/ios"
	 */
	private String sourceDir;
	
	/**
	 * iOS app name
	 * @parameter
	 * 		expression="${ios.appName}"
	 * @required
	 */
	private String appName;
	
	/**
	 * iOS scheme
	 * @parameter
	 * 		expression="${ios.scheme}"
	 */
	private String scheme;		
	
	/**
	 * iOS SDK
	 * @parameter
	 * 		expression="${ios.sdk}"
	 * 		default-value="iphoneos"
	 */
	private String sdk;
	
	/**
	 * iOS code sign identity
	 * @parameter
	 * 		expression="${ios.codeSignIdentity}"
	 * @required
	 */
	private String codeSignIdentity;
	
	/**
	 * iOS configuration
	 * @parameter
	 * 		expression="${ios.configuration}"
	 * 		default-value="Release"
	 */
	private String configuration;
	
	/**
	 * build id
	 * @parameter
	 * 		expression="${ios.buildId}"
	 */
	private String buildId;	
	
	/**
	 * target
	 * @parameter
	 * 		expression="${ios.target}"
	 */
	private String target;	
		
	/**
	* The maven project.
	* 
	* @parameter expression="${project}"
	* @required
	* @readonly
	*/
	protected MavenProject mavenProject;
	
	/**
	 * 
	 */
	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			final String targetDir = mavenProject.getBuild().getDirectory();
			
			Map<String, String> properties = new HashMap<String, String>();
			properties.put("appName", appName);
			properties.put("codeSignIdentity", codeSignIdentity);
			properties.put("sdk", sdk);
			properties.put("sourceDir", sourceDir);
			properties.put("targetDir", targetDir);
			properties.put("configuration", configuration);
			properties.put("buildId", buildId);
			properties.put("scheme", scheme);
			properties.put("target", target);
			
			ProjectBuilder.build(properties, mavenProject);
		} catch (IOSException e) {
			throw new MojoExecutionException(e.getMessage());
		}
	}

}
