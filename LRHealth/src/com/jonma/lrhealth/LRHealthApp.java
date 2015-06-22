package com.jonma.lrhealth;

import java.util.LinkedList;
import java.util.List;

import com.jinoux.android.bledatawarehouse.BluetoothService;

import android.R.integer;
import android.app.Activity;
import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;

public class LRHealthApp extends Application{
	public BluetoothService mbluetoothService;
	
	private List<Activity> mList = new LinkedList<Activity>();  
	private static LRHealthApp instance; 
	public boolean connectStatus = false; 
	public boolean isDisconnUnexpecedly = false;
	public int reConnStatusNum = 0;
	public int scanIsDevice = 0;
	public int scanButtionClickTimes = 0;
	private int m_nScreenW, m_nScreenH, m_nScreenDpi;
	private float m_fScreenDensity;
	public Boolean sendStatusBoolean = false;
	public Boolean reConnWhenResendStatus = false;
	public Boolean reSendEn = false;
	public Boolean allSenddataButtonEn = true;
	public BluetoothAdapter bluetoothAdapter;
	public BluetoothManager bluetoothManager;
	public BleTool m_bleTool;
	public String macBleModule;// 00:1B:35:0B:5E:42
	public int devicelistTimerCnt = 0;
	
	private ConnectCallback connInterface;
	
	public int curActivity;
	public static final int DEVICE = 0x001;
	public static final int OPER = 0x002;

	
	public void setScreenSize(int w, int h, int dpi, float den)
	{
		m_nScreenW = w;
		m_nScreenH = h;
		m_nScreenDpi = dpi;
		m_fScreenDensity = den;
	}
	
	public int getScreenWidth()
	{
		return m_nScreenW;
	}
	
	public int getScreenHeight()
	{
		return m_nScreenH;
	}
	
	public int getScreenDpi()
	{
		return m_nScreenDpi;
	}
	
	public float getScreenDensity()
	{
		return m_fScreenDensity;
	}
	
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
    
    public void initConn(ConnectCallback connectInter) {
    	connInterface = connectInter;
    }
    
    public void conn(){
    	connInterface.connCallback();
    }
    
    public interface ConnectCallback {
    	public void connCallback();
    }
}
