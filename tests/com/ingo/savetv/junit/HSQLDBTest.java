package com.ingo.savetv.junit;



import static org.junit.Assert.*;

import java.sql.SQLException;
import java.util.Date;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ingo.savetv.data.Recording;
import com.ingo.savetv.data.RecordingManager;
import com.ingo.savetv.data.RecordingManagerFactory;


public class HSQLDBTest {
	
	private static RecordingManager _rcm = RecordingManagerFactory.getInstance(RecordingManager.HSQLDB);
	
<<<<<<< HEAD:tests/com/ingo/savetv/junit/HSQLDBTest.java
=======
	public HSQLDBTest(String name){
		super(name);
	}

>>>>>>> fc76003a7b9552fb936ba6d6478fe2581373f9e2:src/com/ingo/savetv/junit/HSQLDBTest.java
	 /**
	  * setUp() method that initialized common objects
	  */
	@BeforeClass
	protected void setUpClass() throws Exception {
		try {
			_rcm.clean();
		} catch(SQLException e){
		    System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}
	
	/**
	 * tearDown() method that cleanup the common objects
	 */
	@AfterClass
	protected void tearDownClass() throws Exception {
		try {
			_rcm.clean();
		} catch(SQLException e){
		    System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}
	
<<<<<<< HEAD:tests/com/ingo/savetv/junit/HSQLDBTest.java
	
	@Test
	public void insert(){
=======
	public void testInitialize(){
		try {
		   _rcm.clean();
		} catch(SQLException e){
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}
	
	public void testInsert(){
>>>>>>> fc76003a7b9552fb936ba6d6478fe2581373f9e2:src/com/ingo/savetv/junit/HSQLDBTest.java
		
		Recording rec = new Recording();
		
		rec.setId("123456");
		rec.setAddFree(true);
		rec.setDescription("This is a simple descriptino for a recording");
		rec.setDownloadURL("http://cs53.save.tv/DL0030000399930003993?");
		rec.setFilename("The_filename_of_the_recording_that_was_downloaded.mp4");
		Date d = new Date();
		rec.setFirstTried(d);
		rec.setTitle("Krieg und Frieden");
		rec.setType(Recording.H264_STANDARD);
		

		try {
		  _rcm.insert(rec);
		} catch (SQLException e){
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		
		Recording recording = _rcm.find(rec.getId(), rec.getType());
		
		assertTrue("Expected non-null result", recording != null);
		assertEquals("Wrong URL", "http://cs53.save.tv/DL0030000399930003993?", recording.getDownloadURL());
		assertEquals("Wrong Title", "Krieg und Frieden", recording.getTitle());
		
		
	}
	
	@Test
	public void update(){
		
<<<<<<< HEAD:tests/com/ingo/savetv/junit/HSQLDBTest.java
	    Recording recording = _rcm.find("123456", Recording.H264_STANDARD);	
	    if(recording.getId() != null){
	      recording.setTitle("Nochmal ein Test");
	      recording.setComplete();
	      try {
	        _rcm.update(recording);
=======
		Recording rec = new Recording();
		
	    Recording recording = _rcm.find("123456", Recording.H264_STANDARD);	
	    if(recording.getId() != null){
	      rec.setTitle("Nochmal ein Test");
	      rec.setComplete();
	      try {
	        _rcm.update(rec);
>>>>>>> fc76003a7b9552fb936ba6d6478fe2581373f9e2:src/com/ingo/savetv/junit/HSQLDBTest.java
	      } catch (SQLException e){
				System.out.println(e.getMessage());
				e.printStackTrace();
	      }
	    }
	    
	    assertTrue("Expected non-null result", recording != null);
	    assertEquals("Wrong Title ", "Nochmal ein Test", recording.getTitle());
	    assertEquals("Wrong Complete ", true, recording.isComplete());
	}
}
