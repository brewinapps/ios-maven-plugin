package com.brewinapps.ios;

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
 * @phase compile
 */
public class IOSBuildMojo extends AbstractMojo {
	
	/**
	 * iOS Source Directory
	 * @parameter
	 * 		expression="${ios.sourceDir}"
	 * 		default-value="."
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
	 * iOS version 
	 * @parameter
	 * 		expression="${ios.version}"
	 */
	private String version;
	
	/**
	 * build id
	 * @parameter
	 * 		expression="${ios.buildId}" 
	 * 		default-value="${project.version}"
	 */
	private String buildId;	
		
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
			
			Map<String, String> properties = new HashMap<String, String>();
			properties.put("appName", appName);
			properties.put("codeSignIdentity", codeSignIdentity);
			properties.put("sdk", sdk);
			properties.put("baseDir", project.getBasedir().toString());
			properties.put("sourceDir", sourceDir);
			properties.put("targetDir", targetDir);
			properties.put("configuration", configuration);
			properties.put("buildId", buildId);
			properties.put("version", version);
			properties.put("scheme", scheme);
			
			ProjectBuilder.build(properties);
		} catch (IOSException e) {
			throw new MojoExecutionException(e.getMessage());
		}
	}

}
