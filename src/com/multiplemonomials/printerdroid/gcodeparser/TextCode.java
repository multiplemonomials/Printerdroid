package com.multiplemonomials.printerdroid.gcodeparser;

/**
 * This class holds a gcode that isn't a move command
 * 
 * @author Jamie Smith
 *
 */
public class TextCode extends Gcode{
	
	String _text;
	
	public TextCode(String text)
	{
		_text = text;
	}
	
	@Override
	public String toString()
	{
		return _text;
	}

}
