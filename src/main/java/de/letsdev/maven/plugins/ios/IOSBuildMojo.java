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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

import java.util.HashMap;
import java.util.Map;

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
     * Path to keychain to sign with
     * @parameter
     * 		expression="${ios.keychainPath}"
     * @required
     */
    private String keychainPath;

    /**
     * Password to unlock keychain to sign with
     * @parameter
     * 		expression="${ios.keychainPassword}"
     * @required
     */
    private String keychainPassword;
	
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
			properties.put(Utils.PLUGIN_PROPERTIES.APPNAME.toString(), appName);
			properties.put(Utils.PLUGIN_PROPERTIES.CODE_SIGN_IDENTITY.toString(), codeSignIdentity);
			properties.put(Utils.PLUGIN_PROPERTIES.SDK.toString(), sdk);
			properties.put(Utils.PLUGIN_PROPERTIES.SOURCE_DIR.toString(), sourceDir);
			properties.put(Utils.PLUGIN_PROPERTIES.TARGET_DIR.toString(), targetDir);
			properties.put(Utils.PLUGIN_PROPERTIES.CONFIGURATION.toString(), configuration);
			properties.put(Utils.PLUGIN_PROPERTIES.BUILD_ID.toString(), buildId);
			properties.put(Utils.PLUGIN_PROPERTIES.SCHEME.toString(), scheme);
			properties.put(Utils.PLUGIN_PROPERTIES.TARGET.toString(), target);
            properties.put(Utils.PLUGIN_PROPERTIES.KEYCHAIN_PATH.toString(), keychainPath);
            properties.put(Utils.PLUGIN_PROPERTIES.KEYCHAIN_PASSWORD.toString(), keychainPassword);
			
			ProjectBuilder.build(properties, mavenProject);
		} catch (IOSException e) {
			throw new MojoExecutionException(e.getMessage());
		}
	}

}
