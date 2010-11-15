package com.vutbr.fit.tam.acceleroscroll;

import android.content.Context;
import android.hardware.SensorManager;
import android.hardware.SensorListener;
import org.openintents.sensorsimulator.hardware.SensorManagerSimulator;


public class EmulatorSensorManager implements AcceleroSensorManagerInterface{
	private SensorManagerSimulator sensorManager;
	private AcceleroSensorListener listener;
	private boolean running = false;

	private int sensorDelay = SensorManager.SENSOR_DELAY_UI;

    /**
     * Returns true if the manager is listening to orientation changes
     */
    public boolean isListening() {
        return running;
    }
	
    public void stopListening() {
    	running = false;
    	try {
    		if (sensorManager != null && sensorEventListener != null) {
    			sensorManager.unregisterListener(sensorEventListener);
    			sensorManager.disconnectSimulator();
    		}
    	} catch (Exception e) {}
    }
	
	public void startListening(Context context,
			AcceleroSensorListener listener) {

		/**
		 * The ip and port should be set by SensorSimulatorSettings
		 * application
		 */
		sensorManager = SensorManagerSimulator.getSystemService(context, Context.SENSOR_SERVICE);
		sensorManager.connectSimulator();

		running = sensorManager.registerListener(sensorEventListener, SensorManager.SENSOR_ACCELEROMETER,
				sensorDelay);
		this.listener = listener;
	}

	public void setSensorDelay(int sensorDelay) {
		this.sensorDelay = sensorDelay;
	}

	private SensorListener sensorEventListener = 
		new SensorListener() {

		private long now = 0;
		private long timeDiff = 0;
		private long lastUpdate = 0;

		private float x = 0;
		private float y = 0;
		private float z = 0;

		public void onAccuracyChanged(int sensor, int accuracy) {}

		public void onSensorChanged(int sensor, float[] values) {
			// use the event timestamp as reference
			// so the manager precision won't depends 
			// on the AccelerometerListener implementation
			// processing time
			now = System.nanoTime();

			x = values[0];
			y = values[1];
			z = values[2];

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
