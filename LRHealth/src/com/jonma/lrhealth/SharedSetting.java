package com.jonma.lrhealth;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/*new this class in onResume() function */
public class SharedSetting {
	SharedPreferences mySharedPreferences;
	SharedPreferences.Editor editor;
	
	public SharedSetting(Context context) {
		this.mySharedPreferences = context.getSharedPreferences("setting", Activity.MODE_PRIVATE);
		editor = mySharedPreferences.edit(); 
	}
	
	public void add(String key, String value) {
		editor.putString(key, value);
		editor.commit(); 
	}
}
