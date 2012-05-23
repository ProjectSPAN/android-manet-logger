/* 
SPAN - Smart Phone AdHoc Networking project
©2012 The MITRE Corporation
*/
package android.adhoc.manet.logger;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TreeSet;

import android.adhoc.manet.ManetObserver;
import android.adhoc.manet.service.ManetService.AdhocStateEnum;
import android.adhoc.manet.system.ManetConfig;
import android.util.Log;

public class ManetLoggerHelper implements ManetObserver{

	private static final String TAG = "ManetLoggerHelper";
	//Constants and parameters
	ManetLoggerService service = null;
	FileWriter fWriter = null;
	SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");


public ManetLoggerHelper(ManetLoggerService MLService){
	this.service = MLService;
	
}
	
public void setup(){
	//Setup the File IO
	File file = new File("data/data/android.adhoc.manet.logger/manetLog.txt");
	boolean exists = file.exists();
	try{
		fWriter = new FileWriter("data/data/android.adhoc.manet.logger/manetLog.txt", true);
		if(!exists){
			Log.i(TAG, "Log file did not exist. Creating and initializing");
			fWriter.write("Timestamp\tBatteryInfo\tGPSInfo\taccelerometerInfo\tManetInfo\n");
			fWriter.flush();
		}else{
			Log.i(TAG, "Log File found. Continuing Log.");
		}
	}catch(Exception e){
		e.printStackTrace();
	}
	
	
}

private void writeLog(String s){
	try{
		fWriter.write(s);
		fWriter.flush();
		Log.d(TAG, "Entry Logged");
	}catch(Exception e){
		e.printStackTrace();
	}
}

public String createLogEntry(){
	String timestamp = sdf.format(new Date());
	String batteryInfo = "sample battery info";
	String gpsInfo = "sample gps info";
	String accelerometerInfo = "sample accelerometer info";
	String manetInfo = "sample manet info";
	
	String logEntry = timestamp + ":\t" + batteryInfo + "\t" + gpsInfo + "\t" + accelerometerInfo + "\t" + manetInfo + "\n";
	writeLog(logEntry);
	return logEntry;
}

@Override
public void onServiceConnected() {
	// TODO Log service connection
	
}

@Override
public void onServiceDisconnected() {
	// Log service disconnect
	
}

@Override
public void onServiceStarted() {
	// Log service start
	
}

@Override
public void onServiceStopped() {
	// Log service stop
	
}

@Override
public void onAdhocStateUpdated(AdhocStateEnum state, String info) {
	// Log adhoc state
	
}

@Override
public void onConfigUpdated(ManetConfig manetcfg) {
	// Log config update
	
}

@Override
public void onPeersUpdated(TreeSet<String> peers) {
	// TODO Auto-generated method stub
	
}

@Override
public void onRoutingInfoUpdated(String info) {
	// TODO Auto-generated method stub
	
}

@Override
public void onError(String error) {
	// TODO Auto-generated method stub
	
}



}