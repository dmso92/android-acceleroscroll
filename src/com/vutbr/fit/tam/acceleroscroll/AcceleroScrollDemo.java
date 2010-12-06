package com.vutbr.fit.tam.acceleroscroll;


import java.io.File;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;


public class AcceleroScrollDemo extends Activity {
	
	private static final String TAG = "AcceleroScrollDemo";
	private static final String TAG1 = "TAG1";
	private static final int REQUEST_IMAGE_BROWSER = 11;
    private static final int REQUEST_CODE_PREFERENCES = 1;
    private static final double inch = 25.4;
	
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
	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        /*  
        if (savedInstanceState != null){
        	Log.v(TAG1, "notnull");
	        isDefaultImage = savedInstanceState.getBoolean("defaultImage", true);
	     }
        */
        
        // Get the between instance stored values
        SharedPreferences imagePreferences = getPreferences(MODE_PRIVATE);
        isDefaultImage = imagePreferences.getBoolean("defaultImage", true);
        
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN); 
        
        setContentView(R.layout.main);
        
        startService(new Intent(this, AcceleroScrollService.class));
    	doBindService();
    	
    	Display scrollDisplay = getWindowManager().getDefaultDisplay(); 
    	displayWidth = scrollDisplay.getWidth();
    	displayHeight = scrollDisplay.getHeight();
    	
    	DisplayMetrics metricsDPI = new DisplayMetrics();
    	scrollDisplay.getMetrics(metricsDPI);
    	xDPI = metricsDPI.xdpi;
    	yDPI = metricsDPI.ydpi;
        	
    	
    	
    	/*Button prefButton = (Button) findViewById(R.id.prefButton);
    	prefButton.setWidth(displayWidth/2);
    	prefButton.setHeight(50);
        prefButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
            	Intent launchPreferences = new Intent(view.getContext(), AcceleroScrollPreferences.class);
    			startActivity(launchPreferences);
            }

        });
        
        Button imageButton = (Button) findViewById(R.id.imageButton);
        imageButton.setWidth(displayWidth/2);
    	imageButton.setHeight(50);
        imageButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent imagesIntent = new Intent(view.getContext(), AcceleroScrollImages.class);
                startActivityForResult(imagesIntent, REQUEST_IMAGE_BROWSER);
            }

        });*/
    	
    	if (isDefaultImage){
	    	scrollImage = BitmapFactory.decodeResource(getResources(), R.drawable.android);	    
	    	Log.v(TAG1, "isdefault");
    	} else {
    		
    		currentImagePath = imagePreferences.getString("imagePath", "/mnt/sdcard/droid.png");
    		scrollImage = BitmapFactory.decodeFile(currentImagePath);

	      	currentPosX = imagePreferences.getInt("currentPosX", 0);
	      	currentPosY = imagePreferences.getInt("currentPosY", 0);
    		
    		/*
    		currentImagePath = savedInstanceState.getString("imagePath");
    		scrollImage = BitmapFactory.decodeFile(currentImagePath);

	      	currentPosX = savedInstanceState.getInt("currentPosX");
	      	currentPosY = savedInstanceState.getInt("currentPosY");
	      	*/
	      	
	      	Log.v(TAG1, "okok");
    	}
    	
   	
    	ImageView scrollView = (ImageView) findViewById(R.id.scrollView);
    	scrollView.setImageBitmap(scrollImage);
    	
	    scrollView.scrollBy(currentPosX, currentPosY);
	    
	    maxHTScroll = (int)((scrollImage.getHeight()/2) - (displayHeight /2));
	    maxWRScroll = (int)((scrollImage.getWidth()/2) - (displayWidth /2));
	    maxHBScroll = maxHTScroll * -1;
	    maxWLScroll = maxWRScroll * -1;  
    	//Log.v(TAG, "viewHeight: " + displayHeight + " viewWidth: " + displayWidth + " maxHScroll: " + maxHScroll + " maxWScroll: " + maxWScroll);
    	
    }
    
    /** Called when the activity is resumed. */
    @Override
    public void onResume(){
    	super.onResume();
    	if(!mIsBound){
    		startService(new Intent(this, AcceleroScrollService.class));
    		ImageView scrollView = (ImageView) findViewById(R.id.scrollView);
        	scrollView.setImageBitmap(scrollImage);
    		doBindService();
    	}
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
    	    	scrollView.setImageBitmap(defaultImage);
    	    	
    	    	maxHTScroll = (int)((defaultImage.getHeight()/2) - (displayHeight /2));
    	    	maxWRScroll = (int)((defaultImage.getWidth()/2) - (displayWidth /2));
    	    	maxHBScroll = maxHTScroll * -1;
    	    	maxWLScroll = maxWRScroll * -1;
    	    	
    	    	scrollImage = defaultImage;
    	    	isDefaultImage = true;
    	    	
    			return true;
    		default:
    			return super.onOptionsItemSelected(item);
    	}
    }
    
    /** on preferences return */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
    	super.onActivityResult(requestCode, resultCode, data);
    	
    	if (requestCode == REQUEST_CODE_PREFERENCES) {
    		try {
	        	Message msg = Message.obtain(null,
	        			AcceleroScrollService.MSG_RESET_VALUE);
	        	mService.send(msg);
    		} catch (RemoteException e) {
    			doUnbindService();
    			doBindService();
    		}
    	}

    	if (requestCode == REQUEST_IMAGE_BROWSER) {
    		if (resultCode == RESULT_OK) {
    			Bundle extras = data.getExtras();
    			String imagePath = extras.getString("imagePath");
    			
    			Log.v(TAG1, imagePath);
    			
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
    					Log.v(TAG1, "bad image");
    				}
    			} else {
    				//
    			}
    		} else {
    			//Log.v(TAG1, "fakk");
    		}
    	}
    }
    
    /*
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
      // Save UI state changes to the savedInstanceState.
      // This bundle will be passed to onCreate if the process is
      // killed and restarted.
    	if(isDefaultImage){
    		savedInstanceState.putBoolean("defaultImage", true);	
    		Log.v(TAG1, "isdefaultimage");
    	} else {
    		Log.v(TAG1, currentImagePath + " " + currentPosX + " " + currentPosY);
    		savedInstanceState.putBoolean("defaultImage", false);
    		savedInstanceState.putString("imagePath", currentImagePath);
    		savedInstanceState.putInt("currentPosX", currentPosX);
    		savedInstanceState.putInt("currentPosY", currentPosY);
    	}
      
      super.onSaveInstanceState(savedInstanceState);
    }
    */
    
    
    /*
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
      super.onRestoreInstanceState(savedInstanceState);
      // Restore UI state from the savedInstanceState.
      // This bundle has also been passed to onCreate.
      if (!savedInstanceState.getBoolean("defaultImage")){
    	  Bitmap currentImage = BitmapFactory.decodeFile(savedInstanceState.getString("imagePath"));
    	  ImageView myScrollView = (ImageView) findViewById(R.id.scrollView);
			
    	  maxHTScroll = (int)((currentImage.getHeight()/2) - (displayHeight /2));
    	  maxWRScroll = (int)((currentImage.getWidth()/2) - (displayWidth /2));
    	  maxHBScroll = maxHTScroll * -1;
    	  maxWLScroll = maxWRScroll * -1;
    	  
    	  currentPosX = savedInstanceState.getInt("currentPosX");
    	  currentPosY = savedInstanceState.getInt("currentPosY");
    	  myScrollView.scrollBy(currentPosX, currentPosY);
	    	
    	  myScrollView.setImageBitmap(currentImage);
    	  myScrollView.setScaleType(ImageView.ScaleType.CENTER);
    	  myScrollView.requestLayout();
    	  
    	  scrollImage = currentImage;
      }
    }
    */
    
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
            switch (msg.what) {
                case AcceleroScrollService.MSG_UPDATE_VALUES:
                    //do something with the received stuff
                	int scrollX = 0;
                	int scrollY = 0;
                	
                	float[] movement = msg.getData().getFloatArray("updateMovement");
                	float[] speed = msg.getData().getFloatArray("updateSpeed");
                	Log.v(TAG, "Recieved update from service: " + movement[0]+ " " + movement[1] + " [" + speed[0] + ", " + speed[1]+"].");
                	
                	int moveX = (int)((movement[0] / inch) * xDPI) * (-1);
                	int moveY = (int)((movement[1] / inch) * yDPI);
                	Log.v(TAG, "moveX: " + moveX + " moveY: " + moveY);
                	
                	int nextX = currentPosX + moveX;
                	int nextY = currentPosY + moveY;
                	
                	if(moveX < 0 && currentPosX != maxWLScroll){
	                	if (nextX < maxWLScroll){ 
	                		scrollX = maxWLScroll - currentPosX;
	                		currentPosX = maxWLScroll;
	                	} else {
	                		currentPosX = nextX;
	                		scrollX = moveX;
	                	}
                	}
                	if (moveX > 0 && currentPosX != maxWRScroll){
	                	if (nextX > maxWRScroll){ 
	                		scrollX = maxWRScroll - currentPosX;
	                		currentPosX = maxWRScroll;
	                	} else {
	                		currentPosX = nextX;
	                		scrollX = moveX;
	                	}
                	}
                	
                	if (moveY < 0 && currentPosY != maxHBScroll){
	                	if (nextY < maxHBScroll){ 
	                		scrollY = maxHBScroll - currentPosY;
	                		currentPosY = maxHBScroll;
	                	} else {
	                		currentPosY = nextY;
	                		scrollY = moveY;
	                	}
                	}
                	if (moveY > 0 && currentPosY != maxHTScroll){
	                	if (nextY > maxHTScroll){ 
	                		scrollY = maxHTScroll - currentPosY;
	                		currentPosY = maxHTScroll;
	                	} else {
	                		currentPosY = nextY;
	                		scrollY = moveY;
	                	}
                	}
                	                	
                	ImageView scrollViewH = (ImageView) findViewById(R.id.scrollView);
                	scrollViewH.scrollBy(scrollX, scrollY);
                	Log.v(TAG, "scrollX: " + scrollX + " scrollY: " + scrollY);
                	
                	break;
                default:
                    super.handleMessage(msg);
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
            	
                // Give it some value as an example.
                Bundle msgBundle = new Bundle();
                msgBundle.putFloat("value", 2.0f);
                msg = Message.obtain(null,
                        AcceleroScrollService.MSG_SET_PREFERENCES_VALUE,
                        AcceleroScrollService.PREFERENCE_ACCELERATION, 0);
                msg.setData(msgBundle);
                mService.send(msg);

                msg = Message.obtain(null,
                        AcceleroScrollService.MSG_REGISTER_CLIENT);
                msg.replyTo = mMessenger;
                mService.send(msg);
                
                
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
        if (mIsBound) {
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
        }
    }
    
}