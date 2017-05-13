package com.trace.marks;


import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * 用于检测手机摇晃
 *
 * @author 郑智仁
 * @see <a href="http://blog.csdn.net/zhengzhiren" mce_href="http://blog.csdn.net/zhengzhiren">Blog</a>
 */
public class ShakeDetector implements SensorEventListener {

	//private Handler handler;
	public Queue<Double> queue = new LinkedList<Double>();
	private int queuesize=20;
	private double queueAverage=10d;
//	private MyApp myApp;
//	private final static String TAG="gpsclient.Message";
//	private SimpleDateFormat sdfShakeDetector=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.CHINA);
	/**
	 * 检测的时间间隔
	 */
	static final int UPDATE_INTERVAL = 100;//检测的时间间隔0.1秒
	/**
	 * 上一次检测的时间
	 */
	long mLastUpdateTime;
	/**
	 * 上一次检测时，加速度在x、y、z方向上的分量，用于和当前加速度比较求差。
	 */
	float mLastX, mLastY, mLastZ;
	Context mContext;
	SensorManager mSensorManager;
	ArrayList<OnShakeListener> mListeners;
	/**
	 * 摇晃检测阈值，决定了对摇晃的敏感程度，越小越敏感。
	 */
	public int shakeThreshold = 300;
	public double quietThreshold = 1.0d;
	public ShakeDetector(Context context) {//构筑函数
		mContext = context;
		mSensorManager = (SensorManager) context
				.getSystemService(Context.SENSOR_SERVICE);
		mListeners = new ArrayList<OnShakeListener>();
		//Log.i(TAG,"构筑函数");
	}
	/**
	 * 当摇晃事件发生时，接收通知
	 */
	public interface OnShakeListener {
		/**
		 * 当手机摇晃时被调用
		 */
		//void onShake(Context context,Handler handler);
		void onShake(Context context);
		void onQuiet(Context context);
	}
	/**
	 * 注册OnShakeListener，当摇晃时接收通知
	 *
	 * @param listener
	 */
	public void registerOnShakeListener(OnShakeListener listener) {
		if (mListeners.contains(listener))
			return;
		mListeners.add(listener);
		start();
	}
	/**
	 * 移除已经注册的OnShakeListener
	 *
	 * @param listener
	 */
	public void unregisterOnShakeListener(OnShakeListener listener) {
		stop();
		mListeners.remove(listener);

	}


	/**
	 * 启动摇晃检测
	 */
	//public void start(Handler handler) {
	public void start() {
		//this.handler=handler;
		//	Log.i("shake","add queue size on start"+String.valueOf(queue.size()));
		if(queueAverage>5){//这个数是临时放在这，以后再判断
			if(queue.size()==0){
				for(int i=0;i<queuesize;i++)
					queue.add(queueAverage);
			}
			//		Log.i("shake","add queue size"+String.valueOf(queue.size()));


			if (mSensorManager == null) {
				throw new UnsupportedOperationException();
			}
			Sensor sensor = mSensorManager
					.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
			if (sensor == null) {
				throw new UnsupportedOperationException();
			}
			boolean success = mSensorManager.registerListener(this, sensor,
					SensorManager.SENSOR_DELAY_GAME);
			if (!success) {
				throw new UnsupportedOperationException();
			}
		}
	}
	/**
	 * 停止摇晃检测
	 */
	public void stop() {
		for (OnShakeListener listener : mListeners) {		//先推出GPS

			//queueAverage=10;
			listener.onQuiet(mContext);
		}
		if (mSensorManager != null)
			mSensorManager.unregisterListener(this);
	}
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
	}
	@Override
	public void onSensorChanged(SensorEvent event) {
		long currentTime = System.currentTimeMillis();
		long diffTime = currentTime - mLastUpdateTime;
		if (diffTime < UPDATE_INTERVAL)
			return;
		mLastUpdateTime = currentTime;
		float x = event.values[0];
		float y = event.values[1];
		float z = event.values[2];
		float deltaX = x - mLastX;
		float deltaY = y - mLastY;
		float deltaZ = z - mLastZ;
		mLastX = x;
		mLastY = y;
		mLastZ = z;
		//	float delta = FloatMath.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ*deltaZ)/diffTime*10000;
		double delta = java.lang.Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ*deltaZ)/diffTime*10000;
		queueAverage=queueAverage+(delta-queue.poll())/queuesize;
		queue.offer(delta);
		if (queueAverage < quietThreshold)
			this.notifyListeners(false);
		if (queueAverage > shakeThreshold)
			this.notifyListeners(true);
	}
	/**
	 * 当摇晃事件发生时，通知所有的listener
	 */
	private void notifyListeners(Boolean Shake ) {
		for (OnShakeListener listener : mListeners) {
			if(Shake){
				//listener.onShake(mContext,handler);
				listener.onShake(mContext);
				//	Log.i(TAG,"感应器应晃动而发出信号");
			}
			else{
				listener.onQuiet(mContext);
				//	Log.i(TAG,"感应器应静止而发出信号");
			}
		}
	}
}