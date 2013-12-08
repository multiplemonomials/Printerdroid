package com.multiplemonomials.printerdroid.gcodeparser;

/**\
 * This class holds the value of a variable that can be null under normal circumstances.
 * @author Jamie
 *
 * @param <T>
 */
public class Optional<T> {
	
	public T _variableValue;
	
	public Optional(T variableValue)
	{
		_variableValue = variableValue;
	}
	

}
