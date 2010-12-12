package com.vutbr.fit.tam.acceleroscroll;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;

public class AcceleroScrollService extends Service {
	
	/** Tag for logging. */
	private static final String TAG = "AcceleroScrollService";
	private static final String PREFS_NAME = "AcceleroScrollServicePrefs";
	
	/** For showing and hiding our notification. */
    NotificationManager mNM;
    /** Keeps track of all current registered clients. */
    ArrayList<Messenger> mClients = new ArrayList<Messenger>();
    
    /**scrollmanager */
    private ScrollManager scrollManager = new ScrollManager();
    /**
     * Timer to send events to send event updates to clients
     */
    private Timer updateTimer;
    
    private float updateFPS = 15.0f;
    
    /**
     * *********************
     * Just for emulator should be removed from production
     * 
     */
    private boolean useEmulator = true;
    private AcceleroSensorManagerInterface sensorManager;
    

    /**
     * Command to the service to register a client, receiving callbacks
     * from the service.  The Message's replyTo field must be a Messenger of
     * the client where callbacks should be sent.
     */
    static final int MSG_REGISTER_CLIENT = 1;

    /**
     * Command to the service to unregister a client, to stop receiving callbacks
     * from the service.  The Message's replyTo field must be a Messenger of
     * the client as previously given with MSG_REGISTER_CLIENT.
     */
    static final int MSG_UNREGISTER_CLIENT = 2;

    /**
     * Command to service reset the speed, movement and the set the reference
     * accelerometer values to current state.
     */
    static final int MSG_RESET_VALUE = 3;
    
    /**
     * Command to get preferences value. Arg1 should be defined as id. A message will be sent
     * back with the given arg1 and data connected to the preference type.
     * The sender should set the replyTo parameter in message
     */
    static final int MSG_GET_PREFERENCES_VALUE = 4;
    /**
     * Command to service to adjust preferences. Arg1 should define the preference type
     * and the based on the content a Bundle set with setData should be added (with string "value")
     */
    static final int MSG_SET_PREFERENCES_VALUE = 5;
    
    
    /**
     * Threshold under which no movement should be detected.
     */
    static final int PREFERENCE_THRESHOLD = 0;
    /**
     * Maximum scroll speed in pixels/s
     */
    static final int PREFERENCE_MAX_SCROLL_SPEED = 1;
    /**
     * Acceleration of scrolling.
     */
    static final int PREFERENCE_ACCELERATION = 2;
    /**
     * Set springing capability. The movement is gradually stopping even after the mobile
     * phone was returned to original position
     */
    static final int PREFERENCE_SPRINGNESS = 3;
    /**
     * Set phone gui orientation. The bundle data should define orientation
     */
    static final int PREFERENCE_ORIENTATION = 4;
    /**
     * Set update message FPS. The bundla data "value" should containt required fps
     */
    static final int PREFERENCE_FPS = 5;
    /**
     * Set if orientation sensor should be used.
     * Default is phone based method
     */
    static final int PREFERENCE_USE_HAND_BASED = 6;
    /**
     * wall_bounce
     */
    static final int PREFERENCE_WALL_BOUNCE = 7;
    
    /**
     * Set
     */
    
    /**
     * send all preferences at once to preference activity, the 
     * key strings are lowercase values of the given preferences
     * for example max_scroll_speed
     */
    static final int PREFERENCE_ALL = 8;

    /**
     * Message to clients with the updated values.
     */
    static final int MSG_UPDATE_VALUES = 6;
    
    /**
     * Message when a wall was hit, and the scrolling should bounce away
     * The arg1 should define which wall was hit, top = 0, bottom=1, right=2, left=3
     */
    static final int MSG_WALL_HIT = 7;
    
    /**
     * Handler of incoming messages from clients.
     */
    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REGISTER_CLIENT:
                	addClient(msg.replyTo);
                	AcceleroScrollService.this.sendUpdateMessage();
                    break;
                case MSG_UNREGISTER_CLIENT:
                	removeClient(msg.replyTo);
                    break;
                case MSG_RESET_VALUE:
                	scrollManager.resetState();
                	break;
                case MSG_GET_PREFERENCES_VALUE:
                	getPreferencesValue(msg.arg1, msg.replyTo);
                	break;
                case MSG_SET_PREFERENCES_VALUE:
                    setPreferencesValue(msg.arg1, msg.getData());
                    break;
                case MSG_WALL_HIT:
                	scrollManager.hitWall(msg.arg1);
                default:
                    super.handleMessage(msg);
            }
        }
    }

    /**
     * Add a client and optionally start the hardware listeners
     * @param msger the messenger client the callbacks should be sent to
     */
    private synchronized void addClient(Messenger msger){
    	if(mClients.size() == 0){    		
    		//start the timer to add send update to clients
    		this.startTimer();
    		//if the first client start listening on accelerometer
    		sensorManager.startListening(this, scrollManager);
    	}
    	if(mClients.contains(msger)){
    		Log.w(TAG, "Client trying to reconnect without unregistering first.");
    		return;
    	}
    	/* First, get the Display from the WindowManager */  
    	Display display = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();  
    	  
    	/* Now we can retrieve all display-related infos */  
    	int orientation = display.getRotation();
    	scrollManager.setOrientation(orientation);
    	
        mClients.add(msger);
        Log.v(TAG, "Client added." + mClients.size());
    }
    
    private synchronized void startTimer(){
    	
    	int delay = SensorManager.SENSOR_DELAY_UI;
		if(this.updateFPS < 5) {
			delay = SensorManager.SENSOR_DELAY_NORMAL;
		} else if (this.updateFPS > 20) {
			delay = SensorManager.SENSOR_DELAY_GAME;
		}
		this.sensorManager.setRate(delay);
		
    	updateTimer = new Timer(true);
		updateTimer.scheduleAtFixedRate(new TimerTask() {
			
			@Override
			public void run() {
				AcceleroScrollService.this.sendUpdateMessage();
			}
		}, (long) 100, (long) (1000/this.updateFPS));
    }
    
    private synchronized void stopTimer(){
    	if(updateTimer != null) {
        	updateTimer.cancel();
        	updateTimer.purge();
        	updateTimer = null;
    	}
    }
    
    private synchronized void removeClient(Messenger msger){
        mClients.remove(msger);
        if(mClients.size() == 0){
        	//if the last client stop listening on accelerometer
        	sensorManager.stopListening();
        	this.stopTimer();
        }
        Log.v(TAG, "Client removed." + mClients.size());
    }
    
    private synchronized void removeClient(int index){
    	mClients.remove(index);
    	if(mClients.size() == 0){
    		sensorManager.stopListening();
    		this.stopTimer();
    	}
        Log.v(TAG, "Client removed." + mClients.size());
    }
    
    private void getPreferencesValue(int type, Messenger msger){
    	Log.v(TAG, "Preference value get request.");
    	Bundle data = new Bundle();
    	Message updateMessage = Message.obtain(null, MSG_GET_PREFERENCES_VALUE, 0, 0);
    	switch(type){
	    	case PREFERENCE_ACCELERATION:
	    		updateMessage.arg1 = PREFERENCE_ACCELERATION;
	    		data.putFloat("value", scrollManager.getAcceleration());
	    		break;
	    	case PREFERENCE_MAX_SCROLL_SPEED:
	    		updateMessage.arg1 = PREFERENCE_MAX_SCROLL_SPEED;
	    		data.putFloat("value", scrollManager.getMaxSpeed());
	    		break;
	    	case PREFERENCE_SPRINGNESS:
	    		updateMessage.arg1 = PREFERENCE_SPRINGNESS;
	    		data.putFloat("value", scrollManager.getSpringness());
	    		break;
	    	case PREFERENCE_THRESHOLD:
	    		updateMessage.arg1 = PREFERENCE_THRESHOLD;
	    		data.putFloat("value", scrollManager.getThreshold());
	    		break;
	    	case PREFERENCE_FPS:
	    		updateMessage.arg1 = PREFERENCE_FPS;
	    		data.putFloat("value", this.updateFPS);
	    		break;
	    	case PREFERENCE_WALL_BOUNCE:
	    		updateMessage.arg1 = PREFERENCE_WALL_BOUNCE;
	    		data.putFloat("value", scrollManager.getWallBounce());
	    		break;
	    	case PREFERENCE_USE_HAND_BASED:
	    		updateMessage.arg1 = PREFERENCE_USE_HAND_BASED;
	    		data.putBoolean("value", scrollManager.isUseHandBased());
	    		break;
	    	case PREFERENCE_ALL:
	    		updateMessage.arg1 = PREFERENCE_ALL;
	    		data.putFloat("acceleration", scrollManager.getAcceleration());
	    		data.putFloat("max_scroll_speed", scrollManager.getMaxSpeed());
	    		data.putFloat("springness", scrollManager.getSpringness());
	    		data.putFloat("fps", this.updateFPS);
	    		data.putFloat("threshold", scrollManager.getThreshold());
	    		data.putFloat("wall_bounce", scrollManager.getWallBounce());
	    		data.putBoolean("use_hand_based", scrollManager.isUseHandBased());
	    		break;
    		default:
    			return;
    	}

    	try {
    		updateMessage.setData(data);
    		msger.send(updateMessage);
    	} catch (RemoteException e) {
        	Log.w(TAG, "Client dissappeared without unreginstering.");
    	}
    }
    
    private void setPreferencesValue(int type, Bundle data){
    	Log.v(TAG, "Preference value change request." + data.getFloat("value"));

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
    	
    	switch(type){
    	case PREFERENCE_ACCELERATION:
    		scrollManager.setAcceleration(data.getFloat("value"));
    		editor.putFloat("acceleration", scrollManager.getAcceleration());
    		break;
    	case PREFERENCE_MAX_SCROLL_SPEED:
    		scrollManager.setMaxSpeed(data.getFloat("value"));
            editor.putFloat("max_scroll_speed", scrollManager.getMaxSpeed());
    		break;
    	case PREFERENCE_SPRINGNESS:
    		scrollManager.setSpringness(data.getFloat("value"));
            editor.putFloat("springness", scrollManager.getSpringness());
    		break;
    	case PREFERENCE_THRESHOLD:
    		scrollManager.setThreshold(data.getFloat("value"));
            editor.putFloat("threshold", scrollManager.getThreshold());
    		break;
    	case PREFERENCE_WALL_BOUNCE:
    		scrollManager.setWallBounce(data.getFloat("value"));
            editor.putFloat("wall_bounce", scrollManager.getWallBounce());
    		break;
    	case PREFERENCE_ORIENTATION:
    		scrollManager.setOrientation(data.getInt("value"));
    		break;
    	case PREFERENCE_USE_HAND_BASED:
    		scrollManager.setUseHandBased(data.getBoolean("value"));
    		editor.putBoolean("use_hand_based", scrollManager.isUseHandBased());
    	case PREFERENCE_FPS:
    		this.updateFPS = Math.max(data.getFloat("value"), 2.0f);
            editor.putFloat("fps", this.updateFPS);
    		this.stopTimer();
    		
    		this.startTimer();
    		break;
    	}
    	editor.commit();
    }
    
    private synchronized void sendUpdateMessage(){
    	for (int i=mClients.size()-1; i>=0; i--) {
            try {
            	float[] movement = new float[2];
            	float[] speed = new float[2];
            	//TODO remove after disconnecting emulator
            	scrollManager.getMovement(movement, useEmulator);
            	if(Math.max(Math.abs(movement[0]), Math.abs(movement[1])) < 1e-3){
            		return;
            	}
            	scrollManager.getSpeed(speed);
            	Bundle data = new Bundle();
            	data.putFloatArray("updateMovement", movement);
            	data.putFloatArray("updateSpeed", speed);
            	Message updateMessage = Message.obtain(null, MSG_UPDATE_VALUES, 0, 0);
            	updateMessage.setData(data);
                mClients.get(i).send(updateMessage);
            } catch (RemoteException e) {
                // The client is dead.  Remove it from the list;
                // we are going through the list from back to front
                // so this is safe to do inside the loop.
            	Log.w(TAG, "Client dissappeared without unreginstering.");
                this.removeClient(i);
            }
        }
    }
    
    /**
     * Target we publish for clients to send messages to IncomingHandler.
     */
    final Messenger mMessenger = new Messenger(new IncomingHandler());

    @Override
    public void onCreate() {
    	super.onCreate();
    	Log.v(TAG, "succesfully started.");
        mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        // Display a notification about us starting.
        showNotification();
        if(useEmulator) {
        	sensorManager = new EmulatorSensorManager();
        } else {
        	sensorManager = new AcceleroSensorManager();
        }
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        scrollManager.setAcceleration(settings.getFloat("acceleration", scrollManager.getAcceleration()));
        scrollManager.setMaxSpeed(settings.getFloat("max_scroll_speed", scrollManager.getMaxSpeed()));
        scrollManager.setSpringness(settings.getFloat("springness", scrollManager.getSpringness()));
        scrollManager.setThreshold(settings.getFloat("threshold", scrollManager.getThreshold()));
        scrollManager.setUseHandBased(settings.getBoolean("use_hand_based", false));
        scrollManager.setWallBounce(settings.getFloat("wall_bounce", scrollManager.getWallBounce()));
        this.updateFPS = settings.getFloat("fps", this.updateFPS);
    }

    @Override
    public void onDestroy() {
        // Cancel the persistent notification.
        mNM.cancel(R.string.background_service_started);

        // Tell the user we stopped.
        Toast.makeText(this, R.string.background_service_stopped, Toast.LENGTH_SHORT).show();
        sensorManager.stopListening();
    	this.stopTimer();
    }

    /**
     * When binding to the service, we return an interface to our messenger
     * for sending messages to the service.
     */
    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }
    
    @Override
    public boolean onUnbind(Intent intent){
    	super.onUnbind(intent);
    	sensorManager.stopListening();
    	this.stopTimer();
    	return false;
    }

    /**
     * Show a notification while this service is running.
     */
    private void showNotification() {
        // In this sample, we'll use the same text for the ticker and the expanded notification
        CharSequence text = getText(R.string.background_service_started);

        // Set the icon, scrolling text and timestamp
        Notification notification = new Notification(R.drawable.notification_icon, text,
                System.currentTimeMillis());

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, AcceleroScrollDemo.class), 0);

        // Set the info for the views that show in the notification panel.
        notification.setLatestEventInfo(this, getText(R.string.background_service_label),
                       text, contentIntent);

        // Send the notification.
        // We use a string id because it is a unique number.  We use it later to cancel.
        mNM.notify(R.string.background_service_started, notification);
    }

}
