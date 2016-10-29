package com.magcomm.hall;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import android.util.Log;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.view.View;
import android.util.AttributeSet;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import java.io.InputStream;
public class AppleTimeView extends View {
	Drawable bmdDial;
	Drawable bmdHour;
	Drawable bmdMinute;
	Drawable bmdSecond;
	public AppleTimeView(Context context) {
		super(context);
	}

	public AppleTimeView(Context context, AttributeSet attrs) {
		super(context, attrs);
		bmdDial = readBitmap(context,
				R.drawable.time_backgound);
		bmdHour = readBitmap(context,
				R.drawable.hour);

		bmdMinute = readBitmap(context,
				R.drawable.minute);

		bmdSecond = readBitmap(context,
				R.drawable.second);
		run();
	}
	private Drawable readBitmap(Context context,int resId){
		return context.getResources().getDrawable(resId);
	}
	public AppleTimeView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	Handler tickHandler;
	public void run() {
		tickHandler = new Handler();
		tickHandler.post(tickRunnable);
	}

	private Runnable tickRunnable = new Runnable() {
		public void run() {
			postInvalidate();
			tickHandler.postDelayed(tickRunnable, 1000);
		}
	};

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		// hejianfeng start
		Log.v("jeff","hejianfeng onDraw");
		int w=getWidth();
		int h=getHeight();
		int centerX = w / 2;
		int centerY = h / 2;
		canvas.save();
		bmdDial.setBounds(centerX-bmdDial.getIntrinsicWidth()/2, centerY-bmdDial.getIntrinsicHeight()/2, centerX+bmdDial.getIntrinsicWidth()/2, centerY+bmdDial.getIntrinsicHeight()/2);
		bmdDial.draw(canvas);
		canvas.restore();
		long time = System.currentTimeMillis();
		final Calendar mCalendar = Calendar.getInstance();
		mCalendar.setTimeInMillis(time);
		int mHour = mCalendar.get(Calendar.HOUR);
		int mMinutes = mCalendar.get(Calendar.MINUTE);
		int mSeconds = mCalendar.get(Calendar.SECOND);

		float hDegree = ((mHour + (float) mMinutes / 60) / 12) * 360;
		float mDegree = ((mMinutes + (float) mSeconds / 60) / 60) * 360;
		float sDegree = ((float) mSeconds / 60) * 360;
		// 秒针－－－－－－－－－－－
		canvas.save();
		canvas.rotate(sDegree, centerX, centerY);
		bmdSecond.setBounds(centerX-bmdSecond.getIntrinsicWidth()/2, centerY-bmdSecond.getIntrinsicHeight()/2, centerX+bmdSecond.getIntrinsicWidth()/2, centerY+bmdSecond.getIntrinsicHeight()/2);
		bmdSecond.draw(canvas);
		canvas.restore();
		// 分针－－－－－－－－－－－
		canvas.save();
		canvas.rotate(mDegree, centerX, centerY);
		bmdMinute.setBounds(centerX-bmdMinute.getIntrinsicWidth()/2, centerY-bmdMinute.getIntrinsicHeight()/2, centerX+bmdMinute.getIntrinsicWidth()/2, centerY+bmdMinute.getIntrinsicHeight()/2);
		bmdMinute.draw(canvas);
		canvas.restore();
		// 时针－－－－－－－－－－－－－－－－－－
		canvas.save();
		canvas.rotate(hDegree, centerX, centerY);
		bmdHour.setBounds(centerX-bmdHour.getIntrinsicWidth()/2, centerY-bmdHour.getIntrinsicHeight()/2, centerX+bmdHour.getIntrinsicWidth()/2, centerY+bmdHour.getIntrinsicHeight()/2);
		bmdHour.draw(canvas);
		canvas.restore();
		// hejianfeng end
	}
}
