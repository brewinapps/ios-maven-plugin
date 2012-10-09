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

/**
 * @author let's dev
 */
public class ProjectBuilder {

    /**
     * @param properties
     * @throws IOSException
     */
    public static void build(final Map<String, String> properties, MavenProject mavenProject) throws IOSException {

        // Make sure the source directory exists
        File workDir = new File(mavenProject.getBasedir().toString() + "/"
                + properties.get(Utils.PLUGIN_PROPERTIES.SOURCE_DIR.toString()) + "/"
                + mavenProject.getArtifactId());

        if (!workDir.exists()) {
            throw new IOSException("Invalid sourceDir specified: " + workDir.getAbsolutePath());
        }

        File targetDir = new File(mavenProject.getBuild().getDirectory());

        // Run agvtool to stamp marketing version
        ProcessBuilder processBuilder = new ProcessBuilder("agvtool", "new-marketing-version", mavenProject.getVersion());
        processBuilder.directory(workDir);
        CommandHelper.performCommand(processBuilder);

        // Run agvtool to stamp version
        processBuilder = new ProcessBuilder("agvtool", "new-version", "-all", mavenProject.getVersion());
        processBuilder.directory(workDir);
        CommandHelper.performCommand(processBuilder);

        // Run PlistPuddy to stamp build if a build id is specified
        if (properties.get(Utils.PLUGIN_PROPERTIES.BUILD_ID.toString()) != null) {
            String infoPlistFile = workDir + "/" + mavenProject.getArtifactId()
                    + "/" + mavenProject.getArtifactId() + "-Info.plist";

            // Run shell-script from resource-folder.
            try {
                File tempFile = File.createTempFile("write-buildnumber", "sh");

                InputStream inputStream = ProjectBuilder.class
                        .getResourceAsStream("/META-INF/write-buildnumber.sh");
                OutputStream outputStream = new FileOutputStream(tempFile);

                byte[] buffer = new byte[1024];
                int bytesRead;

                while ((bytesRead = inputStream.read(buffer)) != -1) {

                    outputStream.write(buffer, 0, bytesRead);
                }

                buffer = null;
                outputStream.close();

                processBuilder = new ProcessBuilder("sh", tempFile.getAbsoluteFile().toString(), infoPlistFile,properties.get(Utils.PLUGIN_PROPERTIES.BUILD_ID.toString()));

                processBuilder.directory(workDir);
                CommandHelper.performCommand(processBuilder);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Build the application
        List<String> buildParameters = new ArrayList<String>();
        buildParameters.add("xcodebuild");
        buildParameters.add("-sdk");
        buildParameters.add(properties.get(Utils.PLUGIN_PROPERTIES.SDK.toString()));
        buildParameters.add("-configuration");
        buildParameters.add(properties.get(Utils.PLUGIN_PROPERTIES.CONFIGURATION.toString()));
        buildParameters.add("SYMROOT=" + targetDir.getAbsolutePath());

        if(properties.containsKey(Utils.PLUGIN_PROPERTIES.CODE_SIGN_IDENTITY.toString())) {
            buildParameters.add("CODE_SIGN_IDENTITY=" + properties.get(Utils.PLUGIN_PROPERTIES.CODE_SIGN_IDENTITY.toString()));
        }

        if (properties.containsKey(Utils.PLUGIN_PROPERTIES.SCHEME.toString())) {
            buildParameters.add("-scheme");
            buildParameters.add(properties.get(Utils.PLUGIN_PROPERTIES.SCHEME.toString()));
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
            buildParameters.add(mavenProject.getArtifactId());
        }

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
        processBuilder.directory(workDir);
        CommandHelper.performCommand(processBuilder);

        // Zip Frameworks
        if (mavenProject.getPackaging().equals(Utils.PLUGIN_PACKAGING.IOS_FRAMEWORK.toString())) {

            File targetWorkDir = new File(targetDir.toString() + "/" + properties.get(Utils.PLUGIN_PROPERTIES.CONFIGURATION.toString()) + "-iphoneos/");

            processBuilder = new ProcessBuilder("zip", "-r", "../" + mavenProject.getArtifactId() + "." + Utils.PLUGIN_SUFFIX.FRAMEWORK_ZIP.toString(),mavenProject.getArtifactId() + ".framework");

            processBuilder.directory(targetWorkDir);
            CommandHelper.performCommand(processBuilder);

            // Generate IPA
        } else {

            processBuilder = new ProcessBuilder(
                    "xcrun",
                    "-sdk",
                    "iphoneos",
                    "PackageApplication",
                    "-v",
                    targetDir + "/" + properties.get(Utils.PLUGIN_PROPERTIES.CONFIGURATION.toString())
                            + "-iphoneos/" + properties.get(Utils.PLUGIN_PROPERTIES.APPNAME.toString()) + "." + Utils.PLUGIN_SUFFIX.APP,
                    "-o",
                    targetDir + "/" + properties.get(Utils.PLUGIN_PROPERTIES.CONFIGURATION.toString())
                            + "-iphoneos/" + properties.get(Utils.PLUGIN_PROPERTIES.APPNAME.toString()) + "." + Utils.PLUGIN_SUFFIX.IPA,
                    "--sign", properties.get(Utils.PLUGIN_PROPERTIES.CODE_SIGN_IDENTITY.toString()));

            processBuilder.directory(workDir);
            CommandHelper.performCommand(processBuilder);
        }

        //lock keychain
        if (properties.containsKey(Utils.PLUGIN_PROPERTIES.KEYCHAIN_PATH.toString()) && properties.containsKey(Utils.PLUGIN_PROPERTIES.KEYCHAIN_PASSWORD.toString())) {
            String command = "security lock-keychain " + properties.get(Utils.PLUGIN_PROPERTIES.KEYCHAIN_PATH.toString());
            processBuilder = new ProcessBuilder(CommandHelper.getCommand(command));
            CommandHelper.performCommand(processBuilder);
        }
    }
}
