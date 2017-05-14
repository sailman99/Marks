package com.trace.marks;



import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "gpsclient.db";
	private static final int DATABASE_VERSION = 1;
	private   Context context;
	public DBHelper(Context context) {
		//CursorFactory设置为null,使用默认值
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		this.context=context;
	}


	@Override
	public void onCreate(SQLiteDatabase db) {
		try{
			// TODO Auto-generated method stub
			// db.execSQL("DROP TABLE IF EXISTS   gpsdate");
			//	 db.execSQL("CREATE TABLE IF NOT EXISTS gpsdata(gpsdatetime DATETIME PRIMARY KEY ,_id INTEGER,  longitude REAL, latitude REAL,speed REAL,blongitude REAL, blatitude REAL)");
			db.execSQL("CREATE TABLE IF NOT EXISTS gpsdata(realtime  INT8 PRIMARY KEY,gpsdatetime DATETIME  ,_id INTEGER,  longitude REAL, latitude REAL,speed REAL)");
			//db.execSQL("CREATE TABLE IF NOT EXISTS gpsdata(gpsdatetime DATETIME PRIMARY KEY,_id INTEGER,  longitude REAL, latitude REAL,speed REAL)");
			db.execSQL("CREATE TABLE IF NOT EXISTS apdata(bssid char(17) PRIMARY KEY ,singlelevel INTEGER,longitude REAL ,latitude REAL,realtime  INT8)");

			 /*
			  *havegpsdata 这是判断当前这时间扫描的AP在30秒内是否有GPS数据,包含WIFI有的GPS数据,有是"1",无是"0"
			  *判断gps或wifi时间是否30秒内有数据
			  */

			db.execSQL("CREATE TABLE IF NOT EXISTS scanap(realtime  INT8 ,bssid char(17),havegpsdata char(1),constraint pk_scanap primary key (realtime ,bssid))");
			//

			 /*
			  * 在插入apdata数据时,把scanap表字段havegpsdata为"0"的数据插入recordstopbefore,这个表的数据保留24小时
			  * 当apdata表的数据有更新的时候,查询recordstopbefore,把所有对应的记录都提出来,然后用更新apdata所对应的GPS数据
			  * 插入gpsdata,速度用-200表示,然后根据时间删除表recordstopbefore所对应的记录.
			  * 代码修改地方
			  * 1.插入apdata数据时
			  * 2.apdata表的数据有更新的时候
			  *
			  *
			  */
			db.execSQL("CREATE TABLE IF NOT EXISTS recordstopbefore(realtime  INT8 ,bssid char(17),constraint pk_recordstopbefore primary key (realtime ,bssid))");



			db.execSQL("CREATE TABLE IF NOT EXISTS apmap(bssid char(17) PRIMARY KEY,apcounts INTEGER,realtime  INT8)");
			db.execSQL("CREATE TABLE IF NOT EXISTS timekeeper(idindex char(1) PRIMARY KEY,senddata INT8,sendmsg  INT8)");

			Cursor curtimekeeper=db.rawQuery("select idindex from timekeeper", null);//查数据库有没有记录
			if(curtimekeeper.getCount()==0){
				db.execSQL("INSERT INTO timekeeper VALUES(?,?,?)",new Object[]{"1",0,0});
			}
			curtimekeeper.close();



			 /*
			  *idindex主键， _id用户识别号,limited工作时间限制，sampling取样时间，senddatacounts发送数据所用时间间隔（有一计数器counts,每一抽样循环加一，然后用公式counts%senddatacounts==0来判断，这样就形成一时间周期sampling*senddatacounts）,uppublic公共升级版本，upprivate稀有升级版本
			  *stoptime定义一停留时间，upstoptime定义停留时间之前的时间段,单位都是毫秒
			  *twoApMaxDistance两个AP最大距离，判断是否为移动AP，单位是米
			  *maxtimespile最大时间间隔，判断从GPS或WIFI中读取的数据是不是在时间间隔中，单位是毫秒
			  *0.主键"1"
			  *1。用户ID
			  *2.mina端口1236
			  *3.json端口
			  *4。mina服务器地址
			  *5。mina服务器地址
			  *6。mina服务器地址
			  *7.mina服务器地址
			  *8.mina服务器地址
			  *9.手机从服务器得到更新数据的地址
			  *10。这台手机工作时限
			  *11。取样频率，单位秒
			  *12。发送数据时用到的计数
			  *13。 停留时间，是计算手机停留在某一地方的时间，单位是毫秒
			  *14。停留之前的时间，这时间是在停留之前的时间
			  *15。两个AP最大距离，判断是否为移动AP，单位是米
			  *16。最大时间间隔，判断从GPS或WIFI中读取的数据是不是在时间间隔中，单位是毫秒
			  *17。公共版本控制
			  *18。私有版本控制
			  *
			  */
			db.execSQL("CREATE TABLE IF NOT EXISTS configdata(idindex char(1) PRIMARY KEY," +
					"_id integer," +
					"minaport integer," +
					"jsonport char(6)," +
					"server char(50)," +
					"server1 char(50)," +
					"server2 char(50)," +
					"server3 char(50)," +
					"server4 char(50)," +
					"server5 char(50)," +
					"server6 char(50)," +
					"server7 char(50)," +
					"server8 char(50)," +
					"server9 char(50)," +
					"server10 char(50)," +
					"server11 char(50)," +
					"server12 char(50)," +
					"server13 char(50)," +
					"server14 char(50)," +
					"webserver char(50)," +
					"limited INT8," +
					"sampling integer," +
					"senddatacounts integer," +
					"stoptime integer," +
					"upstoptime integer," +
					"twoapmaxdistance integer," +
					"maxtimespile integer," +
					"uppublic char(2)," +
					"upprivate char(2)," +
					"sendtimespile integer," +
					"issend char(1)," +
					"otherone char(100)," +
					"othertwo char(100)," +
					"realtimechecked char(32)," +
					"checked char(32))");
			Cursor cur=db.rawQuery("select idindex from configdata", null);//查数据库有没有记录
			if(cur.getCount()==0){
				//db.execSQL("INSERT INTO configdata VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",new Object[]{"1",null,Integer.valueOf(context.getResources().getString(R.string.minaport)),context.getResources().getString(R.string.jsonport),context.getResources().getString(R.string.serveraddr0),null,null,null,null,null,null,null,null,null,null,null,null,null,null,context.getResources().getString(R.string.serveraddr0),null,10,30,20*60*1000,10*60*1000,1000,10*1000,"1","1",60*60*1000,"1",null,null,null,null});//初始化取样时间10秒，发送数据时间10*30秒，停留时间是15分钟，停留之前的时间是15分钟,最大距离1000米，最大时间间隔10*1000毫秒
				db.execSQL("INSERT INTO configdata VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",new Object[]{"1",30000001,Integer.valueOf(context.getResources().getString(R.string.minaport)),context.getResources().getString(R.string.jsonport),context.getResources().getString(R.string.serveraddr0),null,null,null,null,null,null,null,null,null,null,null,null,null,null,context.getResources().getString(R.string.serveraddr0),null,10,30,20*60*1000,10*60*1000,1000,10*1000,"1","1",60*60*1000,"1",null,null,null,null});//初始化取样时间10秒，发送数据时间10*30秒，停留时间是15分钟，停留之前的时间是15分钟,最大距离1000米，最大时间间隔10*1000毫秒
			}
			cur.close();
		}catch(Exception e){
			System.out.println(e.toString());
		}

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}

}
