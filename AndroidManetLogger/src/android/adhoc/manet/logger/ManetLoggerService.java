/* 
SPAN - Smart Phone AdHoc Networking project
©2012 The MITRE Corporation
*/
package android.adhoc.manet.logger;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.BatteryManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;

public class ManetLoggerService extends Service{
	
	private static final String TAG = "ManetLoggerService";
	
	private ManetLoggerHelper helper = null;
	private LocationManager locMan = null; 
	private Notification notification = null;
	private PendingIntent pendingIntent = null;
	private Timer timer = null;
	private BroadcastReceiver batteryReceiver = null;
	private boolean isLogging = false;

	private final IBinder mBinder = new ManetLogBinder();
	
	// preference management
	private static SharedPreferences sharedPrefs = null;
	
	// notification management
	private static NotificationManager notificationManager = null;
	
	// power management
	private static PowerManager powerManager = null;
	private static PowerManager.WakeLock wakeLock = null;
	
	// unique id for the notification
	private static final int NOTIFICATION_ID = 0;
	
	private static final String ACTIVE_PREF_KEY = "active";
	
	public static final int GPS_REQUEST_INTERVAL_MILLISEC = 33000;
	public static final int LOG_INTERVAL_MILLISEC = 10000;

	
	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(TAG, "onCreate()"); // DEBUG
				
		//Setup ManetLoggerHelper
		helper = new ManetLoggerHelper(this);
		helper.setup();
		
		// notification management
		notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		notificationManager.cancelAll(); // in case service was force-killed
				
        // power management
		PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
    	wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "LOGGER_WAKE_LOCK");
        wakeLock.setReferenceCounted(false);
        wakeLock.acquire();
		
		//Setup Battery Receiver
		batteryReceiver = new BroadcastReceiver() {
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
		locMan.requestLocationUpdates(LocationManager.GPS_PROVIDER, GPS_REQUEST_INTERVAL_MILLISEC, 1, 
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
		
		// TODO: After "No longer want ..." eventually kills the service,
		// this method will be called. Gracefully resume operations.
		
		// preference management
		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		if (sharedPrefs.contains(ACTIVE_PREF_KEY)) {
			isLogging = sharedPrefs.getBoolean(ACTIVE_PREF_KEY, false);
			if (isLogging) {
				beginLogging();
			} else {
				endLogging();
			}
		}
	}

	@Override
	public void onDestroy(){
		super.onDestroy();
		Log.d(TAG, "onDestroy()"); // DEBUG
		
		if(timer != null){
			timer.cancel();
		}
		
		wakeLock.release();
		
		unregisterReceiver(batteryReceiver);
		
		helper.teardown();
	}
	
	@Override
	public void onStart(Intent intent, int startId) {
		Log.d(TAG, "onStart()"); // DEBUG
		super.onStart(intent, startId);
	}
	
	// called by the system every time a client explicitly starts the service by calling startService(Intent)
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d(TAG, "onStartCommand()"); // DEBUG
		return START_STICKY; // run service until explicitly stopped   
	}
	@Override
	public IBinder onBind(Intent arg0) {
		return mBinder;
	}
	
	public boolean isLogging() {
		return isLogging;
	}

	public void beginLogging() {
		Log.i(TAG, "Begin Logging");
		
		isLogging = true;
		sharedPrefs.edit().putBoolean(ACTIVE_PREF_KEY, true).commit();
		showNotification();

		helper.createLog();
		timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				helper.createLogEntry();
			}
		}, 0, LOG_INTERVAL_MILLISEC);
	}
	
	public void endLogging(){
		Log.i(TAG, "End Logging");
				
		if (timer != null) {
			timer.cancel();
			timer.purge();
			timer = null;
		}
		
		isLogging = false;
		sharedPrefs.edit().putBoolean(ACTIVE_PREF_KEY, false).commit();
		showNotification();
	}
	
	public void clearLog(){
		Log.i(TAG, "Clear log");
		helper.deleteLog();
		if (isLogging()) {
			helper.createLog();
		}
	}
	
    public void requestLocationEnable(){
    	Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
    	startActivity(intent);
    }
	
	public String[] getLatestLogInfo(){
		return helper.getLatestLogInfo(); 
	}
	
	private void showNotification() {
		
		int icon;
		String content = null;
		if (isLogging()) {
			icon = R.drawable.on_notification;
			content = "Logger is running.";
		} else {
			icon = R.drawable.off_notification;
			content = "Logger is not running.";
		}
		
		Log.d(TAG, "showNotification()"); // DEBUG
				
    	// set the icon, ticker text, and timestamp        
    	notification = new Notification(icon, content, System.currentTimeMillis());
    	
    	// try to prevent service from being killed with "no longer want";
    	// this only prolongs the inevitable
    	startForeground(NOTIFICATION_ID, notification);

    	// pending intent to launch main activity if the user selects notification        
    	// pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, DummyActivity.class), 0);
    	
    	Intent launchIntent = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);
    	launchIntent.setComponent(new ComponentName("android.adhoc.manet.logger", "android.adhoc.manet.logger.ManetLoggerActivity"));
    	pendingIntent = PendingIntent.getActivity(this, 0, launchIntent, 0);
    	
    	// don't allow user to clear notification
    	notification.flags = notification.flags | Notification.FLAG_ONGOING_EVENT;


    	// set the info for the views that show in the notification panel    
    	notification.setLatestEventInfo(this, "MANET Logger", content, pendingIntent);
    	
    	// send the notification
    	notificationManager.notify(NOTIFICATION_ID, notification);
    }
	
	public class ManetLogBinder extends Binder {
		ManetLoggerService getService() {
			return ManetLoggerService.this;
		}
	}
}
