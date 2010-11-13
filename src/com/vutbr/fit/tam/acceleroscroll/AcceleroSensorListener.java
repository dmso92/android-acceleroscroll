package com.vutbr.fit.tam.acceleroscroll;

public interface AcceleroSensorListener {
	/**
	 * Acceleration changed in the phone coordinate system
	 * @param x - display right
	 * @param y - display up
	 * @param z - back camera direction
	 * @param timeDiff - time since the last change
	 */
	public void onAccelerationChanged(float x, float y, float z, float timeDiff);
}
