package com.trace.marks;

import android.content.Context;
import android.content.Intent;


public class MyOnShakeListener implements ShakeDetector.OnShakeListener {
	
	
	
	//private static String TAG="gpsclient.Message";
	private MyApp myApp;
	public MyOnShakeListener(){
		myApp=new MyApp();
	}
	@Override
	//public  void  onShake(Context context,Handler handler) {
	public  void  onShake(Context context) {
		myApp.setShake(true);
	}

	@Override
	public  void onQuiet(Context context) {
		myApp.setShake(false);
		context.sendBroadcast(new Intent("Stop GPS"));
	//	Log.i(TAG,"ShakeListener is send Stop GPS");
	}

	
	

	

}
