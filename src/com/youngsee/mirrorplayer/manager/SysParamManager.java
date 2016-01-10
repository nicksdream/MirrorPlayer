package com.youngsee.mirrorplayer.manager;

import java.io.File;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import android.text.TextUtils;

import com.youngsee.mirrorplayer.common.Constants;
import com.youngsee.mirrorplayer.system.ModeInfo;
import com.youngsee.mirrorplayer.system.DbSysParam;
import com.youngsee.mirrorplayer.system.DevInfo;
import com.youngsee.mirrorplayer.system.LayoutInfo;
import com.youngsee.mirrorplayer.system.SysParam;
import com.youngsee.mirrorplayer.system.XmlSysParam;
import com.youngsee.mirrorplayer.util.DbHelper;
import com.youngsee.mirrorplayer.util.FileUtils;
import com.youngsee.mirrorplayer.util.Logger;
import com.youngsee.mirrorplayer.util.StorageUtil;
import com.youngsee.mirrorplayer.util.SysInfoHelper;
import com.youngsee.mirrorplayer.util.XmlUtil;

public class SysParamManager {

	private Logger mLogger = new Logger();

	ReadWriteLock mReadWriteLock = new ReentrantReadWriteLock();
	private SysParam mSysParam = new SysParam();

	private SysParamManager() {

	}

	private static class SysParamHolder {
        static final SysParamManager INSTANCE = new SysParamManager();
    }

	public static SysParamManager getInstance() {
		return SysParamHolder.INSTANCE;
	}

	public void init() {
		DbSysParam dbsysparam = DbHelper.getInstance().getSysParam();
		if (dbsysparam == null) {
			loadSysParam(true);
		} else {
			mReadWriteLock.writeLock().lock();

			if (mSysParam.devinfo == null) {
				mSysParam.devinfo = new DevInfo();
			}
			mSysParam.devinfo.id = dbsysparam.deviceid;
			mSysParam.devinfo.model = dbsysparam.devicemodel;
			mSysParam.devinfo.softwareversion = dbsysparam.softwareversion;
			mSysParam.devinfo.kernelversion = dbsysparam.kernelversion;
			mSysParam.devinfo.screenwidth = dbsysparam.screenwidth;
			mSysParam.devinfo.screenheight = dbsysparam.screenheight;
			mSysParam.devinfo.applicationpath = dbsysparam.applicationpath;
			mSysParam.devinfo.autozoomtimeout = dbsysparam.autozoomtimeout;
			mSysParam.devinfo.checkdistance = dbsysparam.checkdistance;
			mSysParam.devinfo.pictureduration = dbsysparam.pictureduration;

			if (mSysParam.modeinfo == null) {
				mSysParam.modeinfo = new ModeInfo();
			}
			mSysParam.modeinfo.type = dbsysparam.modetype;
			mSysParam.modeinfo.description = dbsysparam.modedescription;

			if (mSysParam.layoutinfo == null) {
				mSysParam.layoutinfo = new LayoutInfo();
			}
			mSysParam.layoutinfo.rownum = dbsysparam.layoutrownum;
			mSysParam.layoutinfo.columnnum = dbsysparam.layoutcolumnnum;

			/* If the software version from database doesn't equal the one of system,
			 * just use the latter.
			 */
			String syssoftwareversion = SysInfoHelper.getSoftwareVersion();
			if ((syssoftwareversion != null)
					&& (!syssoftwareversion.equals(mSysParam.devinfo.softwareversion))) {
				mSysParam.devinfo.softwareversion = syssoftwareversion;
				DbHelper.getInstance().updateSoftwareVersion(syssoftwareversion);
			} else {
				mLogger.i("System software version is " + syssoftwareversion);
				mLogger.i("Software version from database is " + mSysParam.devinfo.softwareversion);
			}
			
			/* If the kernel version from database doesn't equal the one of system,
			 * just use the latter.
			 */
			String syskernalversion = SysInfoHelper.getKernelVersion();
			if ((syskernalversion != null)
					&& (!syskernalversion.equals(mSysParam.devinfo.kernelversion))) {
				mSysParam.devinfo.kernelversion = syskernalversion;
				DbHelper.getInstance().updateKernelVersion(syskernalversion);
			} else {
				mLogger.i("System kernel version is " + syskernalversion);
				mLogger.i("Kernel version from database is " + mSysParam.devinfo.kernelversion);
			}
			
			/* If the screen width from database doesn't equal the one of system,
			 * just use the latter.
			 */
			int sysscreenwidth = SysInfoHelper.getScreenWidth();
			if (sysscreenwidth != mSysParam.devinfo.screenwidth) {
				mSysParam.devinfo.screenwidth = sysscreenwidth;
				DbHelper.getInstance().updateScreenWidth(sysscreenwidth);
			}
			
			/* If the screen height from database doesn't equal the one of system,
			 * just use the latter.
			 */
			int sysscreenheight = SysInfoHelper.getScreenHeight();
			if (sysscreenheight != mSysParam.devinfo.screenheight) {
				mSysParam.devinfo.screenheight = sysscreenheight;
				DbHelper.getInstance().updateScreenHeight(sysscreenheight);
			}

			mReadWriteLock.writeLock().unlock();
		}
	}

	private String findApplicationPath() {
		String[] storagepaths = StorageUtil.getStoragePaths();
		if (storagepaths == null) {
			mLogger.i("Storage paths is null.");
			return null;
		} else if (storagepaths.length == 0) {
			mLogger.i("There is no storage path.");
			return null;
		}

		String applicationpath = null;
		long maxusablespace = 0;
		for (String path : storagepaths) {
			if (!path.toLowerCase().contains(Constants.USB_LABEL)) {
				File file = new File(path);
				long fileusablespace = file.getUsableSpace();
				if (fileusablespace > maxusablespace) {
					applicationpath = path;
					maxusablespace = fileusablespace;
				}
			}
		}

		if (applicationpath == null) {
			return null;
		}

		StringBuilder sb = new StringBuilder();
		sb.append(applicationpath);
		sb.append(File.separator);
		sb.append(Constants.APPDIR);
		return sb.toString();
	}

	private void loadSysParam(boolean isinitial) {
		XmlSysParam xmlsysparam = XmlUtil.getSysParam();
		if (xmlsysparam == null) {
			mLogger.i("System parameter from XML is null ");
			return;
		}
		
		mReadWriteLock.writeLock().lock();

		if (mSysParam.devinfo == null) {
			mSysParam.devinfo = new DevInfo();
		}
		mSysParam.devinfo.id = xmlsysparam.deviceid;
		mSysParam.devinfo.model = xmlsysparam.devicemodel;
		mSysParam.devinfo.autozoomtimeout = xmlsysparam.devautozoomtimeout;
		mSysParam.devinfo.checkdistance = xmlsysparam.devcheckdistance;
		mSysParam.devinfo.pictureduration = xmlsysparam.devpictureduration;
		
		String softwareversion = SysInfoHelper.getSoftwareVersion();
		String kernelversion = SysInfoHelper.getKernelVersion();
		int screenwidth = SysInfoHelper.getScreenWidth();
		int screenheight = SysInfoHelper.getScreenHeight();
		mSysParam.devinfo.softwareversion = softwareversion;
		mSysParam.devinfo.kernelversion = kernelversion;
		mSysParam.devinfo.screenwidth = screenwidth;
		mSysParam.devinfo.screenheight = screenheight;

		String applicationpath = findApplicationPath();
		mSysParam.devinfo.applicationpath = applicationpath;

		if (mSysParam.modeinfo == null) {
			mSysParam.modeinfo = new ModeInfo();
		}
		mSysParam.modeinfo.type = xmlsysparam.modetype;
		mSysParam.modeinfo.description = xmlsysparam.modedescription;

		if (mSysParam.layoutinfo == null) {
			mSysParam.layoutinfo = new LayoutInfo();
		}
		mSysParam.layoutinfo.rownum = xmlsysparam.layoutrownum;
		mSysParam.layoutinfo.columnnum = xmlsysparam.layoutcolumnnum;

		DbHelper.getInstance().setSysParam(isinitial, xmlsysparam, softwareversion, kernelversion,
				screenwidth, screenheight, applicationpath);

		mReadWriteLock.writeLock().unlock();
	}

	public String getDeviceId() {
		String deviceid = null;

		mReadWriteLock.readLock().lock();

		if ((mSysParam.devinfo != null)
				&& (mSysParam.devinfo.id != null)) {
			deviceid = new String(mSysParam.devinfo.id);
		}

		mReadWriteLock.readLock().unlock();

		return deviceid;
	}
	
	public String getDeviceModel() {
		String devicemodel = null;

		mReadWriteLock.readLock().lock();

		if ((mSysParam.devinfo != null)
				&& (mSysParam.devinfo.model != null)) {
			devicemodel = new String(mSysParam.devinfo.model);
		}

		mReadWriteLock.readLock().unlock();

		return devicemodel;
	}
	
	public String getSoftwareVersion() {
		String softwareversion = null;

		mReadWriteLock.readLock().lock();

		if ((mSysParam.devinfo != null)
				&& (mSysParam.devinfo.softwareversion != null)) {
			softwareversion = new String(mSysParam.devinfo.softwareversion);
		}

		mReadWriteLock.readLock().unlock();

		return softwareversion;
	}
	
	public String getKernelVersion() {
		String kernelversion = null;

		mReadWriteLock.readLock().lock();

		if ((mSysParam.devinfo != null)
				&& (mSysParam.devinfo.kernelversion != null)) {
			kernelversion = new String(mSysParam.devinfo.kernelversion);
		}

		mReadWriteLock.readLock().unlock();

		return kernelversion;
	}
	
	public int getScreenWidth() {
		int screenwidth = -1;

		mReadWriteLock.readLock().lock();

		if (mSysParam.devinfo != null) {
			screenwidth = mSysParam.devinfo.screenwidth;
		}

		mReadWriteLock.readLock().unlock();

		return screenwidth;
	}
	
	public int getScreenHeight() {
		int screenheight = -1;

		mReadWriteLock.readLock().lock();

		if (mSysParam.devinfo != null) {
			screenheight = mSysParam.devinfo.screenheight;
		}

		mReadWriteLock.readLock().unlock();

		return screenheight;
	}

	public String getApplicationPath() {
		String applicationpath = null;

		mReadWriteLock.readLock().lock();

		if ((mSysParam.devinfo != null)
				&& (mSysParam.devinfo.applicationpath != null)) {
			applicationpath = new String(mSysParam.devinfo.applicationpath);
		}

		mReadWriteLock.readLock().unlock();

		return applicationpath;
	}

	public int getAutoZoomTimeout() {
		int autozoomtimeout = -1;

		mReadWriteLock.readLock().lock();

		if (mSysParam.devinfo != null) {
			autozoomtimeout = mSysParam.devinfo.autozoomtimeout;
		}

		mReadWriteLock.readLock().unlock();

		return autozoomtimeout;
	}

	public int getCheckDistance() {
		int checkdistance = -1;

		mReadWriteLock.readLock().lock();

		if (mSysParam.devinfo != null) {
			checkdistance = mSysParam.devinfo.checkdistance;
		}

		mReadWriteLock.readLock().unlock();

		return checkdistance;
	}

	public int getPictureDuration() {
		int pictureduration = -1;

		mReadWriteLock.readLock().lock();

		if (mSysParam.devinfo != null) {
			pictureduration = mSysParam.devinfo.pictureduration;
		}

		mReadWriteLock.readLock().unlock();

		return pictureduration;
	}

	public int getModeType() {
		int modetype = -1;

		mReadWriteLock.readLock().lock();

		if (mSysParam.modeinfo != null) {
			modetype = mSysParam.modeinfo.type;
		}

		mReadWriteLock.readLock().unlock();

		return modetype;
	}
	
	public String getModeDescription() {
		String modedescription = null;

		mReadWriteLock.readLock().lock();

		if ((mSysParam.modeinfo != null)
				&& (mSysParam.modeinfo.description != null)) {
			modedescription = new String(mSysParam.modeinfo.description);
		}

		mReadWriteLock.readLock().unlock();

		return modedescription;
	}

	public int getLayoutRowNum() {
		int rownum = -1;
		
		mReadWriteLock.readLock().lock();

		if (mSysParam.layoutinfo != null) {
			rownum = mSysParam.layoutinfo.rownum;
		}

		mReadWriteLock.readLock().unlock();

		return rownum;
	}

	public int getLayoutColumnNum() {
		int columnnum = -1;
		
		mReadWriteLock.readLock().lock();

		if (mSysParam.layoutinfo != null) {
			columnnum = mSysParam.layoutinfo.columnnum;
		}

		mReadWriteLock.readLock().unlock();

		return columnnum;
	}

	public void setUserParams(int autozoomtimeout, int checkdistance, int pictureduration) {
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

		mReadWriteLock.writeLock().lock();

		if (mSysParam.devinfo == null) {
			mSysParam.devinfo = new DevInfo();
		}
		mSysParam.devinfo.autozoomtimeout = autozoomtimeout;
		mSysParam.devinfo.checkdistance = checkdistance;
		mSysParam.devinfo.pictureduration = pictureduration;

		DbHelper.getInstance().updateUserParams(autozoomtimeout, checkdistance, pictureduration);

		mReadWriteLock.writeLock().unlock();
	}

	public void setModeInfo(int type, String description) {
		if (type < 0) {
			mLogger.i("Mode type is less than zore.");
			return;
		} else if (TextUtils.isEmpty(description)) {
			mLogger.i("Mode description is empty.");
			return;
		}

		mReadWriteLock.writeLock().lock();

		if (mSysParam.modeinfo == null) {
			mSysParam.modeinfo = new ModeInfo();
		}
		mSysParam.modeinfo.type = type;
		mSysParam.modeinfo.description = description;

		DbHelper.getInstance().updateModeInfo(type, description);

		mReadWriteLock.writeLock().unlock();
	}
	
	public void setLayoutInfo(int rownum, int columnnum) {
		if (rownum < 1) {
			mLogger.i("Layout row number is less than 1.");
			return;
		} else if (columnnum < 1) {
			mLogger.i("Layout column number is less than 1.");
			return;
		}

		mReadWriteLock.writeLock().lock();

		if (mSysParam.layoutinfo == null) {
			mSysParam.layoutinfo = new LayoutInfo();
		}
		mSysParam.layoutinfo.rownum = rownum;
		mSysParam.layoutinfo.columnnum = columnnum;

		DbHelper.getInstance().updateLayoutInfo(rownum, columnnum);

		mReadWriteLock.writeLock().unlock();
	}
}
