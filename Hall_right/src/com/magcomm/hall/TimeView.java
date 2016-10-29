package com.magcomm.hall;

import java.util.Calendar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog.Calls;
import android.text.format.DateFormat;
import android.text.format.Time;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TimeView extends LinearLayout {
	Context mContext = null;

	public TextView missed_message = null;
	public TextView missed_call = null;
	public ImageView message_img = null;
	public ImageView call_img = null;
	public AppleTimeView dialClock;
	int m_call_count, m_sms_count;
	final static int MSG_NEW_SMS_COUNT = 2;
	final static int MSG_NEW_CALL_COUNT = 1;
	final static int MSG_REFRESH_VIEW = 3;

	private static TextView hourText;
	private static TextView dataText;
	private static TextView weekText;

	private boolean hasRes = false;

	public TimeView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		mContext = context;

		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.date_time, this);

		InitView();
		initView();
	}

	public TimeView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		mContext = context;
	}

	public TimeView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		mContext = context;
	}

	void set_Call_Counut(int count) {
		if (missed_call != null) {
			missed_call.setVisibility(View.VISIBLE);
			missed_call.setText(new String().valueOf(count));
		}
	}

	void set_Message_Counut(int count) {
		if (missed_message != null) {
			missed_message.setText(new String().valueOf(count));
		}

	}

	void InitView() {
		m_call_count = findMissedCallCount();
		missed_call = (TextView) findViewById(R.id.textView1);
		call_img = (ImageView) findViewById(R.id.imageView1);
		if (m_call_count != 0) {
			missed_call.setVisibility(View.VISIBLE);
			call_img.setVisibility(View.VISIBLE);
			missed_call.setText(new String().valueOf(findMissedCallCount()));
		} else {
			missed_call.setVisibility(View.INVISIBLE);
			call_img.setVisibility(View.INVISIBLE);
		}

		m_sms_count = findNewSmsCount() + findNewMmsCount();
		missed_message = (TextView) findViewById(R.id.textView2);
		message_img = (ImageView) findViewById(R.id.imageView2);
		if (m_sms_count != 0) {
			missed_message.setVisibility(View.VISIBLE);
			message_img.setVisibility(View.VISIBLE);
			missed_message.setText(new String().valueOf(findNewSmsCount()
					+ findNewMmsCount()));
		} else {
			missed_message.setVisibility(View.INVISIBLE);
			message_img.setVisibility(View.INVISIBLE);
		}
	}

	// 监控短信，彩信数目变化
	private int findNewSmsCount() {
		Cursor csr = null;
		int newSmsCount = 0;
		try {
			csr = mContext
					.getApplicationContext()
					.getContentResolver()
					.query(Uri.parse("content://sms"), null,
							"type = 1 and read = 0", null, null);
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
			csr = mContext
					.getApplicationContext()
					.getContentResolver()
					.query(Uri.parse("content://mms/inbox"), null, "read = 0",
							null, null);
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
			cur = mContext.getContentResolver().query(Calls.CONTENT_URI,
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

	@Override
	protected void onFinishInflate() {
		// TODO Auto-generated method stub
		super.onFinishInflate();

	}

	@Override
	protected void onAttachedToWindow() {
		// TODO Auto-generated method stub
		super.onAttachedToWindow();
		registerReceiver();
	}

	@Override
	protected void onDetachedFromWindow() {
		// TODO Auto-generated method stub
		super.onDetachedFromWindow();
		unRegisterReveiver();
	}

	public void registerReceiver() {
		if (!hasRes) {
			IntentFilter localIntentFilter1 = new IntentFilter();
			localIntentFilter1
					.addAction("android.intent.action.MEDIA_SCANNER_FINISHED");
			localIntentFilter1.addDataScheme("file");
			mContext.registerReceiver(mReceiver, localIntentFilter1);
			IntentFilter localIntentFilter3 = new IntentFilter();
			localIntentFilter3.addAction("android.intent.action.SCREEN_ON");
			localIntentFilter3.addAction("android.intent.action.SCREEN_OFF");
			mContext.registerReceiver(mReceiver, localIntentFilter3);

			IntentFilter localIntentFilter4 = new IntentFilter();
			localIntentFilter4.addAction("android.intent.action.TIME_SET");
			localIntentFilter4.addAction("android.intent.action.TIME_TICK");
			localIntentFilter4
					.addAction("android.intent.action.TIMEZONE_CHANGED");
			localIntentFilter4.addAction("android.intent.action.DATE_CHANGED");
			localIntentFilter4.addAction("status.bar.date.changed");
			mContext.registerReceiver(mReceiver, localIntentFilter4);
			hasRes = true;
		}
	}

	public void unRegisterReveiver() {

		if (hasRes) {
			try {
				mContext.unregisterReceiver(mReceiver);
				hasRes = false;
			} catch (IllegalArgumentException e) {
				if (e.getMessage().contains("Receiver not registered")) {
					// Ignore this exception. This is exactly what is desired
				} else {
					// unexpected, re-throw
					throw e;
				}
			}

		}

	}

	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context paramContext, Intent paramIntent) {
			if (("android.intent.action.TIME_TICK".equals(paramIntent
					.getAction()))
					|| ("android.intent.action.TIME_SET".equals(paramIntent
							.getAction()))
					|| ("android.intent.action.TIMEZONE_CHANGED"
							.equals(paramIntent.getAction()))
					|| ("android.intent.action.DATE_CHANGED".equals(paramIntent
							.getAction()))
					|| ("status.bar.date.changed".equals(paramIntent
							.getAction()))
					|| ("android.intent.action.TIME_TICK".equals(paramIntent
							.getAction()))) {
				if (!"android.intent.action.TIME_TICK".equals(paramIntent
						.getAction())) {
					setDate();
				}
				setHour();
			}
		}
	};

	private void initView() {

		// mNetRemind = (TextView) findViewById(R.id.remindNet_text);
		hourText = (TextView) findViewById(R.id.hour_text);
		dataText = (TextView) findViewById(R.id.data_text);
		weekText = (TextView) findViewById(R.id.week_text);
		dialClock=(AppleTimeView)findViewById(R.id.dial_clock);
		// mUpdateProgressBar = (ProgressBar)
		// findViewById(R.id.update_progress);

		// mNetRemind.setOnClickListener(mOnClickListener);

		setDate();
		setHour();
	}

	public void setDate() {
		// TODO Auto-generated method stub
		Time freshtime = new Time();
		freshtime.setToNow();
		Calendar c = Calendar.getInstance();
		//weiyawei add start
		String nian = getResources().getString(R.string.nian);
		String yue = getResources().getString(R.string.yue);
		String ri = getResources().getString(R.string.ri);
		if (dataText != null) {
			String language = getResources().getConfiguration().locale.getCountry();
			if(language.equals("CN") || language.equals("TW")) {
				dataText.setText((c.get(Calendar.MONTH) + 1) + yue
						+ c.get(Calendar.DATE) + ri + ",");//hejianfeng modif
			}else{
				dataText.setText((c.get(Calendar.MONTH) + 1)+ nian + c.get(Calendar.DATE) + yue
						+",");
			}
		//weiyawei add end
		}
		if (weekText != null) {
			weekText.setText(getWeek());
		}
	}

	public void setHour() {
		Calendar c = Calendar.getInstance();
		int data1, data2;
		if (DateFormat.is24HourFormat(mContext)) {
			data1 = c.get(Calendar.HOUR_OF_DAY);
			data2 = c.get(Calendar.MINUTE);
			Log.i("XXXXX", "++++data1 =" + data1);
			Log.i("XXXXX", "+++++++data2 =" + data2);
		} else {

			data1 = c.get(Calendar.HOUR);
			data2 = c.get(Calendar.MINUTE);

			if (data1 == 0) {
				data1 = 12;
			}
			Log.i("XXXXX", "-----data1 =" + data1);
			Log.i("XXXXX", "------data2 =" + data2);
		}
		int h1, h2, m1, m2;
		String time_hour = "";
		if (data1 < 10) {
			h1 = 0;
			h2 = data1;
			Log.i("XXXXX", "++++h1 =" + h1);
			Log.i("XXXXX", "++++h2 =" + h2);
		} else {
			h1 = data1 / 10;
			h2 = data1 % 10;
			Log.i("XXXXX", "-----h1 =" + h1);
			Log.i("XXXXX", "-----h2 =" + h2);
		}
		if (data2 < 10) {
			m1 = 0;
			m2 = data2;
			Log.i("XXXXX", "++++m1 =" + m1);
			Log.i("XXXXX", "++++m2 =" + m2);
		} else {
			m1 = data2 / 10;
			m2 = data2 % 10;
			Log.i("XXXXX", "-----m1 =" + m1);
			Log.i("XXXXX", "-----m2 =" + m2);
		}
		if (h1 == 0) {
			time_hour = String.valueOf(h2) + ":" + String.valueOf(m1)
					+ String.valueOf(m2) + "";
			Log.i("XXXXX", "++++time_hour =" + time_hour);
			// clock_hour_ten.setBackgroundDrawable(null);
		} else {
			time_hour = String.valueOf(h1) + String.valueOf(h2) + ":"
					+ String.valueOf(m1) + String.valueOf(m2) + "";
			Log.i("XXXXX", "-----time_hour =" + time_hour);
			// clock_hour_ten.setBackgroundResource(getNumImage(h1));
		}
		if (hourText != null) {
			hourText.setText(time_hour);
		}
	}

	private String getWeek() {
		// TODO Auto-generated method stub
		String weekStr;
		Calendar c = Calendar.getInstance();
		int week = c.get(Calendar.DAY_OF_WEEK);
		switch (week) {
		case 1:
			weekStr = getResources().getString(R.string.Sunday);
			break;
		case 2:
			weekStr = getResources().getString(R.string.Monday);
			break;
		case 3:
			weekStr = getResources().getString(R.string.Tuesday);
			break;
		case 4:
			weekStr = getResources().getString(R.string.Wednesday);
			break;
		case 5:
			weekStr = getResources().getString(R.string.Thursday);
			break;
		case 6:
			weekStr = getResources().getString(R.string.Friday);
			break;
		case 7:
			weekStr = getResources().getString(R.string.Saturday);
			break;

		default:
			weekStr = getResources().getString(R.string.Sunday);
			break;
		}
		return weekStr;
	}
}
