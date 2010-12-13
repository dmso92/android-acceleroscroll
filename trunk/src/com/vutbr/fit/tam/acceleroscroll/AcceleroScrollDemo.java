package com.vutbr.fit.tam.acceleroscroll;


import java.io.File;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.Toast;


public class AcceleroScrollDemo extends Activity {
	
	private static final String TAG = "AcceleroScrollDemo";
	private static final int REQUEST_IMAGE_BROWSER = 11;
    private static final int REQUEST_CODE_PREFERENCES = 1;
    private static final double inch = 25.4;
    private static final int TOP_WALL = 0;
    private static final int BOTTOM_WALL = 2;
    private static final int RIGHT_WALL = 1;
    private static final int LEFT_WALL = 3;
    
    private static boolean isTouch = false;
	
	private Bitmap scrollImage;
	private boolean isDefaultImage = true;
	private String currentImagePath;
	
	private int displayWidth;
	private int displayHeight;
	private float xDPI;
	private float yDPI;
	
	private int maxHTScroll = 0;
	private int maxHBScroll = 0;
	private int maxWRScroll = 0;
	private int maxWLScroll = 0;
	
	private int currentPosX = 0;
    private int currentPosY = 0;
    private int orientationRequest = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
      
        
        // Get the between instance stored values
        SharedPreferences imagePreferences = getPreferences(MODE_PRIVATE);
        isDefaultImage = imagePreferences.getBoolean("defaultImage", true);
        
    	currentPosX = imagePreferences.getInt("currentPosX", 0);
      	currentPosY = imagePreferences.getInt("currentPosY", 0);
      	orientationRequest = imagePreferences.getInt("orientationRequest", orientationRequest);
      	this.setRequestedOrientation(orientationRequest);
        
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN); 
        
        setContentView(R.layout.main);
    	
    	Display scrollDisplay = getWindowManager().getDefaultDisplay(); 
    	displayWidth = scrollDisplay.getWidth();
    	displayHeight = scrollDisplay.getHeight();
    	
    	DisplayMetrics metricsDPI = new DisplayMetrics();
    	scrollDisplay.getMetrics(metricsDPI);
    	xDPI = metricsDPI.xdpi;
    	yDPI = metricsDPI.ydpi;
        	
    	
    	if (isDefaultImage){
	    	scrollImage = BitmapFactory.decodeResource(getResources(), R.drawable.android);
	    	Log.v(TAG, "Loading deafult image.");
    	} else {
    		
    		currentImagePath = imagePreferences.getString("imagePath", "/mnt/sdcard/droid.png");
    		scrollImage = BitmapFactory.decodeFile(currentImagePath);
    	}
   	
    	ImageView scrollView = (ImageView) findViewById(R.id.scrollView);
	    scrollView.setOnTouchListener(imageTouchListener);
    	scrollView.setImageBitmap(scrollImage);
	    scrollView.scrollBy(currentPosX, currentPosY);
	    
    }
    
    /** Called when the activity is resumed. */
    @Override
    public void onResume(){
    	super.onResume();
    	if(!mIsBound){
    		startService(new Intent(this, AcceleroScrollService.class));
    		doBindService();
    	}
    	maxHBScroll = (int)((scrollImage.getHeight()/2) - (displayHeight /2));
	    maxWRScroll = (int)((scrollImage.getWidth()/2) - (displayWidth /2));
	    maxHTScroll = maxHBScroll * -1;
	    maxWLScroll = maxWRScroll * -1; 
    }
    
    /** Called when activit paused. */
    @Override
    public void onPause(){
    	super.onPause();
    	if(mIsBound) 
    		doUnbindService();
    	
    	 // Store values between instances here
    	SharedPreferences imagePreferences = getPreferences(MODE_PRIVATE);
    	SharedPreferences.Editor editorIP = imagePreferences.edit();
    	
    	editorIP.putBoolean("defaultImage", isDefaultImage);
    	editorIP.putString("imagePath", currentImagePath);
    	editorIP.putInt("currentPosX", currentPosX);
    	editorIP.putInt("currentPosY", currentPosY);
    	editorIP.putInt("orientationRequest", orientationRequest);
    	
    	editorIP.commit();
    	
    }
        
    /** Create options menu */
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
    	MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.demo_menu, menu);
    	return true;
    }
    
    @Override
    public boolean onPrepareOptionsMenu (Menu menu){
    	MenuItem itemSaved = menu.findItem(R.id.savedRot);
        MenuItem itemAutomatic = menu.findItem(R.id.automaticRot);
        boolean st = orientationRequest == ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
        itemSaved.setVisible(st);
        itemAutomatic.setVisible(!st);
    	return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item	){
    	switch(item.getItemId()){
    		case R.id.settings:
    			//When settings is selected, launch an activity through this intent
    			Intent launchPreferences = new Intent().setClass(this, AcceleroScrollPreferences.class);
    			
    			//Make it a subActivity
    			//startActivityForResult(launchPreferences, REQUEST_CODE_PREFERENCES);
    			startActivity(launchPreferences);
    			return true;
    		case R.id.imageLoader:
    			Intent imagesIntent = new Intent().setClass(this, AcceleroScrollImages.class);
                startActivityForResult(imagesIntent, REQUEST_IMAGE_BROWSER);
    			return true;
    		case R.id.resetImage:
    			Bitmap defaultImage = BitmapFactory.decodeResource(getResources(), R.drawable.android);
    	    	ImageView scrollView = (ImageView) findViewById(R.id.scrollView);
    	    	scrollView.scrollBy(currentPosX * (-1), currentPosY * (-1));
    	    	currentPosX = 0;
    	    	currentPosY = 0;
    	    	
    	    	scrollView.setImageBitmap(defaultImage);
    	    	
    	    	maxHBScroll = (int)((scrollImage.getHeight()/2) - (displayHeight /2));
    		    maxWRScroll = (int)((scrollImage.getWidth()/2) - (displayWidth /2));
    		    maxHTScroll = maxHBScroll * -1;
    		    maxWLScroll = maxWRScroll * -1;
    	    	
    	    	scrollImage = defaultImage;
    	    	isDefaultImage = true;
    	    	
    			return true;
    		case R.id.automaticRot:
    			//clicked on saved item, set automatic rotation
    			orientationRequest = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
    			this.setRequestedOrientation(orientationRequest);
    			return true;
    		case R.id.savedRot:
    			//clicked on automatic item -> save orientation
    			/* First, get the Display from the WindowManager */  
    	    	Display display = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();  
    	    	  
    	    	/* Now we can retrieve all display-related infos */  
    	    	int rot = display.getRotation();
    	    	
    	    	/*
    	    	 * Unfortunately api 8 doesn't support reverse portrait
    	    	 * and reverser landscape modes, so only simple portrait
    	    	 * and landscapes are supported
    	    	 */
    	    	switch(rot){
    	    	
    	    		case Surface.ROTATION_0:
    	    			orientationRequest = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
    	    			break;
    	    		case Surface.ROTATION_90:
    	    			orientationRequest = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
    	    			break;
    	    		case Surface.ROTATION_180:
    	    			orientationRequest = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
    	    			break;
    	    		case Surface.ROTATION_270:
    	    			orientationRequest = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
    	    			break;
    	    			
    	    	}
    	    	this.setRequestedOrientation(orientationRequest);
    			return true;
    		default:
    			return super.onOptionsItemSelected(item);
    	}
    }
    
    /** on preferences return */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
    	super.onActivityResult(requestCode, resultCode, data);

    	if (requestCode == REQUEST_IMAGE_BROWSER) {
    		if (resultCode == RESULT_OK) {
    			Bundle extras = data.getExtras();
    			String imagePath = extras.getString("imagePath");
    			
    			File imageFile = new File(imagePath);
    			if(imageFile.exists()){
    				Bitmap newImage = BitmapFactory.decodeFile(imagePath);
    				if (newImage != null){
    					ImageView myScrollView = (ImageView) findViewById(R.id.scrollView);
    					
    					maxHTScroll = (int)((newImage.getHeight()/2) - (displayHeight /2));
    			    	maxWRScroll = (int)((newImage.getWidth()/2) - (displayWidth /2));
    			    	maxHBScroll = maxHTScroll * -1;
    			    	maxWLScroll = maxWRScroll * -1;
    			    	
    			    	myScrollView.scrollBy(currentPosX * (-1), currentPosY * (-1));
    			    	
    					myScrollView.setImageBitmap(newImage);
    					myScrollView.setScaleType(ImageView.ScaleType.CENTER);
    					myScrollView.requestLayout();
    					currentPosX = 0;
    					currentPosY = 0;
    					
    					scrollImage = newImage;
    					currentImagePath = imagePath;
    					isDefaultImage = false;
    					
    				} else {
    					Log.w(TAG, "Error loading image");
    				}
    			} else {
    				//
    			}
    		} else {
    		}
    	}
    }
    
    OnTouchListener imageTouchListener = new OnTouchListener(){
        public boolean onTouch(View v, MotionEvent event) {
            int action = event.getAction();

            switch (action){
	            case MotionEvent.ACTION_DOWN:
	            	if (isTouch) {
	                	try {
	                		Message msg = Message.obtain(null, AcceleroScrollService.MSG_RESET_VALUE);
							mService.send(msg);
						} catch (RemoteException e) {
							// In this case the service has crashed before we could even
			                // do anything with it; we can count on soon being
			                // disconnected (and then reconnected if it can be restarted)
			                // so there is no need to do anything here.
						}
	            	}
	            	isTouch = !isTouch;
	            	break;
	
	            case MotionEvent.ACTION_UP:
	            	//isTouch = false;
	                break;
	            case MotionEvent.ACTION_CANCEL:
	            	//isTouch = false;
	                break;
            }
            return true;
        }

    };

    
    /** Messenger for communicating with service. */
    Messenger mService = null;
    /** Flag indicating whether we have called bind on the service. */
    boolean mIsBound;

    /**
     * Handler of incoming messages from service.
     */
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
        	if (!isTouch){
	            switch (msg.what) {
	                case AcceleroScrollService.MSG_UPDATE_VALUES:
	                    //do something with the received stuff
	                	Message wallHitV = null; //vertical message
	                	Message wallHitH = null; //horizontal message
	                	
	                	int scrollX = 0;
	                	int scrollY = 0;
	                	
	                	float[] movement = msg.getData().getFloatArray("updateMovement");
	                	float[] speed = msg.getData().getFloatArray("updateSpeed");
	                	//Log.v(TAG, "Recieved update from service: " + movement[0]+ " " + movement[1] + " [" + speed[0] + ", " + speed[1]+"].");
	                	
	                	int moveX = (int)((movement[0] / inch) * xDPI) * (-1);
	                	int moveY = (int)((movement[1] / inch) * yDPI) * (-1);
	                	//Log.v(TAG, "moveX: " + moveX + " moveY: " + moveY);
	                	
	                	int nextX = currentPosX + moveX;
	                	int nextY = currentPosY + moveY;
	                	
	                	if(currentPosX == maxWLScroll){
	                		wallHitH = Message.obtain(null, AcceleroScrollService.MSG_WALL_HIT, LEFT_WALL, 0);
	                	} else {
		                	if (moveX < 0) {
			                	if (nextX < maxWLScroll){ 
			                		scrollX = maxWLScroll - currentPosX;
			                		currentPosX = maxWLScroll;
			                	} else {
			                		currentPosX = nextX;
			                		scrollX = moveX;
			                	}
		                	}
	                	}
	                	
	                	if (currentPosX == maxWRScroll) {
	                		wallHitH = Message.obtain(null, AcceleroScrollService.MSG_WALL_HIT, RIGHT_WALL, 0);
	                	} else {
		                	if (moveX > 0) {
			                	if (nextX > maxWRScroll){ 
			                		scrollX = maxWRScroll - currentPosX;
			                		currentPosX = maxWRScroll;
			                	} else {
			                		currentPosX = nextX;
			                		scrollX = moveX;
			                	}
		                	}
	                	}
	                	
	                	if (currentPosY == maxHBScroll) {
	                		wallHitV = Message.obtain(null, AcceleroScrollService.MSG_WALL_HIT, BOTTOM_WALL, 0);
	                	} else {
		                	if (moveY > 0){
			                	if (nextY > maxHBScroll){ 
			                		scrollY = maxHBScroll - currentPosY;
			                		currentPosY = maxHBScroll;
			                	} else {
			                		currentPosY = nextY;
			                		scrollY = moveY;
			                	}
		                	}
	                	}
	                	
	                	if (currentPosY == maxHTScroll) {
	                		wallHitV = Message.obtain(null, AcceleroScrollService.MSG_WALL_HIT, TOP_WALL, 0);
	                	} else {
		                	if (moveY < 0){
			                	if (nextY < maxHTScroll){ 
			                		scrollY = maxHTScroll - currentPosY;
			                		currentPosY = maxHTScroll;
			                	} else {
			                		currentPosY = nextY;
			                		scrollY = moveY;
			                	}
		                	}
	                	}
	                	                	
                		try {
    	                	if (wallHitH != null){
    	                		mService.send(wallHitH);
    	                	} 
    	                	if (wallHitV != null){
    	                		mService.send(wallHitV);
    	                	}
						} catch (RemoteException e) {
							// In this case the service has crashed before we could even
			                // do anything with it; we can count on soon being
			                // disconnected (and then reconnected if it can be restarted)
			                // so there is no need to do anything here.
						}
	                	
	                	ImageView scrollViewH = (ImageView) findViewById(R.id.scrollView);
	                	scrollViewH.scrollBy(scrollX, scrollY);
	                	//Log.v(TAG, "scrollX: " + scrollX + " scrollY: " + scrollY);
	                	
	                	break;
	                default:
	                    super.handleMessage(msg);
	            }
        	}
        }
    }

    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    final Messenger mMessenger = new Messenger(new IncomingHandler());

    /**
     * Class for interacting with the main interface of the service.
     */
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className,
                IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service.  We are communicating with our
            // service through an IDL interface, so get a client-side
            // representation of that from the raw service object.
            mService = new Messenger(service);

            // We want to monitor the service for as long as we are
            // connected to it.
            try {
            	Message msg = Message.obtain(null,
            			AcceleroScrollService.MSG_RESET_VALUE);
            	mService.send(msg);
            	
                msg = Message.obtain(null,
                        AcceleroScrollService.MSG_REGISTER_CLIENT);
                msg.replyTo = mMessenger;
                mService.send(msg);
                Log.v(TAG, "sending register client");
                
            } catch (RemoteException e) {
                // In this case the service has crashed before we could even
                // do anything with it; we can count on soon being
                // disconnected (and then reconnected if it can be restarted)
                // so there is no need to do anything here.
            }

            // As part of the sample, tell the user what happened.
            Toast.makeText(AcceleroScrollDemo.this, R.string.background_service_connected,
                    Toast.LENGTH_SHORT).show();
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            mService = null;

            // As part of the sample, tell the user what happened.
            Toast.makeText(AcceleroScrollDemo.this, R.string.background_service_disconnected,
                    Toast.LENGTH_SHORT).show();
        }
    };

    void doBindService() {
        // Establish a connection with the service.  We use an explicit
        // class name because there is no reason to be able to let other
        // applications replace our component.
        bindService(new Intent(AcceleroScrollDemo.this, 
                AcceleroScrollService.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    void doUnbindService() {
        //if (mIsBound) {
            // If we have received the service, and hence registered with
            // it, then now is the time to unregister.
            if (mService != null) {
                try {
                    Message msg = Message.obtain(null,
                            AcceleroScrollService.MSG_UNREGISTER_CLIENT);
                    msg.replyTo = mMessenger;
                    mService.send(msg);
                } catch (RemoteException e) {
                    // There is nothing special we need to do if the service
                    // has crashed.
                }
            }

            // Detach our existing connection.
            unbindService(mConnection);
            mIsBound = false;
        //}
    }
    
}