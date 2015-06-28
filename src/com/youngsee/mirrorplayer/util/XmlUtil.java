package com.youngsee.mirrorplayer.util;

import java.io.IOException;
import java.io.InputStream;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Xml;

import com.youngsee.mirrorplayer.MirrorApplication;
import com.youngsee.mirrorplayer.R;
import com.youngsee.mirrorplayer.common.Constants;
import com.youngsee.mirrorplayer.system.XmlSysParam;

public class XmlUtil {

	public static final String SYSPARAMXML_TAG = "sysparam";
	public static final String SYSPARAMXML_VERSION = "version";

	public static final String SYSPARAMXML_TAG_DEVINFO = "devinfo";
	public static final String SYSPARAMXML_TAG_MODEINFO = "modeinfo";
	public static final String SYSPARAMXML_TAG_LAYOUTINFO = "layoutinfo";

	public static final String SYSPARAMXML_PROP_DEV_ID = "id";
	public static final String SYSPARAMXML_PROP_DEV_MODEL = "model";
	public static final String SYSPARAMXML_PROP_DEV_AUTOZOOMTIMEOUT = "autozoomtimeout";
	public static final String SYSPARAMXML_PROP_DEV_PICTUREDURATION = "pictureduration";
	public static final String SYSPARAMXML_PROP_MODE_TYPE = "type";
	public static final String SYSPARAMXML_PROP_MODE_DESC = "description";
	public static final String SYSPARAMXML_PROP_LAYOUT_ROWNUM = "rownum";
	public static final String SYSPARAMXML_PROP_LAYOUT_COLUMNNUM = "columnnum";

	private static final Logger sLogger = new Logger();

	public static final void beginDocument(XmlPullParser parser, String firstElementName)
			throws XmlPullParserException, IOException {
	    int type;

	    while ((type=parser.next()) != XmlPullParser.START_TAG
	    		&& type != XmlPullParser.END_DOCUMENT) {
	        ;
	    }

	    if (type != XmlPullParser.START_TAG) {
	        throw new XmlPullParserException("No start tag found");
	    }

	    if (!parser.getName().equals(firstElementName)) {
	        throw new XmlPullParserException("Unexpected start tag: found " + parser.getName() +
	                ", expected " + firstElementName);
	    }
	}
	
	public static final void nextElement(XmlPullParser parser)
			throws XmlPullParserException, IOException {
	    int type;

	    while ((type=parser.next()) != XmlPullParser.START_TAG
	    		&& type != XmlPullParser.END_DOCUMENT) {
	        ;
	    }
	}
	
	public static final XmlSysParam getSysParam() {
		XmlSysParam param = null;

		InputStream in = null;
		try {
			in = MirrorApplication.getInstance().getResources().openRawResource(R.raw.sysparam);

			XmlPullParser parser = Xml.newPullParser();
			parser.setInput(in, Constants.DEFAULT_CHARSET);
			
			int eventType = parser.getEventType();
	        while (eventType != XmlPullParser.END_DOCUMENT) {
	            switch (eventType) {
	            case XmlPullParser.START_DOCUMENT:
	            	param = new XmlSysParam();
	            	break;
	            case XmlPullParser.START_TAG:
	            	if (parser.getName().equals(SYSPARAMXML_TAG)) {
	            		int sysparamversion = Integer.parseInt(parser.getAttributeValue(
	        					null, SYSPARAMXML_VERSION));
	            		sLogger.i("System parameter version of XML is " + sysparamversion);
	            	} else if (parser.getName().equals(SYSPARAMXML_TAG_DEVINFO)) {
	            		param.deviceid = parser.getAttributeValue(
	        					null, SYSPARAMXML_PROP_DEV_ID);
	            		param.devicemodel = parser.getAttributeValue(null,
	            				SYSPARAMXML_PROP_DEV_MODEL);
	            		param.devautozoomtimeout = Integer.parseInt(parser.getAttributeValue(null,
	            				SYSPARAMXML_PROP_DEV_AUTOZOOMTIMEOUT));
	            		param.devpictureduration = Integer.parseInt(parser.getAttributeValue(null,
	            				SYSPARAMXML_PROP_DEV_PICTUREDURATION));
	            	} else if (parser.getName().equals(SYSPARAMXML_TAG_MODEINFO)) {
	            		param.modetype = Integer.parseInt(parser.getAttributeValue(
	        					null, SYSPARAMXML_PROP_MODE_TYPE));
	            		param.modedescription = parser.getAttributeValue(null,
	            				SYSPARAMXML_PROP_MODE_DESC);
	            	} else if (parser.getName().equals(SYSPARAMXML_TAG_LAYOUTINFO)) {
	            		param.layoutrownum = Integer.parseInt(parser.getAttributeValue(
	        					null, SYSPARAMXML_PROP_LAYOUT_ROWNUM));
	            		param.layoutcolumnnum = Integer.parseInt(parser.getAttributeValue(
	        					null, SYSPARAMXML_PROP_LAYOUT_COLUMNNUM));
	            	}
	            	break;
	            }
	
				eventType = parser.next();
	        }
		} catch (XmlPullParserException e) {
			e.printStackTrace();
			param = null;
		} catch (IOException e) {
			e.printStackTrace();
			param = null;
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return param;
	}

}
