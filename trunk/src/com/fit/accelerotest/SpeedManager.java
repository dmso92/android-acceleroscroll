package com.fit.accelerotest;

import android.content.Context;
import android.hardware.SensorManager;
import android.opengl.Matrix;

public class SpeedManager implements MySensorListener{
	
	public float[] movement = new float[3];
	public float[] speed = new float[3];
	public float[] magSensorValues = new float[3];
	public float[] orientationAngles = new float[3];
	public float[] phoneCoordAcceleration = new float[4];
	public float[] accelerationValues = new float[3];
	public float[] infoArray = new float[3];
	private boolean on_start = true;
	static final float THRESHOLD = 0.5f;
	static final int HISTORY_SIZE = 30;
	public float[] accelerationHistory = new float[90];
	public int historyIndex = 0;
	static final int SHORT_HISTORY = 3;
	public float[] accelerationShortHistory = new float[9]; //SHORT_HISTORY*3
	public int shortHistoryIndex = 0;
	
	
	SpeedManager(Context context){
		this.speed = new float[3];
		this.magSensorValues = new float[3];
		for(int i=0; i<3; i++){
			this.speed[i] = 0.0f;
			this.magSensorValues[i] = 0.0f;
			this.orientationAngles[i] = 0.0f;
		}
	}
	
	public void getMovement(float[] outMovement){
		for(int i = 0; i<3; i++){
			outMovement[i] = this.movement[i];
			this.movement[i] = 0.0f;
		}
	}
	
	private float vectorSize(float[] vector){
		return (float) Math.sqrt(vector[0]*vector[0]+vector[1]*vector[1]+vector[2]*vector[2]);
	}

	public void onAccelerationChanged(float x, float y, float z) {}

	public void onAccelerationChanged(float x, float y, float z, float timeDiff) {
		float[] acceleroValues = {x, y, z};
		
		if (on_start){
			for(int i = 0; i<3; i++){
				accelerationHistory[historyIndex*3+i] = acceleroValues[i];
				accelerationShortHistory[shortHistoryIndex*3+i] = acceleroValues[i];
			}
			
			historyIndex++;
			shortHistoryIndex = (shortHistoryIndex+1) % SHORT_HISTORY;
			
			if(historyIndex == HISTORY_SIZE){
				historyIndex = 0;
				on_start = false;
			}
			return;
		}
		//store and count the short average

		float currentAcceleroValues[] = {x, y, z};
		//get the current average
		//Average is necessary because of the noise
		for(int i = 0; i<SHORT_HISTORY; i++){
			currentAcceleroValues[0] += accelerationShortHistory[i*3];
			currentAcceleroValues[1] += accelerationShortHistory[i*3+1];
			currentAcceleroValues[2] += accelerationShortHistory[i*3+2];
		}
		currentAcceleroValues[0] /= SHORT_HISTORY+1;
		currentAcceleroValues[1] /= SHORT_HISTORY+1;
		currentAcceleroValues[2] /= SHORT_HISTORY+1;
		float shortAvg = this.vectorSize(currentAcceleroValues);
		accelerationShortHistory[shortHistoryIndex*3] = x;
		accelerationShortHistory[shortHistoryIndex*3+1] = y;
		accelerationShortHistory[shortHistoryIndex*3+2] = z;
		shortHistoryIndex = (shortHistoryIndex+1) % SHORT_HISTORY;
		
		
		//get the average over the last 10 recorded values
		float avgValues[] = {0.0f, 0.0f, 0.0f};
		for(int i = 0; i<HISTORY_SIZE; i++){
			avgValues[0] += accelerationHistory[i*3];
			avgValues[1] += accelerationHistory[i*3+1];
			avgValues[2] += accelerationHistory[i*3+2];
		}
		avgValues[0] /= HISTORY_SIZE;
		avgValues[1] /= HISTORY_SIZE;
		avgValues[2] /= HISTORY_SIZE;
		float avg = this.vectorSize(avgValues);
		
		if((shortAvg > avg + THRESHOLD) || (shortAvg < avg - THRESHOLD) ){
			//movement detected
			float[] phoneRotation = new float[16];
			Matrix.setIdentityM(phoneRotation, 0);
			//get the invers rotation matrix to get back the accelerometer values to the phone coordinate system
			Matrix.rotateM(phoneRotation, 0, -orientationAngles[1]/3.14f*180.0f, 1.0f, 0.0f, 0.0f);
			Matrix.rotateM(phoneRotation, 0, orientationAngles[2]/3.14f*180.0f, 0.0f, 1.0f, 0.0f);
			float aclMovement[] = new float[4];
			for(int i = 0; i<3; i++){
				aclMovement[i] = currentAcceleroValues[i] - avgValues[i];
			}
			aclMovement[3] = 1.0f;
			Matrix.multiplyMV(phoneCoordAcceleration, 0, phoneRotation, 0, aclMovement, 0);

			for (int i = 0; i<3; i++){
				//timediff is in nanoseconds
				this.speed[i] += timeDiff*phoneCoordAcceleration[i]/10e9f;
			}
		} else {
			//get orientation
			float[] rotationMatrix = new float[16];
			float[] inclinationMatrix = new float[16];
			SensorManager.getRotationMatrix(rotationMatrix, inclinationMatrix, currentAcceleroValues, this.magSensorValues);
			SensorManager.getOrientation(rotationMatrix, orientationAngles);
			
			for(int i =0; i<3; i++){
				this.speed[i] *= (1.0f-timeDiff/10e7f);
			}
			
		}
		
		for(int i = 0; i<3; i++) {
			this.movement[i] = timeDiff*this.speed[i]/10e7f; //timediff is in nanoseconds 10e9 * 100 to get the speed in cm/s = 10e7
		}

		//store the position
		for(int i = 0; i<3; i++){
			accelerationHistory[historyIndex*3+i] = currentAcceleroValues[i];
		}
		historyIndex = (historyIndex + 1) % HISTORY_SIZE;
		
		for(int i = 0; i<3; i++){
			this.accelerationValues[i] = acceleroValues[i];
		}
		infoArray[0] = avg;
		infoArray[1] = shortAvg;
		infoArray[2] = shortAvg - avg;
	}

	public void onMagSensorChanged(float x, float y, float z) {
		this.magSensorValues[0] = x;
		this.magSensorValues[1] = y;
		this.magSensorValues[2] = z;
	}

}
