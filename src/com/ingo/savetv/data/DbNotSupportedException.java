package com.ingo.savetv.data;

public class DbNotSupportedException extends Exception {
		
	/**
	 * 
	 */
	private static final long serialVersionUID = 2740686533160639895L;
	
	public DbNotSupportedException(){
		super();
	}
	
	public DbNotSupportedException(String message){
		super(message);
	}

}
