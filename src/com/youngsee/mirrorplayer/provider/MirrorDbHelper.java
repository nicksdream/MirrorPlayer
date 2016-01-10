package com.youngsee.mirrorplayer.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MirrorDbHelper extends SQLiteOpenHelper {

	private Context mContext;

	public MirrorDbHelper(Context context) {
		super(context, DbConstants.DATABASE_NAME, null, DbConstants.DATABASE_VERSION);
		mContext = context;
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		createSysParamTable(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

	private void createSysParamTable(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE IF NOT EXISTS " + DbConstants.TABLE_SYSPARAM + "("
				+ DbConstants._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ DbConstants.SPT_DEVICEID + " TEXT DEFAULT NULL,"
				+ DbConstants.SPT_DEVICEMODEL + " TEXT DEFAULT NULL,"
				+ DbConstants.SPT_SOFTWAREVERSION + " TEXT DEFAULT NULL,"
				+ DbConstants.SPT_KERNELVERSION + " TEXT DEFAULT NULL,"
				+ DbConstants.SPT_SCREENWIDTH + " INTEGER DEFAULT '-1',"
				+ DbConstants.SPT_SCREENHEIGHT + " INTEGER DEFAULT '-1',"
				+ DbConstants.SPT_APPLICATIONPATH + " TEXT DEFAULT NULL,"
				+ DbConstants.SPT_AUTOZOOMTIMEOUT + " INTEGER DEFAULT '-1',"
				+ DbConstants.SPT_CHECKDISTANCE + " INTEGER DEFAULT '-1',"
				+ DbConstants.SPT_PICTUREDURATION + " INTEGER DEFAULT '-1',"
				+ DbConstants.SPT_MODETYPE + " INTEGER DEFAULT '-1',"
				+ DbConstants.SPT_MODEDESCRITION + " TEXT DEFAULT NULL,"
				+ DbConstants.SPT_LAYOUTROWNUM + " INTEGER DEFAULT '-1',"
				+ DbConstants.SPT_LAYOUTCOLUMNNUM + " INTEGER DEFAULT '-1')");
	}

}
