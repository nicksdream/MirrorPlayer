package com.youngsee.mirrorplayer.provider;

import com.youngsee.mirrorplayer.util.Logger;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

public class MirrorProvider extends ContentProvider {

	private static Logger mLogger = new Logger();
	
	private static final int URL_SYSPARAM = 1;
	private static final int URL_SYSPARAM_ID = 2;

	private MirrorDbHelper mDbHelper = null;

	private static final UriMatcher s_urlMatcher = new UriMatcher(
			UriMatcher.NO_MATCH);

	static {
		s_urlMatcher.addURI(DbConstants.AUTHORITY, "sysparam", URL_SYSPARAM);
		s_urlMatcher.addURI(DbConstants.AUTHORITY, "sysparam/#", URL_SYSPARAM_ID);
	}

	@Override
	public int delete(Uri uri, String where, String[] whereArgs) {
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		int count;
		Uri deleteUri;

		switch (s_urlMatcher.match(uri)) {
		case URL_SYSPARAM:
			count = db.delete(DbConstants.TABLE_SYSPARAM, where, whereArgs);
			deleteUri = DbConstants.CONTENTURI_SYSPARAM;
			break;

		case URL_SYSPARAM_ID:
			count = db.delete(DbConstants.TABLE_SYSPARAM,
					DbConstants._ID + "=" + uri.getPathSegments().get(1)
					+ (!TextUtils.isEmpty(where) ? " AND " + where : ""), whereArgs);
			deleteUri = DbConstants.CONTENTURI_SYSPARAM;
			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		if (count > 0) {
			getContext().getContentResolver().notifyChange(deleteUri, null);
		}

		return count;
	}

	@Override
	public String getType(Uri url) {
		switch (s_urlMatcher.match(url)) {
		case URL_SYSPARAM:
			return DbConstants.CONTENT_TYPE;

		case URL_SYSPARAM_ID:
			return DbConstants.CONTENT_TYPE_ITME;

		default:
			throw new IllegalArgumentException("Unknown URL " + url);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		long rowId;
		Uri insertUri = null;

		ContentValues values;
		if (initialValues != null) {
			values = new ContentValues(initialValues);
		} else {
			values = new ContentValues();
		}

		switch (s_urlMatcher.match(uri)) {
		case URL_SYSPARAM:
			if (!values.containsKey(DbConstants.SPT_SCREENWIDTH)) {
				values.put(DbConstants.SPT_SCREENWIDTH, -1);
			}
			if (!values.containsKey(DbConstants.SPT_SCREENHEIGHT)) {
				values.put(DbConstants.SPT_SCREENHEIGHT, -1);
			}
			if (!values.containsKey(DbConstants.SPT_AUTOZOOMTIMEOUT)) {
				values.put(DbConstants.SPT_AUTOZOOMTIMEOUT, -1);
			}
			if (!values.containsKey(DbConstants.SPT_PICTUREDURATION)) {
				values.put(DbConstants.SPT_PICTUREDURATION, -1);
			}
			if (!values.containsKey(DbConstants.SPT_MODETYPE)) {
				values.put(DbConstants.SPT_MODETYPE, -1);
			}
			if (!values.containsKey(DbConstants.SPT_LAYOUTROWNUM)) {
				values.put(DbConstants.SPT_LAYOUTROWNUM, -1);
			}
			if (!values.containsKey(DbConstants.SPT_LAYOUTCOLUMNNUM)) {
				values.put(DbConstants.SPT_LAYOUTCOLUMNNUM, -1);
			}

			rowId = db.insert(DbConstants.TABLE_SYSPARAM, null, values);
			if (rowId > 0) {
				insertUri = ContentUris.withAppendedId(DbConstants.CONTENTURI_SYSPARAM, rowId);
			}
			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		if (rowId > 0) {
			getContext().getContentResolver().notifyChange(insertUri, null);
			return insertUri;
		}

		throw new SQLException("Failed to insert row into " + uri);
	}

	@Override
	public boolean onCreate() {
		mDbHelper = new MirrorDbHelper(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

		switch (s_urlMatcher.match(uri)) {
		case URL_SYSPARAM:
			qb.setTables(DbConstants.TABLE_SYSPARAM);
			break;

		case URL_SYSPARAM_ID:
			qb.setTables(DbConstants.TABLE_SYSPARAM);
			qb.appendWhere(DbConstants._ID + "=" + uri.getPathSegments().get(1));
			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		SQLiteDatabase db = mDbHelper.getReadableDatabase();
		Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
		c.setNotificationUri(getContext().getContentResolver(), uri);

		return c;
	}

	@Override
	public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		int count;
		Uri updateUri;

		switch (s_urlMatcher.match(uri)) {
		case URL_SYSPARAM:
			count = db.update(DbConstants.TABLE_SYSPARAM, values, where, whereArgs);
			updateUri = DbConstants.CONTENTURI_SYSPARAM;
			break;

		case URL_SYSPARAM_ID:
			count = db.update(DbConstants.TABLE_SYSPARAM, values,
					DbConstants._ID + "=" + uri.getPathSegments().get(1)
					+ (!TextUtils.isEmpty(where) ? " AND " + where : ""), whereArgs);
			updateUri = ContentUris.withAppendedId(DbConstants.CONTENTURI_SYSPARAM,
					Long.parseLong(uri.getPathSegments().get(1)));
			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		if (count > 0) {
			getContext().getContentResolver().notifyChange(updateUri, null);
		}

		return count;
	}

}
