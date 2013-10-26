package com.multiplemonomials.printerdroid.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class BarView extends View {
	
	Paint paint;
	
	//tells us the layer that we should be drawing
	public int layerToDraw = 0;
	
	int height;
	
	int width;

	public BarView(Context context, AttributeSet attrs) {
		super(context, attrs);
		paint = new Paint();
		paint.setColor(Color.RED);
		this.setBackgroundColor(Color.WHITE);
	}
	
	public BarView(Context context) {
		super(context);
		
		paint = new Paint();
		paint.setColor(Color.RED);
		this.setBackgroundColor(Color.WHITE);
	}
	
	@Override
	public void onWindowFocusChanged(boolean hasFocus)
	{
		super.onWindowFocusChanged(hasFocus);
	}
	
	public void setColor(int color)
	{
		paint.setColor(color);
	}
	
	@Override
	public void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);
		
		
		
	}
	
	

}
