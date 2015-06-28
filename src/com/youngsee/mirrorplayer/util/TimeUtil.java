package com.youngsee.mirrorplayer.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.text.TextUtils;
import android.text.format.Time;

public class TimeUtil {

	private static final Logger sLogger = new Logger();
	
	private static final String PATTERN_DATE = "^(\\d{4})-(\\d{1,2})-(\\d{1,2})$";
	
	private static final String PATTERN_DATETIME =
			"^(\\d{4})-(\\d{1,2})-(\\d{1,2})\\s+(\\d{1,2}):(\\d{1,2}):(\\d{1,2})$";

	/**
	 * Get the milliseconds of the given date.
	 * 
	 * @param YYYY-MM-DD
	 * @return
	 */
	public static long getDateMillis(String dstr) {
		if (TextUtils.isEmpty(dstr)) {
			sLogger.i("Date string is empty, dstr = " + dstr + ".");
			return -1;
		}

		Pattern p = Pattern.compile(PATTERN_DATE);
		Matcher m = p.matcher(dstr);
		
		if (!m.find()) {
			sLogger.i("Invalid date format, dstr = " + dstr + ".");
			return -1;
		}

		int year = Integer.parseInt(m.group(1));
		int month = Integer.parseInt(m.group(2));
		int day = Integer.parseInt(m.group(3));

		if ((month < 1) || (month > 12) || (day < 1) || (day > 31)) {
			sLogger.i("Invalid date, dstr = " + dstr + ".");
			return -1;
		}

		Time t = new Time();
		t.set(day, month - 1, year);
		t.normalize(true);

		return t.toMillis(true);
    }

	/**
	 * Get the milliseconds of the given datetime.
	 * 
	 * @param YYYY-MM-DD HH:MM:SS
	 * @return
	 */
	public static long getDatetimeMillis(String dtstr) {
		if (TextUtils.isEmpty(dtstr)) {
			sLogger.i("Datetime string is empty, dtstr = " + dtstr + ".");
			return -1;
		}

		Pattern p = Pattern.compile(PATTERN_DATETIME);
		Matcher m = p.matcher(dtstr);
		
		if (!m.find()) {
			sLogger.i("Invalid datetime format, dtstr = " + dtstr + ".");
			return -1;
		}
		
		int year = Integer.parseInt(m.group(1));
		int month = Integer.parseInt(m.group(2));
		int day = Integer.parseInt(m.group(3));
		int hour = Integer.parseInt(m.group(4));
		int minute = Integer.parseInt(m.group(5));
		int second = Integer.parseInt(m.group(6));

		if ((month < 1) || (month > 12) || (day < 1) || (day > 31)
				|| (hour > 24) || (minute > 59) || (second > 59)) {
			sLogger.i("Invalid datetime, dtstr = " + dtstr + ".");
			return -1;
		}

		StringBuilder sb = new StringBuilder();
		sb.append(year);
		sb.append((month < 10) ? ("0" + month) : month);
		sb.append((day < 10) ? ("0" + day) : day);
		sb.append("T");
		sb.append((hour < 10) ? ("0" + hour) : hour);
		sb.append((minute < 10) ? ("0" + minute) : minute);
		sb.append((second < 10) ? ("0" + second) : second);

		Time t = new Time();
		t.parse(sb.toString());
		t.normalize(true);

		return t.toMillis(true);
    }

	/**
	 * Get the milliseconds of the current date.
	 * 
	 * @param
	 * @return
	 */
	public static long getCurrentDateMillis() {
		Time t = new Time();
		t.setToNow();
		
		int year = t.year;
		int month = t.month + 1;
		int day = t.monthDay;
		
		t.set(day, month-1, year);
        t.normalize(true);

        return t.toMillis(true);
	}

	/**
	 * Get the current date.
	 * 
	 * @param
	 * @return YYYY-MM-DD
	 */
	public static String getCurrentDate() {
		Time t = new Time();
		t.setToNow();
		
		int year = t.year;
		int month = t.month + 1;
		int day = t.monthDay;
		
		StringBuilder sb = new StringBuilder();
		sb.append(year);
		sb.append("-");
        sb.append((month < 10) ? ("0" + month) : month);
        sb.append("-");
        sb.append((day < 10) ? ("0" + day) : day);
        
        return sb.toString();
	}
	
	/**
	 * Get the current datetime.
	 * 
	 * @param
	 * @return YYYY-MM-DD HH:MM:SS
	 */
	public static String getCurrentDatetime() {
		Time t = new Time();
		t.setToNow();
		
		int year = t.year;
		int month = t.month + 1;
		int day = t.monthDay;
		int hour = t.hour;
		int minute = t.minute;
		int second = t.second;
		
		StringBuilder sb = new StringBuilder();
		sb.append(year);
		sb.append("-");
        sb.append((month < 10) ? ("0" + month) : month);
        sb.append("-");
        sb.append((day < 10) ? ("0" + day) : day);
        sb.append(" ");
        sb.append((hour < 10) ? ("0" + hour) : hour);
		sb.append(":");
        sb.append((minute < 10) ? ("0" + minute) : minute);
        sb.append(":");
        sb.append((second < 10) ? ("0" + second) : second);
        
        return sb.toString();
	}

}
