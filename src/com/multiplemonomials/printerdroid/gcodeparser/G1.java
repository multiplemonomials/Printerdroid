package com.multiplemonomials.printerdroid.gcodeparser;

import android.util.Log;

public class G1 extends Gcode{
	
	private static final String TAG = "G1";
	public Optional<Double> x_value;
	public Optional<Double> y_value;
	public Optional<Double> z_value;
	public Optional<Double> e_value;
	public Optional<Double> f_value;
	
	public G1(String string)
	{
		if(string.contains(";"))
		{
			string = fix_comments(string);
		}
		x_value = get_value_from_char_array(string, 'X');
		y_value = get_value_from_char_array(string, 'Y');
		z_value = get_value_from_char_array(string, 'Z');
		e_value = get_value_from_char_array(string, 'E');
		f_value = get_value_from_char_array(string, 'F');
	}
	
	String fix_comments(String commented_code)
	{
		
		return commented_code.substring(0, commented_code.indexOf(";"));
	}
	
	
	//this section ported from AEONS
	Optional<Double> get_value_from_char_array(String code, char target)
	{

		int position = code.indexOf(Character.toString(target));
		
		if (position == -1)
		{
			return new Optional<Double>(null);
		}
		String half_string = code.substring(position + 1);
		int second_position = half_string.indexOf(" ");
		
		double code_value;
		if(second_position != -1)
		{
			code_value = Double.parseDouble(half_string.substring(0, second_position));
		}
		else
		{
			code_value = Double.parseDouble(half_string);
		}

		return new Optional<Double>(code_value);
	}
	
	@Override
	public String toString()
	{
		//rebuild the string that was originally used to make the object
		String code = "G1";
		code = code + x_value._variableValue != null ? x_value._variableValue.toString() : "";
		code = code + y_value._variableValue != null ? y_value._variableValue.toString() : "";
		code = code + z_value._variableValue != null ? z_value._variableValue.toString() : "";
		code = code + e_value._variableValue != null ? e_value._variableValue.toString() : "";
		
		Log.d(TAG, "code line: " + code);
		return code;
	}

}
