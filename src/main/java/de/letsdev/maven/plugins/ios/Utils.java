package de.letsdev.maven.plugins.ios;

import org.apache.commons.io.FilenameUtils;
import org.apache.maven.project.MavenProject;

import java.io.*;
import java.util.Map;

import de.letsdev.maven.plugins.ios.mojo.IOSException;
import de.letsdev.maven.plugins.ios.mojo.container.XcodeArchiveProductType;
import de.letsdev.maven.plugins.ios.mojo.container.XcodeExportOptions;

/**
 * Maven iOS Plugin
 * <p/>
 * User: cwack
 * Date: 09.10.2012
 * Time: 19:54:44
 * <p/>
 * This code is copyright (c) 2012 let's dev.
 * URL: http://www.letsdev.de
 * e-Mail: contact@letsdev.de
 */

public class Utils {

    public static String BUNDLE_VERSION_SNAPSHOT_ID = "-SNAPSHOT";

    public static String SDK_IPHONE_OS = "iphoneos";
    public static String SDK_IPHONE_SIMULATOR = "iphonesimulator";

    public static String ARCHITECTURES_IPHONE_OS = "arm64 armv7";
    public static String ARCHITECTURES_IPHONE_SIMULATOR = "i386 x86_64";

    public static String RELEASE_TASK = "releaseTask";
    public static String RELEASE_TASK_TESTFLIGHT = "Testflight";
    public static String RELEASE_TASK_APP_STORE_UPLOAD = "AppStoreUpload";

    public static String EXPORT_PRODUCT_PATH_BASE = "Products/Library";
    public static String EXPORT_PRODUCT_PATH_FRAMEWORK = EXPORT_PRODUCT_PATH_BASE + "/Frameworks";
    public static String EXPORT_PRODUCT_PATH_BUNDLE = EXPORT_PRODUCT_PATH_BASE + "/Bundles";

    public static String EXPORT_PRODUCT_TYPE_EXTENSION_FRAMEWORK = "framework";
    public static String EXPORT_PRODUCT_TYPE_EXTENSION_BUNDLE = "bundle";

    public enum PLUGIN_PROPERTIES {

        APP_DIR("appDir"),
        APP_NAME("appName"),
        APP_ICON_NAME("appIconName"),
        BUILD_ID("buildId"),
        BUNDLE_IDENTIFIER("bundleIdentifier"),
        DISPLAY_NAME("displayName"),
        DEPLOY_IPA_PATH("deployIpaPath"),
        DEPLOY_ICON_PATH("deployIconPath"),
        RELEASE_TASK("releaseTask"),
        CLASSIFIER("classifier"),
        IOS_FRAMEWORK_BUILD("iOSFrameworkBuild"),
        MACOSX_FRAMEWORK_BUILD("macOSFrameworkBuild"),
        IPHONEOS_ARCHITECTURES("iphoneosArchitectures"),
        IPHONESIMULATOR_ARCHITECTURES("iphonesimulatorArchitectures"),
        IPHONESIMULATOR_BITCODE_ENABLED("iphonesimulatorBitcodeEnabled"),
        CODE_SIGNING_ENABLED("codeSigningEnabled"),
        CODE_SIGN_WITH_RESOURCE_RULES_ENABLED("codeSignWithResourceRulesEnabled"),
        CODE_SIGN_IDENTITY("codeSignIdentity"),
        CODE_SIGN_ENTITLEMENTS("codeSignEntitlements"),
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
        GCC_PREPROCESSOR_DEFINITIONS("gccPreprocessorDefinitions"),
        PROVISIONING_PROFILE_UUID("provisioningProfileUUID"),
        PROVISIONING_PROFILE_SPECIFIER("provisioningProfileSpecifier"),
        DEVELOPMENT_TEAM("developmentTeam"),
        TARGET("target"),
        TARGET_DIR("targetDir"),
        BUILD_TO_XCARCHIVE_ENABLED("build-xcarchive"),
        COCOA_PODS_ENABLED("cocoa-pods-enabled"),
        CARTHAGE_ENABLED("carthage-enabled"),
        ITUNES_CONNECT_USERNAME("iTunesConnectUsername"),
        ITUNES_CONNECT_PASSWORD("iTunesConnectPassword"),
        XCODE_VERSION("xcodeVersion"),
        XCTEST_SCHEME("xcTestsScheme"),
        XCTEST_CONFIGURATION("xcTestsConfiguration"),
        XCTEST_DESTINATION("xcTestsDestination"),
        XCTEST_SDK("xcTestsSdk"),
        XCTEST_BUILD_ARGUMENTS("xcTestsBuildArguments"),
        RESET_SIMULATORS("resetSimulators"),
        DERIVED_DATA_PATH("derivedDataPath"),
        XCTEST_DERIVED_DATA_PATH("xcTestsDerivedDataPath"),
        PROVISIONING_PROFILE_NAME("provisioningProfileName");

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
        XCARCHIVE("xcarchive"),
        IPA("ipa"),
        APP_DSYM("app.dSYM"),
        FRAMEWORK("framework"),
        FRAMEWORK_ZIP("zip"),
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
        IOS_FRAMEWORK("ios-framework"),
        FRAMEWORK_ZIP("zip");

        private PLUGIN_PACKAGING(String name) {

            this.name = name;
        }

        private final String name;

        public String toString() {

            return name;
        }
    }

    public static boolean isMacOSFramework(Map<String, String> properties) {

        return "true".equals(properties.get(PLUGIN_PROPERTIES.MACOSX_FRAMEWORK_BUILD.toString()));
    }

    public static boolean isiOSFramework(MavenProject mavenProject, Map<String, String> properties) {

        return isiOSFramework(mavenProject,
                "true".equals(properties.get(PLUGIN_PROPERTIES.IOS_FRAMEWORK_BUILD.toString())));
    }

    public static boolean isiOSFramework(MavenProject mavenProject, boolean isFrameworkBuild) {

        return mavenProject.getPackaging().equals(Utils.PLUGIN_PACKAGING.IOS_FRAMEWORK.toString()) || isFrameworkBuild;
    }

    public static boolean shouldCodeSign(MavenProject mavenProject, Map<String, String> properties) {

        return !isiOSFramework(mavenProject, properties) && !isMacOSFramework(properties);
    }

    public static boolean shouldCodeSignWithResourceRules(MavenProject mavenProject, Map<String, String> properties) {

        return "true".equals(properties.get(PLUGIN_PROPERTIES.CODE_SIGN_WITH_RESOURCE_RULES_ENABLED.toString()));
    }

    public static boolean shouldBuildXCArchive(MavenProject mavenProject, Map<String, String> properties) {

        return "true".equals(properties.get(PLUGIN_PROPERTIES.BUILD_TO_XCARCHIVE_ENABLED.toString()));
    }

    public static boolean shouldBuildXCArchiveWithExportOptionsPlist(XcodeExportOptions xcodeExportOptions) {

        return xcodeExportOptions.method != null;
    }

    public static String getArchiveName(final String projectName, MavenProject mavenProject) {

        return getTargetDirectory(mavenProject).getAbsolutePath() + File.separator + projectName + "."
                + PLUGIN_SUFFIX.XCARCHIVE;
    }

    public static String getProjectIpaName(final String projectName) {

        return projectName + "." + PLUGIN_SUFFIX.IPA;
    }

    public static String getIpaName(final String schemeName) {

        return schemeName + "." + PLUGIN_SUFFIX.IPA;
    }

    protected static File getTargetDirectory(MavenProject mavenProject) {

        return new File(mavenProject.getBuild().getDirectory());
    }

    public static String buildProjectName(Map<String, String> buildProperties, MavenProject mavenProject) {

        String projectName = mavenProject.getArtifactId();
        if (buildProperties.get(Utils.PLUGIN_PROPERTIES.PROJECT_NAME.toString()) != null) {
            projectName = buildProperties.get(Utils.PLUGIN_PROPERTIES.PROJECT_NAME.toString());
        }
        return projectName;
    }

    public static boolean cocoaPodsEnabled(Map<String, String> buildProperties) {

        return "true".equals(buildProperties.get(PLUGIN_PROPERTIES.COCOA_PODS_ENABLED.toString()));
    }

    public static boolean carthageEnebled(Map<String, String> buildProperties) {

        return "true".equals(buildProperties.get(PLUGIN_PROPERTIES.CARTHAGE_ENABLED.toString()));
    }

    public static boolean isIphoneSimulatorBitcodeEnabled(Map<String, String> buildProperties) {

        return "true".equals(buildProperties.get(PLUGIN_PROPERTIES.IPHONESIMULATOR_BITCODE_ENABLED.toString()));
    }

    public static boolean shouldResetIphoneSimulators(Map<String, String> buildProperties) {

        return "true".equals(buildProperties.get(PLUGIN_PROPERTIES.RESET_SIMULATORS.toString()));
    }

    public static File getWorkDirectory(Map<String, String> buildProperties, MavenProject mavenProject,
                                        String projectName) throws IOSException {

        File workDirectory = new File(mavenProject.getBasedir().toString() + File.separator + buildProperties.get(
                Utils.PLUGIN_PROPERTIES.SOURCE_DIRECTORY.toString()) + File.separator + projectName);

        if (!workDirectory.exists()) {
            throw new IOSException("Invalid sourceDirectory specified: " + workDirectory.getAbsolutePath());
        }
        return workDirectory;
    }

    public static String getProjectVersion(MavenProject mavenProject, Map<String, String> properties) {

        String projectVersion = mavenProject.getVersion();

        if (properties.get(Utils.PLUGIN_PROPERTIES.IPA_VERSION.toString()) != null) {
            projectVersion = properties.get(Utils.PLUGIN_PROPERTIES.IPA_VERSION.toString());
        }

        return projectVersion;
    }

    public static boolean isTestflightBuild(Map<String, String> buildProperties) {

        String valueReleaseTask = buildProperties.get(Utils.RELEASE_TASK);
        boolean result = Utils.RELEASE_TASK_TESTFLIGHT.equalsIgnoreCase(valueReleaseTask);
        return result;
    }

    public static boolean isAppStoreBuild(Map<String, String> buildProperties) {

        String valueReleaseTask = buildProperties.get(Utils.RELEASE_TASK);
        boolean result = Utils.RELEASE_TASK_APP_STORE_UPLOAD.equalsIgnoreCase(valueReleaseTask);
        return result;
    }

    public static String getAdjustedVersion(MavenProject mavenProject, Map<String, String> properties) {

        String result = getProjectVersion(mavenProject, properties);

        //remove -SNAPSHOT in version number in order to prevent malformed version numbers in framework builds
        if (Utils.isiOSFramework(mavenProject, properties) || Utils.isMacOSFramework(properties)
                || Utils.isTestflightBuild(properties) || Utils.isAppStoreBuild(properties)) {
            result = result.replace(Utils.BUNDLE_VERSION_SNAPSHOT_ID, "");
        }

        return result;
    }

    public static String getAppName(MavenProject mavenProject, Map<String, String> properties) {

        String result = properties.get(Utils.PLUGIN_PROPERTIES.APP_NAME.toString());
        return result;
    }

    public static String getConfiguration(MavenProject mavenProject, Map<String, String> properties) {

        String result = properties.get(PLUGIN_PROPERTIES.CONFIGURATION.toString());
        return result;
    }

    public static String getClassifier(MavenProject mavenProject, Map<String, String> properties) {

        String result = properties.get(PLUGIN_PROPERTIES.CLASSIFIER.toString());
        return result;
    }

    public static String getBuildId(MavenProject mavenProject, Map<String, String> properties) {

        String result = properties.get(PLUGIN_PROPERTIES.BUILD_ID.toString());
        return result;
    }

    public static String getCurrentXcodeVersion(File workDirectory) {

        String xcodeVersion = "";

        // Run shell-script from resource-folder.
        try {
            final String scriptName = "get-xcode-version.sh";
            File tempFile = File.createTempFile(scriptName, "sh");

            InputStream inputStream = ProjectBuilder.class.getResourceAsStream("/META-INF/" + scriptName);
            OutputStream outputStream = new FileOutputStream(tempFile);

            byte[] buffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.close();

            ProcessBuilder processBuilder = new ProcessBuilder("sh", tempFile.getAbsoluteFile().toString());

            processBuilder.directory(workDirectory);
            xcodeVersion = CommandHelper.performCommand(processBuilder);
            System.out.println("############################################################################");
            System.out.println("################################ " + xcodeVersion
                    + " is current xcode version ############################################");
            System.out.println("############################################################################");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IOSException e) {
            e.printStackTrace();
        }

        return xcodeVersion;
    }

    public static String getArchitecturesForSdk(Map<String, String> properties, String sdk) {

        String architectures = properties.get(PLUGIN_PROPERTIES.IPHONEOS_ARCHITECTURES.toString());

        if (SDK_IPHONE_SIMULATOR.equals(sdk)) {
            architectures = properties.get(Utils.PLUGIN_PROPERTIES.IPHONESIMULATOR_ARCHITECTURES.toString());
        }

        return architectures;
    }

    public static String getExportProductPath(XcodeArchiveProductType productType) {

        String productPath;

        switch (productType) {
            case XCODE_ARCHIVE_PRODUCT_TYPE_BUNDLE:
                productPath = Utils.EXPORT_PRODUCT_PATH_BUNDLE;
                break;
            case XCODE_ARCHIVE_PRODUCT_TYPE_FRAMEWORK:
                productPath = Utils.EXPORT_PRODUCT_PATH_FRAMEWORK;
                break;
            default:
                productPath = "";
                break;
        }

        return productPath;
    }

    public static XcodeArchiveProductType getExportProductType(String productName) {

        XcodeArchiveProductType type = XcodeArchiveProductType.XCODE_ARCHIVE_PRODUCT_TYPE_UNKNOWN;

        if (EXPORT_PRODUCT_TYPE_EXTENSION_FRAMEWORK.equals(FilenameUtils.getExtension(productName))) {
            type = XcodeArchiveProductType.XCODE_ARCHIVE_PRODUCT_TYPE_FRAMEWORK;
        } else if (EXPORT_PRODUCT_TYPE_EXTENSION_BUNDLE.equals(FilenameUtils.getExtension(productName))) {
            type = XcodeArchiveProductType.XCODE_ARCHIVE_PRODUCT_TYPE_BUNDLE;
        }

        return type;
    }

    public static boolean shouldUseWorkspaceFile(Map<String, String> properties) {

        if (properties.containsKey(PLUGIN_PROPERTIES.COCOA_PODS_ENABLED.toString())) {
            return properties.get(PLUGIN_PROPERTIES.COCOA_PODS_ENABLED.toString()).equals("true");
        }

        return false;
    }

    public static String getXcprettyCommand(String logFileName, String jsonOutputFileIdentifier) {
        String xcprettyOutputFile = jsonOutputFileIdentifier + "-result.json";
        String xcprettyCompilationDatabaseFile = jsonOutputFileIdentifier + "-compile-commands.json";

        String xcPrettyStatement = "";
        if (jsonOutputFileIdentifier != null && !jsonOutputFileIdentifier.isEmpty()) {
            xcPrettyStatement += "| XCPRETTY_JSON_FILE_OUTPUT=" + xcprettyOutputFile;
        } else {
            xcPrettyStatement += " | tee " + logFileName + " |";
        }
        
        xcPrettyStatement += " xcpretty";

        if (jsonOutputFileIdentifier != null && !jsonOutputFileIdentifier.isEmpty()) {
            xcPrettyStatement += " -f `xcpretty-json-formatter` -r json-compilation-database -o " + xcprettyCompilationDatabaseFile;
        }

        xcPrettyStatement += " && exit ${PIPESTATUS[0]}";
        return xcPrettyStatement;
    }

    public static void executeShellScript(String scriptName, String value1, String value2, String value3,
                                          File workDirectory) throws IOSException {

        // Run shell-script from resource-folder.
        try {
            File tempFile = File.createTempFile(scriptName, "sh");

            InputStream inputStream = ProjectBuilder.class.getResourceAsStream("/META-INF/" + scriptName);
            OutputStream outputStream = new FileOutputStream(tempFile);

            byte[] buffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {

                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.close();

            if (value1 == null) {
                value1 = "";
            }

            if (value2 == null) {
                value2 = "";
            }

            if (value3 == null) {
                value3 = "";
            }

            ProcessBuilder processBuilder = new ProcessBuilder("sh", tempFile.getAbsoluteFile().toString(), value1,
                    value2, value3);

            processBuilder.directory(workDirectory);
            CommandHelper.performCommand(processBuilder);
        } catch (IOException e) {
            e.printStackTrace();
            throw new IOSException(e);
        }
    }

    public static String createJsonOutputFilePath(String relativeFilePath, Map<String, String> properties) {

        String jsonOutputFile = "";
        if (properties.containsKey(Utils.PLUGIN_PROPERTIES.DERIVED_DATA_PATH.toString())) {
            jsonOutputFile += properties.get(Utils.PLUGIN_PROPERTIES.DERIVED_DATA_PATH.toString()) + "/";
        }
        jsonOutputFile += "build/reports/";
        jsonOutputFile += relativeFilePath;
        return jsonOutputFile;
    }
}
