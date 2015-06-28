package com.youngsee.mirrorplayer.util;

import com.youngsee.mirrorplayer.MirrorApplication;

import android.graphics.Typeface;
import android.text.TextUtils;

public class TypefaceUtil {

	private static final Logger sLogger = new Logger();
	
	public static final String DEFAULT = "默认";
	public static final String CUHEI = "粗黑";
	public static final String FANGSONG = "仿宋";
	public static final String HEITI = "黑体";
	public static final String KAISHU = "楷书";
	public static final String LISHU = "隶书";
	public static final String SHUTI = "舒体";
	public static final String SONGTI = "宋体";
	public static final String WEIBEI = "魏碑";
	public static final String XIHEI = "细黑";
	public static final String XINGKAI = "行楷";
	public static final String YAOTI = "姚体";
	public static final String YOUYUAN = "幼圆";
	public static final String ZHONGSONG = "中宋";

	private static String getPath(String fontname) {
		if (TextUtils.isEmpty(fontname)) {
			sLogger.i("Font name is empty.");
			sLogger.i("Use default one...");
			return "fonts/default.ttf";
		}

		StringBuilder sb = new StringBuilder();
		sb.append("fonts/");
		
		if (fontname.equals(DEFAULT)) {
			sb.append("default.ttf");
		} else if (fontname.equals(CUHEI)) {
			sb.append("cuhei.ttf");
		} else if (fontname.equals(FANGSONG)) {
			sb.append("fangsong.ttf");
		} else if (fontname.equals(HEITI)) {
			sb.append("heiti.ttf");
		} else if (fontname.equals(KAISHU)) {
			sb.append("kaishu.ttf");
		} else if (fontname.equals(LISHU)) {
			sb.append("lishu.ttf");
		} else if (fontname.equals(SHUTI)) {
			sb.append("shuti.ttf");
		} else if (fontname.equals(SONGTI)) {
			sb.append("songti.ttf");
		} else if (fontname.equals(WEIBEI)) {
			sb.append("weibei.ttf");
		} else if (fontname.equals(XIHEI)) {
			sb.append("xihei.ttf");
		} else if (fontname.equals(XINGKAI)) {
			sb.append("xingkai.ttf");
		} else if (fontname.equals(YAOTI)) {
			sb.append("yaoti.ttf");
		} else if (fontname.equals(YOUYUAN)) {
			sb.append("youyuan.ttf");
		} else if (fontname.equals(ZHONGSONG)) {
			sb.append("zhongsong.ttf");
		} else {
			sLogger.i("Unkown font name, fontname = '" + fontname + "'.");
			sLogger.i("Use default one...");
			sb.append("default.ttf");
		}

        return sb.toString();
    }

	public static Typeface getTypeface(String fontname) {
        return Typeface.createFromAsset(MirrorApplication.getInstance().getAssets(), getPath(fontname));
    }

}
