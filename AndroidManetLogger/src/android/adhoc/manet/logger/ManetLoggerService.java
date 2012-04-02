package android.adhoc.manet.logger;

import java.util.Timer;
import java.util.TimerTask;

import android.adhoc.manet.logger.*;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.BatteryManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

public class ManetLoggerService extends Service{

	private int LOG_INTERVAL = 30000; //Default 30s
	private static final String TAG = "ManetLoggerService";
	private ManetLoggerHelper helper = null;
	private NotificationManager notifier = null;
	private LocationManager locMan = null; 
	private Notification notification = null;
	private PendingIntent pendingIntent = null;
	private Timer timer = null;

	private final IBinder mBinder = new ManetLogBinder();
	// unique id for the notification
	private static final int NOTIFICATION_ID = 0;

	@Override
	public void onCreate() {
		Log.i(TAG, "onCreate() called");
		super.onCreate();
		
		//Setup ManetLoggerHelper
		helper = new ManetLoggerHelper(this);
		helper.setup();
		
		//Setup Battery Receiver
		BroadcastReceiver batteryReceiver = new BroadcastReceiver() {
	        @Override
	        public void onReceive(Context context, Intent intent) {
	           Log.i(TAG, "BatteryReciever recieved update");
	           int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
	           int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
	           int voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1);
	           int temp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);
	           helper.setBatteryInfo(scale, level, voltage, temp);
	        }
	    };
	    IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
	    registerReceiver(batteryReceiver, filter);
	    
	    //Setup Location Listener
		locMan = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		boolean locationEnabled = locMan.isProviderEnabled(LocationManager.GPS_PROVIDER);
		if (!locationEnabled) {
			  //this.service.requestLocationEnable();
			}
		helper.setLocation(locMan.getLastKnownLocation(LocationManager.GPS_PROVIDER));
		locMan.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOG_INTERVAL, 1, 
			new LocationListener(){
				@Override
				public void onLocationChanged(Location arg0) {
					Log.i(TAG, "Recieved Location Update");
					helper.setLocation(arg0);
				}
				@Override
				public void onProviderDisabled(String arg0) {
					// TODO Auto-generated method stub		
				}
				@Override
				public void onProviderEnabled(String arg0) {
					// TODO Auto-generated method stub	
				}
				@Override
				public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
					// TODO Auto-generated method stub	
				}
			}
		);


		beginLogging();
		
	}

	@Override
	public void onDestroy(){
		Log.i(TAG, "onDestroy() called");
		super.onDestroy();
		if(timer != null){
			timer.cancel();
		}
		
	}
	
	private void beginLogging()
	{
		timer = new Timer();
		Log.i(TAG, "Begin Logging");
		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				helper.createLogEntry();
			}
		}, 0, LOG_INTERVAL);
	}
	
	private void endLogging(){
		Log.i(TAG, "End Logging");
		timer.cancel();
		timer.purge();
	}
	
    public void requestLocationEnable(){
    	Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
    	startActivity(intent);
    }
    
	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
	}
	
	// called by the system every time a client explicitly starts the service by calling startService(Intent)
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_STICKY; // run service until explicitly stopped   
	}
	

	
	@Override
	public IBinder onBind(Intent arg0) {
		return mBinder;
	}

	
	public class ManetLogBinder extends Binder {
		ManetLoggerService getService() {
			return ManetLoggerService.this;
		}
	}

	public int getLogInterval(){
		return this.LOG_INTERVAL;
	}
	
	public void setLogInterval(int i){
		this.LOG_INTERVAL = i;
	}

	public String[] getLatestLogInfo(){
		return helper.getLatestLogInfo(); 
	}

	
	

}
