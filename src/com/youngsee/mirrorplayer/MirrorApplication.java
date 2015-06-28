package com.youngsee.mirrorplayer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import libcore.io.DiskLruCache;

import com.youngsee.mirrorplayer.common.Constants;
import com.youngsee.mirrorplayer.common.FileInfo;
import com.youngsee.mirrorplayer.manager.SysParamManager;
import com.youngsee.mirrorplayer.util.BitmapUtil;
import com.youngsee.mirrorplayer.util.FileUtils;
import com.youngsee.mirrorplayer.util.Logger;
import com.youngsee.mirrorplayer.util.SysInfoHelper;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.util.LruCache;

public class MirrorApplication extends Application {

	private Logger mLogger = new Logger();

    private static MirrorApplication INSTANCE = null;
    
    private final int MEMORYCACHEFACTOR = 10;
    
    private final long MAXDISKCACHE = 40 * 1024 * 1024;

    private LruCache<String, Bitmap> mMemoryCache = null;
    
    private DiskLruCache mDiskLruCache = null;

	@Override
    public void onCreate() {
        super.onCreate();

        INSTANCE = this;

        initSysCache();
    }

	public static MirrorApplication getInstance() {
        return INSTANCE;
    }

	private void initSysCache() {
		mMemoryCache = new LruCache<String, Bitmap>(
				(int)Runtime.getRuntime().maxMemory() / MEMORYCACHEFACTOR) {
			@Override
			protected int sizeOf(String key, Bitmap bitmap) {
				return bitmap.getByteCount();
			}

			@Override
			protected void entryRemoved(boolean evicted, String key,
					Bitmap oldBitmap, Bitmap newBitmap) {
				if (evicted) {
					if ((oldBitmap != null) && (!oldBitmap.isRecycled())) {
						oldBitmap.recycle();
						oldBitmap = null;
					}
				}
			}
		};

		try {
			File cacheDir = getDiskCacheDir(this, "thumb");
			if (!cacheDir.exists()) {
				cacheDir.mkdirs();
			}

			mDiskLruCache = DiskLruCache.open(cacheDir,
					SysInfoHelper.getSoftwareVersionCode(), 1, MAXDISKCACHE);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private File getDiskCacheDir(Context context, String uniqueName) {
		String cachePath;

		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
				|| !Environment.isExternalStorageRemovable()) {
			cachePath = context.getExternalCacheDir().getPath();
		} else {
			cachePath = context.getCacheDir().getPath();
		}

		return new File(cachePath + File.separator + uniqueName);
	}

	public Bitmap getBitmapFromMemoryCache(String key) {
        if (mMemoryCache == null) {
        	mLogger.i("System memory cache is null");
            return null;
        }

        return mMemoryCache.get(key);
    }

	public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemoryCache(key) != null) {
        	mLogger.i("Bitmap has been in the system memeory cache.");
            return;
        }

        mMemoryCache.put(key, bitmap);
    }

	public void clearMemoryCache() {
        if (mMemoryCache != null) {
        	mMemoryCache.evictAll();
        	System.gc();
        }
    }

	private String bytesToHexString(byte[] bytes) {
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < bytes.length; i++) {
			String hex = Integer.toHexString(0xFF & bytes[i]);
			if (hex.length() == 1) {
				sb.append('0');
			}
			sb.append(hex);
		}

		return sb.toString();
	}

	private String hashKeyForDisk(String key) {
		String cacheKey;

		try {
			final MessageDigest mDigest = MessageDigest.getInstance("MD5");
			mDigest.update(key.getBytes());
			cacheKey = bytesToHexString(mDigest.digest());
		} catch (NoSuchAlgorithmException e) {
			cacheKey = String.valueOf(key.hashCode());
		}

		return cacheKey;
	}

	public Bitmap getBitmapFromDiskCache(String key) {
		if (mDiskLruCache == null) {
        	mLogger.i("System disk cache is null");
            return null;
        }
		
		Bitmap bitmap = null;
		InputStream in = null;
		try {
			DiskLruCache.Snapshot snapShot = mDiskLruCache.get(hashKeyForDisk(key));
			if (snapShot != null) {
				in = snapShot.getInputStream(0);
		        bitmap = BitmapFactory.decodeStream(in);
		    } else {
		    	mLogger.i("Hash key isn't in the system disk cache, key = "
		    			+ key + ".");
		    }
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	    return bitmap;
	}
	
	private boolean writeBitmapToStream(Bitmap bitmap, OutputStream outputStream) {
		return bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
	}

	public void addBitmapToDiskCache(String key, Bitmap bitmap) {
		if (mDiskLruCache == null) {
        	mLogger.i("System disk cache is null");
            return;
        }
		
		OutputStream out = null;
		try {
			String hashkey = hashKeyForDisk(key);
			DiskLruCache.Snapshot snapShot = mDiskLruCache.get(hashkey);
			if (snapShot == null) {
				DiskLruCache.Editor editor = mDiskLruCache.edit(hashkey);
				if (editor != null) {  
                    out = editor.newOutputStream(0);
                    if (writeBitmapToStream(bitmap, out)) {
                    	editor.commit();
                    } else {
                    	editor.abort();
                    }
                } else {
                	mLogger.i("Editor of system disk cache is null.");
                }
			} else {
				mLogger.i("Hash key has been in the system disk cache, key = "
						+ key + ".");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void clearDiskCache() {
        if (mDiskLruCache != null) {
        	try {
				mDiskLruCache.delete();
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
    }

	public String getStandbyImagePath() {
		StringBuilder sb = new StringBuilder();
		sb.append(SysParamManager.getInstance().getApplicationPath());
		sb.append(File.separator);
		sb.append(Constants.STANDBYDIR);
		sb.append(File.separator);

		if(getResources().getConfiguration().orientation
				== Configuration.ORIENTATION_LANDSCAPE) {
			sb.append(Constants.STANDBYFILENAME_LANDSCAPE);
		} else {
			sb.append(Constants.STANDBYFILENAME_PORTRAIT);
		}

		return sb.toString();
	}

	private String getStandbyImgResName() {
		StringBuilder sb = new StringBuilder();
		sb.append(getPackageName());
		sb.append(".R.drawable.standby");
		return sb.toString();
	}

	public Bitmap getStandbyImg() {
		String img = getStandbyImagePath();
		Bitmap standbyimg = getBitmapFromMemoryCache(img);
		if ((standbyimg == null) && FileUtils.isExist(img)) {
			// Load the standby image.
			standbyimg = BitmapUtil.decodeFile(img,
					SysParamManager.getInstance().getScreenWidth(),
					SysParamManager.getInstance().getScreenHeight());
			if (standbyimg != null) {
				addBitmapToMemoryCache(img, standbyimg);
			}
		}

		// Try to load the standby resource image.
		if (standbyimg == null) {
			img = getStandbyImgResName();
			standbyimg = getBitmapFromMemoryCache(img);
			if (standbyimg == null) {
				// Try to load the standby image.
				standbyimg = BitmapUtil.decodeResource(getResources(), R.drawable.standby,
						SysParamManager.getInstance().getScreenWidth(),
						SysParamManager.getInstance().getScreenHeight());
				if (standbyimg != null) {
					addBitmapToMemoryCache(img, standbyimg);
				}
			}
		}

		return standbyimg;
	}

	public boolean isNetworkConnected() {
        ConnectivityManager connectivity =
        		(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (info != null && info.isAvailable()) {
                return info.isConnected();
            }
        }
        return false;
    }

	public boolean isForbidToDownload() {
		// TBD
		return false;
	}

}
