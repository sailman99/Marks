package com.trace.marks;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;


public class BootBroadcastReceiver extends BroadcastReceiver {

	private AlarmReceiver alarm;
	private DbAdaptor dbAdaptor;


	@Override

	public void onReceive(Context context, Intent intent) {

		String action = intent.getAction().toString();
		if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {


			dbAdaptor=DbAdaptor.getInstance(context);
			dbAdaptor.getdb().delete("scanap",null,null);

			Cursor cur=dbAdaptor.getdb().rawQuery("select _id,limited,sampling from configdata where idindex=?",new String[]{"1"});//查数据库有没有记录
			try{
				if(cur.getCount()>0){
					cur.moveToFirst();
					int userid=cur.getInt(0);
					if(userid>0){
						if(cur.getLong(1)>System.currentTimeMillis()){
							alarm=new AlarmReceiver();
							alarm.SetAlarm(context,cur.getInt(2));
						}

					}
					else{
						Cursor ccur=dbAdaptor.getdb().rawQuery("select _id from gpsdata",null);//查数据库有没有记录
						try{
							if(ccur.getCount()>0){
								alarm=new AlarmReceiver();
								alarm.SetAlarm(context,cur.getInt(2));
							}
						}
						catch(Exception e){

						}
						finally{
							ccur.close();
						}
					}
				}
			}
			catch(Exception e){

			}
			finally{
				cur.close();
			}
			dbAdaptor.release();

			//context.startService( new Intent (context,LogService.class) );

		}


	}
}
