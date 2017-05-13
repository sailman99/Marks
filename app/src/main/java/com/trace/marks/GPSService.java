package com.trace.marks;

import android.app.Application;
import android.content.Context;
import android.location.Criteria;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;





public class GPSService {



	private  MyApp myApp  ;
	private LocationManager lm;
	// private static final String TAG="gpsclient.Message";
	private Application application;

	public GPSService(Application application){
		this.application = application;
		myApp=(MyApp)application;
	}
	/**     * 返回查询条件     * @return     */


	private Criteria getCriteria(){
		Criteria criteria=new Criteria();
		//设置定位精确度 Criteria.ACCURACY_COARSE比较粗略，Criteria.ACCURACY_FINE则比较精细
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		//设置是否要求速度
		criteria.setSpeedRequired(true);
		// 设置是否允许运营商收费
		criteria.setCostAllowed(false);
		//设置是否需要方位信息
		criteria.setBearingRequired(false);
		//设置是否需要海拔信息
		criteria.setAltitudeRequired(false);
		// 设置对电源的需求
		criteria.setPowerRequirement(Criteria.POWER_LOW);
		return criteria;
	}

	private LocationListener locationListener=new LocationListener() {

		/**         * 位置信息变化时触发         */
		public void onLocationChanged(Location location) {
			updateMyApp(location);
			//	 Log.i(TAG, "位置信息变化时触发,时间是:"+myApp.getSdf().format(System.currentTimeMillis()));
		}
		/**         * GPS状态变化时触发         */
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// Log.i(TAG, "GPS状态变化时触发, 时间是:"+myApp.getSdf().format(System.currentTimeMillis()));

		}
		/**         * GPS开启时触发         */
		public void onProviderEnabled(String provider) {
			Location location=lm.getLastKnownLocation(provider);
			updateMyApp(location);
			//	 Log.i(TAG, "GPS开启时触发,时间是:"+myApp.getSdf().format(System.currentTimeMillis()));
		}
		/**         * GPS禁用时触发         */
		public void onProviderDisabled(String provider) {
			updateMyApp(null);
			//	 Log.i(TAG, "GPS禁用时触发,时间是:"+myApp.getSdf().format(System.currentTimeMillis()));
		}
	};
	//状态监听
	GpsStatus.Listener listener = new GpsStatus.Listener() {
		public void onGpsStatusChanged(int event) {
			//	 Log.i(TAG, "GPS状态监听,onGpsStatusChanged ,时间是:"+myApp.getSdf().format(System.currentTimeMillis()));
		};
	};
	/**     * 实时更新文本内容     *      * @param location     */
	private void updateMyApp(Location location){

		if(null!=location){
			myApp.setGpsstart(myApp.getGpsstart()+1);
			myApp.setLatitude(location.getLatitude());
			myApp.setLongitude(location.getLongitude());
			myApp.setSpeed(location.getSpeed());
			myApp.setAccuracy(location.getAccuracy());
			myApp.setGpsdatetime(myApp.getSdf().format(location.getTime()));
			myApp.set_gpstime(location.getTime());
			/*	Log.i(TAG, "GPS更新myApp,时间是:"+myApp.getSdf().format(System.currentTimeMillis()));
				Log.i(TAG, "location.getLatitude()"+String.valueOf(location.getLatitude())+",时间是:"+myApp.getSdf().format(System.currentTimeMillis()));
				Log.i(TAG, "location.getLongitude()"+String.valueOf(location.getLongitude())+",时间是:"+myApp.getSdf().format(System.currentTimeMillis()));
				Log.i(TAG, "location.getSpeed()"+String.valueOf(location.getSpeed())+",时间是:"+myApp.getSdf().format(System.currentTimeMillis()));
				Log.i(TAG, "location.getAccuracy()"+String.valueOf(location.getAccuracy())+",时间是:"+myApp.getSdf().format(System.currentTimeMillis()));
				Log.i(TAG, "location.getTime()"+String.valueOf(location.getTime())+",时间是:"+String.valueOf(System.currentTimeMillis()));
		*/
		}


	}
	public  void startGPSService(){

		if(null==lm){
			//  	Log.i(TAG, "startGPSService ,并且lm is null ,启动GPS定位,时间是:"+myApp.getSdf().format(System.currentTimeMillis()));
			lm=(LocationManager)application.getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
			//为获取地理位置信息时设置查询条件
			String bestProvider = lm.getBestProvider(getCriteria(), true);
			//获取位置信息
			//如果不设置查询要求，getLastKnownLocation方法传人的参数为LocationManager.GPS_PROVIDER
			Location location= lm.getLastKnownLocation(bestProvider);
			updateMyApp(location);   //???
			//监听状态
			lm.addGpsStatusListener(listener);


			//绑定监听，有4个参数
			//参数1，设备：有GPS_PROVIDER和NETWORK_PROVIDER两种
			//参数2，位置信息更新周期，单位毫秒
			//参数3，位置变化最小距离：当位置距离变化超过此值时，将更新位置信息
			//参数4，监听
			//备注：参数2和3，如果参数3不为0，则以参数3为准；参数3为0，则通过时间来定时更新；两者为0，则随时刷新
			// 1秒更新一次，或最小位移变化超过1米更新一次；
			//注意：此处更新准确度非常低，推荐在service里面启动一个Thread，在run中sleep(10000);然后执行handler.sendMessage(),更新位置
			//lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, locationListener);
				 /*
				if(myApp.isIs_WifiOpenGPS()){
					lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 1, locationListener);
					Log.i(TAG, "myApp.isIs_WifiOpenGPS(),连接上互联网时从互联网获得GPS数据,时间是:"+myApp.getSdf().format(System.currentTimeMillis()));
				}
				else{*/
			if(lm.isProviderEnabled(LocationManager.GPS_PROVIDER))
			{
				lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, locationListener);
				myApp.setStartGPS(true);
				//	  Log.i(TAG, "用手机硬件获得GPS数据,时间是:"+myApp.getSdf().format(System.currentTimeMillis()));
			}
			else{
				myApp.setStartGPS(false);
				lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 1, locationListener);
				//		 Log.i(TAG, "没有手机硬件，从互联网获得GPS数据,时间是:"+myApp.getSdf().format(System.currentTimeMillis()));
			}
			//}
		}
	}
	public  void endGPSService()    {
		//	    Log.i(TAG, "lm != null 判断之前 endGPSService() ");
		if(lm != null && locationListener != null )     {
			//			Log.i(TAG, "lm != null 条件成立  endGPSService() ");
			lm.removeGpsStatusListener(listener);
			lm.removeUpdates(locationListener);
		}
		lm=null;
	}
}
