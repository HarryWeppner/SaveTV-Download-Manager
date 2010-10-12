package com.ingo.savetv.data;


import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public class HSQLDBRecordingManager extends RecordingManager {

	private static final  String _DB = "data/savetv";
	
	private static final Log LOG = LogFactory.getLog(HSQLDBRecordingManager.class);	
	
	public HSQLDBRecordingManager(){
	
		super();
		_recordings = new ArrayList<Recording>();

		
		try {
			Class.forName("org.hsqldb.jdbcDriver");
		} catch (ClassNotFoundException ex){
			return;
		}
		
		try {
			
	        conn = DriverManager.getConnection("jdbc:hsqldb:file:" + _DB, "SA", "");
	        // conn.setAutoCommit(false);
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
	    	st.executeUpdate("CREATE CACHED TABLE recordings( id VARCHAR(20), downloadurl VARCHAR(255), title VARCHAR(100), description VARCHAR(300), filename VARCHAR(100), filetype VARCHAR(1), firsttry TIMESTAMP, complete BOOLEAN, PRIMARY KEY(id, filetype))");
			st.close();
			st = null;
	    	
	    	// more statements declaring more tables should go here.
	    	conn.commit();
	    } catch (SQLException sqlx){
	    	sqlx.printStackTrace();
	        return false;
	    }
	    LOG.trace("Initialization of the database is complete");
	    return true;
	}
	
	public void update(List<Recording> recordings) throws SQLException {
		for(Recording recording : recordings){
			if(recording.isComplete()) this.update(recording);
		}
	}
	
	public Recording find(String id, int type){
		Recording recording = new Recording();
		try {
		    Statement st = conn.createStatement();
	        String query = "SELECT * FROM recordings WHERE id = '" + id + "' AND filetype = '" + type + "'";
		    ResultSet res = st.executeQuery(query);
		    if(res.next()){
		    	// return the found record as a whole
		    	recording.setId(res.getString("id"));
		    	recording.setType(Integer.parseInt(res.getString("filetype")));
		    	recording.setTitle(res.getString("title"));
		    	recording.setDownloadURL(res.getString("downloadurl"));
		    	if(res.getBoolean("complete")) recording.setComplete();
		    	recording.setDescription(res.getString("description"));
		    	recording.setFilename(res.getString("filename"));
		    	recording.setFirstTried(new Date(res.getTimestamp("firsttry").getTime()));
		    } else {
		        recording = new Recording();
		    }
		    res.close();
		    st.close();
		    res = null;
		    st = null;
		      
		} catch (SQLException sqlex){
			LOG.error("Java exception " + sqlex.getMessage() + " was thrown with with SQL message " + sqlex.getSQLState());
		}
		return recording;
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
			if(!(recording.getFirstTried() == null)){ sb.append(", firsttry = '"); sb.append(new java.sql.Timestamp(recording.getFirstTried().getTime())); sb.append("' ");}
			if(!recording.isComplete()) sb.append(", complete = FALSE "); else sb.append(", complete = TRUE "); 
			sb.append("WHERE id = '");
			sb.append(recording.getId());
			sb.append("'");
			
			LOG.debug(sb.toString());
			String strsql = sb.toString();
			st.execute(strsql);
			conn.commit();
			
			st.close();
			st = null;
			

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
			sb.append("', '");
			if(recording.getFirstTried() == null) sb.append(""); else sb.append(new Timestamp(recording.getFirstTried().getTime()));
			sb.append("', ");
			if(!recording.isComplete()) sb.append("FALSE "); else sb.append("TRUE "); 
			sb.append(")");
			LOG.debug(sb.toString());
			
			// execute the insert statement
			st.executeUpdate(sb.toString());
			conn.commit();
			
			st.close();
			st = null;

	}
	
	public void clean() throws SQLException {
			Statement st = conn.createStatement();
			st.executeUpdate("DELETE from recordings");
			conn.commit();
			
			st.close();
			st = null;
	}
	
	public boolean close(){
		try {
			Statement st = conn.createStatement();
			st.execute("SHUTDOWN");	
		} catch (SQLException sqlx){
			LOG.error(sqlx.getMessage());
			return false;
		}

		return true;
	}
	
}