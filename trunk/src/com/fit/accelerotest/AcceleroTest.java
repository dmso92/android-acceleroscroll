package com.fit.accelerotest;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.widget.FrameLayout;


public class AcceleroTest extends Activity {
    /** Called when the activity is first created. */
	private Ball ball;
	private static Context CONTEXT;

    public SpeedManager speedManager;
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AcceleroTest.CONTEXT = this;
        setContentView(R.layout.main);
        FrameLayout main = (FrameLayout) findViewById(R.id.main_layout);

        speedManager = new SpeedManager(this);
        this.ball = new Ball(this, speedManager, 25);
        main.addView(this.ball);
    }
    
    @Override
    public void onResume(){
    	super.onResume();
    	if(!MySensorManager.isListening()){
    		MySensorManager.startListening(speedManager);
    	}
    }
    
    @Override
    public void onPause(){
    	super.onPause();
    	MySensorManager.stopListening();
    }
    
    public static Context getContext() {
        return CONTEXT;
    }
}