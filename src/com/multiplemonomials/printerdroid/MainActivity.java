//package com.multiplemonomials.printerdroid;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import android.app.ActionBar;
//import android.app.ActionBar.Tab;
//import android.app.ActionBar.TabListener;
//import android.app.Activity;
//import android.app.Fragment;
//import android.app.FragmentTransaction;
//import android.os.Bundle;
// 
//public class MainActivity extends Activity
//{
//	@Override
//	protected void onCreate(Bundle savedInstanceState) {
//	    super.onCreate(savedInstanceState);   
//	    setContentView(R.layout.activity_main);
//	    //ActionBar bar = getActionBar();
//	    //bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
//	    
//	    //ActionBar.Tab ConsoleTab = bar.newTab().setText("Console");
//	    
//	    //Fragment ConsoleFragment = new ConsoleFragment();
//	    
//	    //.setTabListener(new MyTabsListener(ConsoleFragment));
//	    
//	    //bar.addTab(ConsoleTab);
//	    Fragment consoleFragment = new ConsoleFragment();
//	    FragmentTransaction transaction = getFragmentManager().beginTransaction();
//	    transaction.replace(R.id.fragment_container, consoleFragment);
//	    transaction.addToBackStack(null);
//	    
//	    transaction.commit();
//	}
//}

package com.multiplemonomials.printerdroid;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.ExecutionException;

import com.multiplemonomials.androidutils.LineReader;
import com.multiplemonomials.androidutils.Pair;
import com.multiplemonomials.androidutils.preferencesmanager.PreferencesActivity;
import com.multiplemonomials.androidutils.progressbox.ErrorDialog;
import com.multiplemonomials.androidutils.progressbox.ProgressBoxManager;
import com.multiplemonomials.printerdroid.R;
import com.multiplemonomials.printerdroid.PrinterService.MyLocalBinder;
import com.multiplemonomials.printerdroid.gcodeparser.Layer;
import com.multiplemonomials.printerdroid.gcodeparser.ParserAsyncTask;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity implements ConsoleListener 
{
	
	//--------------------------------------------------------
	// Class Scope Variables
	//--------------------------------------------------------
	
	private static final String TAG = "Printerdroid";
	private static final int FILE_REQUEST_CODE = 0;
	private static final int PREFERENCES_ACTIVITY_REQUEST_CODE = 1;
	public static Context appContext;
	
	ConsoleFragment consoleFragment;
	
	OverviewFragment overviewFragment;
	
	PrinterService myService;
	
	Thread temperatureChecker; 
	
	Intent printerServiceIntent;
	
	protected ServiceConnection myConnection = new ServiceConnection() {

	    public void onServiceConnected(ComponentName className,
	            IBinder service) {
	    	Log.i(TAG, "Service Connected");
	        MyLocalBinder binder = (MyLocalBinder) service;
	        myService = binder.getService();
	        myService.bindToConsole(MainActivity.this);
	        MainActivity.this.consoleFragment.textView.setText(myService.currentConsole);
	    }
	    
	    public void onServiceDisconnected(ComponentName arg0) {
	    }
	    
	   };
	   
	//--------------------------------------------------------
	// Lifecycle Functions
	//--------------------------------------------------------
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        appContext = getApplicationContext();
        //Set up settings database
        Settings.regenerate(this);
        
        //start service
        printerServiceIntent = new Intent(this, PrinterService.class);
	  	if(!isMyServiceRunning())
	  	{
	  		startService(printerServiceIntent);  
	  	}
	  	bindService(printerServiceIntent, myConnection, 0);

       //ActionBar
        ActionBar actionbar = getActionBar();
        actionbar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        
        ActionBar.Tab ConsoleTab = actionbar.newTab().setText("Console");
        ActionBar.Tab ViewTab = actionbar.newTab().setText("View");
        ActionBar.Tab OverviewTab = actionbar.newTab().setText("Overview");
        
        consoleFragment = new ConsoleFragment();
        Fragment viewFragment = new ViewFragment();
        overviewFragment = new OverviewFragment();

        ConsoleTab.setTabListener(new MyTabsListener(consoleFragment));
        ViewTab.setTabListener(new MyTabsListener(viewFragment));
        OverviewTab.setTabListener(new MyTabsListener(overviewFragment));

        actionbar.addTab(ConsoleTab);
        actionbar.addTab(ViewTab);
        actionbar.addTab(OverviewTab);
        
        if(isMyServiceRunning())
        {
        	Log.i(TAG, "Printer Service Running");
        }
        else
        {
        	Log.e(TAG, "Printer Service Not Started");
        }
    
    }
    
    @Override
    public void onResume()
    {
    	super.onResume();
    	
    	temperatureChecker = new Thread()
    	{
    		@Override
    		public void run()
    		{
    			String TAG = "PrinterdroidTempThread";
    			
    			Log.i(TAG, "Starting...");
    			
    			//loop until we're told to stop.
    			while(!Thread.interrupted())
    			{
    				assert(MainActivity.this.myService != null);
    				
    				//get responce
    				if(myService != null && myService.driver != null)
    				{
    					MainActivity.this.myService.send("M105");
    				}
    				
    				try 
    				{
						sleep(3000);
					}
    				catch (InterruptedException e) 
    				{
						return;
					}
    				
    			}
    			
    			Log.i(TAG, "Shutting down...");
    		}
    	};
        temperatureChecker.start();
   
    }
    
    @Override
    public void onPause()
    {
    	super.onPause();
    	//signal the temperature checking thread to shutdown.
    	temperatureChecker.interrupt();
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) 
    {
        super.onSaveInstanceState(outState);
        outState.putInt("tab", getActionBar().getSelectedNavigationIndex());
    }
    
    @Override
    public void onDestroy()
    {
    	super.onDestroy();
    	unbindService(myConnection);
    	temperatureChecker.interrupt();
    	myService = null;
    }
    
	//--------------------------------------------------------
	// Misc. GUI handlers
	//--------------------------------------------------------
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }
    
	@Override
	public boolean onOptionsItemSelected(MenuItem item) 
	{
		switch(item.getItemId()) 
		{
			case R.id.menuitem_preferences:
				startActivityForResult(new Intent(this, PreferencesActivity.class), PREFERENCES_ACTIVITY_REQUEST_CODE);
				return true;
		}
		return false;
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if(requestCode == FILE_REQUEST_CODE)
		{
			//get the file that was returned
			//check if the user pressed cancel
			if(data != null)
			{
				Uri fileUri = data.getData();
				Settings.currentFilePath = fileUri;
				File file = new File(fileUri.getPath());
				Log.v(TAG, "Loaded file " + file.getPath());
				Log.v(TAG, "File exists: " + file.exists());
				
				//read its data
				Settings.currentFile = loadFile(file);
				Log.i(TAG, "current file has " + Settings.currentFile.size() + " layers");
			}

		}
		else if(requestCode == PREFERENCES_ACTIVITY_REQUEST_CODE)
		{
			Settings.regenerate(this);
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
	    //Handle the back button
	    if((keyCode == KeyEvent.KEYCODE_BACK) && (myService.isPrinting)) 
	    {
	    	if((myService.isPrinting))
	    	{
		        //Ask the user if they want to quit
		        new AlertDialog.Builder(this)
		        .setIcon(android.R.drawable.ic_dialog_alert)
		        .setTitle("Quit?")
		        .setMessage("Close the app and cancel print?")
		        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

		            @Override
		            public void onClick(DialogInterface dialog, int which) {

		                //Stop the activity
		            	MainActivity.this.myService.cancelPrint();
		            	MainActivity.this.myService.stopService(MainActivity.this.printerServiceIntent);
		                MainActivity.this.finish();    
		            }

		        })
		        .setNegativeButton(android.R.string.ok, null)
		        .show();

		        return true;
	    	}
	    	else
	    	{
	    		stopService(printerServiceIntent);
	    		finish();
	    		return true;
	    	}

	    }
	    
	    else 
	    {
	        return super.onKeyDown(keyCode, event);
	    }

	}
    
	//--------------------------------------------------------
	// Temperature Thread
	//--------------------------------------------------------
    
    public void onBedTemperature(String response)
    {
    	if(response != null && response.endsWith("\n"))
		{
			try
			{
				int heaterTempIndex = response.indexOf("T:") + 2;
				String intermediaryHeaterTemp = response.substring(heaterTempIndex);
				
				int bedTempIndex = intermediaryHeaterTemp.indexOf("B");
				//make a new string with a new backing array
				String bedTempString = new String(intermediaryHeaterTemp);
				if(bedTempString.indexOf("B:") != -1)
				{
					bedTempString = bedTempString.substring((bedTempString.indexOf("B:") + 2));
					//remove ending carriage return and newline 
					bedTempString = bedTempString.substring(0, bedTempString.length() - 2);
				}
					
				
				//sometimes it seems like the second half of the temp command responce command gets cut off
				//which is fine. because that's what we were trying to do anyway
				String heaterTempString;
				if(bedTempIndex != -1)
				{
					heaterTempString = intermediaryHeaterTemp.substring(0, bedTempIndex - 1);
				}
				else
				{
					heaterTempString = intermediaryHeaterTemp;
				}
				
				Settings.current_heater_temp = Integer.parseInt(heaterTempString);
				Settings.current_bed_temp = Integer.parseInt(bedTempString);
				
				//check if the overview fragment is being shown
				if(MainActivity.this.overviewFragment.heaterTempTextView != null)
				{
					runOnUiThread(new Runnable()
					{

						@Override
						public void run() {
							MainActivity.this.overviewFragment.setHeaterTemperatureView(Settings.current_heater_temp);
							MainActivity.this.overviewFragment.setBedTemperatureView(Settings.current_bed_temp);			
						}
						
					});
				}
			}
			catch(NumberFormatException error)
			{
				Log.e("PrinterdroidTemperatureThread", "Failed to parse temperature: " + response);
			}
			catch(StringIndexOutOfBoundsException error)
			{
				Log.e("PrinterdroidTemperatureThread", "Length of temperature responce is wrong: " + response);
			}
		
		}
    }
    
	//--------------------------------------------------------
	// Service stuff
	//--------------------------------------------------------
    
    private boolean isMyServiceRunning() 
    {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        
        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) 
        {
            if (PrinterService.class.getName().equals(service.service.getClassName())) 
            {
                return true;
            }
        }
        return false;
    }
    
	//--------------------------------------------------------
	// Button Handlers
	//--------------------------------------------------------
    
	public void doRestart(View view)
	{
		if(myService == null)
		{
			Toast.makeText(this, "Service not running or crashed, restarting", Toast.LENGTH_LONG).show();
		}
		else
		{
			myService.retryConnection();
			myService.rebootQueue();
		}
	}
	
	public void doClear(View view)
	{
		myService.clearConsole();
		consoleFragment.showConsole();
	}
	
	public void doSend(View view)
	{
		//if a driver is not connected, show an error dialog.
		if(myService.driver != null)
		{
			myService.send(consoleFragment.editText.getText().toString());
		}
		else
		{
			ErrorDialog.showErrorDialog(this, R.string.app_name, "No device connected.");
		}
	}
	
	public void onClickLoadFile(View view)
	{
		//get a file from a file browser
		 Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		    intent.setType("file/*");
		    startActivityForResult(intent, FILE_REQUEST_CODE);
	}
	
	/**
	 * Button handler for the button that turns the printer on or off
	 * 
	 */
	public void onClickPrinterOnOff(View view)
	{
		Button button = (Button) view;
		if(Settings.printerIsOn)
		{
			button.setText(getResources().getString(R.string.turn_on_printer));
			//M81 = printer off
			myService.send("M81");
			Settings.printerIsOn = false;
		}
		else
		{
			button.setText(getResources().getString(R.string.turn_off_printer));
			//M80 = printer off
			myService.send("M80");
			Settings.printerIsOn = true;
		}
	}
	
	public void onClickHeaterOnOff(View view)
	{
		Button button = (Button) view;
		if(Settings.current_heater_state_on)
		{
			//we need to toggle the heater off
			button.setText(getResources().getString(R.string.turn_on) + Settings.target_heater_temp + getResources().getString(R.string.degrees_centegrade));
			//M104 Sxxx= set extruder temp
			myService.send("M104 S0");
			Settings.current_heater_state_on = false;
		}
		else
		{
			//we need to turn the heater on
			button.setText(getResources().getString(R.string.turn_off) + "0" + getResources().getString(R.string.degrees_centegrade));
			//M104 Sxxx= set extruder temp
			myService.send("M104 S " + Settings.target_heater_temp);
			Settings.current_heater_state_on = true;
		}
	}
	
	public void onClickBedOnOff(View view)
	{
		Button button = (Button) view;
		if(Settings.current_bed_state_on)
		{
			//we need to toggle the heater off
			button.setText(getResources().getString(R.string.turn_on) + Settings.target_bed_temp + getResources().getString(R.string.degrees_centegrade));
			//M140 Sxxx= set bed temp
			myService.send("M140 S0");
			Settings.current_bed_state_on = false;
		}
		else
		{
			//we need to turn the heater on
			button.setText(getResources().getString(R.string.turn_off) + "0" + getResources().getString(R.string.degrees_centegrade));
			//M140 Sxxx= set bed temp
			myService.send("M140 S " + Settings.target_bed_temp);
			Settings.current_bed_state_on = true;
		}
	}
	
	public void onClickHome(View view)
	{
		myService.send("G28");
	}
	
	public void onClickPrint(View view)
	{
		Button button = (Button) view;
		
		//if we're currently printing, the button turns to cancel
		if(!myService.isPrinting)
		{
			if(Settings.currentFile != null)
			{
				try 
				{
					myService.print(Settings.currentFilePath, this);
				} 
				catch (FileNotFoundException e) 
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				button.setText(getResources().getString(android.R.string.cancel));
			}
			else
			{
				Toast.makeText(this, "No File Loaded", Toast.LENGTH_SHORT).show();
			}
		}
		else
		{
			myService.cancelPrint();
			button.setText(getResources().getString(R.string.print));
		}
	}
	
	//--------------------------------------------------------
	// Service console handling
	//--------------------------------------------------------

	@Override
	public void onNewConsole() 
	{
		consoleFragment.showConsole();
	}

	public List<Layer> loadFile(File file) 
	{
		try 
		{
			//setup file stream
			InputStream inputStream = new FileInputStream(file);
			LineReader lineReader = new LineReader(inputStream);
			
			//run parser
			ParserAsyncTask parserAsyncTask = new ParserAsyncTask(new ProgressBoxManager(this));
			parserAsyncTask.execute(lineReader);
			List<Layer> codes = (List<Layer>)parserAsyncTask.get();
			return codes;
		}
		catch (FileNotFoundException e) 
		{
			e.printStackTrace();
		}
		catch (InterruptedException e) 
		{
			e.printStackTrace();
		} 
		catch (ExecutionException e) 
		{
			e.printStackTrace();
		}
		return null;
	}
	
	
	//--------------------------------------------------------
	// Printing Code
	//--------------------------------------------------------

	//called by PrintAsyncTask when it wants us to show the progress bar
	public void showLayerProgressBar() 
	{
		//at some point I would like to have some sort of transition that pops out the progress bar
	}

	/**Called by PrintAsyncTask every time it finishes printing a layer.
	 * Changes the value of OverviewFragment's progress bar
	 * 
	 * @param progress a pair holding the current layer and the current line, respectively
	 */
	public void updateLayerProgressBar(Pair<Integer, Integer> progress) 
	{
		assert(overviewFragment.layerProgressView != null);
		overviewFragment.layerProgressView.setMax(Settings.currentFile.size());
		overviewFragment.layerProgressView.setProgress(progress._first);
		overviewFragment.layerTextView.setText(getResources().getString(R.string.currentlayer) + progress._first);
	}

	public void closeLayerProgressBar() 
	{
		//at some point I would like to have some sort of transition that hides the progress bar
		
	}

	    
}



class MyTabsListener implements ActionBar.TabListener
{
	public Fragment fragment;
	
	public MyTabsListener(Fragment fragment) 
	{
		this.fragment = fragment;
	}
	
	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) 
	{
		Toast.makeText(MainActivity.appContext, "Reselected!", Toast.LENGTH_LONG).show();
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) 
	{
		ft.replace(R.id.fragment_container, fragment);
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) 
	{
		ft.hide(fragment);
	}
	

	
}