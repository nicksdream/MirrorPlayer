package com.youngsee.mirrorplayer.activity;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.youngsee.mirrorplayer.R;
import com.youngsee.mirrorplayer.MirrorApplication;
import com.youngsee.mirrorplayer.common.Actions;
import com.youngsee.mirrorplayer.common.Constants;
import com.youngsee.mirrorplayer.common.MediaInfo;
import com.youngsee.mirrorplayer.manager.SerialPortManager;
import com.youngsee.mirrorplayer.manager.SysParamManager;
import com.youngsee.mirrorplayer.util.FileUtils;
import com.youngsee.mirrorplayer.util.Logger;
import com.youngsee.mirrorplayer.util.StorageUtil;
import com.youngsee.mirrorplayer.view.MirrorMultiMediaView;
import com.youngsee.mirrorplayer.view.MirrorView;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.BitmapDrawable;

public class MirrorActivity extends Activity {
	
	private final String TAG = "AdMainActivity";

	private Logger mLogger = new Logger();

	private final int MENUITEM_BASE = 0x1000;
	private final int MENUITEM_UPDATEMATERIALS = MENUITEM_BASE + 0;
	private final int MENUITEM_CHANGELAYOUT = MENUITEM_BASE + 1;
	private final int MENUITEM_MODIFYSYSPARAMS = MENUITEM_BASE + 2;

	private final int EVENT_BASE = 0x9000;
	private final int EVENT_SHOWSTANDBYPGM = EVENT_BASE + 0;
	private final int EVENT_AUTOZOOMOUT_TIMEOUT = EVENT_BASE + 1;

	private final long DEFAULT_AUTOZOOMOUT_TIMEOUT = 1000;

	private final int STATUS_IDLE = 0;
	private final int STATUS_FULLSCREEN = 1;
	private final int STATUS_AUTOZOOMOUT = 2;

	private PowerManager.WakeLock mWakeLock = null;

	private SerialPortManager mSerialPortManager = null;

	private FrameLayout mFrameLayout= null;

	private List<MirrorViewInfo> mViewInfoLst = null;

	private int mScreenWidth = -1;
	private int mScreenHeight = -1;
	private int mAutoZoomOutWidth = -1;
	private int mAutoZoomOutHeight = -1;

	private List<MediaInfo> mMediaLst = null;

	private MirrorReceiver mMirrorReceiver = null;
	private IntentFilter mMirrorReceiverFilter = null;

	private int mCurrentStatus = STATUS_IDLE;

	private List<MenuItem> mMenuItemLst = null;

	private UpdateTask mUpdateTask = null;
	private ProgressDialog mUpdateProgressBar = null;

	private AlertDialog mAlertDialog = null;

	private long mExitTime = 0;

	private class MenuItem {
		public String name;
		public int code;

		public MenuItem(String name, int code) {
			this.name = name;
			this.code = code;
		}
	}

	private class MirrorViewInfo {
		public int x;
		public int y;
		public int w;
		public int h;
		public MirrorView view;
		
		public MirrorViewInfo(int x, int y, int w, int h, MirrorView view) {
			this.x = x;
			this.y = y;
			this.w = w;
			this.h = h;
			this.view = view;
		}
	}

	private class ProgressInfo {
		public int status;
		public int progress;
		public int totalnumber;
		public int filenumber;
		public String filename;

		public ProgressInfo(int status, int progress, int totalnumber, int filenumber,
				String filename) {
			this.status = status;
			this.progress = progress;
			this.totalnumber = totalnumber;
			this.filenumber = filenumber;
			this.filename = filename;
		}
	}

	private interface ProgressListener {
		public void onError();
		public void onStart(int totalnumber, int number, String filename);
		public void onProgress(int totalnumber, int number, String filename, int progress);
	}

	private class SortInfo {
    	public int number;
    	public int index;

    	public SortInfo(int number, int index) {
    		this.number = number;
    		this.index = index;
    	}
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mirror);

        initMenuParams();

        mFrameLayout = (FrameLayout)findViewById(R.id.activity_ad_lyt);
        mFrameLayout.setOnLongClickListener(mOnLongClickListener);
        
        PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(
        		PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.FULL_WAKE_LOCK, TAG);

        SysParamManager.getInstance().init();

        initParams();

        initViews();

        initReceiver();

        mSerialPortManager = SerialPortManager.getInstance();
    }

    private void initMenuParams() {
    	mMenuItemLst = new ArrayList<MenuItem>();
    	mMenuItemLst.add(new MenuItem(
    			getResources().getString(R.string.appmenu_item_updatematerials),
    			MENUITEM_UPDATEMATERIALS));
    	mMenuItemLst.add(new MenuItem(
    			getResources().getString(R.string.appmenu_item_changelayout),
    			MENUITEM_CHANGELAYOUT));
    	mMenuItemLst.add(new MenuItem(
    			getResources().getString(R.string.appmenu_item_modifysysparams),
    			MENUITEM_MODIFYSYSPARAMS));
    }

    private String getMaterialsPath() {
    	StringBuilder sb = new StringBuilder();
    	sb.append(SysParamManager.getInstance().getApplicationPath());
    	sb.append(File.separator);
    	sb.append(Constants.MATERIALSDIR);
    	return sb.toString();
    }

    private int getMediaNumber(String path) {
    	if (TextUtils.isEmpty(path)) {
    		mLogger.i("Path is empty.");
    		return Integer.MAX_VALUE;
    	}

    	Matcher m = Pattern.compile("\\d+").matcher(path);
		if (m.find()) {
			return Integer.parseInt(m.group(0));
		}

		return Integer.MAX_VALUE;
    }

    private List<MediaInfo> sortMaterials(List<MediaInfo> lst) {
    	if (lst == null) {
    		mLogger.i("Media list is null.");
    		return null;
    	} else if (lst.isEmpty()) {
    		mLogger.i("Media list is empty.");
    		return null;
    	}

    	int i, j;
    	int size = lst.size();
    	SortInfo[] sortinfos = new SortInfo[size];
    	for (i = 0; i < size; i++) {
    		sortinfos[i] = new SortInfo(getMediaNumber(
    				FileUtils.getFilenameNoExt(lst.get(i).path)), i);
    	}

    	SortInfo tmpinfo;
    	for (i = 0; i < size; i++) {
    		for (j = i + 1; j < size; j++) {
    			if (sortinfos[i].number > sortinfos[j].number) {
    				tmpinfo = sortinfos[i];
    				sortinfos[i] = sortinfos[j];
    				sortinfos[j] = tmpinfo;
    			}
    		}
    	}

    	List<MediaInfo> sortlst = new ArrayList<MediaInfo>();
    	for (i = 0; i < size; i++) {
    		sortlst.add(lst.get(sortinfos[i].index));
    	}
    	return sortlst;
    }

    private void initParams() {
    	mViewInfoLst = new ArrayList<MirrorViewInfo>();

        mScreenWidth = SysParamManager.getInstance().getScreenWidth();
        mScreenHeight = SysParamManager.getInstance().getScreenHeight();
        mAutoZoomOutWidth = mScreenWidth/SysParamManager.getInstance().getLayoutColumnNum();
        mAutoZoomOutHeight = mScreenHeight/SysParamManager.getInstance().getLayoutRowNum();

        mMediaLst = sortMaterials(FileUtils.getMediaInfo(getMaterialsPath(), true));
    }

    private void initViews() {
    	MirrorView view = new MirrorMultiMediaView(this);
    	view.setIndex(0);
    	view.setViewWidth(mScreenWidth);
    	view.setViewHeight(mScreenHeight);
    	view.setMediaList(mMediaLst);

    	view.setX(0);
    	view.setY(0);
		mFrameLayout.addView(view, mScreenWidth, mScreenHeight);

		MirrorViewInfo viewinfo = new MirrorViewInfo(0, 0, mScreenWidth, mScreenHeight, view);
		mViewInfoLst.add(viewinfo);

		startViews();
		mCurrentStatus = STATUS_FULLSCREEN;
    }

    private void initReceiver() {
		mMirrorReceiver = new MirrorReceiver();

		mMirrorReceiverFilter = new IntentFilter();
		mMirrorReceiverFilter.addAction(Actions.CHECK_STATUS_CHANGE_ACTION);
	}

    @Override
	protected void onResume() {
		super.onResume();
		
		if (mWakeLock != null) {
        	mWakeLock.acquire();
        }
		
		if (mViewInfoLst != null) {
    		for (MirrorViewInfo info : mViewInfoLst) {
    			info.view.onResume();
    		}
    	}

		registerReceiver(mMirrorReceiver, mMirrorReceiverFilter);
    }

    @Override
	protected void onPause() {
    	unregisterReceiver(mMirrorReceiver);

    	cleanupMsg();

    	if (mViewInfoLst != null) {
    		for (MirrorViewInfo info : mViewInfoLst) {
    			info.view.onPause();
    		}
    	}

    	if (mWakeLock != null) {
        	mWakeLock.release();
        }

    	super.onPause();
    }

    @Override
	protected void onDestroy() {
    	cleanupMsg();

    	if (mViewInfoLst != null) {
    		for (MirrorViewInfo info : mViewInfoLst) {
    			info.view.onDestroy();
    		}
    	}

    	MirrorApplication.getInstance().clearMemoryCache();

    	if (mSerialPortManager != null) {
    		mSerialPortManager.destroy();
    		mSerialPortManager = null;
    	}

    	super.onDestroy();
    }

    @Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			if ((System.currentTimeMillis() - mExitTime) > 2000){
	            Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();
	            mExitTime = System.currentTimeMillis();
	        } else {
	            finish();
	            System.exit(0);
	        }
            return true;
        case KeyEvent.KEYCODE_MENU:
        	if ((mUpdateProgressBar != null) && mUpdateProgressBar.isShowing()) {
        		mLogger.i("Progress bar is showing, ignore...");
        		return true;
        	}
        	if ((mAlertDialog != null) && mAlertDialog.isShowing()) {
        		mAlertDialog.dismiss();
        		return true;
        	}

        	showMenuDialog();
            return true;
        case KeyEvent.KEYCODE_PAGE_UP:
            return true;
        case KeyEvent.KEYCODE_PAGE_DOWN:
            return true;
        case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
            return true;
        case KeyEvent.KEYCODE_MEDIA_STOP:
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    private String findUdiskMaterialsPath() {
    	String[] storagepaths = StorageUtil.getStoragePaths();
		if (storagepaths == null) {
			mLogger.i("Storage paths is null.");
			return null;
		} else if (storagepaths.length == 0) {
			mLogger.i("There is no storage path.");
			return null;
		}

		for (String path : storagepaths) {
			if (path.toLowerCase().contains(Constants.USB_LABEL)) {
				File file = new File(path);
				if (file.getTotalSpace() > 0) {
					File[] udiskfiles = file.listFiles();
					if (udiskfiles != null) {
        				for (File udiskfile : udiskfiles) {
        					if (udiskfile.isDirectory()
        							&& udiskfile.getName().equals(Constants.UDISK_MATERIALSDIR)) {
        						return udiskfile.getAbsolutePath();
        					}
        				}
					}
				} else {
					File[] subfiles = file.listFiles();
					if (subfiles != null) {
    					for (File subfile : subfiles) {
    						File[] udiskfiles = subfile.listFiles();
    						if (udiskfiles != null) {
	            				for (File udiskfile : udiskfiles) {
	            					if (udiskfile.isDirectory()
	            							&& udiskfile.getName().equals(Constants.UDISK_MATERIALSDIR)) {
	            						return udiskfile.getAbsolutePath();
	            					}
	            				}
    						}
    					}
					}
				}
			}
		}

		return null;
    }

    private class UpdateTask extends AsyncTask<String, ProgressInfo, Integer> {

    	private final int SUCCESS = 0;
    	private final int FAILURE_PATHEXCEPTION = 1;
    	private final int FAILURE_NUMBEREXCEPTION = 2;
    	private final int FAILURE_CANCELED = 3;

    	private final int STATUS_INITIALIZE = 0;
    	private final int STATUS_START = 1;
    	private final int STATUS_PROGRESS = 2;

    	private final int MAX_PROGRESS = 100;

    	private final int DEFAULT_BUFFERSIZE = 128 * 1024;

    	private int mTotalNumber = -1;
    	private int mCurrentNumber = -1;

    	private void copyFile(File srcfile, File destfile, ProgressListener listener)
    			throws InterruptedException {
        	FileInputStream fis = null;
        	FileOutputStream fos = null;
        	BufferedInputStream bis = null;
        	BufferedOutputStream bos = null;
        	mCurrentNumber++;
    		try {
    			fis = new FileInputStream(srcfile);
    			bis = new BufferedInputStream(fis);

    			fos = new FileOutputStream(destfile);
    			bos = new BufferedOutputStream(fos);

    			String filepath = srcfile.getAbsolutePath();
    			String filename = filepath.substring(
    					filepath.indexOf(Constants.UDISK_MATERIALSDIR) +
    					Constants.UDISK_MATERIALSDIR.length() + 1);
    			long filelength = srcfile.length();

    			if (listener != null) {
    				listener.onStart(mTotalNumber, mCurrentNumber, filename);
    			}

    			byte[] buf = new byte[DEFAULT_BUFFERSIZE];
    	        int len;
    	        int progress;
    	        long currlen = 0;
    	        while ((len = bis.read(buf)) != -1) {
    	        	bos.write(buf, 0, len);
    	        	currlen += len;
    	        	progress = (int)(((float)currlen/filelength)*MAX_PROGRESS);
    	        	if (listener != null) {
    					listener.onProgress(mTotalNumber, mCurrentNumber, filename, progress);
    				}
    	        	Thread.sleep(20);
    	        }
    	        bos.flush();
    		} catch (FileNotFoundException e) {
    			e.printStackTrace();
    		} catch (IOException e) {
    			e.printStackTrace();
    		} finally {
    			if (bis != null) {
    				try {
    					bis.close();
    				} catch (IOException e) {
    					e.printStackTrace();
    				}
    			}
    			if (bos != null) {
    				try {
    					bos.close();
    				} catch (IOException e) {
    					e.printStackTrace();
    				}
    			}
    			if (fos != null) {
    				try {
    					fos.close();
    				} catch (IOException e) {
    					e.printStackTrace();
    				}
    			}
    			if (fis != null) {
    				try {
    					fis.close();
    				} catch (IOException e) {
    					e.printStackTrace();
    				}
    			}
    		}
        }

        private void copyFiles(String srcdir, String destdir, ProgressListener listener)
        		throws InterruptedException {
            if (TextUtils.isEmpty(srcdir)) {
            	mLogger.i("Source directory is empty.");
            	if (listener != null) {
            		listener.onError();
            	}
                return;
            } else if (TextUtils.isEmpty(destdir)) {
            	mLogger.i("Destination directory is empty.");
            	if (listener != null) {
            		listener.onError();
            	}
                return;
            }

            File fsrcdir = new File(srcdir);
            if (!fsrcdir.exists()) {
            	mLogger.i("Source directory doesn't exist.");
            	if (listener != null) {
            		listener.onError();
            	}
                return;
            } else if (!fsrcdir.isDirectory()) {
            	mLogger.i("Source directory isn't a directory.");
            	if (listener != null) {
            		listener.onError();
            	}
                return;
            }

            File fdestdir = new File(destdir);
            if (!fdestdir.exists()) {
            	fdestdir.mkdirs();
            }

            String destpath = fdestdir.getAbsolutePath();
            File destfile = null;
            File[] srcfiles = fsrcdir.listFiles();
            if (srcfiles != null) {
    	        for (File srcfile : srcfiles) {
    	        	destfile = new File(destpath + File.separator + srcfile.getName());
    	            if (srcfile.isFile()) {
    	            	copyFile(srcfile, destfile, listener);
    	            } else if (srcfile.isDirectory()) {
    	            	copyFiles(srcfile.getAbsolutePath(), destfile.getAbsolutePath(),
    	            			listener);
    	            }
    	        }
            }
        }

    	@Override
        protected void onPreExecute() {
            mLogger.i("onPreExecute is called.");
        }

		@Override
		protected Integer doInBackground(String... params) {
			mLogger.i("doInBackground is called.");

			String udiskmaterialspath = findUdiskMaterialsPath();
			if (udiskmaterialspath == null) {
				mLogger.i("Udisk materials path is null.");
				return FAILURE_PATHEXCEPTION;
			}

			mTotalNumber = FileUtils.getFileNumber(udiskmaterialspath, true);
			if (mTotalNumber <= 0) {
				mLogger.i("There is no file which needs to copy.");
				return FAILURE_NUMBEREXCEPTION;
			}

			stopViews();
            mMediaLst = null;
			FileUtils.cleanupDir(getMaterialsPath());
			MirrorApplication.getInstance().clearMemoryCache();

			mCurrentNumber = 0;
			publishProgress(new ProgressInfo(STATUS_INITIALIZE, -1, -1, -1, null));
			try {
				copyFiles(udiskmaterialspath, getMaterialsPath(), mProgressListener);
			} catch (InterruptedException e) {
				e.printStackTrace();
				FileUtils.cleanupDir(getMaterialsPath());
				return FAILURE_CANCELED;
			}

			return SUCCESS;
		}

		private ProgressListener mProgressListener = new ProgressListener() {

			@Override
			public void onError() {
				
			}

			@Override
			public void onStart(int totalnumber, int number, String filename) {
				publishProgress(new ProgressInfo(STATUS_START, 0, totalnumber, number, filename));
			}

			@Override
			public void onProgress(int totalnumber, int number, String filename, int progress) {
				publishProgress(new ProgressInfo(STATUS_PROGRESS, progress, totalnumber, number,
						filename));
			}

		};

		@Override
		protected void onProgressUpdate(ProgressInfo... progressinfo) {
			switch (progressinfo[0].status) {
			case STATUS_INITIALIZE:
				if (mUpdateProgressBar == null) {
					mUpdateProgressBar = new ProgressDialog(MirrorActivity.this);
					mUpdateProgressBar.setTitle(R.string.progressbardlg_title);
					mUpdateProgressBar.setMessage(
							getResources().getString(R.string.progressbardlg_msg_initialize));
					mUpdateProgressBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
					mUpdateProgressBar.setProgressDrawable(
							getResources().getDrawable(R.drawable.progressbar));
					mUpdateProgressBar.setCancelable(false);
					mUpdateProgressBar.setButton(DialogInterface.BUTTON_POSITIVE,
							getResources().getString(R.string.dialoglabel_cancel),
							new DialogInterface.OnClickListener() {
	
								@Override
								public void onClick(DialogInterface dialog, int which) {
									if (mUpdateTask != null) {
										mUpdateTask.cancel(true);
									}
									if (mUpdateProgressBar != null) {
										mUpdateProgressBar.cancel();
										mUpdateProgressBar = null;
									}
								}
	
							});
					mUpdateProgressBar.show();
				}

				break;
			case STATUS_START:
				if (mUpdateProgressBar != null) {
					StringBuilder sb = new StringBuilder();
					sb.append(getResources().getString(R.string.progressbardlg_msg_start))
							.append(progressinfo[0].filename);
					mUpdateProgressBar.setMessage(sb.toString());
					mUpdateProgressBar.setMax(MAX_PROGRESS);
					mUpdateProgressBar.setProgress(0);
					mUpdateProgressBar.setProgressNumberFormat(String.format(
							getResources().getString(R.string.progressbardlg_numberformat),
									progressinfo[0].filenumber, progressinfo[0].totalnumber));
				}

				break;
			case STATUS_PROGRESS:
				if (mUpdateProgressBar != null) {
					mUpdateProgressBar.setProgress(progressinfo[0].progress);
				}

				break;
			default:
				mLogger.i("Unknown progress status: " + progressinfo[0].status + ".");
				break;
			}
		}

		@Override
		protected void onPostExecute(Integer result) {
			mLogger.i("onPostExecute is called.");

			if (mUpdateProgressBar != null) {
				mUpdateProgressBar.dismiss();
				mUpdateProgressBar = null;
			}

			switch (result) {
			case SUCCESS:
				mMediaLst = sortMaterials(FileUtils.getMediaInfo(getMaterialsPath(), true));
				mViewInfoLst.get(0).view.setMediaList(mMediaLst);
				startViews();
				break;
			case FAILURE_PATHEXCEPTION:
				mAlertDialog = new AlertDialog.Builder(MirrorActivity.this)
						.setIcon(android.R.drawable.ic_dialog_alert)
						.setTitle(R.string.dialogtitle_updatematerials_failure)
						.setMessage(R.string.dialogmessage_updatematerials_pathexception)
						.setPositiveButton(R.string.dialoglabel_ok, null)
						.show();
				break;
			case FAILURE_NUMBEREXCEPTION:
				mAlertDialog = new AlertDialog.Builder(MirrorActivity.this)
						.setIcon(android.R.drawable.ic_dialog_alert)
						.setTitle(R.string.dialogtitle_updatematerials_failure)
						.setMessage(R.string.dialogmessage_updatematerials_numberexception)
						.setPositiveButton(R.string.dialoglabel_ok, null)
						.show();
				break;
			default:
				mLogger.i("Unknown result: " + result + ".");
				break;
			}

			mUpdateTask = null;
		}

		@Override
		protected void onCancelled(Integer result) {
			mLogger.i("onCancelled is called, result is " + result + ".");
			if (mUpdateProgressBar != null) {
				mUpdateProgressBar.cancel();
				mUpdateProgressBar = null;
			}
			mUpdateTask = null;
		}

    }

    private String[] getMenuItems(List<MenuItem> menuitemlst) {
    	if (menuitemlst == null) {
    		mLogger.i("Menu item list is null.");
    		return null;
    	} else if (menuitemlst.isEmpty()) {
    		mLogger.i("Menu item list is empty.");
    		return null;
    	}

    	int size = menuitemlst.size();
    	String[] menuitems = new String[size];
    	for (int i = 0; i < size; i++) {
    		menuitems[i] = menuitemlst.get(i).name;
    	}

    	return menuitems;
    }

    private void updateMaterials() {
    	mAlertDialog = new AlertDialog.Builder(this)
    			.setIcon(android.R.drawable.ic_dialog_info)
    			.setTitle(R.string.dialogtitle_updatematerials)
    			.setPositiveButton(R.string.dialoglabel_udiskupdate, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (mUpdateTask == null) {
							mUpdateTask = new UpdateTask();
							mUpdateTask.execute();
						}
					}

    			})
    			.setNegativeButton(R.string.dialoglabel_cancel, null)
    			.show();
    }

    private void changeLayout() {
    	final View dlgview = LayoutInflater.from(this).inflate(R.layout.dialog_changelayout, null);
    	final EditText edtxt_rows = (EditText)dlgview.findViewById(R.id.edtxt_rows);
    	final EditText edtxt_columns = (EditText)dlgview.findViewById(R.id.edtxt_columns);
    	edtxt_rows.setText(String.valueOf(SysParamManager.getInstance().getLayoutRowNum()));
    	edtxt_columns.setText(String.valueOf(SysParamManager.getInstance().getLayoutColumnNum()));

    	mAlertDialog = new AlertDialog.Builder(this)
				.setIcon(android.R.drawable.ic_dialog_info)
				.setTitle(R.string.dialogtitle_changelayout)
				.setView(dlgview)
				.setPositiveButton(R.string.dialoglabel_ok, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						int rows = Integer.parseInt(edtxt_rows.getText().toString());
						if (rows < 1) {
							mLogger.i("Change it as 1 if rows is less than 1, rows = "
									+ rows + ".");
							rows = 1;
						}
						int columns = Integer.parseInt(edtxt_columns.getText().toString());
						if (columns < 1) {
							mLogger.i("Change it as 1 if columns is less than 1, columns = "
									+ columns + ".");
							columns = 1;
						}
						SysParamManager.getInstance().setLayoutInfo(rows, columns);
						mAutoZoomOutWidth = mScreenWidth/columns;
				        mAutoZoomOutHeight = mScreenHeight/rows;
				        if (mCurrentStatus == STATUS_AUTOZOOMOUT) {
				        	autoZoomOut();
				        }
					}

				})
				.setNegativeButton(R.string.dialoglabel_cancel, null)
				.show();
    }

    private void modifySysParams() {
    	final View dlgview = LayoutInflater.from(this).inflate(R.layout.dialog_modifysysparams, null);
    	final EditText edtxt_autozoomtimeout = (EditText)dlgview.findViewById(
    			R.id.edtxt_autozoomtimeout);
    	final EditText edtxt_checkdistance = (EditText)dlgview.findViewById(
    			R.id.edtxt_checkdistance);
    	final EditText edtxt_pictureduration = (EditText)dlgview.findViewById(
    			R.id.edtxt_pictureduration);
    	edtxt_autozoomtimeout.setText(String.valueOf(
    			SysParamManager.getInstance().getAutoZoomTimeout()));
    	edtxt_checkdistance.setText(String.valueOf(
    			SysParamManager.getInstance().getCheckDistance()));
    	edtxt_pictureduration.setText(String.valueOf(
    			SysParamManager.getInstance().getPictureDuration()));

    	mAlertDialog = new AlertDialog.Builder(this)
				.setIcon(android.R.drawable.ic_dialog_info)
				.setTitle(R.string.dialogtitle_modifysysparams)
				.setView(dlgview)
				.setPositiveButton(R.string.dialoglabel_ok, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						int autozoomtimeout = Integer.parseInt(
								edtxt_autozoomtimeout.getText().toString());
						if (autozoomtimeout < 1) {
							mLogger.i("Change it as 1 if auto zoom timeout is less than 1, timeout = "
									+ autozoomtimeout + ".");
							autozoomtimeout = 1;
						}
						int checkdistance = Integer.parseInt(
								edtxt_checkdistance.getText().toString());
						if (checkdistance < 20) {
							mLogger.i("Change it as 20 if check distance is less than 20, distance = "
									+ checkdistance + ".");
							checkdistance = 20;
						}
						int pictureduration = Integer.parseInt(
								edtxt_pictureduration.getText().toString());
						if (pictureduration < 1) {
							mLogger.i("Change it as 1 if picture duration is less than 1, duration = "
									+ pictureduration + ".");
							pictureduration = 1;
						}
						SysParamManager.getInstance().setUserParams(autozoomtimeout, checkdistance, pictureduration);
					}

				})
				.setNegativeButton(R.string.dialoglabel_cancel, null)
				.show();
    }

	private DialogInterface.OnClickListener mMenuDialogClickListener = new DialogInterface.OnClickListener() {

		@Override
		public void onClick(DialogInterface dialog, int which) {
			if (which >= mMenuItemLst.size()) {
				mLogger.e("Invalid item number, which = " + which);
				return;
			}

			int code = mMenuItemLst.get(which).code;
			switch (code) {
			case MENUITEM_UPDATEMATERIALS:
				updateMaterials();

				break;
			case MENUITEM_CHANGELAYOUT:
				changeLayout();

				break;
			case MENUITEM_MODIFYSYSPARAMS:
				modifySysParams();

				break;
			default:
				mLogger.i("Unknown menu item, code = " + code);
				break;
			}
		}

	};

    private void showMenuDialog() {
    	mAlertDialog = new AlertDialog.Builder(this)
    			.setTitle(R.string.appmenu_title)
    			.setItems(getMenuItems(mMenuItemLst), mMenuDialogClickListener)
    			.show();
    }

    private OnLongClickListener mOnLongClickListener = new OnLongClickListener() {

		@Override
		public boolean onLongClick(View v) {
			showMenuDialog();
			return false;
		}

    };

    private void cleanupMsg() {
    	mHandler.removeMessages(EVENT_SHOWSTANDBYPGM);
    }

    private void cleanupLayout() {
    	mFrameLayout.setBackground(null);
    	
    	if (mViewInfoLst != null) {
	    	for (MirrorViewInfo info : mViewInfoLst) {
	    		info.view.onDestroy();
	    	}
	    	mFrameLayout.removeAllViews();
	    	mViewInfoLst.clear();
	    	mViewInfoLst = null;
    	}
    }

    private void doShowStandbyPgm() {
    	cleanupLayout();

    	mLogger.i("Show Standby program.");

    	mFrameLayout.setBackground(new BitmapDrawable(getResources(), 
    			MirrorApplication.getInstance().getStandbyImg()));
    }

    private void startViews() {
    	if (mViewInfoLst != null) {
	    	for (MirrorViewInfo info : mViewInfoLst) {
	    		info.view.start();
	    	}
    	}
    }

    private void stopViews() {
    	if (mViewInfoLst != null) {
	    	for (MirrorViewInfo info : mViewInfoLst) {
	    		info.view.stop();
	    	}
    	}
    }

    public void showStandbyPgm() {
    	Message msg = mHandler.obtainMessage();
		msg.what = EVENT_SHOWSTANDBYPGM;
		msg.sendToTarget();
    }

    private void zoomInOrOut(int width, int height) {
    	MirrorViewInfo mvinfo = mViewInfoLst.get(0);
    	mvinfo.w = width;
    	mvinfo.h = height;

		mvinfo.view.setViewWidth(width);
		mvinfo.view.setViewHeight(height);

		FrameLayout.LayoutParams layoutparams =
				(FrameLayout.LayoutParams)mvinfo.view.getLayoutParams();
		layoutparams.width = width;
		layoutparams.height = height;
		mvinfo.view.setLayoutParams(layoutparams);
    }

    private void recoverFullScreen() {
    	zoomInOrOut(mScreenWidth, mScreenHeight);
    }

    private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case EVENT_SHOWSTANDBYPGM:
				doShowStandbyPgm();

				break;
			case EVENT_AUTOZOOMOUT_TIMEOUT:
				recoverFullScreen();
				mCurrentStatus = STATUS_FULLSCREEN;

				break;
            default:
            	mLogger.i("Unknown event, msg.what = " + msg.what + ".");
                break;
            }

            super.handleMessage(msg);
		}
    };

    private void autoZoomOut() {
    	zoomInOrOut(mAutoZoomOutWidth, mAutoZoomOutHeight);
    }

    private class MirrorReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(Actions.CHECK_STATUS_CHANGE_ACTION)) {
				int status = intent.getIntExtra(
						Actions.CHECK_STATUS_CHANGE_ACTION_EXTRA_STATUS, -1);
				switch (status) {
				case Constants.CHECK_STATUS_NONE:
					if ((mCurrentStatus == STATUS_AUTOZOOMOUT)
							&& !mHandler.hasMessages(EVENT_AUTOZOOMOUT_TIMEOUT)) {
						long timeoutmillis;
						int timeout = SysParamManager.getInstance().getAutoZoomTimeout();
						if (timeout >= 1) {
							timeoutmillis = timeout * 1000;
						} else {
							mLogger.i("Auto zoom timeout is invalid, timeout = " + timeout + ".");
						    mLogger.i("Use default auto zoom timeout...");
						    timeoutmillis = DEFAULT_AUTOZOOMOUT_TIMEOUT;
						}
						mHandler.sendEmptyMessageDelayed(EVENT_AUTOZOOMOUT_TIMEOUT, timeoutmillis);
					}

					break;
				case Constants.CHECK_STATUS_SOMEONE:
					mHandler.removeMessages(EVENT_AUTOZOOMOUT_TIMEOUT);

					if (mCurrentStatus == STATUS_FULLSCREEN) {
						autoZoomOut();
						mCurrentStatus = STATUS_AUTOZOOMOUT;
					}

					break;
				default:
					mLogger.i("Check status is invalid, skip. Status is " + status);
					break;
				}
			}
		}
    }
}
