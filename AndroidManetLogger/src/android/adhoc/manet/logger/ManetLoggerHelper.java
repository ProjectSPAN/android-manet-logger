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
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

public class ManetLoggerHelper implements ManetObserver{
	//Constants and parameters
	private static final String TAG = "ManetLoggerHelper";
	private ManetLoggerService logService = null;
	private LocationManager locMan = null;
	private boolean locationEnabled = false;
	private Location location = null;
	private int batt_scale = -1;
    private int batt_level = -1;
    private int batt_voltage = -1;
    private int batt_temp = -1;
    private String timestamp = "00/00/0000 00:00:00";
    private String minfo = "SampleManetInfo honk honk blargh";
     


	FileWriter fWriter = null;
	SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");


public ManetLoggerHelper(ManetLoggerService MLService){
	this.logService = MLService;
	
}
	
public void setup(){

	
	//Setup the File IO
	File file = new File("data/data/android.adhoc.manet.logger/manetLog.txt");
	boolean exists = file.exists();
	try{
		fWriter = new FileWriter("data/data/android.adhoc.manet.logger/manetLog.txt", true);
		if(!exists){
			Log.i(TAG, "Log file did not exist. Creating and initializing");
			//fWriter.write("Timestamp\tBatteryInfo\tGPSInfo\taccelerometerInfo\tManetInfo\n");
			//fWriter.flush();
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
	timestamp = sdf.format(new Date());
	String batteryInfo = getBatteryInfo();
	String gpsInfo = getGPSInfo();
	String accelerometerInfo = getAccelerometerInfo();
	String manetInfo = getManetInfo();
	
	String logEntry = timestamp + ":\tBATTERY:" + batteryInfo + "\tLOCATION:" + gpsInfo + "\tACCEL:" + accelerometerInfo + "\tMANET:" + manetInfo + "\n";
	writeLog(logEntry);
	return logEntry;
}

public String getGPSInfo(){
	if(location!= null){
	double lat = (double) location.getLatitude();
	double lon = (double) location.getLongitude();
	return lat + ", " + lon;
	}else{
		return "null,null";
	}
}

public void setLocation(Location l){
	this.location = l;
}
public int getBatt_scale() {
	return batt_scale;
}

public void setBatt_scale(int batt_scale) {
	this.batt_scale = batt_scale;
}

public int getBatt_level() {
	return batt_level;
}

public void setBatt_level(int batt_level) {
	this.batt_level = batt_level;
}

public int getBatt_voltage() {
	return batt_voltage;
}

public void setBatt_voltage(int batt_voltage) {
	this.batt_voltage = batt_voltage;
}

public int getBatt_temp() {
	return batt_temp;
}

public void setBatt_temp(int batt_temp) {
	this.batt_temp = batt_temp;
}

public String getTimestamp() {
	return timestamp;
}

public void setTimestamp(String timestamp) {
	this.timestamp = timestamp;
}

public Location getLocation() {
	return location;
}

public String getBatteryInfo(){
	return "BATTERY:" + batt_level+"/"+batt_scale+", "+batt_voltage+"mV, "+batt_temp+"degreesC";
}

public void setBatteryInfo(int s, int l, int v, int t){
	batt_scale = s;
	batt_level = l;
	batt_voltage = v;
	batt_temp = t;
}

public String getManetInfo(){
	return "sample manet info";
}



public String getAccelerometerInfo(){
	return "sample accelerometer info";
}

public String[] getLatestLogInfo(){
	String[] fields = new String[7];
	fields[0] = this.timestamp;
	fields[1] = this.location.getLatitude() + "";
	fields[2] = this.location.getLongitude() + "";
	fields[3] = this.batt_level + "/" + this.batt_scale;
	fields[4] = this.batt_voltage+ " mV";
	fields[5] = this.batt_temp + " °C";
	fields[6] = this.minfo;
	
	return fields;
}

@Override
public void onServiceConnected() {
	// TODO Log service connection
	
}



@Override
public void onAdhocStateUpdated(AdhocStateEnum state, String info) {
	// TODO Log adhoc state
	
}

@Override
public void onConfigUpdated(ManetConfig manetcfg) {
	// TODO Log config update
	
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