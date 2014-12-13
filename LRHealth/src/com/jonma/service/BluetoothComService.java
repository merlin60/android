package com.jonma.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.UUID;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class BluetoothComService extends Service{	
   
    public class BtBinder extends Binder implements BluetoothCom {  
    	BluetoothComService getService() {  
            return BluetoothComService.this;  
        }  
          
        public boolean sendData(String str) {  
            // TODO Auto-generated method stub  
            byte[] msgBuffer;  
            msgBuffer = str.getBytes();  
            try {  
                if (btSocket != null) {  
                    outputStream = btSocket.getOutputStream();  
                }  
            } catch (IOException e) {  
                Log.e(TAG, "ON Resume:output stream creation failed");  
            }  
            try {  
                outputStream.write(msgBuffer);  
                ACK++; 
                if(ACK>9999){  
                    ACK=0;  
                }  
                  
                return true;  
            } catch (IOException e) {  
                // TODO: handle exception  
                Log.e(TAG, "On Resume:Exception during write");  
                DisplayMessage("send failed.");  
            }  
            return false;  
        }  
           
        public String getData() {  
            // TODO Auto-generated method stub  
            if (BackACK != null) {  
                return BackACK;  
            }  
            return null;  
        }  
          
        public boolean syncStatus(int mode, int value){ 
            // TODO Auto-generated method stub  
            String string = generateCommand(mode, value);
            sendData(string);  
            return false;  
        }  
    
        public String generateCommand(int mode, int value)
        {  
            return "";      
        }  
    }  
    
    private static final UUID myUUID = UUID  
            .fromString("*****");  
    private static String address = "****";  
  
    private BluetoothAdapter myBluetoothAdapter = BluetoothAdapter  
            .getDefaultAdapter(); 
    private BluetoothDevice mDevice = myBluetoothAdapter  
            .getRemoteDevice(address);
    private BluetoothSocket btSocket = null;  
    private InputStream inputStream = null;  
    private OutputStream outputStream = null;  
    private int temp = 0;  
    private String CMD = null;  
    private String BackACK = null;  
    private static final String TAG = "BluetoothComService";  
    private int ACK=0;  
  
    private IBinder mBinder = new BtBinder();  
  
    @Override  
    public IBinder onBind(Intent intent) {  
        // TODO Auto-generated method stub  
        System.out.println("onBind Service");  
        return mBinder;  
    }  
  
    @Override  
    public void onCreate() {  
        // TODO Auto-generated method stub  
        System.out.println("onCreate Service");  
        DisplayMessage("Bluetooth Service Start");  
  
        DisplayMessage("try to connect bt dev" + mDevice + "wait for moment...");  
         
        new Thread() {  
            public void run() {  
                if (!myBluetoothAdapter.isEnabled()) {  
                    try {  
                        Log.e(TAG, "thread sleep");  
                        sleep(8000);
                    } catch (InterruptedException e1) {  
                        // TODO Auto-generated catch block  
                        e1.printStackTrace();  
                    }  
                }  
                Log.e(TAG, "thread start");  
                try {  
                    btSocket = mDevice  
                            .createRfcommSocketToServiceRecord(myUUID);  
                } catch (IOException e) {  
                    // TODO Auto-generated catch block  
                    DisplayMessage("creat socket failed");  
                    e.printStackTrace();  
                }  
                try {  
                    connectDevice();
                } catch (Exception e) {  
                    // TODO: handle exception  
                }  
                  
            }  
        }.start();  
        super.onCreate();  
    }  
  
    @Override  
    public void onDestroy() {  
        // TODO Auto-generated method stub  
        System.out.println("onDestroy Service");  
        super.onDestroy();  
    }  
  
    @Override  
    public int onStartCommand(Intent intent, int flags, int startId) {  
        // TODO Auto-generated method stub  
        Log.i("LocalService", "Received Start ID" + startId + ":" + intent);  
        System.out.println("onStartCommand Service");  
        return START_STICKY;  
    }  
  
    @Override  
    public void onStart(Intent intent, int startId) {  
        // TODO Auto-generated method stub  
        System.out.println("onStart Service");  
        getMessageThread getMessage = new getMessageThread();  
        getMessage.start();  
        super.onStart(intent, startId);  
    }  
  
    @Override  
    public boolean onUnbind(Intent intent) {  
        // TODO Auto-generated method stub  
        System.out.println("onUnbind Service");  
        return super.onUnbind(intent);  
    }  
  
    public void DisplayMessage(String str) {  
        Toast.makeText(this, str, Toast.LENGTH_LONG).show(); //显示Toast信息  
    }  
  
    protected void connectDevice() {  
        try {   
            if (mDevice.getBondState() == BluetoothDevice.BOND_NONE) {  
                Method creMethod = BluetoothDevice.class  
                        .getMethod("createBond");  
                Log.e("TAG", "start pairing");  
                creMethod.invoke(mDevice);  
            } else {  
            }  
        } catch (Exception e) {  
            // TODO: handle exception  
            DisplayMessage("cann't pair");  
            e.printStackTrace();  
        }  
        myBluetoothAdapter.cancelDiscovery();  
        try {  
            btSocket.connect();  
            DisplayMessage("connect success!");  
        } catch (IOException e) {  
            // TODO: handle exception  
            DisplayMessage("connect failed!");  
            try {  
                btSocket.close();  
            } catch (IOException e2) {  
                // TODO: handle exception  
                Log.e(TAG, "Cannot close connection when connection failed");  
            }  
        }  
    }  
      
    public class getMessageThread extends Thread {  
        public void run() {  
            if (btSocket != null) {  
                try {  
                    inputStream = btSocket.getInputStream();  
                } catch (IOException e) {  
                    // TODO Auto-generated catch block  
                    e.printStackTrace();  
                }  
            }  
            try {  
                BackACK = Integer.toString(inputStream.read());  
            } catch (Exception e) {  
                // TODO: handle exception  
            }  
        }  
    } 

}
