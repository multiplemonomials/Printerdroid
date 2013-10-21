package com.multiplemonomials.printerdroid;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.HexDump;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.view.View;

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
	
	@Override
	public void onCreate()
	{
		super.onCreate();
	}
	
	private static final String TAG = "Printerdroid";
	
	String currentConsole;
	
	UsbSerialDriver driver;
	
	private SerialInputOutputManager mSerialIoManager;
	
	private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();
	
	private final SerialInputOutputManager.Listener mListener =
            new SerialInputOutputManager.Listener() {

        @Override
        public void onRunError(Exception e) {
            Log.d(TAG, "Runner stopped.");
        }

        @Override
        public void onNewData(final byte[] data) {
//            PrinterService.this.runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    
//                }
//            });
        	
        	PrinterService.this.updateReceivedData(data);
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
        if (driver == null) {
            consoleAddLine("No serial device.");
        } else {
            try {
                driver.open();
                driver.setBaudRate(9600);
            } catch (IOException e) {
                Log.e(TAG, "Error setting up device: " + e.getMessage(), e);
                consoleAddLine("Error opening device: " + e.getMessage());
                try {
                    driver.close();
                } catch (IOException e2) {
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
        if (driver != null) {
            try {
                driver.close();
            } catch (IOException e) {
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
    
    


	
	void consoleAddLine(String toAdd)
	{
		currentConsole = currentConsole + toAdd + "\n";
		if(consoleListener != null)
		{
			consoleListener.onNewConsole();
		}
	}
	
	void consoleAdd(String toAdd)
	{
		currentConsole = currentConsole + toAdd;
		if(consoleListener != null)
		{
			consoleListener.onNewConsole();
		}
	}
	
	void updateReceivedData(byte[] data)
	{
		String dataString;
		try {
			dataString = new String(data, "ASCII");
			//dataString = data.toString();
			Log.i(TAG, "First Data Char: " + String.format("%x", data[0]));
			consoleAdd(dataString);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	final protected static char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
	public static String bytesToHex(byte[] bytes) {
	    char[] hexChars = new char[bytes.length * 2];
	    int v;
	    for ( int j = 0; j < bytes.length; j++ ) {
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
    
    public void doSend(String string)
    {

        	try 
        	{
        		byte[] bytes = (string + "\n").getBytes();
        		Log.i(TAG, "Sending bytes: " + bytesToHex(bytes));
        		Log.i(TAG, "Dump: " + HexDump.dumpHexString(bytes));
				driver.write(bytes, 1000);
			} 
        	catch (IOException e) 
        	{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    }
    
    public void doConnect(View view)
    {
    	
		UsbManager manager = (UsbManager)getSystemService(Context.USB_SERVICE);
		driver = UsbSerialProber.acquire(manager);
		
		if(driver != null)
		{
			Log.i(TAG, "Device: " + driver.getDevice());
		}
		
		
    	if (driver == null) {
            consoleAddLine("No serial device.");
        } else {
            try {
                driver.open();
                driver.setBaudRate(9600);
            } catch (IOException e) {
                Log.e(TAG, "Error setting up device: " + e.getMessage(), e);
                consoleAddLine("Error opening device: " + e.getMessage());
                try {
                    driver.close();
                } catch (IOException e2) {
                    // Ignore.
                }
                driver = null;
                return;
            }
            consoleAddLine("Serial device: " + driver.getClass().getSimpleName());
        }
        onDeviceStateChange();
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


}
