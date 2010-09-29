package com.ingo.savetv.data;

public class RecordingManagerFactory {
	
	public static RecordingManager getInstance(int type){
		RecordingManager manager;
		
		if(type == RecordingManager.HSQLDB){
			manager = new HSQLDBRecordingManager();
		} else if(type == RecordingManager.MYSQLDB){
			manager = new MySQLRecordingManager();	
		} else {
			manager = new HSQLDBRecordingManager();
		}
		return manager;
	}

}
