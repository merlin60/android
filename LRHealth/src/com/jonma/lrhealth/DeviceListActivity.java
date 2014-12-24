package com.jonma.lrhealth;

import java.util.ArrayList;
import java.util.HashMap;

import com.jinoux.android.bledatawarehouse.BluetoothService;

import android.R.bool;
import android.R.integer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
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
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class DeviceListActivity extends Activity {
	LRHealthApp application;
	
	private static final int REQUEST_OPEN_BT_CODE = 0x01;

	private static final int MESSAGE_UPDATELIST = 0x1000;
	private static final int MESSAGE_CONNECT = 0x1001;
	private static final int MESSAGE_CONNECTED = 0x1002;

	private static Button m_btnScan;
	private static Button m_btnBack;

	private static ListView m_listviewDev;
	private SimpleAdapter m_itemSimAdapter = null;
	private ArrayList<HashMap<String, Object>> m_listInfo = null;
	private int curListviewId;
	private static final String ObjectStatus = "Icon";
	private static final String ObjectName = "Name";
	private static final String ObjectDetail = "Detail";
	
	private static final String LOGTAG = "LRHealth";

	/*bluetooth*/
	private String address;
	private String macBleModule;// 00:1B:35:0B:5E:42
	private final static String nameBleModule = "BLE0102C2P";
	public static boolean connectstate = false; // �����ゆ�烽����ゆ�峰�归����ゆ�风�舵�������ゆ��false:��������ゆ�峰�������ゆ�烽����ワ�����

	private static int yyd = 0; // ���绐���ゆ�烽����ゆ�烽����ゆ�烽��锟�
	private static int sendxhid = 0; // 姣����杞跨����烽����ゆ�烽����ゆ�锋《������锟� �����ゆ�烽����电����烽����ゆ�烽����风����烽����ゆ�烽��锟�0--255 姣������ゆ�烽����ゆ�蜂�����杞跨》��蜂�������ゆ�� �����ゆ�烽����ゆ�烽����ゆ�烽��缁���斤拷0
	private static int sss = 0; // ���搴������ゆ�烽����ゆ�烽��锟� �����ゆ�烽����ゆ�烽����ゆ�锋��搴������ゆ��
	private static int nm = 0; // �����ゆ�烽����ゆ�烽����ゆ�烽����锋����告�烽����ゆ�烽����ゆ��
	public static boolean senddatastate = false; // ���瑙���ゆ�峰�������ゆ�烽����ゆ�烽�����璁规�烽����ゆ�烽����ゆ�烽����ゆ�� false:��������ゆ�峰��	
	
	private BleTool m_bleTool;
	public BluetoothAdapter bluetoothAdapter;
	public BluetoothService mbluetoothService;
	private static final int lvConnectStaSuc = R.string.lvConnectStaSuc;
	private static final int lvConnectStaNot = R.string.lvConnectStaNot;
	private static final int lvConnectStaDoing = R.string.lvConnectStaDoing;
	
	private boolean unregisterReceiverFlag = true;
	
	private Handler m_handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_UPDATELIST: {
				updateBluetoothDevList();
			}
				break;
			case MESSAGE_CONNECT:
				m_bleTool.stopScan();
				m_bleTool.connect(macBleModule, m_bleConnectCallBack);
				break;
			case MESSAGE_CONNECTED:
				Log.d(LOGTAG, "connected");
				application.connectStatus = true; 
				mbluetoothService = m_bleTool.getBleService();
				m_listInfo.get(curListviewId).put(ObjectStatus, getResources().getString(lvConnectStaSuc));
				Message message = Message.obtain();
				message.what = MESSAGE_UPDATELIST;
				m_handler.sendMessage(message);
				//m_bleTool.unregisterReceiver()
				break;
			default:
				break;
			}
			super.handleMessage(msg);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(LOGTAG, "device list activity create");
		super.onCreate(savedInstanceState);		

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

		setContentView(R.layout.activity_devicelist);
		
		application = LRHealthApp.getInstance();
		mbluetoothService = application.getBluetoothService();
		application.addActivity(this);
		
		// init view
		initView();

		//bt
		Log.d(LOGTAG, "Open bluetooth and start scan");
		openBluetooth();		
		startScanBluetoothDev();
		Log.d(LOGTAG, "open and start bluethooth done");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.device_list, menu);
		return true;
	}

	@Override
	public void onResume() {		
		Log.d(LOGTAG, "device list activity resume");
		/*
		// register receiver
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		registerReceiver(m_bdreceiver, filter);			
		*/
		
		//if(mbluetoothService == null){
			//m_bleTool.registerReceiver();
		//}	
		
		SharedSetting mySharedSetting = new SharedSetting(DeviceListActivity.this);	
		
		super.onResume();
	}

	@Override
	public void onPause() {
		Log.d(LOGTAG, "device list activity pause");
		
		super.onPause();
	}

	@Override
	public void onStop() {	
		Log.d(LOGTAG, "device list activity stop");
		
		if(unregisterReceiverFlag){
			if(mbluetoothService != null){
				Log.i("===", "unregisterReceiver");
				m_bleTool.unregisterReceiver();
				unregisterReceiverFlag = false;
			}else{
				Log.i("===", "no unregisterReceiver");
			}
		}
		m_bleTool.stopScan();
		
		super.onStop();
	}

	@Override
	public void onDestroy() {			
		Log.d(LOGTAG, "device list activity destory");
		
		if(mbluetoothService != null){
			Log.i(LOGTAG, "unbind");
			m_bleTool.disconnect();
			m_bleTool.unbindService();
		}
		
		super.onDestroy();
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		Log.d(LOGTAG, "device list activity configuration changed"); 
	    
	    super.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) 
	{		
		if (keyCode == KeyEvent.KEYCODE_BACK) 
		{
			if (0 == event.getRepeatCount()) 
			{
				Bundle bundle = new Bundle();
				if (macBleModule != null) {
					bundle.putString("mac", macBleModule);
				}
				
				Intent intent = new Intent();
				intent.putExtras(bundle);
				intent.setClass(DeviceListActivity.this, OperationCenterActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				Log.d(LOGTAG, "start intent");
				startActivity(intent);	
			}
			
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

	private void initView() {
		// TODO Auto-generated method stub
		m_btnScan = (Button) findViewById(R.id.button_scan);
		m_btnScan.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// clear
				 m_listInfo.clear();
				 application.scanIsDevice = 0;
				 updateBluetoothDevList();

				// scan bt
				m_listInfo.clear();
				m_bleTool.stopScan();
//				if(mbluetoothService != null){
//					m_bleTool.disconnect();
//					//m_bleTool.unregisterReceiver();
//					m_bleTool.unbindService();					
//				}
				startScanBluetoothDev();
			}
		});

		m_btnBack = (Button) findViewById(R.id.button_back);
		m_btnBack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) 
			{				
				Bundle bundle = new Bundle();
				if (macBleModule != null) {
					bundle.putString("mac", macBleModule);
				}
				
				Intent intent = new Intent();
				intent.putExtras(bundle);
				intent.setClass(DeviceListActivity.this, OperationCenterActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
				Log.d(LOGTAG, "start intent");
				startActivity(intent);				
			}
		});

		m_listInfo = new ArrayList<HashMap<String, Object>>();
		m_listviewDev = (ListView) findViewById(R.id.listView_dev);
		m_itemSimAdapter = new SimpleAdapter(this, m_listInfo,
				R.layout.listview_item_devinfo, new String[] { ObjectName,
						ObjectDetail, ObjectStatus }, new int[] {
						R.id.textView_devname, R.id.textView_devinfo,
						R.id.devstatus});

		m_listviewDev.setAdapter(m_itemSimAdapter);
		m_listviewDev.setOnItemClickListener(new ListOnItemClickListener());
	}

	private final class ListOnItemClickListener implements
			AdapterView.OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {			
			//TODO: click,  then connect corresponding device
			curListviewId = position;			
			m_bleTool.stopScan();
			macBleModule = m_listInfo.get(curListviewId).get(ObjectDetail).toString();
			Log.i("===", "current listview mac:" + macBleModule);
			if(m_bleTool.getBleService() != null) m_bleTool.disconnect(); //disconnect
			/*clear all listview status*/
			int cnt = m_listInfo.size();
			if(cnt != 0){
				for(int i=0; i<cnt; i++){
					m_listInfo.get(i).put(ObjectStatus, getResources().getString(lvConnectStaNot));
				}
			}
			m_bleTool.connect(macBleModule, m_bleConnectCallBack);
			m_listInfo.get(curListviewId).put(ObjectStatus, getResources().getString(lvConnectStaDoing));
			
			// send message
			Message message = Message.obtain();
			message.what = MESSAGE_UPDATELIST;
			m_handler.sendMessage(message);
			
//			Bundle bundle = new Bundle();
//			bundle.putInt("FunIdx", 0);
//			if (macBleModule != null) {
//				bundle.putString("mac", macBleModule);
//			}
//
//			//if connectted, start activity. if not, do nothing
//			Intent intent = new Intent();
//			intent.putExtras(bundle);
//			intent.setClass(DeviceListActivity.this,
//					OperationCenterActivity.class);
//			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
//					| Intent.FLAG_ACTIVITY_SINGLE_TOP);
//			Log.i(LOGTAG, "start intent in list");
//			startActivity(intent);
		}
	}
	

	private void updateBluetoothDevList() {
		m_itemSimAdapter.notifyDataSetChanged();
	}	
	
	/*BLE codes*/
	
	private int openBluetooth() {
		m_bleTool = new BleTool(DeviceListActivity.this);
		return m_bleTool.openBle();	
	}

	private int startScanBluetoothDev() {
		if(mbluetoothService != null){
			Log.i(LOGTAG, "disconnect");
			m_bleTool.disconnect();
			//m_bleTool.unregisterReceiver();
			//m_bleTool.unbindService();					
		}else{
			Log.i(LOGTAG, "mbluetoothService is null .no disconnect");			
		}
		m_bleTool.startScan(m_BleScanCallback, 10000);
		return 0;
	}
	
	/*three callback funciton of BleTool*/
	
	private BleTool.BleScanCallBack m_BleScanCallback = new BleTool.BleScanCallBack() {
		@Override
		public void scanListening(BluetoothDevice device) {
			// TODO Auto-generated method stub
			application.scanIsDevice = 1;
			android.util.Log.d(LOGTAG, device.getAddress());
			HashMap<String, Object> map;
			map = new HashMap<String, Object>();
			map.put(ObjectName, device.getName());
			map.put(ObjectDetail, device.getAddress());
			map.put(ObjectStatus, getResources().getString(lvConnectStaNot));
			m_listInfo.add(map);
			// send message
			Message message = Message.obtain();
			message.what = MESSAGE_UPDATELIST;
			m_handler.sendMessage(message);

			//if (device.getName().equalsIgnoreCase(nameBleModule)) {
				macBleModule = device.getAddress();
				android.util.Log.d(LOGTAG, "finded " + macBleModule);
				m_bleTool.service_init(m_bleServiceCallBack);
			//}			
		}		
	};
	
	private BleTool.BleServiceCallBack m_bleServiceCallBack = new BleTool.BleServiceCallBack() {
		@Override
		public void onBuild() {
			// when in this funciton, it indicate service has been created successfully, then can connect ble device
			// TODO Auto-generated method stub
			
//			Message message = Message.obtain();
//			message.what = MESSAGE_CONNECT;
//			m_handler.sendMessage(message);			
		}		
	};
	
	private BleTool.BleConnectCallBack m_bleConnectCallBack = new BleTool.BleConnectCallBack() {
		@Override
		public void onConnect() {
			Message message = Message.obtain();
			message.what = MESSAGE_CONNECTED;
			m_handler.sendMessage(message);
		}

		@Override
		public void onConnectFailed() {
			//TODO: add code for failing to connect
			//re-connect some times or do nothing			
			m_listInfo.get(curListviewId).put(ObjectStatus, getResources().getString(lvConnectStaNot));
			// send message
			Message message = Message.obtain();
			message.what = MESSAGE_UPDATELIST;
			m_handler.sendMessage(message);
			application.connectStatus = false; 
		}
	};
	
}
