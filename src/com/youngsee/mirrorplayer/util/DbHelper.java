package com.youngsee.mirrorplayer.util;

import com.youngsee.mirrorplayer.MirrorApplication;
import com.youngsee.mirrorplayer.provider.DbConstants;
import com.youngsee.mirrorplayer.system.DbSysParam;
import com.youngsee.mirrorplayer.system.XmlSysParam;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;

public class DbHelper {
	private Logger mLogger = new Logger();

	private static final int DEFAULT_SYSPARAM_DBID = 1;
	
	private ContentResolver mContentResolver;
	
	private DbHelper() {
		mContentResolver = MirrorApplication.getInstance().getContentResolver();
	}
	
	private static class DbHolder {
        static final DbHelper INSTANCE = new DbHelper();
    }

	public static DbHelper getInstance() {
		return DbHolder.INSTANCE;
	}
	
	public DbSysParam getSysParam() {
		DbSysParam param = null;
		Cursor c = mContentResolver.query(DbConstants.CONTENTURI_SYSPARAM, null, null, null, null);
		
		if (c.moveToFirst()) {
			param = new DbSysParam();
			param.deviceid = c.getString(c.getColumnIndex(DbConstants.SPT_DEVICEID));
			param.devicemodel = c.getString(c.getColumnIndex(DbConstants.SPT_DEVICEMODEL));
			param.softwareversion = c.getString(c.getColumnIndex(DbConstants.SPT_SOFTWAREVERSION));
			param.kernelversion = c.getString(c.getColumnIndex(DbConstants.SPT_KERNELVERSION));
			param.screenwidth = c.getInt(c.getColumnIndex(DbConstants.SPT_SCREENWIDTH));
			param.screenheight = c.getInt(c.getColumnIndex(DbConstants.SPT_SCREENHEIGHT));
			param.applicationpath = c.getString(c.getColumnIndex(DbConstants.SPT_APPLICATIONPATH));
			param.autozoomtimeout = c.getInt(c.getColumnIndex(DbConstants.SPT_AUTOZOOMTIMEOUT));
			param.checkdistance = c.getInt(c.getColumnIndex(DbConstants.SPT_CHECKDISTANCE));
			param.pictureduration = c.getInt(c.getColumnIndex(DbConstants.SPT_PICTUREDURATION));
			param.modetype = c.getInt(c.getColumnIndex(DbConstants.SPT_MODETYPE));
			param.modedescription = c.getString(c.getColumnIndex(DbConstants.SPT_MODEDESCRITION));
			param.layoutrownum = c.getInt(c.getColumnIndex(DbConstants.SPT_LAYOUTROWNUM));
			param.layoutcolumnnum = c.getInt(c.getColumnIndex(DbConstants.SPT_LAYOUTCOLUMNNUM));
		} else {
			mLogger.i("No record can be found in the system parameter table.");
		}

		c.close();
		
		return param;
	}
	
	public void setSysParam(boolean isinitial, XmlSysParam xmlsysparam, String softwareversion,
			String kernelversion, int screenwidth, int screenheight, String applicationpath) {
		ContentValues cv = new ContentValues();
		if (xmlsysparam != null) {
			if (!TextUtils.isEmpty(xmlsysparam.deviceid)) {
				cv.put(DbConstants.SPT_DEVICEID, xmlsysparam.deviceid);
			}
			if (!TextUtils.isEmpty(xmlsysparam.devicemodel)) {
				cv.put(DbConstants.SPT_DEVICEMODEL, xmlsysparam.devicemodel);
			}
			if (xmlsysparam.devautozoomtimeout != -1) {
				cv.put(DbConstants.SPT_AUTOZOOMTIMEOUT, xmlsysparam.devautozoomtimeout);
			}
			if (xmlsysparam.devcheckdistance != -1) {
				cv.put(DbConstants.SPT_CHECKDISTANCE, xmlsysparam.devcheckdistance);
			}
			if (xmlsysparam.devpictureduration != -1) {
				cv.put(DbConstants.SPT_PICTUREDURATION, xmlsysparam.devpictureduration);
			}
			if (xmlsysparam.modetype != -1) {
				cv.put(DbConstants.SPT_MODETYPE, xmlsysparam.modetype);
			}
			if (!TextUtils.isEmpty(xmlsysparam.modedescription)) {
				cv.put(DbConstants.SPT_MODEDESCRITION, xmlsysparam.modedescription);
			}
			if (xmlsysparam.layoutrownum != -1) {
				cv.put(DbConstants.SPT_LAYOUTROWNUM, xmlsysparam.layoutrownum);
			}
			if (xmlsysparam.layoutcolumnnum != -1) {
				cv.put(DbConstants.SPT_LAYOUTCOLUMNNUM, xmlsysparam.layoutcolumnnum);
			}
		}
		if (!TextUtils.isEmpty(softwareversion)) {
			cv.put(DbConstants.SPT_SOFTWAREVERSION, softwareversion);
		}
		if (!TextUtils.isEmpty(kernelversion)) {
			cv.put(DbConstants.SPT_KERNELVERSION, kernelversion);
		}
		if (screenwidth != -1) {
			cv.put(DbConstants.SPT_SCREENWIDTH, screenwidth);
		}
		if (screenheight != -1) {
			cv.put(DbConstants.SPT_SCREENHEIGHT, screenheight);
		}
		if (!TextUtils.isEmpty(applicationpath)) {
			cv.put(DbConstants.SPT_APPLICATIONPATH, applicationpath);
		}
		
		if (cv.size() > 0) {
			if (isinitial) {
				mContentResolver.insert(DbConstants.CONTENTURI_SYSPARAM, cv);
			} else {
				mContentResolver.update(ContentUris.withAppendedId(
						DbConstants.CONTENTURI_SYSPARAM, DEFAULT_SYSPARAM_DBID), cv, null, null);
			}
		} else {
			mLogger.i("No content value (System parameter) need to be inserted or updated.");
		}
	}
	
	public void updateSoftwareVersion(String version) {
		if (TextUtils.isEmpty(version)) {
			mLogger.i("Software version is empty.");
			return;
		}

		ContentValues cv = new ContentValues();
		cv.put(DbConstants.SPT_SOFTWAREVERSION, version);
		mContentResolver.update(ContentUris.withAppendedId(
				DbConstants.CONTENTURI_SYSPARAM, DEFAULT_SYSPARAM_DBID), cv, null, null);
	}

	public void updateKernelVersion(String version) {
		if (TextUtils.isEmpty(version)) {
			mLogger.i("Kernel version is empty.");
			return;
		}

		ContentValues cv = new ContentValues();
		cv.put(DbConstants.SPT_KERNELVERSION, version);
		mContentResolver.update(ContentUris.withAppendedId(
				DbConstants.CONTENTURI_SYSPARAM, DEFAULT_SYSPARAM_DBID), cv, null, null);
	}

	public void updateScreenWidth(int width) {
		if (width < 0) {
			mLogger.i("Screen width is less than zore.");
			return;
		}

		ContentValues cv = new ContentValues();
		cv.put(DbConstants.SPT_SCREENWIDTH, width);
		mContentResolver.update(ContentUris.withAppendedId(
				DbConstants.CONTENTURI_SYSPARAM, DEFAULT_SYSPARAM_DBID), cv, null, null);
	}

	public void updateScreenHeight(int height) {
		if (height < 0) {
			mLogger.i("Screen height is less than zore.");
			return;
		}

		ContentValues cv = new ContentValues();
		cv.put(DbConstants.SPT_SCREENHEIGHT, height);
		mContentResolver.update(ContentUris.withAppendedId(
				DbConstants.CONTENTURI_SYSPARAM, DEFAULT_SYSPARAM_DBID), cv, null, null);
	}

	public void updateApplicationPath(String path) {
		if (TextUtils.isEmpty(path)) {
			mLogger.i("Application path is empty.");
			return;
		}

		ContentValues cv = new ContentValues();
		cv.put(DbConstants.SPT_APPLICATIONPATH, path);
		mContentResolver.update(ContentUris.withAppendedId(
				DbConstants.CONTENTURI_SYSPARAM, DEFAULT_SYSPARAM_DBID), cv, null, null);
	}

	public void updateUserParams(int autozoomtimeout, int checkdistance, int pictureduration) {
		if (autozoomtimeout < 0) {
			mLogger.i("Auto zoom timeout is less than zore.");
			return;
		} else if (checkdistance < 0) {
			mLogger.i("Check distance is less than zore.");
			return;
		} else if (pictureduration < 0) {
			mLogger.i("Picture duration is less than zore.");
			return;
		}

		ContentValues cv = new ContentValues();
		cv.put(DbConstants.SPT_AUTOZOOMTIMEOUT, autozoomtimeout);
		cv.put(DbConstants.SPT_CHECKDISTANCE, checkdistance);
		cv.put(DbConstants.SPT_PICTUREDURATION, pictureduration);
		mContentResolver.update(ContentUris.withAppendedId(
				DbConstants.CONTENTURI_SYSPARAM, DEFAULT_SYSPARAM_DBID), cv, null, null);
	}

	public void updateModeInfo(int type, String description) {
		if (type < 0) {
			mLogger.i("Mode type is less than zore.");
			return;
		} else if (TextUtils.isEmpty(description)) {
			mLogger.i("Mode description is empty.");
			return;
		}

		ContentValues cv = new ContentValues();
		cv.put(DbConstants.SPT_MODETYPE, type);
		cv.put(DbConstants.SPT_MODEDESCRITION, description);
		mContentResolver.update(ContentUris.withAppendedId(
				DbConstants.CONTENTURI_SYSPARAM, DEFAULT_SYSPARAM_DBID), cv, null, null);
	}
	
	public void updateLayoutInfo(int rownum, int columnnum) {
		if (rownum < 1) {
			mLogger.i("Layout row number is less than 1.");
			return;
		} else if (columnnum < 1) {
			mLogger.i("Layout column number is less than 1.");
			return;
		}

		ContentValues cv = new ContentValues();
		cv.put(DbConstants.SPT_LAYOUTROWNUM, rownum);
		cv.put(DbConstants.SPT_LAYOUTCOLUMNNUM, columnnum);
		mContentResolver.update(ContentUris.withAppendedId(
				DbConstants.CONTENTURI_SYSPARAM, DEFAULT_SYSPARAM_DBID), cv, null, null);
	}
}
