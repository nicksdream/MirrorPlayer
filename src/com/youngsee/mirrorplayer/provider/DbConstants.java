package com.youngsee.mirrorplayer.provider;

import android.net.Uri;

public class DbConstants {

	public static final String AUTHORITY = "com.youngsee.mirrorplayer.provider";

	public static final String DATABASE_NAME = "mirrorplayer.db";

	public static final int DATABASE_VERSION = 1;

	public static final String NOTSET = "Not set";

	public static final String TABLE_SYSPARAM = "sysparam";

	public static final String _ID = "_id";

	public static final String SPT_DEVICEID = "deviceid";
	public static final String SPT_DEVICEMODEL = "devicemodel";
	public static final String SPT_SOFTWAREVERSION = "softwareversion";
	public static final String SPT_KERNELVERSION = "kernelversion";
	public static final String SPT_SCREENWIDTH = "screenwidth";
	public static final String SPT_SCREENHEIGHT = "screenheight";
	public static final String SPT_APPLICATIONPATH = "applicationpath";
	public static final String SPT_AUTOZOOMTIMEOUT = "autozoomtimeout";
	public static final String SPT_CHECKDISTANCE = "checkdistance";
	public static final String SPT_PICTUREDURATION = "pictureduration";
	public static final String SPT_MODETYPE = "modetype";
	public static final String SPT_MODEDESCRITION = "modedescription";
	public static final String SPT_LAYOUTROWNUM = "layoutrownum";
	public static final String SPT_LAYOUTCOLUMNNUM = "layoutcolumnnum";

	public static final Uri CONTENTURI_SYSPARAM = Uri.parse("content://"
			+ AUTHORITY + "/sysparam");

	public static final String CONTENT_TYPE = "vnd.android.cursor.dir/"
			+ AUTHORITY + ".type";
	public static final String CONTENT_TYPE_ITME = "vnd.android.cursor.item/"
			+ AUTHORITY + ".item";
}
