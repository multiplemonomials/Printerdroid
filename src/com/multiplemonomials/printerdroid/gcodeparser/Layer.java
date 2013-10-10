package com.multiplemonomials.printerdroid.gcodeparser;

import java.util.LinkedList;
import java.util.List;

public class Layer {
	
	LinkedList<Gcode> codes;
	
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
