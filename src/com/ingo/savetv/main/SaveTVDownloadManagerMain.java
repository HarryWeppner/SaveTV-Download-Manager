/*
 * Copyright (c) 2004-2010, P. Simon Tuffs (simon@simontuffs.com)
 * All rights reserved.
 *
 * See the full license at http://one-jar.sourceforge.net/one-jar-license.html
 * This license is also included in the distributions of this software
 * under doc/one-jar-license.txt
 */
package com.ingo.savetv.main;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ingo.savetv.DownloadManager;
import com.ingo.savetv.Parameter;

public class SaveTVDownloadManagerMain {
    
	private static final Log LOG = LogFactory.getLog(SaveTVDownloadManagerMain.class);
	private static final String VERSION = "0.9.1.22";
		
	public static void printVersion(){
		System.out.println(VERSION);
	}
	
    public static void main(String args[]) {
        if (args == null)
            args = new String[0];
        
        new SaveTVDownloadManagerMain().run(args);
    }
    
    // Bring up the application: only expected to exit when user interaction
    // indicates so.
    public void run(String args[]) {
    	
        LOG.info("SaveTVDownloadManager Version: " + VERSION + " is starting");
    	try {

    		Parameter pm = new Parameter(args);
      		if(pm.parameterOk()){
     			DownloadManager dl;
      			if(pm.isProxySet())
      				dl = new DownloadManager(pm.getProxyHost(), Integer.parseInt(pm.getProxyport()));
      			else
      				dl = new DownloadManager();
    	
      			// start parsing the application. main entry
      			dl.start(pm);
    		
      		    LOG.info("SaveTVDownloadManager finished ");
      		} else {
    			this.printUsage();
    		}
      		
      		
    	} catch (Exception ex){
    		ex.printStackTrace();
    	}
    }
    
	public void printUsage(){
		System.out.println();
		System.out.print("Usage of Save.tv download manager is:");
		System.out.println();
		System.out.println(" java -jar SaveTVDownloadManager.jar -username <username> -password <password> ");
		System.out.println("                  [-proxyhost <hostname> -proxyport <port>] [-cut] ");
		System.out.println("                  [-threads <number>] [-downloadto <path to download directory>]");
		System.out.println();
		System.out.println();
		System.out.println("-username:    the user on Save.TV to download from");
		System.out.println("-password:    the password for this user");
		System.out.println();
		System.out.println("-proxyhost:   specifies the proxy host if an HTTP proxy is in between you and Save.TV");
		System.out.println("-proxyport:   specifies the port the HTTP proxy is listening on");
		System.out.println();
		System.out.println("-cut:         only downloads if there is an add free version availiable");
		System.out.println();
		System.out.println("-threads:     sets the number of threads for simultaneous downloads. The default is set to 3");
		System.out.println();
		System.out.println("-mobile:      additionally downloads the mobile version of the H264 recordings if present");
		System.out.println();
		System.out.println("-downloadto:  sets the directory to download the videos to. The default setting");
		System.out.println("              is to a directory named \'downloads\' within the current directory");
	}
    

}
