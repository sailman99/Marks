package com.trace.marks;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;

import java.io.File;


public class AlarmReceiver extends BroadcastReceiver {
    private final static String TAG="gpsclient.Message";
    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub
        //context.startService(new Intent(context,SensorSwitchGPSService.class));
        Log.i(TAG, "启动LocateService线程A" );

        context.startService(new Intent(context,LocateService.class));

    }

    public void SetAlarm(Context context,int sampling)
    {
		 /*
		  * 读取配置文件,如果数据有效,执行下面语句,如果无效,看gpsdata还有没有数据，如果有，还是执行下面语句
		  * 
		  */

        AlarmManager am=(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);//闹钟接口
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, 0);
        am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), sampling*1000, pi); // Millisec * Second * Minute，使应用在休眠时不被关闭
    }

    public void CancelAlarm(Context context)
    {
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
    }

}