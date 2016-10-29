package com.magcomm.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.magcomm.hall.MainGroup;

public class ScreenStatusReceiver extends BroadcastReceiver{

	//关闭霍尔窗口广播
	public static String LID_OFF = "magcomm.action.LID_OFF";
	//打开霍尔窗口广播
	public static String LID_ON = "magcomm.action.LID_ON";
	
	public static String ACTIVITY_FINISH = "magcomm.action.FINISH";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		
		//盖上盖子，打开霍尔窗口广播
		if (LID_ON.equals(intent.getAction())) {
			Log.i("AAAAA", "apk_start");
			if (MainGroup.get_mg_instance() == null)
			{
			Intent wealthIntent = new Intent();
			wealthIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);  
			//wealthIntent.setClass(context, WealthCalendarActivity.class);
			wealthIntent.setClass(context, MainGroup.class);
			context.startActivity(wealthIntent);
			}
			
		}
		// 打开盖子，关闭霍尔窗口广播
		else if (LID_OFF.equals(intent.getAction())) {
			Log.i("AAAAA", "apk_finish");
			//context.sendBroadcast(new Intent(ACTIVITY_FINISH));
			if (MainGroup.get_mg_instance() != null)
			{
				MainGroup.get_mg_instance().finish();
			}
		}
		
	}

}
