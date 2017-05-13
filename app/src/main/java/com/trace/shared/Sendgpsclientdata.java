package com.trace.shared;

import java.util.Date;




public class Sendgpsclientdata  implements java.io.Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1671962360656120828L;
	/**
	 * 
	 */
	 
	 private Integer userid;  
	 private Long    realtime;
	 private Date gpsdatetime;
	 private Double longitude ;
	 private Double  latitude;
	 private Float  speed;	
	 private String ttc;

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
	public Double getLongitude() {
		return longitude;
	}
	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}
	public Double getLatitude() {
		return latitude;
	}
	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}
	public Float getSpeed() {
		return speed;
	}
	public void setSpeed(Float speed) {
		this.speed = speed;
	}
	public String getTtc() {
		return ttc;
	}
	public void setTtc(String ttc) {
		this.ttc = ttc;
	}
	
	
	 

}
