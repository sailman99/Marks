package com.trace.marks;

import java.util.List;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;


public class WifiScanListener extends BroadcastReceiver{



	private MyApp myApp;
	private  int startOrstop=0;
	private static String TAG="gpsclient.Message";
	private DbAdaptor dbAdaptor;//数据库操作用


	public WifiScanListener(MyApp myApp,DbAdaptor dbAdaptor){//构造函数
		this.myApp=myApp;
		this.dbAdaptor = dbAdaptor;

		//	this.handler=handler;
	}
	@Override
	public void onReceive(Context context, Intent intent) {




		Log.i(TAG, "WifiScanListener onReceive ,时间是:"+myApp.getSdf().format(System.currentTimeMillis()));
		myApp.set_gpstiming(System.currentTimeMillis());//更新GPS计时器时间
		myApp.set_scantime(System.currentTimeMillis());



		//myApp=(MyApp)application;
		WifiManager w = (WifiManager) context.getSystemService(Context.WIFI_SERVICE); //得到WIFI工作状态
		List<ScanResult> list = w.getScanResults();
		//  Log.i(TAG,"function is call before startOrstop value is :"+String.valueOf(startOrstop));
		if(null!=list)
			startOrstop = Tools.aa(list,  dbAdaptor, myApp, context, startOrstop,false);
		//  Log.i(TAG,"function is call after startOrstop value is :"+String.valueOf(startOrstop));
	}
}