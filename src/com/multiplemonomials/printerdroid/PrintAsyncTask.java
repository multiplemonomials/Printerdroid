package com.multiplemonomials.printerdroid;

import java.io.IOException;
import java.util.List;

import android.os.AsyncTask;

import com.multiplemonomials.androidutils.LineReader;
import com.multiplemonomials.androidutils.Pair;
import com.multiplemonomials.androidutils.progressbox.ProgressBoxManager;
import com.multiplemonomials.printerdroid.gcodeparser.Layer;
/**
 * Does the actual print, taking a LineReader initalized to the file to print
 * @author Jamie
 *
 */
public class PrintAsyncTask extends AsyncTask<LineReader, Pair<Integer, Integer>, Boolean>
{
	
	private MainActivity _mainActivity;

	public PrintAsyncTask(MainActivity mainActivity) 
	{
		_mainActivity = mainActivity;
	} 
	
	int layer = 1;
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
				if(line != null)
				{
					_mainActivity.myService.send(params[0].readNextLine());
					
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
				
				//now we need to wait for the response that we have the clearence to start again.
				
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		}
		return true;
	}
	
	@Override
	protected void onPreExecute()
	{
		Settings.isPrinting = true;
		_mainActivity.showLayerProgressBar();
	}
	
	void onProgressUpdate(Pair<Integer, Integer> progress)
	{
		_mainActivity.updateLayerProgressBar(progress);
	}
	
	@Override
	protected void onPostExecute(Boolean result)
	{
		Settings.isPrinting = false;
		_mainActivity.closeLayerProgressBar();
	}

}
