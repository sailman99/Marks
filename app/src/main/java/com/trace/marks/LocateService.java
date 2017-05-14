package com.trace.marks;

import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.mina.core.RuntimeIoException;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.serialization.ObjectSerializationCodecFactory;
import org.apache.mina.transport.socket.SocketConnector;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import com.trace.shared.Gpsworkstatic;
import com.trace.shared.Sendgpsclientdata;
import com.trace.marks.MyApp;




import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.os.StrictMode;
import android.util.Log;



public class LocateService extends Service {


	//	private TelephonyManager manager ;
//	private CellLocation cellLocation;
	private ShakeDetector shakeDetector;
	private MyOnShakeListener myOnShakeListener;
	private static String _gpsdatetime=" ";
	private MyApp myApp;
	//private static int timecounts=0;
	public static final long DEFAULT_CONNECT_TIMEOUT = 30*1000L;    //10秒时间内如果连接不到服务器,自动断开连接
	private static List<Sendgpsclientdata> list;
	private  WifiManager.WifiLock mWifiLock;
	private boolean opened = false;
	private final static String TAG="gpsclient.Message";
	private DbAdaptor dbAdaptor;
	// private int iswifiopengpscounts=0;
	//private double[] dd={0d,0d};
	// private Map<String,Long> map=new HashMap<String, Long>();//记录发现AP的次数,第一参数是AP的MAC,第二参数是扫描次数
	// private long Gpsworkstatictime=System.currentTimeMillis();
	//  private long senddatatime=System.currentTimeMillis();
	//  private int  senddatacounts=0;
	//  private int  sendGpsworkstaticCount=0;
	private Gpsworkstatic gpsworkstatic  = new Gpsworkstatic();
	private class ConnectListenerGpsworkstatic implements IoFutureListener<ConnectFuture>{
		public void operationComplete(ConnectFuture future) {
			try{
				if (future.isConnected()) {
					IoSession session = future.getSession();
					session.write(gpsworkstatic);
					session.getConfig().setUseReadOperation(true);
				}
			}catch(Exception e){

			}

		}
	}

	private class ConnectListener implements IoFutureListener<ConnectFuture>{
		public void operationComplete(ConnectFuture future) {
			try{
				if (future.isConnected()) {
					IoSession session = future.getSession();
					session.write(list);
					session.getConfig().setUseReadOperation(true);
				}
			}catch(Exception e){

			}

		}
	}
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	/**
	 * 检测是否主程序是否还在运行
	 * @return
	 */


	public void onCreate() {
		// TODO Auto-generated method stub
		//super.onCreate();

		Log.i(TAG, "LocateService线程_onCreate" );
		myApp=(com.trace.marks.MyApp)this.getApplication();

		myApp.setSendsuccess(false);//初始发送数据是不成功的
		dbAdaptor=DbAdaptor.getInstance(getApplicationContext());
		dbAdaptor.getdb().execSQL("delete from  apdata where realtime<? and singlelevel<?",new Object[]{System.currentTimeMillis()-7776000000l,-180});
		dbAdaptor.getdb().execSQL("delete from  apdata where realtime<? ",new Object[]{System.currentTimeMillis()-31536000000l});


		shakeDetector = new ShakeDetector(this);
		myOnShakeListener=new MyOnShakeListener();
		shakeDetector.registerOnShakeListener(myOnShakeListener);

		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
		intentFilter.addAction(Intent.ACTION_SCREEN_ON);
		intentFilter.addAction("Stop GPS");
		intentFilter.addAction("Start GPS");
		//intentFilter.addAction("WIFI Connected then Start GPS");
		registerReceiver(new ScreenStatusListener(this.getApplication(),getBaseContext()), intentFilter);


		IntentFilter wifiIntent = new IntentFilter();
		wifiIntent.addAction (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
		registerReceiver(new WifiScanListener(this.myApp,dbAdaptor), wifiIntent);

		 /*

		 manager = (TelephonyManager) this.getSystemService(TELEPHONY_SERVICE);
	        // 手动注册对PhoneStateListener中的listen_call_state状态进行监听
	     manager.listen(new MyPhoneStateListener(myApp), PhoneStateListener.LISTEN_CELL_LOCATION);
		 manager.listen(new MyPhoneStateListener(myApp),PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
		 cellLocation = manager.getCellLocation();
		 */
		/*
		 manager.listen(new MyPhoneStateListener(dbAdaptor,myApp),PhoneStateListener.LISTEN_CALL_FORWARDING_INDICATOR);
		 manager.listen(new MyPhoneStateListener(dbAdaptor,myApp), PhoneStateListener.LISTEN_CELL_INFO);
		 manager.listen(new MyPhoneStateListener(dbAdaptor,myApp), PhoneStateListener.LISTEN_MESSAGE_WAITING_INDICATOR);
		 manager.listen(new MyPhoneStateListener(dbAdaptor,myApp), PhoneStateListener.LISTEN_SERVICE_STATE);
		 manager.listen(new MyPhoneStateListener(dbAdaptor,myApp), PhoneStateListener.LISTEN_DATA_ACTIVITY);
		*/


		myApp.set_gpstiming(System.currentTimeMillis());//初始化GPS计时器时间


		StrictMode.ThreadPolicy policy = new StrictMode.
				ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);


		Cursor cur=dbAdaptor.getdb().rawQuery("select _id from configdata where idindex=?",new String[]{"1"});//查数据库有没有记录
		try{
			if(cur.getCount()>0){
				cur.moveToFirst();
				myApp.set_id(cur.getInt(0));
			}
		}
		catch(Exception e){

		}
		finally{
			cur.close();
		}

	}

	private boolean isNetworkAvailable(Context con)
	{
		ConnectivityManager cm = (ConnectivityManager)con.getSystemService(Context.CONNECTIVITY_SERVICE);
		if( cm == null )
			return false;
		NetworkInfo netinfo = cm.getActiveNetworkInfo();
		if (netinfo == null )
			return false;
		if(netinfo.isConnected())
			return true;
		return false;
	}


//	@Override


	public int onStartCommand(Intent intent, int flags, int startId) {


		Log.i(TAG, "onStartCommand,时间是:"+myApp.getSdf().format(System.currentTimeMillis()));

		// sendGpsworkstaticCount++;


		//	cellLocation.requestLocationUpdate();




		Long senddatatmp=0l;
		Long sendmsgtmp=0l;
		Cursor curtimekeeper=dbAdaptor.getdb().rawQuery("select idindex,senddata,sendmsg from timekeeper where idindex=?",new String[]{"1"});
		try{
			if(curtimekeeper.getCount()>0){
				curtimekeeper.moveToFirst();
				senddatatmp=curtimekeeper.getLong(1);
				sendmsgtmp =curtimekeeper.getLong(2);
			}
		}
		catch(Exception e){

		}
		finally{
			curtimekeeper.close();
		}

		final Long senddata=senddatatmp;
		final Long sendmsg=sendmsgtmp;




		Cursor maincur=dbAdaptor.getdb().rawQuery("select _id,sampling,upstoptime,stoptime,senddatacounts,minaport,uppublic,jsonport,limited,realtimechecked,checked,sendtimespile,issend,otherone,server,server1,server2,server3,server4,server5,server6,server7,server8,server9,server10,server11,server12,server13,server14,webserver from configdata where idindex=?",new String[]{"1"});//查数据库有没有记录
		try{
			if(maincur.getCount()>0){
				maincur.moveToFirst();
				myApp.set_id(maincur.getInt(0));//userid
				int sampling=maincur.getInt(1);//取样间隔,单位是秒
				myApp.setSampling(sampling);
				int upstoptime=maincur.getInt(2);//定义停留之前时间,单位是毫秒
				int stoptime=maincur.getInt(3);//定义停留时间,单位是毫秒
				int senddatacounts=maincur.getInt(4);//定义发送数据计数,它乘取样间隔就是发送时间
				final int minaport=maincur.getInt(5);//定义MINA端口号
				myApp.setUppublic(maincur.getString(6));//uppublic
				String jsonport=maincur.getString(7);//定义JSON端口号
				Long limited=maincur.getLong(8);//工作时间期限
				final String realtimechecked=maincur.getString(9).trim();
				final String checked=maincur.getString(10).trim();//发送数据校验
				final Long sendtimespile=maincur.getLong(11);
				final String issend=maincur.getString(12);
				final String otherone=maincur.getString(13);
				String[] server = new String[15];//15个服务器IP地址
				int servercount=0;
				for(int i=0;i<15;i++){
					if(null!=maincur.getString(i+14)){
						server[i]=maincur.getString(i+14);
						servercount++;
					}
				}

				java.util.Random random=new java.util.Random();
				int serverselect=random.nextInt(servercount);
				final String serveraddress=server[serverselect];



				WifiManager wm = (WifiManager)this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);


				if(wm != null){

					if(mWifiLock==null){
						mWifiLock = wm.createWifiLock("WifiService");//创建一个 WifiService
						mWifiLock.setReferenceCounted(true);
					}
					if(!mWifiLock.isHeld())mWifiLock.acquire();//得到锁

				}

					/*
					 * 1。WIFI 管理,搜索AP，看有没有AP在数据库里没有记录，或有记录，但信号强度比当前的小，负75以下都算，启动GPS，如果一个AP都没有，不启动GPS。
					 * 2。如果AP都有记录，并且信号强度都小于记录，或者记录的信号强度都大于负75的，选第一个AP记录的GPS作为当前的位置
					 * 3。读MyApp数据，如果gpsdatetime数据不为NULL，则当前数据为有效GPS数据，可以用它来更新AP记录
					 */


				boolean is_Connect=false;
				ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

				if (null!=cm){
					NetworkInfo[] netInfo = cm.getAllNetworkInfo();
					for (NetworkInfo ni : netInfo) {
						if ((ni.getTypeName().equalsIgnoreCase("WIFI")&ni.isConnected()&ni.isAvailable())){
							is_Connect=true;
								/*
								 * 2013-10-02 00:11:00 begin
								 */
								/*
								if(!myApp.isIs_WifiOpenGPS()){
									myApp.setIs_WifiOpenGPS(true);
								//	iswifiopengpscounts=0;
									sendBroadcast(new Intent("WIFI Connected then Start GPS"));
									Log.i(TAG, "连接上互联网，现在启动从互联网获得GPS数据,时间是:"+myApp.getSdf().format(System.currentTimeMillis()));
								}
								*/
								/*
								else{
									if(iswifiopengpscounts>3)
										sendBroadcast(new Intent("Stop GPS"));
									else
										iswifiopengpscounts++;
								}*/
								/*
								 * 2013-10-02 00:11:00 end
								 */

						}
					}
				}
				if(!is_Connect){
					wm.startScan();
					//	myApp.setIs_WifiOpenGPS(false);
					myApp.set_gpstiming(System.currentTimeMillis());//更新GPS计时器时间

				}
				else{


					if(System.currentTimeMillis()-myApp.get_gpstiming()>3*60*1000){//3分钟没有变化,关闭GPS
						myApp.set_gpstiming(System.currentTimeMillis());//更新GPS计时器时间
						myApp.setIs_WifiStartGPS(false);//???
						sendBroadcast(new Intent("Stop GPS"));
						//	Log.i(TAG, "连上互联网有三分钟，发出关闭GPS信号,时间是:"+myApp.getSdf().format(System.currentTimeMillis()));
					}

				}
				dbAdaptor.getdb().delete("scanap","realtime<"+(System.currentTimeMillis()-(stoptime+upstoptime)),null);
				dbAdaptor.getdb().delete("recordstopbefore","realtime<"+(System.currentTimeMillis()-24*60*60*1000),null);
				String tmprealtimechecked=MD5Util.MD5(getBaseContext().getResources().getString(R.string.realtimechecked)+String.valueOf(limited)+String.valueOf(myApp.get_id())+checked);
				if(System.currentTimeMillis()<limited&&tmprealtimechecked.equals(realtimechecked)){//时间限制

					_gpsdatetime = Tools.bb(myApp,_gpsdatetime,sampling,dbAdaptor,stoptime,otherone);

				}

				if(isNetworkAvailable(getBaseContext())){//判断网络是否可用
					//if(++timecounts%senddatacounts==0){//前面设定的时间触发点是10秒,这等式表明是3*10为一周期------------------
					//	timecounts=0;
					if((System.currentTimeMillis()-senddata>senddatacounts*sampling*1000)){
						//dbAdaptor.getdb().execSQL("UPDATE timekeeper set senddata=? where idindex=?",new Object[]{System.currentTimeMillis(),"1"});
						myApp.setMinaserver(serveraddress);
						myApp.setJsonport(jsonport);

						new Thread(){
							public void run(){
								Cursor curmina=dbAdaptor.getdb().rawQuery("select realtime,gpsdatetime,_id,longitude,latitude,speed from gpsdata", null);
								//		Log.i(TAG,"curmina.row:"+String.valueOf(curmina.getCount()));
								try{
									list=new ArrayList<Sendgpsclientdata>();
									int i_num=curmina.getCount();
									if(i_num!=0){
										dbAdaptor.getdb().execSQL("UPDATE timekeeper set senddata=? where idindex=?",new Object[]{System.currentTimeMillis(),"1"});
										SocketConnector socketConnector = new NioSocketConnector();  //非阻塞的基于TCP协议的IoConnector实现
										socketConnector.setConnectTimeoutMillis(DEFAULT_CONNECT_TIMEOUT);  //设置连接超时
										socketConnector.getFilterChain().addLast("object",        new ProtocolCodecFilter(new ObjectSerializationCodecFactory()));//设置序列化对象传输
										socketConnector.getFilterChain().addLast("loggingFilter", new org.apache.mina.filter.logging.LoggingFilter());
										ClientIoHandler ioHandler = new ClientIoHandler(myApp,dbAdaptor);
										socketConnector.setHandler(ioHandler);  //
										InetSocketAddress addr = new InetSocketAddress(serveraddress, minaport);  //配置服务器地址 ,端口-------------------
										ConnectFuture cf  = socketConnector.connect(addr);  //连接到服务器
										curmina.moveToFirst();
										do{
											Sendgpsclientdata gpsclientdata=new Sendgpsclientdata();
											gpsclientdata.setRealtime(curmina.getLong(0));
											gpsclientdata.setGpsdatetime(myApp.getSdf().parse(curmina.getString(1).replace("T"," ")));
											gpsclientdata.setUserid(curmina.getInt(2));
											gpsclientdata.setLongitude(curmina.getDouble(3));
											gpsclientdata.setLatitude(curmina.getDouble(4));
											gpsclientdata.setSpeed(curmina.getFloat(5));
											gpsclientdata.setTtc(checked);
											list.add(gpsclientdata);
										}while(curmina.moveToNext());
										try {
											//		sendGpsworkstaticCount=0;
											cf.addListener(new ConnectListener());
										} catch (RuntimeIoException e) {
											if (e.getCause() instanceof ConnectException) {
												try {
													if (cf.isConnected()) {
														cf.getSession().close(true);
													}
												} catch (RuntimeIoException e1) {
												}
											}
										}
									}
									curmina.close();
								}
								catch(Exception e){

								}
								finally{

									curmina.close();

								}
							}
						}.start();
					}//上面的IF语句是发送数据到服务器的程序

					 /*
					  * send Gpsworkstatic
					  */
					//sendtimespile,issend



					if(System.currentTimeMillis()<limited&&tmprealtimechecked.equals(realtimechecked)){//时间限制
						new Thread(){
							public void run(){

								if("1".equals(issend)&&(System.currentTimeMillis()-sendmsg>sendtimespile)){
									dbAdaptor.getdb().execSQL("UPDATE timekeeper set sendmsg=? where idindex=?",new Object[]{System.currentTimeMillis(),"1"});
									// Gpsworkstatictime=System.currentTimeMillis();
									SocketConnector socketConnector = new NioSocketConnector();  //非阻塞的基于TCP协议的IoConnector实现
									socketConnector.setConnectTimeoutMillis(DEFAULT_CONNECT_TIMEOUT);  //设置连接超时
									socketConnector.getFilterChain().addLast("object",        new ProtocolCodecFilter(new ObjectSerializationCodecFactory()));//设置序列化对象传输
									socketConnector.getFilterChain().addLast("loggingFilter", new org.apache.mina.filter.logging.LoggingFilter());
									ClientIoHandler ioHandler = new ClientIoHandler(myApp,dbAdaptor);
									socketConnector.setHandler(ioHandler);  //
									InetSocketAddress addr = new InetSocketAddress(serveraddress, minaport);  //配置服务器地址 ,端口-------------------
									ConnectFuture cf  = socketConnector.connect(addr);  //连接到服务器

									gpsworkstatic.setUserid(myApp.get_id());
									gpsworkstatic.setRealtime(System.currentTimeMillis());
									gpsworkstatic.setGpsdatetime(new Date());
									Cursor cur=dbAdaptor.getdb().rawQuery("select count(*) from apdata",null);//查数据库有没有记录
									try{
										cur.moveToFirst();
										if(cur.getCount()>0){
											gpsworkstatic.setApdata(cur.getInt(0));
										}
									}
									catch(Exception e){

									}
									finally{
										cur.close();
									}

									Cursor cura=dbAdaptor.getdb().rawQuery("select count(*) from apdata where singlelevel >-180",null);//查数据库有没有记录
									try{
										cura.moveToFirst();
										if(cura.getCount()>0){
											gpsworkstatic.setApdatasignlevel(cura.getInt(0));
										}
									}
									catch(Exception e){

									}
									finally{
										cura.close();
									}
									Cursor curb=dbAdaptor.getdb().rawQuery("select count(*) from scanap",null);//查数据库有没有记录
									try{
										curb.moveToFirst();
										if(curb.getCount()>0){
											gpsworkstatic.setScanap(curb.getInt(0));
										}
									}
									catch(Exception e){

									}
									finally{
										curb.close();
									}

									Cursor curc=dbAdaptor.getdb().rawQuery("select count(*) from apmap",null);//查数据库有没有记录
									try{
										curc.moveToFirst();
										if(curc.getCount()>0){
											gpsworkstatic.setMapcount(curc.getInt(0));
										}
									}
									catch(Exception e){

									}
									finally{
										curc.close();
									}


									gpsworkstatic.setChecked(checked);
									gpsworkstatic.setOpengps(myApp.getOpengps());
									gpsworkstatic.setOpengpscanuse(myApp.getOpengpscanuse());
									gpsworkstatic.setGpsstart(myApp.getGpsstart());
									gpsworkstatic.setGpsinsert(myApp.getGpsinsert());
									gpsworkstatic.setWifiinsert(myApp.getWifiinsert());
									LocationManager lm=(LocationManager)getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
									if(lm.isProviderEnabled(LocationManager.GPS_PROVIDER)){
										gpsworkstatic.setGpscanuse(android.os.Build.VERSION.SDK_INT);
									}
									else{
										gpsworkstatic.setGpscanuse(android.os.Build.VERSION.SDK_INT*(-1));
									}



									try {
										cf.addListener(new ConnectListenerGpsworkstatic());
									} catch (RuntimeIoException e) {
										if (e.getCause() instanceof ConnectException) {
											try {
												if (cf.isConnected()) {
													cf.getSession().close(true);
												}
											} catch (RuntimeIoException e1) {
											}
										}
									}

								}//--
							}
						}.start();

					}



				}

			}

		}catch(Exception e){

		}
		finally{

			maincur.close();
		}



		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		shakeDetector.unregisterOnShakeListener(myOnShakeListener);
		// TODO Auto-generated method stub
		if(mWifiLock != null && mWifiLock.isHeld())mWifiLock.release();
		if(opened){
			WifiManager wm = (WifiManager)this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
			if(wm != null)wm.setWifiEnabled(false);
		}
		dbAdaptor.release();
		super.onDestroy();

	}




}
