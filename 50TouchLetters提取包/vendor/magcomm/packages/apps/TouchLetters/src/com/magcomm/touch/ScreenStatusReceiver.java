package com.magcomm.touch;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.ImageView;

public class ScreenStatusReceiver extends BroadcastReceiver{

	//关闭霍尔窗口广播
	public static String TOUCH_LETTER = "magcomm.action.TOUCH_LETTER";

	@SuppressLint("NewApi")
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		
		//盖上盖子，打开霍尔窗口广播
		if (TOUCH_LETTER.equals(intent.getAction())) {
			long t3 = System.currentTimeMillis(); // 排序前取得当前时间
                        Log.i("jiaAAAAA", " onReceive TOUCH_LETTER");
			Bundle bundle = intent.getExtras();// .getExtras()得到intent所附带的额外数据
                        Log.i("jiaAAAAA", "ScreenStatusReceiver-------------t3="+t3);
			SharedPreferences mySharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
			Boolean isOpen = mySharedPreferences.getBoolean("letter_touch", true);
			if (bundle != null) {
				if (isOpen) {
					String str = bundle.getString("letter", "a");// getString()返回指定key的值
					// String name = intent.getStringExtra("name");
					Intent mIntent = new Intent();
					mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|
							Intent.FLAG_ACTIVITY_NO_HISTORY |
							Intent.FLAG_ACTIVITY_CLEAR_TOP |
							Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
					// wealthIntent.setClass(context,
					// WealthCalendarActivity.class);
					mIntent.setClass(context, TouchLetterActivity.class);
					mIntent.putExtra("letter", str);
					context.startActivity(mIntent);
				}
			}
		}
		
	}

}
