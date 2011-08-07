package com.ingo.savetv;

import com.ingo.savetv.data.RecordingManager;
import com.ingo.savetv.main.SaveTVDownloadManagerMain;

public class Parameter {
	
	private static String _username;
	private static String _password;
	private static String _proxyhost;
	private static String _proxyport;
	private static String _downloaddirectory = "downloads";
	private static String _tmpdirectory = ".tmp";
	private static int _number_of_downlaodthreads = 3;
	private static boolean _deleterecordingsafterdownload = false;
	private static boolean _proxyset;
	private static boolean _parameterok = false;
	private static boolean _cut;
	private static boolean _mobileversion = false;
	private static String _loglevel;
	private static boolean _initialized = false;
	private static int _dbused = RecordingManager.HSQLDB;
	
	private static class ParameterHolder {
		public static final Parameter INSTANCE = new Parameter();
	}
	
	public static Parameter getInstance(){
		return ParameterHolder.INSTANCE;
	}
	
	private Parameter(){
	}
	
	public static boolean isInitialized(){
		return _initialized;
	}
	
	public static void initialize(String[] args){
		
		// loop through all the parameter to find and set them
		for(int i = 0; i < args.length; i++){
			if(args[i].equalsIgnoreCase("-VERSION")){
				SaveTVDownloadManagerMain.printVersion();
			}
			if(args[i].equalsIgnoreCase("-USERNAME")){
				_username = args[i + 1];
				i++;
				continue;
			}
			if(args[i].equalsIgnoreCase("-PASSWORD")){
				_password = args[i + 1];
				i++;
				continue;
			}
			if(args[i].equalsIgnoreCase("-CUT")){
				_cut = true;
				continue;
			}
			if(args[i].equalsIgnoreCase("-DOWNLOADTO")){
				_downloaddirectory = args[i + 1];
			    String fileseparator = System.getProperty("file.separator");
				_tmpdirectory = args[i + 1] + fileseparator + _tmpdirectory;
				i++;
				continue;
			}
			if(args[i].equalsIgnoreCase("-LOGLEVEL")){
				_loglevel = args[i + 1].toUpperCase();
				i++;
				continue;
			}
			if(args[i].equalsIgnoreCase("-THREADS")){
				_number_of_downlaodthreads = Integer.parseInt(args[i + 1]);
				i++;
				continue;
			}			
			if(args[i].equalsIgnoreCase("-PROXYHOST")){
				_proxyhost = args[i + 1];
				i++;
				continue;
			}
			if(args[i].equalsIgnoreCase("-PROXYPORT")){
				_proxyport = args[i + 1];
				i++;
				continue;
			}
			if(args[i].equalsIgnoreCase("-MOBILE")){
				_mobileversion = true;
				continue;
			}
			if(args[i].equalsIgnoreCase("-USEDB")){
				if(args[i + 1].equalsIgnoreCase("MYSQL")){
				   _dbused = RecordingManager.MYSQLDB;
				} else {
				   _dbused = RecordingManager.HSQLDB;
				}
				i++;
				continue;
			}
			if(args[i].equalsIgnoreCase("-DELETE_ON_SAVETV")){
				_deleterecordingsafterdownload = true;
				continue;
			}
			System.out.println("The parameter " + args[i] + " was not undertood.");
			_parameterok = false;
			return;
		}
		if(_username == null | _password == null){
			System.out.println("Username and password are not specified correctly please use parameter -username and -password");
		    _parameterok = false;
		} else {
			_parameterok = true;
		}
		if(_proxyhost == null ^ _proxyport == null){
			
			_proxyset = false;
			_parameterok = false;
			if(_proxyhost == null)
				System.out.println("-proxyhost was specified but not -proxyport both are needed however");
			else
				System.out.println("-proxyport was specified but not -proxyport both are needed however");
			

		} else if(_proxyhost == null && _proxyport == null){
			_proxyset = false;
		} else {
			_proxyset = true;
		}
		_initialized = true;
	}
	
	public static boolean parameterOk(){
		return _parameterok;
	}
	
	public static String getUsername() {
		return _username;
	}

	public static String getPassword() {
		return _password;
	}

	public static String getProxyHost() {
		return _proxyhost;
	}

	public static String getProxyport() {
		return _proxyport;
	}

	public static String getDownloadDirectory() {
		return _downloaddirectory;
	}
	
	public static String getTmpDirectory(){
		return _tmpdirectory;
	}
	
	public static boolean isDeleteRecordings(){
		return _deleterecordingsafterdownload;
	}
	
	public static int getNumberOfDownloadThreads(){
		return _number_of_downlaodthreads;
	}
	
	public static boolean isProxySet(){
	   return _proxyset;	
	}

	public static boolean isCut(){
		return _cut;
	}
	
	public static String getLogLevel(){
		return _loglevel;
	}
	
	public static boolean getMobileVersion(){
		return _mobileversion;
	}
	
	public static int getDbUsed(){
		return _dbused;
	}
	

}
