package com.ingo.savetv.junit;

import java.sql.SQLException;
import java.util.Date;

import junit.framework.TestCase;

import com.ingo.savetv.data.Recording;
import com.ingo.savetv.data.RecordingManager;
import com.ingo.savetv.data.RecordingManagerFactory;


public class HSQLDBTest extends TestCase {
	
	private Recording rec = new Recording();
	private RecordingManager _rcm = RecordingManagerFactory.getInstance(RecordingManager.HSQLDB);
	
	public HSQLDBTest(String name){
		super(name);
		rec.setId("123456");
		rec.setAddfree();
		rec.setComplete();
		rec.setDownloadnow();
		rec.setDescription("This is a simple descriptino for a recording");
		rec.setDownloadURL("http://cs53.save.tv/DL0030000399930003993?");
		rec.setFilename("The_filename_of_the_recording_that_was_downloaded.mp4");
		Date d = new Date();
		rec.setFirstTried(d);
		rec.setTitle("Krieg und Frieden");
		rec.setType(Recording.H264_STANDARD);
		try {
		  _rcm.clean();
		} catch (SQLException sqlx){
			System.out.println(sqlx);
		}
	}

	 /**
	  * setUp() method that initialized common objects
	  */
	protected void setUp() throws Exception {
		super.setUp();

	}
	
	/**
	 * tearDown() method that cleanup the common objects
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	public void testInsert(){

		try {
		  _rcm.insert(rec);
		} catch (SQLException sqlx){
			System.out.println(sqlx);
			
		}
		
		Recording recording = _rcm.find(rec.getId(), rec.getType());
		
		assertTrue("Expected non-null result", recording != null);
		assertEquals("Wrong URL", "http://cs53.save.tv/DL0030000399930003993?", recording.getId());
		assertEquals("Wrong DownloadNow", true, recording.isDownloadnow());
		
		
	}
	
	public void testUpdate(){
		
	    rec.setTitle("Nochmal ein Test");
	    try {
	      _rcm.update(rec);
	    } catch (SQLException sqlx){
	    	System.out.println(sqlx);
	    }
	    
	    Recording recording = _rcm.find(rec.getId(), rec.getType());
	    
	    assertTrue("Expected non-null result", recording != null);
	    assertEquals("Wrong Title ", "Nochmal ein Test", recording.getTitle());
		
	}
}
