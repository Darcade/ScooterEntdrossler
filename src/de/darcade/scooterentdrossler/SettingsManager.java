package de.darcade.scooterentdrossler;

import android.app.Activity;
import android.content.SharedPreferences;

public class SettingsManager {
	public static final String PREFS_NAME = "settings";
	public static final String DEVICE_ADRESS_KEY = "device_address";
	public static final String DEVICE_NAME_KEY = "device_name";
	public static final String AUTOCONNECT = "autoconnect";
	public static final String BLUE_AUTO_TOGGLE = "blue_auto_toggle";
	
	private SharedPreferences settings;
	
	public SettingsManager(Activity activity){
		settings = activity.getSharedPreferences(PREFS_NAME, 0);
	}

	public String getDeviceAddress(){
		return settings.getString(DEVICE_ADRESS_KEY, "");
	}
	
	public boolean setDevice(String device_address, String device_name){
		System.out.println("setting device");
		return settings.edit().putString(DEVICE_ADRESS_KEY, device_address).putString(DEVICE_NAME_KEY, device_name).commit();
	}
	
	public String getDeviceName(){
		return settings.getString(DEVICE_NAME_KEY, "");
	}
	
	public boolean getAutoconnect(){
		return settings.getBoolean(AUTOCONNECT, false);
	}
	
	public boolean setAutoconnect(boolean autoconnect){
		return settings.edit().putBoolean(AUTOCONNECT, autoconnect).commit();
	}
	
	public boolean getBlueAutotoggle(){
		return settings.getBoolean(BLUE_AUTO_TOGGLE, false);
	}
	
	public boolean setBlueAutotoggle(boolean blueautotoggle){
		return settings.edit().putBoolean(BLUE_AUTO_TOGGLE, blueautotoggle).commit();
	}
}
