package com.jonma.tool;

import com.jonma.lrhealth.R;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.widget.TextView;

public class CustomProgressDialog extends Dialog {
	private Context context = null;
	private static CustomProgressDialog customProgressDialog = null;
	
	public CustomProgressDialog(Context context){
		super(context);
		this.context = context;
	}
	
	public CustomProgressDialog(Context context, int theme) {
		super(context, theme);
	}
	
	public static CustomProgressDialog createDialog(Context context){
		customProgressDialog = new CustomProgressDialog(context,R.style.CustomProgressDialog);
		customProgressDialog.setContentView(R.layout.customprogressdialog);
		customProgressDialog.getWindow().getAttributes().gravity = Gravity.RIGHT | Gravity.TOP;
		
		return customProgressDialog;
	}
 
    public void onWindowFocusChanged(boolean hasFocus){
    	
    	if (customProgressDialog == null){
    		return;
    	}
    	
        //ImageView imageView = (ImageView) customProgressDialog.findViewById(R.id.loadingImageView);
        //AnimationDrawable animationDrawable = (AnimationDrawable) imageView.getBackground();
        //animationDrawable.start();
    }
 
    public CustomProgressDialog setTitile(String strTitle){
    	return customProgressDialog;
    }
    
    public CustomProgressDialog setMessage(String strMessage){
    	TextView msg = (TextView)customProgressDialog.findViewById(R.id.progressmsg);
    	
    	if (msg != null){
    		msg.setText(strMessage);
    	}
    	
    	return customProgressDialog;
    }
}
