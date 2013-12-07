package com.multiplemonomials.printerdroid;

import com.multiplemonomials.printerdroid.R;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

public class ConsoleFragment extends Fragment implements ConsoleListener{
	
	protected static final String TAG = "Printerdroid-Console";

	TextView textView;
	
	EditText editText;
	
	
  @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) 
  	{
	  	View view = inflater.inflate(R.layout.console, container, false);
  		textView = (TextView)view.findViewById(R.id.console);
	  	textView.setGravity(Gravity.LEFT);
	  	editText = (EditText)view.findViewById(R.id.consoleInput);
	  	
	  	if(PrinterService.instance != null)
	  	{
	  		textView.setText(PrinterService.instance.currentConsole);
	  	}
        return view;
    }

	@Override
	public void onNewConsole() 
	{
		assert(textView != null);
		if(getActivity() != null)
		{
			getActivity().runOnUiThread(new Runnable()
			{

				@Override
				public void run() {
					textView.setText(PrinterService.instance.currentConsole);
					
				}
				
			});
		}
		
	}
}
