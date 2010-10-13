package com.ingo.savetv.data;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public abstract class RecordingManager {


	public static final int HSQLDB = 1;
	public static final int MYSQLDB = 2;

	protected static Connection conn;
	protected List<Recording> _recordings;	
	protected static final Log LOG = LogFactory.getLog(RecordingManager.class);	
	protected static final String _DB = "data/savetv";
	
	private   static final  long TIME_ELAPSED_BEFORE_EVENTUAL_DOWNLOAD = 172800000;


	public abstract boolean initialize();
	
	public abstract void update(List<Recording> recordings) throws SQLException;
	
	public abstract void update(Recording recording) throws SQLException;
	
	public abstract void insert(List<Recording> recordings) throws SQLException;
	
	public abstract void insert(Recording recording) throws SQLException;
	
	public abstract Recording find(String id, int type);
	
	public abstract boolean close();
	
	public abstract void clean() throws SQLException;
	
	public boolean remove(Recording recording){
		try {  
		   _recordings.remove(recording);
		   return true;
		} catch (Exception e){
		   return false;
		}
	}
	
	public boolean tablesExist() {
		try {
		  Statement st = conn.createStatement();		
		  st.executeQuery("SELECT count(*) FROM recordings");
		} catch (SQLException sqlx){
			if(sqlx.getErrorCode() == -22){
			    return false;
			} else {
				LOG.error("Error when trying to read local data. The message is " + sqlx.getMessage());
			}
		}
		return true;
	}
	
	/**
	 * Checks a list of recordings against the database. the input List of recordings is expected to have
	 * the ID and the Type of the recording filled at a minimum for the method to work correctly. The method
	 * tries goes over all recordings trying to find them in the database. If it can find them it checks 
	 * whether the recordings was already successfully downloaded. If yes it removes the recording from the list.
	 * If the recording has not been downloaded we check if there is an entry for the recording availiable alread
	 * 	
	 * @param recordings List of recordings to check against the database
	 * @return recordings List of recordings that should be downloaded 
	 */
	public List<Recording> alignWithDB(List<Recording> recordings, boolean removeUncut){
		Recording returnrec = null;
		
		for(Iterator<Recording> it = recordings.iterator(); it.hasNext(); ){
			Recording recording = it.next();
			returnrec = this.find(recording.getId(), recording.getType());
			if(!returnrec.isComplete()){
				if(returnrec.getId() == null){
					try {
				        this.insert(recording);
				        if(!recording.isAddFree() && removeUncut)
				            it.remove();
					} catch (SQLException e) {
						LOG.error("Error when trying to insert the new recording with id " + recording.getId() + " into the db. The error was " + e.getMessage());
					}
				} else {
			    	// check if the 48 hours timespan from the first try to find a custlist is expired. If yes download
			    	// the recording no matter whether we do have a cutlist or not
			    	if((new Date().getTime() - returnrec.getFirstTried().getTime()) > TIME_ELAPSED_BEFORE_EVENTUAL_DOWNLOAD){
			    	 	LOG.info("Found recording with " + returnrec.getId() + " that after 48 hours still has no cutlist. Adding it to the download list for download without cutlist");
			    	} else {
			    		it.remove();
			    	}  	
				}
				
			} else {
				it.remove();
			}
		}
		return recordings;
	}

	
	public void showAll(){
		for(Recording rec : _recordings){
			LOG.info(rec.getId());
			LOG.info(rec.getDownloadURL());
		}
	}
	
}
