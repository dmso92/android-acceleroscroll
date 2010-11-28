package com.vutbr.fit.tam.acceleroscroll;

import android.preference.Preference;
import android.util.AttributeSet;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TableLayout;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class SeekbarPereference extends Preference implements
		OnSeekBarChangeListener {

	//TODO fill the rest http://developerlife.com/tutorials/?p=303
	
	public SeekbarPereference(Context context) {
		super(context);
	}
	
	public SeekbarPereference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public SeekbarPereference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	@Override
	protected View onCreateView(ViewGroup parent){
		LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		TableLayout layout = (TableLayout) inflater.inflate(R.layout.preferences_seekbar, null);
		return layout;
	}



	public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
		//do nothing
	}

	public void onStartTrackingTouch(SeekBar seekBar) {
		//do nothing
	}

	public void onStopTrackingTouch(SeekBar seekBar) {
		notifyChanged();
	}

}
