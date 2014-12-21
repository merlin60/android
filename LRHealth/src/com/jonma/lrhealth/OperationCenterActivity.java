package com.jonma.lrhealth;import com.jinoux.android.bledatawarehouse.BluetoothService;import com.jinoux.android.bledatawarehouse.SampleGattAttributes;import android.R.integer;import android.annotation.SuppressLint;import android.app.Activity;import android.app.AlertDialog;import android.bluetooth.BluetoothGattCharacteristic;import android.content.BroadcastReceiver;import android.content.ComponentName;import android.content.Context;import android.content.DialogInterface;import android.content.Intent;import android.content.IntentFilter;import android.content.ServiceConnection;import android.content.pm.ActivityInfo;import android.content.res.ColorStateList;import android.content.res.Resources;import android.graphics.Color;import android.graphics.drawable.AnimationDrawable;import android.graphics.drawable.Drawable;import android.os.Bundle;import android.os.Handler;import android.os.IBinder;import android.os.Message;import android.util.Log;import android.view.KeyEvent;import android.view.View;import android.view.Window;import android.view.WindowManager;import android.view.View.OnClickListener;import android.view.animation.Animation;import android.view.animation.TranslateAnimation;import android.widget.Button;import android.widget.ImageView;import android.widget.Toast;public class OperationCenterActivity extends Activity {	private static final int MESSAGE_GOTODEVLIST = 0x2000;		private static final int ANIMATION_TIME = 200;		private static View m_layoutViewSelf;// self view	private static View m_layoutViewMenu;// 0	private static View m_layoutViewVent;// 1	private static View m_layoutViewRefri;// 2	private static View m_layoutViewHeat;// 3	private static View m_layoutViewSetting;// 4	// menu panel	private static Button m_btnMenuVent;	private static Button m_btnMenuRefri;	private static Button m_btnMenuHeat;	private static Button m_btnMenuSetting;	private static Button m_btnMenuBack;	// setting panel	private static Button m_btnVentBack;	private static Button m_btnRefriBack;	private static Button m_btnHeatBack;	private static Button m_btnFunVent;	private static Button m_btnFunRefri;	private static Button m_btnFunHeat;		private static Button m_btnUIStyle00;	private static Button m_btnUIStyle01;	private static Button m_btnUIStyle02;	private static Button m_btnDevManage;			private static AnimationDrawable m_animdrawableRoate;		private static ImageView m_imgviewUIStyle;	private static ImageView m_imgviewVentSelectSt;	private static ImageView m_imgviewRefriSelectSt;	private static ImageView m_imgviewHeatSelectSt;	private static ImageView m_imgviewSettingSelectSt;	private TranslateAnimation m_animationShow;	private TranslateAnimation m_animationHide;	private TranslateAnimation m_animationPullUp;	private TranslateAnimation m_animationPullDown;//	//	//status //	private int m_nValueVent;//0:off 1:on//	private int refriStatus;//the gear//	private int heatStatus;// the gear	public BluetoothService mbluetoothService;	private String address;	private String macBleModule;// 00:1B:35:0B:5E:42	private final static String nameBleModule = "BLE0102C2P";	public static boolean connectstate = false; // 閿熸枻鎷烽敓鏂ゆ嫹鍖归敓鏂ゆ嫹鐘舵�侀敓鏂ゆ嫹false:鏈敓鏂ゆ嫹濮嬮敓鏂ゆ嫹閿熸帴锝忔嫹	private static int yyd = 0; // 閿熺獤鍑ゆ嫹閿熸枻鎷烽敓鏂ゆ嫹閿燂拷	private static int sendxhid = 0; // 姣忛敓杞跨鎷烽敓鏂ゆ嫹閿熸枻鎷锋《閿熻剼锟� 閿熸枻鎷烽敓閰电鎷烽敓鏂ゆ嫹閿熸嵎纰夋嫹閿熸枻鎷烽敓锟�0--255 姣忛敓鏂ゆ嫹閿熸枻鎷蜂竴閿熻娇纭锋嫹涓�閿熸枻鎷� 閿熸枻鎷烽敓鏂ゆ嫹閿熸枻鎷烽敓缁炶斀锟�0	private static int sss = 0; // 鏈簲閿熸枻鎷烽敓鏂ゆ嫹閿燂拷 閿熸枻鎷烽敓鏂ゆ嫹閿熸枻鎷锋湭搴旈敓鏂ゆ嫹	private static int nm = 0; // 閿熸枻鎷烽敓鏂ゆ嫹閿熸枻鎷烽敓鎹锋垚鐧告嫹閿熸枻鎷烽敓鏂ゆ嫹	public static boolean senddatastate = false; // 閿熻鍑ゆ嫹濮嬮敓鏂ゆ嫹閿熸枻鎷烽敓鐨嗚鎷烽敓鏂ゆ嫹閿熸枻鎷烽敓鏂ゆ嫹 false:鏈敓鏂ゆ嫹濮�	private static int m_nViewType = 0;		//status	private static int m_nValueVent, m_nValueRefri, m_nValueHeat, m_nValueLanType, m_nValueUIStyle;	private long m_lPressBackTime;		private Handler m_handler = new Handler() {		public void handleMessage(Message msg) {			switch (msg.what) {			case MESSAGE_GOTODEVLIST: 			{				Intent intent = new Intent();				intent.setClass(OperationCenterActivity.this,						DeviceListActivity.class);				startActivity(intent);			}				break;						default:				break;			}			super.handleMessage(msg);		}	};	@Override	protected void onCreate(Bundle savedInstanceState) 	{		super.onCreate(savedInstanceState);		requestWindowFeature(Window.FEATURE_NO_TITLE);		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,				WindowManager.LayoutParams.FLAG_FULLSCREEN);		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);		setContentView(R.layout.activity_operationcenter);		// get bundle		Bundle bundle = getIntent().getExtras();		if (null != bundle)		{			macBleModule = bundle.getString("mac");			Log.d("===", "main menu:" + macBleModule);			if (macBleModule == null) 			{				//finish();			}				}							LRHealthApp application = LRHealthApp.getInstance();		mbluetoothService = application.mbluetoothService;		application.addActivity(this);		 		// init value		m_nValueVent = 1;		m_nValueRefri = 3;		m_nValueHeat = 3;				m_nValueLanType = 0;				m_nValueUIStyle = 0;		// init view		initView();		initTranslateAnimation();					//show menu				switchViewType(0, 0);		m_nViewType = 0;				new  Thread(new Runnable() {			public void run() {				try {					Thread.sleep(1000);				} catch (InterruptedException e) {					// TODO Auto-generated catch block					e.printStackTrace();				}								Message message = Message.obtain();				message.what = MESSAGE_GOTODEVLIST;				m_handler.sendMessage(message);			}								}).run();			}	@Override	public void onResume() 	{			super.onResume();						SharedSetting mySharedSetting = new SharedSetting(OperationCenterActivity.this);	}	@Override	public void onPause() {		super.onPause();	}	@Override	public void onStop() {		super.onStop();	}	@Override	public void onDestroy() {				super.onDestroy();	}		 	@Override	public boolean onKeyDown(int keyCode, KeyEvent event) 	{		if (keyCode == KeyEvent.KEYCODE_BACK) 		{			if ((System.currentTimeMillis() - m_lPressBackTime) > 2000) 			{				Toast.makeText(this, getResources().getString(R.string.text_pressbackforexit), Toast.LENGTH_SHORT).show();				m_lPressBackTime = System.currentTimeMillis();			} 			else 			{				LRHealthApp.getInstance().exit();				System.exit(0);			}			return true;		}				return super.onKeyDown(keyCode, event);	}		private void initView() {		// menu		m_btnMenuVent = (Button) findViewById(R.id.button_menuvent);		m_btnMenuVent.setOnClickListener(new OnClickListener() {			@Override			public void onClick(View v) {				if (m_nViewType != 0)					return;				switchViewType(0, 1);				m_nViewType = 1;				startWind();			}		});		m_btnMenuRefri = (Button) findViewById(R.id.button_menurefri);		m_btnMenuRefri.setOnClickListener(new OnClickListener() {			@Override			public void onClick(View v) {				if (m_nViewType != 0)					return;				switchViewType(0, 2);				m_nViewType = 2;				if(m_nValueRefri == 0){					refriSetting(3);//default value is third gear, when turn on refri function					m_nValueRefri = 3;				}else{					refriSetting(m_nValueRefri);				}			}		});		m_btnMenuHeat = (Button) findViewById(R.id.button_menuheat);		m_btnMenuHeat.setOnClickListener(new OnClickListener() {			@Override			public void onClick(View v) {				if (m_nViewType != 0)					return;				switchViewType(0, 3);				m_nViewType = 3;								if(m_nValueHeat == 0){					hotSetting(3);//default value is third gear, when turn on refri function					m_nValueHeat = 3;				}else{					hotSetting(m_nValueHeat);				}			}		});		m_btnMenuSetting = (Button) findViewById(R.id.button_menusetting);		m_btnMenuSetting.setOnClickListener(new OnClickListener() {			@Override			public void onClick(View v) {				if (m_nViewType != 0)					return;				switchViewType(0, 4);				m_nViewType = 4;			}		});		m_btnMenuBack = (Button) findViewById(R.id.button_menuback);		m_btnMenuBack.setOnClickListener(new OnClickListener() {			@Override			public void onClick(View v) {				if (m_nViewType != 0) 				{					switchViewType(m_nViewType, 0);					m_nViewType = 0;									} 				else 				{					String strMsg = String.format(getResources().getString(R.string.dialog_exitprompttext));					new AlertDialog.Builder(OperationCenterActivity.this)							.setTitle(getResources().getString(R.string.dialog_exitpromptitle))							.setMessage(strMsg)							.setNegativeButton(getResources().getString(R.string.text_cancel),									new DialogInterface.OnClickListener() {										@Override										public void onClick(DialogInterface dialog,												int which) {										}									})							.setPositiveButton(getResources().getString(R.string.text_confirm),									new DialogInterface.OnClickListener() {										@Override										public void onClick(DialogInterface dialog,												int which) {											LRHealthApp.getInstance().exit(); 											System.exit(0);										}									}).show();				}							}		});		// layout view		m_layoutViewSelf = (View) findViewById(R.id.linelayout_selfview);		m_layoutViewMenu = (View) findViewById(R.id.linelayout_menu);		m_layoutViewVent = (View) findViewById(R.id.linelayout_vent);		m_layoutViewRefri = (View) findViewById(R.id.linelayout_refri);		m_layoutViewHeat = (View) findViewById(R.id.linelayout_heat);		m_layoutViewSetting = (View) findViewById(R.id.linelayout_setting);                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  		// back button on setting panel				m_btnVentBack = (Button) findViewById(R.id.button_ventback);		m_btnVentBack.setOnClickListener(new OnClickListener() {			@Override			public void onClick(View v) {				switchViewType(1, 0);				m_nViewType = 0;				stopWork();//click back, stop ventlating				m_nValueVent = 0;				//TODO: rotate			}		});		m_btnRefriBack = (Button) findViewById(R.id.button_refriback);		m_btnRefriBack.setOnClickListener(new OnClickListener() {			@Override			public void onClick(View v) {				switchViewType(2, 0);				m_nViewType = 0;								stopWork();				m_nValueRefri = 0;			}		});		m_btnHeatBack = (Button) findViewById(R.id.button_heatback);		m_btnHeatBack.setOnClickListener(new OnClickListener() {			@Override			public void onClick(View v) {				switchViewType(3, 0);				m_nViewType = 0;								stopWork();				m_nValueHeat = 0;			}		});		// function button		m_btnFunVent = (Button) findViewById(R.id.button_vent);		m_btnFunVent.setOnClickListener(new OnClickListener() {			@Override			public void onClick(View v) {				if(m_nValueVent == 0){//if not ventlating					startWind();					m_nValueVent = 1;						//TODO: rotate				}else if(m_nValueVent == 1){					stopWork();					m_nValueVent = 0;					//TODO: rotate				}							}		});				m_btnFunVent = (Button) findViewById(R.id.button_vent);		m_btnFunVent.setBackgroundResource(R.anim.rotateanimation);		m_animdrawableRoate = (AnimationDrawable) m_btnFunVent.getBackground();		m_animdrawableRoate.setOneShot(false);		m_btnFunVent.setOnClickListener(new OnClickListener() {			@Override			public void onClick(View arg0) {				// TODO Auto-generated method stub				if(m_animdrawableRoate.isRunning())				{					m_animdrawableRoate.stop();				}				else				{					m_animdrawableRoate.start();				}			}		});				m_btnFunRefri = (Button) findViewById(R.id.button_refri);		m_btnFunRefri.setOnClickListener(new OnClickListener() {			@Override			public void onClick(View v) {				m_nValueRefri++;				if (m_nValueRefri > 3)					m_nValueRefri = 1;				if (m_nValueRefri == 1){					m_btnFunRefri							.setBackgroundResource(R.drawable.icon_funrefri01);					refriSetting(1);				}else if (m_nValueRefri == 2){					m_btnFunRefri							.setBackgroundResource(R.drawable.icon_funrefri02);					refriSetting(2);				}else					m_btnFunRefri							.setBackgroundResource(R.drawable.icon_funrefri03);					refriSetting(3);			}		});		m_btnFunHeat = (Button) findViewById(R.id.button_heat);		m_btnFunHeat.setOnClickListener(new OnClickListener() {			@Override			public void onClick(View v) {				m_nValueHeat++;				if (m_nValueHeat > 3)					m_nValueHeat = 1;				if (m_nValueHeat == 1){					m_btnFunHeat							.setBackgroundResource(R.drawable.icon_funheat01);					hotSetting(1);				}else if (m_nValueHeat == 2){					m_btnFunHeat							.setBackgroundResource(R.drawable.icon_funheat02);					hotSetting(2);				}else					m_btnFunHeat							.setBackgroundResource(R.drawable.icon_funheat03);					hotSetting(3);			}		});		//setting view		m_imgviewUIStyle = (ImageView) findViewById(R.id.imageView_uistyle);		m_imgviewVentSelectSt = (ImageView) findViewById(R.id.imageview_ventselectst);		m_imgviewRefriSelectSt = (ImageView) findViewById(R.id.imageview_refriselectst);		m_imgviewHeatSelectSt = (ImageView) findViewById(R.id.imageview_heatselectst);		m_imgviewSettingSelectSt = (ImageView) findViewById(R.id.imageview_settingselectst);						m_btnUIStyle00 = (Button) findViewById(R.id.button_uistyle00);		m_btnUIStyle00.setOnClickListener(new OnClickListener() {						@Override			public void onClick(View arg0) {				// TODO Auto-generated method stub				m_nValueUIStyle = 0;				setViewBackground(m_nValueUIStyle,1);				m_imgviewUIStyle.setImageDrawable(getResources().getDrawable(R.drawable.icon_funsettingui00));												m_btnUIStyle00.setTextColor(Color.BLUE);				m_btnUIStyle01.setTextColor(Color.BLACK);				m_btnUIStyle02.setTextColor(Color.BLACK);			}		});				m_btnUIStyle01 = (Button) findViewById(R.id.button_uistyle01);		m_btnUIStyle01.setOnClickListener(new OnClickListener() {						@Override			public void onClick(View arg0) {				// TODO Auto-generated method stub				m_nValueUIStyle = 1;				setViewBackground(m_nValueUIStyle,1);				m_imgviewUIStyle.setImageDrawable(getResources().getDrawable(R.drawable.icon_funsettingui01));								m_btnUIStyle00.setTextColor(Color.BLACK);				m_btnUIStyle01.setTextColor(Color.BLUE);				m_btnUIStyle02.setTextColor(Color.BLACK);			}		});				m_btnUIStyle02 = (Button) findViewById(R.id.button_uistyle02);		m_btnUIStyle02.setOnClickListener(new OnClickListener() {						@Override			public void onClick(View arg0) {				// TODO Auto-generated method stub				m_nValueUIStyle = 2;				setViewBackground(m_nValueUIStyle,1);				m_imgviewUIStyle.setImageDrawable(getResources().getDrawable(R.drawable.icon_funsettingui02));								m_btnUIStyle00.setTextColor(Color.BLACK);				m_btnUIStyle01.setTextColor(Color.BLACK);				m_btnUIStyle02.setTextColor(Color.BLUE);			}		});				m_btnDevManage = (Button) findViewById(R.id.button_devmanage);		m_btnDevManage.setOnClickListener(new OnClickListener() {						@Override			public void onClick(View arg0) {				// TODO Auto-generated method stub				Intent intent = new Intent();				intent.setClass(OperationCenterActivity.this,						DeviceListActivity.class);				intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);				startActivity(intent);			}		});				//set default ui		m_imgviewUIStyle.setImageDrawable(getResources().getDrawable(R.drawable.icon_funsettingui00));		m_btnUIStyle00.setTextColor(Color.BLUE);		m_btnUIStyle01.setTextColor(Color.BLACK);		m_btnUIStyle02.setTextColor(Color.BLACK);	}	private void initTranslateAnimation() {		// hide and show		m_animationShow = new TranslateAnimation(Animation.RELATIVE_TO_SELF,				0.0f, Animation.RELATIVE_TO_SELF, 0.0f,				Animation.RELATIVE_TO_SELF, -1.0f, Animation.RELATIVE_TO_SELF,				0.0f);		m_animationHide = new TranslateAnimation(Animation.RELATIVE_TO_SELF,				0.0f, Animation.RELATIVE_TO_SELF, 0.0f,				Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,				-1.0f);		m_animationShow.setDuration(ANIMATION_TIME);		m_animationHide.setDuration(ANIMATION_TIME);		// pull up/down		m_animationPullUp = new TranslateAnimation(Animation.RELATIVE_TO_SELF,				0.0f, Animation.RELATIVE_TO_SELF, 0.0f,				Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,				0.5f);		m_animationPullDown = new TranslateAnimation(				Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,				0.0f, Animation.RELATIVE_TO_SELF, 0.5f,				Animation.RELATIVE_TO_SELF, 0.0f);		m_animationPullUp.setDuration(ANIMATION_TIME);		m_animationPullDown.setDuration(ANIMATION_TIME);	}	private void switchViewType(int curtype, int willtype) 	{				if ((curtype == 0) && (willtype == 0))// init		{			// set view status			m_layoutViewMenu.setVisibility(View.VISIBLE);			m_layoutViewVent.setVisibility(View.GONE);			m_layoutViewRefri.setVisibility(View.GONE);			m_layoutViewHeat.setVisibility(View.GONE);			m_layoutViewSetting.setVisibility(View.GONE);			setViewBackground(m_nValueUIStyle, 0);			return;		}		if (willtype == 0)// will show menu		{			switch (curtype) {			case 1: {				m_layoutViewVent.startAnimation(m_animationHide);				m_layoutViewMenu.startAnimation(m_animationPullUp);									m_handler.postDelayed(new Runnable() {                      public void run() {                      	m_layoutViewVent.setVisibility(View.GONE);        				m_layoutViewMenu.setVisibility(View.VISIBLE);            				setViewBackground(m_nValueUIStyle, 0);                    }                  }, ANIMATION_TIME);							}				break;			case 2: {				m_layoutViewRefri.startAnimation(m_animationHide);				m_layoutViewMenu.startAnimation(m_animationPullUp);								m_handler.postDelayed(new Runnable() {                      public void run() {                      	m_layoutViewRefri.setVisibility(View.GONE);        				m_layoutViewMenu.setVisibility(View.VISIBLE);          				setViewBackground(m_nValueUIStyle, 0);                    }                  }, ANIMATION_TIME);			}				break;			case 3: {				m_layoutViewHeat.startAnimation(m_animationHide);				m_layoutViewMenu.startAnimation(m_animationPullUp);								m_handler.postDelayed(new Runnable() {                      public void run() {                      	m_layoutViewHeat.setVisibility(View.GONE);        				m_layoutViewMenu.setVisibility(View.VISIBLE);         				setViewBackground(m_nValueUIStyle, 0);                    }                  }, ANIMATION_TIME);			}				break;			case 4: {				m_layoutViewSetting.startAnimation(m_animationHide);				m_layoutViewMenu.startAnimation(m_animationPullUp);								m_handler.postDelayed(new Runnable() {                      public void run() {                      	m_layoutViewSetting.setVisibility(View.GONE);        				m_layoutViewMenu.setVisibility(View.VISIBLE);         				setViewBackground(m_nValueUIStyle, 0);                    }                  }, ANIMATION_TIME);							}				break;			default: {			}				break;			}						//set select status			m_imgviewVentSelectSt.setVisibility(View.GONE);			m_imgviewRefriSelectSt.setVisibility(View.GONE);			m_imgviewHeatSelectSt.setVisibility(View.GONE);			m_imgviewSettingSelectSt.setVisibility(View.GONE);		}		else// will show function		{			switch (willtype) {			case 1: {				m_layoutViewVent.startAnimation(m_animationShow);				m_layoutViewVent.setVisibility(View.VISIBLE);								m_layoutViewMenu.startAnimation(m_animationPullDown);				m_layoutViewMenu.setVisibility(View.VISIBLE);								//set select status				m_imgviewVentSelectSt.setVisibility(View.VISIBLE);				m_imgviewRefriSelectSt.setVisibility(View.INVISIBLE);				m_imgviewHeatSelectSt.setVisibility(View.INVISIBLE);				m_imgviewSettingSelectSt.setVisibility(View.INVISIBLE);			}				break;			case 2: {				m_layoutViewRefri.startAnimation(m_animationShow);				m_layoutViewRefri.setVisibility(View.VISIBLE);								m_layoutViewMenu.startAnimation(m_animationPullDown);				m_layoutViewMenu.setVisibility(View.VISIBLE);								//set select status				m_imgviewVentSelectSt.setVisibility(View.INVISIBLE);				m_imgviewRefriSelectSt.setVisibility(View.VISIBLE);				m_imgviewHeatSelectSt.setVisibility(View.INVISIBLE);				m_imgviewSettingSelectSt.setVisibility(View.INVISIBLE);			}				break;			case 3: {				m_layoutViewHeat.startAnimation(m_animationShow);				m_layoutViewHeat.setVisibility(View.VISIBLE);								m_layoutViewMenu.startAnimation(m_animationPullDown);				m_layoutViewMenu.setVisibility(View.VISIBLE);								//set select status				m_imgviewVentSelectSt.setVisibility(View.INVISIBLE);				m_imgviewRefriSelectSt.setVisibility(View.INVISIBLE);				m_imgviewHeatSelectSt.setVisibility(View.VISIBLE);				m_imgviewSettingSelectSt.setVisibility(View.INVISIBLE);			}				break;			case 4: {				m_layoutViewSetting.startAnimation(m_animationShow);				m_layoutViewSetting.setVisibility(View.VISIBLE);								m_layoutViewMenu.startAnimation(m_animationPullDown);				m_layoutViewMenu.setVisibility(View.VISIBLE);								//set select status				m_imgviewVentSelectSt.setVisibility(View.INVISIBLE);				m_imgviewRefriSelectSt.setVisibility(View.INVISIBLE);				m_imgviewHeatSelectSt.setVisibility(View.INVISIBLE);				m_imgviewSettingSelectSt.setVisibility(View.VISIBLE);			}				break;			default: {			}				break;			}			setViewBackground(m_nValueUIStyle, 1);		}	}	private void setViewBackground(int style, int type) {		Drawable imgDrawable;		if(style == -1)		{			if (type == 1)// menu				imgDrawable = getResources().getDrawable(						R.drawable.icon_bgstyle01);			else if(type == 2)				// fun				imgDrawable = getResources().getDrawable(						R.drawable.icon_bgstyle02);			else 				imgDrawable = getResources().getDrawable(						R.drawable.icon_bgstyle03);		}		else if (style == 0)// rain		{			if (type == 0)// menu				imgDrawable = getResources().getDrawable(						R.drawable.icon_bgstyle11);			else				// fun				imgDrawable = getResources().getDrawable(						R.drawable.icon_bgstyle10);		} else if (style == 1)// blue		{			if (type == 0)// menu				imgDrawable = getResources().getDrawable(						R.drawable.icon_bgstyle21);			else				// fun				imgDrawable = getResources().getDrawable(						R.drawable.icon_bgstyle20);		} else// shadow		{			if (type == 0)// menu				imgDrawable = getResources().getDrawable(						R.drawable.icon_bgstyle31);			else				// fun				imgDrawable = getResources().getDrawable(						R.drawable.icon_bgstyle30);		}		m_layoutViewSelf.setBackgroundDrawable(imgDrawable);		//m_layoutViewSelf.setBackground(imgDrawable);	}	/*ble communication*///	private int getwindStatus() {//		//		return 0;//	}//	//	private int getRefriStatus() {//		return 0;//	}//	//	private int getHeatStatus() {//		return 0;//	}		private void startWind() {		sendDate("01");	}		private void stopWork() {		sendDate("00");	}		private void refriSetting(int value) {		if(value == 1){// 1 gear			value = 0x05;		}else if(value == 2){//2 gear			value = 0x06;					}else if(value == 3){			value = 0x07;		}		sendDate("0"+ Integer.toHexString(value));	}		private void hotSetting(int value) {		if(value == 1){// 1 gear			value = 0x02;		}else if(value == 2){//2 gear			value = 0x03;					}else if(value == 3){			value = 0x04;		}		sendDate("0" + Integer.toHexString(value));	}	/**	 * 閿熸枻鎷烽敓鏂ゆ嫹鏁堥敓鏂ゆ嫹閿燂拷	 * 	 * @param buffer	 * @param length	 * @return	 */	private byte checksum(byte[] buffer, int length) {		byte nSum;		nSum = 0;		for (int i = 0; i < length; i++) {			nSum += buffer[i];		}		return nSum;	}	private void sendDate(String sendString) {		boolean bs = true;		connectstate = true;		Log.d("===", (connectstate)?"connectstate is true":"connectstate is falst");		if (connectstate == true) {			bs = true;			if (mbluetoothService.uuidb == 5) {				if (yyd != mbluetoothService.nm) {					if (mbluetoothService.nm < 255) {						sendxhid = mbluetoothService.nm + 1;					} else {						sendxhid = 0;					}					yyd = mbluetoothService.nm;					sss = 0;				}			} else if (mbluetoothService.uuidb == 8) { // uuid == b358														// 閿熸枻鎷烽敓閾板嚖鎷烽敓閰靛嚖鎷烽敓鎴鎷烽敓鏂ゆ嫹閿燂拷				sendxhid = mbluetoothService.nm;			}		}else{			//mbluetoothService.connect(macBleModule);			return;					}		if (bs) {			if (!sendString.equals("") && sendString.length() <= 18) {				BluetoothGattCharacteristic characteristic = BluetoothService.SERIAL_PORT_WRITE_Characteristic;				 byte[] by = Tools.hexStrToStr(sendString); //should use this in real situation				//byte[] by = sendString.getBytes();//use this when test				Log.d("===", sendString + " | " + by.toString());				byte[] idByte = new byte[by.length + 2];				idByte[0] = (byte) sendxhid;				for (int i = 0; i < by.length; i++) {					idByte[i + 1] = by[i];				}				Log.d("===",						Integer.toString(by.length) + " | "								+ Integer.toString(idByte.length));				idByte[idByte.length - 1] = 0;				idByte[idByte.length - 1] = checksum(idByte, idByte.length - 1);				if (idByte == null) {					Log.d("===", "1");				}				if (characteristic == null) {					Log.d("===", "2");				}				characteristic.setValue(idByte);// 閿熸枻鎷烽敓鏂ゆ嫹閿熸枻鎷烽敓鏂ゆ嫹閿熸枻鎷烽敓鏂ゆ嫹				nm = nm + 1; // 閿熸枻鎷烽敓鏂ゆ嫹閿熸枻鎷烽敓鎹疯揪鎷烽敓鏂ゆ嫹				/*				 * ==========================================================				 * =====				 */				boolean bb = mbluetoothService						.wirteCharacteristic(characteristic);// 閿熸枻鎷烽敓鏂ゆ嫹閿熸枻鎷锋ā閿熸枻鎷峰啓閿熸枻鎷烽敓鏂ゆ嫹閿熸枻鎷�				if (bb) {					senddatastate = true; // 閿熻鍑ゆ嫹濮嬮敓鏂ゆ嫹閿熸枻鎷烽敓鐨嗚鎷烽敓鏂ゆ嫹閿熸枻鎷烽敓鏂ゆ嫹 false:鏈敓鏂ゆ嫹濮�					SampleGattAttributes.senddatastate = senddatastate;					if (sendxhid >= 255) { // 閿熸枻鎷烽敓閰垫垚鐧告嫹 閿熸枻鎷蜂竴閿熸枻鎷烽敓鏂ゆ嫹閿燂拷						sendxhid = 0;					} else {						sendxhid = sendxhid + 1;					}				} else {					// addsenddata("鍙戦�佺 " + nm + " 鏉℃暟鎹け璐ワ紒");				}			} else {			}		}	}	// private void sendDate(int sendInt) {	// if (mbluetoothService.uuidb == 5) {	// if (yyd != mbluetoothService.nm) {	// if (mbluetoothService.nm < 255) {	// sendxhid = mbluetoothService.nm + 1;	// } else {	// sendxhid = 0;	// }	// yyd = mbluetoothService.nm;	// sss = 0;	// }	// } else if (mbluetoothService.uuidb == 8) { // uuid == b358	// // 閿熸枻鎷烽敓閾板嚖鎷烽敓閰靛嚖鎷烽敓鎴鎷烽敓鏂ゆ嫹閿燂拷	// sendxhid = mbluetoothService.nm;	// }	//	// BluetoothGattCharacteristic characteristic =	// BluetoothService.SERIAL_PORT_WRITE_Characteristic;	// byte[] by = sendInt // Tools.hexStrToStr(sendtext);	// byte[] idByte = new byte[by.length + 2];	// idByte[0] = (byte) sendxhid;	// for (int i = 0; i < by.length; i++) {	// idByte[i + 1] = by[i];	// }	// idByte[idByte.length - 1] = 0;	// idByte[idByte.length - 1] = checksum(idByte,	// idByte.length - 1);	// characteristic.setValue(idByte);// 閿熸枻鎷烽敓鏂ゆ嫹閿熸枻鎷烽敓鏂ゆ嫹閿熸枻鎷烽敓鏂ゆ嫹	// nm = nm + 1; // 閿熸枻鎷烽敓鏂ゆ嫹閿熸枻鎷烽敓鎹疯揪鎷烽敓鏂ゆ嫹	// /*	// * ==========================================================	// * =====	// */	// boolean bb = mbluetoothService	// .wirteCharacteristic(characteristic);// 閿熸枻鎷烽敓鏂ゆ嫹閿熸枻鎷锋ā閿熸枻鎷峰啓閿熸枻鎷烽敓鏂ゆ嫹閿熸枻鎷�	// if (bb) {	// senddatastate = true; // 閿熻鍑ゆ嫹濮嬮敓鏂ゆ嫹閿熸枻鎷烽敓鐨嗚鎷烽敓鏂ゆ嫹閿熸枻鎷烽敓鏂ゆ嫹 false:鏈敓鏂ゆ嫹濮�	// SampleGattAttributes.senddatastate = senddatastate;	// if (sendxhid >= 255) { // 閿熸枻鎷烽敓閰垫垚鐧告嫹 閿熸枻鎷蜂竴閿熸枻鎷烽敓鏂ゆ嫹閿燂拷	// sendxhid = 0;	// } else {	// sendxhid = sendxhid + 1;	// }	// } else {	// // addsenddata("閿熸枻鎷烽敓閰电鎷� " + nm + " 閿熸枻鎷烽敓鏂ゆ嫹閿熸枻鎷峰け閿熸澃锝忔嫹");	// }	//	// }}