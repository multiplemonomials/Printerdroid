package com.multiplemonomials.printerdroid;

import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.multiplemonomials.printerdroid.gcodeparser.Layer;

public class Settings {
	
	public static int bedHeight = 200;
	
	public static int bedWidth = 200;
	
	public static List<Layer> currentFile;

	public static void regenerate(Context context) 
	{
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		
		try
		{
			bedWidth = Integer.parseInt(sharedPreferences.getString("pref_bed_width", ""));
			bedHeight = Integer.parseInt(sharedPreferences.getString("pref_bed_height", ""));
		}
		catch(Exception e)
		{
			e.printStackTrace();
			Toast.makeText(context, "Failed to read settings", Toast.LENGTH_LONG).show();
		}
		
	}

}
