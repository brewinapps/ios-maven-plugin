package com.brewinapps.maven.plugins.ios;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * 
 * @author Brewin' Apps AS
 */
public class ProjectBuilder {
	
	/**
	 * 
	 * @throws AutopilotException
	 */
	public static void clean(final File baseDir) throws AutopilotException {
		ProcessBuilder pb = new ProcessBuilder(
				"xcodebuild",
				"clean");
		
		// Set the project working directory
		pb.directory(baseDir);
		
		performCommand(pb);
	}
	
	/**
	 * 
	 * @throws AutopilotException
	 */
	public static void build(final File baseDir, final String sourceDir, final String targetDir, final String codeSignIdentity) throws AutopilotException {
		// Make sure the source directory exists
		File workDir = new File(baseDir.getAbsoluteFile() + "/" + sourceDir);
		if (!workDir.exists()) {
			throw new AutopilotException("Invalid sourceDir specified: " + workDir.getAbsolutePath());
		}
		
		File buildDir = new File(baseDir.getAbsoluteFile() + "/" + targetDir);
		
		ProcessBuilder pb = new ProcessBuilder(
				"xcodebuild",
				"SYMROOT=" + buildDir.getAbsolutePath(),
				(codeSignIdentity != null) ? "CODE_SIGN_IDENTITY=" + codeSignIdentity : "",
				"build");
		
		// Set the process' working directory
		pb.directory(workDir);
		
		performCommand(pb);
	}
	
	/**
	 * 
	 * @param cmd
	 * @throws AutopilotException
	 */
	private static void performCommand(final ProcessBuilder pb) throws AutopilotException {
		pb.redirectErrorStream(true);
		
		// Start the build
		Process p;
		
		try {
			p = pb.start();
		} catch (IOException e) {
			throw new AutopilotException(e);
		}
		
		BufferedReader input = new BufferedReader(
				new InputStreamReader(p.getInputStream()));

		// Retrieve the return code of the build
		int rc;
		
		try {
			// Display output
			String outLine = null;
			while((outLine = input.readLine()) != null) {
				System.out.println(outLine);
			}
			input.close();
		} catch (IOException e) {
			throw new AutopilotException("An error occured while reading the " +
					"input stream");
		}
		
		try {
			rc = p.waitFor();
		} catch (InterruptedException e) {
			throw new AutopilotException(e);
		}
		
		// Check if the return code indicates an error
		if (rc == 0) {
			throw new AutopilotException("The XCode build command was " +
					"unsuccessful");
		}
	}
	
}
