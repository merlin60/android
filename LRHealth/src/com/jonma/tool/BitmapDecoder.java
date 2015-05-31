package com.jonma.tool;

import android.R.integer;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class BitmapDecoder {
	private static final int SRCPIC_WIDTH = 1280;
	private static final int SRCPIC_HEIGHT = 720;

	public static Bitmap BitmapDecoder(Resources res, int rid, int rwidth, int rheight) 
	{
		// TODO Auto-generated constructor stub
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = calSampleSize(rwidth, rheight);
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

}
