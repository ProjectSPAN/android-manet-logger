/* 
SPAN - Smart Phone AdHoc Networking project
©2012 The MITRE Corporation
*/
package android.adhoc.manet.logger;


import java.util.Timer;
import java.util.TimerTask;

import android.adhoc.manet.logger.ManetLoggerService;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;

public class ManetLoggerActivity extends Activity {
	
	private static final String TAG = "ManetLoggerActivity";
	
	private ManetLoggerService logService = null;
	
	private String[] fields = null;
	
	private static int _TIMESTAMP = 0;
	private static int _LATITUDE = 1;
	private static int _LONGITUDE = 2;
	private static int _BATTERY = 3;
	private static int _VOLTAGE = 4;
	private static int _TEMPERATURE = 5;
	private static int _MINFO = 6;
	
	private static int UPDATE_WAIT_TIME_MILLISEC = 10000; // TODO: DEBUG
	
	private static int ID_DIALOG_CONNECTING = 0;
	
	private Timer timer = null;
	
	private Handler handler = new Handler();
	
	private ProgressDialog progressDialog = null;
	private int currDialogId = -1;
	
	private ToggleButton btnOnOff = null;
	private Button btnClearLog = null;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()"); // DEBUG
        
        setContentView(R.layout.main);
        
        btnOnOff = (ToggleButton) findViewById(R.id.btnOnOff);
        btnOnOff.setOnClickListener(new View.OnClickListener() {
	  		public void onClick(View v) {
	  			// sanity check; logger should represent button state prior to being toggled
	  			if (logService.isLogging() == btnOnOff.isChecked()) {
	  				Log.e(TAG, "Error: Toggle button does not represent logger state!"); // DEBUG
	  				System.exit(1); // abnormal status
	  			}
	  			if (!logService.isLogging()) {
	  				logService.beginLogging();
	  			} else {
	  				logService.endLogging();
	  			}
	  		}
		});
        
        btnClearLog = (Button) findViewById(R.id.btnClearLog);
	  	btnClearLog.setOnClickListener(new View.OnClickListener() {
	  		public void onClick(View v) {
	  			logService.clearLog();
	  		}
		});
    }
    
    @Override
    public void onPostCreate(Bundle savedInstanceState) {
    	super.onPostCreate(savedInstanceState);
        Log.d(TAG, "onPostCreate()"); // DEBUG
        
		// connect to logger service
	  	currDialogId = ID_DIALOG_CONNECTING;
        showDialog(ID_DIALOG_CONNECTING);
		
   		// start logger service so that it runs even if no active activities are bound to it
   		startService(new Intent(this, ManetLoggerService.class));
    }
    
    @Override
    public void onPause(){
    	super.onPause();
        Log.d(TAG, "onPause()"); // DEBUG
    	
		if(timer != null){
			timer.cancel();
		}
    }
    
    @Override
    public void onResume(){
    	super.onResume();
        Log.d(TAG, "onResume()"); // DEBUG
    	
		TextView t = (TextView)findViewById(R.id.timestamp);
		t.setText("Waiting ...");
    	
   		bindService(new Intent(this, ManetLoggerService.class), svcConnection , Context.BIND_AUTO_CREATE);
		
    	runUpdater();
    }
    
    @Override
    public void onDestroy(){
		super.onDestroy();
        Log.d(TAG, "onDestroy()"); // DEBUG
		
		if(timer != null){
			timer.cancel();
		}
		
		unbindService(svcConnection);
    }
    
    @Override
    protected Dialog onCreateDialog(int id) {
    	if (id == ID_DIALOG_CONNECTING) {
    		progressDialog = new ProgressDialog(this);
	    	progressDialog.setTitle(getString(R.string.logger_activity_connect));
	    	progressDialog.setMessage(getString(R.string.logger_activity_connect_summary));
	    	progressDialog.setIndeterminate(false);
	    	progressDialog.setCancelable(true);
	        return progressDialog;  		
    	} 
    	return null;
    }
    
    @Override
    protected Dialog onCreateDialog(int id, Bundle args){
    	Log.d(TAG, "onCreateDialog()"); // DEBUG
    	if (id == ID_DIALOG_CONNECTING) {
    		return onCreateDialog(id);
    	}
    	return null;
    }
    
 	private void removeDialog() {
    	Log.d(TAG, "removeDialog()"); // DEBUG
		if (currDialogId != -1) {
			super.removeDialog(currDialogId);
			currDialogId = -1;
		}
 	}
    
	private ServiceConnection svcConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName className, IBinder binder) {
	        Log.d(TAG, "onServiceConnected()"); // DEBUG
			logService = ((ManetLoggerService.ManetLogBinder) binder).getService();
		}
		@Override
		public void onServiceDisconnected(ComponentName arg0) {
	        Log.d(TAG, "onServiceDisconnected()"); // DEBUG
			logService = null;
		}
	};
        
	public void runUpdater(){
		timer = new Timer();
		Log.i(TAG, "Begin Updating");
		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				handler.post(new UpdateRunnable());
			}
		}, 0, UPDATE_WAIT_TIME_MILLISEC);
	}
    
    private class UpdateRunnable implements Runnable {
		 @Override
		 public void run() {
			 if (logService != null) {
				 btnOnOff.setChecked(logService.isLogging());
				 removeDialog();
				 if (logService.isLogging()) {
					 fields = logService.getLatestLogInfo();
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
			 }
		 }
   };    
}