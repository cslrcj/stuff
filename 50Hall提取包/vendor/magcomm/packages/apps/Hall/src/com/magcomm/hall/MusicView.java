package com.magcomm.hall;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.magcomm.receiver.MusicInfo;
import android.media.MediaScannerConnection;//hejianfeng add 

public class MusicView extends LinearLayout {
	Context mContext = null;

	public ImageButton m_play = null;
	public ImageButton m_stop = null;
	public ImageButton m_previous = null;
	public ImageButton m_next = null;
	public TextView m_name = null;
	public TextView m_songername = null;

	private int music_count = 0;

	private static final String SERVICECMD = "com.android.music.musicservicecommand";
	private static final String CMDNAME = "command";
	private static final String CMDSTOP = "stop";
	private static final String CMDPAUSE = "pause";
	private static final String CMDPLAY = "play";
	private static final String CMDPREV = "previous";
	private static final String CMDNEXT = "next";

	public MusicView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		mContext = context;
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.music_view, this);

		music_count = getMusicCount(context);
		if (music_count == 0) {
			scanSdCard();
		}
		InitView();
	}

	public MusicView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		mContext = context;
	}

	public MusicView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		mContext = context;
	}

	void InitView() {

		m_previous = (ImageButton) findViewById(R.id.previous);
		m_next = (ImageButton) findViewById(R.id.next);
		m_play = (ImageButton) findViewById(R.id.play);
		m_previous.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				// 上一曲
				if (music_count != 0) {
					connectMediaService();
					Intent intent = new Intent();
					intent.setAction(SERVICECMD);
					intent.putExtra(CMDNAME, CMDPREV);
					mContext.sendBroadcast(intent);
				} else {
					music_count = getMusicCount(mContext);
				}
			}

		});
		m_next.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				// 下一曲
				if (music_count != 0) {
					connectMediaService();
					Intent intent = new Intent();
					intent.setAction(SERVICECMD);
					intent.putExtra(CMDNAME, CMDNEXT);
					mContext.sendBroadcast(intent);
				} else {
					music_count = getMusicCount(mContext);
				}
			}

		});
		m_play.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				if (music_count != 0) {
					connectMediaService();
					Intent intent = new Intent();
					intent.setAction(SERVICECMD);
					if (MusicInfo.isPlaying()) {
						// 暂停音乐
						intent.putExtra(CMDNAME, CMDPAUSE);
						// m_play.setText("▶");
						m_play.setImageResource(R.drawable.hhplay);
						m_play.invalidate();
					} else {
						// 播放音乐
						intent.putExtra(CMDNAME, CMDPLAY);
						// m_play.setText("||");
						m_play.setImageResource(R.drawable.hhpause);
						m_play.invalidate();
					}
					mContext.sendBroadcast(intent);
				} else {
					music_count = getMusicCount(mContext);
				}
			}

		});

		m_name = (TextView) findViewById(R.id.name);
		m_name.setSelected(true);
		m_songername = (TextView) findViewById(R.id.songername);
		m_songername.setSelected(true);
		if (MusicInfo.isPlaying()) {
			// 暂停音乐
			// m_play.setText("||");
			m_play.setImageResource(R.drawable.hhpause);

		} else {
			// 播放音乐
			// m_play.setText("▶");
			m_play.setImageResource(R.drawable.hhplay);
		}
		m_name.setText(MusicInfo.getMusicName());
		m_songername.setText(MusicInfo.getArtistName());

		// getMusicCount(this);
	}

	public int getMusicCount(Context context) {
		int count1 = 0;
		Cursor csr = null;
		try {
			csr = context.getContentResolver().query(
					MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
					new String[] { MediaStore.Audio.Media.TITLE,
							MediaStore.Audio.Media.DURATION,
							MediaStore.Audio.Media.ARTIST,
							MediaStore.Audio.Media._ID,
							MediaStore.Audio.Media.DISPLAY_NAME }, null, null,
					null);
			count1 = csr.getCount();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (csr != null)
				csr.close();
		}

		Log.i("XXXXX", "count1=" + count1);

		return count1;
	}

	public void connectMediaService() {

		Intent intent = new Intent();
		intent.setClassName("com.android.music",
				"com.android.music.MediaPlaybackService");
		// 绑定连接远程服务
		mContext.startService(intent);
	}

	private void scanSdCard() {
		// IntentFilter intentfilter = new IntentFilter(
		// Intent.ACTION_MEDIA_SCANNER_STARTED);
		// intentfilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
		// intentfilter.addDataScheme("file");
		// scanSdReceiver = new ScanSdReceiver();
		// registerReceiver(scanSdReceiver, intentfilter);
		MediaScannerConnection.scanFile(mContext, new String[] { Environment
				.getExternalStorageDirectory().getAbsolutePath()}, null, null);
	}
	/*
	 * public class ScanSdReceiver extends BroadcastReceiver {
	 * 
	 * private AlertDialog.Builder builder = null; private AlertDialog ad =
	 * null; private int count1; private int count2; private int count;
	 * 
	 * @Override public void onReceive(Context context, Intent intent) { String
	 * action = intent.getAction(); if
	 * (Intent.ACTION_MEDIA_SCANNER_STARTED.equals(action)){ Cursor c1 =
	 * context.getContentResolver()
	 * .query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, new
	 * String[]{MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.DURATION,
	 * MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media._ID,
	 * MediaStore.Audio.Media.DISPLAY_NAME }, null, null, null); count1 =
	 * c1.getCount(); System.out.println("count:"+count); builder = new
	 * AlertDialog.Builder(context); builder.setMessage("正在扫描存储卡..."); ad =
	 * builder.create(); ad.show();
	 * 
	 * }else if(Intent.ACTION_MEDIA_SCANNER_FINISHED.equals(action)){ Cursor c2
	 * = context.getContentResolver()
	 * .query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, new
	 * String[]{MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.DURATION,
	 * MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media._ID,
	 * MediaStore.Audio.Media.DISPLAY_NAME }, null, null, null); count2 =
	 * c2.getCount(); count = count2-count1; ad.cancel(); if (count>=0){
	 * Toast.makeText(context, "共增加" + count + "首歌曲", Toast.LENGTH_LONG).show();
	 * } else { Toast.makeText(context, "共减少" + count + "首歌曲",
	 * Toast.LENGTH_LONG).show(); } } } }
	 */
}
