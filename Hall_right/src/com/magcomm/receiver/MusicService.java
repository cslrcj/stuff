package com.magcomm.receiver;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;


public class MusicService extends Service {
	private static final boolean DBG = true;
	private static final String TAG = "MusicService";
	
	private static final String refresh_view = "MusicService_REFRESH_VIEW";
	
	private MusicReceiver mMBR = null;
	
	public static final String PLAYSTATE_CHANGED = "com.android.music.playstatechanged";
	public static final String META_CHANGED = "com.android.music.metachanged";
	public static final String QUEUE_CHANGED = "com.android.music.queuechanged";
	public static final String PLAYBACK_COMPLETE = "com.android.music.playbackcomplete";
	public static final String ASYNC_OPEN_COMPLETE = "com.android.music.asyncopencomplete";
	public static final String SERVICECMD = "com.android.music.musicservicecommand";
	public static final String CMDNAME = "command";
	public static final String CMDTOGGLEPAUSE = "togglepause";
	public static final String CMDSTOP = "stop";
	public static final String CMDPAUSE = "pause";
	public static final String CMDPREVIOUS = "previous";
	public static final String CMDNEXT = "next";

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		registerComponent();
	}

	@Override
	public void onStart(Intent intent, int startId) {
		// TODO Auto-generated method stub
		super.onStart(intent, startId);
		if (DBG)
			Log.d(TAG, "-->onStart()");
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (DBG)
			Log.d(TAG, "-->onDestroy()");
		unregisterComponent();
		// 被销毁时启动自身，保持自身在后台存活
		startService(new Intent(MusicService.this, MusicService.class));
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		if (DBG)
			Log.d(TAG, "-->onBind()");
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		if (DBG)
			Log.d(TAG, "-->onStartCommand()");
		return Service.START_STICKY;
	}
	public void registerComponent()
	{
		if (mMBR == null)
		{   
			mMBR = new MusicReceiver();
        	IntentFilter mFilter = new IntentFilter();
        	mFilter.addAction("com.android.music.playstatechanged"); 	
        	mFilter.addAction("com.android.music.metachanged");
        	mFilter.addAction("com.android.music.queuechanged");
        	mFilter.addAction("com.android.music.playbackcomplete"); 	
        	registerReceiver(mMBR, mFilter);
		}
	}
	public void unregisterComponent() 
	{
		// TODO Auto-generated method stub
		if(DBG) Log.d(TAG, "unregisterComponent()");
		
		if (mMBR != null)
		{
			this.unregisterReceiver(mMBR);
		}
	}
	public class MusicReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			// TODO Auto-generated method stub
			Log.i("AAAAA", "MusicReceiver, enter onReceive");
			MusicInfo.setArtistName(intent.getStringExtra("artist"));
			// artistName = intent.getStringExtra("artist");
			MusicInfo.setMusicName(intent.getStringExtra("track"));
			// musicName = intent.getStringExtra("track");
			MusicInfo.setPlaying(intent.getBooleanExtra("playing", false));
			// playing = intent.getBooleanExtra("playing", playing);
			// String album = intent.getStringExtra("album");

			if (DBG)
				Log.d("AAAAA", "artistName-->" + MusicInfo.getArtistName());
			if (DBG)
				Log.d("AAAAA", "musicName-->" + MusicInfo.getMusicName());
			if (DBG)
				Log.d("AAAAA", "playing-->" + MusicInfo.isPlaying());
			// Log.d(TAG, "album-->" + album);

			// 因为音乐后台服务发送的是粘性广播，所以接收后要删除，不然会保持
			removeStickyBroadcast(intent);
			
			Intent in = new Intent(refresh_view);
			sendBroadcast(in);
		}

	}
	
}
