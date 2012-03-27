package com.brewinapps.maven.plugins.ios;

/**
 * 
 * @author Brewin' Apps AS
 */
public class AutopilotException extends Exception {
	private static final long serialVersionUID = -41072478151566153L;
	
	/**
	 * 
	 * @param e
	 */
	public AutopilotException(Exception e) {
		this.initCause(e);
	}
	
	/**
	 * 
	 * @param msg
	 */
	public AutopilotException(String msg) {
		this.initCause(new Exception(msg));
	}
}
