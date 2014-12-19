package com.jonma.lrhealth;

import com.jinoux.android.bledatawarehouse.BluetoothService;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;

public class LRHealthApp extends Application{
	public BluetoothService mbluetoothService;
	
	public BluetoothService getBluetoothService() {
		return mbluetoothService;
	}
}
