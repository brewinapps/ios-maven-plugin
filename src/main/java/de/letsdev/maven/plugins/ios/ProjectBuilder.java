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
		// File workDir = new File(properties.get("baseDir") + "/" +
		// properties.get("sourceDir"));
		File workDir = new File(mavenProject.getBasedir().toString()
				+ "/src/ios/" + mavenProject.getArtifactId());

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

				System.out.println("Command: "
						+ processBuilder.command().toString());

				processBuilder.directory(workDir);
				CommandHelper.performCommand(processBuilder);

			} catch (IOException e) {
				// TODO Auto-generated catch block
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
		buildParameters.add("SYMROOT=" + targetDir.getAbsolutePath());
		buildParameters.add("CODE_SIGN_IDENTITY="
				+ properties.get("codeSignIdentity"));

		if (properties.get("scheme") != null) {
			buildParameters.add("-scheme");
			buildParameters.add(properties.get("scheme"));
		}

		if (properties.get("target") != null) {
			buildParameters.add("-target");
			buildParameters.add(properties.get("target"));
		}

		processBuilder = new ProcessBuilder(buildParameters);
		processBuilder.directory(workDir);
		CommandHelper.performCommand(processBuilder);
		
		

		if (properties.get("target").equals("framework")) {
			
			// Zip Framwork
			processBuilder = new ProcessBuilder(
					"zip",
					"-r",
					mavenProject.getArtifactId() + ".framwork.zip",
					properties.get("configuration") + "-iphoneos/" + mavenProject.getArtifactId() + ".framwork"				
					);
		
			processBuilder.directory(targetDir);
			CommandHelper.performCommand(processBuilder);

		} else {

			// Generate IPA
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
	}
}
