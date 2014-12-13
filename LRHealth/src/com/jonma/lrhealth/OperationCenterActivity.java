package com.jonma.lrhealth;

import com.jinoux.android.bledatawarehouse.BluetoothService;
import com.jinoux.android.bledatawarehouse.SampleGattAttributes;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;

public class OperationCenterActivity extends Activity {
	private static View m_layoutViewSelf;// self view
	private static View m_layoutViewMenu;// 0
	private static View m_layoutViewVent;// 1
	private static View m_layoutViewRefri;// 2
	private static View m_layoutViewHeat;// 3
	private static View m_layoutViewSetting;// 4

	// menu panel
	private static Button m_btnMenuVent;
	private static Button m_btnMenuRefri;
	private static Button m_btnMenuHeat;
	private static Button m_btnMenuSetting;
	private static Button m_btnMenuBack;

	// setting panel
	private static Button m_btnVentBack;
	private static Button m_btnRefriBack;
	private static Button m_btnHeatBack;
	private static Button m_btnSettingBack;

	private static Button m_btnFunVent;
	private static Button m_btnFunRefri;
	private static Button m_btnFunHeat;

	private TranslateAnimation m_animationShow;
	private TranslateAnimation m_animationHide;
	private TranslateAnimation m_animationPullUp;
	private TranslateAnimation m_animationPullDown;

	public BluetoothService mbluetoothService;

	private String address;
	private String macBleModule;// 00:1B:35:0B:5E:42
	private final static String nameBleModule = "BLE0102C2P";
	public static boolean connectstate = false; // 连接匹配状态（false:未开始连接）

	private static int yyd = 0; // 已返回序号
	private static int sendxhid = 0; // 每次点击发送按钮 发送的数据的序号0--255 每发送一次加一， 清空数据时为0
	private static int sss = 0; // 未应答个数 最多五个未应答
	private static int nm = 0; // 发送数据成功次数
	public static boolean senddatastate = false; // 是否开始发送自定义数据 false:未开始

	private static int m_nViewType = 0;
	private static int m_nValueVent, m_nValueRefri, m_nValueHeat,
			m_nValueUIStyle, m_nValueLanType;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

		setContentView(R.layout.activity_operationcenter);

		// get bundle
		Bundle bundle = getIntent().getExtras();
		if (null != bundle)
			m_nViewType = bundle.getInt("FunIdx");

//		macBleModule = bundle.getString("mac");
//		Log.d("===", "main menu:" + macBleModule);
//		if (macBleModule == null) {
//			finish();
//		}
		
		LRHealthApp application = (LRHealthApp)getApplication();
		mbluetoothService = application.mbluetoothService;

		// init value
		m_nValueVent = 0;
		m_nValueRefri = 1;
		m_nValueHeat = 1;
		m_nValueUIStyle = 0;
		m_nValueLanType = 0;

		// init view
		initView();
		initTranslateAnimation();
		switchViewType(0, m_nViewType);
	}

	@Override
	public void onResume() {
		SharedSetting mySharedSetting = new SharedSetting(OperationCenterActivity.this);
		super.onResume();
	}

	@Override
	public void onPause() {

		super.onPause();
	}

	@Override
	public void onStop() {

		super.onStop();
	}

	@Override
	public void onDestroy() {
		
		super.onDestroy();
	}

	

	private void initView() {
		// menu
		m_btnMenuVent = (Button) findViewById(R.id.button_menuvent);
		m_btnMenuVent.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (m_nViewType != 0)
					return;

				switchViewType(0, 1);
				m_nViewType = 1;
				startWind();
			}
		});

		m_btnMenuRefri = (Button) findViewById(R.id.button_menurefri);
		m_btnMenuRefri.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (m_nViewType != 0)
					return;

				switchViewType(0, 2);
				m_nViewType = 2;
			}
		});

		m_btnMenuHeat = (Button) findViewById(R.id.button_menuheat);
		m_btnMenuHeat.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (m_nViewType != 0)
					return;

				switchViewType(0, 3);
				m_nViewType = 3;
			}
		});

		m_btnMenuSetting = (Button) findViewById(R.id.button_menusetting);
		m_btnMenuSetting.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (m_nViewType != 0)
					return;

				switchViewType(0, 4);
				m_nViewType = 4;
			}
		});

		m_btnMenuBack = (Button) findViewById(R.id.button_menuback);
		m_btnMenuBack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				switchViewType(m_nViewType, 0);
				m_nViewType = 0;

				finish();
			}
		});

		// layout view
		m_layoutViewSelf = (View) findViewById(R.id.linelayout_selfview);
		m_layoutViewMenu = (View) findViewById(R.id.linelayout_menu);
		m_layoutViewVent = (View) findViewById(R.id.linelayout_vent);
		m_layoutViewRefri = (View) findViewById(R.id.linelayout_refri);
		m_layoutViewHeat = (View) findViewById(R.id.linelayout_heat);
		m_layoutViewSetting = (View) findViewById(R.id.linelayout_setting);

		// back button on setting panel
		m_btnVentBack = (Button) findViewById(R.id.button_ventback);
		m_btnVentBack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				switchViewType(1, 0);
				m_nViewType = 0;
			}
		});

		m_btnRefriBack = (Button) findViewById(R.id.button_refriback);
		m_btnRefriBack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				switchViewType(2, 0);
				m_nViewType = 0;
			}
		});

		m_btnHeatBack = (Button) findViewById(R.id.button_heatback);
		m_btnHeatBack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				switchViewType(3, 0);
				m_nViewType = 0;
			}
		});

		m_btnSettingBack = (Button) findViewById(R.id.button_settingback);
		m_btnSettingBack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				switchViewType(4, 0);
				m_nViewType = 0;
			}
		});

		// function button on setting panel
		m_btnFunRefri = (Button) findViewById(R.id.button_refri);
		m_btnFunRefri.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				m_nValueRefri++;
				if (m_nValueRefri > 3)
					m_nValueRefri = 1;

				if (m_nValueRefri == 1)
					m_btnFunRefri
							.setBackgroundResource(R.drawable.icon_funrefri01);
				else if (m_nValueRefri == 2)
					m_btnFunRefri
							.setBackgroundResource(R.drawable.icon_funrefri02);
				else
					m_btnFunRefri
							.setBackgroundResource(R.drawable.icon_funrefri03);

			}
		});

		m_btnFunHeat = (Button) findViewById(R.id.button_heat);
		m_btnFunHeat.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				m_nValueHeat++;
				if (m_nValueHeat > 3)
					m_nValueHeat = 1;

				if (m_nValueHeat == 1)
					m_btnFunHeat
							.setBackgroundResource(R.drawable.icon_funheat01);
				else if (m_nValueHeat == 2)
					m_btnFunHeat
							.setBackgroundResource(R.drawable.icon_funheat02);
				else
					m_btnFunHeat
							.setBackgroundResource(R.drawable.icon_funheat03);
			}
		});

	}

	private void initTranslateAnimation() {
		// hide and show
		m_animationShow = new TranslateAnimation(Animation.RELATIVE_TO_SELF,
				0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
				Animation.RELATIVE_TO_SELF, -1.0f, Animation.RELATIVE_TO_SELF,
				0.0f);
		m_animationHide = new TranslateAnimation(Animation.RELATIVE_TO_SELF,
				0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
				Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
				-1.0f);
		m_animationShow.setDuration(500);
		m_animationHide.setDuration(500);

		// pull down/down
		m_animationPullUp = new TranslateAnimation(Animation.RELATIVE_TO_SELF,
				0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
				Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
				0.5f);
		m_animationPullDown = new TranslateAnimation(
				Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
				0.0f, Animation.RELATIVE_TO_SELF, 0.5f,
				Animation.RELATIVE_TO_SELF, 0.0f);
		m_animationPullUp.setDuration(500);
		m_animationPullDown.setDuration(500);
	}

	private void switchViewType(int curtype, int willtype) {
		if ((curtype == 0) && (willtype == 0))// init
		{
			// set view status
			m_layoutViewMenu.setVisibility(View.VISIBLE);
			m_layoutViewVent.setVisibility(View.GONE);
			m_layoutViewRefri.setVisibility(View.GONE);
			m_layoutViewHeat.setVisibility(View.GONE);
			m_layoutViewSetting.setVisibility(View.GONE);

			setViewBackground(0, 0);
			return;
		}

		if (willtype == 0)// will show menu
		{
			switch (curtype) {
			case 1: {
				m_layoutViewVent.startAnimation(m_animationHide);
				m_layoutViewVent.setVisibility(View.GONE);
			}
				break;
			case 2: {
				m_layoutViewRefri.startAnimation(m_animationHide);
				m_layoutViewRefri.setVisibility(View.GONE);

			}
				break;
			case 3: {
				m_layoutViewHeat.startAnimation(m_animationHide);
				m_layoutViewHeat.setVisibility(View.GONE);

			}
				break;
			case 4: {
				m_layoutViewSetting.startAnimation(m_animationHide);
				m_layoutViewSetting.setVisibility(View.GONE);
			}
				break;
			default: {

			}
				break;
			}

			m_layoutViewMenu.startAnimation(m_animationPullUp);
			m_layoutViewMenu.setVisibility(View.VISIBLE);

			setViewBackground(0, 0);

		} else// will show function
		{
			switch (willtype) {
			case 1: {
				m_layoutViewVent.startAnimation(m_animationShow);
				m_layoutViewVent.setVisibility(View.VISIBLE);
			}
				break;
			case 2: {
				m_layoutViewRefri.startAnimation(m_animationShow);
				m_layoutViewRefri.setVisibility(View.VISIBLE);

			}
				break;
			case 3: {
				m_layoutViewHeat.startAnimation(m_animationShow);
				m_layoutViewHeat.setVisibility(View.VISIBLE);

			}
				break;
			case 4: {
				m_layoutViewSetting.startAnimation(m_animationShow);
				m_layoutViewSetting.setVisibility(View.VISIBLE);
			}
				break;
			default: {

			}
				break;
			}

			m_layoutViewMenu.startAnimation(m_animationPullDown);
			m_layoutViewMenu.setVisibility(View.VISIBLE);

			setViewBackground(0, 1);
		}
	}

	private void setViewBackground(int style, int type) {
		Drawable imgDrawable;
		if (style == 0)// rain
		{
			if (type == 0)// menu
				imgDrawable = getResources().getDrawable(
						R.drawable.icon_bgstyle00);
			else
				// fun
				imgDrawable = getResources().getDrawable(
						R.drawable.icon_bgstyle01);
		} else if (style == 1)// blue
		{
			if (type == 0)// menu
				imgDrawable = getResources().getDrawable(
						R.drawable.icon_bgstyle02);
			else
				// fun
				imgDrawable = getResources().getDrawable(
						R.drawable.icon_bgstyle02);
		} else// shadow
		{
			if (type == 0)// menu
				imgDrawable = getResources().getDrawable(
						R.drawable.icon_bgstyle03);
			else
				// fun
				imgDrawable = getResources().getDrawable(
						R.drawable.icon_bgstyle03);
		}

		m_layoutViewSelf.setBackgroundDrawable(imgDrawable);
		// m_layoutViewSelf.setBackground(imgDrawable);
	}

	private void startWind() {
		sendDate("01");
	}

	/**
	 * 计算效验和
	 * 
	 * @param buffer
	 * @param length
	 * @return
	 */
	private byte checksum(byte[] buffer, int length) {
		byte nSum;
		nSum = 0;
		for (int i = 0; i < length; i++) {
			nSum += buffer[i];
		}
		return nSum;
	}

	private void sendDate(String sendString) {
		boolean bs = false;
		Log.d("===", (connectstate)?"connectstate is true":"connectstate is falst");
		if (connectstate == true) {
			bs = true;
			if (mbluetoothService.uuidb == 5) {
				if (yyd != mbluetoothService.nm) {
					if (mbluetoothService.nm < 255) {
						sendxhid = mbluetoothService.nm + 1;
					} else {
						sendxhid = 0;
					}
					yyd = mbluetoothService.nm;
					sss = 0;
				}
			} else if (mbluetoothService.uuidb == 8) { // uuid == b358
														// 重新发送返回的序号
				sendxhid = mbluetoothService.nm;
			}
		}else{
			mbluetoothService.connect(macBleModule);
			return;			
		}

		if (bs) {
			if (!sendString.equals("") && sendString.length() <= 18) {
				BluetoothGattCharacteristic characteristic = BluetoothService.SERIAL_PORT_WRITE_Characteristic;
				// byte[] by = Tools.hexStrToStr(sendString); //
				// sendString.getBytes();
				byte[] by = sendString.getBytes();

				Log.d("===", sendString + " | " + by.toString());
				byte[] idByte = new byte[by.length + 2];
				idByte[0] = (byte) sendxhid;
				for (int i = 0; i < by.length; i++) {
					idByte[i + 1] = by[i];
				}
				Log.d("===",
						Integer.toString(by.length) + " | "
								+ Integer.toString(idByte.length));
				idByte[idByte.length - 1] = 0;
				idByte[idByte.length - 1] = checksum(idByte, idByte.length - 1);
				if (idByte == null) {
					Log.d("===", "1");

				}
				if (characteristic == null) {
					Log.d("===", "2");

				}
				characteristic.setValue(idByte);// 设置数据内容
				nm = nm + 1; // 发送数据次数
				/*
				 * ==========================================================
				 * =====
				 */
				boolean bb = mbluetoothService
						.wirteCharacteristic(characteristic);// 往蓝牙模块写入数据
				if (bb) {
					senddatastate = true; // 是否开始发送自定义数据 false:未开始
					SampleGattAttributes.senddatastate = senddatastate;
					if (sendxhid >= 255) { // 发送成功 下一个序号
						sendxhid = 0;
					} else {
						sendxhid = sendxhid + 1;
					}
				} else {
					// addsenddata("发送第 " + nm + " 条数据失败！");
				}
			} else {

			}
		}
	}

	// private void sendDate(int sendInt) {
	// if (mbluetoothService.uuidb == 5) {
	// if (yyd != mbluetoothService.nm) {
	// if (mbluetoothService.nm < 255) {
	// sendxhid = mbluetoothService.nm + 1;
	// } else {
	// sendxhid = 0;
	// }
	// yyd = mbluetoothService.nm;
	// sss = 0;
	// }
	// } else if (mbluetoothService.uuidb == 8) { // uuid == b358
	// // 重新发送返回的序号
	// sendxhid = mbluetoothService.nm;
	// }
	//
	// BluetoothGattCharacteristic characteristic =
	// BluetoothService.SERIAL_PORT_WRITE_Characteristic;
	// byte[] by = sendInt // Tools.hexStrToStr(sendtext);
	// byte[] idByte = new byte[by.length + 2];
	// idByte[0] = (byte) sendxhid;
	// for (int i = 0; i < by.length; i++) {
	// idByte[i + 1] = by[i];
	// }
	// idByte[idByte.length - 1] = 0;
	// idByte[idByte.length - 1] = checksum(idByte,
	// idByte.length - 1);
	// characteristic.setValue(idByte);// 设置数据内容
	// nm = nm + 1; // 发送数据次数
	// /*
	// * ==========================================================
	// * =====
	// */
	// boolean bb = mbluetoothService
	// .wirteCharacteristic(characteristic);// 往蓝牙模块写入数据
	// if (bb) {
	// senddatastate = true; // 是否开始发送自定义数据 false:未开始
	// SampleGattAttributes.senddatastate = senddatastate;
	// if (sendxhid >= 255) { // 发送成功 下一个序号
	// sendxhid = 0;
	// } else {
	// sendxhid = sendxhid + 1;
	// }
	// } else {
	// // addsenddata("发送第 " + nm + " 条数据失败！");
	// }
	//
	// }

}
