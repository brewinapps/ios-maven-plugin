package com.brewinapps.maven.plugins.ios;

import java.io.File;
import java.util.Map;

/**
 * 
 * @author Brewin' Apps AS
 */
public class ProjectBuilder {
	
	/**
	 * 
	 * @throws IOSException
	 */
	public static void clean(final File baseDir) throws IOSException {
		ProcessBuilder pb = new ProcessBuilder(
				"xcodebuild",
				"clean");
		pb.directory(baseDir);
		CommandHelper.performCommand(pb);
	}
	
	/**
	 * 
	 * @throws IOSException
	 */
	public static void build(final Map<String, String> properties) throws IOSException {
		// Make sure the source directory exists
		File workDir = new File(properties.get("baseDir") + "/" + properties.get("sourceDir"));
		if (!workDir.exists()) {
			throw new IOSException("Invalid sourceDir specified: " + workDir.getAbsolutePath());
		}
		
		File targetPath = new File(properties.get("baseDir") + "/" + properties.get("targetDir"));
		
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
		pb = new ProcessBuilder(
				"xcodebuild",
				"-sdk", properties.get("sdk"),
				"-configuration", properties.get("configuration"),
				"SYMROOT=" + targetPath.getAbsolutePath(),
				"CODE_SIGN_IDENTITY=" + properties.get("codeSignIdentity"), 
				"build");
		pb.directory(workDir);
		CommandHelper.performCommand(pb);
		
		// Generate IPA
		pb = new ProcessBuilder(
				"xcrun",
				"-sdk", "iphoneos",
				"PackageApplication",
				"-v", targetPath + "/" + properties.get("configuration") + "-iphoneos/" + properties.get("appName") + ".app",
				"-o", targetPath + "/" + properties.get("configuration") + "-iphoneos/" + properties.get("appName") + ".ipa",
				"--sign", properties.get("codeSignIdentity"));
		pb.directory(workDir);
		CommandHelper.performCommand(pb);
	}
	
	
}
