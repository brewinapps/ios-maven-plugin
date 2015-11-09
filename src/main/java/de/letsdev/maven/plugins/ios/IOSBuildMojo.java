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
	 * 		property="ios.sourceDir"
	 * 		default-value="src/ios"
	 */
	private String sourceDir;
	
	/**
	 * iOS app name
	 * @parameter property="ios.appName"
     * @required
	 */
    private String appName;

    /**
     * classifier
     * @parameter
     * 		property="ios.classifier"
     */
    private String classifier;

    /**
     * iOS project name
     * @parameter
     * 		property="ios.projectName"
     */
    private String projectName;

    /**
     * iOS provisioning profile  UUID
     *
     * If not set the default provisioning file in xcode project will be used.
     * @parameter
     * 		property="ios.provisioningProfileUUID"
     */
    private String provisioningProfileUUID;

    /**
     * iOS bundle identifier
     *
     * If not set the bundle identifier in the info.plist will be used
     * @parameter
     * 		property="ios.bundleIdentifier"
     */
    private String bundleIdentifier;

    /**
     * iOS display name
     *
     * If not set the display name in the info.plist will be used
     * @parameter
     * 		property="ios.displayName"
     */
    private String displayName;

	
	/**
	 * iOS scheme
	 * @parameter
	 * 		property="ios.scheme"
	 */
	private String scheme;		
	
	/**
	 * iOS SDK
	 * @parameter
	 * 		property="ios.sdk"
	 * 		default-value="iphoneos"
	 */
	private String sdk;

    /**
     * iphoneos SDK build architectures
     * @parameter
     * 		property="ios.iphoneosArchitectures"
     * 		default-value="arm64 armv7 armv7s"
     */
    private String iphoneosArchitectures;

    /**
     * iphonesimulator SDK build architectures
     * @parameter
     * 		property="ios.iphonesimulatorArchitectures"
     * 		default-value="i386 x86_64"
     */
    private String iphonesimulatorArchitectures;

    /**
     * flag for iOS framework builds
     * @parameter
     * 		property="ios.iOSFrameworkBuild"
     * 		default-value="false"
     */
    private boolean iOSFrameworkBuild;

    /**
     * flag for macosx framework builds
     * @parameter
     * 		property="ios.macOSFrameworkBuild"
     * 		default-value="false"
     */
    private boolean macOSFrameworkBuild;

    /**
     * flag for iOS code signing enabled
     * @parameter property="ios.codeSigningEnabled"
     */
    private boolean codeSigningEnabled;

    /**
     * flag for iOS code signing with resources rules enabled
     *
     * Following will be added to code sign execution:
     *
     * <pre>CODE_SIGN_RESOURCE_RULES_PATH=$(SDKROOT)/ResourceRules.plist</pre>
     *
     * This was necessary from iOS SDK 6.1 until 8.0
     *
     * Default: false
     *
     * @parameter property="ios.codeSigningWithResourceRulesEnabled"
     */
    private boolean codeSigningWithResourceRulesEnabled = false;
	
	/**
	 * iOS code sign identity
	 * @parameter
	 * 		property="ios.codeSignIdentity"
	 */
	private String codeSignIdentity;

    /**
     * Path to keychain to sign with
     * @parameter
     * 		property="ios.keychainPath"
     */
    private String keychainPath;

    /**
     * Password to unlock keychain to sign with
     * @parameter
     * 		property="ios.keychainPassword"
     */
    private String keychainPassword;
	
	/**
	 * iOS configuration
	 * @parameter
	 * 		property="ios.configuration"
	 * 		default-value="Release"
	 */
	private String configuration;
	
	/**
	 * build id
	 * @parameter
	 * 		property="ios.buildId"
	 */
	private String buildId;	
	
	/**
	 * target
	 * @parameter
	 * 		property="ios.target"
	 */
	private String target;

    /**
     * infoPlist
     * @parameter
     * 		property="ios.infoPlist"
     */
    private String infoPlist;

    /**
     * app icon name
     * @parameter
     * 		property="ios.appIconName"
     */
    private String appIconName;

    /**
     * gccPreprocessorDefinitions, added to xcodebuild command
     * @parameter
     * 		property="ios.gccPreprocessorDefinitions"
     */
    private String gccPreprocessorDefinitions;

    /**
     * ipaVersion
     * @parameter
     * 		property="ios.ipaVersion"
     */
    private String ipaVersion;

    /**
     * assetsDirectory, directory for assets (existing one will be renamed to assets-tmp)
     * @parameter
     */
    private String assetsDirectory;

    /**
     * appIcons, directory for appIcons (existing one will be renamed to appIcons-tmp)
     * @parameter
     */
    private String appIconsDirectory;

    /**
	* The maven project.
	*
	* @parameter property="project"
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
			this.addProperty(properties, Utils.PLUGIN_PROPERTIES.APP_ICON_NAME.toString(), this.appIconName);
			this.addProperty(properties, Utils.PLUGIN_PROPERTIES.PROJECT_NAME.toString(), this.projectName);
			this.addProperty(properties, Utils.PLUGIN_PROPERTIES.GCC_PREPROCESSOR_DEFINITIONS.toString(), this.gccPreprocessorDefinitions);
			this.addProperty(properties, Utils.PLUGIN_PROPERTIES.IPHONESIMULATOR_ARCHITECTURES.toString(), this.iphonesimulatorArchitectures);
			this.addProperty(properties, Utils.PLUGIN_PROPERTIES.IPHONEOS_ARCHITECTURES.toString(), this.iphoneosArchitectures);
            this.addProperty(properties, Utils.PLUGIN_PROPERTIES.IOS_FRAMEWORK_BUILD.toString(), Boolean.toString(this.iOSFrameworkBuild));
            this.addProperty(properties, Utils.PLUGIN_PROPERTIES.MACOSX_FRAMEWORK_BUILD.toString(), Boolean.toString(this.macOSFrameworkBuild));
            this.addProperty(properties, Utils.PLUGIN_PROPERTIES.CODE_SIGNING_ENABLED.toString(), Boolean.toString(this.codeSigningEnabled));
            this.addProperty(properties, Utils.PLUGIN_PROPERTIES.CODE_SIGN_WITH_RESOURCE_RULES_ENABLED.toString(), Boolean.toString(this.codeSigningEnabled));
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
            this.addProperty(properties, Utils.PLUGIN_PROPERTIES.APP_ICONS_DIRECTORY.toString(), this.appIconsDirectory);
            this.addProperty(properties, Utils.PLUGIN_PROPERTIES.PROVISIONING_PROFILE_UUID.toString(), this.provisioningProfileUUID);
            this.addProperty(properties, Utils.PLUGIN_PROPERTIES.BUNDLE_IDENTIFIER.toString(), this.bundleIdentifier);
            this.addProperty(properties, Utils.PLUGIN_PROPERTIES.DISPLAY_NAME.toString(), this.displayName);
            this.addProperty(properties, Utils.PLUGIN_PROPERTIES.CLASSIFIER.toString(), this.classifier);

			ProjectBuilder.build(properties, this.mavenProject);
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
