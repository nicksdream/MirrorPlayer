package com.youngsee.mirrorplayer.system;

public class LayoutInfo {

	public int rownum;
	public int columnnum;

	public LayoutInfo() {
		rownum = -1;
		columnnum = -1;
	}

	public LayoutInfo(LayoutInfo info) {
		rownum = info.rownum;
		columnnum = info.columnnum;
	}

}
