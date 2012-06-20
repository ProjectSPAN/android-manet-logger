/* 
SPAN - Smart Phone AdHoc Networking project
©2012 The MITRE Corporation
*/
package android.adhoc.manet.logger;


import java.util.Timer;
import java.util.TimerTask;

import android.adhoc.manet.logger.ManetLoggerService;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.TextView;

public class ManetLoggerActivity extends Activity {
	
	private static final String TAG = "ManetLoggerActivity";
	
	private ManetLoggerService logService;
	
	private String[] fields = null;
	
	private static int _TIMESTAMP = 0;
	private static int _LATITUDE = 1;
	private static int _LONGITUDE = 2;
	private static int _BATTERY = 3;
	private static int _VOLTAGE = 4;
	private static int _TEMPERATURE = 5;
	private static int _MINFO = 6;
	
	private static int UPDATE_WAIT_TIME_MILLISEC = 1000;
	
	private Timer timer = null;
	
	private Handler handler = new Handler();
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main2);
        doBindService();
    }
    
    @Override
    public void onPause(){
    	super.onPause();
		if(timer != null){
			timer.cancel();
		}
    }
    
    @Override
    public void onResume(){
    	super.onResume();
    	
		TextView t = (TextView)findViewById(R.id.timestamp);
		t.setText("Waiting ...");
    	
    	runUpdater();
    }
    
    public void onDestroy(){
		Log.i(TAG, "onDestroy() called");
		super.onDestroy();
		if(timer != null){
			timer.cancel();
		}
    }
    
	private ServiceConnection svcConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName className, IBinder binder) {
			logService = ((ManetLoggerService.ManetLogBinder) binder).getService();
		}
		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			logService = null;
		}
	};
        
	public void runUpdater(){
		timer = new Timer();
		Log.i(TAG, "Begin Updating");
		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				if (logService != null) {
					fields = logService.getLatestLogInfo();
					handler.post(new UpdateRunnable());
				}
			}
		}, 0, UPDATE_WAIT_TIME_MILLISEC);
	}
        
    void doBindService(){
        bindService(new Intent(this, ManetLoggerService.class), svcConnection , Context.BIND_AUTO_CREATE);
    }
    
    private class UpdateRunnable implements Runnable {
		 @Override
		 public void run() {
			 TextView t = (TextView)findViewById(R.id.timestamp);
			 t.setText(fields[_TIMESTAMP]);
			 t = (TextView)findViewById(R.id.latitude);
			 t.setText(fields[_LATITUDE]);
			 t = (TextView)findViewById(R.id.longitude);
			 t.setText(fields[_LONGITUDE]);
			 t = (TextView)findViewById(R.id.battery);
			 t.setText(fields[_BATTERY]);
			 t = (TextView)findViewById(R.id.voltage);
			 t.setText(fields[_VOLTAGE]);
			 t = (TextView)findViewById(R.id.temperature);
			 t.setText(fields[_TEMPERATURE]);
			 t = (TextView)findViewById(R.id.minfo);
			 t.setText(fields[_MINFO]);
		 }
   };    
}