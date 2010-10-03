
package com.ingo.savetv;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HttpContext;

import com.ingo.savetv.data.Recording;
import com.ingo.savetv.data.RecordingManager;
import com.ingo.savetv.data.RecordingManagerFactory;


public class DownloadManager {
	
	private DefaultHttpClient _client;
	private HttpEntity _entity;
	private static final Log LOG = LogFactory.getLog(DownloadManager.class);;
	private static RecordingManager _rcm = null;
	
	
	private void createHTTPClient(){
        // Create and initialize HTTP parameters
        HttpParams params = new BasicHttpParams();
        ConnManagerParams.setMaxTotalConnections(params, 100);
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        
        // Create and initialize scheme registry 
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(
                new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        schemeRegistry.register(
        		new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
        
        // Create an HttpClient with the ThreadSafeClientConnManager.
        // This connection manager must be used if more than one thread will
        // be using the HttpClient.
        ClientConnectionManager cm = new ThreadSafeClientConnManager(params, schemeRegistry);
		
		 _client = new DefaultHttpClient(cm, params);
		
	}
	
	public DownloadManager(){
		
		this.createHTTPClient();
		
	}
	
	public DownloadManager(final String proxyhost, final int proxyport) throws NoSuchAlgorithmException, KeyManagementException {
		
		this.createHTTPClient();		
				
		/* TrustAllManager trustManager = new TrustAllManager();
		// set the ssl contentext for the Charles Proxy software to work
		SSLContext sslc = SSLContext.getInstance("TLS");
		sslc.init(null, new TrustManager[]{ trustManager }, null);
		SSLSocketFactory sf = new SSLSocketFactory(sslc);
		sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        _client.getConnectionManager().getSchemeRegistry().register(new Scheme("https", sf, 443)); */
	 
	   	 _client.setRoutePlanner(new HttpRoutePlanner() {
	    	 		public HttpRoute determineRoute(
	    	 				HttpHost target,HttpRequest request, HttpContext context) throws HttpException {
	    	 			return new HttpRoute(target, null, new HttpHost(proxyhost, proxyport),
	    	 					"https".equalsIgnoreCase(target.getSchemeName()));
	    	 			}
		 			});
	}
	
	
	/**
	 * The method used to log on to save.tv. Currently it takes a username and password only. the URL that has
	 * to be called is fixed inside the method.
	 * 
	 * @param username
	 * @param password
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	private boolean logonToSaveTV(String username, String password) throws ClientProtocolException, IOException{
		
		 boolean logon_succeeded = true;
		 
	     List<NameValuePair> formparams = new ArrayList<NameValuePair>();
		 formparams.add(new BasicNameValuePair("sUsername", username ));
		 formparams.add(new BasicNameValuePair("sPassword", password));
		 formparams.add(new BasicNameValuePair("image.x", "8"));
		 formparams.add(new BasicNameValuePair("image.y", "10"));
		 formparams.add(new BasicNameValuePair("image", "Login"));
		 UrlEncodedFormEntity ent = new UrlEncodedFormEntity(formparams, "UTF-8");	
		 HttpPost httppost = new HttpPost("https://www.save.tv/STV/M/Index.cfm?sk=PREMIUM");
		 httppost.setEntity(ent); 
		 
		 HttpResponse response = _client.execute(httppost);
		 _entity = null;
		 _entity = response.getEntity();
		 Header[] h = response.getHeaders("location");
		 if(h!= null){
			 // if this is coming back the logon was unsuccessful
			 // location /STV/S/obj/user/usShowLogin.cfm?DL=&&sToken=ErrorCodeID_49	
			String logon = h[0].getValue().toLowerCase();
			int pos = logon.indexOf("=errorcodeid_49");
			if(pos > 0)
				logon_succeeded = false;
		 }
		  
		if(_entity != null){
		   BufferedReader reader = new BufferedReader(new InputStreamReader(_entity.getContent()));
		   while(reader.readLine() != null);
		   	reader.close();
		}
		return logon_succeeded;
		
	}
	
	private String executeGet(String uri) throws IOException {
		
		StringBuffer sb = new StringBuffer();
		HttpGet get = new HttpGet(uri);
		
		this._entity = null;
		
		HttpResponse res = _client.execute(get);
		this._entity = res.getEntity();
		if(this._entity != null){
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(_entity.getContent()));
			String inputLine;

			while((inputLine = reader.readLine()) != null)
				sb.append(inputLine);
			reader.close();	

		}
		return sb.toString();
		
		
	}
	
    /**
     * Makes a request to Save.TV to get the download URL for a recording that was previously found in the HTTP
     * Website.
     * @param recording
     * @return
     * @throws ClientProtocolException
     * @throws IOException
     */
	private String getDownloadURL(Recording recording) throws SaveTVResponseException, ClientProtocolException, IOException {
		
		// build the download form for the give recording
		List<NameValuePair> formparams = new ArrayList<NameValuePair>();
		formparams.add(new BasicNameValuePair("ajax", "true"));
		formparams.add(new BasicNameValuePair("clientAuthenticationKey", ""));
		formparams.add(new BasicNameValuePair("callCount", "1"));
		formparams.add(new BasicNameValuePair("c0-scriptName", "null"));
		formparams.add(new BasicNameValuePair("c0-methodName", "GetDownloadUrl"));
		formparams.add(new BasicNameValuePair("c0-id", "1428_1284926380636"));
		formparams.add(new BasicNameValuePair("c0-param0", "number:" + recording.getId()));
		if((recording.getType() == Recording.H264_STANDARD) || recording.getType() == Recording.DIVX_STANDARD)    
		    formparams.add(new BasicNameValuePair("c0-param1", "number:0"));
		else
		    formparams.add(new BasicNameValuePair("c0-param1", "number:1"));
		if(recording.isAddfree())
			formparams.add(new BasicNameValuePair("c0-param2", "boolean:true"));
		else
			formparams.add(new BasicNameValuePair("c0-param2", "boolean:false"));
		formparams.add(new BasicNameValuePair("xml", "true"));
		formparams.add(new BasicNameValuePair("extend", "function (object) { for (property in object) { this[property] = object[property];  }  return this;}"));
		UrlEncodedFormEntity ent = new UrlEncodedFormEntity(formparams, "UTF-8");	
		HttpPost httppost = new HttpPost("http://www.save.tv/STV/M/obj/cRecordOrder/croGetDownloadUrl.cfm?null.GetDownloadUrl");
		httppost.setEntity(ent); 
		
		StringBuffer sb = new StringBuffer();
		String inputLine = "";
		
		this._entity = null;
		
		HttpResponse response = _client.execute(httppost);
		LOG.debug("Retrieval complete. Start looking into http body to find the URL");
		this._entity = response.getEntity();
		if(this._entity != null){
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(_entity.getContent()));

			while((inputLine = reader.readLine()) != null)
				sb.append(inputLine);
			reader.close();	
		    
		    inputLine = sb.toString();
		    sb = null;
		    int start = inputLine.indexOf("http://");
		    int end = inputLine.indexOf("m=dl", start);
		    if(start > 0 && end > 0){
		    	inputLine = inputLine.substring(start, end + 4); 
				LOG.info("Found download URL " + inputLine); // + " at start = " + start + " and end = " + end);
		    } else {
		    	start = inputLine.indexOf("NOK");
		    	end   = inputLine.indexOf(".'", start);
		    	if(start > 0 && end > 0)
	    	        inputLine = inputLine.substring(start,end);
		        LOG.error("No URL found in response body. The response from the server was " + inputLine );
			    throw new SaveTVResponseException("The response from SaveTV did not contain a download URL it was: " + inputLine);
		    }
			
		} else {
			LOG.error("The response body was empty.");
			throw new SaveTVResponseException("The response body was when trying to find the download URL was empty.");
		}
		return inputLine;
	}
	
	public boolean isAddFreeAvailable(String recordingid) throws IllegalStateException, IOException {
		
		LOG.debug("Checking to see if there is an add free version available for ID: " + recordingid);
		List<NameValuePair> formparams = new ArrayList<NameValuePair>();
		formparams.add(new BasicNameValuePair("ajax", "true"));
		formparams.add(new BasicNameValuePair("clientAuthenticationKey", ""));
		formparams.add(new BasicNameValuePair("callCount", "1"));
		formparams.add(new BasicNameValuePair("c0-scriptName", "null"));
		formparams.add(new BasicNameValuePair("c0-methodName", "GetAdFreeAvailable"));
		formparams.add(new BasicNameValuePair("c0-id", "4127_12795177172"));
		formparams.add(new BasicNameValuePair("c0-param0", "number:" + recordingid));
		formparams.add(new BasicNameValuePair("xml", "true"));
		formparams.add(new BasicNameValuePair("extend", "function (object) { for (property in object) { this[property] = object[property];  }  return this;}"));
		UrlEncodedFormEntity ent = new UrlEncodedFormEntity(formparams, "UTF-8");	
		HttpPost httppost = new HttpPost("http://www.save.tv/STV/M/obj/cRecordOrder/croGetAdFreeAvailable.cfm?null.GetAdFreeAvailable");
		httppost.setEntity(ent); 
		 
		HttpResponse response = _client.execute(httppost);
		_entity = null;
		_entity = response.getEntity();
		StringBuffer sb = new StringBuffer();
		String inputLine = null;
		
		boolean addfreethere = false;
		
		if(_entity != null){
			BufferedReader reader = new BufferedReader(new InputStreamReader(_entity.getContent()));
			String lineout = null;
			while((lineout = reader.readLine()) != null)
			      sb.append(lineout);
			reader.close();
			
			inputLine = sb.toString();
			sb = null;
			int start = inputLine.indexOf("_4127_12795177172 = '1';");
			if(start > 0){
				LOG.info("Found an add free version for recording ID: " + recordingid);
				addfreethere =  true;
			} else {
				LOG.info("No add free version avaiable for ID: " + recordingid + " skiping recording for mow ");
			}
		}
		return addfreethere;
	}
	
		
	public boolean deleteVideo(String recordingid) throws IllegalStateException, IOException{
	     
		LOG.info("Start: delete recording at Save.TV with ID: " + recordingid);
		List<NameValuePair> formparams = new ArrayList<NameValuePair>();
		formparams.add(new BasicNameValuePair("iFilterType", "1"));
		formparams.add(new BasicNameValuePair("sText", ""));
		formparams.add(new BasicNameValuePair("iTextSearchType", "1"));
		formparams.add(new BasicNameValuePair("ChannelID", "0"));
		formparams.add(new BasicNameValuePair("TVCategoryID", "0"));
		formparams.add(new BasicNameValuePair("TVSubCategoryID", "0"));
		formparams.add(new BasicNameValuePair("iRecordingState", "1"));
		formparams.add(new BasicNameValuePair("iPageNumber", "1"));
		formparams.add(new BasicNameValuePair("sSortOrder", ""));
		formparams.add(new BasicNameValuePair("ITelecastID", recordingid));
		UrlEncodedFormEntity ent = new UrlEncodedFormEntity(formparams, "UTF-8");	
		HttpPost httppost = new HttpPost("http://www.save.tv/STV/M/obj/user/usShowVideoArchive.cfm");
		httppost.setEntity(ent); 
		 
		HttpResponse response = _client.execute(httppost);
		_entity = null;
		_entity = response.getEntity();
		if(_entity != null){
			BufferedReader reader = new BufferedReader(new InputStreamReader(_entity.getContent()));
			while(reader.readLine() != null);
			reader.close();
		}
		
		LOG.info("End: delete recording at Save.TV done.");
		return true;
	}
	
	
	
    /**
     * Start a new download session with username and password of a given user at save.tv and a directory to download
     * the newly found videos to. The method keeps track in an internal DB of what was downloaded in previous sessions\
     * already and only downloads the new stuff that was recorded on save.tv
     * @param username	Username @ Save.tv
     * @param password  Password @ Save.tv
     * @param directory Local Directory to download the new videos to.
     * @param delete	if set to true the recording on Save.TV will be deleted after successful download
     * @param thrads    number of threads for simultaneous download
     * @param cut		try to download the recording with the applied cutlist that has the ads already removed
     * @param mobile    also try to find the mobile version of the recording and download it aswell.
     */
	public void start(Parameter pm){
		try {
			
			// first we need to find out if the directory to download the files to does alrady exist. if not we
			// simply crate it.
			if(!(new File(pm.getDownloadDirectory()).exists()))
				new File(pm.getDownloadDirectory()).mkdir();
			
			// log which database we are going to use
			if(pm.getDbUsed() == 1){
				LOG.info("Using HSQLDB to store the information for the already downloaded recordings");
			} else {
			    LOG.info("Using MYSQL to store the information for the already downloaded recordings");	
		    }
			
			// get the initial page from SAVETV. This is necessary as SaveTV writes a header entry back that is used as
			// session information throughout the logon to the website. Unfortunately this entry is only written when one
			// logges on to the main page
			LOG.debug("Start: Calling the main page of Save.tv to get the session informaiton back in the header");
			executeGet("http://www.save.tv/STV/S/index.cfm?");
			LOG.debug("End: Calling the main page of Save.tv");
			
			// Log on to the system itself by providing username and password
			LOG.info("Login in to Save.tv with user " + pm.getUsername());
			if(logonToSaveTV(pm.getUsername(), pm.getPassword())){
				
				LOG.debug("Login in to Save.tv comlete");
			
				// get the Archive of already recorded videos for the logged on user
				LOG.debug("Retrieving list of recorded videos availiable on Save.tv for user " + pm.getUsername());
				String content = executeGet("http://www.save.tv/STV/M/obj/user/usShowVideoArchive.cfm");
				LOG.debug("Retrieving list of recordings for user " + pm.getUsername() + " complete");
			
				// Instantiate the RecordingManager that locally keeps track of what recordings have been downloaded already
				// so they do not get downloaded time and again. Compare the list that came back from save.tv with the recordings
				// that are already downloaded and only add the ones that are new to the list of recordings to download.
				LOG.debug("Initialize Recording Manager");
				_rcm = RecordingManagerFactory.getInstance(pm.getDbUsed());
				List<Recording> recordings = _rcm.findNewRecordings(content, pm.getMobileVersion(), pm.isCut(), this);
				// set the content to null so it can be removed
				content = null;
				LOG.debug("Initialize Recording Manager complete");
			
				if(recordings.size() > 0){
                    boolean removeFromList = false;
					// Loop over all the recordings that we want to download now and that match the parameters there where
					// specified on the commandline.
					LOG.info("Looking for download URLs for new recordings");
					for(Recording recording : recordings){
						if(recording.isDownloadNow()){
							try {					   
								recording.setDownloadURL(getDownloadURL(recording));
								_rcm.insert(recording);
							} catch(SaveTVResponseException stvex){
								LOG.debug(stvex.getMessage());					   
                                removeFromList = true;
							}
						} else {
						// just write the newly found entry that is not yet ready for download to the db. This is used
						// so that we can overcome the shortcoming of Save.TV to provide a cutlist for each recording
							   _rcm.insert(recording);
							   removeFromList = true;
						}
						// remove the recording from the list of recording that are to be downloaed either because there was an error
						// finding the download url or because -cut was specified and there was actually no cutlist available at the moment
						if(removeFromList){
							// since there was an error we in finding the download url we need to remove the recording from the list again.
							Iterator<Recording> it = recordings.iterator(); 
							while(it.hasNext()){
								Recording rec = it.next();
								if(rec.getId().equals(recording.getId())){
									it.remove();
									break;
								}
							}
							
						}
					}	
					
					// Create a new ThreadScheduler that will initially start the number of threads given, restarting a new
					// one as soon as an old one finishes up. This is done as long as there are recordings to download
					if(recordings.size() > 1)
					  LOG.info("Starting to download with " + pm.getNumberOfDownloadThreads() + " threads simultanously");
					else
					  LOG.info("Starting to download the one recording that was found.");
					
					ThreadScheduler scheduler = new ThreadScheduler(_client, recordings, pm.getDownloadDirectory(), pm.getDbUsed());
					// start the number of threads given in the arguments of the application
					scheduler.start(pm.getNumberOfDownloadThreads());
					
					LOG.debug("Check whether the download thereads are still running");
					// loop and sleep with this thread until all the download threads are done.
					while(!scheduler.allThreadsDone()){
						LOG.debug("Sleeping 1 Minute before checking again if all threads are done");
						Thread.sleep(60000);
					}
					LOG.info("Downloading finished");
			
				} else {
					LOG.info("No new recordings where found. Nothing to do here");
				}

				// close everything and finish up.
				LOG.debug("Closing all download resources");
				_rcm.close();
				_client.getConnectionManager().shutdown();
				
			} else {
				LOG.info("Username or password are not correct");
				
			}
		
	  } catch (Exception e){
			LOG.debug("Exception occured. Message was " + e.getMessage());
			e.printStackTrace();
		}
		
	}
		
	
	static class GetRecording extends Thread{
		
		private final HttpClient _httpClient;
		private final ThreadScheduler _scheduler;
		private final String _downloadURL;
		private final String _folder;
		private String _id;
		private int _type;

		/**
		 * 
		 * @param httpClient
		 * @param scheduler
		 * @param downloadurl
		 * @param folder
		 */
		public GetRecording(HttpClient httpClient, ThreadScheduler scheduler, Recording recording, String folder){
			this._httpClient = httpClient;
			this._downloadURL = recording.getDownloadURL();
			this._folder = folder;
			this._scheduler = scheduler;
			this._id = recording.getId();
			this._type = recording.getType();
		}
		
		public String getRecordingId(){
			return _id;
		}
		
		/**
		 * Download the video from the URL that is entered and save it to the directory that is specified. The name
		 * of the video is coming back in the HTTP header and is reused as is. If no filename is found the method currently
		 * tries to download the video to a file called no_filename_found.mp4. If there already is a file with that name
		 * an exception is thrown. This has to be fixed so there will be an incremental number assigned to that download file
		 * However since save.tv is probably always sending he filename chances are very small that this pieces of code is ever
		 * executed.
		 */
        @Override
        public void run() {
        	
        	HttpGet get;
        	RandomAccessFile out = null;
    		BufferedInputStream bis = null;
    		String filename = null;
    		int    downloadstatus = ThreadScheduler.ABORDET;
    		
    		// start downloading the video to the specified directory
  		    get = new HttpGet(_downloadURL);
    		try {
    		  
    		  HttpResponse response = _httpClient.execute(get);
    		  HttpEntity _entity = response.getEntity();
    		  Header[] h = response.getHeaders("Content-Disposition");
              String headervalue = h[0].toString();
              headervalue.indexOf("=");
              filename = headervalue.substring(headervalue.indexOf("=") + 1, headervalue.length());
              
              // append the word Mobile at the begining of all the mobile files that we download
              if(_type == Recording.H264_MOBILE)
            	  filename = "Mobile_" + filename;
              
              // let's check if the file exist alread on the harddrive. That is usually a partial download
              // as with complete downloads being marked in the database we would not get here with a completed
              // download
              if(filename != null){		  
           	  	  filename = _folder + "/" + filename;
    		  } else {
	    		  Random generator = new Random();
	    		  filename = _folder + "/no_filename_found_ " + generator.nextInt(99999) + ".mp4";
	    	  }
              
              File f = new File(filename);
              long filesize = 0;
              
              if(f.exists()){
            	  LOG.debug("run - The file we are trying to write to does already exist. Continue download from where we left of");
                  filesize = f.length();
                  // abord the previous request that was only used to read the filename. Now that we have
                  // the current local size we can go and initiate a new request that let's us start from 
                  // where we left off the download the last time.
                  get.abort();
                  get = new HttpGet(_downloadURL);

                  
                  get.addHeader("Range", "bytes=" + filesize + "-");
                  LOG.debug("Abordet old HTTP connection and create a new one starting at " + filesize);
                  response = _httpClient.execute(get);
                     
                  _entity =  response.getEntity();        
                  LOG.debug("run - Got new HTTP connection starting to append to the existing file");
               }
              
               bis = new BufferedInputStream(_entity.getContent());

            	 
            	out = new RandomAccessFile(filename,"rw");
            	out.seek(filesize); 
            	 
	    		int offset  = 0;
	    		int len = 4096;
	    		int bytes = 0;
	    		byte[] block = new byte[len];
	    		LOG.debug("run - Starting to stream to the file from the HTTP client connection");
	    		while((bytes = bis.read(block, offset,len)) > -1){
	    		    out.write(block,0,bytes);
	    		}
	    		LOG.debug("run - Download is complete, going to call the ThreadSheduler telling him that we are done");
    		  
    		    downloadstatus = ThreadScheduler.FINISHED_SUCCESSFUL;
    		
    		} catch (FileNotFoundException fex){
                get.abort();
                downloadstatus = ThreadScheduler.ABORDET;
    			LOG.error("Some problem with the file we where trying to write the recording to " + fex.getMessage());
    		} catch (ClientProtocolException e) {
    			get.abort();
    			downloadstatus = ThreadScheduler.ABORDET;
    			LOG.error("Some problem with the http client connection " + e.getMessage());
    		} catch (IOException iox) {
    			get.abort();
    			downloadstatus = ThreadScheduler.ABORDET;
    			LOG.error("Some other problem that caused an IO Exception " + iox.getMessage());   			
    		} finally {
    			
    			try {
    				if(out != null) out.close();
    				if(bis != null) bis.close();
				} catch (IOException e) { }
				
				LOG.debug("run - Calling the thread finished method on the theread scheduler");
				_scheduler.finished(downloadstatus,_id);
    		}
    		
        }
	}
}
