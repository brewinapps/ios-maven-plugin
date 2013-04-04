package com.brewinapps.ios;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * 
 * @author Brewin' Apps AS
 */
public class CommandHelper {

	/**
	 * @param pb
	 * @throws IOSException
	 */
	public static void performCommand(final ProcessBuilder pb) throws IOSException {
		pb.redirectErrorStream(true);
		
		StringBuilder joinedCommand = new StringBuilder();
		for (String segment : pb.command()) {
			joinedCommand.append(segment + " ");
		}
		System.out.printf("Executing '%s'\n", joinedCommand.toString().trim());
		
		Process p = null;
		try {
			p = pb.start();
		} catch (IOException e) {
			throw new IOSException(e);
		} catch (NullPointerException e) {
			throw new IOSException(String.format("A command argument is null '%s'", pb.command()), e);
		}
		
		BufferedReader input = new BufferedReader(
				new InputStreamReader(p.getInputStream()));

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
		
		if (rc != 0) {
			throw new IOSException("The XC command was " +
					"unsuccessful");
		}
	}

}
