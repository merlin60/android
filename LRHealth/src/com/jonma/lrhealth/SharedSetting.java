package com.jonma.lrhealth;

import android.R.integer;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/*new this class in onResume() function */
public class SharedSetting {
	SharedPreferences appSharedPreferences;
	private SharedPreferences.Editor editor;
	
	
	public SharedSetting(Context context) {
		this.appSharedPreferences = context.getSharedPreferences("setting", Activity.MODE_PRIVATE);
		editor = appSharedPreferences.edit(); 		
	}
	
	private void saveData(String key, int value) 
	{
		editor.putInt(key, value);
		editor.commit(); 
	}
	
	private void saveData(String key, String value) 
	{
		editor.putString(key, value);
		editor.commit(); 
	}
	
	private int queryIntData(String key) 
	{
		int value = appSharedPreferences.getInt(key, 0);
		return value;
	}
	
	private String queryStringData(String key) 
	{
		String value = appSharedPreferences.getString(key, null);
		return value;
	}
	
	public void apiSaveUIStyle(int style)
	{
		saveData("UIStyle", style);
	}
	
	public int apiQueryUIStyle()
	{
		return queryIntData("UIStyle");
	}
	
	public void saveLastConndevice(String mac) {
		saveData("mac", mac);
	}
	
	public String queryLastConndevice() {
		return queryStringData("mac");
	}
	
	
}
