package com.vutbr.fit.tam.acceleroscroll;

import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TableLayout;
import android.widget.TextView;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.util.Log;


public class SeekBarPreference extends DialogPreference implements
		OnSeekBarChangeListener {
	private static final String TAG = "SeekBarPreference";
	private static final String androidns="http://schemas.android.com/apk/res/android";
	private static final String myns="http://schemas.android.com/apk/res/com.vutbr.fit.tam.acceleroscroll";
	
	private float mValue = 0.0f, mMax, mMin, mDefault;
	private TextView currVal;
	private SeekBar mSeekBar;
	
	
	
	public SeekBarPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		this.mDefault = attrs.getAttributeFloatValue(androidns, "defaultValue", 0);
		this.mMin = attrs.getAttributeFloatValue(myns, "intervalMin", 0.0f);
		this.mMax = attrs.getAttributeFloatValue(myns, "intervalMax", 10.0f);
	}
	
	@Override
	protected View onCreateDialogView(){
		LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		TableLayout layout = (TableLayout) inflater.inflate(R.layout.preferences_seekbar, null);
		mSeekBar = (SeekBar) layout.findViewById(R.id.seekbar);
		mSeekBar.setOnSeekBarChangeListener(this);
		mSeekBar.setMax(100);
		
		TextView min = (TextView) layout.findViewById(R.id.minText);
		min.setText(Float.toString(mMin));
		TextView max = (TextView) layout.findViewById(R.id.maxText);
		max.setText(Float.toString(mMax));
		
		if (shouldPersist())
		      mValue = getPersistedFloat(mDefault);

		currVal = (TextView) layout.findViewById(R.id.currText);
		currVal.setText(Float.toString(mValue));
		mSeekBar.setProgress(this.getProgressPercent());
		
		return layout;
	}
	
	private int getProgressPercent() {
		return (int) ((mValue-mMin)/(mMax-mMin)*100);
	}
	
	private float getValueFromPercent(int percent){
		return percent/100.0f*(mMax-mMin) + mMin;
	}
	
	@Override 
	protected void onBindDialogView(View v) {
	  super.onBindDialogView(v);
	  mSeekBar.setMax(100);
	  mSeekBar.setProgress(this.getProgressPercent());
	}
	
	@Override
	protected void onSetInitialValue(boolean restore, Object defaultValue)  
	{
	  super.onSetInitialValue(restore, defaultValue);
	  if (restore) 
	    mValue = shouldPersist() ? getPersistedFloat(mDefault) : 0;
	  else 
	    mValue = (Float)defaultValue;
	}


	public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
		currVal.setText(Float.toString(this.getValueFromPercent(arg1)));
	}

	public void onStartTrackingTouch(SeekBar seekBar) {
		//do nothing
	}

	public void onStopTrackingTouch(SeekBar seekBar) {
		mValue = this.getValueFromPercent(seekBar.getProgress());
		
	}
	
	@Override
	protected void onDialogClosed(boolean positiveResult){
		Log.v(TAG, "Dialog closed with result: " + positiveResult);
		if(positiveResult){
			if(shouldPersist()){
				persistFloat(mValue);
			}
			callChangeListener(new Float(mValue));	
		}
	}

	  public void setMax(float max) { mMax = max; }
	  public float getMax() { return mMax; }

	  public void setMin(float min) { mMin = min; }
	  public float getMin() { return mMin; }

	  public void setValue(float progress) { 
	    mValue = progress;
	    if (mSeekBar != null)
	      mSeekBar.setProgress(this.getProgressPercent()); 
	  }
	  public float getValue() { return mValue; }
	
}
