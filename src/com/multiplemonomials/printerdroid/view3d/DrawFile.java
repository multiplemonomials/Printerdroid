package com.multiplemonomials.printerdroid.view3d;

import java.util.List;

import android.graphics.Color;

import com.multiplemonomials.printerdroid.Settings;
import com.multiplemonomials.printerdroid.gcodeparser.G1;
import com.multiplemonomials.printerdroid.gcodeparser.Gcode;
import com.multiplemonomials.printerdroid.gcodeparser.Layer;
import com.multiplemonomials.printerdroid.gcodeparser.TextCode;

import com.threed.jpct.Object3D;
import com.threed.jpct.Primitives;
import com.threed.jpct.SimpleVector;
import com.threed.jpct.Texture;
import com.threed.jpct.TextureManager;

public class DrawFile 
{
	public static Object3D drawFile()
	{
		
		Object3D lines = Primitives.getCube(5.0F);
		
		// note: you will need to define a texture ahead of time.  For a solid-color line, you could use something like:
		TextureManager.getInstance().addTexture( "GviewLine", new Texture( 8, 8, Color.RED ) );
		
		DrawLine.createLine(new SimpleVector(0.0, 0.0, 0.0), new SimpleVector(1.0, 1.0, 1.0), 1.0F, "GviewLine");
		
		List<Layer> currentFile = Settings.currentFile;
      int last_x = 0;
      int last_y = 0;
      int last_z = 0;
      
      //chart library is autoscaling
      int x_mm_per_point = 1;
      int y_mm_per_point = 1;
      
      for(Layer layer : currentFile)
      {
     	 for(Gcode gcode : layer.codes)
          {
              //are we working with a move command?
              if(!(gcode instanceof TextCode))
              {                    	 
                  G1 g1 = (G1) gcode;
                  //if a parameter is not given it defaults t the last one
                  int current_x = g1.x_value._variableValue != null ? g1.x_value._variableValue.intValue() : last_x;
                  int current_y = g1.y_value._variableValue != null ? g1.y_value._variableValue.intValue() : last_y;
                  int current_z = g1.z_value._variableValue != null ? g1.z_value._variableValue.intValue() : last_z;
                  //draw the last line
                  //make sure this isn't the first one
                  Object3D line = DrawLine.createLine(new SimpleVector((current_x / x_mm_per_point), (current_y / y_mm_per_point), current_z), new SimpleVector((last_x / x_mm_per_point), (last_y / y_mm_per_point), current_z), 1.0f, "GviewLine");
                  lines = Object3D.mergeObjects(lines, line);
                  //addChild(line);
                  
                  //only set if set in command
                  if(g1.x_value._variableValue != null)
                  {
                          last_x = g1.x_value._variableValue.intValue();
                  }
                  if(g1.y_value._variableValue != null)
                  {
                          last_y = g1.y_value._variableValue.intValue();
                  }
                  if(g1.z_value._variableValue != null)
                  {
                          last_z = g1.z_value._variableValue.intValue();
                  }
              }
                  
          }
      }
      
      return lines;
	}
}
