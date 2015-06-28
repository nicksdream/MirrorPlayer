package com.youngsee.mirrorplayer.common;

public class FileInfo {
	public String path;
	public long length;
	public long lastmodified;

	public FileInfo(String path, long length, long lastmodified) {
		this.path = path;
		this.length = length;
		this.lastmodified = lastmodified;
	}
}
