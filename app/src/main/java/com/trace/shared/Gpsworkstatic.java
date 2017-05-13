package com.trace.shared;

import java.util.Date;





public class Gpsworkstatic  implements java.io.Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -5764921894545906193L;
	/**
	 * 
	 */
	
	
	 private Integer userid;  
	 private Long    realtime;
	 private Date gpsdatetime;
	 private Integer apdata;
	 private Integer apdatasignlevel;
	 private Integer scanap;
	 private Integer opengps;
	 private Integer opengpscanuse ;
	 private Integer gpsstart;
	 private Integer gpsinsert;
	 private Integer wifiinsert;
	 private Integer gpscanuse;
	 private Integer mapcount;
	 private String  checked;
	public Integer getUserid() {
		return userid;
	}
	public void setUserid(Integer integer) {
		this.userid = integer;
	}

	public Long getRealtime() {
		return realtime;
	}
	public void setRealtime(Long realtime) {
		this.realtime = realtime;
	}
	
	public Date getGpsdatetime() {
		return gpsdatetime;
	}
	public void setGpsdatetime(Date gpsdatetime) {
		this.gpsdatetime = gpsdatetime;
	}
	public Integer getApdata() {
		return apdata;
	}
	public void setApdata(Integer apdata) {
		this.apdata = apdata;
	}
	public Integer getApdatasignlevel() {
		return apdatasignlevel;
	}
	public void setApdatasignlevel(Integer apdatasignlevel) {
		this.apdatasignlevel = apdatasignlevel;
	}
	public Integer getScanap() {
		return scanap;
	}
	public void setScanap(Integer scanap) {
		this.scanap = scanap;
	}
	public Integer getOpengps() {
		return opengps;
	}
	public void setOpengps(Integer opengps) {
		this.opengps = opengps;
	}
	public Integer getOpengpscanuse() {
		return opengpscanuse;
	}
	public void setOpengpscanuse(Integer opengpscanuse) {
		this.opengpscanuse = opengpscanuse;
	}
	public Integer getGpsstart() {
		return gpsstart;
	}
	public void setGpsstart(Integer gpsstart) {
		this.gpsstart = gpsstart;
	}
	public Integer getGpsinsert() {
		return gpsinsert;
	}
	public void setGpsinsert(Integer gpsinsert) {
		this.gpsinsert = gpsinsert;
	}
	public Integer getWifiinsert() {
		return wifiinsert;
	}
	public void setWifiinsert(Integer wifiinsert) {
		this.wifiinsert = wifiinsert;
	}
	public Integer getGpscanuse() {
		return gpscanuse;
	}
	public void setGpscanuse(Integer gpscanuse) {
		this.gpscanuse = gpscanuse;
	}
	public String getChecked() {
		return checked;
	}
	public void setChecked(String checked) {
		this.checked = checked;
	}
	public Integer getMapcount() {
		return mapcount;
	}
	public void setMapcount(Integer mapcount) {
		this.mapcount = mapcount;
	}
	
	 

}
