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
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends Activity implements ConsoleListener {
	private static final String TAG = "Printerdroid";
	private static final int FILE_REQUEST_CODE = 0;
	private static final int PREFERENCES_ACTIVITY_REQUEST_CODE = 1;
	public static Context appContext;
	
	ConsoleFragment consoleFragment;
	
	PrinterService myService;
	
	Thread temperatureChecker; 
	
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
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        appContext = getApplicationContext();
        //Set up settings database
        Settings.regenerate(this);
        
        //start service
        Intent intent = new Intent(this, PrinterService.class);
	  	if(!isMyServiceRunning())
	  	{
	  		startService(intent);  
	  	}
	  	bindService(intent, myConnection, 0);

       //ActionBar
        ActionBar actionbar = getActionBar();
        actionbar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        
        ActionBar.Tab ConsoleTab = actionbar.newTab().setText("Console");
        ActionBar.Tab ViewTab = actionbar.newTab().setText("View");
        ActionBar.Tab OverviewTab = actionbar.newTab().setText("Overview");
        
        consoleFragment = new ConsoleFragment();
        Fragment viewFragment = new ViewFragment();
        Fragment overviewFragment = new OverviewFragment();

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
    				
    				if(myService != null && myService.driver != null)
    				{
    					myService.setWaitingForResponce();
    					//M105 tells the printer to print back its temperatures
    					MainActivity.this.myService.doSend("M105");
    				}
    				//wait a while
    				try 
    				{
    					sleep(2000);
    				} catch (InterruptedException e) 
    				{
    					return;
    				}
    				
    				if(myService != null && myService.driver != null)
    				{
    					if(myService.responce != null && myService.responce.endsWith("\n"))
    					{
    						
    						int heaterTempIndex = MainActivity.this.myService.responce.indexOf("T:") + 2;
    						
    						try
    						{
    							String intermediaryHeaterTemp = MainActivity.this.myService.responce.substring(heaterTempIndex);
    							//sometimes it seems like the second half of the temp command responce command gets cut off
    							//which is fine. because that's what we were trying to do anyway
    							int bedTempIndex = intermediaryHeaterTemp.indexOf("B");
    							String heaterTempString;
    							if(bedTempIndex != -1)
    							{
    								heaterTempString = intermediaryHeaterTemp.substring(0, bedTempIndex - 1);
    							}
    							else
    							{
    								heaterTempString = intermediaryHeaterTemp;
    							}

    							int heaterTemp = Integer.parseInt(heaterTempString);
    							Log.i(TAG, "heaterTemp: " + heaterTemp);	
    						}
    						catch(NumberFormatException error)
    						{
    							Log.e("PrinterdroidTemperatureThread", "Failed to parse temperature: " + MainActivity.this.myService.responce);
    						}
    					
    					}
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
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }
    
    private boolean isMyServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (PrinterService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
			case R.id.menuitem_preferences:
				startActivityForResult(new Intent(this, PreferencesActivity.class), PREFERENCES_ACTIVITY_REQUEST_CODE);
				return true;
		}
		return false;
	}
	
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("tab", getActionBar().getSelectedNavigationIndex());
    }
    
	public void doRestart(View view)
	{
		Intent intent = new Intent(this, PrinterService.class);
		stopService(intent);
    	startService(intent);
    	bindService(intent, myConnection, 0);
	}
	
	public void doClear(View view)
	{
		myService.clearConsole();
		consoleFragment.onNewConsole();
	}
	
	public void doSend(View view)
	{
		//if a driver is not connected, show an error dialog.
		if(myService.driver != null)
		{
			myService.doSend(consoleFragment.editText.getText().toString());
		}
		else
		{
			ErrorDialog.showErrorDialog(this, R.string.app_name, "No device connected.");
		}
	}

	@Override
	public void onNewConsole() {
		consoleFragment.onNewConsole();
		
	}
	
	
	public void onClickLoadFile(View view)
	{
		//get a file from a file browser
		 Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		    intent.setType("file/*");
		    startActivityForResult(intent, FILE_REQUEST_CODE);
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

	    
}



class MyTabsListener implements ActionBar.TabListener {
	public Fragment fragment;
	
	public MyTabsListener(Fragment fragment) {
		this.fragment = fragment;
	}
	
	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
		Toast.makeText(MainActivity.appContext, "Reselected!", Toast.LENGTH_LONG).show();
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		ft.replace(R.id.fragment_container, fragment);
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		ft.remove(fragment);
	}
	

	
}