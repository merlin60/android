package com.jonma.lrhealth;

import java.util.LinkedList;
import java.util.List;

import com.jinoux.android.bledatawarehouse.BluetoothService;

import android.app.Activity;
import android.app.Application;
import android.bluetooth.BluetoothAdapter;

public class LRHealthApp extends Application{
	public BluetoothService mbluetoothService;
	
	private List<Activity> mList = new LinkedList<Activity>();  
	private static LRHealthApp instance; 
	public boolean connectStatus = false; 
	public int scanIsDevice = 0;
	public int scanButtionClickTimes = 0;
	
	public BluetoothService getBluetoothService() {
		return mbluetoothService;
	}
	
	public synchronized static LRHealthApp getInstance(){   
        if (null == instance) {   
            instance = new LRHealthApp();   
        }   
        return instance;   
    }   
	
    public void addActivity(Activity activity) {   
        mList.add(activity);   
    }   
     
    public void exit() {   
        try {   
            for (Activity activity:mList) {   
                if (activity != null)   
                    activity.finish();   
            }   
        } catch (Exception e) {   
            e.printStackTrace();   
        } finally {   
            System.exit(0);   
        }   
    }   

    public void onLowMemory() {   
        super.onLowMemory();       
        System.gc();   
    }  
}
