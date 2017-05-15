package com.trace.marks;

import java.lang.reflect.Method;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.util.Log;


public class ScreenStatusListener  extends BroadcastReceiver {

	private ConnectivityManager mCM;
	private boolean isScreenOff=false;
	private boolean isWifiOn;//判断WIFI是否打开
	private boolean isGprsOn;//判断GPRS是否打开
	private boolean isWiFiSwitchGPSOn;//是否由WIFI打开GPS的
	private WifiManager wm;
	private GPSService gpsService;//
	private final static String TAG="gpsclient.Message";
	private MyApp myApp;
	public ScreenStatusListener(Application application,Context context){
		wm = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);

		gpsService=new GPSService(application);
		isWiFiSwitchGPSOn=false;
		myApp=(MyApp)application;

		mCM = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

		//      Log.i(TAG,"ScreenStatusListener in run a time");

		//在黑屏状态下打开WIFI


		isGprsOn=gprsIsOpenMethod("getMobileDataEnabled");
		if(wm != null && !wm.isWifiEnabled()){
			isWifiOn=false;
		}
		else{
			isWifiOn=true;
		}
	}

	private boolean gprsEnable(boolean bEnable)
	{
		Object[] argObjects = null;

		boolean isOpen = gprsIsOpenMethod("getMobileDataEnabled");
		if(isOpen == !bEnable)
		{
			setGprsEnable("setMobileDataEnabled", bEnable);
		}

		return isOpen;
	}

	//检测GPRS是否打开
	private boolean gprsIsOpenMethod(String methodName)
	{
		Class cmClass 		= mCM.getClass();
		Class[] argClasses 	= null;
		Object[] argObject 	= null;

		Boolean isOpen = false;
		try
		{
			Method method = cmClass.getMethod(methodName, argClasses);

			isOpen = (Boolean) method.invoke(mCM, argObject);
		} catch (Exception e)
		{
			e.printStackTrace();
		}

		return isOpen;
	}

	//开启/关闭GPRS
	private void setGprsEnable(String methodName, boolean isEnable)
	{
		Class cmClass 		= mCM.getClass();
		Class[] argClasses 	= new Class[1];
		argClasses[0] 		= boolean.class;

		try
		{
			Method method = cmClass.getMethod(methodName, argClasses);
			method.invoke(mCM, isEnable);
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}







	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		if(intent.getAction().equals(Intent.ACTION_SCREEN_ON)){

			isScreenOff=false;
			gpsService.endGPSService();
			//	Log.i(TAG,"ScreanStatusListener onReceive ACTION_SCREEN_ON then gpsService.endGPSService();时间是:"+myApp.getSdf().format(System.currentTimeMillis()));

			if(isWifiOn){//1、wifi is on  不用处理
				//		Log.i(TAG,"on 1");
				return;
			}
			if(!isWifiOn&&!isGprsOn){//3、wifi is off,gprs is off,在黑屏状态打开wifi,在亮屏状态下关wifi
				wm.setWifiEnabled(false);
				//	Log.i(TAG,"on 3");
				return;

			}
			if(!isWifiOn&&isGprsOn){//2、wifi is off,gprs is on ,在黑屏状态下先关gprs,再打开wifi,在亮屏状态下，先关wifi,再打开gprs

				wm.setWifiEnabled(false);//打开wifi
				gprsEnable(true);
				//		Log.i(TAG,"on 2");
			}





		}
		if(intent.getAction().equals(Intent.ACTION_SCREEN_OFF)){

			if(isWiFiSwitchGPSOn&&myApp.isShake()){
				//	Log.i(TAG,"ScreanStatusListener onReceive ACTION_SCREEN_OFF then gpsService.startGPSService();,时间是:"+myApp.getSdf().format(System.currentTimeMillis()));
				gpsService.startGPSService();
			}
			isScreenOff=true;




			/*
			 * 进入黑屏时得到Wifi,Gprs的状态
			 */

			isGprsOn=gprsIsOpenMethod("getMobileDataEnabled");
			if(wm != null && !wm.isWifiEnabled()){
				isWifiOn=false;
			}
			else{
				isWifiOn=true;
			}
			/*
			 * 1、wifi is on  不用处理
			 * 2、wifi is off,gprs is on ,在黑屏状态下先关gprs,再打开wifi,在亮屏状态下，先关wifi,再打开gprs
			 * 3、wifi is off,gprs is off,在黑屏状态打开wifi,在亮屏状态下关wifi
			 */

			if(isWifiOn){//1、wifi is on  不用处理
				//	Log.i(TAG,"off 1");
				return;
			}
			if(!isWifiOn&&!isGprsOn){//3、wifi is off,gprs is off,在黑屏状态打开wifi,在亮屏状态下关wifi
				wm.setWifiEnabled(true);
				//	Log.i(TAG,"off 3");
				return;
			}
			if(!isWifiOn&&isGprsOn){//2、wifi is off,gprs is on ,在黑屏状态下先关gprs,再打开wifi,在亮屏状态下，先关wifi,再打开gprs
				gprsEnable(false);
				wm.setWifiEnabled(true);//打开wifi
				//	Log.i(TAG,"off 2");
			}







		}
		if(intent.getAction().equals("Start GPS")){

			Log.i(TAG,"Start  GPS");
			isWiFiSwitchGPSOn=true;//WIFI打开GPS

			if(isScreenOff&&myApp.isShake()){
				myApp.setOpengpscanuse(myApp.getOpengpscanuse()+1);
				//	Log.i(TAG,"ScreenStatusListener onReceive Start GPS 并且是黑屏  then gpsService.startGPSService();时间是:"+myApp.getSdf().format(System.currentTimeMillis()));
				gpsService.startGPSService();
				//	Log.i(TAG,"Start gps");
			}

		}
		if(intent.getAction().equals("Stop GPS")){
			Log.i(TAG,"Stop  GPS");
			//if(isScreenOff)
			//	Log.i(TAG,"ScreanStatusListener onReceive Stop GPS  then gpsService.endGPSService();时间是:"+myApp.getSdf().format(System.currentTimeMillis()));
			gpsService.endGPSService();
			//	Log.i(TAG,"Stop gps");
			isWiFiSwitchGPSOn=false;//WIFI或手机在静止状态下关闭GPS
			//shakeDetector.stop("Stop GPS");
		}

		/*
		if(intent.getAction().equals("WIFI Connected then Start GPS")){
			gpsService.startGPSService();
		//	Log.i(TAG,"WIFI Connected then Start GPS;时间是:"+myApp.getSdf().format(System.currentTimeMillis()));
		}	
		*/




	}

}
