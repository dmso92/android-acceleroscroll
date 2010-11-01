package com.fit.accelerotest;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.widget.FrameLayout;


public class AcceleroTest extends Activity {
    /** Called when the activity is first created. */
	private Ball ball;
	private static Context CONTEXT;
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AcceleroTest.CONTEXT = this;
        setContentView(R.layout.main);
        FrameLayout main = (FrameLayout) findViewById(R.id.main_layout);
        this.ball = new Ball(this, 25);
        main.addView(this.ball);
    }
    
    public static Context getContext() {
        return CONTEXT;
    }
}