package com.trace.marks;

import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import org.apache.mina.core.RuntimeIoException;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.serialization.ObjectSerializationCodecFactory;
import org.apache.mina.transport.socket.SocketConnector;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.trace.shared.Sendgpsclientdata;

public class SendDataToServer {
	private static String TAGA="gpsclient.Message";
	private class ConnectListener implements IoFutureListener<ConnectFuture>{
		private List<Sendgpsclientdata> list;
		public ConnectListener(List<Sendgpsclientdata> list){
			this.list=list;
		}
		public void operationComplete(ConnectFuture future) {
			try{

				if (future.isConnected()) {

					IoSession session = future.getSession();
					Log.i(TAGA,"mina send data to server before");
					session.write(list);
					Log.i(TAGA,"mina send data to server after");
				}
			}catch(Exception e){
				Log.i(TAGA,"mina send data to server Error");
			}
			finally{
				//	future.getSession().close(isRestricted());
				//	socketConnector.dispose();
			}

		}
	}
	public  void cc(final DbAdaptor dbAdaptor,final MyApp myApp,Context context,final String serveraddress,final int minaport){
		final List<Sendgpsclientdata> list=new ArrayList<Sendgpsclientdata>();
		final int   DEFAULT_CONNECT_TIMEOUT=10;
		new Thread(){
			public void run(){
				Log.i(TAGA,"B");
				Cursor curmina=dbAdaptor.getdb().rawQuery("select realtime,gpsdatetime,_id,longitude,latitude,speed from gpsdata", null);
				try{
					int i_num=curmina.getCount();
					Log.i(TAGA,"curmina.getCount():"+String.valueOf(i_num));
					if(i_num!=0){
						SocketConnector socketConnector = new NioSocketConnector();  //非阻塞的基于TCP协议的IoConnector实现
						socketConnector.setConnectTimeoutMillis(DEFAULT_CONNECT_TIMEOUT);  //设置连接超时
						socketConnector.getFilterChain().addLast("object",        new ProtocolCodecFilter(new ObjectSerializationCodecFactory()));//设置序列化对象传输
						socketConnector.getFilterChain().addLast("loggingFilter", new org.apache.mina.filter.logging.LoggingFilter());
						ClientIoHandler ioHandler = new ClientIoHandler(myApp,dbAdaptor);
						socketConnector.setHandler(ioHandler);  //
						InetSocketAddress addr = new InetSocketAddress(serveraddress, minaport);  //配置服务器地址 ,端口-------------------
						ConnectFuture cf  = socketConnector.connect(addr);  //连接到服务器
						curmina.moveToFirst();
						Log.i(TAGA,"curmina.moveToFirst");
						do{
							Sendgpsclientdata gpsclientdata=new Sendgpsclientdata();
							gpsclientdata.setRealtime(curmina.getLong(0));
							gpsclientdata.setGpsdatetime(myApp.getSdf().parse(curmina.getString(1).replace("T"," ")));
							gpsclientdata.setUserid(curmina.getInt(2));
							gpsclientdata.setLongitude(curmina.getDouble(3));
							gpsclientdata.setLatitude(curmina.getDouble(4));
							gpsclientdata.setSpeed(curmina.getFloat(5));
							list.add(gpsclientdata);
						}while(curmina.moveToNext());
						Log.i(TAGA,"curmina.moveToNext end");
						try {
							cf.addListener(new ConnectListener(list));
						} catch (RuntimeIoException e) {
							if (e.getCause() instanceof ConnectException) {
								try {
									if (cf.isConnected()) {
										cf.getSession().close(true);
									}
								} catch (RuntimeIoException e1) {
									Log.i(TAGA,e1.toString());
								}
							}
						}
					}
				}catch(Exception e){
					Log.i(TAGA,"Cursor database error");
				}
				finally{
					curmina.close();
				}
			}
		}.start();
	}

}
