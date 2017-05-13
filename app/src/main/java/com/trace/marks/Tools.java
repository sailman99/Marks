package com.trace.marks;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.wifi.ScanResult;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;





public  class Tools {

//	private static String TAG="gpsclient.Message";

	private static final double EARTH_RADIUS = 6378137;//地球半径，单位为米
	public static String getDeviceId(Context context){
		String id=((TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();//IMEI
		if(null==id||"000000000000000".equals(id)){
			id= Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
			if(id==null||"0000000000000000".equals(id)||"9774d56d682e549c".equals(id)){
				id=android.os.Build.SERIAL;
				if(null==id)
					id="123456789987654";
			}
		}

		return MD5Util.MD5(id);
	}
	private static double rad(double d)
	{
		return d * Math.PI / 180.0;
	}

	/** *//**
	 * 根据两点间经纬度坐标（double值），计算两点间距离，单位为米
	 * @param lng1
	 * @param lat1
	 * @param lng2
	 * @param lat2
	 * @return
	 */
	public static double GetDistance(double lng1, double lat1, double lng2, double lat2)//求两点距离
	{
		double radLat1 = rad(lat1);
		double radLat2 = rad(lat2);
		double a = radLat1 - radLat2;
		double b = rad(lng1) - rad(lng2);
		double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a/2),2) +  Math.cos(radLat1)*Math.cos(radLat2)*Math.pow(Math.sin(b/2),2)));
		s = s * EARTH_RADIUS;
		s = Math.round(s * 10000) / 10000;
		return s;
	}
	public static String MathFloor(double lnglat){
		int _lnglata=(int)Math.floor(Math.abs(lnglat*10000));
		int _lnglatb=(int)Math.floor(_lnglata/10)*10;
		int subtract =(int)(_lnglata-_lnglatb);
		if (subtract<3){
			subtract=0;
		}
		else{
			if(subtract<6){
				subtract=3;
			}else{
				subtract=6;
			}
		}
		return String.valueOf(_lnglatb+subtract);
	}
	@SuppressLint("DefaultLocale")
	public static int aa(List<ScanResult> list,DbAdaptor dbAdaptor,MyApp myApp,Context context,int startOrstop,boolean is_startGPS ){
		//int startOrstop=0;//通过它计数,当它达到3的时候,启动GPS,当它小于0的时候,关闭GP
		long l_gpstimespile=0;//myApp.get_gpstime时间间隔
//		 private Map<String,Long> map=new HashMap<String, Long>();//记录发现AP的次数
		final int twoApMaxDistance=400;//定义两个AP之间最大距离，如果大于400米，表明两个AP不是在同一地方，有可能其中一AP是移动AP
		if(null==list)
			return  startOrstop;

		Collections.sort(list,new Comparator<ScanResult>(){  //排序，用信号强度来排序，相同的用名字排序
			public int compare(ScanResult arg0, ScanResult arg1) {
				int flag = arg1.level-arg0.level;
				if(flag==0)
					return arg0.BSSID.compareTo(arg1.BSSID);
				else
					return flag;
			}
		});

		int i_counts=0;
		double longitude=0,latitude=0;
		double longitude1=0,latitude1=0;
		double longitude2=0,latitude2=0;
		double longitude3=0,latitude3=0;
		String _bssid1="",_bssid2="",_bssid3="";

		startOrstop++;//先把它加一,如果不合条件,再减二

		long scanaptime=System.currentTimeMillis();
		Map<String,Long> scanmap=new HashMap<String,Long>();
		dbAdaptor.getdb().execSQL("delete from  apmap where realtime<? ",new Object[]{System.currentTimeMillis()-21600000l});
		for(ScanResult scanResult : list){//ap扫描结果
			if(scanResult.BSSID.trim().length()!=17||"00:00:00:00:00:00".equals(scanResult.BSSID))//保证MAC地址是17位，多拉或少啦都不行，这是常量，最终都不用该变得
				continue;


			Cursor curapmap=dbAdaptor.getdb().rawQuery("select apcounts from apmap where bssid=?",new String[]{scanResult.BSSID});//查数据库有没有记录
			try{
				if(curapmap.getCount()>0){
					curapmap.moveToFirst();
					int apcounts=curapmap.getInt(0)+1;
					if(apcounts<12)
						dbAdaptor.getdb().execSQL("UPDATE apmap set apcounts=? where bssid=?",new Object[]{apcounts,scanResult.BSSID});
				}
				else{
					dbAdaptor.getdb().execSQL("INSERT INTO apmap VALUES(?, ?, ?)", new Object[]{scanResult.BSSID,1,System.currentTimeMillis()});
				}
			}
			catch(Exception e){

			}
			finally{
				curapmap.close();
			}




			if(scanResult.BSSID!=null&&!scanmap.containsKey(scanResult.BSSID)){
				scanmap.put(scanResult.BSSID,scanaptime);

					   /*
					    * 判断30秒内是否有数据
					    */
				String havegpsdata="0";
				if(myApp.get_gpstime()>System.currentTimeMillis()-30*1000||((myApp.get_wifitime()>System.currentTimeMillis()-30*1000)&&myApp.getWifilatitude()>-200&&myApp.getWifilongitude()>-200)){
					havegpsdata="1";
				}
				dbAdaptor.getdb().execSQL("INSERT INTO scanap VALUES(?, ? , ?)", new Object[]{scanaptime,scanResult.BSSID,havegpsdata});
			}

			if(scanResult.SSID.toLowerCase(Locale.US).contains("cmcc")||scanResult.SSID.toLowerCase(Locale.US).contains("china"))//中国移动,联通,电信AP不能用,它们发射功率太强,影响很远
				continue;

			Cursor cur=dbAdaptor.getdb().rawQuery("select singlelevel,longitude,latitude from apdata where bssid=?",new String[]{scanResult.BSSID});//查数据库有没有记录
			try{
				int i_rowcount=cur.getCount();
				if(i_rowcount>0){//如果有记录
					cur.moveToFirst();
					longitude=cur.getDouble(1);//取数据库的数据
					latitude=cur.getDouble(2);//取数据库的数据
					int singleLevel=cur.getInt(0);//作为判读是否启动GPS的依据，取它的信号强度


					if(longitude>-180&&latitude>-180)//这句有用
						i_counts++;

						   /*
						    *判断这条记录是否有效，看longitude和latitude是否小于0。如果无效，则要判断现在GPS数据是否有效，如果没效，计数加一，判读现在的计数，如果大于10，这次这条数据不用启动GPS
						    *1、判断GPS数据是否有效，如果有效，直接用GPS数据更新数据库。   如果没效
						    *2、判断读出的数据是否有效，如果有效，不用更新 ，如果没效，计数加一
						    */
					l_gpstimespile= System.currentTimeMillis()-myApp.get_gpstime();//看GPS时间和现在时间相隔多久

					if(null!=myApp.getGpsdatetime()&&(l_gpstimespile<myApp.getSampling()*1000)){//gpsdatetime有数据并且更新数据的时间在十秒之内，是有效数据------------------------

							   /*
							    * 用recordstopbefore表
							    */
						if(singleLevel<-180){//表明有可能有数据
							Cursor currecordstopbefore=dbAdaptor.getdb().rawQuery("select realtime from recordstopbefore where bssid=? order by realtime",new String[]{scanResult.BSSID});//查数据库有没有记录
							if(currecordstopbefore.getCount()>0){
								currecordstopbefore.moveToFirst();
								do{
									Long realtime=currecordstopbefore.getLong(0);
									String gpsdatetime=myApp.getSdf().format(realtime);
									dbAdaptor.getdb().execSQL("INSERT INTO gpsdata VALUES(?, ?, ?, ?, ?,?)", new Object[]{realtime,gpsdatetime.replace(" ","T"),myApp.get_id(),myApp.getLongitude(),myApp.getLatitude(),-200});
									// dbAdaptor.getdb().delete("recordstopbefore","realtime="+realtime,null);
									dbAdaptor.getdb().execSQL("delete from recordstopbefore where realtime=?", new Object[]{realtime});
									//				   Log.i(TAG,"use recordstopbefore INSERT INTO gpsdata:"+realtime);
								}while(currecordstopbefore.moveToNext());
							}
							currecordstopbefore.close();
						}



						if(singleLevel<scanResult.level&&myApp.getAccuracy()<30){//且当前信号强度大于记录的，更新数据库,并且精度小于30米内更新数据库???????????????????????



							dbAdaptor.getdb().execSQL("UPDATE apdata set singlelevel=?,longitude=?,latitude=?,realtime=? where bssid=?",new Object[]{scanResult.level,myApp.getLongitude(),myApp.getLatitude(),System.currentTimeMillis(),scanResult.BSSID});
							if(longitude<-180&&latitude<-180)
								i_counts++;//补回上面没有加一的记录
							longitude=myApp.getLongitude();//用当前的数据代替数据库的
							latitude=myApp.getLatitude();//用当前的数据代替数据库的
							is_startGPS=true;//有数据更新都,不用减二
						}

					}else{//GPS数据无效
						//			   Log.i(TAG,""+scanResult.BSSID+":"+String.valueOf(map.get(scanResult.BSSID)));

						Cursor curapmapa=dbAdaptor.getdb().rawQuery("select apcounts from apmap where bssid=?",new String[]{scanResult.BSSID});//查数据库有没有记录
						int apcountsa=13;
						try{
							if(curapmapa.getCount()>0){
								curapmapa.moveToFirst();
								apcountsa=curapmapa.getInt(0);
								//	 Log.i(TAG,"inside apcountsa value is :"+String.valueOf(apcountsa));
							}

						}
						catch(Exception e){

						}
						finally{
							curapmapa.close();
						}
						//  	Log.i(TAG,"apcountsa value is :"+String.valueOf(apcountsa));
						if(apcountsa<10&&singleLevel<scanResult.level){//无论这条数据是否有效，当一条数据更新10以后再碰到这AP都不需要启动GPS更新数据????????????????
							is_startGPS=true;//连续执行10要求GPS工作,不用减二
						}
					}

				}
			}catch(Exception e){

			}
			finally{
				cur.close();
			}
			if(i_counts==1){
				longitude1=longitude;
				latitude1=latitude;
				_bssid1=scanResult.BSSID;
			}
			//如果是第二条记录
			if(i_counts==2){
				longitude2=longitude;
				latitude2=latitude;
				_bssid2=scanResult.BSSID;
			}
			//如果是第三条记录
			if(i_counts==3){
				longitude3=longitude;
				latitude3=latitude;
				_bssid3=scanResult.BSSID;
			}
		}//读完全部WIFI数据------------
			 /* if(apmap.size()>0){
			//  myApp.getBlockingDeque().add(apmap);
			  try{
				  if(myApp.getBlockingDeque().offer(apmap,3L,TimeUnit.SECONDS))
					  myApp.setApmap(apmap);
			  }
			  catch(Exception e){
				  Log.i(TAG,"offer catch");
			  }
		  }*/
		//	  dbAdaptor.release();
		myApp.setWifidatetime(myApp.getSdf().format(System.currentTimeMillis()));	//更新为现在的系统时间，为防止得到过期数据这个时间很重要
		myApp.set_wifitime(System.currentTimeMillis());//直接用long保存，后面好用它来比较时间间隔
		myApp.setWifilatitude(-200);//用负的表明这个数据是无效的，如果后面的语句没有更新这数据，这数据就不能用
		myApp.setWifilongitude(-200);//用负的表明这个数据是无效的，如果后面的语句没有更新这数据，这数据就不能用;
		myApp.setSpeed(-100);//用AP填充数据，这个是标式AP数据
		if(i_counts>2){//有3个或以上的AP，并且数据都有效可用
			if(GetDistance(longitude1,latitude1,longitude2,latitude2)<twoApMaxDistance&&GetDistance(longitude1,latitude1,longitude3,latitude3)<twoApMaxDistance){//检测两点的距离，两个AP如果相隔达到500米，表明第一个AP是移动AP--------------------
				myApp.setWifilatitude(latitude1);
				myApp.setWifilongitude(longitude1);
				//   Log.i(TAG,"有大于两个AP的情况下,选第一AP");
			}
			else{//用第二个AP 	假设第一个AP是移动AP
				if(GetDistance(longitude1,latitude1,longitude2,latitude2)>twoApMaxDistance&&GetDistance(longitude1,latitude1,longitude3,latitude3)>twoApMaxDistance&&GetDistance(longitude2,latitude2,longitude3,latitude3)<twoApMaxDistance){//------------------------
					myApp.setWifilatitude(latitude2);
					myApp.setWifilongitude(longitude2);
					dbAdaptor.getdb().execSQL("UPDATE apdata set singlelevel=?,longitude=?,latitude=?,realtime=? where bssid=?",new Object[]{-200,-200,-200,System.currentTimeMillis(),_bssid1});
			/*		   Log.i(TAG,"有大于两个AP的情况下,但一，二AP的距离是"+String.valueOf(GetDistance(longitude1,latitude1,longitude2,latitude2))+"米,大于设定值,删除第一AP数据");
					   Log.i(TAG,"一，三AP的距离是"+String.valueOf(GetDistance(longitude1,latitude1,longitude3,latitude3))+"米");
					   Log.i(TAG,"二，三AP的距离是"+String.valueOf(GetDistance(longitude2,latitude2,longitude3,latitude3))+"米");
			*/
				}else{//假设第二个AP是移动AP,用第一个AP数据
					if(GetDistance(longitude1,latitude1,longitude2,latitude2)>twoApMaxDistance&&GetDistance(longitude1,latitude1,longitude3,latitude3)<twoApMaxDistance&&GetDistance(longitude2,latitude2,longitude3,latitude3)>twoApMaxDistance){//----------------
						myApp.setWifilatitude(latitude1);
						myApp.setWifilongitude(longitude1);
						dbAdaptor.getdb().execSQL("UPDATE apdata set singlelevel=?,longitude=?,latitude=?,realtime=? where bssid=?",new Object[]{-200,-200,-200,System.currentTimeMillis(),_bssid2});
					/*		   Log.i(TAG,"有大于两个AP的情况下,但一，二AP的距离是"+String.valueOf(GetDistance(longitude1,latitude1,longitude2,latitude2))+"米,大于设定值,删除第二AP数据");
							   Log.i(TAG,"一，三AP的距离是"+String.valueOf(GetDistance(longitude1,latitude1,longitude3,latitude3))+"米");
							   Log.i(TAG,"二，三AP的距离是"+String.valueOf(GetDistance(longitude2,latitude2,longitude3,latitude3))+"米");
					*/
					}else{//假设第三个AP是移动AP,用第一个AP数据
						if(GetDistance(longitude1,latitude1,longitude2,latitude2)<twoApMaxDistance&&GetDistance(longitude1,latitude1,longitude3,latitude3)>twoApMaxDistance&&GetDistance(longitude2,latitude2,longitude3,latitude3)>twoApMaxDistance){//-----------------------
							myApp.setWifilatitude(latitude1);
							myApp.setWifilongitude(longitude1);
							dbAdaptor.getdb().execSQL("UPDATE apdata set singlelevel=?,longitude=?,latitude=?,realtime=? where bssid=?",new Object[]{-200,-200,-200,System.currentTimeMillis(),_bssid3});
							/*		   Log.i(TAG,"有大于两个AP的情况下,但一，二AP的距离是"+String.valueOf(GetDistance(longitude1,latitude1,longitude2,latitude2))+"米,删除第三AP数据");
									   Log.i(TAG,"一，三AP的距离是"+String.valueOf(GetDistance(longitude1,latitude1,longitude3,latitude3))+"米");
									   Log.i(TAG,"二，三AP的距离是"+String.valueOf(GetDistance(longitude2,latitude2,longitude3,latitude3))+"米");
								*/
						}
					}
				}

			}
		}else{
			if(i_counts==2){//只有二个AP情况下
				if(GetDistance(longitude1,latitude1,longitude2,latitude2)<twoApMaxDistance){//------------------------
					myApp.setWifilatitude(latitude1);
					myApp.setWifilongitude(longitude1);
					//   Log.i(TAG,"只有两个AP的情况下选用第一个AP");
				}else{
					dbAdaptor.getdb().execSQL("UPDATE apdata set singlelevel=?,longitude=?,latitude=?,realtime=? where bssid=?",new Object[]{-200,-200,-200,System.currentTimeMillis(),_bssid1});
					dbAdaptor.getdb().execSQL("UPDATE apdata set singlelevel=?,longitude=?,latitude=?,realtime=? where bssid=?",new Object[]{-200,-200,-200,System.currentTimeMillis(),_bssid2});
					// Log.i(TAG,"只有两个AP的情况下,但两个AP的距离是"+String.valueOf(GetDistance(longitude1,latitude1,longitude2,latitude2))+"米,大于设定值,删除这两AP数据");
				}

			}   /*//只有一个AP情况下无法验证是否是移动AP,干脆不用

		   		   if(i_counts==1){
		   			   if("1".equals(apswitch)){
		   				   myApp.setWifilatitude(latitude1);
		   				   myApp.setWifilongitude(longitude1);
		   			   }
		   		   }*/

		}

		if(!is_startGPS){
			startOrstop=startOrstop-2;//先前已经加上一，现在减回二
			if(startOrstop<0){//小于0就关闭GPS
				context.sendBroadcast(new Intent("Stop GPS"));
				//   Log.i(TAG,"context.sendBroadcast(new Intent(\"stop GPS\"));and  startOrstop value is:"+String.valueOf(startOrstop));
				myApp.setIs_WifiStartGPS(false);
				startOrstop=0;
			}
		}
		if(startOrstop>1){//大于1就开启GPS
			context.sendBroadcast(new Intent("Start GPS"));
			//   Log.i(TAG,"context.sendBroadcast(new Intent(\"Start GPS\"));and  startOrstop value is:"+String.valueOf(startOrstop));
			myApp.setOpengps(myApp.getOpengps()+1);
			myApp.setIs_WifiStartGPS(true);
			if(startOrstop>3)
				startOrstop=3;//
		}


		return startOrstop;
	}
	public static int cc(List<ScanResult> list,DbAdaptor dbAdaptor,MyApp myApp,Context context,int startOrstop,boolean is_startGPS ){
		//int startOrstop=0;//通过它计数,当它达到3的时候,启动GPS,当它小于0的时候,关闭GP
		long l_gpstimespile=0;//myApp.get_gpstime时间间隔
//		 private Map<String,Long> map=new HashMap<String, Long>();//记录发现AP的次数
		final int twoApMaxDistance=400;//定义两个AP之间最大距离，如果大于1000米，表明两个AP不是在同一地方，有可能其中一AP是移动AP
		if(null==list)
			return  startOrstop;

		Collections.sort(list,new Comparator<ScanResult>(){  //排序，用信号强度来排序，相同的用名字排序
			public int compare(ScanResult arg0, ScanResult arg1) {
				int flag = arg1.level-arg0.level;
				if(flag==0)
					return arg0.BSSID.compareTo(arg1.BSSID);
				else
					return flag;
			}
		});

		int i_counts=0;
		double longitude=0,latitude=0;
		double longitude1=0,latitude1=0;
		double longitude2=0,latitude2=0;
		double longitude3=0,latitude3=0;
		String _bssid1="",_bssid2="",_bssid3="";

		startOrstop++;//县把它加一,如果不合条件,再减二
		//  Log.i(TAG,"AA scan");
		//  Log.i(TAG,"map count:"+String.valueOf(map.size()));
		// Map<String,Long> apmap=new HashMap<String,Long>();//记录每次扫描得到的AP的MAC及当时的时间
		long scanaptime=System.currentTimeMillis();
		Map<String,Long> scanmap=new HashMap<String,Long>();
		dbAdaptor.getdb().execSQL("delete from  apmap where realtime<? ",new Object[]{System.currentTimeMillis()-21600000l});
		for(ScanResult scanResult : list){//ap扫描结果
			if(scanResult.BSSID.trim().length()!=17||"00:00:00:00:00:00".equals(scanResult.BSSID))//保证MAC地址是17位，多拉或少啦都不行，这是常量，最终都不用该变得
				continue;


			Cursor curapmap=dbAdaptor.getdb().rawQuery("select apcounts from apmap where bssid=?",new String[]{scanResult.BSSID});//查数据库有没有记录
			try{
				if(curapmap.getCount()>0){
					curapmap.moveToFirst();
					int apcounts=curapmap.getInt(0)+1;
					if(apcounts<12)
						dbAdaptor.getdb().execSQL("UPDATE apmap set apcounts=? where bssid=?",new Object[]{apcounts,scanResult.BSSID});
				}
				else{
					dbAdaptor.getdb().execSQL("INSERT INTO apmap VALUES(?, ?, ?)", new Object[]{scanResult.BSSID,1,System.currentTimeMillis()});
				}
			}
			catch(Exception e){

			}
			finally{
				curapmap.close();
			}



			//	  	   apmap.put(scanResult.BSSID,System.currentTimeMillis());///记录扫描得到的AP的MAC及当时的时间

			if(scanResult.BSSID!=null&&!scanmap.containsKey(scanResult.BSSID)){
				scanmap.put(scanResult.BSSID,scanaptime);
					   /*
					    * 判断30秒内是否有数据
					    */
				String havegpsdata="0";
				if(myApp.get_gpstime()>System.currentTimeMillis()-30*1000||myApp.get_wifitime()>System.currentTimeMillis()-30*1000){
					havegpsdata="1";
				}
				dbAdaptor.getdb().execSQL("INSERT INTO scanap VALUES(?, ? , ? )", new Object[]{scanaptime,scanResult.BSSID,havegpsdata});
			}

			if(scanResult.SSID.toLowerCase(Locale.US).contains("cmcc")||scanResult.SSID.toLowerCase(Locale.US).contains("china"))//中国移动,联通,电信AP不能用,它们发射功率太强,影响很远
				continue;

			Cursor cur=dbAdaptor.getdb().rawQuery("select singlelevel,longitude,latitude from apdata where bssid=?",new String[]{scanResult.BSSID});//查数据库有没有记录
			try{
				int i_rowcount=cur.getCount();
				if(i_rowcount>0){//如果有记录
					cur.moveToFirst();
					longitude=cur.getDouble(1);//取数据库的数据
					latitude=cur.getDouble(2);//取数据库的数据
					int singleLevel=cur.getInt(0);//作为判读是否启动GPS的依据，取它的信号强度


					if(longitude>-180&&latitude>-180)//这句有用
						i_counts++;

						   /*
						    *判断这条记录是否有效，看longitude和latitude是否小于0。如果无效，则要判断现在GPS数据是否有效，如果没效，计数加一，判读现在的计数，如果大于10，这次这条数据不用启动GPS
						    *1、判断GPS数据是否有效，如果有效，直接用GPS数据更新数据库。   如果没效
						    *2、判断读出的数据是否有效，如果有效，不用更新 ，如果没效，计数加一
						    */
					l_gpstimespile= System.currentTimeMillis()-myApp.get_gpstime();//看GPS时间和现在时间相隔多久

					if(null!=myApp.getGpsdatetime()&&(l_gpstimespile<myApp.getSampling()*1000)){//gpsdatetime有数据并且更新数据的时间在十秒之内，是有效数据------------------------

							   /*
							    * 用recordstopbefore表
							    */
						if(singleLevel<-180){//表明有可能有数据
							Cursor currecordstopbefore=dbAdaptor.getdb().rawQuery("select realtime from recordstopbefore where bssid=? order by realtime",new String[]{scanResult.BSSID});//查数据库有没有记录
							if(currecordstopbefore.getCount()>0){
								currecordstopbefore.moveToFirst();
								do{
									Long realtime=currecordstopbefore.getLong(0);
									String gpsdatetime=myApp.getSdf().format(realtime);
									dbAdaptor.getdb().execSQL("INSERT INTO gpsdata VALUES(?, ?, ?, ?, ?,?)", new Object[]{realtime,gpsdatetime.replace(" ","T"),myApp.get_id(),myApp.getLongitude(),myApp.getLatitude(),-200});
									// dbAdaptor.getdb().delete("recordstopbefore","realtime="+realtime,null);
									dbAdaptor.getdb().execSQL("delete from recordstopbefore where realtime=?", new Object[]{realtime});

								}while(currecordstopbefore.moveToNext());
							}
							currecordstopbefore.close();
						}


						if(singleLevel<scanResult.level&&myApp.getAccuracy()<30){//且当前信号强度大于记录的，更新数据库,并且精度小于30米内更新数据库???????????????????????


							dbAdaptor.getdb().execSQL("UPDATE apdata set singlelevel=?,longitude=?,latitude=?,realtime=? where bssid=?",new Object[]{scanResult.level,myApp.getLongitude(),myApp.getLatitude(),System.currentTimeMillis(),scanResult.BSSID});
							if(longitude<-180&&latitude<-180)
								i_counts++;//补回上面没有加一的记录
							longitude=myApp.getLongitude();//用当前的数据代替数据库的
							latitude=myApp.getLatitude();//用当前的数据代替数据库的
							is_startGPS=true;//有数据更新都,不用减二
						}

					}else{//GPS数据无效
						//			   Log.i(TAG,""+scanResult.BSSID+":"+String.valueOf(map.get(scanResult.BSSID)));

						Cursor curapmapa=dbAdaptor.getdb().rawQuery("select apcounts from apmap where bssid=?",new String[]{scanResult.BSSID});//查数据库有没有记录
						int apcountsa=13;
						try{
							if(curapmapa.getCount()>0){
								curapmapa.moveToFirst();
								apcountsa=curapmapa.getInt(0);
								//	 Log.i(TAG,"inside apcountsa value is :"+String.valueOf(apcountsa));
							}

						}
						catch(Exception e){

						}
						finally{
							curapmapa.close();
						}
						//  	Log.i(TAG,"apcountsa value is :"+String.valueOf(apcountsa));
						if(apcountsa<10&&singleLevel<scanResult.level){//无论这条数据是否有效，当一条数据更新10以后再碰到这AP都不需要启动GPS更新数据????????????????
							is_startGPS=true;//连续执行10要求GPS工作,不用减二
						}
					}

				}
			}catch(Exception e){

			}
			finally{
				cur.close();
			}
			if(i_counts==1){
				longitude1=longitude;
				latitude1=latitude;
				_bssid1=scanResult.BSSID;
			}
			//如果是第二条记录
			if(i_counts==2){
				longitude2=longitude;
				latitude2=latitude;
				_bssid2=scanResult.BSSID;
			}
			//如果是第三条记录
			if(i_counts==3){
				longitude3=longitude;
				latitude3=latitude;
				_bssid3=scanResult.BSSID;
			}
		}//读完全部WIFI数据------------
			 /* if(apmap.size()>0){
			//  myApp.getBlockingDeque().add(apmap);
			  try{
				  if(myApp.getBlockingDeque().offer(apmap,3L,TimeUnit.SECONDS))
					  myApp.setApmap(apmap);
			  }
			  catch(Exception e){
				  Log.i(TAG,"offer catch");
			  }
		  }*/
		//	  dbAdaptor.release();
		myApp.setWifidatetime(myApp.getSdf().format(System.currentTimeMillis()));	//更新为现在的系统时间，为防止得到过期数据这个时间很重要
		myApp.set_wifitime(System.currentTimeMillis());//直接用long保存，后面好用它来比较时间间隔
		myApp.setWifilatitude(-200);//用负的表明这个数据是无效的，如果后面的语句没有更新这数据，这数据就不能用
		myApp.setWifilongitude(-200);//用负的表明这个数据是无效的，如果后面的语句没有更新这数据，这数据就不能用;
		myApp.setSpeed(-100);//用AP填充数据，这个是标式AP数据
		if(i_counts>2){//有3个或以上的AP，并且数据都有效可用
			if(GetDistance(longitude1,latitude1,longitude2,latitude2)<twoApMaxDistance&&GetDistance(longitude1,latitude1,longitude3,latitude3)<twoApMaxDistance){//检测两点的距离，两个AP如果相隔达到500米，表明第一个AP是移动AP--------------------
				myApp.setWifilatitude(latitude1);
				myApp.setWifilongitude(longitude1);
				//   Log.i(TAG,"有大于两个AP的情况下,选第一AP");
			}
			else{//用第二个AP 	假设第一个AP是移动AP
				if(GetDistance(longitude1,latitude1,longitude2,latitude2)>twoApMaxDistance&&GetDistance(longitude1,latitude1,longitude3,latitude3)>twoApMaxDistance&&GetDistance(longitude2,latitude2,longitude3,latitude3)<twoApMaxDistance){//------------------------
					myApp.setWifilatitude(latitude2);
					myApp.setWifilongitude(longitude2);
					dbAdaptor.getdb().execSQL("UPDATE apdata set singlelevel=?,longitude=?,latitude=?,realtime=? where bssid=?",new Object[]{-200,-200,-200,System.currentTimeMillis(),_bssid1});
					//   Log.i(TAG,"有大于两个AP的情况下,但一，二AP的距离是"+String.valueOf(GetDistance(longitude1,latitude1,longitude2,latitude2))+"米,大于设定值,删除第一AP数据");
					//   Log.i(TAG,"一，三AP的距离是"+String.valueOf(GetDistance(longitude1,latitude1,longitude3,latitude3))+"米");
					//   Log.i(TAG,"二，三AP的距离是"+String.valueOf(GetDistance(longitude2,latitude2,longitude3,latitude3))+"米");
				}else{//假设第二个AP是移动AP,用第一个AP数据
					if(GetDistance(longitude1,latitude1,longitude2,latitude2)>twoApMaxDistance&&GetDistance(longitude1,latitude1,longitude3,latitude3)<twoApMaxDistance&&GetDistance(longitude2,latitude2,longitude3,latitude3)>twoApMaxDistance){//----------------
						myApp.setWifilatitude(latitude1);
						myApp.setWifilongitude(longitude1);
						dbAdaptor.getdb().execSQL("UPDATE apdata set singlelevel=?,longitude=?,latitude=?,realtime=? where bssid=?",new Object[]{-200,-200,-200,System.currentTimeMillis(),_bssid2});
						//	   Log.i(TAG,"有大于两个AP的情况下,但一，二AP的距离是"+String.valueOf(GetDistance(longitude1,latitude1,longitude2,latitude2))+"米,大于设定值,删除第二AP数据");
						//	   Log.i(TAG,"一，三AP的距离是"+String.valueOf(GetDistance(longitude1,latitude1,longitude3,latitude3))+"米");
						//	   Log.i(TAG,"二，三AP的距离是"+String.valueOf(GetDistance(longitude2,latitude2,longitude3,latitude3))+"米");
					}else{//假设第三个AP是移动AP,用第一个AP数据
						if(GetDistance(longitude1,latitude1,longitude2,latitude2)<twoApMaxDistance&&GetDistance(longitude1,latitude1,longitude3,latitude3)>twoApMaxDistance&&GetDistance(longitude2,latitude2,longitude3,latitude3)>twoApMaxDistance){//-----------------------
							myApp.setWifilatitude(latitude1);
							myApp.setWifilongitude(longitude1);
							dbAdaptor.getdb().execSQL("UPDATE apdata set singlelevel=?,longitude=?,latitude=?,realtime=? where bssid=?",new Object[]{-200,-200,-200,System.currentTimeMillis(),_bssid3});
							//		   Log.i(TAG,"有大于两个AP的情况下,但一，二AP的距离是"+String.valueOf(GetDistance(longitude1,latitude1,longitude2,latitude2))+"米,删除第三AP数据");
							//		   Log.i(TAG,"一，三AP的距离是"+String.valueOf(GetDistance(longitude1,latitude1,longitude3,latitude3))+"米");
							//		   Log.i(TAG,"二，三AP的距离是"+String.valueOf(GetDistance(longitude2,latitude2,longitude3,latitude3))+"米");
						}
					}
				}

			}
		}else{
			if(i_counts==2){//只有二个AP情况下
				if(GetDistance(longitude1,latitude1,longitude2,latitude2)<twoApMaxDistance){//------------------------
					myApp.setWifilatitude(latitude1);
					myApp.setWifilongitude(longitude1);
					//	   Log.i(TAG,"只有两个AP的情况下选用第一个AP");
				}else{
					dbAdaptor.getdb().execSQL("UPDATE apdata set singlelevel=?,longitude=?,latitude=?,realtime=? where bssid=?",new Object[]{-200,-200,-200,System.currentTimeMillis(),_bssid1});
					dbAdaptor.getdb().execSQL("UPDATE apdata set singlelevel=?,longitude=?,latitude=?,realtime=? where bssid=?",new Object[]{-200,-200,-200,System.currentTimeMillis(),_bssid2});
					//	 Log.i(TAG,"只有两个AP的情况下,但两个AP的距离是"+String.valueOf(GetDistance(longitude1,latitude1,longitude2,latitude2))+"米,大于设定值,删除这两AP数据");
				}

			}   /*//只有一个AP情况下无法验证是否是移动AP,干脆不用

		   		   if(i_counts==1){
		   			   if("1".equals(apswitch)){
		   				   myApp.setWifilatitude(latitude1);
		   				   myApp.setWifilongitude(longitude1);
		   			   }
		   		   }*/

		}

		if(!is_startGPS){
			startOrstop=startOrstop-2;//先前已经加上一，现在减回二
			if(startOrstop<0){//小于0就关闭GPS
				context.sendBroadcast(new Intent("Stop GPS"));
				//   Log.i(TAG,"context.sendBroadcast(new Intent(\"stop GPS\"));and  startOrstop value is:"+String.valueOf(startOrstop));
				myApp.setIs_WifiStartGPS(false);
				startOrstop=0;
			}
		}
		if(startOrstop>1){//大于1就开启GPS
			context.sendBroadcast(new Intent("Start GPS"));
			//   Log.i(TAG,"context.sendBroadcast(new Intent(\"Start GPS\"));and  startOrstop value is:"+String.valueOf(startOrstop));
			myApp.setOpengps(myApp.getOpengps()+1);
			myApp.setIs_WifiStartGPS(true);
			if(startOrstop>3)
				startOrstop=3;//
		}


		return startOrstop;
	}
	public static String bb(MyApp myApp,String _gpsdatetime,long sampling,DbAdaptor dbAdaptor,long stoptime,String otherone){
		boolean gpsdatainsert=false;//用它判断是否已经用GPS数据插入了gpsdata表

		if(null!=myApp.getGpsdatetime()){//这个IF语句是读GPS数据的程序,表明myApp有数据
			if(!_gpsdatetime.equals(myApp.getGpsdatetime())){//表明和上一条数据不同
				if(myApp.getGpsdatetime().compareTo(_gpsdatetime)>0){
					if(System.currentTimeMillis()-myApp.get_gpstime()<sampling*1000){
						_gpsdatetime=myApp.getGpsdatetime();
						try{


							if(null==otherone||!otherone.equals(MathFloor(myApp.getLongitude())+MathFloor(myApp.getLatitude()))){

								/*if(myApp.getLongitude()>110.8&&myApp.getLongitude()<110.9&&myApp.getLatitude()>21.7&&myApp.getLatitude()<21.95){
									Log.i(TAG,"GPS不用插入数据");
								}else{
								*/
								dbAdaptor.getdb().execSQL("INSERT INTO gpsdata VALUES(?, ?, ?, ?, ?,?)", new Object[]{myApp.get_gpstime(),myApp.getGpsdatetime().replace(" ","T"),myApp.get_id(),myApp.getLongitude(),myApp.getLatitude(),myApp.getSpeed()});
								//	}
								myApp.setGpsinsert(myApp.getGpsinsert()+1);
								//		Log.i(TAG,"INSERT INTO gpsdata use GPS:时间是:"+myApp.getSdf().format(System.currentTimeMillis()));
								gpsdatainsert=true;//成功用GPS数据插入表gpsdata
								dbAdaptor.getdb().execSQL("UPDATE configdata set otherone=? where idindex=?",new String[]{MathFloor(myApp.getLongitude())+MathFloor(myApp.getLatitude()),"1"});
							}
						}
						catch(Exception e){
						}
					}//else Log.i(TAG,"GPS数据时间超出10秒:时间是:"+myApp.getSdf().format(System.currentTimeMillis()));
				}
			}
		}
		if(!gpsdatainsert){//未能用GPS数据插入表,现在用WIFI数据插入表
			if(null!=myApp.getWifidatetime()){
				if(myApp.getWifilatitude()>-180&&myApp.getWifilongitude()>-180){//从AP中得到数据
					if(!_gpsdatetime.equals(myApp.getWifidatetime())){//表明和上一条数据不同
						if(myApp.getWifidatetime().compareTo(_gpsdatetime)>0){
							if((System.currentTimeMillis()-myApp.get_wifitime())<sampling*1000){//-------------
								_gpsdatetime=myApp.getWifidatetime();

								try{

									if(null==otherone||!otherone.equals(MathFloor(myApp.getWifilongitude())+MathFloor(myApp.getWifilatitude()))){

									/*	if(myApp.getWifilongitude()>110.8&&myApp.getWifilongitude()<110.9&&myApp.getWifilatitude()>21.7&&myApp.getWifilatitude()<21.95){
											Log.i(TAG,"WIFI不用插入数据");
										}
										else{*/
										dbAdaptor.getdb().execSQL("INSERT INTO gpsdata VALUES(?, ?, ?, ?, ?,?)", new Object[]{myApp.get_wifitime(),myApp.getWifidatetime().replace(" ","T"),myApp.get_id(),myApp.getWifilongitude(),myApp.getWifilatitude(),myApp.getSpeed()});
										//}
										myApp.setWifiinsert(myApp.getWifiinsert()+1);
										//	Log.i(TAG,"INSERT INTO gpsdata use WIFI:时间是:"+myApp.getSdf().format(System.currentTimeMillis()));
										dbAdaptor.getdb().execSQL("UPDATE configdata set otherone=? where idindex=?",new String[]{MathFloor(myApp.getWifilongitude())+MathFloor(myApp.getWifilatitude()),"1"});
									}


								}
								catch(Exception e){

								}
							}//else Log.i(TAG,"WIFI数据时间超出10秒:时间是:"+myApp.getSdf().format(System.currentTimeMillis()));
						}
					}
				}
			}
		}









		/*
		 * 判断myApp.getQueue()数据
		 */

		long firstrealtime=0;
		long oldrealtime=0;
		Map<String,Long> apmap=new HashMap<String,Long>();//记录每次扫描得到的AP的MAC及当时的时间
		Cursor scanapcur=dbAdaptor.getdb().rawQuery("select realtime ,bssid from scanap   order by realtime desc",null);//查数据库有没有记录
		//Log.i(TAG,"scanap counts:"+String.valueOf(scanapcur.getCount()));
		if(scanapcur.getCount()>0){
			scanapcur.moveToFirst();
			firstrealtime=scanapcur.getLong(0);
			oldrealtime=firstrealtime;

			apmap.put(scanapcur.getString(1),scanapcur.getLong(0));
			while(scanapcur.moveToNext()){
				firstrealtime=scanapcur.getLong(0);
				if(firstrealtime==oldrealtime){
					apmap.put(scanapcur.getString(1),scanapcur.getLong(0));
				}
				else{

					break;
				}
			}
		}
		scanapcur.close();


		boolean iscopy=false;
		for(Map.Entry<String,Long> lastAp : apmap.entrySet()){//Map中每一个AP
			String lastKey=lastAp.getKey();//AP的MAC
			Long   lastVal=lastAp.getValue();//时间

			Cursor scanapcura=dbAdaptor.getdb().rawQuery("select realtime ,bssid from scanap   order by realtime desc",null);//查数据库有没有记录
			int  i_count=1;//计数,用来判断是否有连续的记录
			int  ii_count=0;
			if(scanapcura.getCount()>0){
				scanapcura.moveToFirst();
				oldrealtime=scanapcura.getLong(0);
				do{
					firstrealtime=scanapcura.getLong(0);
					if(firstrealtime!=oldrealtime){
						oldrealtime=firstrealtime;
						i_count++;

					}
					if(scanapcura.getString(1).equals(lastKey)){
						ii_count++;
						if((System.currentTimeMillis()-scanapcura.getLong(0)>stoptime&&ii_count==i_count&&i_count==1&&(System.currentTimeMillis()-myApp.get_scantime()>stoptime))||((lastVal-scanapcura.getLong(0)>stoptime)&&ii_count==i_count)){//------
							iscopy=true;
						}
					}

				}while(scanapcura.moveToNext());
			}
			scanapcura.close();
		}


		if(iscopy){//如果拷贝条件成立,把myApp.getBlockingDeque()都插入apdata
			Cursor scanapcurb=dbAdaptor.getdb().rawQuery("select realtime ,bssid,havegpsdata from scanap  order by realtime desc",null);//查数据库有没有记录
			if(scanapcurb.getCount()>0){
				scanapcurb.moveToFirst();
				do{
					Long realtime=scanapcurb.getLong(0);
					String mac=scanapcurb.getString(1);
					String havegpsdata=scanapcurb.getString(2);

					Cursor cur=dbAdaptor.getdb().rawQuery("select bssid from apdata where bssid=?",new String[]{mac});//查数据库有没有记录
					try{

						if(cur.getCount()==0){

							if(havegpsdata.equals("0")){//判断数据是否要插入recordstopbefore
								dbAdaptor.getdb().execSQL("INSERT INTO recordstopbefore VALUES(?, ?)", new Object[]{realtime,mac});
							}
							dbAdaptor.getdb().execSQL("INSERT INTO apdata VALUES(?, ?, ?, ?, ?)", new Object[]{mac,-200,-200,-200,System.currentTimeMillis()});
							//Log.i(TAG,"INSERT INTO apdata:");
							Cursor curapmap=dbAdaptor.getdb().rawQuery("select apcounts from apmap where bssid=?",new String[]{mac});//查数据库有没有记录
							try{
								if(curapmap.getCount()>0){
									dbAdaptor.getdb().execSQL("UPDATE apmap set apcounts=? where bssid=?",new Object[]{1,mac});
								}
								else{
									dbAdaptor.getdb().execSQL("INSERT INTO apmap VALUES(?, ?, ?)", new Object[]{mac,1,System.currentTimeMillis()});
								}
							}
							catch(Exception e){

							}
							finally{
								curapmap.close();
							}



								/*if(myApp.getMap().containsKey(mac)){  //记录发现AP的次数，软件设定发现10次就不用再启动GPS搜索它的位置，每次关机都自动清零
									  if(myApp.getMap().get(mac)>1L){
										  myApp.getMap().remove(mac);
										  myApp.getMap().put(mac, 0L);
									  }
								}
							  	else{
							  		   myApp.getMap().put(mac, 0l);//第一次用0开始
			
							  	   }*/
						}
					}
					catch(Exception e){

					}
					finally{
						cur.close();
					}
				}while(scanapcurb.moveToNext());

			}
			scanapcurb.close();
		}

		return _gpsdatetime;
	}

}
