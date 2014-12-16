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

	private String address;
	private String macBleModule;// 00:1B:35:0B:5E:42
	private final static String nameBleModule = "BLE0102C2P";
	public static boolean connectstate = false; // 连接匹配状态（false:未开始连接）

	private static int yyd = 0; // 已返回序号
	private static int sendxhid = 0; // 每次点击发送按钮 发送的数据的序号0--255 每发送一次加一， 清空数据时为0
	private static int sss = 0; // 未应答个数 最多五个未应答
	private static int nm = 0; // 发送数据成功次数
	public static boolean senddatastate = false; // 是否开始发送自定义数据 false:未开始

	public BluetoothAdapter bluetoothAdapter;

	public BluetoothService mbluetoothService;

	private final BroadcastReceiver m_bdreceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {

			String action = intent.getAction();
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				// add to array
				BluetoothDevice device = intent
						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
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
			}
		}
	};

	private Handler m_handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_UPDATELIST: {
				updateBluetoothDevList();
			}
				break;
			case MESSAGE_CONNECT:
				//connect();
				break;
			case MESSAGE_CONNECTED:
				Log.d("===", "connected");
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

		// config bt
		configBluetoothDev();

		// scan bt
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
		// register receiver
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		// registerReceiver(m_bdreceiver, filter);
		
		SharedSetting mySharedSetting = new SharedSetting(DeviceListActivity.this);

		super.onResume();
	}

	@Override
	public void onPause() {
		// unregister receiver
		// unregisterReceiver(m_bdreceiver);

		super.onPause();
	}

	@Override
	public void onStop() {

		super.onStop();
	}

	@Override
	public void onDestroy() {
		stopScanBluetoothDev();
//		unregisterReceiver(mGattUpdateReceiver);
//		unbindService(mServiceConnection);

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
				startScanBluetoothDev();
			}
		});

		m_btnBack = (Button) findViewById(R.id.button_back);
		m_btnBack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(DeviceListActivity.this,
						OperationCenterActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
						| Intent.FLAG_ACTIVITY_SINGLE_TOP);
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

			Bundle bundle = new Bundle();
			bundle.putInt("FunIdx", 0);
//			if (macBleModule != null) {
//				bundle.putString("mac", macBleModule);
//			}

			//if connectted, start activity. if not, do nothing
			Intent intent = new Intent();
			intent.putExtras(bundle);
			intent.setClass(DeviceListActivity.this,
					OperationCenterActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
					| Intent.FLAG_ACTIVITY_SINGLE_TOP);
			startActivity(intent);
		}
	}

	private void updateBluetoothDevList() {
		m_itemSimAdapter.notifyDataSetChanged();
	}

	private int configBluetoothDev() {
		BluetoothManager bluetoothManager = (BluetoothManager) this
				.getSystemService(Context.BLUETOOTH_SERVICE);
		bluetoothAdapter = bluetoothManager.getAdapter();
		if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
			BluetoothAdapter.getDefaultAdapter().enable();
		}

		return 0;
	}

	private int startScanBluetoothDev() {
		if (bluetoothAdapter == null)
			return -1;
		bluetoothAdapter.startLeScan(mLeScanCallback);
		return 0;
	}

	// Device scan callback.搜索到设备则执行
	private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
		@Override
		public void onLeScan(final BluetoothDevice device, int rssi,
				byte[] scanRecord) {
			boolean flag = true;
			android.util.Log.d("===", device.getAddress());

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
				android.util.Log.d("===", "finded " + macBleModule);
				//service_init();

			}
		}
	};

	private int stopScanBluetoothDev() {
		if (bluetoothAdapter == null)
			return -1;

		bluetoothAdapter.stopLeScan(mLeScanCallback);

		return 0;
	}

	private void connect() {
		// TODO Auto-generated method stub
		mbluetoothService.gethandler(deviceHandler);
		mbluetoothService.connect(macBleModule);

		service_init();
	}

	@SuppressLint("InlinedApi")
	private void service_init() {
		Intent gattServiceIntent = new Intent(this, BluetoothService.class);
		boolean bll = bindService(gattServiceIntent, mServiceConnection,
				BIND_AUTO_CREATE);
		if (bll) {
			Log.i("===", "绑定服务gattServiceIntent成功");
		} else {
			Log.i("===", "绑定服务gattServiceIntent失败");
		}
		registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
	}

	private static IntentFilter makeGattUpdateIntentFilter() {
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BluetoothService.ACTION_GATT_DISCONNECTED);
		intentFilter.addAction(BluetoothService.ACTION_GATT_CONNECTED);
		intentFilter
				.addAction(BluetoothService.ACTION_GATT_READCHARACTERISTICSUCCESS);
		return intentFilter;
	}

	private final ServiceConnection mServiceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName componentName,
				IBinder service) {
			mbluetoothService = ((BluetoothService.LocalBinder) service)
					.getService();
			if (mbluetoothService == null) {
				Log.d("===", "mbluetoothService is null");

			} else {
				Log.d("===", "mbluetoothService is not null");

				Message message = Message.obtain();
				message.what = MESSAGE_CONNECT;
				m_handler.sendMessage(message);
			}
			boolean ba = mbluetoothService.initialize();
			if (!ba) {
				Log.i("===", "Unable to initialize Bluetooth");
			} else {
				Log.i("===", "initialize Bluetooth");
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName componentName) {
			mbluetoothService = null;
		}
	};

	// 接收广播
	private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			Log.i("===", "action = " + action);
			if (BluetoothService.ACTION_GATT_READCHARACTERISTICSUCCESS
					.equals(action)) { // 连接成功 并读取characteristic成功
				// String connecttext = "disconnect";
				// connectButton.setText(connecttext);
				// ConnectProgressBarzt(false);
				connectstate = true;
				Message message = Message.obtain();
				message.what = MESSAGE_CONNECTED;
				m_handler.sendMessage(message);
			} else if (BluetoothService.ACTION_GATT_DISCONNECTED.equals(action)) { // 模块已断开连接
				if (connectstate == true) { // 正在连接中
					// showAlertDialog(
					// "模块已关闭连接，请断开!",
					// getResources().getString(
					// R.string.alertOneButtonTitle), null, 0);
					Log.d("===", "模块已关闭连接，请断开!");
				}
			}
		}
	};

	@SuppressLint("HandlerLeak")
	public Handler deviceHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			// Log.i("what === "+msg.what);
			switch (msg.what) {
			case 0:
				Log.i("===", "连接失败");
				connectstate = false;
				break;
			case 1:
				String str = (String) msg.obj;
				Log.d("===", "received data: " + str);
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

}
