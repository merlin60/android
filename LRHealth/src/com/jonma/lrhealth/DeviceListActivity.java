package com.jonma.lrhealth;

import java.util.ArrayList;
import java.util.HashMap;

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

	private static final int REQUEST_OPEN_BT_CODE = 0x01;

	private static final int MESSAGE_UPDATELIST = 0x1000;
	private static final int MESSAGE_CONNECT = 0x1001;
	private static final int MESSAGE_CONNECTED = 0x1002;
	private static final int MESSAGE_NODEVICE = 0x1003;
	private static final int MESSAGE_FINDDEVICE = 0x1004;

	private static final int STOP_NOTIFIER = 0x2000;
	private static final int THREADING_NOTIFIER = 0x2001;

	private static final int SCANTIME = 15;

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
	private String macBleModule;// 00:1B:35:0B:5E:42
	private final static String nameBleModule = "BLE0102C2P";
	public static boolean connectstate = false; // 閿熸枻鎷烽敓鏂ゆ嫹閿熷鍊栭敓鐣屽厴閿熸枻鎷烽敓鏂ゆ嫹閵堝棴鎷峰畡甯嫹瑜版帪鎷烽敓鏂ゆ嫹閿熷鍊栭敓浠嬵棑閿熷�熷煐閿熸枻鎷烽敓鏂ゆ嫹閿熸枻鎷烽敓濮愬�栭敓鏂ゆ嫹false:閿熸枻鎷烽敓鏂ゆ嫹閿熸枻鎷烽敓鏂ゆ嫹閵堝棴鎷峰畡甯嫹閿熸枻鎷烽敓鏂ゆ嫹閿熸枻鎷烽妶鍡嫹閻戞枻鎷烽敓鏂ゆ嫹閿熷鍎婚敓鏂ゆ嫹閿熸枻鎷烽敓锟�

	private static int yyd = 0; // 閿熸枻鎷烽敓鐣岀玻閿熸枻鎷烽敓濮愬�栭敓鐣屽厴閿熸枻鎷烽敓鏂ゆ嫹閵堝棴鎷烽悜鏂ゆ嫹閿熸枻鎷烽敓濮愬�栭敓鐣屽厴閿熸枻鎷烽柨鐕傛嫹
	private static int sendxhid = 0; // 婵綇鎷烽敓鏂ゆ嫹閿熻姤娼捄顭掓嫹閿熸枻鎷烽敓鐣屽厴閿熸枻鎷烽敓鏂ゆ嫹閵堝棴鎷烽悜鏂ゆ嫹閿熸枻鎷烽敓濮愬�栭敓浠嬫敱閵嗗鎷烽敓鏂ゆ嫹閿熸枻鎷烽敓浠嬫晸閿燂拷
										// 閿熸枻鎷烽敓鏂ゆ嫹閿熷鍊栭敓鐣屽厴閿熸枻鎷烽敓鏂ゆ嫹閻㈢鎷烽敓鏂ゆ嫹閿熺晫鍏橀敓鏂ゆ嫹閿熸枻鎷烽妶鍡嫹閻戞枻鎷烽敓鏂ゆ嫹閿熶粙顥撻敓鏂ゆ嫹閿熸枻鎷烽悜鏂ゆ嫹閿熸枻鎷烽敓濮愬�栭敓鐣屽厴閿熸枻鎷烽柨鐕傛嫹0--255
										// 婵綇鎷烽敓鏂ゆ嫹閿熸枻鎷烽敓濮愬�栭敓鐣屽厴閿熸枻鎷烽敓鏂ゆ嫹閵堝棴鎷烽摎鍌︽嫹閿熸枻鎷烽敓鏂ゆ嫹閺夌偠娉曢妴瀣舵嫹閿熷�熸腹閿熸枻鎷烽敓鏂ゆ嫹閿熸枻鎷烽敓濮愬�栭敓鏂ゆ嫹
										// 閿熸枻鎷烽敓鏂ゆ嫹閿熷鍊栭敓鐣屽厴閿熸枻鎷烽敓鏂ゆ嫹閵堝棴鎷烽悜鏂ゆ嫹閿熸枻鎷烽敓濮愬�栭敓鐣屽厴閿熸枻鎷风紓渚婃嫹閿熸枻鎷烽弬銈嗗0
	private static int sss = 0; // 閿熸枻鎷烽敓鑺ユ償閿熸枻鎷烽敓鏂ゆ嫹閿熸枻鎷烽妶鍡嫹閻戞枻鎷烽敓鏂ゆ嫹閿熷鍊栭敓鐣屽厴閿熸枻鎷烽柨鐕傛嫹
								// 閿熸枻鎷烽敓鏂ゆ嫹閿熷鍊栭敓鐣屽厴閿熸枻鎷烽敓鏂ゆ嫹閵堝棴鎷烽悜鏂ゆ嫹閿熸枻鎷烽敓濮愬�栭敓浠嬫敱閿熸枻鎷烽幖杈炬嫹閿熸枻鎷烽敓鏂ゆ嫹閿熷鍊栭敓鏂ゆ嫹
	private static int nm = 0; // 閿熸枻鎷烽敓鏂ゆ嫹閿熷鍊栭敓鐣屽厴閿熸枻鎷烽敓鏂ゆ嫹閵堝棴鎷烽悜鏂ゆ嫹閿熸枻鎷烽敓濮愬�栭敓鐣屽厴閿熸枻鎷烽敓鏂ゆ嫹闁垮鎷烽敓鏂ゆ嫹閿熻棄鎲￠敓鐣屽厴閿熸枻鎷烽敓鏂ゆ嫹閵堝棴鎷烽悜鏂ゆ嫹閿熸枻鎷烽敓濮愬�栭敓鏂ゆ嫹
	public static boolean senddatastate = false; // 閿熸枻鎷烽敓鐣屾喆閿熸枻鎷烽敓濮愬�栭敓钘夊槻閿熸枻鎷烽敓鏂ゆ嫹閿熸枻鎷烽敓濮愬�栭敓鐣屽厴閿熸枻鎷烽敓鏂ゆ嫹閵堝棴鎷烽悜鏂ゆ嫹閿熸枻鎷烽敓鏂ゆ嫹閻犱浇顫夐敓鐣屽厴閿熸枻鎷烽敓鏂ゆ嫹閵堝棴鎷烽悜鏂ゆ嫹閿熸枻鎷烽敓濮愬�栭敓鐣屽厴閿熸枻鎷烽敓鏂ゆ嫹閵堝棴鎷烽敓锟�
													// false:閿熸枻鎷烽敓鏂ゆ嫹閿熸枻鎷烽敓鏂ゆ嫹閵堝棴鎷峰畡甯嫹閿燂拷

	private BleTool m_bleTool;
	public BluetoothAdapter bluetoothAdapter;
	public BluetoothService mbluetoothService;
	private static final int lvConnectStaSuc = R.string.lvConnectStaSuc;
	private static final int lvConnectStaNot = R.string.lvConnectStaNot;
	private static final int lvConnectStaDoing = R.string.lvConnectStaDoing;

	private boolean unregisterReceiverFlag = true;
	private boolean isFirstStart = true;

	/* scan status */
	private boolean rescanStatus = false;
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
				m_bleTool.stopScan();
				m_bleTool.connect(macBleModule, m_bleConnectCallBack);
				break;
			case MESSAGE_CONNECTED:
				Log.d(LOGTAG, "connected");
				application.connectStatus = true;
				mbluetoothService = m_bleTool.getBleService();
				m_listInfo.get(curListviewId).put(ObjectStatus,
						getResources().getString(lvConnectStaSuc));
				Message message = Message.obtain();
				message.what = MESSAGE_UPDATELIST;
				m_handler.sendMessage(message);
				// m_bleTool.unregisterReceiver()
				break;
			case MESSAGE_NODEVICE:
				goneProShowbtn();
				break;
			case MESSAGE_FINDDEVICE:
				goneProShowbtn();
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

		// bt
		Log.d(LOGTAG, "Open bluetooth and start scan");
		application.scanIsDevice = 0;
		int ret = openBluetooth();
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
		 * // register receiver IntentFilter filter = new
		 * IntentFilter(BluetoothDevice.ACTION_FOUND);
		 * registerReceiver(m_bdreceiver, filter);
		 */

		// if(mbluetoothService == null){
		// m_bleTool.registerReceiver();
		// }

		SharedSetting mySharedSetting = new SharedSetting(
				DeviceListActivity.this);
		if (unregisterReceiverFlag == false) {
			unregisterReceiverFlag = true;
			m_bleTool.registerReceiver();
		}

		if (isFirstStart == false) {
			goneProShowbtn();
			application.scanButtionClickTimes = 0;
		}
		isFirstStart = false;
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

		if (unregisterReceiverFlag) {
			if (mbluetoothService != null) {
				Log.i("===", "unregisterReceiver");
				m_bleTool.unregisterReceiver();
				unregisterReceiverFlag = false;
			} else {
				Log.i("===", "no unregisterReceiver");
			}
		}
		// m_bleTool.stopScan();

		super.onStop();
	}

	@Override
	public void onDestroy() {
		Log.d(LOGTAG, "device list activity destory");

		if (mbluetoothService != null) {
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
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (0 == event.getRepeatCount()) {
				Bundle bundle = new Bundle();
				if (macBleModule != null) {
					bundle.putString("mac", macBleModule);
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
				m_listInfo.clear();
				m_bleTool.stopScan();
				if (application.connectStatus == true) {
					application.connectStatus = false;
					if (mbluetoothService != null) {
						m_bleTool.disconnect();
					}
				}
				// //m_bleTool.unregisterReceiver();
				// m_bleTool.unbindService();
				// }
				
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
			if (macBleModule != null) {
				bundle.putString("mac", macBleModule);
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
			rescanStatus = false;

			curListviewId = position;
			m_bleTool.stopScan();
			macBleModule = m_listInfo.get(curListviewId).get(ObjectDetail)
					.toString();
			Log.i("===", "current listview mac:" + macBleModule);

			if (application.connectStatus == true) {
				application.connectStatus = false;
				if (m_bleTool.getBleService() != null)
					m_bleTool.disconnect(); // disconnect
				m_listInfo.get(curListviewId).put(ObjectStatus,
						getResources().getString(lvConnectStaNot));
				// send message
				Message message = Message.obtain();
				message.what = MESSAGE_UPDATELIST;
				m_handler.sendMessage(message);
			} else {
				// application.connectStatus = true;
				// if(m_bleTool.getBleService() != null) m_bleTool.disconnect();
				// //disconnect
				/* clear all listview status */
				int cnt = m_listInfo.size();
				if (cnt != 0) {
					for (int i = 0; i < cnt; i++) {
						m_listInfo.get(i).put(ObjectStatus,
								getResources().getString(lvConnectStaNot));
					}
				}
				m_bleTool.connect(macBleModule, m_bleConnectCallBack);
				curConnectDeviceMac = macBleModule;
				m_listInfo.get(curListviewId).put(ObjectStatus,
						getResources().getString(lvConnectStaDoing));

				// send message
				Message message = Message.obtain();
				message.what = MESSAGE_UPDATELIST;
				m_handler.sendMessage(message);
			}

			// Bundle bundle = new Bundle();
			// bundle.putInt("FunIdx", 0);
			// if (macBleModule != null) {
			// bundle.putString("mac", macBleModule);
			// }
			//
			// //if connectted, start activity. if not, do nothing
			// Intent intent = new Intent();
			// intent.putExtras(bundle);
			// intent.setClass(DeviceListActivity.this,
			// OperationCenterActivity.class);
			// intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
			// | Intent.FLAG_ACTIVITY_SINGLE_TOP);
			// Log.i(LOGTAG, "start intent in list");
			// startActivity(intent);
		}
	}

	private void updateBluetoothDevList() {
		m_itemSimAdapter.notifyDataSetChanged();
	}

	/* BLE codes */

	private int openBluetooth() {
		m_bleTool = new BleTool(DeviceListActivity.this);
		return m_bleTool.openBle();
	}

	private int startScanBluetoothDev() {
		if (haveScanned == false) {
			application.scanButtionClickTimes++;
		}
		if (mbluetoothService != null) {
			// Log.i(LOGTAG, "disconnect");
			// m_bleTool.disconnect();
			// m_bleTool.unregisterReceiver();
			// m_bleTool.unbindService();
		} else {
			Log.i(LOGTAG, "mbluetoothService is null .no disconnect");
		}
		if (haveScanned == false) {
			firtStartScan();
			// m_bleTool.startScan(m_BleScanCallback, 1*1000);
		} else {
			m_bleTool.startScan(m_BleScanCallback, SCANTIME * 1000);
			Log.i("===", "startCustomerProgress");
			startCustomerProgress();
			m_btnScan.setVisibility(View.INVISIBLE);
			haveScanned = true;
		}

		return 0;
	}

	private void firtStartScan() {
		Log.i("===", "first scan");
		m_bleTool.firstStartScan();

		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				Log.i("===", "haveScanned:" + haveScanned);
				if (haveScanned == false) {
					m_bleTool.stopScan();
					haveScanned = true;
					startScanBluetoothDev();

				}
			}
		}, 1 * 1000);
	}

	private int reScanConn() {
		Log.i("===", "rescan");
		rescanStatus = true;
		m_bleTool.stopScan();
		m_bleTool.reStartScan();

		return 0;
	}

	/* three callback funciton of BleTool */

	private BleTool.BleScanCallBack m_BleScanCallback = new BleTool.BleScanCallBack() {
		@Override
		public void scanListening(BluetoothDevice device) {
			// TODO Auto-generated method stub
			application.scanIsDevice = 1;
			android.util.Log.d(LOGTAG, device.getAddress());

			/* must put this if before checkIsExit() */
			if (rescanStatus == true) {
				if (device.getAddress().toString().equals(curConnectDeviceMac)) {
					Log.i("===", "re-connect");
					m_bleTool.stopScan();
					m_bleTool
							.connect(curConnectDeviceMac, m_bleConnectCallBack);
				}

			}

			if (checkIsExist(device) == true) {
				return;
			}
			HashMap<String, Object> map;
			map = new HashMap<String, Object>();
			if (device.getName().length() >= 3
					//&& device.getName().substring(0, 3).equals("BLE")) {
					&& device.getName().contains("BLE0202") ) {
				//Log.i("&&&", device.getName());
				map.put(ObjectName,
						getResources().getString(R.string.devicename));
			} else {
				map.put(ObjectName, device.getName());
			}
		//	Log.i("===", "2" + device.getName());
			map.put(ObjectDetail, device.getAddress());
			map.put(ObjectStatus, getResources().getString(lvConnectStaNot));
			m_listInfo.add(map);

			// send message
			Message message = Message.obtain();
			message.what = MESSAGE_UPDATELIST;
			m_handler.sendMessage(message);

			// if (device.getName().equalsIgnoreCase(nameBleModule)) {
			macBleModule = device.getAddress();
			android.util.Log.d(LOGTAG, "finded " + macBleModule);
			m_bleTool.service_init(m_bleServiceCallBack);
			// }

			Message message1 = Message.obtain();
			message1.what = MESSAGE_FINDDEVICE;
			m_handler.sendMessage(message1);

		}

		private boolean checkIsExist(BluetoothDevice device) {
			for (HashMap<String, Object> tmp : m_listInfo) {
//				Log.i("###", "compare" + tmp.get(ObjectDetail).toString()
//						+ " | " + device.getAddress());
				String test = tmp.get(ObjectDetail).toString();
				//if (tmp.get(ObjectDetail).toString() == device.getAddress()) {
				if (test.equals(device.getAddress()) ) {
//					Log.i("@@@",
//							"return ture checkIsExit" + device.getAddress());
					//Log.i("===", "true");
					return true;
				}
			}
			//Log.i("===", "false");
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
			// when in this funciton, it indicate service has been created
			// successfully, then can connect ble device

			// Message message = Message.obtain();
			// message.what = MESSAGE_CONNECT;
			// m_handler.sendMessage(message);
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
			m_listInfo.get(curListviewId).put(ObjectStatus,
					getResources().getString(lvConnectStaNot));
			if (rescanStatus == false) {
				reScanConn();
			} else {
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
			m_listInfo.get(curListviewId).put(ObjectStatus,
					getResources().getString(lvConnectStaNot));

			// send message
			Message message = Message.obtain();
			message.what = MESSAGE_UPDATELIST;
			m_handler.sendMessage(message);

			application.connectStatus = false;

		}
	};

	/* progress */

	private void goneProShowbtn() {
		// pro.setVisibility(View.GONE);
		// stopProgressDialog();
		mProgress.setVisibility(View.GONE);
		m_btnScan.setVisibility(View.VISIBLE);
	}

	/* customer progress */
	void startCustomerProgress() {
		// RefreshTask task = new RefreshTask(this);
		// task.execute("");

		// Drawable d = this.getResources().getDrawable(R.drawable.my_progress);
		// mProgress.setProgressDrawable(d);

		// setContentView(R.layout.customprogressdialog);
		// mProgress.getWindow().getAttributes().gravity = Gravity.RIGHT |
		// Gravity.TOP;

		mProgress.setVisibility(View.VISIBLE);
	}

	// class RefreshTask extends AsyncTask<String, Integer, String> {
	// public RefreshTask(Context context) {
	//
	// }
	//
	// @Override
	// protected String doInBackground(String... params) {
	// // en_manual_update = 6;
	// // startUpdateTimer();//TODO: need test
	// // while(en_manual_update > 0);
	// return null;
	// }
	//
	// @Override
	// protected void onCancelled() {
	// stopProgressDialog();
	// super.onCancelled();
	// }
	//
	// @Override
	// protected void onPostExecute(String result) {
	// // stopProgressDialog();
	// }
	//
	// @Override
	// protected void onPreExecute() {
	// startProgressDialog();
	// }
	//
	// @Override
	// protected void onProgressUpdate(Integer... values) {
	//
	// }
	// }
	//
	// private void startProgressDialog() {
	// if (progressDialog == null) {
	// progressDialog = CustomProgressDialog.createDialog(this);
	// // progressDialog.setMessage("Refresh...");
	// }
	//
	// progressDialog.show();
	// }
	//
	// private void stopProgressDialog() {
	// if (progressDialog != null) {
	// progressDialog.dismiss();
	// progressDialog = null;
	// }
	// }

}
