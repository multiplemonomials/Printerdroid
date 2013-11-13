package com.multiplemonomials.printerdroid;

import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.multiplemonomials.printerdroid.gcodeparser.Layer;

public class Settings {
	
	//to add a preference, first add it as a class variable and then put its setter in regenerate
	
	public static int bedHeight = 200;
	
	public static int bedWidth = 200;
	
	public static int baudrate = 9600;
	
	public static int target_heater_temp = 200;
	
	public static int target_bed_temp = 80;
	
//global app data
//----------------------------------------------------------------------------------
	
	public static List<Layer> currentFile;
	
	public static Uri currentFilePath;
	
	public static boolean current_heater_state_on = false;
	
	public static boolean current_bed_state_on = false;

	public static int current_heater_temp = 0;

	public static int current_bed_temp = 0;
	
	public static boolean isPrinting = false;
	
	public static boolean printerIsOn = false;

	public static void regenerate(Context context) 
	{
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		
		try
		{
			bedWidth = Integer.parseInt(sharedPreferences.getString("pref_bed_width", ""));
			bedHeight = Integer.parseInt(sharedPreferences.getString("pref_bed_height", ""));
			baudrate = Integer.parseInt(sharedPreferences.getString("pref_baudrate", ""));
			target_heater_temp = Integer.parseInt(sharedPreferences.getString("pref_heater_default_temp", ""));
			target_bed_temp = Integer.parseInt(sharedPreferences.getString("pref_bed_default_temp", ""));
		}
		catch(Exception e)
		{
			e.printStackTrace();
			Toast.makeText(context, "Failed to read settings", Toast.LENGTH_LONG).show();
		}
		
	}

}
