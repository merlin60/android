package com.jonma.lrhealth;

import android.R.integer;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/*new this class in onResume() function */
public class SharedSetting {
	SharedPreferences appSharedPreferences;
	
	
	public SharedSetting(Context context) {
		this.appSharedPreferences = context.getSharedPreferences("setting", Activity.MODE_PRIVATE);
		
	}
	
	private void saveData(String key, int value) 
	{
		SharedPreferences.Editor editor = appSharedPreferences.edit(); 
		
		editor.putInt(key, value);
		editor.commit(); 
	}
	
	private int queryData(String key) 
	{
		int value = appSharedPreferences.getInt(key, 0);
		return value;
	}
	
	public void apiSaveUIStyle(int style)
	{
		saveData("UIStyle", style);
	}
	
	public int apiQueryUIStyle()
	{
		return queryData("UIStyle");
	}
	
	
}
