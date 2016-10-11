package com.magcomm.locker.lock;

import com.magcomm.locker.ui.MainActivity;

import com.magcomm.applocker.R;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.app.Notification;
import android.util.Log;

public class HelperService extends Service {

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Intent i = new Intent(this, MainActivity.class);
		PendingIntent pi = PendingIntent.getActivity(this, 0, i, 0);
		String title = getString(R.string.notification_title);
		String content = getString(R.string.notification_state_locked);
		Notification.Builder nb = new Notification.Builder(this);
		nb.setSmallIcon(R.drawable.ic_notify_applocker);
		nb.setContentTitle(title);
		nb.setContentText(content);
		nb.setWhen(System.currentTimeMillis());
		nb.setContentIntent(pi);
		nb.setOngoing(true);
		nb.setLocalOnly(true);
		Log.d("", "Starting fg");
		startForeground(AppLockService.NOTIFICATION_ID, nb.build());

		stopForeground(true);
		stopSelf();
		return START_NOT_STICKY;
	}

	public static void removeNotification(Context c) {
		Intent i = new Intent(c, HelperService.class);
		c.startService(i);
	}
}
