package com.jonma.lrhealth;

import com.jonma.tool.BitmapDecoder;

import android.R.integer;
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
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class StartupActivity extends Activity {	
	private static SharedSetting appSetting;
	private static final int MESSAGE_GOTOOPERATION = 0x1000;
	private final int STARTUP_DISPLAY_LENGHT = 3000;
	
	private static Button m_btnVent;
	private static Button m_btnRefri;
	private static Button m_btnHeat;
	private static Button m_btnSetting;
	private static Button m_btnBack;
	private static ImageView m_imgViewUIStyle;
	private static LinearLayout m_layout;
	
	private static final String LOGTAG = "LRHealth";
	private static int m_nValueUIStyle;
	
	private int m_nScreenW, m_nScreenH;
	private LRHealthApp mApplication;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(LOGTAG, "startup activity create");
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

		setContentView(R.layout.activity_startup);	
		
		//application
		mApplication = LRHealthApp.getInstance();
		mApplication.addActivity(this);
		
		//Shared Preferences
		appSetting = new SharedSetting(this);
		m_nValueUIStyle = appSetting.apiQueryUIStyle();
		
		//screen size
		getScreenSize();	

		// init view
		initView();
		
		// show for some seconds
		new Handler().postDelayed(new Runnable() {
			public void run() {
				startActivity(new Intent(StartupActivity.this,
						OperationCenterActivity.class));
				StartupActivity.this.finish();
				Log.d(LOGTAG, "startup go to dev list");
			}

		}, STARTUP_DISPLAY_LENGHT);
	}	

	@Override
	public void onResume() {
		Log.d(LOGTAG, "startup activity resume");

		super.onResume();
	}

	@Override
	public void onPause() {
		Log.d(LOGTAG, "startup activity pause"); 

		super.onPause();
	}

	@Override
	public void onStop() {
		Log.d(LOGTAG, "startup activity stop"); 
		
		super.onStop();
	}

	@Override
	public void onDestroy() {
		Log.d(LOGTAG, "startup activity destory"); 
		
		super.onDestroy();
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		Log.d(LOGTAG, "startup activity configuration changed"); 
	    
	    super.onConfigurationChanged(newConfig);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) 
	{		
		if (keyCode == KeyEvent.KEYCODE_BACK) 
		{
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

	private void initView() 
	{
		m_layout = (LinearLayout) findViewById(R.id.layout_startup);
		
		//add startup picture
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
		m_imgViewUIStyle = new ImageView(this);		
		setViewBackground(m_nValueUIStyle);//set default ui
		
		m_imgViewUIStyle.setLayoutParams(params);		
		m_layout.addView(m_imgViewUIStyle);
	}
	
	private void getScreenSize() 
	{
		// screen width and height
		DisplayMetrics metric = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metric);
		int nScreenW = metric.widthPixels;
		int nScreenH = metric.heightPixels;
		int nScreenDpi = metric.densityDpi;
		float fScreenDensity = metric.density;	
		mApplication.setScreenSize(nScreenW, nScreenH, nScreenDpi, fScreenDensity);
		Log.d(LOGTAG, "screen size:" + "(" + nScreenW + "," + nScreenH	+ ")");
		
		m_nScreenW = nScreenW;
		m_nScreenH = nScreenH;	
	}
	
	private void setViewBackground(int style) 
	{
		int resId;
		if (style == 0)// rain
		{
			resId = R.drawable.icon_bgstyle01;

		} else if (style == 1)// blue
		{
			resId = R.drawable.icon_bgstyle02;

		} else// shadow
		{
			resId = R.drawable.icon_bgstyle03;
		}
		
		Bitmap bitmap = BitmapDecoder.BitmapDecoder(this.getResources(), resId, m_nScreenW, m_nScreenH);			
		m_imgViewUIStyle.setBackground(new BitmapDrawable(this.getResources(), bitmap));
	}
}
