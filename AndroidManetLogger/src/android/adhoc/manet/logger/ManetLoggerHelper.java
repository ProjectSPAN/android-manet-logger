/* 
SPAN - Smart Phone AdHoc Networking project
©2012 The MITRE Corporation
*/
package android.adhoc.manet.logger;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;

import android.adhoc.manet.ManetHelper;
import android.adhoc.manet.ManetObserver;
import android.adhoc.manet.routing.Node;
import android.adhoc.manet.service.ManetService.AdhocStateEnum;
import android.adhoc.manet.system.ManetConfig;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

public class ManetLoggerHelper implements ManetObserver {
	
	//Constants and parameters
	private static final String TAG = "ManetLoggerHelper";
	
	private ManetLoggerService logService = null;
	
	private LocationManager locMan = null;
	
	private boolean locationEnabled = false;
	
	private Location location = null;
	private int batt_scale = 0;
    private int batt_level = 0;
    private int batt_voltage = 0;
    private int batt_temp = 0;
    private String timestamp = "waiting ...";
    private String minfo = "none";
    private String rinfo = null;
    private AdhocStateEnum state = AdhocStateEnum.UNKNOWN;
     
    private static final String LOG_FILE = Environment.getExternalStorageDirectory() + "/manet.log";
    
    private File file = null;
	private FileWriter fWriter = null;
	private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
	
	// MANET helper
	public ManetHelper manet = null;

	public ManetLoggerHelper(ManetLoggerService MLService){
		this.logService = MLService;
		
        // init MANET helper
		manet = new ManetHelper(logService);
		manet.registerObserver(this);
	}
		
	public void setup(){
		// connect to MANET service
        if (!manet.isConnectedToService()) {
			manet.connectToService();
        }
	}
	
	public void teardown() {
		// disconnect from MANET service
        if (manet.isConnectedToService()) {
			manet.disconnectFromService();
        }
	}
	
	public synchronized void createLog() {
		try {
			//Setup the File IO
			file = new File(LOG_FILE);
			if (!file.exists()) {
				file.createNewFile();
			} // otherwise append
			fWriter = new FileWriter(file, true);
		} catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public synchronized void deleteLog(){
		try {
			if (file != null) {
				fWriter.close();
			} else {
				file = new File(LOG_FILE);
			}
			file.delete();
			file = null;
		} catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private synchronized void writeLog(String s){
		try {
			if (file != null) {
				fWriter.write(s);
				fWriter.flush();
				Log.d(TAG, "Entry Logged");
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	// generate info on-demand
	private void generateLogInfo() {
		// timestamp
		timestamp = sdf.format(new Date());
		// manet
		try {
			GetManetInfoThread thread = new GetManetInfoThread();
			thread.start();
			thread.join(ManetLoggerService.LOG_INTERVAL_MILLISEC/2);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public String createLogEntry(){
		generateLogInfo();
		
		String timestampInfo = getTimestampInfo();
		String batteryInfo = getBatteryInfo();
		String gpsInfo = getGPSInfo();
		// String accelerometerInfo = getAccelerometerInfo();
		String manetInfo = getManetInfo();
		
		String logEntry = 
			timestampInfo +
			", " + batteryInfo +
			", " + gpsInfo +
			", " + manetInfo + 
			"\n";
		
		writeLog(logEntry);
		return logEntry;
	}
	
	public String getGPSInfo(){
		if(location != null){
			double lat = (double) location.getLatitude();
			double lon = (double) location.getLongitude();
			return "Latitude: " + lat + ", Longitude: " + lon;
		}else{
			return "Latitude: 0, Longitude: 0";
		}
	}
	
	public void setLocation(Location l){
		this.location = l;
	}
	
	public String getTimestampInfo(){
		return "Timestamp: " + timestamp;
	}
	
	public String getBatteryInfo(){
		return "BatteryLevel: " + batt_level + "/" + batt_scale + ", BatteryVoltage: " + batt_voltage + " mV, BatteryTemp: " + batt_temp + " °C";
	}
	
	public void setBatteryInfo(int s, int l, int v, int t){
		batt_scale = s;
		batt_level = l;
		batt_voltage = v;
		batt_temp = t;
	}
	
	public String getManetInfo(){
		if (minfo == null) {
			return "none";
		} else {
			String manetInfo = minfo.trim();
			manetInfo = manetInfo.replace("\n\n", ", ");
			manetInfo = manetInfo.replace("\n", ";     ");
			return manetInfo;
		}
	}
	
	private class GetManetInfoThread extends Thread {
		@Override
		public void run() {
			rinfo = null;
			if (manet.isConnectedToService()) {
				manet.sendRoutingInfoQuery();
				while (state.equals(AdhocStateEnum.STARTED) && rinfo == null) {
					Thread.yield();
				}
				minfo = rinfo;
			}
		}
	}
	
	public String getAccelerometerInfo(){
		return "sample accelerometer info";
	}
	
	// logger UI
	public String[] getLatestLogInfo() {
		
		String[] fields = new String[7];
		fields[0] = this.timestamp;
		
		if (this.location == null) {
			fields[1] = "0 ";
			fields[2] = "0 ";	
		} else {
			fields[1] = this.location.getLatitude() + "";
			fields[2] = this.location.getLongitude() + "";
		}
		
		fields[3] = this.batt_level + "/" + this.batt_scale;
		fields[4] = this.batt_voltage + " mV";
		fields[5] = this.batt_temp + " °C";
		
		if (this.minfo == null) {
			fields[6] = "none";
		} else {
			String[] tokens = this.minfo.split("\n");
			fields[6] = tokens[0];
			if (tokens.length > 1) {
				fields[6] += " ...";
			}
		}
		
		return fields;
	}
	
	@Override
	public void onServiceConnected() {
		// TODO Log service connection
		manet.sendAdhocStatusQuery(); // get initial state
	}
	
	@Override
	public void onAdhocStateUpdated(AdhocStateEnum state, String info) {
		// TODO Log adhoc state
		// System.out.println("onAdhocStateUpdated"); // DEBUG
		this.state = state;
	}
	
	@Override
	public void onConfigUpdated(ManetConfig manetcfg) {
		// TODO Log config update
	}
	
	@Override
	public void onPeersUpdated(HashSet<Node> peers) {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void onRoutingInfoUpdated(String info) {
		// TODO Auto-generated method stub
		rinfo = info;
	}
	
	@Override
	public void onError(String error) {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void onServiceDisconnected() {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void onServiceStarted() {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void onServiceStopped() {
		// TODO Auto-generated method stub
	}
}