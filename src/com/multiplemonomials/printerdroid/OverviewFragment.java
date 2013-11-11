package com.multiplemonomials.printerdroid;

import com.multiplemonomials.printerdroid.R;
import com.multiplemonomials.printerdroid.view.BarView;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

public class OverviewFragment extends Fragment 
{
	
	BarView heaterTempView;
	BarView bedTempView;
	
	public ProgressBar layerProgressView;
	
	public TextView layerTextView;
	public TextView heaterTempTextView;
	public TextView bedTempTextView;

	/** Called when the activity is first created. */
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.overview, container, false);
		
		heaterTempView = (BarView) view.findViewById(R.id.barView1);
		heaterTempView.setColor(Color.RED);
		heaterTempView.setMaxDegrees(Settings.heater_temp);
		
		bedTempView = (BarView) view.findViewById(R.id.barView2);
		//set color to ORANGE!
		bedTempView.setColor(Color.parseColor("#FFA500"));
		bedTempView.setMaxDegrees(Settings.heater_temp);
		
		layerProgressView = (ProgressBar) view.findViewById(R.id.progressBar1);
		
		layerTextView = (TextView) view.findViewById(R.id.currentLayerTextView);
		heaterTempTextView = (TextView) view.findViewById(R.id.heaterTempTextView);
		bedTempTextView = (TextView) view.findViewById(R.id.bedTempTextView);
		
		return view;
	}
	
	public void setHeaterTemperatureView(int temp)
	{
		if(heaterTempView != null)
		{
			heaterTempView.setCurrentDegrees(temp);
			
			String heaterTempReadout = getResources().getString(R.string.heatertempindicator) + temp + getResources().getString(R.string.degrees_centegrade);
			heaterTempTextView.setText(heaterTempReadout);
		}
	}
	
	public void setBedTemperatureView(int temp)
	{
		if(bedTempView != null)
		{
			bedTempView.setCurrentDegrees(temp);
			
			String bedTempReadout = getResources().getString(R.string.bedtempindicator) + temp + getResources().getString(R.string.degrees_centegrade);
			bedTempTextView.setText(bedTempReadout);
		}
	}
	

}
