package com.vutbr.fit.tam.acceleroscroll;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.widget.Toast;

public class AcceleroScrollPreferences extends PreferenceActivity 
	implements OnPreferenceChangeListener
{
	/** Tag for logging. */
	private static final String TAG = "AcceleroScrollPreferences";
	 
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.acceleroscroll_preferences);
        
        SeekBarPreference seek;
        
        seek = (SeekBarPreference) this.findPreference("springness");
        seek.setOnPreferenceChangeListener(this);
        seek = (SeekBarPreference) this.findPreference("maxspeed");
        seek.setOnPreferenceChangeListener(this);
        seek = (SeekBarPreference) this.findPreference("acceleration");
        seek.setOnPreferenceChangeListener(this);
        seek = (SeekBarPreference) this.findPreference("threshold");
        seek.setOnPreferenceChangeListener(this);
        Log.v(TAG, "Setting change listeners");
        
        CheckBoxPreference use_orient = (CheckBoxPreference) this.findPreference("use_orientation");
        use_orient.setOnPreferenceChangeListener(this);
        
        startService(new Intent(this, AcceleroScrollService.class));
    	doBindService();
    }

	public boolean onPreferenceChange(Preference preference, Object newValue) {
		String key = preference.getKey();
		if(key.equals("springness")) {
			this.sendFloat(AcceleroScrollService.PREFERENCE_SPRINGNESS, newValue);
		} else if(key.equals("acceleration")) {
			this.sendFloat(AcceleroScrollService.PREFERENCE_ACCELERATION, newValue);
		} else if(key.equals("maxspeed")) {
			this.sendFloat(AcceleroScrollService.PREFERENCE_MAX_SCROLL_SPEED, newValue);
		} else if(key.equals("threshold")) {
			this.sendFloat(AcceleroScrollService.PREFERENCE_THRESHOLD, newValue);
		} else if(key.equals("use_orientation")) {
			this.sendBoolean(AcceleroScrollService.PREFERENCE_USE_HAND_BASED, newValue);
		}
		return true;
	}
	
	private void sendBoolean(int type, Object newValue){
		Bundle msgBundle = new Bundle();
		msgBundle.putBoolean("value", (Boolean) newValue);
		sendValue(type, msgBundle);
	}
	
	private void sendFloat(int type, Object newValue){
		Bundle msgBundle = new Bundle();
		msgBundle.putFloat("value", (Float) newValue);
		sendValue(type, msgBundle);
	}
	
	//send the new value to the service
	private void sendValue(int type, Bundle data){
		Log.v(TAG, "Sending changed value to service");
		if(mIsBound){
			try {
				Message msg = Message.obtain(null,
				        AcceleroScrollService.MSG_SET_PREFERENCES_VALUE,
				        type, 0);
				msg.setData(data);
				mService.send(msg);
			} catch (RemoteException e) {
				Toast.makeText(AcceleroScrollPreferences.this, R.string.preferences_set_error,
	                    Toast.LENGTH_SHORT).show();
			}
		}
	}
	
	/** Called when the activity is resumed. */
    @Override
    public void onResume(){
    	super.onResume();
    	if(!mIsBound){
    		startService(new Intent(this, AcceleroScrollService.class));
    		doBindService();
    	}
    }
    
    /** Called when activit paused. */
    @Override
    public void onPause(){
    	super.onPause();
    	if(mIsBound) 
    		doUnbindService();
    }
    
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
        	Log.v(TAG, "Incoming message from service");
            switch (msg.what) {
            	case AcceleroScrollService.MSG_GET_PREFERENCES_VALUE:
            		if(msg.arg1 != AcceleroScrollService.PREFERENCE_ALL){
            			return;
            		}
            		Bundle data = msg.getData();
            		SeekBarPreference seek;
                    
                    seek = (SeekBarPreference) AcceleroScrollPreferences.this.findPreference("springness");
                    seek.setValue(data.getFloat("springness"));
                    seek = (SeekBarPreference) AcceleroScrollPreferences.this.findPreference("maxspeed");
                    seek.setValue(data.getFloat("max_scroll_speed"));
                    seek = (SeekBarPreference) AcceleroScrollPreferences.this.findPreference("acceleration");
                    seek.setValue(data.getFloat("acceleration"));
                    seek = (SeekBarPreference) AcceleroScrollPreferences.this.findPreference("threshold");
                    seek.setValue(data.getFloat("threshold"));
                    
                    CheckBoxPreference use_orient = (CheckBoxPreference) AcceleroScrollPreferences.this.findPreference("use_orientation");
                    use_orient.setChecked(data.getBoolean("use_hand_based"));
                    
                    Log.v(TAG, "Got current values from service.");
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
                // Give it some value as an example.
                Message msg = Message.obtain(null,
                        AcceleroScrollService.MSG_GET_PREFERENCES_VALUE,
                        AcceleroScrollService.PREFERENCE_ALL, 0);
                msg.replyTo = mMessenger;
                mService.send(msg);
                Log.v(TAG, "Requesting default values from service");
                
            } catch (RemoteException e) {
                // In this case the service has crashed before we could even
                // do anything with it; we can count on soon being
                // disconnected (and then reconnected if it can be restarted)
                // so there is no need to do anything here.
            }

            // As part of the sample, tell the user what happened.
            Toast.makeText(AcceleroScrollPreferences.this, R.string.background_service_connected,
                    Toast.LENGTH_SHORT).show();
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            mService = null;

            // As part of the sample, tell the user what happened.
            Toast.makeText(AcceleroScrollPreferences.this, R.string.background_service_disconnected,
                    Toast.LENGTH_SHORT).show();
        }
    };

    void doBindService() {
        // Establish a connection with the service.  We use an explicit
        // class name because there is no reason to be able to let other
        // applications replace our component.
        bindService(new Intent(AcceleroScrollPreferences.this, 
                AcceleroScrollService.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
    }

    void doUnbindService() {
        if (mIsBound) {
            // Detach our existing connection.
            unbindService(mConnection);
            mIsBound = false;
        }
    }
	
}
