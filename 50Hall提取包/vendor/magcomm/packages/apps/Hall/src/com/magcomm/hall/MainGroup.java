package com.magcomm.hall;

import java.util.ArrayList;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.provider.CallLog.Calls;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import com.magcomm.receiver.MusicInfo;
import com.magcomm.receiver.MusicService;

public class MainGroup extends Activity {

	private ViewPager viewPager;
	private ArrayList<View> pageViews;
	private ViewGroup main, group;
	private ImageView imageView;
	private ImageView[] imageViews;

	private MusicView m_mv = null;
	private TimeView m_tv = null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);

		getWindow().addFlags(
				WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
						| WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
		
		m_mv = new MusicView(this);
		m_tv = new TimeView(this);
		LayoutInflater inflater = getLayoutInflater();
		pageViews = new ArrayList<View>();
		pageViews.add(m_tv);
		pageViews.add(m_mv);
		imageViews = new ImageView[pageViews.size()];
		main = (ViewGroup) inflater.inflate(R.layout.hallgroup, null);

		// group是R.layou.main中的负责包裹小圆点的LinearLayout.
		group = (ViewGroup) main.findViewById(R.id.viewGroup);
		viewPager = (ViewPager) main.findViewById(R.id.guidePages);
		
		for (int i = 0; i < pageViews.size(); i++) {
			imageView = new ImageView(this);
			imageView.setLayoutParams(new LayoutParams(60, 20));
			imageView.setPadding(90, 0, 90, 0);
			imageViews[i] = imageView;

			if (i == 0) {
				// 默认选中第一张图片
				imageViews[i]
						.setBackgroundResource(R.drawable.clear_page_indicator_focus);
			} else {
				imageViews[i]
						.setBackgroundResource(R.drawable.clear_page_indicator_normal);
			}

			group.addView(imageViews[i]);
		}
		setContentView(main);
		
		viewPager.setAdapter(new GuidePageAdapter());
		viewPager.setOnPageChangeListener(new GuidePageChangeListener());

		mg_instance = this;

		register_refresh();

		Intent intent_connect = new Intent(this, MusicService.class);
		startService(intent_connect);

		getApplicationContext().getContentResolver().registerContentObserver(
				Uri.parse("content://mms-sms/"), true,
				new newMmsContentObserver(getApplicationContext(), myHandler));
		getApplicationContext().getContentResolver().registerContentObserver(
				android.provider.CallLog.Calls.CONTENT_URI,
				true,
				new MissedCallContentObserver(getApplicationContext(),
						myHandler));

	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		unregister_refresh();
		mg_instance = null;
	}

	/** 指引页面Adapter */
	class GuidePageAdapter extends PagerAdapter {
		@Override
		public int getCount() {
			return pageViews.size();
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

		@Override
		public int getItemPosition(Object object) {
			// TODO Auto-generated method stub
			return super.getItemPosition(object);
		}

		@Override
		public void destroyItem(View arg0, int arg1, Object arg2) {
			// TODO Auto-generated method stub
			((ViewPager) arg0).removeView(pageViews.get(arg1));
		}

		@Override
		public Object instantiateItem(View arg0, int arg1) {
			// TODO Auto-generated method stub
			((ViewPager) arg0).addView(pageViews.get(arg1));

			return pageViews.get(arg1);
		}

		@Override
		public void restoreState(Parcelable arg0, ClassLoader arg1) {
			// TODO Auto-generated method stub

		}

		@Override
		public Parcelable saveState() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void startUpdate(View arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void finishUpdate(View arg0) {
			// TODO Auto-generated method stub

		}
	}

	/** 指引页面改监听器 */
	class GuidePageChangeListener implements OnPageChangeListener {

		@Override
		public void onPageScrollStateChanged(int arg0) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
			// TODO Auto-generated method stub
		}
		@Override
		public void onPageSelected(int arg0) {
			for (int i = 0; i < imageViews.length; i++) {
				imageViews[arg0]
						.setBackgroundResource(R.drawable.clear_page_indicator_focus);
				if (arg0 != i) {
					imageViews[i]
						.setBackgroundResource(R.drawable.clear_page_indicator_normal);
				}
			}
		}

	}

	static MainGroup mg_instance = null;

	public static MainGroup get_mg_instance() {
		return mg_instance;
	}

	int m_call_count, m_sms_count;
	final static int MSG_NEW_SMS_COUNT = 2;
	final static int MSG_NEW_CALL_COUNT = 1;
	final static int MSG_REFRESH_VIEW = 3;
	// 通过ＨＡＮＤＬＥＲ修改电话短信条数
	private Handler myHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_NEW_SMS_COUNT:
				int sms = (Integer) msg.obj;
				m_sms_count = sms;
				if (m_sms_count != 0) {
					m_tv.missed_message.setVisibility(View.VISIBLE);
					m_tv.message_img.setVisibility(View.VISIBLE);
				} else {
					m_tv.missed_message.setVisibility(View.INVISIBLE);
					m_tv.message_img.setVisibility(View.INVISIBLE);
				}
				m_tv.missed_message.setText(new String().valueOf(m_sms_count));
				m_tv.missed_message.invalidate();
				break;
			case MSG_NEW_CALL_COUNT:
				int call = (Integer) msg.obj;
				m_call_count = call;
				if (m_call_count != 0) {
					m_tv.missed_call.setVisibility(View.VISIBLE);
					m_tv.call_img.setVisibility(View.VISIBLE);
				} else {
					m_tv.missed_call.setVisibility(View.INVISIBLE);
					m_tv.call_img.setVisibility(View.INVISIBLE);
				}
				m_tv.missed_call.setText(new String().valueOf(m_call_count));
				m_tv.missed_call.invalidate();
				break;
			case MSG_REFRESH_VIEW:
				if (MusicInfo.isPlaying()) {
					// 暂停音乐
					// m_play.setText("||");
					m_mv.m_play.setImageResource(R.drawable.hhpause);
				} else {
					// 播放音乐
					// m_play.setText("▶");
					m_mv.m_play.setImageResource(R.drawable.hhplay);
				}
				m_mv.m_name.setText(MusicInfo.getMusicName());
				m_mv.m_songername.setText(MusicInfo.getArtistName());
				break;
			default:
				break;
			}
		}
	};

	// 监控短信，彩信数目变化
	private int findNewSmsCount() {
		Cursor csr = null;
		int newSmsCount = 0;
		try {
			csr = getApplicationContext().getContentResolver().query(
					Uri.parse("content://sms"), null, "type = 1 and read = 0",
					null, null);
			newSmsCount = csr.getCount(); // 未读短信数目
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (csr != null)
				csr.close();
		}
		return newSmsCount;
	}

	// 监控短信，短信数目变化
	private int findNewMmsCount() {
		Cursor csr = null;
		int newMmsCount = 0;
		try {
			csr = getApplicationContext().getContentResolver().query(
					Uri.parse("content://mms/inbox"), null, "read = 0", null,
					null);
			newMmsCount = csr.getCount();// 未读彩信数目
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (csr != null)
				csr.close();
		}
		return newMmsCount;
	}

	// 监控电话数目
	private int findMissedCallCount() {
		int missedCallCount = 0;

		StringBuilder where = new StringBuilder("type = ");
		where.append(Calls.MISSED_TYPE);
		where.append(" AND new = 1");

		// start the query
		Cursor cur = null;
		try {
			cur = this.getContentResolver().query(Calls.CONTENT_URI,
					new String[] { Calls._ID }, where.toString(), null,
					Calls.DEFAULT_SORT_ORDER);

			if (cur != null) {
				missedCallCount = cur.getCount();
			}
		} catch (Exception ex) {
		} finally {
			if (cur != null) {
				cur.close();
			}
		}
		return missedCallCount;
	}

	// 监控信息数据库
	public class newMmsContentObserver extends ContentObserver {
		private Context ctx;
		private Handler m_handler;
		int newMmsCount = 0;
		int newSmsCount = 0;

		public newMmsContentObserver(Context context, Handler handler) {
			super(handler);
			ctx = context;
			m_handler = handler;
		}

		@Override
		public void onChange(boolean selfChange) {
			newMmsCount = findNewSmsCount();
			newSmsCount = findNewMmsCount();
			m_handler.obtainMessage(MSG_NEW_SMS_COUNT,
					(newMmsCount + newSmsCount)).sendToTarget();
		}
	}

	// 监控电话数据库
	public class MissedCallContentObserver extends ContentObserver {

		private Context ctx;
		int missedCallCount = 0;
		private Handler m_handler;
		private static final String TAG = "MissedCallContentObserver";

		public MissedCallContentObserver(Context context, Handler handler) {
			super(handler);
			ctx = context;
			m_handler = handler;
		}

		@Override
		public void onChange(boolean selfChange) {
			missedCallCount = findMissedCallCount();
			m_handler.obtainMessage(MSG_NEW_CALL_COUNT, missedCallCount)
					.sendToTarget();

		}
	}

	private static final String refresh_view = "MusicService_REFRESH_VIEW";
	private RefreshReceiver m_Refresh = null;

	public void register_refresh() {
		if (m_Refresh == null) {
			m_Refresh = new RefreshReceiver();
			IntentFilter mFilter = new IntentFilter();
			mFilter.addAction(refresh_view);
			registerReceiver(m_Refresh, mFilter);
		}
	}

	public void unregister_refresh() {
		// TODO Auto-generated method stub

		if (m_Refresh != null) {
			this.unregisterReceiver(m_Refresh);
		}
	}

	public class RefreshReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			// TODO Auto-generated method stub

			MainGroup.this.myHandler.obtainMessage(MSG_REFRESH_VIEW, null)
					.sendToTarget();
		}

	}

	// modify by even
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_HOME:
			return true;
		case KeyEvent.KEYCODE_BACK:
			return true;
		case KeyEvent.KEYCODE_CALL:
			return true;
		case KeyEvent.KEYCODE_SYM:
			return true;
			// case KeyEvent.KEYCODE_VOLUME_DOWN:
			// return true;
			// case KeyEvent.KEYCODE_VOLUME_UP:
			// return true;
		case KeyEvent.KEYCODE_STAR:
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

}