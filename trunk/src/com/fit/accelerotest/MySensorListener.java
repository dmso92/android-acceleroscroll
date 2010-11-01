package com.fit.accelerotest;

public interface MySensorListener {	
	/**
	 * Acceleration changed in the phone coordinate system
	 * @param x - display right
	 * @param y - display up
	 * @param z - back camera direction
	 */
	public void onAccelerationChanged(float x, float y, float z);
	public void onAccelerationChanged(float x, float y, float z, float timeDiff);
	public void onMagSensorChanged(float x, float y, float z);
}
