package com.jonma.lrhealth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import javax.security.auth.PrivateCredentialPermission;

import com.jinoux.android.bledatawarehouse.BluetoothService;
import com.jonma.tool.CustomDialog;

import android.R.bool;
import android.R.integer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
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
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class DeviceListActivity extends Activity {
	LRHealthApp application;

	private SharedSetting mySharedSetting = null;

	private static final int REQUEST_OPEN_BT_CODE = 0x01;

	private static final int MESSAGE_UPDATELIST = 0x1000;
	private static final int MESSAGE_CONNECT = 0x1001;
	private static final int MESSAGE_CONNECTED = 0x1002;
	private static final int MESSAGE_NODEVICE = 0x1003;
	private static final int MESSAGE_FINDDEVICE = 0x1004;
	private static final int MESSAGE_SCAN = 0x1005;

	private static final int STOP_NOTIFIER = 0x2000;
	private static final int THREADING_NOTIFIER = 0x2001;

	private static final int SCANTIME = 20;// unit:s

	private Boolean openRet = false;
	private Timer timer;
	/* view */
	private static Button m_btnScan;
	private static Button m_btnBack;
	private ProgressBar pro;
	private int intCounter = 0;

	private static ListView m_listviewDev;
	private SimpleAdapter m_itemSimAdapter = null;
	private ArrayList<HashMap<String, Object>> m_listInfo = null;
	private int curListviewId;
	private static final String ObjectStatus = "Icon";
	private static final String ObjectName = "Name";
	private static final String ObjectDetail = "Detail";

	private static final String LOGTAG = "LRHealth";

	private ProgressBar mProgress;

	/* bluetooth */
	private String address;
	// private String application.macBleModule;// 00:1B:35:0B:5E:42
	// private final static String nameBleModule = "BLE0102C2P";
	private final static String nameBleModule = "BLE0202";

	public static boolean connectstate = false;
	private static int yyd = 0;
	private static int sendxhid = 0;
	private static int sss = 0;
	private static int nm = 0;
	public static boolean senddatastate = false;
	public BluetoothAdapter bluetoothAdapter;
	public BluetoothService mbluetoothService;
	private static final int lvConnectStaSuc = R.string.lvConnectStaSuc;
	private static final int lvConnectStaNot = R.string.lvConnectStaNot;
	private static final int lvConnectStaDoing = R.string.lvConnectStaDoing;
	private static final int lvConnectStaReDoing = R.string.lvConnectStaReDoing;

	private boolean unregisterReceiverFlag = true;
	private boolean isFirstStart = true;
	private boolean isFirstScan = true;

	/* scan status */
	private boolean reConneStatus = false;

	private String curConnectDeviceMac = new String();
	private boolean haveScanned = false;
	private Handler mHandler = new Handler();

	private Handler m_handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_UPDATELIST: {
				updateBluetoothDevList();
			}
				break;
			case MESSAGE_CONNECT:
				application.m_bleTool.stopScan();
				application.m_bleTool.connect(application.macBleModule,
						m_bleConnectCallBack);
				break;
			case MESSAGE_CONNECTED:
				Log.d(LOGTAG, "connected");
				application.allSenddataButtonEn = true;
				application.connectStatus = true;
				mbluetoothService = application.m_bleTool.getBleService();
				m_listInfo.get(curListviewId).put(ObjectStatus,
						getResources().getString(lvConnectStaSuc));
				Message message = Message.obtain();
				message.what = MESSAGE_UPDATELIST;
				m_handler.sendMessage(message);
				// application.m_bleTool.unregisterReceiver()
				mySharedSetting.saveLastConndevice(application.macBleModule);
				Log.i("***", "save last mac: " + application.macBleModule);
				break;
			case MESSAGE_NODEVICE:
				goneProShowbtn();
				break;
			case MESSAGE_FINDDEVICE:
				goneProShowbtn();
				break;
			case MESSAGE_SCAN:
				startScanBluetoothDev();
				break;
			default:
				break;
			}
			super.handleMessage(msg);
		}
	};

	TimerTask task = new TimerTask() {
		public void run() {
			Log.i("===", "timer");
			if (application.m_bleTool.isOpened()) {
				application.m_bleTool.service_init(m_bleServiceCallBack);
				m_listInfo.clear();
				Message message = new Message();
				message.what = MESSAGE_SCAN;
				m_handler.sendMessage(message);
				timer.cancel(); // 闁拷閸戦缚顓搁弮璺烘珤
			}
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
		
		initAPPConn();

		// bt
		Log.d(LOGTAG, "Open bluetooth and start scan");
		application.scanIsDevice = 0;
		int ret = openBluetooth();

		timer = new Timer(true);
		timer.schedule(task, 1000, 500); // 瀵よ埖妞�1000ms閸氬孩澧界悰宀嬬礉1000ms閹笛嗩攽娑擄拷濞嗭拷
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
		mySharedSetting = new SharedSetting(DeviceListActivity.this);
//		if (unregisterReceiverFlag == false) {
//			unregisterReceiverFlag = true;
//			application.m_bleTool.registerReceiver();
//		}

		if (isFirstStart == false) {
			goneProShowbtn();
			application.scanButtionClickTimes = 0;
		}
		isFirstStart = false;

		if (application.isDisconnUnexpecedly == true) {
			application.isDisconnUnexpecedly = false;
			reConneStatus = false;
			application.reConnStatusNum = 0;
			Message message = Message.obtain();
			message.what = MESSAGE_CONNECT;
			m_handler.sendMessage(message);
		}
		
		application.curActivity = application.DEVICE;
		super.onResume();
	}

	@Override
	public void onPause() {
		Log.d(LOGTAG, "device list activity pause");
		if (application.m_bleTool != null) {
			Log.i("===", "device list activity pause and stop scan");
			application.m_bleTool.stopScan();
		}

		super.onPause();
	}

	@Override
	public void onStop() {
		Log.d(LOGTAG, "device list activity stop");

//		if (unregisterReceiverFlag) {
//			if (mbluetoothService != null) {
//				Log.i("===", "unregisterReceiver");
//				application.m_bleTool.unregisterReceiver();
//				unregisterReceiverFlag = false;
//			} else {
//				Log.i("===", "no unregisterReceiver");
//			}
//		}
		// application.m_bleTool.stopScan();

		super.onStop();
	}

	@Override
	public void onDestroy() {
		Log.d(LOGTAG, "device list activity destory");

		if (mbluetoothService != null) {
			Log.i(LOGTAG, "unbind");
			application.m_bleTool.disconnect();
			application.m_bleTool.unbindService();
			application.m_bleTool.unregisterReceiver();
		}

		super.onDestroy();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		Log.d(LOGTAG, "device list activity configuration changed");

		super.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (0 == event.getRepeatCount()) {
				Bundle bundle = new Bundle();
				if (application.macBleModule != null) {
					bundle.putString("mac", application.macBleModule);
				}

				Intent intent = new Intent();
				intent.putExtras(bundle);
				intent.setClass(DeviceListActivity.this,
						OperationCenterActivity.class);
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
		mProgress = (ProgressBar) findViewById(R.id.myView_ProgressBar3);
		mProgress.setVisibility(View.GONE);

		m_btnScan = (Button) findViewById(R.id.button_scan);
		m_btnScan.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// clear
				m_listInfo.clear();
				application.scanIsDevice = 0;
				updateBluetoothDevList();

				// scan bt
				// m_listInfo.clear();
				application.m_bleTool.stopScan();
				if (application.connectStatus == true) {
					application.connectStatus = false;
					if (mbluetoothService != null) {
						application.m_bleTool.disconnect();
					}
				}

				startScanBluetoothDev();

			}
		});

		ImageButton backImageButton = (ImageButton) findViewById(R.id.imagebutton_back);
		backImageButton.getBackground().setAlpha(0);
		backImageButton.setOnClickListener(new BtnBackOnClick());

		m_btnBack = (Button) findViewById(R.id.button_back);
		m_btnBack.setOnClickListener(new BtnBackOnClick());

		m_listInfo = new ArrayList<HashMap<String, Object>>();
		m_listviewDev = (ListView) findViewById(R.id.listView_dev);
		m_itemSimAdapter = new SimpleAdapter(this, m_listInfo,
				R.layout.listview_item_devinfo, new String[] { ObjectName,
						ObjectDetail, ObjectStatus }, new int[] {
						R.id.textView_devname, R.id.textView_devinfo,
						R.id.devstatus });

		m_listviewDev.setAdapter(m_itemSimAdapter);
		m_listviewDev.setOnItemClickListener(new ListOnItemClickListener());
	}

	private class BtnBackOnClick implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			Bundle bundle = new Bundle();
			Log.i("===", "send bundle to Oper");

			if (application.macBleModule != null) {
				Log.i("===1", "send bundle to Oper");

				bundle.putString("mac", application.macBleModule);
			}

			Intent intent = new Intent();
			intent.putExtras(bundle);
			intent.setClass(DeviceListActivity.this,
					OperationCenterActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
			Log.d(LOGTAG, "start intent");
			startActivity(intent);
		}

	}

	private final class ListOnItemClickListener implements
			AdapterView.OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			// TODO: click, then connect corresponding device
			reConneStatus = false;
			application.reConnStatusNum = 0;
			application.isDisconnUnexpecedly = false;

			curListviewId = position;
			// application.m_bleTool.stopScan();
			application.macBleModule = m_listInfo.get(curListviewId)
					.get(ObjectDetail).toString();
			Log.i("===", "current listview mac:" + application.macBleModule);

			if (application.connectStatus == true) {
				application.connectStatus = false;
				if (application.m_bleTool.getBleService() != null) {
					application.m_bleTool.disconnect(); // disconnect
				}
				mySharedSetting.saveLastConndevice(null);
				String test = mySharedSetting.queryLastConndevice();
				m_listInfo.get(curListviewId).put(ObjectStatus,
						getResources().getString(lvConnectStaNot));
				// send message
				Message message = Message.obtain();
				message.what = MESSAGE_UPDATELIST;
				m_handler.sendMessage(message);
			} else {
				/* clear all listview status */
				int cnt = m_listInfo.size();
				if (cnt != 0) {
					for (int i = 0; i < cnt; i++) {
						m_listInfo.get(i).put(ObjectStatus,
								getResources().getString(lvConnectStaNot));
					}
				}
				application.m_bleTool.connect(application.macBleModule,
						m_bleConnectCallBack);
				curConnectDeviceMac = application.macBleModule;
				m_listInfo.get(curListviewId).put(ObjectStatus,
						getResources().getString(lvConnectStaDoing));

				// send message
				Message message = Message.obtain();
				message.what = MESSAGE_UPDATELIST;
				m_handler.sendMessage(message);
			}
		}
	}

	private void updateBluetoothDevList() {
		m_itemSimAdapter.notifyDataSetChanged();
	}

	/* BLE codes */

	private int openBluetooth() {
		openRet = false;
		application.m_bleTool = new BleTool(DeviceListActivity.this);
		return application.m_bleTool.openBle();
	}

	private int startScanBluetoothDev() {
		Log.i("===", "start sacn fun");
		if (haveScanned == false) {
			application.scanButtionClickTimes++;
		}
		if (mbluetoothService != null) {
			// Log.i(LOGTAG, "disconnect");
			// application.m_bleTool.disconnect();
			// application.m_bleTool.unregisterReceiver();
			// application.m_bleTool.unbindService();
		} else {
			Log.i(LOGTAG, "mbluetoothService is null .no disconnect");
		}
		if (haveScanned == false) {
			firtStartScan();
			// application.m_bleTool.startScan(m_BleScanCallback, 1*1000);
		} else {
			application.m_bleTool.startScan(m_BleScanCallback, SCANTIME * 1000);
			Log.i("===", "startCustomerProgress");
			startCustomerProgress();
			m_btnScan.setVisibility(View.INVISIBLE);
			haveScanned = true;
		}

		return 0;
	}

	private void firtStartScan() {
		Log.i("===", "first scan");
		application.m_bleTool.firstStartScan();

		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				Log.i("===", "haveScanned:" + haveScanned);
				if (haveScanned == false) {
					application.m_bleTool.stopScan();
					haveScanned = true;
					startScanBluetoothDev();

				}
			}
		}, 1 * 1000);
	}

	private int reScanConn() {
		application.reConnStatusNum++;
		Log.i("===", "rescan:" + application.reConnStatusNum + "times");

		reConneStatus = true;
		application.m_bleTool.stopScan();
		application.m_bleTool.reStartScan();

		return 0;
	}

	private LRHealthApp.ConnectCallback m_connCallback = new LRHealthApp.ConnectCallback() {

		@Override
		public void connCallback() {
			// TODO Auto-generated method stub
			application.isDisconnUnexpecedly = false;
			reConneStatus = false;
			application.reConnStatusNum = 0;
			Message message = Message.obtain();
			message.what = MESSAGE_CONNECT;
			m_handler.sendMessage(message);
		}
	};

	private void initAPPConn() {
		application.initConn(m_connCallback);
	}

	/* three callback funciton of BleTool */
	private BleTool.BleScanCallBack m_BleScanCallback = new BleTool.BleScanCallBack() {
		@Override
		public void scanListening(BluetoothDevice device) {
			// TODO Auto-generated method stub
			// android.util.Log.d(LOGTAG, device.getAddress());

			/* must put this if before checkIsExit() */
			if (reConneStatus == true) {
				if (device.getAddress().toString().equals(curConnectDeviceMac)) {
					Log.i("===", "re-connect");
					application.m_bleTool.stopScan();
					// m_listInfo.get(curListviewId).put(ObjectStatus,
					// getResources().getString(lvConnectStaReDoing));
					// Message message = Message.obtain();
					// message.what = MESSAGE_UPDATELIST;
					// m_handler.sendMessage(message);
					application.m_bleTool.connect(curConnectDeviceMac,
							m_bleConnectCallBack);
				}

			}

			if (checkIsExist(device) == true) {
				return;
			}
			HashMap<String, Object> map;
			map = new HashMap<String, Object>();
			if (device.getName().length() >= 3
					&& device.getName().contains("BLE")) {
				application.scanIsDevice = 1;
				map.put(ObjectName,
						getResources().getString(R.string.devicename));
			} else {
				return;// only search BLE device

				// map.put(ObjectName, device.getName());
				// Log.i("===", "non-ble device");
			}
			// Log.i("===", "2" + device.getName());
			map.put(ObjectDetail, device.getAddress());
			map.put(ObjectStatus, getResources().getString(lvConnectStaNot));
			m_listInfo.add(map);
			// send message
			Message message = Message.obtain();
			message.what = MESSAGE_UPDATELIST;
			m_handler.sendMessage(message);

			// if (device.getName().equalsIgnoreCase(nameBleModule)) {
			application.macBleModule = device.getAddress();
			android.util.Log.d(LOGTAG, "finded " + application.macBleModule);
			// application.m_bleTool.service_init(m_bleServiceCallBack); //TODO
			// 0
			// }

			Message message1 = Message.obtain();
			message1.what = MESSAGE_FINDDEVICE;
			m_handler.sendMessage(message1);

			/* only run once when first open app */

			if (isFirstScan == true) {
				String lastMac = mySharedSetting.queryLastConndevice();
				if (lastMac != null) {
					if (device.getAddress().toString().equals(lastMac)) {
						reConneStatus = false;
						application.reConnStatusNum = 0;
						isFirstScan = false;
						curConnectDeviceMac = lastMac;
						application.macBleModule = lastMac;
						application.m_bleTool.stopScan();
						curListviewId = m_listInfo.size() - 1;
						m_listInfo.get(curListviewId).put(ObjectStatus,
								getResources().getString(lvConnectStaDoing));

						// send message
						Message message2 = Message.obtain();
						message2.what = MESSAGE_UPDATELIST;
						m_handler.sendMessage(message2);
						application.m_bleTool.connect(lastMac,
								m_bleConnectCallBack);

					}
				} else {
					isFirstScan = false;
				}
			}

		}

		private boolean checkIsExist(BluetoothDevice device) {
			for (HashMap<String, Object> tmp : m_listInfo) {
				// Log.i("===", "compare" + tmp.get(ObjectDetail).toString() +
				// " | " + device.getAddress());
				if (tmp.get(ObjectDetail).equals(device.getAddress())) {
					Log.i("===", "return ture checkIsExit");
					return true;
				}
			}
			return false;
		}

		@Override
		public void scanNoDevice() {
			Log.i("===", "no device message");
			Message message = Message.obtain();
			message.what = MESSAGE_NODEVICE;
			m_handler.sendMessage(message);
			goneProShowbtn();
		}
	};

	private BleTool.BleServiceCallBack m_bleServiceCallBack = new BleTool.BleServiceCallBack() {
		@Override
		public void onBuild() {
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
			Log.i("===", "onConnectFailed");
			// re-connect some times or do nothing
			if (application.reConnStatusNum < 2) {
				reScanConn();
			} else {// only after re-connect, can show failed. Or still show
					// connectting.
				m_listInfo.get(curListviewId).put(ObjectStatus,
						getResources().getString(lvConnectStaNot));
				// send message
				Message message = Message.obtain();
				message.what = MESSAGE_UPDATELIST;
				m_handler.sendMessage(message);
			}
			application.connectStatus = false;

		}

		@Override
		public void onDisconnect() {
			// TODO Auto-generated method stub
			Log.i("===", "onDisconnect");
			// re-connect some times or do nothing
			if (application.reConnStatusNum >= 2
					&& application.connectStatus == false) {// only after
															// re-connect, can
				// show failed. Or still
				// show connectting.
				m_listInfo.get(curListviewId).put(ObjectStatus,
						getResources().getString(lvConnectStaNot));

				// send message
				Message message = Message.obtain();
				message.what = MESSAGE_UPDATELIST;
				m_handler.sendMessage(message);
				application.connectStatus = false;
			}
			if (application.connectStatus == true) {
				application.isDisconnUnexpecedly = true;
				m_listInfo.get(curListviewId).put(ObjectStatus,
						getResources().getString(lvConnectStaNot));

				// send message
				Message message = Message.obtain();
				message.what = MESSAGE_UPDATELIST;
				m_handler.sendMessage(message);
				application.connectStatus = false;
			}
		}

		public void onConnectTimeout() {
			Log.i("***", "time out send message");
			if (application.reConnStatusNum < 2) {
				application.reConnStatusNum = 1; // re-conn once
				// application.m_bleTool.unregisterReceiver();
				application.m_bleTool.registerReceiver();

				application.m_bleTool.disconnect();
				application.m_bleTool.service_init(m_bleServiceCallBack);
				mHandler.postDelayed(new Runnable() {
					@Override
					public void run() {
						reScanConn();
					}
				}, 2 * 100);
			} else {
				// application.reConnStatusNum = 2;// stop re-conn
				reConneStatus = false;
				m_listInfo.get(curListviewId).put(ObjectStatus,
						getResources().getString(lvConnectStaNot));

				// send message
				Message message = Message.obtain();
				message.what = MESSAGE_UPDATELIST;
				m_handler.sendMessage(message);
				application.connectStatus = false;

				// startScanBluetoothDev();
			}
		}
	};

	/* progress */
	private void goneProShowbtn() {
		mProgress.setVisibility(View.GONE);
		m_btnScan.setVisibility(View.VISIBLE);
	}

	/* customer progress */
	void startCustomerProgress() {
		mProgress.setVisibility(View.VISIBLE);
	}
}
