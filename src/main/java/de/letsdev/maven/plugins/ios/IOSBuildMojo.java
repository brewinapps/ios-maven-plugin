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
     * iOS project name
     * @parameter
     * 		expression="${ios.projectName}"
     */
    private String projectName;

    /**
     * iOS provisioning profile  UUID
     *
     * If not set the default provisioning file in xcode project will be used.
     * @parameter
     * 		expression="${ios.provisioningProfileUUID}"
     */
    private String provisioningProfileUUID;

	
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
	 */
	private String codeSignIdentity;

    /**
     * Path to keychain to sign with
     * @parameter
     * 		expression="${ios.keychainPath}"
     */
    private String keychainPath;

    /**
     * Password to unlock keychain to sign with
     * @parameter
     * 		expression="${ios.keychainPassword}"
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
     * infoPlist
     * @parameter
     * 		expression="${ios.infoPlist}"
     */
    private String infoPlist;

    /**
     * ipaVersion
     * @parameter
     * 		expression="${ios.ipaVersion}"
     */
    private String ipaVersion;

    /**
     * assetsDirectory
     * @parameter
     */
    private String assetsDirectory;

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
			final String targetDir = this.mavenProject.getBuild().getDirectory();
			
			Map<String, String> properties = new HashMap<String, String>();
			this.addProperty(properties, Utils.PLUGIN_PROPERTIES.APP_NAME.toString(), this.appName);
			this.addProperty(properties, Utils.PLUGIN_PROPERTIES.PROJECT_NAME.toString(), this.projectName);
            this.addProperty(properties, Utils.PLUGIN_PROPERTIES.CODE_SIGN_IDENTITY.toString(), this.codeSignIdentity);
            this.addProperty(properties, Utils.PLUGIN_PROPERTIES.SDK.toString(), this.sdk);
            this.addProperty(properties, Utils.PLUGIN_PROPERTIES.SOURCE_DIRECTORY.toString(), this.sourceDir);
            this.addProperty(properties, Utils.PLUGIN_PROPERTIES.TARGET_DIR.toString(), targetDir);
            this.addProperty(properties, Utils.PLUGIN_PROPERTIES.CONFIGURATION.toString(), this.configuration);
            this.addProperty(properties, Utils.PLUGIN_PROPERTIES.BUILD_ID.toString(), this.buildId);
            this.addProperty(properties, Utils.PLUGIN_PROPERTIES.SCHEME.toString(), this.scheme);
            this.addProperty(properties, Utils.PLUGIN_PROPERTIES.TARGET.toString(), this.target);
            this.addProperty(properties, Utils.PLUGIN_PROPERTIES.KEYCHAIN_PATH.toString(), this.keychainPath);
            this.addProperty(properties, Utils.PLUGIN_PROPERTIES.KEYCHAIN_PASSWORD.toString(), this.keychainPassword);
            this.addProperty(properties, Utils.PLUGIN_PROPERTIES.INFO_PLIST.toString(), this.infoPlist);
            this.addProperty(properties, Utils.PLUGIN_PROPERTIES.IPA_VERSION.toString(), this.ipaVersion);
            this.addProperty(properties, Utils.PLUGIN_PROPERTIES.ASSETS_DIRECTORY.toString(), this.assetsDirectory);
            this.addProperty(properties, Utils.PLUGIN_PROPERTIES.PROVISIONING_PROFILE_UUID.toString(), this.provisioningProfileUUID);

			ProjectBuilder.build(properties, mavenProject);
		} catch (IOSException e) {
			throw new MojoExecutionException(e.getMessage());
		}
	}

    private void addProperty(Map<String, String> properties, String key, String value) {
        if(properties != null && key != null && value != null) {
            properties.put(key, value);
        }
    }

}
