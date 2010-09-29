package com.ingo.savetv.data;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ingo.savetv.DownloadManager;

public class MySQLRecordingManager implements RecordingManager {
	
	private static Connection conn;
	private static final Log LOG = LogFactory.getLog(MySQLRecordingManager.class); ;
	private List<Recording> _recordings;
	private static final String _DB = "savetv";
	
    /**
     * The class for using MySQL as a storage for all recorded software is not yet implemented
     * right now the download tool only supports HSQLDB as an internal database that only
     * one process at the time can connect to.
     */
	public MySQLRecordingManager() {
		
		_recordings = new ArrayList<Recording>();
		
		try {
			Class.forName("com.mysql.jdbc.Driver");
			
		} catch (ClassNotFoundException ex){
			return;
		}
		
		try {
			conn = DriverManager.getConnection("jdbc:mysql://192.168.1.10:3306/" + _DB, "savetv", "abcd1234");
	        if(!this.tablesExist())
		           this.initialize();
	        
		} catch (SQLException sqlx){
			LOG.fatal("Fatal error when trying to read local data. The message is " + sqlx.getMessage());
			System.exit(-1);
		}
		
	}
	
	public boolean tablesExist() {
		try {
			  Statement st = conn.createStatement();		
			  st.executeQuery("SELECT count(*) FROM recordings");
			} catch (SQLException sqlx){
				if(sqlx.getErrorCode() == 1146){
				    return false;
				} else {
					LOG.error("Error when trying to read local data. The message is " + sqlx.getMessage() + sqlx.getErrorCode());
				}
			}
			return true;
	}
	
	public boolean initialize(){
	    try {
	    	LOG.info("Inititializing the database");
	    	Statement st = conn.createStatement();
	    	st.executeUpdate("CREATE TABLE recordings( id VARCHAR(20), downloadurl VARCHAR(255), title VARCHAR(100), description VARCHAR(300), filename VARCHAR(100), filetype VARCHAR(1), complete BOOLEAN, PRIMARY KEY(id, filetype))");
			// more statements declaring more tables should go here.

	    	// conn.commit();  // not necessary as we assume MYSQL is installed with default setting and auto commit is on
	    } catch (SQLException sqlx){
	    	sqlx.printStackTrace();
	        return false;
	    }
	    LOG.trace("Initialization of the database is complete");
	    return true;
	}
	
	public boolean isExisting(Recording recording){
		try {
			boolean ret = false;
		    Statement st = conn.createStatement();
		    ResultSet res = st.executeQuery("SELECT COUNT(id) AS rowcount FROM recordings WHERE id ='" + recording.getId() + "' AND filetype = '" + recording.getType() + "' AND complete = TRUE");
		    res.next();
		    if(res.getInt("rowcount") > 0)
		    	ret = true;   	
		    res.close();
		    
		    return ret;   
		} catch (SQLException sqlex){
			LOG.error("Java exception " + sqlex.getMessage() + " was thrown with with SQL message " + sqlex.getSQLState());
			return false;
		}
	}
	
	public boolean remove(Recording recording){
		try {  
			  _recordings.remove(recording);
			  return true;
		} catch (Exception e){
			  return false;
		}
	}
	
	public List<Recording> findNewRecordings(String htmlpage, boolean mobile, boolean cut, DownloadManager dl){
		Pattern MY_PATTERN = Pattern.compile("openWindow\\([0-9]+\\, [0,1]\\, -?[0,1]\\, [0-9]\\)");
		Matcher m = MY_PATTERN.matcher(htmlpage);
		while(m.find()){
			Recording recording = new Recording();
			String s[] = m.group().split("[^0-9]+");
			// set the recording id
			recording.setId(s[1]);
			
			// determine the recording format
			int format = Integer.parseInt(s[4]);
			switch(format){
			   case Recording.H264_STANDARD: recording.setType(Recording.H264_STANDARD); break;
			   case Recording.H264_MOBILE : recording.setType(Recording.H264_MOBILE); break;
			   case Recording.DIVX_STANDARD : recording.setType(Recording.DIVX_STANDARD); break;
			
		    }
			boolean add = false;
			if(!isExisting(recording)){
				// Only use the mobile recoridngs if the mobile parameter was used 
				if((recording.getType() != Recording.H264_MOBILE) || ((recording.getType() == Recording.H264_MOBILE) && mobile)){
				 
					if(s[3].equals("0")){
						LOG.info("Found DIVX verion for recording with ID: " + recording.getId() + " skiping the check for add free version");
						recording.setAddfree(false);
					} else {
						// now that we found out that add free versions are in general available let's make sure there really is
						// already an add free version there.
						try {
							if(cut && dl.isAddFreeAvailable(recording.getId()))
								recording.setAddfree(true);
							else
								recording.setAddfree(false);
						} catch (IOException ex){}
					}
				}	

				if(recording.getType() == Recording.DIVX_STANDARD)
					add = true;
				else {
					if(cut && recording.isAddfree())
						add = true;
					if(!cut && !recording.isAddfree())
					    add = true;
				}				
			}  
			if(add)	  
		       _recordings.add(recording);
		}
		return _recordings; 
	}
	
	public void showAll(){
		for(Recording rec : _recordings){
			System.out.println(rec.getId());
			System.out.println(rec.getDownloadURL());
		}
	}
	
	public void update(List<Recording> recordings) throws SQLException {
		for(Recording recording : recordings){
			if(recording.isComplete()) this.update(recording);
		}
	}
	
	
	public void update(Recording recording) throws SQLException {
		Statement st = conn.createStatement();
		StringBuffer sb = new StringBuffer();
		sb.append("UPDATE recordings SET ");
		if(!(recording.getDownloadURL() == null)){ sb.append(" downloadurl = '"); sb.append(recording.getDownloadURL()); sb.append("' "); }
		if(!(recording.getTitle() == null)){ sb.append(", title = '"); sb.append(recording.getTitle()); sb.append("' "); }
		if(!(recording.getDescription() == null)){ sb.append(", description = '"); sb.append(recording.getDescription()); sb.append("' "); }
		if(!(recording.getFilename() == null)){ sb.append(", filename = '"); sb.append(recording.getFilename()); sb.append("' "); }
		if(!(recording.getFilename() == null)){ sb.append(", filetype = '"); sb.append(recording.getType()); sb.append("' "); }
		if(!recording.isComplete()) sb.append(", complete = FALSE "); else sb.append(", complete = TRUE "); 
		sb.append("WHERE id = '");
		sb.append(recording.getId());
		sb.append("'");
		
		LOG.trace(sb.toString());
		st.execute(sb.toString());
		// conn.commit(); // not necessary as we assume MYSQL is installed with default setting and auto commit is on
	}
	
	public void insert(List<Recording> recordings) throws SQLException{	
		for(Recording recording : recordings)
			insert(recording);
	
	}
	
	public void insert(Recording recording) throws SQLException {
		Statement st = conn.createStatement();
	    StringBuffer sb = new StringBuffer();
		sb.append("INSERT INTO recordings VALUES('");
		sb.append(recording.getId());
		sb.append("', '");
		if(recording.getDownloadURL() == null) sb.append(""); else sb.append(recording.getDownloadURL());
		sb.append("', '");
		if(recording.getTitle() == null) sb.append(""); else sb.append(recording.getTitle());
		sb.append("', '");
		if(recording.getDescription() == null) sb.append("");else sb.append(recording.getDescription());
		sb.append("', '");
		if(recording.getFilename() == null) sb.append("");else sb.append(recording.getFilename());
		sb.append("', '");
		if(recording.getType() == 0 ) sb.append("");else sb.append(recording.getType());
		sb.append("', ");
		if(!recording.isComplete()) sb.append("FALSE "); else sb.append("TRUE "); 
		sb.append(")");
		LOG.trace(sb.toString());
		
		// execute the insert statement
		st.executeUpdate(sb.toString());
		
		// conn.commit(); // not necessary as we assume MYSQL is installed with default setting and auto commit is on
	}
	
	public boolean close(){
		return true;
	}
	
}