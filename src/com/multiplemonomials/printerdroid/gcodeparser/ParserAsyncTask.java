package com.multiplemonomials.printerdroid.gcodeparser;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import com.multiplemonomials.androidutils.LineReader;
import com.multiplemonomials.printerdroid.MainActivity;

import android.os.AsyncTask;
import android.util.Log;

/**
 * Handles reading of the gcode file.
 * @author Jamie
 *
 */
public class ParserAsyncTask extends AsyncTask<LineReader, Integer, List<Layer>> {

	 private static final String TAG = "ParserAsyncTask";
	private MainActivity _mainActivity;

	 public ParserAsyncTask(MainActivity mainActivity) {
	        _mainActivity = mainActivity;
	 } 
	
	@Override
	protected List<Layer> doInBackground(LineReader... params) {
		return readFromFile(params[0]);
	}
	
	@Override
	protected void onPreExecute()
	{
		_mainActivity.showLayerProgressBar();
	}
	
	List<Layer> readFromFile(LineReader lineReader)
	{
		List<Layer> output = new LinkedList<Layer>();
		Layer layer;
		int progress = 1;
		do
		{
			layer = readLayer(lineReader);
			Log.i(TAG, "Layer " + progress);
			progress++;
			publishProgress(progress);
		}
		while(layer.isLast == false);
		return output;
	}
	
	private Layer readLayer(LineReader lineReader) {
		Layer layer = new Layer();
		while(true)
		{
			try 
			{
				String line = lineReader.readNextLine();
				if(line == null)
				{
					//reached end of file
					layer.isLast = true;
					return layer;
				}
				else if(line.startsWith("G1"))
				{
					G1 g1 = new G1(line);
					if(g1.z_value._variableValue != null)
					{
						layer.codes.add(g1);
						return layer;
					}
					layer.codes.add(g1);
					
				}
				else
				{
					layer.codes.add(new TextCode(line));
				}
			}
			catch (IOException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	void onProgressUpdate(int progress)
	{
		_mainActivity.updateLayerProgressBar(progress);
	}
	
	@Override
	protected void onPostExecute(List<Layer> result)
	{
		_mainActivity.closeLayerProgressBar();
	}

}
