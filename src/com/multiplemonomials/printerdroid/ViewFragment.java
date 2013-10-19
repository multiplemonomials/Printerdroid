package com.multiplemonomials.printerdroid;

import java.util.ArrayList;
import java.util.List;

import com.multiplemonomials.printerdroid.R;
import com.multiplemonomials.printerdroid.gcodeparser.G1;
import com.multiplemonomials.printerdroid.gcodeparser.Gcode;
import com.multiplemonomials.printerdroid.gcodeparser.Layer;
import com.multiplemonomials.printerdroid.gcodeparser.TextCode;
import com.piedpiper.barchart.*;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ViewFragment extends Fragment {
	
	public int layerToDraw = 0;

	  @Override
	    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			  if(Settings.currentFile != null)
			  {
				  List<Layer> currentFile = Settings.currentFile;
					Layer layer = currentFile.get(layerToDraw);
					//Layer layer = new Layer();
					//layer.codes.add(new G1("G1 X0 Y0"));
					//layer.codes.add(new G1("G1 X10 Y10"));
					assert(layer != null);
					
					int last_x = 0;
					int last_y = 0;
					
					//chart library is autoscaling
					int x_mm_per_point = 1;
					int y_mm_per_point = 1;
//					
					PathAttributes pathAttributes1 = new PathAttributes();
					pathAttributes1.setPointColor("#00AAAAAA");
					pathAttributes1.setPathColor("#FFAF00");
					ArrayList<PathOnChart> paths = new ArrayList<PathOnChart>();
					
					for(Gcode gcode : layer.codes)
					{
						//are we working with a move command?
						if(!(gcode instanceof TextCode))
						{
							G1 g1 = (G1) gcode;
							//don't draw upwards moves
							if(g1.e_value == null);
							{
								//if a parameter is not given it defaults t the last one
								int current_x = g1.x_value._variableValue != null ? g1.x_value._variableValue.intValue() : last_x;
								int current_y = g1.y_value._variableValue != null ? g1.y_value._variableValue.intValue() : last_y;
								//draw the last line
								//make sure this isn't the first one
								PathOnChart path1 = new PathOnChart(drawPath((current_x / x_mm_per_point), (current_y / y_mm_per_point), (last_x / x_mm_per_point), (last_y / y_mm_per_point)), pathAttributes1);
								paths.add(path1);
								//only set if set in command
								if(g1.x_value._variableValue != null)
								{
									last_x = g1.x_value._variableValue.intValue();
								}
								if(g1.y_value._variableValue != null)
								{
									last_y = g1.y_value._variableValue.intValue();
								}
							}
						}
					}
					
					/*
					 * this doesn't seem to work for some reason
					 */
					//draw bed
					//ArrayList<PathOnChart> bed = drawBed();
					//for(PathOnChart path : bed)
					//{
						//paths.add(path);
					//}
					
					//ArrayList<PointOnChart> points3 = new ArrayList<PointOnChart>();
					//points3.add(new PointOnChart((Settings.bedWidth + 1), (Settings.bedHeight + 1)));
					//paths.add(new PathOnChart(points3, pathAttributes1));
					
					LineChartAttributes lineChartAttributes = new LineChartAttributes();
					lineChartAttributes.setBackgroundColor("#aaabbb");
					return new LineChartView(getActivity(), paths, lineChartAttributes);
			  }
			  else
			  {
				  return inflater.inflate(R.layout.view, container, false);
			  }
	  	}
			  
			  private ArrayList<PointOnChart> drawPath(int x1, int y1, int x2, int y2)
			  {
				  
				  int higherx = Math.max(x1, x2);
				  int lowerx = Math.min(x1, x2);
				  int highery = Math.max(y1, y2);
				  int lowery = Math.min(y2, y1);
				  
				  ArrayList<PointOnChart> points2 = new ArrayList<PointOnChart>();
				  
				  //handle this annoying special case
				  if(higherx - lowerx == 0)
				  {
					  int width = highery - lowery;
					  for(int counter = 0; counter < width; counter ++)
					  {
						  points2.add(new PointOnChart(0,counter));
					  }
				  }
				  
				  float ratio = ((float)highery - lowery) / ((float)higherx - lowerx);
				  
				  int width = higherx - lowerx;
				  for(int i = 0; i < width; i++) 
				  {
				      float x = lowerx + i;
				      float y = lowery + (ratio * i);
				      points2.add(new PointOnChart(x,y));
				      
				   }
				  
				  return points2;
			  }
			  
			  /**
			   * draws the bed according to the dimensions in settings
			   * @return an arraylist of paths
			   */
			  private ArrayList<PathOnChart> drawBed()
			  {
				  	ArrayList<PathOnChart> paths = new ArrayList<PathOnChart>();
					PathAttributes pathAttributes1 = new PathAttributes();
					pathAttributes1.setPointColor("#00AAAAAA");
					pathAttributes1.setPathColor("#FF302F");
					//|
					//|
					//|
					paths.add(new PathOnChart(drawPath(0, 0, 0, Settings.bedHeight), pathAttributes1));
					//---
					paths.add(new PathOnChart(drawPath(Settings.bedWidth, 0, 0, 0), pathAttributes1));
					//   |
					//   |
					//   |
					paths.add(new PathOnChart(drawPath(Settings.bedWidth, 0, Settings.bedWidth, Settings.bedHeight), pathAttributes1));
					//---
					paths.add(new PathOnChart(drawPath(0, Settings.bedHeight, Settings.bedWidth, Settings.bedHeight), pathAttributes1));
					return paths;
				  
			  }
	    }


