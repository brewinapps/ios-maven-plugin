package com.brewinapps.maven.plugins.ios;

import java.util.HashMap;
import java.util.Map;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;


/**
 * 
 * @author Brewin' Apps AS
 * @goal build
 */
public class IOSBuildMojo extends AbstractMojo {
	
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
	 * 		default-value="target"
	 */
	private String targetDir;
	
	/**
	 * iOS app name
	 * @parameter
	 * 		expression="${ios.appName}"
	 * @required
	 */
	private String appName;
	
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
	 * build number
	 * @parameter
	 * 		expression="${ios.buildNumber}"
	 */
	private String buildNumber;	
		
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
			Map<String, String> properties = new HashMap<String, String>();
			properties.put("appName", appName);
			properties.put("codeSignIdentity", codeSignIdentity);
			properties.put("sdk", sdk);
			properties.put("baseDir", project.getBasedir().toString());
			properties.put("sourceDir", sourceDir);
			properties.put("targetDir", targetDir);
			properties.put("configuration", configuration);
			properties.put("buildNumber", buildNumber);
			properties.put("version", project.getVersion());
			
			ProjectBuilder.build(properties);
		} catch (IOSException e) {
			throw new MojoExecutionException(e.getMessage());
		}
	}

}
