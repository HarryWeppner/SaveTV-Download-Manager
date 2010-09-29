package com.ingo.savetv;

public class SaveTVResponseException extends Exception {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5458702937699321156L;
	
    public SaveTVResponseException(){
    	super();
    }
    
    public SaveTVResponseException(String message){
    	super(message);
    }
	
}
