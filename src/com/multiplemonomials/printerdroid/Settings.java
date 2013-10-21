package com.multiplemonomials.printerdroid;

import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;

import com.multiplemonomials.printerdroid.gcodeparser.Layer;

public class Settings {
	
	public static int bedHeight = 200;
	
	public static int bedWidth = 200;
	
	public static List<Layer> currentFile;

	public static void regenerate(Context context) 
	{
		SharedPreferences sharedPreferences = context.getSharedPreferences("preferences.xml", Context.MODE_PRIVATE);
		
		bedWidth = Integer.parseInt(sharedPreferences.getString("pref_bed_width", null));
		bedHeight = Integer.parseInt(sharedPreferences.getString("pref_bed_height", null));
		
	}

}
