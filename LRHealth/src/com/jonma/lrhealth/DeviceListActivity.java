package com.jonma.lrhealth;

import java.util.ArrayList;
import java.util.HashMap;

import com.jinoux.android.bledatawarehouse.BluetoothService;

import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
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
	private static final int REQUEST_OPEN_BT_CODE = 0x01;

	private static final int MESSAGE_UPDATELIST = 0x1000;
	private static final int MESSAGE_CONNECT = 0x1001;
	private static final int MESSAGE_CONNECTED = 0x1002;

	private static Button m_btnScan;
	private static Button m_btnBack;

	private static ListView m_listviewDev;
	private SimpleAdapter m_itemSimAdapter = null;
	private ArrayList<HashMap<String, Object>> m_listInfo = null;

	private static final String ObjectIcon = "Icon";
	private static final String ObjectName = "Name";
	private static final String ObjectDetail = "Detail";
	
	private static final String LOGTAG = "test";

	
	/*bluetooth*/
	private String address;
	private String macBleModule;// 00:1B:35:0B:5E:42
	private final static String nameBleModule = "BLE0102C2P";
	public static boolean connectstate = false; // 连接匹配状态（false:未开始连接）

	private static int yyd = 0; // 已返回序号
	private static int sendxhid = 0; // 每次点击发送按钮 发送的数据的序号0--255 每发送一次加一， 清空数据时为0
	private static int sss = 0; // 未应答个数 最多五个未应答
	private static int nm = 0; // 发送数据成功次数
	public static boolean senddatastate = false; // 是否开始发送自定义数据 false:未开始	
	
	private BleTool m_bleTool;
	public BluetoothAdapter bluetoothAdapter;
	public BluetoothService mbluetoothService;

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
				//TODO:
				Log.d(LOGTAG, "connected");
				Toast.makeText(DeviceListActivity.this, "连接成功", 0).show();
				mbluetoothService = m_bleTool.getBleService();
				//m_bleTool.unregisterReceiver();
				break;
			default:
				break;
			}
			super.handleMessage(msg);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

		setContentView(R.layout.activity_devicelist);
		
		LRHealthApp application = (LRHealthApp)getApplication();
		mbluetoothService = application.getBluetoothService();
		// init view
		initView();

		//bt
		openBluetooth();
		startScanBluetoothDev();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.device_list, menu);
		return true;
	}

	@Override
	public void onResume() {
		super.onResume();
		// register receiver
		//IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		// registerReceiver(m_bdreceiver, filter);		
		SharedSetting mySharedSetting = new SharedSetting(DeviceListActivity.this);	
		//m_bleTool.registerReceiver();	
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onStop() {
		m_bleTool.unregisterReceiver();
		m_bleTool.stopScan();
		super.onStop();
	}

	@Override
	public void onDestroy() {
		m_bleTool.unbindService();
		super.onDestroy();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK) && (0 == event.getRepeatCount())) {
			String strMsg = String.format("Will you exit left right app?");
			new AlertDialog.Builder(this)
					.setTitle("Warm Prompt")
					.setMessage(strMsg)
					.setNegativeButton("Cancel",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {

								}
							})
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									finish();
									System.exit(0);
								}
							}).show();
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
				// m_listInfo.clear();
				// updateBluetoothDevList();

				// scan bt
				m_listInfo.clear();
				m_bleTool.stopScan();
				if(mbluetoothService != null){
					//m_bleTool.unregisterReceiver();
					//m_bleTool.unbindService();					
				}
				startScanBluetoothDev();
			}
		});

		m_btnBack = (Button) findViewById(R.id.button_back);
		m_btnBack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Bundle bundle = new Bundle();
				bundle.putInt("FunIdx", 0);
				//Log.d(LOGTAG, macBleModule);
				if (macBleModule != null) {
					bundle.putString("mac", macBleModule);
				}
				
				Intent intent = new Intent();
				intent.putExtras(bundle);
				intent.setClass(DeviceListActivity.this,
						OperationCenterActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
						| Intent.FLAG_ACTIVITY_SINGLE_TOP);
				Log.i(LOGTAG, "start intent");
				startActivity(intent);
				
				/*
				String strMsg = String.format("Will you exit left right app?");
				new AlertDialog.Builder(DeviceListActivity.this)
						.setTitle("Warm Prompt")
						.setMessage(strMsg)
						.setNegativeButton("Cancel",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {

									}
								})
						.setPositiveButton("OK",
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										finish();
										System.exit(0);
									}
								}).show();
				*/
			}
		});

		m_listInfo = new ArrayList<HashMap<String, Object>>();
		m_listviewDev = (ListView) findViewById(R.id.listView_dev);
		m_itemSimAdapter = new SimpleAdapter(this, m_listInfo,
				R.layout.listview_item_devinfo, new String[] { ObjectName,
						ObjectDetail, ObjectIcon }, new int[] {
						R.id.textView_devname, R.id.textView_devinfo,
						R.id.imageView_devstatus });

		m_listviewDev.setAdapter(m_itemSimAdapter);
		m_listviewDev.setOnItemClickListener(new ListOnItemClickListener());
	}

	private final class ListOnItemClickListener implements
			AdapterView.OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			
			//TODO: click,  then connect corresponding device
			m_bleTool.stopScan();
			m_bleTool.connect(macBleModule, m_bleConnectCallBack);

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
		Log.d(LOGTAG, "notifylistupdate");
		m_itemSimAdapter.notifyDataSetChanged();
	}	
	
	/*BLE codes*/
	
	private int openBluetooth() {
		m_bleTool = new BleTool(DeviceListActivity.this);
		return m_bleTool.openBle();		
	}

	private int startScanBluetoothDev() {
		m_bleTool.startScan(m_BleScanCallback);
		return 0;
	}
	
	/*three callback funciton of BleTool*/
	
	private BleTool.BleScanCallBack m_BleScanCallback = new BleTool.BleScanCallBack() {
		@Override
		public void scanListening(BluetoothDevice device) {
			// TODO Auto-generated method stub
			android.util.Log.d(LOGTAG, device.getAddress());
			HashMap<String, Object> map;
			map = new HashMap<String, Object>();
			map.put(ObjectName, device.getName());
			map.put(ObjectDetail, device.getAddress());
			map.put(ObjectIcon, R.drawable.icon_update);
			m_listInfo.add(map);
			// send message
			Message message = Message.obtain();
			message.what = MESSAGE_UPDATELIST;
			m_handler.sendMessage(message);

			if (device.getName().equalsIgnoreCase(nameBleModule)) {
				macBleModule = device.getAddress();
				android.util.Log.d(LOGTAG, "finded " + macBleModule);
				m_bleTool.service_init(m_bleServiceCallBack);
			}			
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
		}
	};
	
}
