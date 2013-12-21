package com.multiplemonomials.printerdroid;

import java.io.IOException;
import android.os.AsyncTask;
import android.util.Log;

import com.multiplemonomials.androidutils.LineReader;
import com.multiplemonomials.androidutils.Pair;
/**
 * Does the actual print, taking a LineReader initalized to the file to print
 * @author Jamie
 *
 */
public class PrintAsyncTask extends AsyncTask<LineReader, Pair<Integer, Integer>, Boolean>
{
	
	private static final String TAG = "PrintAsyncTask";
	private MainActivity _mainActivity;

	public PrintAsyncTask(MainActivity mainActivity) 
	{
		_mainActivity = mainActivity;
	} 
	
	int layer = 1;
	@SuppressWarnings("unchecked")
	@Override
	protected Boolean doInBackground(LineReader... params) 
	{
		while(true)
		{
			if(isCancelled())
			{
				return false;
			}
			
			try 
			{
				String line = params[0].readNextLine();
				if(line != null && line.matches("(\\s*);(.*)")) //make sure it's not an EOF or a comment
				{
					while(PrinterService.instance.sendingQueue.size() >= 10)
					{
						//keep waiting until there's space in the print queue
						Thread.sleep(250);
					}
					
					Log.d(TAG, "Queing line " + line);
					
					if(_mainActivity == null || _mainActivity.myService == null)
					{
						return false;
					}
					
					_mainActivity.myService.send(line);
					
					//are we starting a new layer?
					if(line.contains("Z") && line.contains("G1"))
					{
						++layer;
						publishProgress(new Pair<Integer, Integer>(layer, params[0]._currentLine));
					}
				}
				else
				{
					break;
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
		}
		return true;
	}
	
	@Override
	protected void onPreExecute()
	{
		_mainActivity.myService.isPrinting = true;
		_mainActivity.showLayerProgressBar();
	}
	
	void onProgressUpdate(Pair<Integer, Integer> progress)
	{
		_mainActivity.updateLayerProgressBar(progress);
	}
	
	@Override
	protected void onPostExecute(Boolean result)
	{
		_mainActivity.myService.isPrinting = false;
		_mainActivity.closeLayerProgressBar();
	}

}
