package com.ingo.savetv.data;

import java.sql.SQLException;
import java.util.List;

import com.ingo.savetv.DownloadManager;


public abstract interface RecordingManager {
	
	public static final int HSQLDB = 1;
	public static final int MYSQLDB = 2;

	public static final  String _DB = "data/savetv";
	
	public abstract boolean tablesExist();
	
	public abstract boolean initialize();
	
	public abstract boolean isExisting(Recording recording);
	
	public abstract boolean remove(Recording recording);
	
	public abstract List<Recording> findNewRecordings(String htmlpage, boolean mobile, boolean cut, DownloadManager dl);
	
	public abstract void update(List<Recording> recordings) throws SQLException;
	
	public abstract void update(Recording recording) throws SQLException;
	
	public abstract void insert(List<Recording> recordings) throws SQLException;
	
	public abstract void insert(Recording recording) throws SQLException;
	
	public abstract boolean close();
	
}
