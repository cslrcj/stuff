package com.magcomm.locker.lock;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import com.magcomm.applocker.R;
import com.magcomm.locker.ui.MainActivity;
import com.magcomm.locker.util.PrefUtils;
//Yar add start
import android.content.SharedPreferences.Editor;
//Yar add end

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.lang.reflect.Field; 
import java.util.Timer;
import java.util.TimerTask;

public class AppLockService extends Service {

    /**
     * Sent to {@link MainActivity} when the service has been completely started
     * and is running
     */
    public static final String BROADCAST_SERVICE_STARTED = "com.magcomm.locker.intent.action.service_started";
    /**
     * Sent to {@link MainActivity} when the service has been stopped
     */
    public static final String BROADCAST_SERVICE_STOPPED = "com.magcomm.locker.intent.action.service_stopped";
    /**
     * This category allows the receiver to receive actions relating to the
     * state of the service, such as when it is started or stopped
     */
    public static final String CATEGORY_STATE_EVENTS = "com.magcomm.locker.intent.category.service_start_stop_event";

    private static final int REQUEST_CODE = 0x1234AF;
    public static final int NOTIFICATION_ID = R.drawable.ic_launcher_applocker;
    private static final String TAG = "AppLockService";

    /**
     * Use this action to stop the intent
     */
    private static final String ACTION_STOP = "com.magcomm.locker.intent.action.stop_lock_service";
    /**
     * Starts the alarm
     */
    private static final String ACTION_START = "com.magcomm.locker.intent.action.start_lock_service";
    /**
     * When specifying this action, the service will initialize everything
     * again.<br>
     * This has only effect if the service was explicitly started using
     * {@link #getRunIntent(Context)}
     */
    private static final String ACTION_RESTART = "com.magcomm.locker.intent.action.restart_lock_service";
    private static final String EXTRA_FORCE_RESTART = "com.magcomm.locker.intent.extra.force_restart";
    private static final String ACTION_UPDATE_NOTIFY = "com.magcomm.locker.intent.update.notify";
    private ActivityManager mActivityManager;
    /**
     * 0 for disabled
     */
    private static long mShortExitMillis=0;

    private static boolean mRelockScreenOff;
    private static boolean mShowNotification;

    private boolean mExplicitStarted;
    private boolean mAllowDestroy;
    private boolean mAllowRestart;
    private Handler mHandler;
    private BroadcastReceiver mScreenReceiver;
    private Timer mTimer ;
    private LockTask mLockTask;
    private String mLastPackageName;
    /**
     * This map contains locked apps in the form<br>
     * <PackageName, ShortExitEndTime>
     */
    private static Map<String, Boolean> mLockedPackages;
    private Map<String, Runnable> mUnlockMap;
    private static PrefUtils prefs;

    @Override
    public IBinder onBind(Intent i) {
        return new LocalBinder();
    }

    public class LocalBinder extends Binder {
        public AppLockService getInstance() {
            return AppLockService.this;
        }
    }

    private final class ScreenReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                Log.i(TAG, "Screen ON");
                // Trigger package again
                mLastPackageName = "";
                startAlarm(AppLockService.this);
            }
            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                Log.i(TAG, "Screen OFF");
                stopAlarm(AppLockService.this);
                if (mRelockScreenOff) {
                    lockAll();
                }
            }
            if(intent.getAction().equals(ACTION_UPDATE_NOTIFY)){
            	startNotification();
            }
        }
    }

    ;

    @Override
    public void onCreate() {
        super.onCreate();
        mExplicitStarted=init();
        Log.d(TAG, "onCreate");

    }

    /**
     * Starts everything, including notification and repeating alarm
     *
     * @return True if all OK, false if the service is not allowed to start (the
     * caller should stop the service)
     */
    private boolean init() {
        Log.d(TAG, "init");
        //Log.d("", "dengjianzhang6+"+System.currentTimeMillis());
        if (new PrefUtils(this).isCurrentPasswordEmpty()) {
            Log.w(TAG, "Not starting service, current password empty");
            return false;
        }

        mHandler = new Handler();
        mActivityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);

        mUnlockMap = new HashMap<>();
        mLockedPackages = new HashMap<>();
        mScreenReceiver = new ScreenReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(ACTION_UPDATE_NOTIFY);
        registerReceiver(mScreenReceiver, filter);

        final Set<String> apps = PrefUtils.getLockedApps(this);
        for (String s : apps) {
            mLockedPackages.put(s, true);
        }
        prefs = new PrefUtils(this);
        boolean delay = prefs.getBoolean(R.string.pref_key_delay_status,
                R.bool.pref_def_delay_status);
        //Log.d("", "dengjianzhang2+"+delay);
        if (delay) {
            int secs = prefs.parseInt(R.string.pref_key_delay_time,
                    R.string.pref_def_delay_time);
            mShortExitMillis = secs * 1000;
        }else{
        	mShortExitMillis = 0l;
        }
        
        mRelockScreenOff = prefs.getBoolean(
                R.string.pref_key_relock_after_screenoff,
                R.bool.pref_def_relock_after_screenoff);
        
        mShowNotification = prefs.getBoolean(
                R.string.pref_key_hide_notification_icon,
                R.bool.pref_def_hide_notification_icon);

        startNotification();
        startAlarm(this);
        StartTimer();
        // Tell MainActivity we're done
        Intent i = new Intent(BROADCAST_SERVICE_STARTED);
        i.addCategory(CATEGORY_STATE_EVENTS);
        sendBroadcast(i);
        return true;
    }
    
    public static  void updateLockedPackages(Context c){
    		mLockedPackages.clear();
    		final Set<String> apps = PrefUtils.getLockedApps(c);
    		for (String s : apps) {
    			mLockedPackages.put(s, true);
    		}
    }
    public static  void updatePrefs(){
    	  boolean delay = prefs.getBoolean(R.string.pref_key_delay_status,
                  R.bool.pref_def_delay_status);
          if (delay) {
              int secs = prefs.parseInt(R.string.pref_key_delay_time,
                      R.string.pref_def_delay_time);
              mShortExitMillis = secs * 1000;
          }else{
          	  mShortExitMillis = 0l;
          }
          
          mRelockScreenOff = prefs.getBoolean(
                  R.string.pref_key_relock_after_screenoff,
                  R.bool.pref_def_relock_after_screenoff);
          
          mShowNotification = prefs.getBoolean(
                  R.string.pref_key_hide_notification_icon,
                  R.bool.pref_def_hide_notification_icon);
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
         Log.d(TAG, "test");
        if (intent == null || ACTION_START.equals(intent.getAction())) {
            if (!mExplicitStarted) {
                Log.d(TAG, "explicitStarted = false");
                if (mExplicitStarted == false) {
                    doStopSelf();
                    return START_NOT_STICKY;
                }
                mExplicitStarted = true;
            }
            //checkPackageChanged();
        } else if (ACTION_RESTART.equals(intent.getAction())) {
            if (mExplicitStarted
                    || intent.getBooleanExtra(EXTRA_FORCE_RESTART, false)) {
                Log.d(TAG,
                        "ACTION_RESTART (force="
                                + intent.getBooleanExtra(EXTRA_FORCE_RESTART,
                                false));
                // init();
            	doRestartSelf(); // not allowed, so service will restart
            } else {
                doStopSelf();
            }
        } else if (ACTION_STOP.equals(intent.getAction())) {
            Log.d(TAG, "ACTION_STOP");
            doStopSelf();
        }

        return START_STICKY;
    }
    
	private void StartTimer(){
			mTimer = new Timer("AppLockService");
			mLockTask = new LockTask(this);
			mTimer.schedule(mLockTask, 0L, 100L);
	}
	
	public class LockTask extends TimerTask {
		private Context mContext ;
		public LockTask(Context context){
			mContext= context ;	
		}
		
		@Override
		public void run() {
			checkPackageChanged();
		}
		
	}
	
    private void checkPackageChanged() {
        final String packageName = getTopPackageName();

        Log.d(TAG,"jeff packageName="+packageName);
        if (!packageName.equals(mLastPackageName)) {
            Log.d(TAG, "appchanged " + " (" + mLastPackageName + ">"
                    + packageName + ")");
            //Log.d("", "dengjianzhang3+"+mShortExitMillis);
            onAppClose(mLastPackageName, packageName);
            //Log.d("", "dengjianzhang4+"+mShortExitMillis);
            onAppOpen(packageName, mLastPackageName);
        }

        mLastPackageName = packageName;
    }

    private void onAppOpen(final String open, final String close) {
    	//For lock 'Download' added by Yar start 
    	if ("com.android.documentsui".equals(open)) {
    		if (mLockedPackages.containsKey("com.android.providers.downloads.ui")) {
                onLockedAppOpen("com.android.providers.downloads.ui");
            }
    		return;
    	}
    	//For lock 'Download' added by Yar end
        if (mLockedPackages.containsKey(open)) {
            onLockedAppOpen(open);
        }
    }

    private void onLockedAppOpen(final String open) {
        final boolean locked = mLockedPackages.get(open);
        // Log.d(TAG, "onLockedAppOpen (locked=" + locked + ")");
        if (locked) {
            showLocker(open);
        }
        removeRelockTimer(open);
    }

    private void showLocker(String packageName) {
        Intent intent = LockService.getLockIntent(this, packageName);
        intent.setAction(LockService.ACTION_COMPARE);
        intent.putExtra(LockService.EXTRA_PACKAGENAME, packageName);
        startService(intent);

    }

    private void onAppClose(String close, String open) {
        if (mLockedPackages.containsKey(close)) {
        	//Log.d("", "dengjianzhang+"+mShortExitMillis);
            onLockedAppClose(close, open);
        }
    }

    private void onLockedAppClose(String close, String open) {

        setRelockTimer(close);

        if (getPackageName().equals(close) || getPackageName().equals(open)) {
            // Don't interact with own app
            return;
        }

        if (mLockedPackages.containsKey(open)) {
            // The newly opened app needs a lock screen, so don't hide previous
            return;
        }
        LockService.hide(this);
    }

    private void setRelockTimer(String packageName) {
        boolean locked = mLockedPackages.get(packageName);
        if (!locked) {     	
            if (mShortExitMillis != 0) {
            	Log.d("", "dengjianzhang22+"+mShortExitMillis);
                Runnable r = new RelockRunnable(packageName);
                mHandler.postDelayed(r, mShortExitMillis);
                mUnlockMap.put(packageName, r);
            } else {
                lockApp(packageName);
            }
        }
    }

    private void removeRelockTimer(String packageName) {
        if (mUnlockMap.containsKey(packageName)) {
            mHandler.removeCallbacks(mUnlockMap.get(packageName));
            mUnlockMap.remove(packageName);
        }
    }

    /**
     * This class will re-lock an app
     */
    private class RelockRunnable implements Runnable {
        private final String mPackageName;

        public RelockRunnable(String packageName) {
            mPackageName = packageName;
        }

        @Override
        public void run() {
            lockApp(mPackageName);
        }
    }

    List<RunningTaskInfo> mTestList = new ArrayList<>();


    private String getTopPackageName() {
    	
    	final List<ActivityManager.RunningAppProcessInfo> pis = mActivityManager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo pi : pis) {
            if (pi.pkgList.length == 1&& 
            	pi.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND && 
            	pi.importanceReasonCode == 0){ 
            		return pi.pkgList[0];
            	}
            }
    
        return "";
//        final int PROCESS_STATE_TOP = 2;
//        RunningAppProcessInfo currentInfo = null;
//        Field field = null;
//        try {
//            field = RunningAppProcessInfo.class.getDeclaredField("processState");
//        } catch(Exception e){ 
//        	
//        }
//        ActivityManager am = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
//        List<RunningAppProcessInfo> appList = am.getRunningAppProcesses();
//        for (RunningAppProcessInfo app : appList) {
//            if (app.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND &&
//                app.importanceReasonCode == 0 ) {
//                Integer state = null;
//                try {
//                    state = field.getInt( app );
//                } catch(Exception e){
//                	
//                }
//                if (state != null && state == PROCESS_STATE_TOP) {
//                    currentInfo = app;
//                    break;
//                }
//            }
//        }
//        return currentInfo.pkgList[0];
    }

    public void unlockApp(String packageName) {
        Log.d(TAG, "unlocking app (packageName=" + packageName + ")");
        if (mLockedPackages.containsKey(packageName)) {
            mLockedPackages.put(packageName, false);
        }
    }

    private void lockAll() {
        for (Map.Entry<String, Boolean> entry : mLockedPackages.entrySet()) {
            entry.setValue(true);
        }
    }

    void lockApp(String packageName) {
        if (mLockedPackages.containsKey(packageName)) {
            mLockedPackages.put(packageName, true);
        }
    }

    private void startNotification() {
        // Start foreground anyway
        if (!mShowNotification){
        	startForegroundWithNotification();
        }else{
            // Retain foreground state
            HelperService.removeNotification(this);
        }
    }

    @SuppressLint("InlinedApi")
    private void startForegroundWithNotification() {
        Log.d(TAG, "showNotification");

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
        nb.setPriority( Notification.PRIORITY_DEFAULT);
     
        startForeground(NOTIFICATION_ID, nb.build());
    }

    public static void start(Context c) {
    	Intent i = new Intent(c, AppLockService.class);
        i.setAction(ACTION_START);
        c.startService(i);
        //startAlarm(c);
    }

    /**
     * @param c
     * @return The new state for the service, true for running, false for not
     * running
     */
    public static boolean toggle(Context c) {
        if (isRunning(c)) {
            stop(c);
            //Yar add start
            Editor mEditor = PrefUtils.appsPrefs(c).edit();
            mEditor.putBoolean(c.getString(R.string.pref_key_start_boot), false);
            PrefUtils.apply(mEditor);
            // Yar add end
            return false;
        } else {
            start(c);
            // Yar add start
            Editor mEditor = PrefUtils.appsPrefs(c).edit();
            mEditor.putBoolean(c.getString(R.string.pref_key_start_boot), true);
            PrefUtils.apply(mEditor);
            // Yar add end
            return true;
        }
       
    }

    public static boolean isRunning(Context c) {
        ActivityManager manager = (ActivityManager) c
                .getSystemService(Context.ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager
                .getRunningServices(Integer.MAX_VALUE)) {
            if (AppLockService.class.getName().equals(
                    service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Starts the service
     */
    private static void startAlarm(Context c) {
        AlarmManager am = (AlarmManager) c.getSystemService(ALARM_SERVICE);
        PendingIntent pi = getRunIntent(c);
        SharedPreferences sp = PrefUtils.prefs(c);
        String defaultPerformance = c.getString(R.string.pref_val_perf_normal);
        String s = sp.getString(c.getString(R.string.pref_key_performance),
                defaultPerformance);
        if (s.length() == 0)
            s = "0";
        long interval = Long.parseLong(s);
        //Log.d("", "dengjianzhang10+"+System.currentTimeMillis());
        long startTime = SystemClock.elapsedRealtime();
        am.setRepeating(AlarmManager.ELAPSED_REALTIME, startTime, interval, pi);
        //Log.d("", "dengjianzhang11+"+System.currentTimeMillis());
    }

    private static PendingIntent running_intent;

    private static PendingIntent getRunIntent(Context c) {
        if (running_intent == null) {
            Intent i = new Intent(c, AppLockService.class);
            i.setAction(ACTION_START);
            running_intent = PendingIntent.getService(c, REQUEST_CODE, i, 0);
        }
        return running_intent;
    }

    private static void stopAlarm(Context c) {
        AlarmManager am = (AlarmManager) c.getSystemService(ALARM_SERVICE);
        am.cancel(getRunIntent(c));
    }

    /**
     * Stop this service, also stopping the alarm
     */
    public static void stop(Context c) {
        stopAlarm(c);
        Intent i = new Intent(c, AppLockService.class);
        i.setAction(ACTION_STOP);
        c.startService(i);
    }

    /**
     * Forces the service to stop and then start again. This means that if the
     * service was already stopped, it will just start
     */
    public static void forceRestart(Context c) {
        Intent i = new Intent(c, AppLockService.class);
        i.setAction(ACTION_RESTART);
        i.putExtra(EXTRA_FORCE_RESTART, true);
        c.startService(i);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: (mAllowRestart=" + mAllowRestart + ")");
        if (mScreenReceiver != null)
            unregisterReceiver(mScreenReceiver);
        if (mShowNotification)
            stopForeground(true);
		if(mLockTask!=null){
        	mLockTask.cancel();
        	mLockTask=null;
		}
		if(mTimer!=null){
        	mTimer.cancel();
        	mTimer = null;
		}
        
        if (mAllowRestart) {
            start(this);
            mAllowRestart = false;
            return;
        }
         
        Log.i(TAG, "onDestroy (mAllowDestroy=" + mAllowDestroy + ")");
        if (!mAllowDestroy) {
            Log.d(TAG, "Destroy not allowed, restarting service");
            start(this);
        } else {
            // Tell MainActivity we're stopping
            Intent i = new Intent(BROADCAST_SERVICE_STOPPED);
            i.addCategory(CATEGORY_STATE_EVENTS);
            sendBroadcast(i);
        }
        mAllowDestroy = false;
    }

    private void doStopSelf() {
        stopAlarm(this);
        mAllowDestroy = true;
        stopForeground(true);
        stopSelf();
    }

    private void doRestartSelf() {
        Log.d(TAG, "Setting allowrestart to true");
        mAllowRestart = true;
        stopSelf();
    }

	public static void updateNoify(Context context) {
		Intent i = new Intent(ACTION_UPDATE_NOTIFY);
		context.sendBroadcast(i);
	}

}
