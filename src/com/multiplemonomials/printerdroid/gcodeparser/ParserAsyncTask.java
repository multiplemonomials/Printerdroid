package com.multiplemonomials.printerdroid.gcodeparser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.multiplemonomials.androidutils.LineReader;
import com.multiplemonomials.androidutils.progressbox.*;

import android.os.AsyncTask;
import android.util.Log;

/**
 * Handles reading of the gcode file.
 * @author Jamie
 *
 */
public class ParserAsyncTask extends AsyncTask<LineReader, Integer, List<Layer>> 
{

	private static final String TAG = "ParserAsyncTask";
	private ProgressBoxManager _progressBoxManager;

	public ParserAsyncTask(ProgressBoxManager progressBoxManager) 
	{
		_progressBoxManager = progressBoxManager;
	} 
	
	@Override
	protected List<Layer> doInBackground(LineReader... params) {
		return readFromFile(params[0]);
	}
	
	@Override
	protected void onPreExecute()
	{
		_progressBoxManager.showLayerProgressBar("Parsing File...", "Parsing Layer 1...");
	}
	
	List<Layer> readFromFile(LineReader lineReader)
	{
		List<Layer> output = new ArrayList<Layer>();
		Layer layer;
		int progress = 0;
		do
		{
			layer = readLayer(lineReader);
			Log.i(TAG, "Layer " + progress);
			progress++;
			publishProgress(progress);
			output.add(layer);
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
		_progressBoxManager.updateLayerProgressBar(progress);
	}
	
	@Override
	protected void onPostExecute(List<Layer> result)
	{
		_progressBoxManager.closeLayerProgressBar();
	}

}
