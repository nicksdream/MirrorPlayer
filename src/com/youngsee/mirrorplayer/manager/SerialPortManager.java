package com.youngsee.mirrorplayer.manager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Intent;

import com.youngsee.mirrorplayer.MirrorApplication;
import com.youngsee.mirrorplayer.common.Actions;
import com.youngsee.mirrorplayer.common.Constants;
import com.youngsee.mirrorplayer.common.SerialPort;
import com.youngsee.mirrorplayer.util.Logger;

public class SerialPortManager {

	private final long DEFAULT_READTHREAD_PERIOD = 20;
	private final long DEFAULT_MONITORTHREAD_PERIOD = 100;

	private Logger mLogger = new Logger();

	private final String DEVFILE_SERIALPORT = "/dev/ttyS2";
	private final int BAUTRATE = 9600;

	private final int MAX_BUF_SIZE = 64;

	private ReadThread mReadThread = null;
	private MonitorThread mMonitorThread = null;

	private SerialPort mSerialPort = null;
	private OutputStream mOutputStream = null;
	private InputStream mInputStream = null;

	private byte[] mSendBuffer = {0x55};

	private int mReceiveSize = 0;
	private byte[] mReceiveBuffer = new byte[MAX_BUF_SIZE];

	private int mCurrentStatus = -1;

	private SerialPortManager() {
		try {
			mSerialPort = new SerialPort(new File(DEVFILE_SERIALPORT), BAUTRATE, 0);
			mOutputStream = mSerialPort.getOutputStream();
			mInputStream = mSerialPort.getInputStream();

			mReadThread = new ReadThread();
			mReadThread.start();

	    	mMonitorThread = new MonitorThread();
	    	mMonitorThread.start();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static class SerialPortHolder {
        static final SerialPortManager INSTANCE = new SerialPortManager();
    }

	public static SerialPortManager getInstance() {
		return SerialPortHolder.INSTANCE;
	}

	public void destroy() {
		if (mMonitorThread != null) {
			mMonitorThread.cancel();
			mMonitorThread = null;
		}
		if (mReadThread != null) {
			mReadThread.cancel();
			mReadThread = null;
		}
		if (mSerialPort != null) {
			mSerialPort.close();
			mSerialPort = null;
		}
	}

	private final class MonitorThread extends Thread {
		private boolean mIsCanceled = false;

		public void cancel() {
        	mLogger.i("Cancel the monitor thread.");
        	mIsCanceled = true;
            interrupt();
        }

		@Override
		public void run() {
			mLogger.i("A new monitor thread is started. Thread id is " + getId() + ".");

			while (!mIsCanceled) {
                try {
                	try {
						mOutputStream.write(mSendBuffer);
					} catch (IOException e) {
						e.printStackTrace();
					}

                	Thread.sleep(DEFAULT_MONITORTHREAD_PERIOD);
                } catch (InterruptedException e) {
                	e.printStackTrace();
                }
            }
            
            mLogger.i("Monitor thread is safely terminated, id is: " + currentThread().getId());
		}
	}

	private void informCheckStatusChange(int status) {
    	Intent intent = new Intent(Actions.CHECK_STATUS_CHANGE_ACTION);
        intent.putExtra(Actions.CHECK_STATUS_CHANGE_ACTION_EXTRA_STATUS, status);
        MirrorApplication.getInstance().sendStickyBroadcast(intent);
    }

	private void onDataReceived(final byte[] buffer, final int size) {
		if (buffer == null) {
			mLogger.i("Buffer is null.");
			return;
		} else if (size != 2) {
			mLogger.i("Size is invalid, size = " + size + ".");
			return;
		}

		int receivedistance = (buffer[0] & 0xFF) * 256 + (buffer[1] & 0xFF);
		if (receivedistance > SysParamManager.getInstance().getCheckDistance()) {
			if (mCurrentStatus != Constants.CHECK_STATUS_NONE) {
				mCurrentStatus = Constants.CHECK_STATUS_NONE;
				informCheckStatusChange(mCurrentStatus);
			}
		} else {
			if (mCurrentStatus != Constants.CHECK_STATUS_SOMEONE) {
				mCurrentStatus = Constants.CHECK_STATUS_SOMEONE;
				informCheckStatusChange(mCurrentStatus);
			}
		}
	}

	private class ReadThread extends Thread {
		private boolean mIsCanceled = false;

		public void cancel() {
        	mLogger.i("Cancel the read thread.");
        	mIsCanceled = true;
            interrupt();
        }

		@Override
		public void run() {
			mLogger.i("A new read thread is started. Thread id is " + getId() + ".");

			while (!mIsCanceled) {
				if (mInputStream == null) {
					mLogger.i("Input stream is null.");
					return;
				}

				try {
					try {
						if (mInputStream.available() > 0) {
							mReceiveSize = mInputStream.read(mReceiveBuffer);
							onDataReceived(mReceiveBuffer, mReceiveSize);
						}
					} catch (IOException e) {
						e.printStackTrace();
					}

					Thread.sleep(DEFAULT_READTHREAD_PERIOD);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			mLogger.i("Read thread is safely terminated, id is: " + currentThread().getId());
		}
	}

}
