package com.vutbr.fit.tam.acceleroscroll;

import android.content.Context;

public interface AcceleroSensorManagerInterface {
	 public void stopListening();
	 public void startListening(Context context,
				AcceleroSensorListener listener);
	 public boolean isListening();
}
