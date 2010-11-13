package com.vutbr.fit.tam.acceleroscroll;

import java.util.ArrayList;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.widget.Toast;
import android.util.Log;

public class AcceleroScrollService extends Service {
	
	/** Tag for logging. */
	private static final String TAG = "AcceleroScrollService";
	
	/** For showing and hiding our notification. */
    NotificationManager mNM;
    /** Keeps track of all current registered clients. */
    ArrayList<Messenger> mClients = new ArrayList<Messenger>();
    /** Holds last value set by a client. */
    int mValue = 0;

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
     * back with the given arg1 and data connected to the preference type
     */
    static final int MSG_GET_PREFERENCES_VALUE = 4;
    /**
     * Command to service to adjust preferences. Arg1 should define the preference type
     * and the based on the content a Bundle set with setData should be added.
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
     * Message to clients with the updated values.
     */
    static final int MSG_UPDATE_VALUES = 6;
    
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
                case MSG_SET_PREFERENCES_VALUE:
                    setPreferencesValue(msg.arg1, msg.getData());
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    /**
     * Add a client and optionally start the hardware listeners
     * @param msger the messenger client the callbacks should be sent to
     */
    private void addClient(Messenger msger){
    	if(mClients.size() == 0){
    		//if the first client start listening on accelerometer
    	}
        mClients.add(msger);
        Log.v(TAG, "Client added." + mClients.size());
    }
    
    private void removeClient(Messenger msger){
        mClients.remove(msger);
        if(mClients.size() == 0){
        	//if the last client stop listening on accelerometer
        }
        Log.v(TAG, "Client removed." + mClients.size());
    }
    
    private void removeClient(int index){
    	mClients.remove(index);
    	if(mClients.size() == 0){
    	}
        Log.v(TAG, "Client removed." + mClients.size());
    }
    
    private void setPreferencesValue(int type, Bundle data){
    	Log.v(TAG, "Preference value change request.");
    	switch(type){
    	case PREFERENCE_ACCELERATION:
    		//set accel pref
    		break;
    	case PREFERENCE_MAX_SCROLL_SPEED:
    		//set max scroll speed
    		break;
    	case PREFERENCE_SPRINGNESS:
    		//set springness
    		break;
    	case PREFERENCE_THRESHOLD:
    		//set threshold
    		break;
    	}
    }
    
    private void sendUpdateMessage(){
    	for (int i=mClients.size()-1; i>=0; i--) {
            try {
                mClients.get(i).send(Message.obtain(null,
                        MSG_UPDATE_VALUES, 1, 0));
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
        mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

        // Display a notification about us starting.
        showNotification();
    }

    @Override
    public void onDestroy() {
        // Cancel the persistent notification.
        mNM.cancel(R.string.background_service_started);

        // Tell the user we stopped.
        Toast.makeText(this, R.string.background_service_stopped, Toast.LENGTH_SHORT).show();
    }

    /**
     * When binding to the service, we return an interface to our messenger
     * for sending messages to the service.
     */
    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
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
