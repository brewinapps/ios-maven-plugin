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

/**
 * 
 * @author let's dev
 */
public class IOSException extends Exception {
	private static final long serialVersionUID = -41072478151566153L;
	
	/**
	 * 
	 * @param e
	 */
	public IOSException(Exception e) {
		super(e);
	}
	
	/**
	 * 
	 * @param msg
	 */
	public IOSException(String msg) {
		super(msg);
	}
}
