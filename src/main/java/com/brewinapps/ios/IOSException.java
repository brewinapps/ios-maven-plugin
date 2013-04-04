package com.brewinapps.ios;

/**
 * 
 * @author Brewin' Apps AS
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
	 * @param e
	 */
	public IOSException(String msg, Exception e) {
		super(msg, e);
	}
	
	/**
	 * 
	 * @param msg
	 */
	public IOSException(String msg) {
		super(msg);
	}
}
