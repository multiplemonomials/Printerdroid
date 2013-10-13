package com.multiplemonomials.printerdroid.view;

import java.util.List;

import com.multiplemonomials.printerdroid.Settings;
import com.multiplemonomials.printerdroid.gcodeparser.G1;
import com.multiplemonomials.printerdroid.gcodeparser.Gcode;
import com.multiplemonomials.printerdroid.gcodeparser.Layer;
import com.multiplemonomials.printerdroid.gcodeparser.TextCode;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class GcodeView extends View {
	
	Paint paint;
	
	//tells us the layer that we should be drawing
	public int layerToDraw = 0;
	
	//height pixels per mm
	float height_scaler;
	
	//width pixels per mm
	float width_scaler;

	public GcodeView(Context context, AttributeSet attrs) {
		super(context, attrs);
		paint = new Paint();
		paint.setColor(Color.RED);
		this.setBackgroundColor(Color.WHITE);
	}
	
	public GcodeView(Context context) {
		super(context);
		
		paint = new Paint();
		paint.setColor(Color.RED);
		this.setBackgroundColor(Color.WHITE);
	}
	
	@Override
	public void onWindowFocusChanged(boolean hasFocus)
	{
		super.onWindowFocusChanged(hasFocus);
		int height = this.getLayoutParams().height;
		int width = this.getLayoutParams().width;
		
		height_scaler = (float)height / Settings.bedHeight;
		width_scaler = (float)width / Settings.bedWidth;
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);
		
		Layer layer = Settings.currentFile.get(layerToDraw);
		assert(layer != null);
		
		float last_x = -1;
		float last_y = -1;
		
		for(Gcode gcode : layer.codes)
		{
			//are we working with a move command?
			if(!(gcode instanceof TextCode))
			{
				G1 g1 = (G1) gcode;
				if(g1.e_value == null);
				{
					//draw the last line
					if((last_x != -1) && (last_y != -1))
					{
						canvas.drawLine(last_x * height_scaler, last_y * width_scaler, g1.x_value._variableValue.floatValue() * height_scaler, g1.y_value._variableValue.floatValue() * width_scaler, paint);
						last_x = g1.x_value._variableValue.floatValue();
						last_x = g1.y_value._variableValue.floatValue();
					}
				}
			}
		}
		
	}
	
	

}
