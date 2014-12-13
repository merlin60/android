package com.jonma.lrhealth;

import com.jinoux.android.bledatawarehouse.BluetoothService;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainMenuActivity extends Activity {

	private static Button m_btnVent;
	private static Button m_btnRefri;
	private static Button m_btnHeat;
	private static Button m_btnSetting;
	private static Button m_btnBack;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

		setContentView(R.layout.activity_mainmenu);

		

		// init view
		initView();
	}

	

	@Override
	public void onResume() {

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
		m_btnVent = (Button) findViewById(R.id.button_vent);
		m_btnVent.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Bundle bundle = new Bundle();
				bundle.putInt("FunIdx", 1);

				Intent intent = new Intent();
				intent.putExtras(bundle);
				intent.setClass(MainMenuActivity.this,
						OperationCenterActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
						| Intent.FLAG_ACTIVITY_SINGLE_TOP);
				startActivity(intent);

			}
		});

		m_btnRefri = (Button) findViewById(R.id.button_refri);
		m_btnRefri.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Bundle bundle = new Bundle();
				bundle.putInt("FunIdx", 2);

				Intent intent = new Intent();
				intent.putExtras(bundle);
				intent.setClass(MainMenuActivity.this,
						OperationCenterActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
						| Intent.FLAG_ACTIVITY_SINGLE_TOP);
				startActivity(intent);
			}
		});

		m_btnHeat = (Button) findViewById(R.id.button_heat);
		m_btnHeat.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Bundle bundle = new Bundle();
				bundle.putInt("FunIdx", 3);

				Intent intent = new Intent();
				intent.putExtras(bundle);
				intent.setClass(MainMenuActivity.this,
						OperationCenterActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
						| Intent.FLAG_ACTIVITY_SINGLE_TOP);
				startActivity(intent);
			}
		});

		m_btnSetting = (Button) findViewById(R.id.button_setting);
		m_btnSetting.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Bundle bundle = new Bundle();
				bundle.putInt("FunIdx", 4);

				Intent intent = new Intent();
				intent.putExtras(bundle);
				intent.setClass(MainMenuActivity.this,
						OperationCenterActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
						| Intent.FLAG_ACTIVITY_SINGLE_TOP);
				startActivity(intent);
			}
		});

		m_btnBack = (Button) findViewById(R.id.button_back);
		m_btnBack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});

	}

}
