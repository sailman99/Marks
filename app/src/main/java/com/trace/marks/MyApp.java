package com.trace.marks;


import java.text.SimpleDateFormat;
import java.util.TimeZone;
import android.app.Application;


public class MyApp extends Application {
	
	private boolean is_onQuit=true;//手机是否处在静止状�?,初始为静止状�?
	private boolean is_onScreen=true;//手机是否亮屏,初始为亮屏状�?
	private boolean is_requestReStartLocation=false;//是否�?��重新定位,初始为不�?��
	private boolean is_WifiStartGPS=false;//是否是用WIFI启动GPS的状�?
	private boolean is_WifiOpenGPS=false;//是否因为连接上WIFI而启动GPS
	
	private int   _id;//识别�?
	private String gpsdatetime ;//时间 
	private double longitude;//经度
	private double latitude;//纬度
	private float speed;//速度
	private float  accuracy;//精度
	
	private String wifidatetime;
	private double wifilongitude;
	private double wifilatitude;
	
	private long _gpstime=0;//从GPS中得到实时的时间，用它来判断数据是否已经更新
	private long _wifitime=0;//用WIFI更新数据时存系统时间
	private long _gpstiming;//GPS工作计时�?这是在WIFI已连上网�?但GPS还工�?有一计时�?当WIFI状�?�?0分钟没有变化,主动断开GPS节约电源
	private boolean sendsuccess;
	
	private long _scantime;
	private boolean isStartGPS=false;
	
	private String minaserver;
	private String jsonport;
	private String uppublic;
	
	
	private int opengps=0;
	private int opengpscanuse=0;
	private int gpsstart=0;
	private int gpsinsert=0;
	private int wifiinsert=0;
	
	private boolean isShake=true;
	
	
	/*
	
	private int GsmCellLocation_cid;
	private int GsmCellLocation_lac;
	private int GsmCellLocation_signalStrength;
	public int getGsmCellLocation_cid() {
		return GsmCellLocation_cid;
	}
	public void setGsmCellLocation_cid(int gsmCellLocation_cid) {
		GsmCellLocation_cid = gsmCellLocation_cid;
	}
	public int getGsmCellLocation_lac() {
		return GsmCellLocation_lac;
	}
	public void setGsmCellLocation_lac(int gsmCellLocation_lac) {
		GsmCellLocation_lac = gsmCellLocation_lac;
	}
	public int getGsmCellLocation_signalStrength() {
		return GsmCellLocation_signalStrength;
	}
	public void setGsmCellLocation_signalStrength(int gsmCellLocation_signalStrength) {
		GsmCellLocation_signalStrength = gsmCellLocation_signalStrength;
	}
*/


	
	public boolean isShake() {
		return isShake;
	}
	public void setShake(boolean isShake) {
		this.isShake = isShake;
	}
	public boolean isIs_WifiOpenGPS() {
		return is_WifiOpenGPS;
	}



	public void setIs_WifiOpenGPS(boolean is_WifiOpenGPS) {
		this.is_WifiOpenGPS = is_WifiOpenGPS;
	}



	public int getOpengps() {
		return opengps;
	}



	public void setOpengps(int opengps) {
		this.opengps = opengps;
	}



	public int getOpengpscanuse() {
		return opengpscanuse;
	}



	public void setOpengpscanuse(int opengpscanuse) {
		this.opengpscanuse = opengpscanuse;
	}



	public int getGpsstart() {
		return gpsstart;
	}



	public void setGpsstart(int gpsstart) {
		this.gpsstart = gpsstart;
	}



	public int getGpsinsert() {
		return gpsinsert;
	}



	public void setGpsinsert(int gpsinsert) {
		this.gpsinsert = gpsinsert;
	}



	public int getWifiinsert() {
		return wifiinsert;
	}



	public void setWifiinsert(int wifiinsert) {
		this.wifiinsert = wifiinsert;
	}


	private int sampling;
	/*
	 * 用来记录扫描AP次数,当扫描有10,就算再扫描到AP,而其他条件都符合,也不会启动GPS
	 */
	
	//private Map<String,Long> map=new HashMap<String, Long>();//记录发现AP的次�?第一参数是AP的MAC,第二参数是扫描次�?
	
	
	private SimpleDateFormat sdf;//
	
	public SimpleDateFormat getSdf() {
		//return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.CHINA); //本地时间格式
		SimpleDateFormat sim=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		sim.setTimeZone(TimeZone.getDefault());
		return sim;	
	}
	
	
	
	public boolean isStartGPS() {
		return isStartGPS;
	}



	public void setStartGPS(boolean isStartGPS) {
		this.isStartGPS = isStartGPS;
	}



	
	public long get_scantime() {
		return _scantime;
	}


	public void set_scantime(long _scantime) {
		this._scantime = _scantime;
	}
	
	

	public boolean isSendsuccess() {
		return sendsuccess;
	}


	public void setSendsuccess(boolean sendsuccess) {
		this.sendsuccess = sendsuccess;
	}


	
	
	public long get_gpstiming() {
		return _gpstiming;
	}
	public void set_gpstiming(long _gpstiming) {
		this._gpstiming = _gpstiming;
	}
	public long get_wifitime() {
		return _wifitime;
	}
	public void set_wifitime(long _wifitime) {
		this._wifitime = _wifitime;
	}
	public boolean isIs_WifiStartGPS() {
		return is_WifiStartGPS;
	}
	public void setIs_WifiStartGPS(boolean is_WifiStartGPS) {
		this.is_WifiStartGPS = is_WifiStartGPS;
	}
	public long get_gpstime() {
		return _gpstime;
	}
	public void set_gpstime(long _gpstime) {
		this._gpstime = _gpstime;
	}
	public String getWifidatetime() {
		return wifidatetime;
	}
	public void setWifidatetime(String wifidatetime) {
		this.wifidatetime = wifidatetime;
	}
	public double getWifilongitude() {
		return wifilongitude;
	}
	public void setWifilongitude(double wifilongitude) {
		this.wifilongitude = wifilongitude;
	}
	public double getWifilatitude() {
		return wifilatitude;
	}
	public void setWifilatitude(double wifilatitude) {
		this.wifilatitude = wifilatitude;
	}
	public boolean isIs_requestReStartLocation() {
		return is_requestReStartLocation;
	}
	public void setIs_requestReStartLocation(boolean is_requestReStartLocation) {
		this.is_requestReStartLocation = is_requestReStartLocation;
	}
	public boolean isIs_onQuit() {
		return is_onQuit;
	}
	public void setIs_onQuit(boolean is_onQuit) {
		this.is_onQuit = is_onQuit;
	}
	public boolean isIs_onScreen() {
		return is_onScreen;
	}
	public void setIs_onScreen(boolean is_onScreen) {
		this.is_onScreen = is_onScreen;
	}
	public int get_id() {
		return _id;
	}
	public void set_id(int _id) {
		this._id = _id;
	}
	public String getGpsdatetime() {
		return gpsdatetime;
	}
	public void setGpsdatetime(String gpsdatetime) {
		this.gpsdatetime = gpsdatetime;
	}
	public double getLongitude() {
		return longitude;
	}
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	public double getLatitude() {
		return latitude;
	}
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	public float getSpeed() {
		return speed;
	}
	public void setSpeed(float speed) {
		this.speed = speed;
	}
	public float getAccuracy() {
		return accuracy;
	}
	public void setAccuracy(float accuracy) {
		this.accuracy = accuracy;
	}


	public String getMinaserver() {
		return minaserver;
	}


	public void setMinaserver(String minaserver) {
		this.minaserver = minaserver;
	}


	public String getJsonport() {
		return jsonport;
	}


	public void setJsonport(String jsonport) {
		this.jsonport = jsonport;
	}


	public String getUppublic() {
		return uppublic;
	}


	public void setUppublic(String uppublic) {
		this.uppublic = uppublic;
	}


	public int getSampling() {
		return sampling;
	}


	public void setSampling(int sampling) {
		this.sampling = sampling;
	}


	
}
