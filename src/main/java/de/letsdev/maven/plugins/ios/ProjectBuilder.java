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
     * @param properties Propertiew
     * @throws IOSException
     */
    public static void build(final Map<String, String> properties, MavenProject mavenProject) throws IOSException {

        // Make sure the source directory exists
        String projectName = mavenProject.getArtifactId();
        if (properties.get(Utils.PLUGIN_PROPERTIES.PROJECT_NAME.toString()) != null) {
            projectName = properties.get(Utils.PLUGIN_PROPERTIES.PROJECT_NAME.toString());
        }

        File workDirectory = new File(mavenProject.getBasedir().toString() + "/"
                + properties.get(Utils.PLUGIN_PROPERTIES.SOURCE_DIRECTORY.toString()) + "/"
                + projectName);

        if (!workDirectory.exists()) {
            throw new IOSException("Invalid sourceDirectory specified: " + workDirectory.getAbsolutePath());
        }

        File projectDirectory = new File(workDirectory.toString() + "/" + projectName);
        File assetsDirectory = null;
        File assetsTempDirectory = null;
        File newAssetsDirectory = null;

        //Rename assets directory
        if (properties.get(Utils.PLUGIN_PROPERTIES.ASSETS_DIRECTORY.toString()) != null) {
            assetsDirectory = new File(projectDirectory.toString() + "/assets");
            assetsTempDirectory = new File(projectDirectory.toString() + "/assets.tmp");
            newAssetsDirectory = new File(projectDirectory.toString() + "/" + properties.get(Utils.PLUGIN_PROPERTIES.ASSETS_DIRECTORY.toString()));

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

        ProcessBuilder processBuilder = new ProcessBuilder("agvtool", "new-marketing-version", projectVersion);
        processBuilder.directory(workDirectory);
        CommandHelper.performCommand(processBuilder);

        // Run agvtool to stamp version
        processBuilder = new ProcessBuilder("agvtool", "new-version", "-all", projectVersion);
        processBuilder.directory(workDirectory);
        CommandHelper.performCommand(processBuilder);

        // Run PlistPuddy to stamp build if a build id is specified
        if (properties.get(Utils.PLUGIN_PROPERTIES.BUILD_ID.toString()) != null) {
            executePlistScript("write-buildnumber.sh",  properties.get(Utils.PLUGIN_PROPERTIES.BUILD_ID.toString()), workDirectory, projectName, properties, processBuilder);
        }

        // Run PlistPuddy to app icon name if a build id is specified
        if (properties.get(Utils.PLUGIN_PROPERTIES.APP_ICON_NAME.toString()) != null) {
            executePlistScript("write-app-icon-name.sh",  properties.get(Utils.PLUGIN_PROPERTIES.APP_ICON_NAME.toString()), workDirectory, projectName, properties, processBuilder);
        }

        // Run PlistPuddy to overwrite the bundle identifier in info plist
        if (properties.get(Utils.PLUGIN_PROPERTIES.BUNDLE_IDENTIFIER.toString()) != null) {
            executePlistScript("write-bundleidentifier.sh",  properties.get(Utils.PLUGIN_PROPERTIES.BUNDLE_IDENTIFIER.toString()), workDirectory, projectName, properties, processBuilder);
        }

        // Run PlistPuddy to overwrite the display name in info plist
        if (properties.get(Utils.PLUGIN_PROPERTIES.DISPLAY_NAME.toString()) != null) {
            executePlistScript("write-displayname.sh",  properties.get(Utils.PLUGIN_PROPERTIES.DISPLAY_NAME.toString()), workDirectory, projectName, properties, processBuilder);
        }

        File precompiledHeadersDir = new File(targetDirectory, "precomp-dir-" + UUID.randomUUID().toString());
        if(!precompiledHeadersDir.mkdir()){
           System.err.println("Could not create precompiled headers dir at path = " + precompiledHeadersDir.getAbsolutePath());
        }

        // Build the application
        List<String> buildParameters = new ArrayList<String>();
        buildParameters.add("xcodebuild");
        buildParameters.add("-sdk");
        buildParameters.add(properties.get(Utils.PLUGIN_PROPERTIES.SDK.toString()));
        buildParameters.add("-configuration");
        buildParameters.add(properties.get(Utils.PLUGIN_PROPERTIES.CONFIGURATION.toString()));
        buildParameters.add("SYMROOT=" + targetDirectory.getAbsolutePath());

        if (properties.containsKey(Utils.PLUGIN_PROPERTIES.CODE_SIGN_IDENTITY.toString())) {
            buildParameters.add("CODE_SIGN_IDENTITY=" + properties.get(Utils.PLUGIN_PROPERTIES.CODE_SIGN_IDENTITY.toString()));
        }

        if (properties.containsKey(Utils.PLUGIN_PROPERTIES.SCHEME.toString())) {
            buildParameters.add("-scheme");
            buildParameters.add(properties.get(Utils.PLUGIN_PROPERTIES.SCHEME.toString()));
        }

        if (properties.containsKey(Utils.PLUGIN_PROPERTIES.PROVISIONING_PROFILE_UUID.toString())) {
            buildParameters.add("PROVISIONING_PROFILE=" + properties.get(Utils.PLUGIN_PROPERTIES.PROVISIONING_PROFILE_UUID.toString()));
        }

        if (properties.containsKey(Utils.PLUGIN_PROPERTIES.APP_NAME.toString())) {
            buildParameters.add("PRODUCT_NAME=" + properties.get(Utils.PLUGIN_PROPERTIES.APP_NAME.toString()));
        }

        // Add target. Uses target 'framework' to build Frameworks.
        buildParameters.add("-target");

        if (properties.containsKey(Utils.PLUGIN_PROPERTIES.TARGET.toString()) || (mavenProject.getPackaging().equals(Utils.PLUGIN_PACKAGING.IOS_FRAMEWORK.toString()))) {

            if (mavenProject.getPackaging().equals(Utils.PLUGIN_PACKAGING.IOS_FRAMEWORK.toString())) {
                buildParameters.add(Utils.PLUGIN_SUFFIX.FRAMEWORK.toString());

            } else {
                buildParameters.add(properties.get(Utils.PLUGIN_PROPERTIES.TARGET.toString()));
            }

        } else {
            buildParameters.add(projectName);
        }

        buildParameters.add("SHARED_PRECOMPS_DIR=" + precompiledHeadersDir.getAbsolutePath());
        buildParameters.add("clean build");
//        buildParameters.add("CACHE_ROOT");

        if (properties.containsKey(Utils.PLUGIN_PROPERTIES.KEYCHAIN_PATH.toString())) {
            buildParameters.add("OTHER_CODE_SIGN_FLAGS=--keychain " + properties.get(Utils.PLUGIN_PROPERTIES.KEYCHAIN_PATH.toString()));
        }

        //unlock keychain
        if (properties.containsKey(Utils.PLUGIN_PROPERTIES.KEYCHAIN_PATH.toString()) && properties.containsKey(Utils.PLUGIN_PROPERTIES.KEYCHAIN_PASSWORD.toString())) {

            List<String> keychainParameters = new ArrayList<String>();
            keychainParameters.add("security");
            keychainParameters.add("unlock-keychain");
            keychainParameters.add("-p");
            keychainParameters.add(properties.get(Utils.PLUGIN_PROPERTIES.KEYCHAIN_PASSWORD.toString()));
            keychainParameters.add(properties.get(Utils.PLUGIN_PROPERTIES.KEYCHAIN_PATH.toString()));

            processBuilder = new ProcessBuilder(keychainParameters);
            CommandHelper.performCommand(processBuilder);
        }

        processBuilder = new ProcessBuilder(buildParameters);
        processBuilder.directory(workDirectory);
        CommandHelper.performCommand(processBuilder);

        // Zip Frameworks
        if (mavenProject.getPackaging().equals(Utils.PLUGIN_PACKAGING.IOS_FRAMEWORK.toString())) {

            File targetWorkDirectory = new File(targetDirectory.toString() + "/" + properties.get(Utils.PLUGIN_PROPERTIES.CONFIGURATION.toString()) + "-iphoneos/");

            processBuilder = new ProcessBuilder("zip", "-r", "../" + projectName + "." + Utils.PLUGIN_SUFFIX.FRAMEWORK_ZIP.toString(), projectName + ".framework");

            processBuilder.directory(targetWorkDirectory);
            CommandHelper.performCommand(processBuilder);

            // Generate IPA
        } else {
            if (properties.get(Utils.PLUGIN_PROPERTIES.BUILD_ID.toString()) != null) {
                projectVersion += "_(" + properties.get(Utils.PLUGIN_PROPERTIES.BUILD_ID.toString()) + ")";
            }

            File appTargetPath = new File(targetDirectory + "/" + properties.get(Utils.PLUGIN_PROPERTIES.CONFIGURATION.toString())
                    + "-iphoneos/" + properties.get(Utils.PLUGIN_PROPERTIES.TARGET.toString()) + "." + Utils.PLUGIN_SUFFIX.APP);

            File newAppTargetPath = new File(targetDirectory + "/" + properties.get(Utils.PLUGIN_PROPERTIES.CONFIGURATION.toString())
                    + "-iphoneos/" + properties.get(Utils.PLUGIN_PROPERTIES.APP_NAME.toString()) + "." + Utils.PLUGIN_SUFFIX.APP);

            File ipaTargetPath = new File(targetDirectory + "/" + properties.get(Utils.PLUGIN_PROPERTIES.CONFIGURATION.toString())
                    + "-iphoneos/" + properties.get(Utils.PLUGIN_PROPERTIES.APP_NAME.toString()) + "_" + projectVersion + "." + Utils.PLUGIN_SUFFIX.IPA);

            File dsymTargetPath = new File(targetDirectory + "/" + properties.get(Utils.PLUGIN_PROPERTIES.CONFIGURATION.toString())
                    + "-iphoneos/" + properties.get(Utils.PLUGIN_PROPERTIES.TARGET.toString()) + "." + Utils.PLUGIN_SUFFIX.APP_DSYM);

            File newDsymTargetPath = new File(targetDirectory + "/" + properties.get(Utils.PLUGIN_PROPERTIES.CONFIGURATION.toString())
                    + "-iphoneos/" + properties.get(Utils.PLUGIN_PROPERTIES.APP_NAME.toString()) + "." + Utils.PLUGIN_SUFFIX.APP_DSYM);

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
                    "-sdk",
                    properties.get(Utils.PLUGIN_PROPERTIES.SDK.toString()),
                    "PackageApplication",
                    "-v",
                    newAppTargetPath.toString(),
                    "-o",
                    ipaTargetPath.toString(),
                    "--sign", properties.get(Utils.PLUGIN_PROPERTIES.CODE_SIGN_IDENTITY.toString()),
                    "TMPDIR=" + ipaTmpDir.getAbsolutePath()
                    );

            processBuilder.directory(workDirectory);
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

            if (assetsDirectory.exists()) {
                processBuilder = new ProcessBuilder("mv", assetsDirectory.toString(), newAssetsDirectory.toString());
                processBuilder.directory(projectDirectory);
                CommandHelper.performCommand(processBuilder);
            }

            if (assetsTempDirectory.exists()) {
                processBuilder = new ProcessBuilder("mv", assetsTempDirectory.toString(), assetsDirectory.toString());
                processBuilder.directory(projectDirectory);
                CommandHelper.performCommand(processBuilder);
            }
        }
    }

    private static void executePlistScript(String scriptName, String value, File workDirectory, String projectName, final Map<String, String> properties, ProcessBuilder processBuilder) throws IOSException {
        String infoPlistFile = workDirectory + "/" + projectName
                + "/" + projectName + "-Info.plist";

        if (properties.get(Utils.PLUGIN_PROPERTIES.INFO_PLIST.toString()) != null) {
            infoPlistFile = workDirectory + "/" + properties.get(Utils.PLUGIN_PROPERTIES.INFO_PLIST.toString());

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
}
