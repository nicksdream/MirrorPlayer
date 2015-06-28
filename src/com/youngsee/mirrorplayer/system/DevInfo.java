package com.youngsee.mirrorplayer.system;

public class DevInfo {

	/** Device id */
	public String id;
	/** Device model */
	public String model;
	/** Software version */
	public String softwareversion;
	/** Kernel version */
	public String kernelversion;
	/** Screen width */
	public int screenwidth;
	/** Screen height */
	public int screenheight;
	/** Application path */
	public String applicationpath;
	/** Auto zoom timeout */
	public int autozoomtimeout;
	/** Picture duration */
	public int pictureduration;

	public DevInfo() {
		id = null;
		model = null;
		softwareversion = null;
		kernelversion = null;
		screenwidth = -1;
		screenheight = -1;
		applicationpath = null;
		autozoomtimeout = -1;
		pictureduration = -1;
	}

	public DevInfo(DevInfo info) {
		id = (info.id != null) ? new String(info.id) : null;
		model = (info.model != null) ? new String(info.model) : null;
		softwareversion = (info.softwareversion != null) ? new String(info.softwareversion) : null;
		kernelversion = (info.kernelversion != null) ? new String(info.kernelversion) : null;
		screenwidth = info.screenwidth;
		screenheight = info.screenheight;
		applicationpath = (info.applicationpath != null) ? new String(info.applicationpath) : null;
		autozoomtimeout = info.autozoomtimeout;
		pictureduration = info.pictureduration;
	}

}
