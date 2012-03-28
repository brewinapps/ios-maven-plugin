package com.brewinapps.maven.plugins.ios;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class CommandHelper {
	/**
	 * 
	 * @param cmd
	 * @throws IOSException
	 */
	public static void performCommand(final ProcessBuilder pb) throws IOSException {
		pb.redirectErrorStream(true);
		
		StringBuilder joinedCommand = new StringBuilder();
		for (String segment : pb.command()) {
			joinedCommand.append(segment + " ");
		}
		System.out.printf("Executing '%s'\n", joinedCommand.toString());
		
		// Start the build
		Process p;
		
		try {
			p = pb.start();
		} catch (IOException e) {
			throw new IOSException(e);
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
			throw new IOSException("An error occured while reading the " +
					"input stream");
		}
		
		try {
			rc = p.waitFor();
		} catch (InterruptedException e) {
			throw new IOSException(e);
		}
		
		// Check if the return code indicates an error 
		if (rc != 0) {
			throw new IOSException("The XC command was " +
					"unsuccessful");
		}
	}

}
