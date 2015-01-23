package com.jonma.lrhealth;

import java.util.HashMap;

import com.jinoux.android.bledatawarehouse.BluetoothService;
import com.jonma.tool.CustomDialog;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

public class BleTool {
	private BluetoothManager bluetoothManager;
	private BluetoothAdapter bluetoothAdapter;
	private Context context;
	private BleScanCallBack m_bleScanCallBack;
	private BleServiceCallBack m_bleServiceCallBack;
	private BluetoothService m_bluetoothService;
	private BleConnectCallBack m_bleConnectCallBack;
	private static boolean connectstate = false; // 连接匹配状态（false:未开始连接）
	private boolean connectIsUncon = false;
	private Handler mHandler = new Handler();	
	private static final String LOGTAG = "test";

	public BleTool(Context context) {
		super();
		// TODO Auto-generated constructor stub
		this.context = context;

	}

	/* bl open and scan */
	/**
	 * 
	 * @return 0:successful
	 */
	public int openBle() {
		bluetoothManager = (BluetoothManager) context
				.getSystemService(Context.BLUETOOTH_SERVICE);
		bluetoothAdapter = bluetoothManager.getAdapter();
		if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
			BluetoothAdapter.getDefaultAdapter().enable();
		}
		return 0;
	}

	/**
	 * 
	 * @param bleScanCallBack
	 * @param period scan time. stop scanning automatically when the time is up.
	 * @return
	 */
	public int startScan(BleScanCallBack bleScanCallBack, long period) {
		if(period == 0){
			period = 1000;
		}
		
		if (bluetoothAdapter == null) {
			return -1;
		}
		
		m_bleScanCallBack = bleScanCallBack;		
		
		mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
            	//bluetoothAdapter.stopLeScan(mLeScanCallback);
            	if(LRHealthApp.getInstance().scanIsDevice == 0 && LRHealthApp.getInstance().scanButtionClickTimes <= 1){
            		m_bleScanCallBack.scanNoDevice();
            		
            		LayoutInflater inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);    
            		final View loginLayout = inflater.inflate(R.layout.dialoggeneral, null);            		
            		CustomDialog.Builder customBuilder = new CustomDialog.Builder(context);
            		customBuilder.setView(loginLayout)
            			.setMessage(context.getResources().getString(R.string.scanNoDevice))
            			.setNegativeButton(context.getResources().getString(R.string.cancel), new DialogInterface.OnClickListener(){
            				public void onClick(DialogInterface dialog, int which) {
            					//TODO:
            					dialog.dismiss(); 
            				}
            			});
            		Dialog scanDialog = customBuilder.create();
            		scanDialog.show();
            	}
            	
            	LRHealthApp.getInstance().scanButtionClickTimes--;
            }
        }, period);
		
		bluetoothAdapter.startLeScan(mLeScanCallback);
		
		return 0;
	}

	// Device scan callback.搜索到设备则执行 扫描通过回调告知哪个devie被扫描到
	private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
		@Override
		public void onLeScan(final BluetoothDevice device, int rssi,
				byte[] scanRecord) {
			// boolean flag = true;
			if (m_bleScanCallBack != null) {
				m_bleScanCallBack.scanListening(device);
			}
		}
	};

	public interface BleScanCallBack {
		/**
		 * 
		 * @param device
		 *            the devie that have been founded.
		 */
		public void scanListening(BluetoothDevice device);
		
		public void scanNoDevice();

	}

	/* bl service */
	/**
	 * connect() can be called only when service is created successfully.
	 * 
	 * @param bleServiceCallBack
	 */
	@SuppressLint("InlinedApi")
	public void service_init(BleServiceCallBack bleServiceCallBack) {
		Intent gattServiceIntent = new Intent(context, BluetoothService.class);
		boolean bll = context.bindService(gattServiceIntent,
				mServiceConnection, context.BIND_AUTO_CREATE);
		if (bll) {
			Log.i(LOGTAG, "绑定服务gattServiceIntent成功");
		} else {
			Log.i(LOGTAG, "绑定服务gattServiceIntent失败");
		}
		//context.registerReceiver(mGattUpdateReceiver,	makeGattUpdateIntentFilter());
		if(m_bluetoothService == null){
			registerReceiver();
		}

		m_bleServiceCallBack = bleServiceCallBack;
	}

	private static IntentFilter makeGattUpdateIntentFilter() {
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BluetoothService.ACTION_GATT_DISCONNECTED);
		intentFilter.addAction(BluetoothService.ACTION_GATT_CONNECTED);
		intentFilter
				.addAction(BluetoothService.ACTION_GATT_READCHARACTERISTICSUCCESS);
		return intentFilter;
	}

	// 通过回调通知service建立成功
	private final ServiceConnection mServiceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName componentName,
				IBinder service) {
			m_bluetoothService = ((BluetoothService.LocalBinder) service)
					.getService();
			if (m_bluetoothService == null) {
				Log.d(LOGTAG, "m_bluetoothService is null");
			} else {
				Log.d(LOGTAG, "m_bluetoothService is not null");
				m_bleServiceCallBack.onBuild();

			}
			boolean ba = m_bluetoothService.initialize();
			if (!ba) {
				Log.i(LOGTAG, "Unable to initialize Bluetooth");
			} else {
				Log.i(LOGTAG, "initialize Bluetooth");
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName componentName) {
			m_bluetoothService = null;
		}
	};

	public interface BleServiceCallBack {
		/**
		 * when service is created successfully, call this call back funciton
		 */
		public void onBuild();
	}

	/* connect */
	public void connect(String macAddr, BleConnectCallBack bleConnectCallBack) {
		// TODO Auto-generated method stub
		if (bluetoothAdapter == null) {
			bluetoothAdapter = bluetoothManager.getAdapter();
			return;
		}
		
		m_bluetoothService.gethandler(deviceHandler);
		Log.d(LOGTAG, "connect:"+macAddr);
		m_bluetoothService.connect(macAddr);
		m_bleConnectCallBack = bleConnectCallBack;
		
		connectIsUncon = false;
		
		Handler con_Handler = new Handler();
		con_Handler.postDelayed(new Runnable() {
            @Override
            public void run() {
            	Log.i("test1", "connect timeout");
            	Log.i("test1", (connectstate == false)?"connectstate == false":"connectstate != false");
            	if(connectstate == false && connectIsUncon == false){
            		if(m_bleConnectCallBack != null){
            			Log.i("test", "connect timeout");
            			disconnect();
						m_bleConnectCallBack.onConnectFailed();
					}
            	}
            }
        }, 15000);
	}
	
	public void disconnect() {
		Log.i(LOGTAG, "disconnect");
		connectIsUncon = true;
		m_bluetoothService.disconnect();
	}	

	// 接收广播 sevice 通过接收广播来知道是否连接成功，handler还可以根据连接是否成功做出相应动作
	private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			Log.i(LOGTAG, "action = " + action);
			if (BluetoothService.ACTION_GATT_READCHARACTERISTICSUCCESS
					.equals(action)) { // 连接成功 并读取characteristic成功
				// String connecttext = "disconnect";
				// connectButton.setText(connecttext);
				// ConnectProgressBarzt(false);
				if(m_bleConnectCallBack != null){
					m_bleConnectCallBack.onConnect();
				}
				connectstate = true;
			} else if (BluetoothService.ACTION_GATT_DISCONNECTED.equals(action)) { // 模块已断开连接
				Log.i("test", (connectstate == true)?"true":"false");
				connectIsUncon = true;
				//if (connectstate == true) { // 正在连接中
					// showAlertDialog(
					// "模块已关闭连接，请断开!",
					// getResources().getString(
					// R.string.alertOneButtonTitle), null, 0);
					Log.d(LOGTAG, "模块已关闭连接，请断开!");
					if(m_bleConnectCallBack != null){
						m_bleConnectCallBack.onConnectFailed();
					}
				//}
			}
		}
	};

	public interface BleConnectCallBack {
		/**
		 * when connect successfully, call this call back funciton
		 */
		public void onConnect();
		
		public void onConnectFailed();
	}

	/* handler of ble service */
	@SuppressLint("HandlerLeak")
	public Handler deviceHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			// Log.i("what === "+msg.what);
			switch (msg.what) {
			case 0:
				Log.i(LOGTAG, "连接失败");
				connectIsUncon = true;
				connectstate = false;
				if(m_bleConnectCallBack != null){
					m_bleConnectCallBack.onConnectFailed();
				}
				break;
			case 1:
				String str = (String) msg.obj;
				// str = Tools.bytesToHexString(str.getBytes());
				Log.d(LOGTAG, "received data:" + str);
				break;
			case 2:
				// String ss = (String) msg.obj;
				// receivetop_text.setText("接收数据：已接收 "+ss+" 个字节");
				break;
			default:
				break;
			}
		}
	};

	public int stopScan() {
		if (bluetoothAdapter == null) {
			return -1;
		}

		bluetoothAdapter.stopLeScan(mLeScanCallback);
		return 0;
	}

	public BluetoothService getBleService() {
		return m_bluetoothService;
	}

	public void unregisterReceiver(){
		context.unregisterReceiver(mGattUpdateReceiver);
	}
	
	public void unbindService() {		
		context.unbindService(mServiceConnection);
	}
	
	public void registerReceiver(){
		context.registerReceiver(mGattUpdateReceiver,
				makeGattUpdateIntentFilter());
	}
}
