package com.youngsee.mirrorplayer.system;

public class DbSysParam {
	public String deviceid;
	public String devicemodel;
	public String softwareversion;
	public String kernelversion;
	public int screenwidth;
	public int screenheight;
	public String applicationpath;
	public int autozoomtimeout;
	public int checkdistance;
	public int pictureduration;
	public int modetype;
	public String modedescription;
	public int layoutrownum;
	public int layoutcolumnnum;

	public DbSysParam() {
		deviceid = null;
        devicemodel = null;
        softwareversion = null;
        kernelversion = null;
        screenwidth = -1;
        screenheight = -1;
        applicationpath = null;
        autozoomtimeout = -1;
        checkdistance = -1;
        pictureduration = -1;
        modetype = -1;
        modedescription = null;
        layoutrownum = -1;
        layoutcolumnnum = -1;
	}
}
