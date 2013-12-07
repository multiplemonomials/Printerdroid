package com.multiplemonomials.printerdroid;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.HexDump;
import com.hoho.android.usbserial.util.SerialInputOutputManager;
import com.multiplemonomials.androidutils.LineReader;
import com.multiplemonomials.androidutils.progressbox.*;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

/**
 * Service for handling serial communications with the printer.
 * @author Jamie
 *
 */
public class PrinterService extends Service {
	
	static PrinterService instance;
	
	private final IBinder myBinder = new MyLocalBinder();

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return myBinder;
	}
	
	private static final String TAG = "Printerdroid";
	
	String currentConsole;
	
	UsbSerialDriver driver;
	
	private SerialInputOutputManager mSerialIoManager;
	
	private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();
	
	//--------------------------------------------------------
	//sending and recieving code
	//--------------------------------------------------------
	
	void updateReceivedData(byte[] data)
	{
		String dataString;
		try 
		{
			dataString = new String(data, "ASCII");
			Log.i(TAG, "First Data Char: " + String.format("%x", data[0]));
			consoleAdd(dataString);
		}
		catch (UnsupportedEncodingException e) 
		{
			e.printStackTrace();
		}
	}

	
    //this is used to do a quick comparision on what the printer sends back
    private final String badResponse = "rs";
    
    private Object sendingLock = new Object();
    
    private boolean waiting_for_response = false;;
    
    private String response;
    
    
    private void addResponse(byte[] data)
    {
    	try 
    	{
			response = response + new String(data, "ASCII");
		} 
    	catch (UnsupportedEncodingException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	//if we got a newline, then the current line is over
    	if(response.endsWith("\n"))
    	{
    		synchronized(sendingLock)
    		{
        		sendingLock.notify();
    		}
    	}
    }
    
    /**
     * sends the string, adding a newline if it doesn't have one.
     * 
     * this uses a synchronized block, so only one send can be processed at a time.
     * It will wait for a non-error responce from the printer before continuing
     * 
     * WARNING: this will add a newline to the source string it it doesn't end with one
     * 
     * @param string
     */
    public String send(String string)
    {
		try 
    	{
    		if(driver != null)
    		{
        		if(!string.endsWith("\n"))
        		{
        			string = string + "\n";
        		}
        		byte[] bytes = string.getBytes();
        		Log.i(TAG, "Sending bytes: " + bytesToHexString(bytes));
        		
        		response = new String();
        		
            	//we need this synchronized block so that we can wait for the responce that it was received successfully ("ok\n")
        		synchronized(sendingLock)
        		{
            		waiting_for_response = true;
            		//keep looping if we get rs, which means that there was a bad transmission
            		do
            		{
        				driver.write(bytes, 1000);
        				sendingLock.wait();
            		}
            		while(response == badResponse);
            		
            		waiting_for_response = false;
        		}
        		
        		Log.d(TAG, "Responce: " + response);
        		
        		return response;
        		
    		}
    		else
    		{
        		Log.w(TAG, "Trying to send data over uninitialized serial connection");
    		}
		} 
    	catch (IOException e) 
    	{
			e.printStackTrace();
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		
		return null;
    }
    
    public void onNewData(final byte[] data) 
    {
    	
    	if(waiting_for_response)
    	{
    		addResponse(data);
    	}
    	
    	else
    	{
    		updateReceivedData(data);
    	}
    	
    }
    
    
	
	private final SerialInputOutputManager.Listener mListener = new SerialInputOutputManager.Listener() 
	{

        @Override
        public void onRunError(Exception e) {
            Log.d(TAG, "Runner stopped.");
        }
        
        @Override
        public void onNewData(final byte[] data) 
        {
        	PrinterService.this.onNewData(data);
        }
    };
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
    	
		UsbManager manager = (UsbManager)getSystemService(Context.USB_SERVICE);
		driver = UsbSerialProber.acquire(manager);
		
    	currentConsole = "";
    	
    	instance = this;
    	
    	Log.d(TAG, "Resumed, sDriver=" + driver);
        if (driver == null) 
        {
            consoleAddLine("No serial device.");
        }
        
        else 
        {
            try 
            {
                driver.open();
                driver.setBaudRate(Settings.baudrate);
            }
            catch (IOException e) 
            {
                Log.e(TAG, "Error setting up device: " + e.getMessage(), e);
                consoleAddLine("Error opening device: " + e.getMessage());
                try 
                {
                    driver.close();
                }
                catch (IOException e2)
                {
                    // Ignore.
                }
                driver = null;
                return super.onStartCommand(intent,  flags,  startId);
            }
            
           consoleAddLine("Serial device: " + driver.getClass().getSimpleName());
        }
        
        onDeviceStateChange();
        
        Log.i(TAG, "Printerdroid Service Started");
        
        return super.onStartCommand(intent, flags, startId);

    }
    
    @Override
    public void onDestroy()
    {
    	stopIoManager();
        if (driver != null) 
        {
            try 
            {
                driver.close();
            } 
            catch (IOException e) 
            {
                // Ignore.
            }
            driver = null;
        }

    }
    
    ConsoleListener consoleListener;
    
    public void bindToConsole(ConsoleListener consoleListener)
    {
    	this.consoleListener = consoleListener;
    }
    
    


	/**
	 * call this function to add a string and then a newline to the console.
	 * @param toAdd the string to print
	 */
	void consoleAddLine(String toAdd)
	{
		currentConsole = currentConsole + toAdd + "\n";
		if(consoleListener != null)
		{
			consoleListener.onNewConsole();
		}
	}
	/**
	 * call this function to add a string to the console.
	 * @param toAdd the string to print
	 */	
	void consoleAdd(String toAdd)
	{
		currentConsole = currentConsole + toAdd;
		if(consoleListener != null)
		{
			consoleListener.onNewConsole();
		}
	}
	
	final protected static char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
	public static String bytesToHexString(byte[] bytes) 
	{
	    char[] hexChars = new char[bytes.length * 2];
	    int v;
	    for ( int j = 0; j < bytes.length; j++ ) 
	    {
	        v = bytes[j] & 0xFF;
	        hexChars[j * 2] = hexArray[v >>> 4];
	        hexChars[j * 2 + 1] = hexArray[v & 0x0F];
	    }
	    return new String(hexChars);
	}
	
    private void onDeviceStateChange() {
        stopIoManager();
        startIoManager();
    }
    
    private void stopIoManager() {
        if (mSerialIoManager != null) {
            Log.i(TAG, "Stopping io manager ..");
            mSerialIoManager.stop();
            mSerialIoManager = null;
        }
    }

    private void startIoManager() {
        if (driver != null) {
            Log.i(TAG, "Starting io manager ..");
            mSerialIoManager = new SerialInputOutputManager(driver, mListener);
            mExecutor.submit(mSerialIoManager);
        }
    }
    
	public class MyLocalBinder extends Binder {
        PrinterService getService() {
            return PrinterService.this;
     }
	}
	
	void clearConsole()
	{
		currentConsole = "";
		consoleListener.onNewConsole();
	}
	
	ProgressBoxManager progressDialog;
	
	PrintAsyncTask printAsyncTask;

	public void print(Uri currentFilePath, MainActivity mainActivity) throws FileNotFoundException 
	{
		File file = new File(currentFilePath.getPath());
		InputStream inputStream = new FileInputStream(file);
		LineReader lineReader = new LineReader(inputStream);
		PrintAsyncTask printAsyncTask = new PrintAsyncTask(mainActivity);
		printAsyncTask.execute(lineReader);
	}
	
	public void cancelPrint()
	{
		if(printAsyncTask != null)
		{
			printAsyncTask.cancel(false);
		}
	}


}
