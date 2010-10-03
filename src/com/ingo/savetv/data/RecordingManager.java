package com.ingo.savetv.data;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ingo.savetv.DownloadManager;


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
	 *  Searches the website for new recordings. This is currently done with a pretty primitive approach that
	 *  searches for a "openWindow(" string followed by numbers. The first number is the ID of the recording
	 *  the second number is apparently some old format information that seems to be not used any more the
	 *  third one specifies whether there is an addFree version available or at least if there potentially
	 *  is one available and the last number is the format Type
	 */
	public List<Recording> findNewRecordings(String htmlpage, boolean mobile, boolean cut, DownloadManager dl){
		Pattern MY_PATTERN = Pattern.compile("openWindow\\([0-9]+\\, [0,1]\\, -?[0,1]\\, [0-9]\\)");
		Matcher m = MY_PATTERN.matcher(htmlpage);
		while(m.find()){
			Recording recording = new Recording();
			String s[] = m.group().split("[^0-9]+");
			String recordingId = s[1];
			int recordingType = Integer.parseInt(s[4]);;
			
			recording = this.find(recordingId, recordingType);
		    if(recording == null){
		    	recording = new Recording();
		        recording.setId(recordingId);
		        recording.setType(recordingType);
				// Only use the mobile recoridngs if the mobile parameter was used 
				if((recordingType != Recording.H264_MOBILE) || ((recordingType == Recording.H264_MOBILE) && mobile)){
                    LOG.debug("Found recording of type : " + recordingType + " while the mobile switch is set to:" + mobile);
					if(s[3].equals("0")){ 
						LOG.info("Found DIVX verion for recording with ID: " + recordingId + " skiping the check for add free version");
						recording.setDownloadnow();
					} else {
						// now that we found out that add free versions are in general available let's make sure there really is
						// already an add free version there.
						try {
							if(cut && dl.isAddFreeAvailable(recording.getId())){
								recording.setFirstTried(new Date());
								recording.setAddfree();
								recording.setDownloadnow();
							} else {
							 // since there is not add free version available let's find out about the current date and time and store it
							 // in the record so that next time around we can check against this date when the recording should be downloaded
						     // no matter whether there is a recording or not
								recording.setFirstTried(new Date());
							}	
						} catch (IOException ex){}
					}
	                _recordings.add(recording);
				}	
				
			}  else {

			   if(!recording.isComplete()){
				  // check when we tried the first time to download the show but there was no cutlist available.
				  // if this is already 48 hours ago the download anyways
				  if((new Date().getTime() - recording.getFirstTried().getTime()) > TIME_ELAPSED_BEFORE_EVENTUAL_DOWNLOAD){
				      recording.setDownloadnow();
					  _recordings.add(recording);
				      
				  }
			   }
			}

		}
		return _recordings; 
	}

	
	public void showAll(){
		for(Recording rec : _recordings){
			LOG.info(rec.getId());
			LOG.info(rec.getDownloadURL());
		}
	}
	
}
