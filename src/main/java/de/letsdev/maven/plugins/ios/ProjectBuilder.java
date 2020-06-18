/**
 * Maven iOS Plugin
 * <p/>
 * User: sbott
 * Date: 19.07.2012
 * Time: 19:54:44
 * <p/>
 * This code is copyright (c) 2012 let's dev.
 * URL: https://www.letsdev.de
 * e-Mail: contact@letsdev.de
 */

package de.letsdev.maven.plugins.ios;

import com.google.gson.Gson;

import de.letsdev.maven.plugins.ios.mojo.container.InfoPlistValue;
import org.apache.maven.project.MavenProject;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import de.letsdev.maven.plugins.ios.mojo.IOSException;
import de.letsdev.maven.plugins.ios.mojo.container.FileReplacement;
import de.letsdev.maven.plugins.ios.mojo.container.StringReplacement;
import de.letsdev.maven.plugins.ios.mojo.container.StringReplacementConfig;
import de.letsdev.maven.plugins.ios.mojo.container.XcodeArchiveProductType;
import de.letsdev.maven.plugins.ios.mojo.container.XcodeExportOptions;

/**
 * @author let's dev
 */
public class ProjectBuilder {

    /**
     * @param properties Properties
     * @throws IOSException
     */
    public static void build(final Map<String, String> properties, MavenProject mavenProject,
                             final List<FileReplacement> fileReplacements, final List<InfoPlistValue> infoPlistValues,
                             final List<String> xcodeBuildParameters, final XcodeExportOptions xcodeExportOptions,
                             final StringReplacementConfig stringReplacements,
                             List<String> targetDependencies) throws IOSException, IOException {
        // Make sure the source directory exists
        String projectName = Utils.buildProjectName(properties, mavenProject);
        String schemeName = properties.get(Utils.PLUGIN_PROPERTIES.SCHEME.toString());
        File projectDirectory = Utils.getWorkDirectory(properties, mavenProject, projectName);

        try {
            //replace all configured files
            if (fileReplacements != null && fileReplacements.size() > 0) {
                replaceFiles(fileReplacements, projectDirectory);
            }

            //replace all configured strings
            if (stringReplacements != null && stringReplacements.stringReplacementList != null
                    && stringReplacements.stringReplacementList.size() > 0) {
                replaceStrings(stringReplacements, projectDirectory);
            }

            File targetDirectory = Utils.getTargetDirectory(mavenProject);
            String projectVersion = updateXcodeProjectInfoPlist(properties, mavenProject, projectName,
                    projectDirectory);

            if (infoPlistValues != null && infoPlistValues.size() > 0) {
                addInfoPlistEntries(properties, infoPlistValues, projectDirectory, projectName);
            }

            //update entitlements file
            prepareEntitlementsFile(properties, projectDirectory);

            //BEG clean the application
            cleanXcodeProject(properties, projectDirectory, xcodeBuildParameters);
            //END clean the application

            //unlock keychain
            unlockKeychain(properties, mavenProject, projectDirectory);

            //if project contains cocoaPods dependencies, we install them first
            if (Utils.cocoaPodsEnabled(properties)) {
                installCocoaPodsDependencies(projectDirectory);
            }

            if (Utils.carthageEnebled(properties)) {
                installCarthageDependencies(projectDirectory);
            }

            // Build the application

            buildXcodeProject(mavenProject, properties, projectDirectory, targetDirectory, projectName, false,
                    xcodeBuildParameters);

            if (xcodeExportOptions.method != null && xcodeExportOptions.method.equals("app-store")) {
                //remove simulator architectures if app-store is chosen
                removeSimulatorArchitectures(targetDirectory);
                removeSimulatorArchitectures(projectDirectory);
            }

            if (Utils.isiOSFramework(mavenProject, properties) || Utils.isMacOSFramework(properties)) {
                String simulatorArchitectures = Utils.getArchitecturesForSdk(properties, Utils.SDK_IPHONE_SIMULATOR);
                boolean shouldBuildSimulatorArchitectures =
                        simulatorArchitectures != null && !simulatorArchitectures.isEmpty();

                if (!Utils.isMacOSFramework(properties) && shouldBuildSimulatorArchitectures) {
                    //generate framework product also for iphonesimulator sdk
                    buildXcodeProject(mavenProject, properties, projectDirectory, targetDirectory, projectName, true,
                            xcodeBuildParameters);
                }

                String appName = properties.get(Utils.PLUGIN_PROPERTIES.APP_NAME.toString());
                String frameworkBuildName = Utils.getFrameworkBuildName(appName);
                String frameworkTargetName = Utils.getFrameworkTargetName(appName, properties);

                File targetWorkDirectory;
                List<String> frameworkPaths = new ArrayList<>();
                if (Utils.isMacOSFramework(properties)) {
                    targetWorkDirectory = new File(targetDirectory.toString() + File.separator + properties.get(
                            Utils.PLUGIN_PROPERTIES.CONFIGURATION.toString()) + File.separator);

                    frameworkPaths.add(targetWorkDirectory.getAbsolutePath() + File.separator + frameworkBuildName);
                } else {
                    File targetWorkDirectoryIphone = new File(
                            targetDirectory.toString() + File.separator + properties.get(
                                    Utils.PLUGIN_PROPERTIES.CONFIGURATION.toString()) + "-" + Utils.SDK_IPHONE_OS
                                    + File.separator);
                    frameworkPaths.add(
                            targetWorkDirectoryIphone.getAbsolutePath() + File.separator + frameworkBuildName);

                    File targetWorkDirectoryIphoneSimulator = new File(
                            targetDirectory.toString() + File.separator + properties.get(
                                    Utils.PLUGIN_PROPERTIES.CONFIGURATION.toString()) + "-" + Utils.SDK_IPHONE_SIMULATOR
                                    + File.separator);

                    //if we'd build the framework with xcodebuild archive command, we have to export the framework
                    // from archive
                    if (Utils.shouldBuildXCArchive(properties)) {
                        File archiveFile = new File(Utils.getArchiveName(projectName, mavenProject));
                        exportProductArchive(archiveFile, targetWorkDirectoryIphone, frameworkBuildName);

                        exportTargetDependencies(targetDependencies, archiveFile, targetWorkDirectoryIphone);
                    }

                    if (shouldBuildSimulatorArchitectures) {
                        frameworkPaths.add(targetWorkDirectoryIphoneSimulator.getAbsolutePath() + File.separator
                                + frameworkBuildName);

                        // if we don't use xcframeworks, we have to merge framework binaries (iphoneOS +
                        // iphonesimulatorOS)
                        if (!Utils.isiOSXcFramework(properties)) {
                            mergeFrameworkProducts(targetWorkDirectoryIphone, targetWorkDirectoryIphoneSimulator,
                                    appName, frameworkBuildName);
                        }
                    }

                    targetWorkDirectory = targetWorkDirectoryIphone;
                }

                if (Utils.isiOSXcFramework(properties)) {
                    generateXcFramework(properties, frameworkPaths, frameworkTargetName, targetWorkDirectory,
                            projectDirectory);
                }

                zipFrameworkArchive(properties, targetDependencies, frameworkTargetName, targetWorkDirectory);
            } else {
                //unlock keychain
                unlockKeychain(properties, mavenProject,
                        projectDirectory); //unlock it again, if during xcrun keychain is closed automatically again.

                if (properties.get(Utils.PLUGIN_PROPERTIES.BUILD_ID.toString()) != null) {
                    projectVersion += "-b" + properties.get(Utils.PLUGIN_PROPERTIES.BUILD_ID.toString());
                }

                generateIpaArchive(properties, mavenProject, xcodeExportOptions, schemeName, projectDirectory,
                        targetDirectory, projectVersion);
            }

            //lock keychain
            lockKeychain(properties);

            //revert all replaced files
            if (fileReplacements != null && fileReplacements.size() > 0) {
                revertReplacedFiles(fileReplacements, projectDirectory);
            }

            //revert all replaced strings
            if (stringReplacements != null && stringReplacements.stringReplacementList != null
                    && stringReplacements.stringReplacementList.size() > 0) {
                revertReplacedStrings(stringReplacements, projectDirectory);
            }

            // Generate the the deploy plist file
            generateDeployPlistFile(mavenProject, properties, targetDirectory, projectVersion);
        } catch (Exception e) {
            System.err.println("exception occurred while building project, e=" + e.getMessage());

            throw new IOSException(e.getMessage());
        }
    }

    private static void generateIpaArchive(Map<String, String> properties, MavenProject mavenProject,
                                           XcodeExportOptions xcodeExportOptions, String schemeName,
                                           File projectDirectory, File targetDirectory,
                                           String projectVersion) throws IOSException {

        File appTargetPath = new File(
                targetDirectory + File.separator + properties.get(Utils.PLUGIN_PROPERTIES.CONFIGURATION.toString())
                        + "-" + Utils.SDK_IPHONE_OS + "/" + properties.get(Utils.PLUGIN_PROPERTIES.TARGET.toString())
                        + "." + Utils.PLUGIN_SUFFIX.APP);

        File newAppTargetPath = new File(
                targetDirectory + File.separator + properties.get(Utils.PLUGIN_PROPERTIES.CONFIGURATION.toString())
                        + "-" + Utils.SDK_IPHONE_OS + "/" + properties.get(Utils.PLUGIN_PROPERTIES.APP_NAME.toString())
                        + "." + Utils.PLUGIN_SUFFIX.APP);

        File ipaBasePath = new File(
                targetDirectory + File.separator + properties.get(Utils.PLUGIN_PROPERTIES.CONFIGURATION.toString())
                        + "-" + Utils.SDK_IPHONE_OS);

        File ipaTargetPath = new File(
                ipaBasePath.getAbsolutePath() + "/" + properties.get(Utils.PLUGIN_PROPERTIES.APP_NAME.toString()) + "-"
                        + projectVersion + "." + Utils.PLUGIN_SUFFIX.IPA);

        File dsymTargetPath = new File(
                targetDirectory + File.separator + properties.get(Utils.PLUGIN_PROPERTIES.CONFIGURATION.toString())
                        + "-" + Utils.SDK_IPHONE_OS + "/" + properties.get(Utils.PLUGIN_PROPERTIES.TARGET.toString())
                        + "." + Utils.PLUGIN_SUFFIX.APP_DSYM);

        File newDsymTargetPath = new File(
                targetDirectory + File.separator + properties.get(Utils.PLUGIN_PROPERTIES.CONFIGURATION.toString())
                        + "-" + Utils.SDK_IPHONE_OS + "/" + properties.get(Utils.PLUGIN_PROPERTIES.APP_NAME.toString())
                        + "." + Utils.PLUGIN_SUFFIX.APP_DSYM);

        if (appTargetPath.exists() && !(appTargetPath.toString().equalsIgnoreCase(newAppTargetPath.toString()))) {
            ProcessBuilder processBuilder = new ProcessBuilder("mv", appTargetPath.toString(),
                    newAppTargetPath.toString());
            processBuilder.directory(projectDirectory);
            CommandHelper.performCommand(processBuilder);
        }

        if (dsymTargetPath.exists() && !(dsymTargetPath.toString().equalsIgnoreCase(newDsymTargetPath.toString()))) {
            ProcessBuilder processBuilder = new ProcessBuilder("mv", dsymTargetPath.toString(),
                    newDsymTargetPath.toString());
            processBuilder.directory(projectDirectory);
            CommandHelper.performCommand(processBuilder);
        }

        File ipaTmpDir = new File(targetDirectory, "ipa-temp-dir-" + UUID.randomUUID().toString());

        if (!ipaTmpDir.mkdir()) {
            System.err.println("Could not create ipa temp dir at path = " + ipaTmpDir.getAbsolutePath());
        }

        if (Utils.shouldBuildXCArchiveWithExportOptionsPlist(xcodeExportOptions)) {
            codeSignAfterXcode8_3(properties, mavenProject, projectDirectory, Utils.getIpaName(schemeName), ipaBasePath,
                    ipaTargetPath, ipaTmpDir, xcodeExportOptions);
        } else if (Utils.shouldBuildXCArchive(properties)) {
            codeSignAfterXcode6(properties, mavenProject, projectDirectory, ipaTargetPath);
        } else {
            codeSignBeforeXcode6(properties, projectDirectory, newAppTargetPath, ipaTargetPath, ipaTmpDir);
        }
    }

    private static void zipFrameworkArchive(Map<String, String> properties, List<String> targetDependencies,
                                            String frameworkName, File targetWorkDirectory) throws IOSException {
        // Zip Frameworks
        String targetZipPath = "../" + properties.get(Utils.PLUGIN_PROPERTIES.APP_NAME.toString()) + "."
                + Utils.PLUGIN_SUFFIX.FRAMEWORK_ZIP.toString();
        List<String> zipCommandParams = new ArrayList<>();
        zipCommandParams.add("zip");
        zipCommandParams.add("-r");
        zipCommandParams.add(targetZipPath);
        zipCommandParams.add(frameworkName);
        zipCommandParams.addAll(targetDependencies);

        ProcessBuilder processBuilder = new ProcessBuilder(zipCommandParams);
        processBuilder.directory(targetWorkDirectory);
        CommandHelper.performCommand(processBuilder);
    }

    private static void generateXcFramework(Map<String, String> properties, List<String> frameworkPaths,
                                            String frameworkTargetName, File targetWorkDirectory,
                                            File workDirectory) throws IOSException {

        StringBuilder buildCommand = new StringBuilder();

        if (properties.containsKey(Utils.PLUGIN_PROPERTIES.XCODE_BUILD_COMMAND_WRAPPER_EXECUTABLE.toString())) {
            String parameter =
                    Utils.buildCommandWrapperParameter(properties, targetWorkDirectory, workDirectory, "xcframework")
                            + " ";
            buildCommand.append(parameter);
        }

        buildCommand.append("xcodebuild");
        buildCommand.append(" -create-xcframework");

        for (String frameworkPath : frameworkPaths) {
            buildCommand.append(" -framework ").append(frameworkPath);
        }

        buildCommand.append(" -output ");
        buildCommand.append(frameworkTargetName);

        Utils.executeShellScript("execute-xcodebuild.sh", buildCommand.toString(), null, targetWorkDirectory);
    }

    private static void removeSimulatorArchitectures(File rootDirectory) {
        // Run shell-script from resource-folder.
        try {
            final String scriptName = "remove-simulator-archs.sh";
            File tempFile = Utils.createTempFile(scriptName);
            ProcessBuilder processBuilder = new ProcessBuilder("sh", tempFile.getAbsoluteFile().toString(),
                    rootDirectory.getAbsolutePath());

            processBuilder.directory(rootDirectory);
            CommandHelper.performCommand(processBuilder);
        } catch (IOException | IOSException e) {
            e.printStackTrace();
            //throw new IOSException(e);
        }
    }

    private static String updateXcodeProjectInfoPlist(Map<String, String> properties, MavenProject mavenProject,
                                                      String projectName, File workDirectory) throws IOSException {
        // Run agvtool to stamp marketing version
        String projectVersion = Utils.getAdjustedVersion(mavenProject, properties);

        // Run agvtool to stamp version
        ProcessBuilder processBuilderNewMarketingVersion = new ProcessBuilder("agvtool", "new-marketing-version",
                projectVersion);
        processBuilderNewMarketingVersion.directory(workDirectory);
        CommandHelper.performCommand(processBuilderNewMarketingVersion);

        // Run agvtool to stamp build number
        String buildNumber = getBuildNumber(mavenProject, properties);
        ProcessBuilder processBuilderNewVersion = new ProcessBuilder("agvtool", "new-version", "-all", buildNumber);
        processBuilderNewVersion.directory(workDirectory);
        CommandHelper.performCommand(processBuilderNewVersion);

        // Run PlistBuddy to stamp build if a build id is specified
        if (properties.get(Utils.PLUGIN_PROPERTIES.BUILD_ID.toString()) != null) {
            executePlistScript("write-buildnumber.sh", null,
                    properties.get(Utils.PLUGIN_PROPERTIES.BUILD_ID.toString()), workDirectory, projectName,
                    properties);
        }

        // Run PlistBuddy to app icon name if a build id is specified
        if (properties.get(Utils.PLUGIN_PROPERTIES.APP_ICON_NAME.toString()) != null) {
            executePlistScript("write-app-icon-name.sh", null,
                    properties.get(Utils.PLUGIN_PROPERTIES.APP_ICON_NAME.toString()), workDirectory, projectName,
                    properties);
        }

        // Run PlistBuddy to overwrite the bundle identifier in info plist
        if (properties.get(Utils.PLUGIN_PROPERTIES.BUNDLE_IDENTIFIER.toString()) != null) {
            executePlistScript("write-bundleidentifier.sh", null,
                    properties.get(Utils.PLUGIN_PROPERTIES.BUNDLE_IDENTIFIER.toString()), workDirectory, projectName,
                    properties);
        }

        // Run PlistBuddy to overwrite the display name in info plist
        if (properties.get(Utils.PLUGIN_PROPERTIES.DISPLAY_NAME.toString()) != null) {
            executePlistScript("write-displayname.sh", null,
                    properties.get(Utils.PLUGIN_PROPERTIES.DISPLAY_NAME.toString()), workDirectory, projectName,
                    properties);
        }
        return projectVersion;
    }

    private static void cleanXcodeProject(Map<String, String> properties, File workDirectory,
                                          List<String> xcodeBuildParameters) throws IOSException {

        String sdk = Utils.getSdk(properties, false);

        String buildCommandWrapperParameter = "";
        if (properties.containsKey(Utils.PLUGIN_PROPERTIES.XCODE_BUILD_COMMAND_WRAPPER_EXECUTABLE.toString())) {
            buildCommandWrapperParameter += Utils.buildCommandWrapperParameter(properties, workDirectory, workDirectory,
                    "clean");
        }

        StringBuilder xcodebuildCommand = new StringBuilder(
                buildCommandWrapperParameter + " " + "xcodebuild -alltargets -configuration " + properties.get(
                        Utils.PLUGIN_PROPERTIES.CONFIGURATION.toString()) + " clean -scheme " + properties.get(
                        Utils.PLUGIN_PROPERTIES.SCHEME.toString()) + " -sdk " + sdk);

        //add each dynamic parameter from pom
        for (String param : xcodeBuildParameters) {
            xcodebuildCommand.append(" ").append(param);
        }

        //append xcpretty arguments
        String jsonOutputFile = Utils.createJsonOutputFilePath("clean", properties);
        for (String xcprettyArg : Utils.getXcprettyCommand("xcodebuild-clean.log", jsonOutputFile).split(" ")) {
            xcodebuildCommand.append(" ").append(xcprettyArg);
        }

        Utils.executeShellScript("execute-xcodebuild.sh", xcodebuildCommand.toString(), null, workDirectory);
    }

    private static void generateDeployPlistFile(MavenProject mavenProject, Map<String, String> properties,
                                                File targetDirectory, String projectVersion) throws IOSException {

        if ((properties.get(Utils.PLUGIN_PROPERTIES.DEPLOY_IPA_PATH.toString()) != null) && (
                properties.get(Utils.PLUGIN_PROPERTIES.DEPLOY_ICON_PATH.toString()) != null)) {
            final String deployPlistName =
                    properties.get(Utils.PLUGIN_PROPERTIES.APP_NAME.toString()) + "-" + projectVersion + "."
                            + Utils.PLUGIN_SUFFIX.PLIST;
            writeDeployPlistFile(mavenProject, targetDirectory, deployPlistName, properties);
        }
    }

    private static void revertReplacedFiles(final List<FileReplacement> fileReplacements,
                                            File projectDirectory) throws IOSException {

        for (FileReplacement fileReplacement : fileReplacements) {
            replaceFile(projectDirectory, fileReplacement.targetFile, fileReplacement.sourceFile, true);
        }
    }

    private static void replaceFiles(final List<FileReplacement> fileReplacements,
                                     File projectDirectory) throws IOSException {

        for (FileReplacement fileReplacement : fileReplacements) {
            replaceFile(projectDirectory, fileReplacement.sourceFile, fileReplacement.targetFile, false);
        }
    }

    private static void replaceFile(File projectDirectory, String replaceSource, String replaceTarget,
                                    boolean revertTempFile) throws IOSException {

        File sourceFile = new File(projectDirectory.toString() + File.separator + replaceSource);
        File tempFile = new File(
                projectDirectory.toString() + File.separator + ((revertTempFile) ? replaceSource : replaceTarget)
                        + ".tmp");
        File targetFile = new File(projectDirectory.toString() + File.separator + replaceTarget);

        if (sourceFile.exists()) {
            //move existing target file to temp
            if (targetFile.exists()) {
                ProcessBuilder processBuilder = new ProcessBuilder("mv", targetFile.toString(), tempFile.toString());
                processBuilder.directory(projectDirectory);
                CommandHelper.performCommand(processBuilder);
            }

            //move source to target
            ProcessBuilder processBuilder = new ProcessBuilder("mv", sourceFile.toString(), targetFile.toString());
            processBuilder.directory(projectDirectory);
            CommandHelper.performCommand(processBuilder);
        } else {
            System.err.println("source file doesn't exist at path= " + sourceFile.toString());
        }

        if (revertTempFile) {
            ProcessBuilder processBuilder = new ProcessBuilder("mv", tempFile.toString(), sourceFile.toString());
            processBuilder.directory(projectDirectory);
            CommandHelper.performCommand(processBuilder);
        }
    }

    private static void addInfoPlistEntries(Map<String, String> properties, final List<InfoPlistValue> plistValues,
                                            File projectDirectory, String projectName) throws IOSException {

        for (InfoPlistValue plistEntry : plistValues) {
            addInfoPlistEntry(properties, projectDirectory, projectName, plistEntry.key, plistEntry.value);
        }
    }

    private static void addInfoPlistEntry(Map<String, String> properties, File projectDirectory, String projectName,
                                          String key, String value) throws IOSException {

        executePlistScript("write-key.sh", key, value, projectDirectory, projectName, properties);
    }

    private static void revertReplacedStrings(final StringReplacementConfig stringReplacements,
                                              File projectDirectory) throws IOSException, IOException {

        for (StringReplacement stringReplacement : stringReplacements.stringReplacementList) {
            replaceString(projectDirectory, stringReplacement.sourceFile, stringReplacement.targetString,
                    stringReplacement.sourceString, stringReplacements.failWhenNotFound);
        }
    }

    private static void replaceStrings(final StringReplacementConfig stringReplacements,
                                       File projectDirectory) throws IOSException, IOException {

        for (StringReplacement stringReplacement : stringReplacements.stringReplacementList) {
            replaceString(projectDirectory, stringReplacement.sourceFile, stringReplacement.sourceString,
                    stringReplacement.targetString, stringReplacements.failWhenNotFound);
        }
    }

    private static void replaceString(File projectDirectory, String sourceFilePath, String replaceSource,
                                      String replaceTarget, boolean failWhenNotFound) throws IOSException, IOException {

        File sourceFile = new File(projectDirectory.toString() + File.separator + sourceFilePath);

        if (sourceFile.exists()) {
            byte[] encoded = Files.readAllBytes(Paths.get(sourceFile.getAbsolutePath()));
            String sourceFileContents = new String(encoded, Charset.defaultCharset());
            if (sourceFileContents.contains(replaceSource)) {
                sourceFileContents = sourceFileContents.replace(replaceSource, replaceTarget);
            } else if (failWhenNotFound) {
                throw new IOSException("string '" + replaceSource + "' not found in: " + sourceFile.toString());
            }

            FileOutputStream sourceFileStream = new FileOutputStream(sourceFile, false);
            byte[] contentBytes = sourceFileContents.getBytes();
            sourceFileStream.write(contentBytes);
            sourceFileStream.close();
        } else {
            String error = "source file doesn't exist at path= " + sourceFile.toString();
            System.err.println(error);

            if (failWhenNotFound) {
                throw new IOSException(error);
            }
        }
    }

    private static void lockKeychain(Map<String, String> properties) throws IOSException {

        if (properties.containsKey(Utils.PLUGIN_PROPERTIES.KEYCHAIN_PATH.toString()) && properties.containsKey(
                Utils.PLUGIN_PROPERTIES.KEYCHAIN_PASSWORD.toString())) {
            String command =
                    "security lock-keychain " + properties.get(Utils.PLUGIN_PROPERTIES.KEYCHAIN_PATH.toString());
            ProcessBuilder processBuilderLockKeyChain = new ProcessBuilder(CommandHelper.getCommand(command));
            CommandHelper.performCommand(processBuilderLockKeyChain);
        }
    }

    private static void codeSignBeforeXcode6(Map<String, String> properties, File workDirectory, File newAppTargetPath,
                                             File ipaTargetPath, File ipaTmpDir) throws IOSException {

        ProcessBuilder processBuilderCodeSign = new ProcessBuilder("xcrun", "--no-cache",  //disbale caching
                "-sdk", properties.get(Utils.PLUGIN_PROPERTIES.SDK.toString()), "PackageApplication", "-v",
                newAppTargetPath.toString(), "-o", ipaTargetPath.toString(), "--sign",
                properties.get(Utils.PLUGIN_PROPERTIES.CODE_SIGN_IDENTITY.toString()));

        processBuilderCodeSign.directory(workDirectory);
        processBuilderCodeSign.environment()
                .put("TMPDIR", ipaTmpDir.getAbsolutePath());  //this is really important to avoid collisions, if not set
        // /var/folders will be used here
        CommandHelper.performCommand(processBuilderCodeSign);
    }

    private static void codeSignAfterXcode6(Map<String, String> properties, MavenProject mavenProject,
                                            File workDirectory, File ipaTargetPath) throws IOSException {
        /*
            xcodebuild -exportArchive -exportFormat format -archivePath xcarchivepath -exportPath destinationpath
                [-exportProvisioningProfile profilename] [-exportSigningIdentity identityname]
                [-exportInstallerIdentity identityname]
         */

        if (!ipaTargetPath.getParentFile().exists() && !ipaTargetPath.getParentFile().mkdirs()) {
            throw new RuntimeException(
                    "Could not create directories for ipa target path=" + ipaTargetPath.getAbsolutePath());
        }

        StringBuilder buildCommand = new StringBuilder();

        if (properties.containsKey(Utils.PLUGIN_PROPERTIES.XCODE_BUILD_COMMAND_WRAPPER_EXECUTABLE.toString())) {
            String parameter = Utils.buildCommandWrapperParameter(properties, workDirectory, workDirectory, "codesign");
            buildCommand.append(parameter + " ");
        }

        buildCommand.append("xcodebuild");
        buildCommand.append(" -exportArchive");
        buildCommand.append(" -exportFormat ");
        buildCommand.append(Utils.PLUGIN_SUFFIX.IPA.toString());
        buildCommand.append(" -archivePath ");
        buildCommand.append(Utils.getArchiveName(Utils.buildProjectName(properties, mavenProject), mavenProject));
        buildCommand.append(" -exportPath ");
        buildCommand.append(ipaTargetPath.toString());
        buildCommand.append(" -exportWithOriginalSigningIdentity");

        //append xcpretty arguments
        String jsonOutputFile = Utils.createJsonOutputFilePath("codesign", properties);
        for (String xcprettyArg : Utils.getXcprettyCommand("xcodebuild-codesign.log", jsonOutputFile).split(" ")) {
            buildCommand.append(" ").append(xcprettyArg);
        }

        Utils.executeShellScript("execute-xcodebuild.sh", buildCommand.toString(), null, workDirectory);
    }

    private static void codeSignAfterXcode8_3(Map<String, String> properties, MavenProject mavenProject,
                                              File workDirectory, String ipaName, File ipaBasePath, File ipaTargetPath,
                                              File ipaTmpDir,
                                              XcodeExportOptions xcodeExportOptions) throws IOSException {
        /*
            xcodebuild -exportArchive -archivePath xcarchivepath -exportPath destinationpath -exportOptionsPlist
            plistpath
         */

        if (!ipaTargetPath.getParentFile().exists() && !ipaTargetPath.getParentFile().mkdirs()) {
            throw new RuntimeException(
                    "Could not create directories for ipa target path=" + ipaTargetPath.getAbsolutePath());
        }

        File plistFilePath = generateExportOptionsPlist(xcodeExportOptions, workDirectory);

        StringBuilder buildCommand = new StringBuilder();

        if (properties.containsKey(Utils.PLUGIN_PROPERTIES.XCODE_BUILD_COMMAND_WRAPPER_EXECUTABLE.toString())) {
            String parameter = Utils.buildCommandWrapperParameter(properties, workDirectory, workDirectory, "codesign");
            buildCommand.append(parameter + " ");
        }

        buildCommand.append("xcodebuild");
        buildCommand.append(" -exportArchive");
        buildCommand.append(" -archivePath ");
        buildCommand.append(Utils.getArchiveName(Utils.buildProjectName(properties, mavenProject), mavenProject));
        buildCommand.append(" -exportPath ");
        buildCommand.append(ipaBasePath.toString());
        buildCommand.append(" -exportOptionsPlist ");
        buildCommand.append(plistFilePath.getAbsolutePath());

        //append xcpretty arguments
        String jsonOutputFile = Utils.createJsonOutputFilePath("codesign", properties);
        for (String xcprettyArg : Utils.getXcprettyCommand("xcodebuild-codesign.log", jsonOutputFile).split(" ")) {
            buildCommand.append(" ").append(xcprettyArg);
        }

        Utils.executeShellScript("execute-xcodebuild.sh", buildCommand.toString(), ipaTmpDir.getAbsolutePath(),
                workDirectory);

        File ipaPath = new File(ipaBasePath.getAbsolutePath() + "/" + ipaName);

        //move created ipa to final destination
        ProcessBuilder processBuilder = new ProcessBuilder("mv", ipaPath.toString(), ipaTargetPath.toString());
        processBuilder.directory(workDirectory);
        CommandHelper.performCommand(processBuilder);

        //remove export options plist file
        processBuilder = new ProcessBuilder("rm", plistFilePath.getAbsolutePath());
        processBuilder.directory(workDirectory);
        CommandHelper.performCommand(processBuilder);
    }

    private static void unlockKeychain(Map<String, String> properties, MavenProject mavenProject,
                                       File workDirectory) throws IOSException {

        if (Utils.shouldCodeSign(mavenProject, properties) && properties.containsKey(
                Utils.PLUGIN_PROPERTIES.KEYCHAIN_PATH.toString()) && properties.containsKey(
                Utils.PLUGIN_PROPERTIES.KEYCHAIN_PASSWORD.toString())) {
            Utils.executeShellScript("unlock-keychain.sh",
                    properties.get(Utils.PLUGIN_PROPERTIES.KEYCHAIN_PASSWORD.toString()),
                    properties.get(Utils.PLUGIN_PROPERTIES.KEYCHAIN_PATH.toString()), workDirectory);
        }
    }

    private static void buildXcodeProject(MavenProject mavenProject, Map<String, String> properties, File workDirectory,
                                          File targetDirectory, String projectName, boolean shouldUseIphoneSimulatorSDK,
                                          List<String> xcodeBuildParameters) throws IOSException {

        List<String> buildParameters = new ArrayList<>();
        if (properties.containsKey(Utils.PLUGIN_PROPERTIES.XCODE_BUILD_COMMAND_WRAPPER_EXECUTABLE.toString())) {
            String parameter = Utils.buildCommandWrapperParameter(properties, workDirectory, workDirectory, "build");
            buildParameters.add(parameter);
        }

        buildParameters.add("xcodebuild");

        //if cocoa pods is enabled, we have to build the .xcworkspace file instead of .xcodeproj
        if (Utils.shouldUseWorkspaceFile(properties)) {
            buildParameters.add("-workspace");
            buildParameters.add(projectName + ".xcworkspace");
        }

        String sdk = Utils.getSdk(properties, shouldUseIphoneSimulatorSDK);
        buildParameters.add("-sdk");
        buildParameters.add(sdk);

        String archs = Utils.getArchitecturesForSdk(properties, sdk);
        if (archs != null && !archs.isEmpty()) {
            buildParameters.add("ARCHS=\"" + archs + "\"");
            buildParameters.add("VALID_ARCHS=\"" + archs + "\"");
        }

        buildParameters.add("-configuration");
        buildParameters.add(properties.get(Utils.PLUGIN_PROPERTIES.CONFIGURATION.toString()));

        if (Utils.shouldBuildXCArchive(properties) && !shouldUseIphoneSimulatorSDK) {
            buildParameters.add("archive");
            buildParameters.add("-archivePath");
            buildParameters.add(Utils.getArchiveName(projectName, mavenProject));
        } else {
            buildParameters.add("SYMROOT=" + targetDirectory.getAbsolutePath());   //only possible without xcarchive
        }

        if (properties.containsKey(Utils.PLUGIN_PROPERTIES.SCHEME.toString())) {
            buildParameters.add("-scheme");
            buildParameters.add(properties.get(Utils.PLUGIN_PROPERTIES.SCHEME.toString()));
        }

        //if product should be code signed, we add flags for code signing
        if (Utils.shouldCodeSign(mavenProject, properties)) {

            if (Utils.shouldCodeSignWithResourceRules(properties)) {
                buildParameters.add(
                        "CODE_SIGN_RESOURCE_RULES_PATH=$(SDKROOT)/ResourceRules.plist"); //since xcode 6.1 is
                // necessary, if not set, app is not able to be signed with a key.
            }

            if (properties.containsKey(Utils.PLUGIN_PROPERTIES.CODE_SIGN_IDENTITY.toString())) {
                buildParameters.add(
                        "CODE_SIGN_IDENTITY=\"" + properties.get(Utils.PLUGIN_PROPERTIES.CODE_SIGN_IDENTITY.toString())
                                + "\"");
            }
        } else {
            //otherwise we skip code signing
            buildParameters.add("CODE_SIGN_IDENTITY=");
            buildParameters.add("CODE_SIGNING_REQUIRED=NO");
        }

        if (Utils.shouldCodeSign(mavenProject, properties) && properties.containsKey(
                Utils.PLUGIN_PROPERTIES.PROVISIONING_PROFILE_UUID.toString()) && !properties.get(
                Utils.PLUGIN_PROPERTIES.PROVISIONING_PROFILE_UUID.toString()).equals("")) {
            buildParameters.add("PROVISIONING_PROFILE=\"" + properties.get(
                    Utils.PLUGIN_PROPERTIES.PROVISIONING_PROFILE_UUID.toString()) + "\"");
        }

        if (Utils.shouldCodeSign(mavenProject, properties) && properties.containsKey(
                Utils.PLUGIN_PROPERTIES.PROVISIONING_PROFILE_SPECIFIER.toString()) && !properties.get(
                Utils.PLUGIN_PROPERTIES.PROVISIONING_PROFILE_SPECIFIER.toString()).equals("")) {
            buildParameters.add("PROVISIONING_PROFILE_SPECIFIER=\"" + properties.get(
                    Utils.PLUGIN_PROPERTIES.PROVISIONING_PROFILE_SPECIFIER.toString()) + "\"");
        }

        if (Utils.shouldCodeSign(mavenProject, properties) && properties.containsKey(
                Utils.PLUGIN_PROPERTIES.DEVELOPMENT_TEAM.toString())) {
            buildParameters.add(
                    "DEVELOPMENT_TEAM=\"" + properties.get(Utils.PLUGIN_PROPERTIES.DEVELOPMENT_TEAM.toString()) + "\"");
        } else {
            buildParameters.add("DEVELOPMENT_TEAM=");
        }

        if (properties.get(Utils.PLUGIN_PROPERTIES.BUNDLE_IDENTIFIER.toString()) != null) {
            buildParameters.add("PRODUCT_BUNDLE_IDENTIFIER=\"" + properties.get(
                    Utils.PLUGIN_PROPERTIES.BUNDLE_IDENTIFIER.toString()) + "\"");
        }

        if ((Utils.isMacOSFramework(properties) || Utils.isiOSFramework(mavenProject, properties))
                && properties.containsKey(Utils.PLUGIN_PROPERTIES.APP_NAME.toString())) {
            buildParameters.add("PRODUCT_NAME=\"" + properties.get(Utils.PLUGIN_PROPERTIES.APP_NAME.toString()) + "\"");
        }

        String target = null;
        if (properties.containsKey(Utils.PLUGIN_PROPERTIES.TARGET.toString())) {
            target = properties.get(Utils.PLUGIN_PROPERTIES.TARGET.toString());
        }

        //only if target tag is present and we are not building via xcArchive, we set the target switch
        if (!Utils.shouldBuildXCArchive(properties)
                && target != null) { //from XCode > Version 7 target should not be used any more. Use scheme instead!
            // Add target. Uses target 'framework' to build Frameworks.
            buildParameters.add("-target");
            buildParameters.add(target);
        }

        //buildParameters.add("SHARED_PRECOMPS_DIR=" + precompiledHeadersDir.getAbsolutePath());   //this is really
        // important to avoid collisions, if not set /var/folders will be used here
        if (Utils.shouldCodeSign(mavenProject, properties) && properties.containsKey(
                Utils.PLUGIN_PROPERTIES.KEYCHAIN_PATH.toString())) {
            buildParameters.add("OTHER_CODE_SIGN_FLAGS=\"--keychain " + properties.get(
                    Utils.PLUGIN_PROPERTIES.KEYCHAIN_PATH.toString()) + "\"");
        }

        if (properties.containsKey(Utils.PLUGIN_PROPERTIES.GCC_PREPROCESSOR_DEFINITIONS.toString())) {
            buildParameters.add("GCC_PREPROCESSOR_DEFINITIONS='" + properties.get(
                    Utils.PLUGIN_PROPERTIES.GCC_PREPROCESSOR_DEFINITIONS.toString()) + "'");
        }

        //add parameter to enable bitcode for build with iphone simulator sdk
        if (shouldUseIphoneSimulatorSDK && Utils.isIphoneSimulatorBitcodeEnabled(properties)) {
            buildParameters.add("ENABLE_BITCODE=YES");
            buildParameters.add("OTHER_CFLAGS='-fembed-bitcode'");
        }

        if (properties.containsKey(Utils.PLUGIN_PROPERTIES.DERIVED_DATA_PATH.toString())) {
            buildParameters.add(
                    "-derivedDataPath " + properties.get(Utils.PLUGIN_PROPERTIES.DERIVED_DATA_PATH.toString()));
        }

        //add each dynamic parameter from pom
        buildParameters.addAll(xcodeBuildParameters);

        //append xcpretty arguments

        String jsonOutputFile = Utils.createJsonOutputFilePath("build", properties);
        Collections.addAll(buildParameters, Utils.getXcprettyCommand("xcodebuild.log", jsonOutputFile).split(" "));

        StringBuilder buildCommand = new StringBuilder();
        for (String buildParam : buildParameters) {
            buildCommand.append(buildParam);
            buildCommand.append(" ");
        }

        Utils.executeShellScript("execute-xcodebuild.sh", buildCommand.toString(), null, workDirectory);
    }

    private static void mergeFrameworkProducts(File targetWorkDirectoryIphone, File targetWorkDirectoryIphoneSimulator,
                                               String appName, String frameworkName) throws IOSException {
        // Run shell-script from resource-folder.
        final String scriptName = "merge-framework-products";

        final String iphoneosFrameworkProductPath = targetWorkDirectoryIphone.toString() + "/" + frameworkName;
        final String iphoneSimulatorFrameworkProductPath =
                targetWorkDirectoryIphoneSimulator.toString() + "/" + frameworkName;
        final String mergedFrameworkPath = targetWorkDirectoryIphone.toString() + "/" + frameworkName;

        File tempFile;
        try {
            tempFile = File.createTempFile(scriptName, "sh");
        } catch (IOException e) {
            e.printStackTrace();
            throw new IOSException("Cannot create tempfile for script: " + scriptName);
        }

        InputStream inputStream = ProjectBuilder.class.getResourceAsStream("/META-INF/" + scriptName + ".sh");

        OutputStream outputStream = null;

        try {
            outputStream = new FileOutputStream(tempFile);
            byte[] buffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new IOSException("Cannot find tempfile at path: " + tempFile.getAbsolutePath());
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        ProcessBuilder processBuilder = new ProcessBuilder("sh", tempFile.getAbsoluteFile().toString(),
                iphoneosFrameworkProductPath, iphoneSimulatorFrameworkProductPath, mergedFrameworkPath, appName);

        processBuilder.directory(targetWorkDirectoryIphone);
        CommandHelper.performCommand(processBuilder);
    }

    private static void exportTargetDependencies(List<String> targetDependencies, File archiveFile,
                                                 File targetDirectory) throws IOSException {

        if (targetDependencies != null && targetDependencies.size() > 0) {
            for (String targetDependency : targetDependencies) {
                exportProductArchive(archiveFile, targetDirectory, targetDependency);
            }
        }
    }

    private static void exportProductArchive(File archiveFile, File targetPath,
                                             String productName) throws IOSException {
        // Run shell-script from resource-folder.
        try {
            final String scriptName = "export-product-archive.sh";

            targetPath.mkdir();
            final String productTargetPath = targetPath.toString() + "/" + productName;

            XcodeArchiveProductType productType = Utils.getExportProductType(productName);
            String productPath = Utils.getExportProductPath(productType);

            File tempFile = Utils.createTempFile(scriptName);
            ProcessBuilder processBuilder = new ProcessBuilder("sh", tempFile.getAbsoluteFile().toString(),
                    archiveFile.toString(), productPath, productName, targetPath.toString(), productTargetPath);

            processBuilder.directory(targetPath);
            CommandHelper.performCommand(processBuilder);
        } catch (Exception e) {
            e.printStackTrace();
            throw new IOSException(e);
        }
    }

    private static String getSchemeOrTarget(final Map<String, String> properties) {

        String targetName = null;
        if (properties.containsKey(Utils.PLUGIN_PROPERTIES.SCHEME.toString())) {
            targetName = properties.get(Utils.PLUGIN_PROPERTIES.SCHEME.toString());
        } else if (properties.containsKey(Utils.PLUGIN_PROPERTIES.TARGET.toString())) {
            targetName = properties.get(Utils.PLUGIN_PROPERTIES.TARGET.toString());
        }

        return targetName;
    }

    private static void prepareEntitlementsFile(final Map<String, String> properties,
                                                File workDirectory) throws IOSException, FileNotFoundException,
            IOException {

        String targetName = getSchemeOrTarget(properties);

        String entitlementsFilePath = workDirectory + File.separator + targetName + ".entitlements";
        File entitlementsFile = new File(entitlementsFilePath);
        if (entitlementsFile.exists() && !entitlementsFile.isDirectory()) {
            byte[] encoded = Files.readAllBytes(Paths.get(entitlementsFilePath));
            String entitlementsFileContents = new String(encoded, Charset.defaultCharset());

            boolean isTestflightBuild = Utils.isTestflightBuild(properties);
            if (isTestflightBuild) {
                if (!entitlementsFileContents.contains("<key>beta-reports-active</key>")) {
                    entitlementsFileContents = entitlementsFileContents.replace("</dict>",
                            "<key>beta-reports-active</key><true/></dict>");
                }
            }

            FileOutputStream entitlementsFileStream = new FileOutputStream(entitlementsFile, false);
            byte[] contentBytes = entitlementsFileContents.getBytes();
            entitlementsFileStream.write(contentBytes);
            entitlementsFileStream.close();
        } else {
            System.err.println("entitlements file does not exists at path=" + entitlementsFilePath);
        }
    }

    private static void executePlistScript(String scriptName, String key, String value, File workDirectory,
                                           String projectName,
                                           final Map<String, String> properties) throws IOSException {

        String infoPlistFile =
                workDirectory + File.separator + projectName + File.separator + projectName + "-Info.plist";

        if (properties.get(Utils.PLUGIN_PROPERTIES.INFO_PLIST.toString()) != null) {
            infoPlistFile =
                    workDirectory + File.separator + properties.get(Utils.PLUGIN_PROPERTIES.INFO_PLIST.toString());
        }

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

            ProcessBuilder processBuilder;

            if (key == null) {
                processBuilder = new ProcessBuilder("sh", tempFile.getAbsoluteFile().toString(), infoPlistFile, value);
            } else {
                processBuilder = new ProcessBuilder("sh", tempFile.getAbsoluteFile().toString(), infoPlistFile, key,
                        value);
            }

            processBuilder.directory(workDirectory);
            CommandHelper.performCommand(processBuilder);
        } catch (IOException e) {
            e.printStackTrace();
            throw new IOSException(e);
        }
    }

    private static void writeDeployPlistFile(MavenProject mavenProject, File targetDirectory, String deployPlistName,
                                             final Map<String, String> properties) throws IOSException {
        // Run shell-script from resource-folder.
        try {
            final String scriptName = "write-deploy-plist";

            final String ipaLocation = properties.get(Utils.PLUGIN_PROPERTIES.DEPLOY_IPA_PATH.toString());
            final String iconLocation = properties.get(Utils.PLUGIN_PROPERTIES.DEPLOY_ICON_PATH.toString());
            final String displayName = properties.get(Utils.PLUGIN_PROPERTIES.DISPLAY_NAME.toString());
            final String bundleIdentifier = properties.get(Utils.PLUGIN_PROPERTIES.BUNDLE_IDENTIFIER.toString());
            String bundleVersion = Utils.getAdjustedVersion(mavenProject, properties);
            final String buildNumber = getBuildNumber(mavenProject, properties);

            File tempFile = File.createTempFile(scriptName, "sh");

            InputStream inputStream = ProjectBuilder.class.getResourceAsStream("/META-INF/" + scriptName);
            OutputStream outputStream = new FileOutputStream(tempFile);

            byte[] buffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.close();

            ProcessBuilder processBuilder = new ProcessBuilder("sh", tempFile.getAbsoluteFile().toString(),
                    deployPlistName, ipaLocation, iconLocation, displayName, bundleIdentifier, bundleVersion,
                    buildNumber);

            processBuilder.directory(targetDirectory);
            CommandHelper.performCommand(processBuilder);
        } catch (IOException e) {
            e.printStackTrace();
            throw new IOSException(e);
        }
    }

    private static String getBuildNumber(MavenProject mavenProject, final Map<String, String> properties) {

        String bundleVersion = Utils.getAdjustedVersion(mavenProject, properties);
        String tmpBuildNumber = properties.get(Utils.PLUGIN_PROPERTIES.BUILD_ID.toString());
        if (tmpBuildNumber == null || tmpBuildNumber.equals("") || tmpBuildNumber.equals("n/a")) {
            tmpBuildNumber = bundleVersion;
        }

        return tmpBuildNumber;
    }

    private static void installCocoaPodsDependencies(File projectDirectory) throws IOSException {

        try {
            ProcessBuilder processBuilder = new ProcessBuilder("pod", "install", "--repo-update");
            processBuilder.directory(projectDirectory);
            CommandHelper.performCommand(processBuilder);
        } catch (Exception e) {
            e.printStackTrace();
            throw new IOSException(e);
        }
    }

    private static void installCarthageDependencies(File projectDirectory) throws IOSException {

        try {
            ProcessBuilder processBuilder = new ProcessBuilder("carthage", "update");
            processBuilder.directory(projectDirectory);
            CommandHelper.performCommand(processBuilder);
        } catch (Exception e) {
            e.printStackTrace();
            throw new IOSException(e);
        }
    }

    private static File generateExportOptionsPlist(XcodeExportOptions xcodeExportOptions,
                                                   File workDirectory) throws IOSException {
        //create tmp file path
        String plistFilePath = "/tmp/exportOptions-" + UUID.randomUUID() + ".plist";
        File plistFile = new File(plistFilePath);

        //generate json from export options
        Gson gson = new Gson();
        String jsonString = gson.toJson(xcodeExportOptions);

        //call plutil to generate plist file
        try {
            final String scriptName = "create-export-options-plist.sh";
            File tempFile = File.createTempFile(scriptName, "sh");

            InputStream inputStream = ProjectBuilder.class.getResourceAsStream("/META-INF/" + scriptName);
            OutputStream outputStream = new FileOutputStream(tempFile);

            byte[] buffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.close();

            ProcessBuilder processBuilder = new ProcessBuilder("sh", tempFile.getAbsoluteFile().toString(), jsonString,
                    plistFilePath);

            processBuilder.directory(workDirectory);
            CommandHelper.performCommand(processBuilder);
        } catch (IOException e) {
            e.printStackTrace();
            throw new IOSException(e);
        }

        return plistFile;
    }
}
