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

import org.apache.maven.project.MavenProject;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author let's dev
 */
public class ProjectBuilder {

    /**
     * @param properties Properties
     * @throws IOSException
     */
    public static void build(final Map<String, String> properties, MavenProject mavenProject) throws IOSException {

        // Make sure the source directory exists
        String projectName = mavenProject.getArtifactId();
        if (properties.get(Utils.PLUGIN_PROPERTIES.PROJECT_NAME.toString()) != null) {
            projectName = properties.get(Utils.PLUGIN_PROPERTIES.PROJECT_NAME.toString());
        }

        File workDirectory = new File(mavenProject.getBasedir().toString() + File.separator
                + properties.get(Utils.PLUGIN_PROPERTIES.SOURCE_DIRECTORY.toString()) + File.separator
                + projectName);

        if (!workDirectory.exists()) {
            throw new IOSException("Invalid sourceDirectory specified: " + workDirectory.getAbsolutePath());
        }

        File projectDirectory = new File(workDirectory.toString() + File.separator + projectName);
        File assetsDirectory = null;
        File assetsTempDirectory = null;
        File newAssetsDirectory = null;

        //Rename assets directory
        if (properties.get(Utils.PLUGIN_PROPERTIES.ASSETS_DIRECTORY.toString()) != null) {
            assetsDirectory = new File(projectDirectory.toString() + File.separator + "assets");
            assetsTempDirectory = new File(projectDirectory.toString() + File.separator + "assets.tmp");
            newAssetsDirectory = new File(projectDirectory.toString() + File.separator + properties.get(Utils.PLUGIN_PROPERTIES.ASSETS_DIRECTORY.toString()));

            if (assetsDirectory.exists() && !(newAssetsDirectory.toString().equalsIgnoreCase(assetsDirectory.toString()))){
                ProcessBuilder processBuilder = new ProcessBuilder("mv", assetsDirectory.toString(), assetsTempDirectory.toString());
                processBuilder.directory(projectDirectory);
                CommandHelper.performCommand(processBuilder);
            }

            if (newAssetsDirectory.exists() && !(newAssetsDirectory.toString().equalsIgnoreCase(assetsDirectory.toString()))) {
                ProcessBuilder processBuilder = new ProcessBuilder("mv", newAssetsDirectory.toString(), assetsDirectory.toString());
                processBuilder.directory(projectDirectory);
                CommandHelper.performCommand(processBuilder);
            }
        }

        File targetDirectory = new File(mavenProject.getBuild().getDirectory());

        // Run agvtool to stamp marketing version
        String projectVersion = mavenProject.getVersion();

        if (properties.get(Utils.PLUGIN_PROPERTIES.IPA_VERSION.toString()) != null) {
            projectVersion = properties.get(Utils.PLUGIN_PROPERTIES.IPA_VERSION.toString());
        }
        //remove -SNAPSHOT in version number in order to prevent malformed version numbers in framework builds
        if (Utils.isiOSFramework(mavenProject, properties)) {
            projectVersion = projectVersion.replace(Utils.BUNDLE_VERSION_SNAPSHOT_ID, "");
        }

        ProcessBuilder processBuilder = new ProcessBuilder("agvtool", "new-marketing-version", projectVersion);
        processBuilder.directory(workDirectory);
        CommandHelper.performCommand(processBuilder);

        // Run agvtool to stamp version
        processBuilder = new ProcessBuilder("agvtool", "new-version", "-all", projectVersion);
        processBuilder.directory(workDirectory);
        CommandHelper.performCommand(processBuilder);

        // Run PlistBuddy to stamp build if a build id is specified
        if (properties.get(Utils.PLUGIN_PROPERTIES.BUILD_ID.toString()) != null) {
            executePlistScript("write-buildnumber.sh",  properties.get(Utils.PLUGIN_PROPERTIES.BUILD_ID.toString()), workDirectory, projectName, properties, processBuilder);
        }

        // Run PlistBuddy to app icon name if a build id is specified
        if (properties.get(Utils.PLUGIN_PROPERTIES.APP_ICON_NAME.toString()) != null) {
            executePlistScript("write-app-icon-name.sh",  properties.get(Utils.PLUGIN_PROPERTIES.APP_ICON_NAME.toString()), workDirectory, projectName, properties, processBuilder);
        }

        // Run PlistBuddy to overwrite the bundle identifier in info plist
        if (properties.get(Utils.PLUGIN_PROPERTIES.BUNDLE_IDENTIFIER.toString()) != null) {
            executePlistScript("write-bundleidentifier.sh",  properties.get(Utils.PLUGIN_PROPERTIES.BUNDLE_IDENTIFIER.toString()), workDirectory, projectName, properties, processBuilder);
        }

        // Run PlistBuddy to overwrite the display name in info plist
        if (properties.get(Utils.PLUGIN_PROPERTIES.DISPLAY_NAME.toString()) != null) {
            executePlistScript("write-displayname.sh",  properties.get(Utils.PLUGIN_PROPERTIES.DISPLAY_NAME.toString()), workDirectory, projectName, properties, processBuilder);
        }

        File precompiledHeadersDir = new File(targetDirectory, "precomp-dir-" + UUID.randomUUID().toString());
        if(!precompiledHeadersDir.mkdir()){
           System.err.println("Could not create precompiled headers dir at path = " + precompiledHeadersDir.getAbsolutePath());
        }

        //BEG clean the application
        List<String> cleanParameters = new ArrayList<String>();
        cleanParameters.add("xcodebuild");
        cleanParameters.add("-alltargets");
        cleanParameters.add("-configuration");
        cleanParameters.add(properties.get(Utils.PLUGIN_PROPERTIES.CONFIGURATION.toString()));
        cleanParameters.add("clean");
        processBuilder = new ProcessBuilder(cleanParameters);
        processBuilder.directory(workDirectory);
        CommandHelper.performCommand(processBuilder);
        //END clean the application

        //unlock keychain
        if (Utils.shouldCodeSign(mavenProject, properties) && properties.containsKey(Utils.PLUGIN_PROPERTIES.KEYCHAIN_PATH.toString()) && properties.containsKey(Utils.PLUGIN_PROPERTIES.KEYCHAIN_PASSWORD.toString())) {
            List<String> keychainParameters = new ArrayList<String>();
            keychainParameters.add("security");
            keychainParameters.add("unlock-keychain");
            keychainParameters.add("-p");
            keychainParameters.add(properties.get(Utils.PLUGIN_PROPERTIES.KEYCHAIN_PASSWORD.toString()));
            keychainParameters.add(properties.get(Utils.PLUGIN_PROPERTIES.KEYCHAIN_PATH.toString()));

            processBuilder = new ProcessBuilder(keychainParameters);
            CommandHelper.performCommand(processBuilder);
        }

        // Build the application
        List<String> buildParameters = generateBuildParameters(mavenProject, properties, targetDirectory, projectName, precompiledHeadersDir, false);
        processBuilder = new ProcessBuilder(buildParameters);
        processBuilder.directory(workDirectory);
        CommandHelper.performCommand(processBuilder);

        if (Utils.isiOSFramework(mavenProject, properties)) {
            //generate framework product also for iphonesimulator sdk
            buildParameters = generateBuildParameters(mavenProject, properties, targetDirectory, projectName, precompiledHeadersDir, true);
            processBuilder = new ProcessBuilder(buildParameters);
            processBuilder.directory(workDirectory);
            CommandHelper.performCommand(processBuilder);
        }

        if (Utils.isiOSFramework(mavenProject, properties)) {
            String appName = properties.get(Utils.PLUGIN_PROPERTIES.APP_NAME.toString());
            String frameworkName = appName + ".framework";
            File targetWorkDirectoryIphone = new File(targetDirectory.toString() + File.separator + properties.get(Utils.PLUGIN_PROPERTIES.CONFIGURATION.toString()) + "-" + Utils.SDK_IPHONE_OS + File.separator);
            File targetWorkDirectoryIphoneSimulator = new File(targetDirectory.toString() + File.separator + properties.get(Utils.PLUGIN_PROPERTIES.CONFIGURATION.toString()) + "-" + Utils.SDK_IPHONE_SIMULATOR + File.separator);

            // use lipo to merge framework binarys
            mergeFrameworkProducts(targetWorkDirectoryIphone, targetWorkDirectoryIphoneSimulator, appName, frameworkName);

            // Zip Frameworks
            processBuilder = new ProcessBuilder("zip", "-r", "../" + properties.get(Utils.PLUGIN_PROPERTIES.APP_NAME.toString()) + "." + Utils.PLUGIN_SUFFIX.FRAMEWORK_ZIP.toString(), frameworkName);

            processBuilder.directory(targetWorkDirectoryIphone);
            CommandHelper.performCommand(processBuilder);
        }
        // Generate IPA
        else {
            if (properties.get(Utils.PLUGIN_PROPERTIES.BUILD_ID.toString()) != null) {
                projectVersion += "-b" + properties.get(Utils.PLUGIN_PROPERTIES.BUILD_ID.toString());
            }

            File appTargetPath = new File(targetDirectory + File.separator + properties.get(Utils.PLUGIN_PROPERTIES.CONFIGURATION.toString())
                    + "-" + Utils.SDK_IPHONE_OS + "/" + properties.get(Utils.PLUGIN_PROPERTIES.TARGET.toString()) + "." + Utils.PLUGIN_SUFFIX.APP);

            File newAppTargetPath = new File(targetDirectory + File.separator + properties.get(Utils.PLUGIN_PROPERTIES.CONFIGURATION.toString())
                    + "-" + Utils.SDK_IPHONE_OS + "/" + properties.get(Utils.PLUGIN_PROPERTIES.APP_NAME.toString()) + "." + Utils.PLUGIN_SUFFIX.APP);

            File ipaTargetPath = new File(targetDirectory + File.separator + properties.get(Utils.PLUGIN_PROPERTIES.CONFIGURATION.toString())
                    + "-" + Utils.SDK_IPHONE_OS + "/" + properties.get(Utils.PLUGIN_PROPERTIES.APP_NAME.toString()) + "-" + projectVersion + "." + Utils.PLUGIN_SUFFIX.IPA);

            File dsymTargetPath = new File(targetDirectory + File.separator + properties.get(Utils.PLUGIN_PROPERTIES.CONFIGURATION.toString())
                    + "-" + Utils.SDK_IPHONE_OS + "/" + properties.get(Utils.PLUGIN_PROPERTIES.TARGET.toString()) + "." + Utils.PLUGIN_SUFFIX.APP_DSYM);

            File newDsymTargetPath = new File(targetDirectory + File.separator + properties.get(Utils.PLUGIN_PROPERTIES.CONFIGURATION.toString())
                    + "-" + Utils.SDK_IPHONE_OS + "/" + properties.get(Utils.PLUGIN_PROPERTIES.APP_NAME.toString()) + "." + Utils.PLUGIN_SUFFIX.APP_DSYM);

            if (appTargetPath.exists() && !(appTargetPath.toString().equalsIgnoreCase(newAppTargetPath.toString()))) {
                processBuilder = new ProcessBuilder("mv", appTargetPath.toString(), newAppTargetPath.toString());
                processBuilder.directory(projectDirectory);
                CommandHelper.performCommand(processBuilder);
            }

            if (dsymTargetPath.exists() && !(dsymTargetPath.toString().equalsIgnoreCase(newDsymTargetPath.toString()))) {
                processBuilder = new ProcessBuilder("mv", dsymTargetPath.toString(), newDsymTargetPath.toString());
                processBuilder.directory(projectDirectory);
                CommandHelper.performCommand(processBuilder);
            }

            File ipaTmpDir = new File(targetDirectory, "ipa-temp-dir-" + UUID.randomUUID().toString());
            if(!ipaTmpDir.mkdir()){
                System.err.println("Could not create ipa temp dir at path = " + ipaTmpDir.getAbsolutePath());
            }

            processBuilder = new ProcessBuilder(
                    "xcrun",
                    "--no-cache",  //disbale caching
                    "-sdk",
                    properties.get(Utils.PLUGIN_PROPERTIES.SDK.toString()),
                    "PackageApplication",
                    "-v",
                    newAppTargetPath.toString(),
                    "-o",
                    ipaTargetPath.toString(),
                    "--sign", properties.get(Utils.PLUGIN_PROPERTIES.CODE_SIGN_IDENTITY.toString())
                    );

            processBuilder.directory(workDirectory);
            processBuilder.environment().put("TMPDIR", ipaTmpDir.getAbsolutePath());  //this is really important to avoid collisions, if not set /var/folders will be used here
            CommandHelper.performCommand(processBuilder);
        }

        //lock keychain
        if (properties.containsKey(Utils.PLUGIN_PROPERTIES.KEYCHAIN_PATH.toString()) && properties.containsKey(Utils.PLUGIN_PROPERTIES.KEYCHAIN_PASSWORD.toString())) {
            String command = "security lock-keychain " + properties.get(Utils.PLUGIN_PROPERTIES.KEYCHAIN_PATH.toString());
            processBuilder = new ProcessBuilder(CommandHelper.getCommand(command));
            CommandHelper.performCommand(processBuilder);
        }

        //Rename assets directory to origin
        if (properties.get(Utils.PLUGIN_PROPERTIES.ASSETS_DIRECTORY.toString()) != null) {
            if ((assetsTempDirectory != null) && (assetsDirectory.exists())) {
                processBuilder = new ProcessBuilder("mv", assetsDirectory.toString(), newAssetsDirectory.toString());
                processBuilder.directory(projectDirectory);
                CommandHelper.performCommand(processBuilder);
            }

            if ((assetsTempDirectory != null) && (assetsTempDirectory.exists())) {
                    processBuilder = new ProcessBuilder("mv", assetsTempDirectory.toString(), assetsDirectory.toString());
                    processBuilder.directory(projectDirectory);
                    CommandHelper.performCommand(processBuilder);
            }
        }

        // Generate the the deploy plist file
        if ((properties.get(Utils.PLUGIN_PROPERTIES.DEPLOY_IPA_PATH.toString()) != null) && (properties.get(Utils.PLUGIN_PROPERTIES.DEPLOY_ICON_PATH.toString()) != null)) {
            final String deployPlistName = properties.get(Utils.PLUGIN_PROPERTIES.APP_NAME.toString()) + "-" + projectVersion + "." + Utils.PLUGIN_SUFFIX.PLIST;
            writeDeployPlistFile(targetDirectory, projectName, deployPlistName, properties, processBuilder);

        }
    }

    private static List<String> generateBuildParameters(MavenProject mavenProject, Map<String, String> properties, File targetDirectory, String projectName, File precompiledHeadersDir, boolean shouldUseIphoneSimulatorSDK) {
        List<String> buildParameters = new ArrayList<String>();
        buildParameters.add("xcodebuild");
        buildParameters.add("-sdk");

        if (shouldUseIphoneSimulatorSDK) {
            buildParameters.add(Utils.SDK_IPHONE_SIMULATOR);
            buildParameters.add("ARCHS=" + Utils.ARCHITECTURES_IPHONE_SIMULATOR);
            buildParameters.add("VALID_ARCHS=" + Utils.ARCHITECTURES_IPHONE_SIMULATOR);
        }
        else {
            buildParameters.add(properties.get(Utils.PLUGIN_PROPERTIES.SDK.toString()));
        }

        buildParameters.add("-configuration");
        buildParameters.add(properties.get(Utils.PLUGIN_PROPERTIES.CONFIGURATION.toString()));
        buildParameters.add("SYMROOT=" + targetDirectory.getAbsolutePath());

        //if product should be code signed, we add flags for code signing
        if (Utils.shouldCodeSign(mavenProject, properties)) {
            buildParameters.add("CODE_SIGN_RESOURCE_RULES_PATH=$(SDKROOT)/ResourceRules.plist"); //since xcode 6.1 is necessary, if not set, app is not able to be signed with a key.
            if (properties.containsKey(Utils.PLUGIN_PROPERTIES.CODE_SIGN_IDENTITY.toString())) {
                buildParameters.add("CODE_SIGN_IDENTITY=" + properties.get(Utils.PLUGIN_PROPERTIES.CODE_SIGN_IDENTITY.toString()));
            }
        }
        else {
            //otherwise we skip code signing
            buildParameters.add("CODE_SIGN_IDENTITY=");
            buildParameters.add("CODE_SIGNING_REQUIRED=NO");
        }

        if (properties.containsKey(Utils.PLUGIN_PROPERTIES.SCHEME.toString())) {
            buildParameters.add("-scheme");
            buildParameters.add(properties.get(Utils.PLUGIN_PROPERTIES.SCHEME.toString()));
        }

        if (Utils.shouldCodeSign(mavenProject, properties) && properties.containsKey(Utils.PLUGIN_PROPERTIES.PROVISIONING_PROFILE_UUID.toString())) {
            buildParameters.add("PROVISIONING_PROFILE=" + properties.get(Utils.PLUGIN_PROPERTIES.PROVISIONING_PROFILE_UUID.toString()));
        }

        if (properties.containsKey(Utils.PLUGIN_PROPERTIES.APP_NAME.toString())) {
            buildParameters.add("PRODUCT_NAME=" + properties.get(Utils.PLUGIN_PROPERTIES.APP_NAME.toString()));
        }

        // Add target. Uses target 'framework' to build Frameworks.
        buildParameters.add("-target");

        if (properties.containsKey(Utils.PLUGIN_PROPERTIES.TARGET.toString()) || (Utils.isiOSFramework(mavenProject, properties))) {
            buildParameters.add(properties.get(Utils.PLUGIN_PROPERTIES.TARGET.toString()));

        } else {
            buildParameters.add(projectName);
        }

        buildParameters.add("SHARED_PRECOMPS_DIR=" + precompiledHeadersDir.getAbsolutePath());   //this is really important to avoid collisions, if not set /var/folders will be used here
        if (Utils.shouldCodeSign(mavenProject, properties) && properties.containsKey(Utils.PLUGIN_PROPERTIES.KEYCHAIN_PATH.toString())) {
            buildParameters.add("OTHER_CODE_SIGN_FLAGS=--keychain " + properties.get(Utils.PLUGIN_PROPERTIES.KEYCHAIN_PATH.toString()));
        }

        return buildParameters;
    }

    private static void mergeFrameworkProducts(File targetWorkDirectoryIphone, File targetWorkDirectoryIphoneSimulator, String appName, String frameworkName) {
        // Run shell-script from resource-folder.
        try {
            final String scriptName = "merge-framework-products";

            final String iphoneosFrameworkProductPath = targetWorkDirectoryIphone.toString() + "/" + frameworkName + "/" + appName;
            final String iphoneSimulatorFrameworkProductPath = targetWorkDirectoryIphoneSimulator.toString() + "/" + frameworkName + "/" + appName;
            final String mergedFrameworkPath = targetWorkDirectoryIphone.toString() + "/" + frameworkName + "/" + appName;

            File tempFile = File.createTempFile(scriptName, "sh");
            InputStream inputStream = ProjectBuilder.class.getResourceAsStream("/META-INF/" + scriptName + ".sh");
            OutputStream outputStream = new FileOutputStream(tempFile);

            byte[] buffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.close();

            ProcessBuilder processBuilder = new ProcessBuilder("sh", tempFile.getAbsoluteFile().toString(),
                    iphoneosFrameworkProductPath,
                    iphoneSimulatorFrameworkProductPath,
                    mergedFrameworkPath);

            processBuilder.directory(targetWorkDirectoryIphone);
            CommandHelper.performCommand(processBuilder);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void executePlistScript(String scriptName, String value, File workDirectory, String projectName, final Map<String, String> properties, ProcessBuilder processBuilder) throws IOSException {
        String infoPlistFile = workDirectory + File.separator + projectName + File.separator + projectName + "-Info.plist";

        if (properties.get(Utils.PLUGIN_PROPERTIES.INFO_PLIST.toString()) != null) {
            infoPlistFile = workDirectory + File.separator + properties.get(Utils.PLUGIN_PROPERTIES.INFO_PLIST.toString());
        }

        // Run shell-script from resource-folder.
        try {
            File tempFile = File.createTempFile(scriptName, "sh");

            InputStream inputStream = ProjectBuilder.class
                    .getResourceAsStream("/META-INF/" + scriptName);
            OutputStream outputStream = new FileOutputStream(tempFile);

            byte[] buffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {

                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.close();

            processBuilder = new ProcessBuilder("sh", tempFile.getAbsoluteFile().toString(), infoPlistFile, value);

            processBuilder.directory(workDirectory);
            CommandHelper.performCommand(processBuilder);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeDeployPlistFile(File targetDirectory, String projectName, String deployPlistName, final Map<String, String> properties, ProcessBuilder processBuilder) throws IOSException {
        // Run shell-script from resource-folder.
        try {
            final String scriptName = "write-deploy-plist";

            final String ipaLocation = properties.get(Utils.PLUGIN_PROPERTIES.DEPLOY_IPA_PATH.toString());
            final String iconLocation = properties.get(Utils.PLUGIN_PROPERTIES.DEPLOY_ICON_PATH.toString());
            final String displayName = properties.get(Utils.PLUGIN_PROPERTIES.DISPLAY_NAME.toString());
            final String bundleIdentifier = properties.get(Utils.PLUGIN_PROPERTIES.BUNDLE_IDENTIFIER.toString());
            final String bundleVersion = properties.get(Utils.PLUGIN_PROPERTIES.IPA_VERSION.toString());

            File tempFile = File.createTempFile(scriptName, "sh");

            InputStream inputStream = ProjectBuilder.class.getResourceAsStream("/META-INF/" + scriptName);
            OutputStream outputStream = new FileOutputStream(tempFile);

            byte[] buffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.close();

            processBuilder = new ProcessBuilder("sh", tempFile.getAbsoluteFile().toString(),
                    deployPlistName,
                    ipaLocation,
                    iconLocation,
                    displayName,
                    bundleIdentifier,
                    bundleVersion);

            processBuilder.directory(targetDirectory);
            CommandHelper.performCommand(processBuilder);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
