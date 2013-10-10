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

import java.util.LinkedList;
import java.util.concurrent.ExecutionException;

import com.multiplemonomials.printerdroid.R;
import com.multiplemonomials.androidutils.LineReader;
import com.multiplemonomials.printerdroid.PrinterService.MyLocalBinder;
import com.multiplemonomials.printerdroid.gcodeparser.Layer;
import com.multiplemonomials.printerdroid.gcodeparser.ParserAsyncTask;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
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
	public static Context appContext;
	
	ConsoleFragment consoleFragment;
	
	PrinterService myService;
	
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
        if(!isMyServiceRunning())
        {
        	intent = new Intent(this, PrinterService.class);
        	stopService(intent);
        	startService(intent);
        	Log.i(TAG, "Printer Service Started");
        }
        else
        {
        	Log.i(TAG, "Printer Service Running");
        }
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
			case R.id.menuitem_search:
				Toast.makeText(appContext, "search", Toast.LENGTH_SHORT).show();
				return true;
			case R.id.menuitem_add:
				Toast.makeText(appContext, "add", Toast.LENGTH_SHORT).show();
				return true;
			case R.id.menuitem_share:
				Toast.makeText(appContext, "share", Toast.LENGTH_SHORT).show();
				return true;
			case R.id.menuitem_feedback:
				Toast.makeText(appContext, "feedback", Toast.LENGTH_SHORT).show();
				return true;
			case R.id.menuitem_about:
				Toast.makeText(appContext, "about", Toast.LENGTH_SHORT).show();
				return true;
			case R.id.menuitem_quit:
				Toast.makeText(appContext, "quit", Toast.LENGTH_SHORT).show();
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
		myService.doSend(consoleFragment.editText.getText().toString());
	}

	@Override
	public void onNewConsole() {
		consoleFragment.onNewConsole();
		
	}
	
	//called by ParserAsyncTask
	ProgressDialog progressDialog;
	public void showLayerProgressBar()
	{
		progressDialog = new ProgressDialog(this);
		progressDialog.setTitle("Parsing File...");
		progressDialog.setMessage("Parsing Layer 1...");
		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		progressDialog.show();
		
	}
	
	public void updateLayerProgressBar(int progress)
	{
		if(progressDialog != null)
		{
			progressDialog.setMessage("Parsing Layer " + progress + "...");
		}
		
	}
	
	public void closeLayerProgressBar()
	{
		if(progressDialog != null)
		{
			progressDialog.dismiss();
		}
		
	}
	
	public void onClickLoadFile(View view)
	{
		LineReader lineReader = new LineReader(getResources().openRawResource(R.raw.test2));
		ParserAsyncTask parserAsyncTask = new ParserAsyncTask(this);
		parserAsyncTask.execute(lineReader);
		try 
		{
			LinkedList<Layer> codes = (LinkedList<Layer>)parserAsyncTask.get();
		}
		catch (InterruptedException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (ExecutionException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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