package com.ingo.savetv;

import com.ingo.savetv.data.RecordingManager;
import com.ingo.savetv.main.SaveTVDownloadManagerMain;

public class Parameter {
	
	private String _username;
	private String _password;
	private String _proxyhost;
	private String _proxyport;
	private String _downloaddirectory = "downloads";
	private int _number_of_downlaodthreads = 3;
	private boolean _deleterecordingsafterdownload = false;
	private boolean _proxyset;
	private boolean _parameterok = false;
	private boolean _cut;
	private boolean _mobileversion = false;
	private String _loglevel;
	private int _dbused = RecordingManager.HSQLDB;
	
	public Parameter(String[] args){
		
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
		
	}
	
	public boolean parameterOk(){
		return _parameterok;
	}
	
	public String getUsername() {
		return _username;
	}

	public String getPassword() {
		return _password;
	}

	public String getProxyHost() {
		return _proxyhost;
	}

	public String getProxyport() {
		return _proxyport;
	}

	public String getDownloadDirectory() {
		return _downloaddirectory;
	}
	
	public boolean isDeleteRecordings(){
		return _deleterecordingsafterdownload;
	}
	
	public int getNumberOfDownloadThreads(){
		return _number_of_downlaodthreads;
	}
	
	public boolean isProxySet(){
	   return _proxyset;	
	}

	public boolean isCut(){
		return _cut;
	}
	
	public String getLogLevel(){
		return _loglevel;
	}
	
	public boolean getMobileVersion(){
		return _mobileversion;
	}
	
	public int getDbUsed(){
		return _dbused;
	}
	

}
