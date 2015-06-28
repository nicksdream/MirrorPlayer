package com.youngsee.mirrorplayer.manager;

import android.content.Intent;
import android.os.Gpio;

import com.youngsee.mirrorplayer.MirrorApplication;
import com.youngsee.mirrorplayer.common.Actions;
import com.youngsee.mirrorplayer.common.Constants;
import com.youngsee.mirrorplayer.util.Logger;

public class GpioManager {

	private final long DEFAULT_THREAD_PERIOD = 100;

	private Logger mLogger = new Logger();

	private MyThread mMyThread = null;

	private final int GPIO_TOTALNUM = 3;
	private GpioInfo[] mGpioInfoArray = null;

	private class GpioInfo {
		public int number;
		public int status;

		public GpioInfo(int number, int status) {
			this.number = number;
			this.status = status;
		}
	}

	private GpioManager() {
		mGpioInfoArray = new GpioInfo[GPIO_TOTALNUM];
		mGpioInfoArray[0] = new GpioInfo(Constants.GPIO_IO1_NUM, Constants.GPIO_STATUS_NONE);
		mGpioInfoArray[1] = new GpioInfo(Constants.GPIO_IO2_NUM, Constants.GPIO_STATUS_NONE);
		mGpioInfoArray[2] = new GpioInfo(Constants.GPIO_IO3_NUM, Constants.GPIO_STATUS_NONE);

		Gpio.setMulSel(Constants.GPIO_GROUP, Constants.GPIO_IO1_NUM, 0);
    	Gpio.setMulSel(Constants.GPIO_GROUP, Constants.GPIO_IO2_NUM, 0);
    	Gpio.setMulSel(Constants.GPIO_GROUP, Constants.GPIO_IO3_NUM, 0);

		mMyThread = new MyThread();
		mMyThread.start();
	}

	private static class GpioHolder {
        static final GpioManager INSTANCE = new GpioManager();
    }

	public static GpioManager getInstance() {
		return GpioHolder.INSTANCE;
	}

	public void destroy() {
		if (mMyThread != null) {
			mMyThread.cancel();
			mMyThread = null;
		}
	}

	private void informGpioStatusChange(int number, int status) {
    	Intent intent = new Intent(Actions.GPIO_STATUS_CHANGE_ACTION);
        intent.putExtra(Actions.GPIO_STATUS_CHANGE_ACTION_EXTRA_NUMBER, number);
        intent.putExtra(Actions.GPIO_STATUS_CHANGE_ACTION_EXTRA_STATUS, status);
        MirrorApplication.getInstance().sendStickyBroadcast(intent);
    }

	private final class MyThread extends Thread {
		private boolean mIsCanceled = false;

		public void cancel() {
        	mLogger.i("Cancel the GpioManager thread.");
        	mIsCanceled = true;
            interrupt();
        }

		@Override
		public void run() {
			mLogger.i("A new GpioManager thread is started. Thread id is " + getId() + ".");

			int i, status;
			while (!mIsCanceled) {
                try {
                	for (i = 0; i < GPIO_TOTALNUM; i++) {
        				status = Gpio.readGpio(Constants.GPIO_GROUP,
        						mGpioInfoArray[i].number);
        				if (mGpioInfoArray[i].status != status) {
        					mGpioInfoArray[i].status = status;
        					informGpioStatusChange(mGpioInfoArray[i].number,
        							mGpioInfoArray[i].status);
        				}
        			}

                	Thread.sleep(DEFAULT_THREAD_PERIOD);
                } catch (InterruptedException e) {
                	e.printStackTrace();
                }
            }
            
            mLogger.i("GpioManager thread is safely terminated, id is: " + currentThread().getId());
		}
	}

}
