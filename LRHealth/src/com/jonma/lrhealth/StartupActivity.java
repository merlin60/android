package com.jonma.lrhealth;

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

public class StartupActivity extends Activity {	
	private static final int MESSAGE_GOTOOPERATION = 0x1000;

	private static Button m_btnVent;
	private static Button m_btnRefri;
	private static Button m_btnHeat;
	private static Button m_btnSetting;
	private static Button m_btnBack;
	
	private Handler m_handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_GOTOOPERATION: 
			{
				startActivity(new Intent(getApplication(),OperationCenterActivity.class));  
				StartupActivity.this.finish(); 
			}
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

		setContentView(R.layout.activity_startup);	
		
		LRHealthApp application = LRHealthApp.getInstance();
		application.addActivity(this);

		// init view
		initView();
		
		new  Thread(new Runnable() {
			public void run() {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				Message message = Message.obtain();
				message.what = MESSAGE_GOTOOPERATION;
				m_handler.sendMessage(message);
			}			
			
		}).run();
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
		
	}

}
