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
//hejianfeng add start
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.os.PowerManager;
import android.widget.TextView;
import android.widget.ImageButton;
import android.view.View.OnClickListener;
import android.telecom.TelecomManager;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.view.View.OnLongClickListener;
import android.telephony.PhoneNumberUtils;
import com.mediatek.geocoding.GeoCodingQuery;
import android.provider.ContactsContract.PhoneLookup;
import android.content.pm.ApplicationInfo;
import android.provider.Settings;
//hejianfeng add end
public class MainGroup extends Activity implements Slidelistener{
	private static String TAG="MainGroup";
	private ViewPager viewPager;
	private ArrayList<View> pageViews;
	private ViewGroup main, group;
	private ImageView imageView;
	private ImageView[] imageViews;

	//hejianfeng add start
	private ViewGroup callPhone;
	private ViewGroup backGound;
	private ImageView imgUp;
	private ImageView imgDown;
	private AnimationDrawable animationDrawableUp;
	private AnimationDrawable animationDrawableDown;
	private PhoneView btnSlide;
	//hejianfeng add end
	private MusicView m_mv = null;
	private TimeView m_tv = null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);

		getWindow().addFlags(
				WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
						| WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
						| WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
		
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
		callPhone=(ViewGroup)main.findViewById(R.id.call_phone);//hejianfeng add
		backGound=(ViewGroup)main.findViewById(R.id.back_gound);
		wallpapers=getResources().getStringArray(R.array.wallpapers);
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
		//hejianfeng add start
		//获取电话通讯服务  
        TelephonyManager tpm = (TelephonyManager) this  
                .getSystemService(Context.TELEPHONY_SERVICE);  
        //创建一个监听对象，监听电话状态改变事件  
        tpm.listen(new HallPhoneStateListener(),  
                PhoneStateListener.LISTEN_CALL_STATE); 
        mPowerManager = (PowerManager)getSystemService(Context.POWER_SERVICE);
        mPowerKeyWakeLock = mPowerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK| PowerManager.ACQUIRE_CAUSES_WAKEUP,
                TAG);
        mTelecomManager=(TelecomManager)getSystemService(Context.TELECOM_SERVICE);
        mSharedPreferences= getSharedPreferences("hall_phone_num", 
        		Activity.MODE_PRIVATE); 
        mEditor = mSharedPreferences.edit(); 
        txtPhoneNum=(TextView)callPhone.findViewById(R.id.txt_phone_num);
        txtPhoneName=(TextView)callPhone.findViewById(R.id.txt_phone_name);
        txtPhoneAscription=(TextView)callPhone.findViewById(R.id.txt_phone_ascription);
        btnHangUp=(ImageButton)callPhone.findViewById(R.id.btn_hang_up);
        imgUp=(ImageView)callPhone.findViewById(R.id.img_up);
        imgDown=(ImageView)callPhone.findViewById(R.id.img_down);
        btnSlide=(PhoneView)callPhone.findViewById(R.id.btn_slide);
        btnAnswer=(ImageButton)callPhone.findViewById(R.id.btn_answer);
        btnSlide.setOnLongClickListener(new OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
				imgUp.setVisibility(View.INVISIBLE);
				imgDown.setVisibility(View.INVISIBLE);
				animationDrawableUp.stop();
				animationDrawableDown.stop();
				return false;
			}
        });
        btnSlide.setOnSlide(this);
        btnHangUp.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mTelecomManager.endCall();
			}
        });
		//hejianfeng add end

	}

	private int getResID(String name) {
		ApplicationInfo appInfo = getApplicationInfo();
		int resID = getResources().getIdentifier(name, "drawable",
				appInfo.packageName);
		return resID;
	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		animationDrawableUp=(AnimationDrawable)imgUp.getBackground();
		animationDrawableDown=(AnimationDrawable)imgDown.getBackground();
		geoCodingQuery = GeoCodingQuery.getInstance(this);
		int position=Settings.System.getInt(getContentResolver(), "set_hall_backgound", 0);
		backGound.setBackgroundResource(getResID(wallpapers[position]));
	}

	private String getContactId(Context context, String number) {
		Cursor c = null;
		try {
			c = context.getContentResolver()
					.query(Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI,
							number),
							new String[] { PhoneLookup._ID, PhoneLookup.NUMBER,
									PhoneLookup.DISPLAY_NAME, PhoneLookup.TYPE,
									PhoneLookup.LABEL }, null, null, null);

			if (c.getCount() == 0) {
				return null;
			} else if (c.getCount() > 0) {
				c.moveToFirst();
				String phonename = c.getString(2); // 获取姓名
				return phonename;
			}
		} catch (Exception e) {
			Log.e(TAG, "getContactId error:", e);
		} finally {
			if (c != null) {
				c.close();
			}
		}
		return null;
	}
	@Override
	protected void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub
		super.onNewIntent(intent);
		Log.v(TAG, "jeff onNewIntent");
		if(!mPowerManager.isScreenOn()&&!mPowerKeyWakeLock.isHeld()){
			Log.v(TAG, "jeff acquire()");
			mPowerKeyWakeLock.acquire();
		}
	}
	//hejianfeng add start
	private PowerManager.WakeLock mPowerKeyWakeLock;
	private PowerManager mPowerManager;
	private TelecomManager mTelecomManager;
	private TextView txtPhoneNum;
	private TextView txtPhoneName;
	private TextView txtPhoneAscription;
	private ImageButton btnHangUp;
	private ImageButton btnAnswer;
	private SharedPreferences mSharedPreferences;
	private SharedPreferences.Editor mEditor;
	private GeoCodingQuery geoCodingQuery;
	private String cityName;
	 private String[] wallpapers;
	
	class HallPhoneStateListener extends PhoneStateListener {
		 @Override  
	        public void onCallStateChanged(int state, String incomingNumber) {  
	            switch(state) {  
	            case TelephonyManager.CALL_STATE_IDLE: //空闲  
	            	Log.v(TAG, "jeff CALL_STATE_IDLE");
	            	callPhone.setVisibility(View.GONE);
	            	viewPager.setVisibility(View.VISIBLE);
	            	 if (mPowerKeyWakeLock.isHeld()) {
	            		 Log.v(TAG, "jeff release()");
	                     mPowerKeyWakeLock.release();
	                 }
	                break;  
	            case TelephonyManager.CALL_STATE_RINGING: //来电  
	            	Log.v(TAG, "jeff CALL_STATE_RINGING");
	            	mEditor.putString("phone_num", incomingNumber); 
	            	mEditor.commit();
	            	txtPhoneNum.setText(incomingNumber);
	                cityName = geoCodingQuery.queryByNumber(incomingNumber);
	            	txtPhoneName.setText(getContactId(MainGroup.this,incomingNumber));
	            	txtPhoneAscription.setText(cityName);
	            	btnAnswer.setVisibility(View.VISIBLE);
	            	callPhone.setVisibility(View.VISIBLE);
	            	viewPager.setVisibility(View.GONE);
	            	btnSlide.setVisibility(View.VISIBLE);
	            	imgUp.setVisibility(View.VISIBLE);
	            	imgDown.setVisibility(View.VISIBLE);
	        		animationDrawableUp.start();
	        		animationDrawableDown.start();
	                break;  
	            case TelephonyManager.CALL_STATE_OFFHOOK: //摘机（正在通话中）  
	            	Log.v(TAG, "jeff CALL_STATE_OFFHOOK");
	            	incomingNumber=mSharedPreferences.getString("phone_num", "");
	            	txtPhoneNum.setText(incomingNumber);
	            	cityName = geoCodingQuery.queryByNumber(incomingNumber);
	            	txtPhoneName.setText(getContactId(MainGroup.this,incomingNumber));
	            	txtPhoneAscription.setText(cityName);
	            	callPhone.setVisibility(View.VISIBLE);
	            	viewPager.setVisibility(View.GONE);
	            	btnAnswer.setVisibility(View.INVISIBLE);
	            	imgUp.setVisibility(View.INVISIBLE);
	            	imgDown.setVisibility(View.INVISIBLE);
	            	btnSlide.setVisibility(View.INVISIBLE);
	                break;  
	            }  
	        }
	}
	//hejianfeng add end
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
	@Override
	public void SlideUp() {
		// TODO Auto-generated method stub
		Log.v("jeff", "hejianfeng SlideUp");
		mTelecomManager.endCall();
	}
	@Override
	public void SlideDown() {
		// TODO Auto-generated method stub
		Log.v("jeff", "hejianfeng SlideDown");
		mTelecomManager.acceptRingingCall();
		btnSlide.setVisibility(View.INVISIBLE);
	}
	@Override
	public void SlideMiddle() {
		// TODO Auto-generated method stub
		Log.v("jeff", "hejianfeng SlideMiddle");
		imgUp.setVisibility(View.VISIBLE);
		imgDown.setVisibility(View.VISIBLE);
		animationDrawableUp.start();
		animationDrawableDown.start();
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