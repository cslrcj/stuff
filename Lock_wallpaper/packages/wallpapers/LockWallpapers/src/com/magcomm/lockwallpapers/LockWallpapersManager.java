package com.magcomm.lockwallpapers;

import android.content.Context;
import android.content.pm.ApplicationInfo;

public class LockWallpapersManager {
	private String[] wallpapapers;
	private Context mContext;
	private static LockWallpapersManager mLockWallpapersManager;
	private LockWallpapersManager(Context context){
		mContext=context;
	}
	public static LockWallpapersManager getInstance(Context context){
		if(mLockWallpapersManager==null){
			mLockWallpapersManager=new LockWallpapersManager(context);
		}
		return mLockWallpapersManager;
	}
	public void setLockWallPapers(String[] wallpapapers){
		this.wallpapapers=wallpapapers;
	}
	public String[] getLockWallPapers(){
		return wallpapapers;
	}
	public String[] getWallpapersSmall(String[] wallpapapers) {
		String[] wallpapapersSmall = new String[wallpapapers.length];
		for (int i = 0; i < wallpapapers.length; i++) {
			wallpapapersSmall[i] = wallpapapers[i] + "_small";
		}
		return wallpapapersSmall;
	}
	public int getResID(String name){
		ApplicationInfo appInfo = mContext.getApplicationInfo();
		int resID = mContext.getResources().getIdentifier(name, "drawable", appInfo.packageName);
		return resID;
	}
}
