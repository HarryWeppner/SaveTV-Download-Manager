package com.ingo.savetv.data;

import java.util.Date;

public class Recording {

	public static final int DIVX_STANDARD = 1;
	public static final int H264_MOBILE = 4;
	public static final int H264_STANDARD = 5;

	private String _id;
	private String _title;
	private String _filename;
	private int _type;
	private String _description;
	private String _downloadurl;
	private boolean _complete;
	private boolean _addfree;
	private Date _firsttried = null;
	
	public boolean isDownloadnow() {
		return _downloadnow;
	}
	public void setDownloadnow(boolean downloadnow) {
		this._downloadnow = downloadnow;
	}
	private boolean _downloadnow;

	public boolean isAddfree() {
		return _addfree;
	}
	public void setAddfree(boolean addfree) {
		this._addfree = addfree;
	}
	public String getId() {
		return _id;
	}
	public void setId(String id) {
		_id = id;
	}
	public String getTitle() {
		return _title;
	}
	public void setTitle(String title) {
		_title = title;
	}
	public String getFilename() {
		return _filename;
	}
	public void setFilename(String filename) {
		_filename = filename;
	}
	public String getDescription() {
		return _description;
	}
	public void setDescription(String decription) {
		_description = decription;
	}
	public void setDownloadURL(String url){
		_downloadurl = url;
	}
	public String getDownloadURL(){
		return _downloadurl;
	}
	public boolean isComplete(){
		return _complete;
	}
	public void setComplete(){
        _complete = true;		
	}
	public int getType(){
		return _type;
	}
	public void setType(int type){
		_type = type;
	}
	public void setFirstTried(Date date){
		_firsttried = date;
	}
	public Date getFirstTried(){
		return _firsttried;
	}
	

	
}
