package com.multiplemonomials.printerdroid.gcodeparser;

import java.util.LinkedList;

public class Layer {
	
	public LinkedList<Gcode> codes;
	
	/**
	 * true if this is the last layer
	 */
	public boolean isLast;
	
	public Layer()
	{
		codes = new LinkedList<Gcode>();
		isLast = false;
	}

}
