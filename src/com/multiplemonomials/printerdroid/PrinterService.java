package com.multiplemonomials.printerdroid;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialProber;
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
	
	private BlockingDeque<String> sendingQueue;
    
    ConsoleListener consoleListener;
    
	ProgressBoxManager progressDialog;
	
	PrintAsyncTask printAsyncTask;
	
	private String response;
	
	private Object sendingLock = new Object();
	
	private Thread senderThread;
	
	public final static String temperatureCommandRegex =  "ok [Tt]:\\d+ [Bb]:\\d+(\\r\\n)*";

    
	//--------------------------------------------------------
	// Initialization & Destruction
	//--------------------------------------------------------
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
    	sendingQueue = new LinkedBlockingDeque<String>();    	
		
    	currentConsole = "";
    	
    	instance = this;
    	
    	retryConnection();
        
        Log.i(TAG, "Printerdroid Service Started");
        
        startSenderThread();
        
        return super.onStartCommand(intent, flags, startId);

    }
    
    public void retryConnection()
    {
    	UsbManager manager = (UsbManager)getSystemService(Context.USB_SERVICE);
		driver = UsbSerialProber.acquire(manager);
		
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
                return;
            }
            
           consoleAddLine("Serial device: " + driver.getClass().getSimpleName());
        }
        
        
        onDeviceStateChange();
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
    
	//--------------------------------------------------------
	// Service binding
	//--------------------------------------------------------
    
	public class MyLocalBinder extends Binder {
        PrinterService getService() {
            return PrinterService.this;
     }
	}
	
	//--------------------------------------------------------
	// Rx code
	//--------------------------------------------------------
	
	void updateReceivedData(byte[] data)
	{
		
		try 
		{
			response = response + new String(data, "ASCII");
		}
		catch (UnsupportedEncodingException e) 
		{
			e.printStackTrace();
			return;
		}
		
    	//if we got a newline, then the current line is over
    	if(response.endsWith("\n"))
    	{
    		if(response.contains("ok"))
    		{
    			//tell the queue manager service to send another one
        		synchronized(sendingLock)
        		{
        			sendingLock.notify();
        		}
    		}
    		
    		consoleAdd(response);
    		response = new String();
    	}
	}
    
    public void onNewData(final byte[] data) 
    {
		updateReceivedData(data);
    }
    
	//--------------------------------------------------------
	// Public Tx code
	//--------------------------------------------------------
    
    /**
     * sends the string to the printer.
     * 
     * Inserts it into a BlockingDeque of commands to send, so it might not get sent immediately
     * Uses the source string at an unknown time in the future, and will 
     * add a newline to the source string it it doesn't end with one.
     * 
     * @param string
     */
    
    public void sendNoCopy(String string)
    {
		sendingQueue.push(string);
    }
    
    /**
     * sends the string to the printer.
     * 
     * Inserts it into a BlockingDeque of commands to send, so it might not get sent immediately
     * 
     * Makes a copy of the source string, so you can modify it after
     * 
     * @param string
     */
    
    public void send(String string)
    {
    	String copiedString = new String(string);
		sendingQueue.addLast(copiedString);
    }
    
     /**
      * restarts the sending thread and deletes everything in the sending queue
      */
    public void rebootQueue()
    {
    	Log.d(TAG, "Rebooting Queue...");
    	
    	//delete everything in the sending queue
    	sendingQueue.clear();
    	
    	//tell the sending thread to shut down
    	//will also unstick it if it is waiting for a response
    	senderThread.interrupt();
    	
    	//give it time to shut down
    	while(senderThread.isAlive())
    	{
    		try 
    		{
				Thread.sleep(10);
			}
    		catch (InterruptedException e) 
    		{
				e.printStackTrace();
			}
    	}
    	
    	//start up the sender thread
    	startSenderThread();
    }
    
	//--------------------------------------------------------
	// private Tx backend
	//--------------------------------------------------------
    
    void startSenderThread()
    {
        senderThread = new Thread(new Runnable()
        {

    		@Override
    		public void run() 
    		{
    			
    			Log.i(TAG, "senderThread starting...");
    			
    			try 
    			{
    				while(!Thread.interrupted())
    				{
    					String string = PrinterService.this.sendingQueue.take();
    					
    					PrinterService.this.sendImpl(string);
    					
    					synchronized(sendingLock)
    					{
        					//wait for the printer to respond
        					PrinterService.this.sendingLock.wait();
    					}
    				}
    			} 
    			catch (InterruptedException e) 
    			{
    				e.printStackTrace();
    			}
    		}
        	
        });
        
        senderThread.start();
    }
    
    private void sendImpl(String string)
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
        		

				driver.write(bytes, 1000);
        		
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
    }
    
	//--------------------------------------------------------
	// Serial library stuff
	//--------------------------------------------------------
	
	private final SerialInputOutputManager.Listener mListener = new SerialInputOutputManager.Listener() 
	{

        @Override
        public void onRunError(Exception e) 
        {
            Log.d(TAG, "Runner stopped.");
        }
        
        @Override
        public void onNewData(final byte[] data) 
        {
        	PrinterService.this.onNewData(data);
        }
    };
    
    public void bindToConsole(ConsoleListener consoleListener)
    {
    	this.consoleListener = consoleListener;
    }
    
	final protected static char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
	
	public static String bytesToHexString(byte[] bytes) 
	{
	    char[] hexChars = new char[bytes.length * 2];
	    int v;
	    
	    for (int j = 0; j < bytes.length; j++) 
	    {
	        v = bytes[j] & 0xFF;
	        hexChars[j * 2] = hexArray[v >>> 4];
	        hexChars[j * 2 + 1] = hexArray[v & 0x0F];
	    }
	    
	    return new String(hexChars);
	}
	
    private void onDeviceStateChange()
    {
        stopIoManager();
        startIoManager();
    }
    
    private void stopIoManager() 
    {
        if (mSerialIoManager != null) 
        {
            Log.i(TAG, "Stopping io manager ..");
            
            mSerialIoManager.stop();
            mSerialIoManager = null;
        }
    }

    private void startIoManager() 
    {
        if (driver != null) 
        {
            Log.i(TAG, "Starting io manager ..");
            mSerialIoManager = new SerialInputOutputManager(driver, mListener);
            mExecutor.submit(mSerialIoManager);
        }
    }

	//--------------------------------------------------------
	// Console management
	//--------------------------------------------------------

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
		//check if it's an incoming temperature command
		if(toAdd.matches(temperatureCommandRegex))
		{
			consoleListener.onBedTemperature(toAdd);
		}
		else
		{
			currentConsole = currentConsole + toAdd;
			if(consoleListener != null)
			{
				consoleListener.onNewConsole();
			}
		}
	}
	
	void clearConsole()
	{
		currentConsole = "";
		consoleListener.onNewConsole();
	}

	//--------------------------------------------------------
	// Printing code
	//--------------------------------------------------------
	
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
