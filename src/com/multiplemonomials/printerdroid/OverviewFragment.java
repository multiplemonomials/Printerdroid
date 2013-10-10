package com.multiplemonomials.printerdroid;

import com.multiplemonomials.printerdroid.R;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class OverviewFragment extends Fragment {
	
	//BindToButton bindToButton;

	/** Called when the activity is first created. */
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.overview, container, false);
		//bindToButton = new BindToButton(getActivity());
		//Button loadFileButton = (Button) view.findViewById(R.id.loadfilebutton);
		//bindToButton.bind(loadFileButton, "onClickLoadFile");
		return view;
	}
	

}
