package com.youngsee.mirrorplayer.view;

import java.util.List;

import com.youngsee.mirrorplayer.common.MediaInfo;
import com.youngsee.mirrorplayer.util.Logger;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public abstract class MirrorView extends LinearLayout {

	protected Logger mLogger = new Logger();

	protected int mIndex = -1;
	protected List<MediaInfo> mMediaLst = null;
	
	protected Context mContext = null;

	protected int mViewWidth = -1;
	protected int mViewHeight = -1;

	public MirrorView(Context context) {
		super(context);
		mContext = context;
	}

	public MirrorView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
	}

    public MirrorView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
	}

    public int getIndex() {
    	return mIndex;
    }

    public int getViewWidth() {
    	return mViewWidth;
    }

    public int getViewHeight() {
    	return mViewHeight;
    }

    public void setIndex(int index) {
    	mIndex = index;
    }

    public void setMediaList(List<MediaInfo> lst) {
    	mMediaLst = lst;
    }

    public void setViewWidth(int width) {
    	mViewWidth = width;
    }

    public void setViewHeight(int height) {
    	mViewHeight = height;
    }

	public abstract void onPause();
    public abstract void onResume();

    public void onDestroy() {
    	mMediaLst = null;
    }

    public abstract void start();
    public abstract void stop();

    protected int getFontColor(String fcstr) {
    	if (TextUtils.isEmpty(fcstr)) {
    		mLogger.i("Font color string is empty.");
    		return Color.WHITE;
    	} else if (!fcstr.startsWith("0x")) {
    		mLogger.i("Invalid font color string, fcstr = " + fcstr + ".");
    		return Color.WHITE;
    	}
    	
    	int fontcolor = Color.WHITE;
    	try {
    		fontcolor = 0xFF000000 | Integer.parseInt(fcstr.substring(2), 16);
    	} catch (NumberFormatException e) {
			e.printStackTrace();
		}
    	
    	return fontcolor;
    }

}
