package com.fit.accelerotest;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
 
public class Ball extends SurfaceView implements SurfaceHolder.Callback{
    private float x;
    private float y;
    private float r;
    private Paint mPaint = new Paint();
    private BackgroundThread _thread;
    private SpeedManager speedManager;
 
    public Ball(Context context, float r) {
        super(context);
        getHolder().addCallback(this);
        mPaint.setColor(0xFFFF0000);

    	int width = this.getWidth();
    	int height = this.getHeight();
        this.x = width/2;
        this.y = height/2;
        this.r = r;
        _thread = new BackgroundThread(this.getHolder(), this);
        speedManager = new SpeedManager(context);
        MySensorManager.startListening(speedManager);
    }
    
    class BackgroundThread extends Thread {
        private SurfaceHolder _surfaceHolder;
        private Ball _ball;
        private boolean _run = false;
 
        public BackgroundThread(SurfaceHolder surfaceHolder, Ball ball) {
            _surfaceHolder = surfaceHolder;
            _ball = ball;
        }
 
        public void setRunning(boolean run) {
            _run = run;
        }
 
        public SurfaceHolder getSurfaceHolder() {
            return _surfaceHolder;
        }
 
        @Override
        public void run() {
            Canvas c;
            while (_run) {
                c = null;
                try {
                    c = _surfaceHolder.lockCanvas(null);
                    synchronized (_surfaceHolder) {
                        _ball.onDraw(c);
                    }
                } finally {
                    // do this in a finally so that if an exception is thrown
                    // during the above, we don't leave the Surface in an
                    // inconsistent state
                    if (c != null) {
                        _surfaceHolder.unlockCanvasAndPost(c);
                    }
                }
            }
        }
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    	canvas.drawColor(0xFF000000);
    	mPaint.setColor(0xFFFFFFFF);
    	mPaint.setTextSize(16);
    	for(int i = 0; i<3; i++){
    		canvas.drawText(Float.toString(speedManager.phoneCoordAcceleration[i]), 10, (i+1)*16, mPaint);
    		canvas.drawText(Float.toString(speedManager.accelerationValues[i]), 200, (i+1)*16, mPaint);

    		canvas.drawText(Float.toString(speedManager.orientationAngles[i]/3.14f*180.0f), 10, (i+5)*16, mPaint);
    		canvas.drawText(Float.toString(speedManager.speed[i]*100), 200, (i+5)*16, mPaint); //speed in cm/s
    		
    		canvas.drawText(Float.toString(speedManager.infoArray[i]), 150, (i+10)*16, mPaint);
    	}
    	int height = this.getHeight();
    	int width = this.getWidth();
        this.y += speedManager.speed[1]*100;
        this.x += speedManager.speed[0]*100;
        this.r += speedManager.speed[2]*50;
        if(this.y > height){
        	this.y  = 0.0f;
        } else if(this.y < 0){
        	this.y = height;
        }
        if(this.x > width){
        	this.x = 0.0f;
        } else if(this.x < 0) {
        	this.x = width;
        }
        if(this.r < 0){
        	this.r = 100;
        } else if (this.r > 100){
        	this.r = 1.0f;
        }
        mPaint.setColor(0xFFFF0000);
        canvas.drawCircle(x, y, r, mPaint);
    }

	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// TODO Auto-generated method stub
		
	}

	public void surfaceCreated(SurfaceHolder holder) {
		this._thread.setRunning(true);
		this._thread.start();
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		boolean retry = true;
        _thread.setRunning(false);
        while (retry) {
            try {
                _thread.join();
                retry = false;
            } catch (InterruptedException e) {
                // we will try it again and again...
            }
        }
	}

}
