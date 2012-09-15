package com.brewinapps.ios;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author Brewin' Apps AS
 */
public class ProjectBuilder {
	
	/**
	 * @param properties
	 * @throws IOSException
	 */
	public static void build(final Map<String, String> properties) throws IOSException {
		// Make sure the source directory exists
		File workDir = new File(properties.get("baseDir") + "/" + properties.get("sourceDir"));
		if (!workDir.exists()) {
			throw new IOSException("Invalid sourceDir specified: " + workDir.getAbsolutePath());
		}
		
		File targetDir = new File(properties.get("targetDir"));
		
		// Run agvtool to stamp marketing version
		ProcessBuilder pb = new ProcessBuilder(
				"agvtool",
				"new-marketing-version",
				properties.get("version"));
		pb.directory(workDir);
		CommandHelper.performCommand(pb);			
		
		// Run agvtool to stamp build if a build id is specified
		if (properties.get("buildId") != null) {
			pb = new ProcessBuilder(
					"agvtool",
					"new-version",
					"-all",
					properties.get("buildId"));
			pb.directory(workDir);
			CommandHelper.performCommand(pb);			
		}
		
		// Build the application
		List<String> buildParameters = new ArrayList<String>();
		buildParameters.add("xcodebuild");
		buildParameters.add("-sdk");
		buildParameters.add(properties.get("sdk"));
		buildParameters.add("-configuration");
		buildParameters.add(properties.get("configuration"));
		buildParameters.add("SYMROOT=" + targetDir.getAbsolutePath());
		buildParameters.add("CODE_SIGN_IDENTITY=" + properties.get("codeSignIdentity"));
		
		if (properties.get("scheme") != null) {
			buildParameters.add("-scheme");
			buildParameters.add(properties.get("scheme"));
		}
		pb = new ProcessBuilder(buildParameters);
		pb.directory(workDir);
		CommandHelper.performCommand(pb);
		
		// Generate IPA
		pb = new ProcessBuilder(
				"xcrun",
				"-sdk", "iphoneos",
				"PackageApplication",
				"-v", targetDir + "/" + properties.get("configuration") + "-iphoneos/" + properties.get("appName") + ".app",
				"-o", targetDir + "/" + properties.get("configuration") + "-iphoneos/" + properties.get("appName") + ".ipa",
				"--sign", properties.get("codeSignIdentity"));
		pb.directory(workDir);
		CommandHelper.performCommand(pb);
	}
	
	
}
