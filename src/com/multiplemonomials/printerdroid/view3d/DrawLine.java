package com.multiplemonomials.printerdroid.view3d;

import com.threed.jpct.Object3D;
import com.threed.jpct.SimpleVector;
import com.threed.jpct.TextureManager;

public class DrawLine {
	


	public static Object3D createLine (SimpleVector pointA, SimpleVector pointB, float width, String textureName)
	{
		
	    Object3D line = new Object3D( 8 );
	    float offset = width / 2.0f;

	    // Quad A:
	    line.addTriangle( new SimpleVector( pointA.x, pointA.y - offset, pointA.z ), 0, 0,
	                     new SimpleVector( pointA.x, pointA.y + offset, pointA.z ), 0, 1,
	                     new SimpleVector( pointB.x, pointB.y + offset, pointB.z ), 1, 1,
	                     TextureManager.getInstance().getTextureID( textureName ) );
	    line.addTriangle( new SimpleVector( pointB.x, pointB.y + offset, pointB.z ), 0, 0,
	                     new SimpleVector( pointB.x, pointB.y - offset, pointB.z ), 0, 1,
	                     new SimpleVector( pointA.x, pointA.y - offset, pointA.z ), 1, 1,
	                     TextureManager.getInstance().getTextureID( textureName ) );
	    // Quad A, back-face:
	    line.addTriangle( new SimpleVector( pointB.x, pointB.y - offset, pointB.z ), 0, 0,
	                     new SimpleVector( pointB.x, pointB.y + offset, pointB.z ), 0, 1,
	                     new SimpleVector( pointA.x, pointA.y + offset, pointA.z ), 1, 1,
	                     TextureManager.getInstance().getTextureID( textureName ) );
	    line.addTriangle( new SimpleVector( pointA.x, pointA.y + offset, pointA.z ), 0, 0,
	                     new SimpleVector( pointA.x, pointA.y - offset, pointA.z ), 0, 1,
	                     new SimpleVector( pointB.x, pointB.y - offset, pointB.z ), 1, 1,
	                     TextureManager.getInstance().getTextureID( textureName ) );
	    // Quad B:
	    line.addTriangle( new SimpleVector( pointA.x, pointA.y, pointA.z + offset ), 0, 0,
	                     new SimpleVector( pointA.x, pointA.y, pointA.z - offset ), 0, 1,
	                     new SimpleVector( pointB.x, pointB.y, pointB.z - offset ), 1, 1,
	                     TextureManager.getInstance().getTextureID( textureName ) );
	    line.addTriangle( new SimpleVector( pointB.x, pointB.y, pointB.z - offset ), 0, 0,
	                     new SimpleVector( pointB.x, pointB.y, pointB.z + offset ), 0, 1,
	                     new SimpleVector( pointA.x, pointA.y, pointA.z + offset ), 1, 1,
	                     TextureManager.getInstance().getTextureID( textureName ) );
	    // Quad B, back-face:
	    line.addTriangle( new SimpleVector( pointB.x, pointB.y, pointB.z + offset ), 0, 0,
	                     new SimpleVector( pointB.x, pointB.y, pointB.z - offset ), 0, 1,
	                     new SimpleVector( pointA.x, pointA.y, pointA.z - offset ), 1, 1,
	                     TextureManager.getInstance().getTextureID( textureName ) );
	    line.addTriangle( new SimpleVector( pointA.x, pointA.y, pointA.z - offset ), 0, 0,
	                     new SimpleVector( pointA.x, pointA.y, pointA.z + offset ), 0, 1,
	                     new SimpleVector( pointB.x, pointB.y, pointB.z + offset ), 1, 1,
	                     TextureManager.getInstance().getTextureID( textureName ) );

	    // If you don't want the line to react to lighting:
	    //line.setLighting( Object3D.LIGHTING_NO_LIGHTS );
	    //line.setAdditionalColor( Color.WHITE );

	    // done
	    return line;
	}

}
