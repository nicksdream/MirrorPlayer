package com.youngsee.mirrorplayer.system;

public class ModeInfo {

	public int type;
	public String description;

	public ModeInfo() {
		type = -1;
		description = null;
	}

	public ModeInfo(ModeInfo info) {
		type = info.type;
		description = (info.description != null) ? new String(info.description) : null;
	}

}
