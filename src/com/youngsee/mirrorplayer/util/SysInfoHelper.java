package com.youngsee.mirrorplayer.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import com.youngsee.mirrorplayer.MirrorApplication;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.text.format.Time;
import android.util.DisplayMetrics;
import android.view.WindowManager;

public class SysInfoHelper {
	private static Logger sLogger = new Logger();
	
	/**
	 * 获取屏幕宽度
	 * 
	 * @return 屏幕宽度
	 */
	public static int getScreenWidth() {
		WindowManager wm = (WindowManager) MirrorApplication.getInstance().getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics dm = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(dm);

		return dm.widthPixels;
	}
	
	/**
	 * 获取屏幕高度
	 * 
	 * @return 屏幕高度
	 */
	public static int getScreenHeight() {
		WindowManager wm = (WindowManager) MirrorApplication.getInstance().getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics dm = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(dm);

		return dm.heightPixels;
	}

	/**
	 * 获取软件名称
	 * 
	 * @return 软件名称
	 */
	public static String getApplicationName() {
	    try {
	        PackageManager pm = MirrorApplication.getInstance().getPackageManager();
	        return pm.getApplicationLabel(pm.getApplicationInfo(
	        		MirrorApplication.getInstance().getPackageName(), 0)).toString();
	    } catch (NameNotFoundException e) {
	        e.printStackTrace();
	    }

	    return null;
	}

	/**
	 * 获取软件版本号
	 * 
	 * @return 软件版本号
	 */
	public static int getSoftwareVersionCode() {
	    try {
	        PackageManager pm = MirrorApplication.getInstance().getPackageManager();
	        PackageInfo info = pm.getPackageInfo(MirrorApplication.getInstance().getPackageName(), 0);
	        return info.versionCode;
	    } catch (NameNotFoundException e) {
	        e.printStackTrace();
	    }

	    return -1;
	}
	
	/**
	 * 获取软件版本
	 * 
	 * @return 软件版本
	 */
	public static String getSoftwareVersion() {
	    try {
	        PackageManager pm = MirrorApplication.getInstance().getPackageManager();
	        PackageInfo info = pm.getPackageInfo(MirrorApplication.getInstance().getPackageName(), 0);
	        return info.versionName;
	    } catch (NameNotFoundException e) {
	        e.printStackTrace();
	    }

	    return null;
	}
	
	/**
	 * 获取内核版本
	 * 
	 * @return 内核版本
	 */
	public static String getKernelVersion() {
		String kernelVersion = null;
		InputStream inputStream = null;
		try {
			inputStream = new FileInputStream("/proc/version");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return kernelVersion;
		}
		BufferedReader bufferedReader = new BufferedReader(
				new InputStreamReader(inputStream), 8 * 1024);
		String info = "";
		String line = "";
		try {
			while ((line = bufferedReader.readLine()) != null) {
				info += line;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				bufferedReader.close();
				inputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		try {
			if (info != "") {
				final String keyword = "version ";
				int index = info.indexOf(keyword);
				line = info.substring(index + keyword.length());
				index = line.indexOf(" ");
				kernelVersion = line.substring(0, index);
			}
		} catch (IndexOutOfBoundsException e) {
			e.printStackTrace();
		}

		return kernelVersion;
	}
	
	public static String getLocalIp() {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface
					.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf
						.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress() && (inetAddress instanceof Inet4Address)) {
						return inetAddress.getHostAddress().toString();
					}
				}
			}
		} catch (SocketException ex) {
			ex.printStackTrace();
		}

		return null;
	}
	
	/**
     * 获取当前时间
     * 
     * @return yyyy-MM-dd HH:mm:ss
     */
    public static String getCurrentTime() {
        Time time = new Time();
        time.setToNow();
        StringBuilder sb = new StringBuilder();
        sb.append(time.year);
        sb.append("-");
        int month = time.month + 1;
        sb.append((month < 10) ? ("0" + month) : month);
        sb.append("-");
        sb.append((time.monthDay < 10) ? ("0" + time.monthDay) : time.monthDay);
        sb.append(" ");
        sb.append((time.hour < 10) ? ("0" + time.hour) : time.hour);
        sb.append(":");
        sb.append((time.minute < 10) ? ("0" + time.minute) : time.minute);
        sb.append(":");
        sb.append((time.second < 10) ? ("0" + time.second) : time.second);
        return sb.toString();
    }
    
    public static String getDiskInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append(FileUtils.getDiskUseSpace());
        sb.append("/");
        sb.append(FileUtils.getDiskSpace());
        return sb.toString();
    }

    public static String getCpuUsage() {
        RandomAccessFile reader = null;
        try {
            // Read CPU usage File
            reader = new RandomAccessFile("/proc/stat", "r");
            String[] tokCPU1 = reader.readLine().split("\\s+");
            reader.close();
            reader = null;
            if (tokCPU1.length < 8) {
                return null;
            }
            
            // Get the first CPU usage
            long idle1 = Long.parseLong(tokCPU1[4]);
            long cpu1 = Long.parseLong(tokCPU1[1]) + Long.parseLong(tokCPU1[2])
            		+ Long.parseLong(tokCPU1[3]) + Long.parseLong(tokCPU1[4])
            		+ Long.parseLong(tokCPU1[5]) + Long.parseLong(tokCPU1[6])
            		+ Long.parseLong(tokCPU1[7]);
            
            Thread.sleep(360);
            
            // Read CPU usage File
            reader = new RandomAccessFile("/proc/stat", "r");
            String[] tokCPU2 = reader.readLine().split("\\s+");
            reader.close();
            reader = null;
            if (tokCPU2.length < 8) {
                return null;
            }
            
            // Get the second CPU usage
            long idle2 = Long.parseLong(tokCPU2[4]);
            long cpu2 = Long.parseLong(tokCPU2[1]) + Long.parseLong(tokCPU2[2])
            		+ Long.parseLong(tokCPU2[3]) + Long.parseLong(tokCPU2[4])
            		+ Long.parseLong(tokCPU2[5]) + Long.parseLong(tokCPU2[6])
            		+ Long.parseLong(tokCPU2[7]);
            
            // Calculation of CPU usage
            double dbValue = (double) ((cpu2 - cpu1) - (idle2 - idle1)) / (cpu2 - cpu1) * 100;
            
            // Build string for Server
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("%.0f", dbValue));
            sb.append("%");
            return sb.toString();
        } catch (Exception e) {
        	e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        
        return null;
    }
    
    public static String getMemoryUsage() {
        RandomAccessFile reader = null;
        try {
            reader = new RandomAccessFile("/proc/meminfo", "r");
            String[] totals = reader.readLine().split("\\s+");
            String[] frees = reader.readLine().split("\\s+");
            String[] buffers = reader.readLine().split("\\s+");
            String[] cached = reader.readLine().split("\\s+");
            reader.close();
            reader = null;
            
            if (totals.length < 3 || frees.length < 3 || buffers.length < 3 || cached.length < 3) {
                return null;
            }

            // get total memory
            long totalMemory = Long.parseLong(totals[1]);
            if (totals[2].toLowerCase().equals("kb")) {
                totalMemory *= 1024;
            } else if (totals[2].toLowerCase().equals("mb")) {
                totalMemory *= 1024 * 1024;
            } else if (totals[2].toLowerCase().equals("gb")) {
                totalMemory *= 1024 * 1024 * 1024;
            }
            
            // get free memory
            long freeMemory = Long.parseLong(frees[1]);
            if (frees[2].toLowerCase().equals("kb")) {
                freeMemory *= 1024;
            } else if (frees[2].toLowerCase().equals("mb")) {
                freeMemory *= 1024 * 1024;
            } else if (frees[2].toLowerCase().equals("gb")) {
                freeMemory *= 1024 * 1024 * 1024;
            }

            // get buffers memory
            long bufMemory = Long.parseLong(buffers[1]);
            if (buffers[2].toLowerCase().equals("kb")) {
                bufMemory *= 1024;
            } else if (buffers[2].toLowerCase().equals("mb")) {
                bufMemory *= 1024 * 1024;
            } else if (buffers[2].toLowerCase().equals("gb")) {
                bufMemory *= 1024 * 1024 * 1024;
            }
            
            // get cached memory
            long cacheMemory = Long.parseLong(cached[1]);
            if (cached[2].toLowerCase().equals("kb")) {
                cacheMemory *= 1024;
            } else if (cached[2].toLowerCase().equals("mb")) {
                cacheMemory *= 1024 * 1024;
            } else if (cached[2].toLowerCase().equals("gb")) {
                cacheMemory *= 1024 * 1024 * 1024;
            }
            
            // Calculation of memory usage
            double dbValue = (double) (totalMemory - freeMemory - bufMemory - cacheMemory) / totalMemory * 100;
            
            // Build string for Server
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("%.0f", dbValue));
            sb.append("%");
            return sb.toString();
        } catch (Exception e) {
        	e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return null;
    }
}
