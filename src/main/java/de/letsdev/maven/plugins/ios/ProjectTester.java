/**
 * ios-maven-plugin
 * <p/>
 * User: fkoebel
 * Date: 2016-06-23
 * <p/>
 * This code is copyright (c) 2016 let's dev.
 * URL: https://www.letsdev.de
 * e-Mail: contact@letsdev.de
 */

package de.letsdev.maven.plugins.ios;

import org.apache.maven.project.MavenProject;

import java.io.*;
import java.util.Map;

import de.letsdev.maven.plugins.ios.mojo.IOSException;

public class ProjectTester {

    public static void test(final Map<String, String> properties,
                            MavenProject mavenProject) throws IOSException, IOException {

        String projectName = Utils.buildProjectName(properties, mavenProject);
        File workDirectory = Utils.getWorkDirectory(properties, mavenProject, projectName);

        if (Utils.shouldResetIphoneSimulators(properties)) {
            resetSimulators(workDirectory);
        }

        String scheme = properties.get(Utils.PLUGIN_PROPERTIES.XCTEST_SCHEME.toString());
        String configuration = properties.get(Utils.PLUGIN_PROPERTIES.XCTEST_CONFIGURATION.toString());
        String sdk = properties.get(Utils.PLUGIN_PROPERTIES.XCTEST_SDK.toString());
        String sdkArchs = Utils.getArchitecturesForSdk(properties, sdk);
        String destination = properties.get(Utils.PLUGIN_PROPERTIES.XCTEST_DESTINATION.toString());
        StringBuilder otherArguments = new StringBuilder();
        otherArguments.append(properties.get(Utils.PLUGIN_PROPERTIES.XCTEST_BUILD_ARGUMENTS.toString()));

        if (Utils.shouldUseWorkspaceFile(properties)) {
            otherArguments.append("-workspace");
            otherArguments.append(projectName).append(".xcworkspace");
        }

        if (properties.containsKey(Utils.PLUGIN_PROPERTIES.XCTEST_DERIVED_DATA_PATH.toString())) {
            otherArguments.append(" " + "-derivedDataPath ").append(properties.get(
                    Utils.PLUGIN_PROPERTIES.XCTEST_DERIVED_DATA_PATH.toString()));
        } else if (properties.containsKey(Utils.PLUGIN_PROPERTIES.DERIVED_DATA_PATH.toString())) {
            otherArguments.append(" " + "-derivedDataPath ").append(properties.get(Utils.PLUGIN_PROPERTIES.DERIVED_DATA_PATH.toString()));
        }

        String jsonOutputFile = Utils.createJsonOutputFilePath("test", properties);
        String xcPrettyCommand = Utils.getXcprettyCommand("testResults.txt", jsonOutputFile);

        final String scriptName = "run-xctests.sh";

        File tempFile = Utils.createTempFile(scriptName);

        ProcessBuilder processBuilder = new ProcessBuilder("/bin/bash", tempFile.getAbsoluteFile().toString(), scheme,
                configuration, sdk, sdkArchs, destination, otherArguments.toString(), xcPrettyCommand);

        processBuilder.directory(workDirectory);
        CommandHelper.performCommand(processBuilder);
    }

    private static void resetSimulators(File workDirectory) throws IOSException, IOException {

        final String scriptName = "reset-simulators.sh";

        File tempFile = Utils.createTempFile(scriptName);

        ProcessBuilder processBuilder = new ProcessBuilder("/bin/bash", tempFile.getAbsoluteFile().toString());
        processBuilder.directory(workDirectory);
        CommandHelper.performCommand(processBuilder);
    }
}
