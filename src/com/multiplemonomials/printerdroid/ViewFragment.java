package com.multiplemonomials.printerdroid;

import java.lang.reflect.Field;
import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.opengles.GL10;

import com.multiplemonomials.printerdroid.view3d.DrawFile;
import com.threed.jpct.Camera;
import com.threed.jpct.FrameBuffer;
import com.threed.jpct.Light;
import com.threed.jpct.Logger;
import com.threed.jpct.Object3D;
import com.threed.jpct.RGBColor;
import com.threed.jpct.SimpleVector;
import com.threed.jpct.World;
import com.threed.jpct.util.MemoryHelper;

import android.app.Fragment;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;

public class ViewFragment extends Fragment implements OnTouchListener {
	
	public int layerToDraw = 0;
	
	// Used to handle pause and resume...
	private static ViewFragment master = null;
	
	private GLSurfaceView mGLView;
	private MyRenderer renderer = null;
	private FrameBuffer fb = null;
	private World world = null;
	private RGBColor back = new RGBColor(50, 50, 100);

	private float touchTurn = 0;
	private float touchTurnUp = 0;

	private float xpos = -1;
	private float ypos = -1;

	private Object3D object = null;
	private int fps = 0;

	private Light sun = null;

	
	
	public void onCreate(Bundle savedInstanceState)
	{
		Logger.log("onCreate");

		if (master != null) {
			copy(master);
		}

		super.onCreate(savedInstanceState);

	}
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
	{
		if(Settings.currentFile != null)
		{
			mGLView = new GLSurfaceView(getActivity().getApplication());
			
			mGLView.setEGLConfigChooser(new GLSurfaceView.EGLConfigChooser() {
				public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) {
					// Ensure that we get a 16bit framebuffer. Otherwise, we'll fall
					// back to Pixelflinger on some device (read: Samsung I7500)
					int[] attributes = new int[] { EGL10.EGL_DEPTH_SIZE, 16, EGL10.EGL_NONE };
					EGLConfig[] configs = new EGLConfig[1];
					int[] result = new int[1];
					egl.eglChooseConfig(display, attributes, configs, 1, result);
					return configs[0];
				}
			});
			
			//jPCT memory clearing function
			MemoryHelper.compact();
			
			renderer = new MyRenderer();
			mGLView.setRenderer(renderer);
			mGLView.setOnTouchListener(this);
			
			return mGLView;
		}

		else
		{
			return inflater.inflate(R.layout.view, null, false);
		}
		
	}
	
	private void copy(Object src) {
		try {
			Logger.log("Copying data from master Activity!");
			Field[] fs = src.getClass().getDeclaredFields();
			for (Field f : fs) {
				f.setAccessible(true);
				f.set(this, f.get(src));
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	protected boolean isFullscreenOpaque() {
		return true;
	}
	
	public boolean onTouch(View view, MotionEvent me) {

		if (me.getAction() == MotionEvent.ACTION_DOWN) {
			xpos = me.getX();
			ypos = me.getY();
			return true;
		}

		if (me.getAction() == MotionEvent.ACTION_UP) {
			xpos = -1;
			ypos = -1;
			touchTurn = 0;
			touchTurnUp = 0;
			return true;
		}

		if (me.getAction() == MotionEvent.ACTION_MOVE) {
			float xd = me.getX() - xpos;
			float yd = me.getY() - ypos;

			xpos = me.getX();
			ypos = me.getY();

			touchTurn = xd / -100f;
			touchTurnUp = yd / -100f;
			return true;
		}

		try {
			Thread.sleep(15);
		} catch (Exception e) {
			// No need for this...
		}

		return false;
	}


			  
			  class MyRenderer implements GLSurfaceView.Renderer {

					private long time = System.currentTimeMillis();

					public MyRenderer() {
					}

					public void onSurfaceChanged(GL10 gl, int w, int h) {
						if (fb != null) {
							fb.dispose();
						}
						fb = new FrameBuffer(gl, w, h);

						if (master == null) {

							world = new World();
							world.setAmbientLight(20, 20, 20);

							sun = new Light(world);
							sun.setIntensity(250, 250, 250);

							// Create a texture out of the icon...:-)
							//Texture texture = new Texture(BitmapHelper.rescale(BitmapHelper.convert(getResources().getDrawable(R.drawable.ic_launcher)), 64, 64));
							//TextureManager.getInstance().addTexture("texture", texture);

//							cube = Primitives.getCube(10);
//							cube.calcTextureWrapSpherical();
//							cube.setTexture("texture");
//							cube.strip();
//							cube.build();
							
							object = DrawFile.drawFile();

							world.addObject(object);

							Camera cam = world.getCamera();
							cam.moveCamera(Camera.CAMERA_MOVEOUT, 50);
							cam.lookAt(new SimpleVector(100, 100, 50));

							SimpleVector sv = new SimpleVector(Settings.bedWidth/2, Settings.bedHeight/2, 50);
							sv.y -= 100;
							sv.z -= 100;
							sun.setPosition(sv);
							MemoryHelper.compact();

							if (master == null) {
								Logger.log("Saving master Activity!");
								master = ViewFragment.this;
							}
						}
					}

					public void onDrawFrame(GL10 gl) {
						if (touchTurn != 0) {
							object.rotateY(touchTurn);
							touchTurn = 0;
						}

						if (touchTurnUp != 0) {
							object.rotateX(touchTurnUp);
							touchTurnUp = 0;
						}

						fb.clear(back);
						world.renderScene(fb);
						world.draw(fb);
						fb.display();

						if (System.currentTimeMillis() - time >= 1000) {
							Logger.log(fps + "fps");
							fps = 0;
							time = System.currentTimeMillis();
						}
						fps++;
					}

					@Override
					public void onSurfaceCreated(GL10 gl, EGLConfig config) {
						// TODO Auto-generated method stub
						
					}
				}
			  
			  
}


