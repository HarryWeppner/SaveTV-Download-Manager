package com.ingo.savetv;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.HttpClient;

import com.ingo.savetv.DownloadManager.GetRecording;
import com.ingo.savetv.data.Recording;
import com.ingo.savetv.data.RecordingManager;
import com.ingo.savetv.data.RecordingManagerFactory;

public class ThreadScheduler {
	
	private static HttpClient _client;
	private static List<Recording> _recordings;
	private static List<Recording> _trackkeeper;
	private static String _directory;
	private static final String THREADNAME = "Downloader-";
	private static final Log LOG = LogFactory.getLog(DownloadManager.class);
	private Random _rnd = null;
	private static final int RANDOMBOUNDARY = 999999999;
	
	public static final int FINISHED_SUCCESSFUL = 0;
	public static final int ABORDET = 1;
	private static RecordingManager _rcm = null;
	private int numberOfRunningThreads = 0;


	
	public ThreadScheduler(HttpClient client, List<Recording> recordings, String directory, int dbused){
		
		
		_recordings = recordings;
		_trackkeeper = new ArrayList<Recording>(recordings);
		
		_client = client;
		_directory = directory;
		
		_rnd = new Random();
		
		_rcm = RecordingManagerFactory.getInstance(dbused);
		
	}
	
	/**
	 * Returns true if there are no more threads running
	 * @return  true if all download threads are done.
	 */
	public boolean allThreadsDone(){
		LOG.debug("Number of running threads is " + numberOfRunningThreads);
        if(numberOfRunningThreads <= 0)
            return true;
        else
        	return false;
	}
	
	synchronized public void start(int number_of_threads){
        // start the number of threads concurrently that have been given to the initial start
		if(_recordings.size() > 0){
			// check that the recordings to download is larger than the number of threads to initial start with
			// if it is smaller then there is no reason to use more threads than there are downloads :)
			if(_recordings.size() < number_of_threads){
			       number_of_threads = _recordings.size();
			}
			
			for(int i = 0; i < number_of_threads; i++){
				    Recording recording = _recordings.get(i);
					GetRecording video = new GetRecording(_client, this, recording, _directory);
					
					
					// Give the download thread a name and generate a random number so all download thread are unique
					video.setName(THREADNAME + _rnd.nextInt(RANDOMBOUNDARY));
					LOG.debug("Created new thread for download with name: " + video.getName());
					
					// start the thread with the newly generated name for the download  
					video.start();
					numberOfRunningThreads++;
					LOG.debug("New Thread Started successfully");
			
					// loop over all recordings in the _trackkeeper to find the recording for which we just started a download thread
					// and remove it from the list so the list will eventually be empty.
					Iterator<Recording> it = _trackkeeper.iterator();
					LOG.debug("Trying to find the recording we started a thread for in the internal ist of recordings");
					while(it.hasNext()){
						Recording rec = it.next();
						if(rec.getId().equals(recording.getId()) && rec.getType() == recording.getType()){
						    it.remove();
						    LOG.debug("Found the recording and successfully deleted it from the list");
			                break;
						}
					}
					if(recording.getType() == Recording.DIVX_STANDARD | recording.getType() == Recording.H264_STANDARD)  
					   LOG.info("Download for standard version of recording with ID: " + recording.getId()  + " started successfully");
					else
					   LOG.info("Download for mobile version of recording with ID: " + recording.getId()  + " started successfully");
			}
		}
	}
	
	synchronized public void startNext(){
		// get the next id from the list of recordings and start a thread for it.
		Iterator<Recording> it = _trackkeeper.iterator(); 
		while(it.hasNext()){
			Recording recording = it.next();
			it.remove();
			GetRecording video = new GetRecording(_client, this, recording, _directory);
			video.setName(THREADNAME + _rnd.nextInt(RANDOMBOUNDARY));
			video.start();
			if(recording.getType() == Recording.DIVX_STANDARD | recording.getType() == Recording.H264_STANDARD)  
			   LOG.info("Download for standard recording " + recording.getId()  + " started successfully");
			else
			   LOG.info("Download for mobile recording " + recording.getId()  + " started successfully");
			numberOfRunningThreads++;
			break;
		}
		
	}
	
	synchronized public void finished(int status, String id){
		// if the video downloaded successful remove the video from the list and
		// update the database that the video download was successful
		if(status == FINISHED_SUCCESSFUL){
			
		    LOG.info("The recording with id " + id + " was downloaded sucessfully");
		    // now find the video with the respective ID again and remove it from the list
			// find out which of the threads was successful and update the database accordingly
			for(Recording recording : _recordings){
				if(recording.getId().equals(id)){
				    try{
						recording.setComplete();	
					    _rcm.insert(recording);
				    } catch (SQLException ex){
				    	LOG.error("Error when trying to set the recording with id " + recording.getId() + " to complete.");
				    	LOG.error(ex.getMessage());
				    }
					break;
				}
			}
		}
		numberOfRunningThreads--;
		
		// start the next download as long as there are videos left in the list
		if(_trackkeeper.size() > 0){
			this.startNext();
		}
		
	}
	

}
