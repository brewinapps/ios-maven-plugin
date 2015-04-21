package de.letsdev.maven.plugins.ios;

import org.apache.maven.project.MavenProject;

import java.util.Map;

/**
 * Maven iOS Plugin
 *
 * User: cwack
 * Date: 09.10.2012
 * Time: 19:54:44
 *
 * This code is copyright (c) 2012 let's dev.
 * URL: http://www.letsdev.de
 * e-Mail: contact@letsdev.de
 */

public class Utils {

    public static String BUNDLE_VERSION_SNAPSHOT_ID = "-SNAPSHOT";

    public static String SDK_IPHONE_OS = "iphoneos";
    public static String SDK_IPHONE_SIMULATOR = "iphonesimulator";

    public static String ARCHITECTURES_IPHONE_OS = "arm64 armv7 armv7s";
    public static String ARCHITECTURES_IPHONE_SIMULATOR = "i386 x86_64";

    public enum PLUGIN_PROPERTIES {

        APP_DIR("appDir"),
        APP_NAME("appName"),
        APP_ICON_NAME("appIconName"),
        ASSETS_DIRECTORY("assetsDirectory"),
        BUILD_ID("buildId"),
        BUNDLE_IDENTIFIER("bundleIdentifier"),
        DISPLAY_NAME("displayName"),
        DEPLOY_IPA_PATH("deployIpaPath"),
        DEPLOY_ICON_PATH("deployIconPath"),
        CLASSIFIER("classifier"),
        IOS_FRAMEWORK_BUILD("iOSFrameworkBuild"),
        CODE_SIGNING_ENABLED("codeSigningEnabled"),
        CODE_SIGN_IDENTITY("codeSignIdentity"),
        CONFIGURATION("configuration"),
        HOCKEY_APP_TOKEN("hockeyAppToken"),
        INFO_PLIST("infoPlist"),
        IPA_VERSION("ipaVersion"),
        KEYCHAIN_PASSWORD("keychainPassword"),
        KEYCHAIN_PATH("keychainPath"),
        RELEASE_NOTES("releaseNotes"),
        SCHEME("scheme"),
        SDK("sdk"),
        SOURCE_DIRECTORY("sourceDir"),
        PROJECT_NAME("projectName"),
        PROVISIONING_PROFILE_UUID("provisioningProfileUUID"),
        TARGET("target"),
        TARGET_DIR("targetDir");

        private PLUGIN_PROPERTIES(String name) {
            this.name = name;
        }

        private final String name;

        public String toString() {
            return name;
        }
    }

    public enum PLUGIN_SUFFIX {
        APP("app"),
        IPA("ipa"),
        APP_DSYM("app.dSYM"),
        FRAMEWORK("framework"),
        FRAMEWORK_ZIP("framework.zip"),
        PLIST("plist");

        private PLUGIN_SUFFIX(String name) {
            this.name = name;
        }

        private final String name;

        public String toString() {
            return name;
        }
    }

    public enum PLUGIN_PACKAGING {
        IPA("ipa"),
        IOS_FRAMEWORK("ios-framework");

        private PLUGIN_PACKAGING(String name) {
            this.name = name;
        }

        private final String name;

        public String toString() {
            return name;
        }
    }

    public static boolean isiOSFramework(MavenProject mavenProject, Map<String, String> properties) {
        return isiOSFramework(mavenProject, properties.get(PLUGIN_PROPERTIES.IOS_FRAMEWORK_BUILD.toString()).equals("true"));
    }

    public static boolean isiOSFramework(MavenProject mavenProject, boolean isFrameworkBuild) {
        return mavenProject.getPackaging().equals(Utils.PLUGIN_PACKAGING.IOS_FRAMEWORK.toString()) || isFrameworkBuild;
    }

    public static boolean shouldCodeSign(MavenProject mavenProject, Map<String, String> properties) {
        return !isiOSFramework(mavenProject, properties);
    }
}
