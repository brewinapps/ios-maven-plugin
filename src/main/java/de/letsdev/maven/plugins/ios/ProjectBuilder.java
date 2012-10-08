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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.maven.project.MavenProject;

/**
 * 
 * @author let's dev
 */
public class ProjectBuilder {

	/**
	 * @param properties
	 * @throws IOSException
	 */
	public static void build(final Map<String, String> properties,
			MavenProject mavenProject) throws IOSException {

		// Make sure the source directory exists
		File workDir = new File(mavenProject.getBasedir().toString() + "/"
				+ properties.get("sourceDir") + "/"
				+ mavenProject.getArtifactId());

		if (!workDir.exists()) {
			throw new IOSException("Invalid sourceDir specified: "
					+ workDir.getAbsolutePath());
		}

		File targetDir = new File(mavenProject.getBuild().getDirectory());

		// Run agvtool to stamp marketing version
		ProcessBuilder processBuilder = new ProcessBuilder("agvtool",
				"new-marketing-version", mavenProject.getVersion());
		processBuilder.directory(workDir);
		CommandHelper.performCommand(processBuilder);

		// Run agvtool to stamp version
		processBuilder = new ProcessBuilder("agvtool", "new-version", "-all",
				mavenProject.getVersion());
		processBuilder.directory(workDir);
		CommandHelper.performCommand(processBuilder);

		// Run PlistPuddy to stamp build if a build id is specified
		if (properties.get("buildId") != null) {
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

				processBuilder = new ProcessBuilder("sh", tempFile
						.getAbsoluteFile().toString(), infoPlistFile,
						properties.get("buildId"));

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
		buildParameters.add(properties.get("sdk"));
		buildParameters.add("-configuration");
		buildParameters.add(properties.get("configuration"));
        buildParameters.add("SYMROOT=\"" + targetDir.getAbsolutePath() + "\"");
		buildParameters.add("CODE_SIGN_IDENTITY=\"" + properties.get("codeSignIdentity") + "\"");

		if (properties.get("scheme") != null) {
			buildParameters.add("-scheme");
			buildParameters.add(properties.get("scheme"));
		}

		// Add target. Uses target 'framework' to build Frameworks.
		buildParameters.add("-target");

		if (properties.containsKey("target") || (mavenProject.getPackaging().equals("ios-framework"))) {

			if (mavenProject.getPackaging().equals("ios-framework")) {
				buildParameters.add("framework");

			} else {
				buildParameters.add(properties.get("target"));
			}

		} else {
			buildParameters.add(mavenProject.getArtifactId());
		}

        if(properties.containsKey("keychainPath")) {
            buildParameters.add("OTHER_CODE_SIGN_FLAGS=\"--keychain " + properties.get("keychainPath") +"\"");
        }

        //unlock keychain
        if(properties.containsKey("keychainPath") && properties.containsKey("keychainPassword")) {
            String command = "security unlock-keychain -p \"" + properties.containsKey("keychainPassword") + "\" " + properties.containsKey("keychainPath");
            processBuilder = new ProcessBuilder(CommandHelper.getCommand(command));
            processBuilder.directory(workDir);
            CommandHelper.performCommand(processBuilder);
        }

		processBuilder = new ProcessBuilder(buildParameters);
		processBuilder.directory(workDir);
		CommandHelper.performCommand(processBuilder);

		// Zip Frameworks
		if (mavenProject.getPackaging().equals("ios-framework")) {

			File targetWorkDir = new File(targetDir.toString() + "/"
					+ properties.get("configuration") + "-iphoneos/");

			processBuilder = new ProcessBuilder("zip", "-r", "../"
					+ mavenProject.getArtifactId() + ".framework.zip",
					mavenProject.getArtifactId() + ".framework");

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
					targetDir + "/" + properties.get("configuration")
							+ "-iphoneos/" + properties.get("appName") + ".app",
					"-o",
					targetDir + "/" + properties.get("configuration")
							+ "-iphoneos/" + properties.get("appName") + ".ipa",
					"--sign", properties.get("codeSignIdentity"));

			processBuilder.directory(workDir);
			CommandHelper.performCommand(processBuilder);
		}

        //lock keychain
        if(properties.containsKey("keychainPath") && properties.containsKey("keychainPassword")) {
            String command = "security lock-keychain " + properties.containsKey("keychainPath");
            processBuilder = new ProcessBuilder(CommandHelper.getCommand(command));
            processBuilder.directory(workDir);
            CommandHelper.performCommand(processBuilder);
        }
	}
}
