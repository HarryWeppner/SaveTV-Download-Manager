package com.ingo.savetv.data;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class MySQLRecordingManager extends RecordingManager {
	
	private static final String DB = "savetv";
	private static final String DBHOST = "localhost";
	private static final String DBPORT = "3306";
	private static final String DBUSER = "savetv";
	private static final String DBPASSWORD = "abcd1234";
	
	private static final Log LOG = LogFactory.getLog(MySQLRecordingManager.class);
	
	
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
			conn = DriverManager.getConnection("jdbc:mysql://" + DBHOST + ":" + DBPORT + "/" + DB, DBUSER, DBPASSWORD);
	        if(!this.tablesExist())
		           this.initialize();
	        
		} catch (SQLException sqlx){
			LOG.fatal("Fatal error when trying to read local data. The message is " + sqlx.getMessage());
			System.exit(-1);
		}
		
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
	
	public Recording getRecording(String id, int type){
		try {
			Recording recording = new Recording();
		    Statement st = conn.createStatement();
	        String query = "SELECT * FROM recordings WHERE id = '" + id + "' AND filetype = '" + type + "'";
		    ResultSet res = st.executeQuery(query);
		    if(res.next()){
		    	// return the found record as a whole
		    	recording.setTitle(res.getString("title"));
		    	recording.setDownloadURL(res.getString("downloadurl"));
		    	if(res.getBoolean("complete")) recording.setComplete();
		    	recording.setDescription(res.getString("description"));
		    	recording.setFilename(res.getString("filename"));
		    	recording.setFirstTried(new Date(res.getDate("firsttry").getTime()));
		    } else {
		        recording = null;
		    }
		    res.close();
		    return recording;   
		} catch (SQLException sqlex){
			LOG.error("Java exception " + sqlex.getMessage() + " was thrown with with SQL message " + sqlex.getSQLState());
			return null;
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
		// if(!(recording.getFirstTried() == null)){ sb.append(", firsttried '" )
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
	
	public void insertOrUpdate(Recording recording){
		
	}
	
	public boolean close(){
		return true;
	}
	
}