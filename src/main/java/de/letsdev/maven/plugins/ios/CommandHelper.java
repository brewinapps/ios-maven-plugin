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

import java.io.IOException;
import java.util.StringTokenizer;
import org.apache.commons.io.IOUtils;

public class CommandHelper {

	/**
	 * @param processBuilder
	 * @throws IOSException
	 */
	public static void performCommand(final ProcessBuilder processBuilder) throws IOSException {
		processBuilder.redirectErrorStream(true);
		
		StringBuilder joinedCommand = new StringBuilder();
		for (String segment : processBuilder.command()) {
			joinedCommand.append(segment + " ");
		}
		System.out.printf("Executing '%s'\n", joinedCommand.toString().trim());


//        try {
//            Runtime.getRuntime().exec(joinedCommand.toString());
//        } catch (IOException e) {
//            throw new IOSException("The XC command was " +
//                    "unsuccessful, exception=" + e.getLocalizedMessage());
//        }

        String command = "security list-keychain";
        Process p = null;
        try {
            p = new ProcessBuilder(getCommand(command)).start();
        } catch (IOException e) {
            throw new IOSException("An error occured, error=" +e.getMessage());
        }
        try {
            System.out.println(IOUtils.toString(p.getInputStream()));
        } catch (IOException e) {
            throw new IOSException("An error occured, error=" +e.getMessage());
        }
        p.destroy();


        try {
            p = processBuilder.start();
        } catch (IOException e) {
            throw new IOSException("An error occured, error=" +e.getMessage());
        }
        try {
            System.out.println(IOUtils.toString(p.getInputStream()));
        } catch (IOException e) {
            throw new IOSException("An error occured, error=" +e.getMessage());
        }
        p.destroy();



		
		/*Process process = null;
		try {
			process = processBuilder.start();
		} catch (IOException e) {
			throw new IOSException(e);
		}
		
		BufferedReader input = new BufferedReader(
				new InputStreamReader(process.getInputStream()));

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
			rc = process.waitFor();
		} catch (InterruptedException e) {
			throw new IOSException(e);
		}
		
		if (rc != 0) {
			throw new IOSException("The XC command was " +
					"unsuccessful");
		}*/
	}

    private static String[] getCommand(String input) {
        StringTokenizer tokenizer = new StringTokenizer(input);
        String[] result = new String[tokenizer.countTokens()];
        for (int i = 0; tokenizer.hasMoreTokens(); i++) {
            result[i] = tokenizer.nextToken();
        }
        return result;
    }

}
