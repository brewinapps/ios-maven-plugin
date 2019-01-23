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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

import de.letsdev.maven.plugins.ios.mojo.IOSException;

public class CommandHelper {

    /**
     * @param processBuilder
     * @throws IOSException
     */
    public static String performCommand(final ProcessBuilder processBuilder) throws IOSException {
        processBuilder.redirectErrorStream(true);

        StringBuilder joinedCommand = new StringBuilder();
        for (String segment : processBuilder.command()) {
            joinedCommand.append(segment).append(" ");
        }
        System.out.printf("Executing '%s'\n", joinedCommand.toString().trim());

        Process process;
        try {
            process = processBuilder.start();
        } catch (IOException e) {
            throw new IOSException(e);
        }

        BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));

        int rc;
        StringBuilder output = new StringBuilder();
        int readLines = 0;
        try {
            // Display output
            String outLine;
            while ((outLine = input.readLine()) != null) {
                if (readLines > 0) {
                    output.append("\n");
                }
                output.append(outLine);
                System.out.println(outLine);

                readLines++;
            }
            input.close();
        } catch (IOException e) {
            throw new IOSException("An error occured while reading the input stream");
        }

        try {
            rc = process.waitFor();
        } catch (InterruptedException e) {
            throw new IOSException(e);
        }

        if (rc != 0) {
            throw new IOSException("The Xcode command was unsuccessful");
        }

        return output.toString();
    }

    static String[] getCommand(String input) {
        StringTokenizer tokenizer = new StringTokenizer(input);
        String[] result = new String[tokenizer.countTokens()];
        for (int i = 0; tokenizer.hasMoreTokens(); i++) {
            result[i] = tokenizer.nextToken();
        }
        return result;
    }

}
