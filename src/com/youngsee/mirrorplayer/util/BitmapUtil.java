package com.youngsee.mirrorplayer.util;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class BitmapUtil {

	private static int calculateInSampleSize(BitmapFactory.Options options,
			int reqWidth, int reqHeight) {
	    if ((options.outWidth <= reqWidth) && (options.outHeight <= reqHeight)) {
	    	return 1;
	    }

	    return (int)(((options.outWidth / (float)reqWidth)
	    		+ (options.outHeight / (float)reqHeight)) / 2);
	}

	private static void fillOptions(BitmapFactory.Options options, int reqWidth, int reqHeight) {
		options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
		options.inPreferredConfig = Bitmap.Config.RGB_565;
	    options.inDither = false;
	    options.inPurgeable = true;
	    options.inInputShareable = true;
	}

	public static Bitmap decodeResource(Resources res, int resId, int reqWidth, int reqHeight) {
	    BitmapFactory.Options options = new BitmapFactory.Options();
	    options.inJustDecodeBounds = true;
	    BitmapFactory.decodeResource(res, resId, options);

	    fillOptions(options, reqWidth, reqHeight);

	    options.inJustDecodeBounds = false;

	    Bitmap bitmap = null;
	    try {
	    	bitmap = BitmapFactory.decodeResource(res, resId, options);
	    } catch (OutOfMemoryError e) {
	    	e.printStackTrace();
	    	System.gc();
	    }

	    return bitmap;
	}
	
	public static Bitmap decodeFile(String file, int reqWidth, int reqHeight) {
	    BitmapFactory.Options options = new BitmapFactory.Options();
	    options.inJustDecodeBounds = true;
	    BitmapFactory.decodeFile(file, options);

	    fillOptions(options, reqWidth, reqHeight);

	    options.inJustDecodeBounds = false;

	    Bitmap bitmap = null;
	    try {
	    	bitmap = BitmapFactory.decodeFile(file, options);
	    } catch (OutOfMemoryError e) {
	    	e.printStackTrace();
	    	System.gc();
	    }

	    return bitmap;
	}

}
