package com.trace.marks;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.mina.core.RuntimeIoException;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.json.JSONArray;
import org.json.JSONObject;

import com.trace.shared.ResultMessage;

import android.app.Application;
//import android.util.Log;

public class ClientIoHandler extends IoHandlerAdapter {


	private DbAdaptor dbAdaptor;
	private MyApp myApp;
	//	    private final Set<IoSession> sessions = Collections.synchronizedSet(new HashSet<IoSession>());
	// private final static String TAG="gpsclient.Message";
	public String readpublicsqlFromGPSServer(String Url){
		 	/*
		 	 *从服务器返回的值idtype:0.需要更新数据
		 	 *                 1.提示
		 	 *                 2.帐号或密码错
		 	 *          message:信息
		 	 *          limitime:时间
		 	 */
		StringBuilder builder = new StringBuilder();
		HttpClient httpclient = new DefaultHttpClient();

		try{
			HttpPost httppost = new HttpPost(Url);
			//httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			HttpResponse response = httpclient.execute(httppost);
			StatusLine statusLine = response.getStatusLine();
			int statusCode = statusLine.getStatusCode();
			if (statusCode == 200) { //从服务器返回成功
				HttpEntity entity = response.getEntity();
				InputStream content = entity.getContent();
				BufferedReader reader = new BufferedReader(new InputStreamReader(content));
				String line;
				while ((line = reader.readLine()) != null) {
					builder.append(line);
				}
				JSONArray jsonArray =new JSONArray(builder.toString());
				JSONObject jsonObject = jsonArray.getJSONObject(0);
				if(null!=jsonObject.getString("updatesql")){
					dbAdaptor.getdb().execSQL(jsonObject.getString("updatesql"));
					dbAdaptor.getdb().execSQL("UPDATE configdata set uppublic=?",new String[]{jsonObject.getString("uppublic")});
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally{
			httpclient.getConnectionManager().shutdown();
		}
		return builder.toString();
	}


	private void releaseSession(IoSession session) throws Exception {

		if (session.isConnected()) {

			session.close(true);
		}
	}
	public ClientIoHandler(Application application,DbAdaptor dbAdaptor){
		this.dbAdaptor=dbAdaptor;
		myApp=(MyApp)application;
	}
	@Override
	public void sessionOpened(IoSession session) throws Exception {

	}

	@Override
	public void sessionClosed(IoSession session) throws Exception {

	}

	@Override
	public void sessionIdle(IoSession session, IdleStatus status) throws Exception {

		try {
			releaseSession(session);
		} catch (RuntimeIoException e) {
		}
	}

	@Override
	public void messageReceived(IoSession session, Object message) throws Exception {  //消息接受完毕

		ResultMessage rm = (ResultMessage) message;

		if (rm.isOk()) {

			if("Gpsclientdata".equals(rm.getCheck())){
				dbAdaptor.getdb().delete("gpsdata", null, null);
			}
			if("Gpsworkstatic".equals(rm.getCheck())){
				myApp.setOpengps(0);
				myApp.setOpengpscanuse(0);
				myApp.setGpsstart(0);
				myApp.setGpsinsert(0);
				myApp.setWifiinsert(0);
			}
			if(null!=rm.getPrivatesql()){
				dbAdaptor.getdb().execSQL(rm.getPrivatesql());
			}



			if(!myApp.getUppublic().equals(rm.getUppublic())){
				final String url="http://"+myApp.getMinaserver()+":"+myApp.getJsonport()+"/gpsserver/GetpublicsqlAction";
				new Thread(){
					public void run(){
						readpublicsqlFromGPSServer(url);
					}
				}.start();
			}


			releaseSession(session);
			//dbAdaptor.release();
		}
	}

	@Override
	public void exceptionCaught(IoSession session, Throwable cause) throws Exception {

		cause.printStackTrace();
		releaseSession(session);
	}

	@Override
	public void messageSent(IoSession session, Object message) throws Exception {

		super.messageSent(session, message);
	}

}
