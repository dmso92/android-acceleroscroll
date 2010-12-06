package com.vutbr.fit.tam.acceleroscroll;

import java.util.List;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class AcceleroSensorManager implements AcceleroSensorManagerInterface{

	private Sensor sensor;
	private Sensor magSensor;
    private SensorManager sensorManager;
    // you could use an OrientationListener array instead
    // if you plans to use more than one listener
    private AcceleroSensorListener listener;
    private int sensorDelay = SensorManager.SENSOR_DELAY_UI;
    
 
    public void setSensorDelay(int sensorDelay) {
		this.sensorDelay = sensorDelay;
	}

	/** indicates whether or not Accelerometer Sensor is supported */
    private Boolean supported;
    /** indicates whether or not Accelerometer Sensor is running */
    private boolean running = false;
 
    /**
     * Returns true if the manager is listening to orientation changes
     */
    public boolean isListening() {
        return running;
    }
 
    /**
     * Unregisters listeners
     */
    public void stopListening() {
        running = false;
        try {
            if (sensorManager != null && sensorEventListener != null) {
                sensorManager.unregisterListener(sensorEventListener);
            }
            if (sensorManager != null && magSensorEventListener != null) {
            	sensorManager.unregisterListener(magSensorEventListener);
            }
        } catch (Exception e) {}
    }
 
    /**
     * Returns true if at least one Accelerometer sensor is available
     */
    public boolean isSupported(Context context) {
        if (supported == null) {
            if (context != null) {
                sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
                List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
                List<Sensor> sensors2 = sensorManager.getSensorList(Sensor.TYPE_MAGNETIC_FIELD);
                supported = new Boolean(sensors.size() > 0 && sensors2.size() > 0);
            } else {
                supported = Boolean.FALSE;
            }
        }
        return supported;
    }
 
    /**
     * Registers a listener and start listening
     * @param accelerometerListener
     *             callback for accelerometer events
     */
    public void startListening(
    		Context context,
            AcceleroSensorListener accelerometerListener) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
        List<Sensor> sensors2 = sensorManager.getSensorList(Sensor.TYPE_MAGNETIC_FIELD);
        if (sensors.size() > 0 && sensors2.size() > 0) {
            sensor = sensors.get(0);
            magSensor = sensors2.get(0);
            running = sensorManager.registerListener(
                    sensorEventListener, sensor, 
                    this.sensorDelay) &&
                    sensorManager.registerListener(magSensorEventListener, magSensor, this.sensorDelay);
            listener = accelerometerListener;
        }
    }
    
    private SensorEventListener magSensorEventListener =
        new SensorEventListener() {

        public void onAccuracyChanged(Sensor sensor, int accuracy) {    
        }

        public void onSensorChanged(SensorEvent event) {
                listener.onMagSensorChanged(event.values[0], event.values[1], event.values[2]);
        }
    };
    
    /**
     * The listener that listen to events from the accelerometer listener
     */
    private SensorEventListener sensorEventListener = 
        new SensorEventListener() {
 
        private long now = 0;
        private long timeDiff = 0;
        private long lastUpdate = 0;
 
        private float x = 0;
        private float y = 0;
        private float z = 0;
 
        public void onAccuracyChanged(Sensor sensor, int accuracy) {}
 
        public void onSensorChanged(SensorEvent event) {
            // use the event timestamp as reference
            // so the manager precision won't depends 
            // on the AccelerometerListener implementation
            // processing time
            now = event.timestamp;
 
            x = event.values[0];
            y = event.values[1];
            z = event.values[2];
 
            // if not interesting in shake events
            // just remove the whole if then else bloc
            if (lastUpdate == 0) {
                lastUpdate = now;
            } else {
                timeDiff = now - lastUpdate;
                if (timeDiff > 0) {
                    lastUpdate = now;
                    //trigger change event
                    listener.onAccelerationChanged(x, y, z, timeDiff);
                    return;
                }
            }
        }
 
    };
	
}
