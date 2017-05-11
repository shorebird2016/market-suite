package org.marketsuite.component.xml;

public class InvalidResponseException extends Exception {
	public InvalidResponseException(Exception ex){
		super(ex);
	}

	public InvalidResponseException(String msg){
		super(msg);
	}
}
