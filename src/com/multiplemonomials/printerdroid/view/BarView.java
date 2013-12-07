package com.multiplemonomials.printerdroid.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class BarView extends View {
	
	Paint paint;
	
	int maxDegrees;
	
	float currentWidthInPixels;

	public BarView(Context context, AttributeSet attrs) 
	{
		super(context, attrs);
		paint = new Paint();
		paint.setColor(Color.RED);
		
		this.setBackgroundColor(Color.WHITE);
		
	}
	
	public BarView(Context context) 
	{
		super(context);
		
		paint = new Paint();
		paint.setColor(Color.RED);
		this.setBackgroundColor(Color.WHITE);
		
	}
	public void setColor(int color)
	{
		paint.setColor(color);
	}
	
	@Override
	public void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);
		
		canvas.drawRect(0, 0, currentWidthInPixels, getHeight(), paint);
	}
	
	public void setMaxDegrees(int max)
	{
		maxDegrees = max;
	}
	
	public void setCurrentDegrees(int current)
	{
		currentWidthInPixels = current * (getWidth() / maxDegrees);
		invalidate();
	}
	
	
	
	

}
