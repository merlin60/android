package com.jonma.tool;

import android.R.integer;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class BitmapDecoder {
	private static final int SRCPIC_WIDTH = 1280;
	private static final int SRCPIC_HEIGHT = 720;

	public static Bitmap BitmapDecoder(Resources res, int rid, int rwidth, int rheight) 
	{
		// TODO Auto-generated constructor stub
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = calSampleSize(rwidth, rheight);
		//Log.d("LRHealth", "bitmap insample size:"+options.inSampleSize);
		
		return BitmapFactory.decodeResource(res, rid, options);
	}

	public static int calSampleSize(int rwidth, int rheight) 
	{
		int height = SRCPIC_WIDTH;
		int width = SRCPIC_WIDTH;
		int inSampleSize = 1;

		int heightRatio = Math.round((float) height / (float) rheight);
		int widthRatio = Math.round((float) width / (float) rwidth);
		inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;

		return inSampleSize;
	}
	
	public static Bitmap BitmapDecoderBySampleSize(Resources res, int rid, int rwidth, int rheight, int samplesize) 
	{
		//Log.d("LRHealth", "bitmap insample size:" + samplesize);		
		Bitmap bmp = null;
		try 
		{
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inSampleSize = samplesize;
			bmp = BitmapFactory.decodeResource(res, rid, options);		
		} 
		catch (OutOfMemoryError err) 
		{
			System.gc();
			bmp = null;
			Log.d("LRHealth", "bitmap decode oom");
		}
		
		return bmp;
	}

}
