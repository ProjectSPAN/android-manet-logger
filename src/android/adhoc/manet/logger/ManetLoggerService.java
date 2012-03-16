package android.adhoc.manet.logger;

import java.util.Timer;
import java.util.TimerTask;

import android.adhoc.manet.logger.*;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class ManetLoggerService extends Service {

	private int LOG_INTERVAL = 30000; //Default 30s
	private static final String TAG = "ManetLoggerService";
	private ManetLoggerHelper helper = null;
	private NotificationManager notifier = null;	
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
		Toast.makeText(this,"Service created ...", Toast.LENGTH_LONG).show();
		
		helper = new ManetLoggerHelper(this);
		helper.setup();
		beginLogging();
		
	}

	@Override
	public void onDestroy(){
		Log.i(TAG, "onDestroy() called");
		super.onDestroy();
		Toast.makeText(this,"Service created ...", Toast.LENGTH_LONG).show();
		
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


	
	

}
