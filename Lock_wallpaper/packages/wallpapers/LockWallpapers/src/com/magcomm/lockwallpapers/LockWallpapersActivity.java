package com.magcomm.lockwallpapers;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.view.View.OnClickListener;
import java.io.File;
import android.os.Environment;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.Bitmap;
import android.content.ComponentName;
// added by bruce begin
import android.provider.Settings;
import android.util.Log;
// added by bruce end

public class LockWallpapersActivity extends Activity {
	private LinearLayout lyWallpaperList;
	private RelativeLayout wallpaperRoot;
	private String[] wallpapapers;
	private LockWallpaperListAdapter mAdapter;
	private LockWallpapersManager lockWallpapersManager;
	private Button btnSetWallpaper;
	private int position;
	private String action="com.magcomm.lockwallpapers";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_lock_wallpapers);
		lockWallpapersManager = LockWallpapersManager.getInstance(this);
		lyWallpaperList=(LinearLayout)findViewById(R.id.wallpaper_list);
		wallpaperRoot=(RelativeLayout)findViewById(R.id.wallpaper_root);
		btnSetWallpaper=(Button)findViewById(R.id.btn_set_wallpaper);
		wallpapapers = getResources().getStringArray(R.array.wallpapers);
		position=Settings.System.getInt(getContentResolver(),Settings.System.MAGCOMM_LOCK_WALLPAPER_ID,1);
		Drawable mDrawable=null;
		if(position==0){
			mDrawable=getLockWallpaper();
		}
		if(position==0){
			wallpaperRoot.setBackground(mDrawable);
		}else{
			wallpaperRoot.setBackgroundResource(lockWallpapersManager.getResID(wallpapapers[position]));
		}
		lockWallpapersManager.setLockWallPapers(wallpapapers);
		mAdapter = new LockWallpaperListAdapter(this,
				lockWallpapersManager.getWallpapersSmall(wallpapapers));
		populateWallpapersFromAdapter(lyWallpaperList, mAdapter, false);
		btnSetWallpaper.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
                // added by bruce begin
                Log.i("bruce_nan", "LockWallpapersActivity_nfl_01: position = " + position);
                Settings.System.putInt((LockWallpapersActivity.this).getContentResolver(),
                    Settings.System.MAGCOMM_LOCK_WALLPAPER_ID, position);
                // added by bruce end
				finish();
			}
		});
	}
	private static final String SCREENSHOTS_DIR_NAME = "Screenshots";
    private static final String FILE_NAME="defaultLockWallpaper.png";
    private Drawable getLockWallpaper(){
    	File mScreenshotDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), SCREENSHOTS_DIR_NAME);
    	File mImageFilePath=new File(mScreenshotDir,FILE_NAME);
    	if(!mImageFilePath.exists()){
    		position=1;
    	}
    	Bitmap bm = BitmapFactory.decodeFile(mImageFilePath.getAbsolutePath());
    	return new BitmapDrawable(bm);
    }
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.lock_wallpapers, menu);
		return true;
	}
	private void startGallery(){
		Intent intent=new Intent(Intent.ACTION_SET_WALLPAPER);     
		intent.setComponent(new ComponentName(
                    "com.android.gallery3d", "com.android.gallery3d.app.Wallpaper"));
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
	}
	private void populateWallpapersFromAdapter(ViewGroup parent,
			BaseAdapter adapter, boolean addLongPressHandler) {
		for (int i = 0; i < adapter.getCount(); i++) {
			RelativeLayout thumbnail = (RelativeLayout) adapter.getView(i, null,
					parent);
			parent.addView(thumbnail, i);
			thumbnail.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					for(int i=0;i<lyWallpaperList.getChildCount();i++){
						if(v.equals(lyWallpaperList.getChildAt(i))){
							position=i; 
							if(i==0){
								startGallery();
								finish();
							}else{
								wallpaperRoot.setBackgroundResource(lockWallpapersManager.getResID(wallpapapers[i]));
							}
						}
					}
				}
			});
		}
	}
}
