package com.magcomm.locker.lock;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
//import android.gxFP.FingerprintManager;
//import android.gxFP.IVerifyCallback;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.provider.Settings;	//For iris control Yar add

import com.magcomm.applocker.R;
import com.magcomm.locker.lock.PasswordView.OnNumberListener;
import com.magcomm.locker.lock.PatternView.DisplayMode;
import com.magcomm.locker.lock.PatternView.OnPatternListener;
import com.magcomm.locker.util.PrefUtils;
import com.magcomm.locker.util.Util;
import com.magcomm.util.Analytics;

import java.io.FileNotFoundException;
//hejianfeng add start
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
//hejianfeng add end

//Yar add start
import com.magcomm.locker.ui.Dialogs;
import com.magcomm.locker.ui.NavigationElement;
//Yar add end

public class LockService extends Service implements View.OnClickListener,
        View.OnKeyListener ,IrisLockListener{

    private static final boolean DEBUG_VIEW = true;
    private static final boolean DEBUG_BIND = true;

    private enum LeftButtonAction {
        BACK, CANCEL
    }

    private ViewState mViewState = ViewState.HIDDEN;

    private enum ViewState {
        /**
         * The view is visible but not yet completely shown
         */
        SHOWING,
        /**
         * The view has been completely animated and the user is ready to
         * interact with it
         */
        SHOWN,
        /**
         * The user has unlocked / pressed back, and the view is animating
         */
        HIDING,
        /**
         * The view is not visible to the user
         */
        HIDDEN
    }
/**
    private FingerprintManager.VerifySession mVerifySession=null;
	private FingerprintManager mFingerprintManager=null;
	
	private void startFingerVerify() {
		mFingerprintManager=FingerprintManager.getFpManager();
		mVerifySession=mFingerprintManager.newVerifySession(mVerifyCallback);
		mVerifySession.enter();
	}
	
	private void stopFingerVerify() {
		if(mVerifySession!=null){
			mVerifySession.exit();
		}
	}

	private final IVerifyCallback.Stub mVerifyCallback = new IVerifyCallback.Stub() {
		public void handleMessage(int msg, int arg0, int arg1,byte[] data)
				throws RemoteException {
			switch (msg) {
				case 257:
					mfpHandler.sendMessage(mfpHandler.obtainMessage(1,
						arg0, arg1));
					break;
				case 259:
					mfpHandler.sendMessage(mfpHandler.obtainMessage(2,
							arg0, arg1));
					break;

				default:
					break;
			}

			}
	};

	Handler mfpHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch(msg.what) {
            	case 1:
            		exitSuccessCompare();
            		break;
            	case 2:
            		
            		break;
            	default:
            		break;
			}
		}
	};
	*/
    private class MyOnNumberListener implements OnNumberListener {

        @Override
        public void onStart() {
            mTimeFirstFingerDown = System.nanoTime();
        }

        @Override
        public void onBackButton() {
            updatePassword();
        }

        @Override
        public void onBackButtonLong() {
            updatePassword();
        }

        @Override
        public void onNumberButton(String newPassword) {
            if (newPassword.length() > MAX_PASSWORD_LENGTH) {
                newPassword = newPassword.substring(0, MAX_PASSWORD_LENGTH);
                mLockPasswordView.setPassword(newPassword);
            }
            updatePasswordTextView(newPassword);
            if (ACTION_COMPARE.equals(mAction)) {
                doComparePassword(false);
            }
        }

        @Override
        public void onOkButton() {
            if (ACTION_COMPARE.equals(mAction)) {
                doComparePassword(true);
            }
        }

        @Override
        public void onOkButtonLong() {
        }
    }

    private class MyOnPatternListener implements OnPatternListener {

        @Override
        public void onPatternCellAdded() {
        }

        @Override
        public void onPatternCleared() {
        	
        }

        @Override
        public void onPatternDetected() {
            if (ACTION_COMPARE.equals(mAction)) {
                doComparePattern();
            // Yar modify start
            } else if (mRightButtonAction != null && mRightButtonAction != RightButtonAction.CONTINUE) {
            	doConfirm();
            // Yar modify end
            } else if (ACTION_CREATE.equals(mAction)) {
                mViewMessage.setText(R.string.pattern_detected);
            }
        }

        @Override
        public void onPatternStart() {
            mTimeFirstFingerDown = System.nanoTime();
            mLockPatternView.cancelClearDelay();
            mLockPatternView.setDisplayMode(DisplayMode.Correct);
            if (ACTION_CREATE.equals(mAction)) {
                if (mRightButtonAction == RightButtonAction.CONTINUE) {
                    mViewMessage.setText(R.string.pattern_change_head);
                } else {
                    mViewMessage.setText(R.string.pattern_change_confirm);
                }
            }
        }
    }

    private enum RightButtonAction {
        CONFIRM, CONTINUE
    }

    private static final String CLASSNAME = LockService.class.getName();

    /**
     * Check a currently set password, (either number or pattern)
     */
    public static final String ACTION_COMPARE = CLASSNAME + ".action.compare";

    /**
     * Create a new password by asking the user to enter it twice (either number
     * or pattern)
     */
    public static final String ACTION_CREATE = CLASSNAME + ".action.create";

    private static final String ACTION_NOTIFY_PACKAGE_CHANGED = CLASSNAME
            + ".action.notify_package_changed";

    public static final String EXTRA_LOCK = CLASSNAME + ".action.extra_lock";

    /**
     * When the action is {@link #ACTION_COMPARE} use {@link #EXTRA_PACKAGENAME}
     * for specifying the target app.
     */
    public static final String EXTRA_PACKAGENAME = CLASSNAME
            + ".extra.target_packagename";
    /**
     * A {@link LockPreferences} providing additional details on how this
     * {@link LockService} should behave. You should start with a
     * and change only the
     * properties you want to.
     */
    private static final String EXTRA_PREFERENCES = CLASSNAME + ".extra.options";
    public static final int PATTERN_COLOR_BLUE = 2;
    public static final int PATTERN_COLOR_GREEN = 1;

    public static final int PATTERN_COLOR_WHITE = 0;
    private static final String ACTION_HIDE = CLASSNAME + ".action.hide";

    private static final int MAX_PASSWORD_LENGTH = 8;
    private static final long PATTERN_DELAY = 600;

    private static final String TAG = LockService.class.getSimpleName();

    /**
     * Time in terms of {@link System#nanoTime()} when the first finger was
     * placed on the view
     */
    private long mTimeFirstFingerDown;

    /**
     * Time in terms of {@link System#nanoTime()} when the view was completely
     * shown
     */
    private long mTimeViewShown;

    /**
     * Finger distance in inches
     */
    private float mFingerDistance;

    private static int calculateInSampleSize(BitmapFactory.Options options,
                                             int reqWidth, int reqHeight) {
        int scale = 1;
        int width = options.outWidth;
        int height = options.outHeight;
        while (true) {
            if (width / 2 < reqWidth || height / 2 < reqHeight) {
                break;
            }
            width /= 2;
            height /= 2;
            scale *= 2;
        }
        return scale;
    }

    /**
     * Get the lock intent (no options are provided)
     *
     * @param c
     * @param packageName
     * @return
     */
    public static Intent getLockIntent(Context c, String packageName) {
        Intent i = new Intent(c, LockService.class);
        i.setAction(ACTION_COMPARE);
        i.putExtra(EXTRA_PACKAGENAME, packageName);
        return i;
    }

    public static void hide(Context c) {
        Intent i = new Intent(c, LockService.class);
        i.setAction(ACTION_HIDE);
        c.startService(i);
    }

    /**
     * Show this {@link LockService} for the given package name
     *
     * @param c
     * @param packageName
     */
    public static void showCompare(Context c, String packageName) {
        c.startService(getLockIntent(c, packageName));
        mType = NavigationElement.TYPE_STATUS;// Yar add here
    }
    
    // Yar add start
    private static int mType = NavigationElement.TYPE_STATUS;
    public static void showCompare(Context c, String packageName, int type) {
    	showCompare(c, packageName);
    	mType = type;
    }
    // Yar add end

    public static void showCreate(Context c, int type) {
        Log.d(TAG, "showCreate (type=" + type + ")");
        Intent i = new Intent(c, LockService.class);
        i.setAction(ACTION_CREATE);
        LockPreferences prefs = new LockPreferences(c);
        prefs.type = type;
        i.putExtra(EXTRA_PREFERENCES, prefs);
        c.startService(i);
    }

    public static void showCreate(Context c, int type, int size) {
        Log.d(TAG, "showCreate (type=" + type + ",size=" + size + ")");
        Intent i = new Intent(c, LockService.class);
        i.setAction(ACTION_CREATE);
        LockPreferences prefs = new LockPreferences(c);
        prefs.type = type;
        prefs.patternSize = size;
        i.putExtra(EXTRA_PREFERENCES, prefs);
        c.startService(i);
    }

    @SuppressLint("InlinedApi")
    private static int getLandscapeCompat() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD) {
            return ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        } else {
            return ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;
        }
    }

    private String mAction;
    /**
     * Called after views are inflated
     */
    // private AdViewManager mAdViewManager;
    // private AppLockService mAppLockService;
    private AppLockService mAppLockService;
    private Analytics mAnalytics;
    private Animation mAnimHide;

    private Animation mAnimShow;
    private ImageView mAppIcon;

    private ServiceState mServiceState = ServiceState.NOT_BOUND;

    private enum ServiceState {
        /**
         * Service is not bound
         */
        NOT_BOUND,
        /**
         * We have requested binding, but not yet received it...
         */
        BINDING,
        /**
         * Service is successfully bound (we can interact with it)
         */
        BOUND,
        /**
         * Service requesting unbind
         */
        UNBINDING
    }

    private final ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName cn, IBinder binder) {
            if (DEBUG_BIND)
                Log.v(TAG, "Service bound (mServiceState=" + mServiceState
                        + ")");
            final AppLockService.LocalBinder b = (AppLockService.LocalBinder) binder;
            mAppLockService = b.getInstance();
            mServiceState = ServiceState.BOUND;
        }

        @Override
        public void onServiceDisconnected(ComponentName cn) {
            if (DEBUG_BIND)
                Log.v(TAG, "Unbound service (mServiceState=" + mServiceState
                        + ")");
            // We can't make it "UNBOUND", because even if the server got
            // unbound, android expects us to call unbindService
            mServiceState = ServiceState.UNBINDING;
        }
    };

    // views
    private RelativeLayout mContainer;
    private LinearLayout mFooterButtons;

    private Intent mIntent;
    private WindowManager.LayoutParams mLayoutParams;

    private Button mLeftButton;

    private LeftButtonAction mLeftButtonAction;

    private PasswordView mLockPasswordView;

    private PatternView mLockPatternView;

    private ViewGroup mLockView;
    private String mNewPassword;
    private String mNewPattern;
    // options
    private String mPackageName;
    private OnNumberListener mPasswordListener;
    private OnPatternListener mPatternListener;
    private Button mRightButton;
    private RightButtonAction mRightButtonAction;

    private View mRootView;

    private TextView mTextViewPassword;

    private ImageView mViewBackground;

    private TextView mViewMessage;

    private TextView mViewTitle;

    private WindowManager mWindowManager;
    ;

    private LockPreferences options;

    Bitmap decodeSampledBitmapFromUri(Uri uri, int reqWidth,
                                      int reqHeight) throws FileNotFoundException {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(getContentResolver().openInputStream(uri),
                null, options);

        options.inSampleSize = calculateInSampleSize(options, reqWidth,
                reqHeight);

        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeStream(getContentResolver()
                .openInputStream(uri), null, options);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    ;

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.lock_footer_b_left:
                if (ACTION_CREATE.equals(mAction)) {
                    if (mLeftButtonAction == LeftButtonAction.BACK) {
                        setupFirst();
                    } else {
                        exitCreate();
                    }
                }
                break;
            case R.id.lock_footer_b_right:
                if (ACTION_CREATE.equals(mAction)) {
                    if (mRightButtonAction == RightButtonAction.CONTINUE) {
                        setupSecond();
                    } else {
                        doConfirm();
                    }
                }
                break;
        }
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Log.d(TAG, "onConfigChange");
        // super.onConfigurationChanged(newConfig);
        if (mViewState == ViewState.SHOWING || mViewState == ViewState.SHOWN) {
            showView();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // if (mAdViewManager != null)
        // mAdViewManager.onDestroy();
        //hejianfeng add start
        if(receiver!=null){
        	getApplicationContext().unregisterReceiver(receiver);
        }
        //hejianfeng add end
        if (DEBUG_BIND)
            Log.v(TAG, "onDestroy (mServiceState=" + mServiceState + ")");
        if (mServiceState != ServiceState.NOT_BOUND) {
            Log.v(TAG, "onDestroy unbinding");
            unbindService(mConnection);
            mServiceState = ServiceState.NOT_BOUND;
        }
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                finish(false);
                return true;
        }
        return true;
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Log.d(TAG, "onLowMemory()");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mAnalytics = new Analytics(this);
        //hejianfeng add start 
        mIrisUnlock= new IrisUnlock(getApplicationContext());
        mIrisUnlock.setOnIrisLockListener(this);
        registerScreenActionReceiver();
        //hejianfeng add end
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            return START_NOT_STICKY;
        }
        Log.d(TAG, "action: " + intent.getAction());
        if (ACTION_HIDE.equals(intent.getAction())) {
            finish(true);
            return START_NOT_STICKY;
        }
        if (ACTION_NOTIFY_PACKAGE_CHANGED.equals(intent.getAction())) {
            String newPackageName = intent.getStringExtra(EXTRA_PACKAGENAME);
            if (newPackageName == null
                    || !getPackageName().equals(newPackageName)) {
                finish(true);
                return START_NOT_STICKY;
            }
        } else {
            mIntent = intent;
            //hejianfeng add start
            KeyguardManager keyguardManager = (KeyguardManager)getApplicationContext().getSystemService(Context.KEYGUARD_SERVICE);
            Log.d(TAG, "jeff keyguardManager.isKeyguardLocked" + keyguardManager.isKeyguardLocked());
            if(!keyguardManager.isKeyguardLocked()){
            	showView();
            }
            //hejianfeng add end
        }
        return START_NOT_STICKY;
    }

    /**
     * @param explicit true if the user has clicked the OK button to explicitly ask
     *                 for a password check (this should never happen)
     */
    private void doComparePassword(boolean explicit) {
        final String currentPassword = mLockPasswordView.getPassword();
        if (currentPassword.equals(options.password)) {
            mFingerDistance = mLockPasswordView.getFingerDistance();
            exitSuccessCompare();
        } else if (explicit) {
            mLockPasswordView.clearPassword();
            updatePassword();
            Toast.makeText(this, R.string.locker_invalid_password,
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Called every time a pattern has been detected by the user and the action
     * was {@link #ACTION_COMPARE}
     */
    private void doComparePattern() {
        final String currentPattern = mLockPatternView.getPatternString();
        if (currentPattern.equals(options.pattern)) {
            mFingerDistance = mLockPatternView.getFingerDistance();
            exitSuccessCompare();
        } else {
            if (options.patternErrorStealth) {
                Toast.makeText(this, R.string.locker_invalid_pattern,
                        Toast.LENGTH_SHORT).show();
                mLockPatternView.clearPattern();
            } else {
                mLockPatternView.setDisplayMode(DisplayMode.Wrong);
                mLockPatternView.clearPattern(PATTERN_DELAY);
            }
        }
    }

    private void doConfirm() {
        if (options.type == LockPreferences.TYPE_PATTERN) {
            doConfirmPattern();
        } else {
            doConfirmPassword();
        }
    }

    private void doConfirmPassword() {
        final String newValue = mLockPasswordView.getPassword();
        if (!newValue.equals(mNewPassword)) {
            Toast.makeText(this, R.string.password_change_not_match,
                    Toast.LENGTH_SHORT).show();
            //Yar modify start
            //setupFirst();
            //Yar modify end
            return;
        }
        PrefUtils prefs = new PrefUtils(this);
        prefs.put(R.string.pref_key_password, newValue);
        prefs.putString(R.string.pref_key_lock_type,
                R.string.pref_val_lock_type_password);
        prefs.apply();
        Toast.makeText(this, R.string.password_change_saved, Toast.LENGTH_SHORT)
                .show();
        exitCreate();
    }

    private void doConfirmPattern() {
        final String newValue = mLockPatternView.getPatternString();
        if (!newValue.equals(mNewPattern)) {
            Toast.makeText(this, R.string.pattern_change_not_match,
                    Toast.LENGTH_SHORT).show();
            mLockPatternView.setDisplayMode(DisplayMode.Wrong);
            //Yar modify start
            //setupFirst();
            //Yar modify end
            return;
        }
        // patterns are equal
        PrefUtils prefs = new PrefUtils(this);
        prefs.put(R.string.pref_key_pattern, newValue);
        prefs.putString(R.string.pref_key_lock_type,
                R.string.pref_val_lock_type_pattern);
        // Save size as a string
        prefs.put(R.string.pref_key_pattern_size,
                String.valueOf(options.patternSize));
        prefs.apply();
        Toast.makeText(this, R.string.pattern_change_saved, Toast.LENGTH_SHORT)
                .show();
        exitCreate();
    }

    private void exitCreate() {
        AppLockService.forceRestart(this);
        finish(true);
    }

    /**
     * Exit when an app has been unlocked successfully
     */
    private void exitSuccessCompare() {
        long current = System.nanoTime();
        long total = (current - mTimeViewShown) / 1000000;
        long interacting = (current - mTimeFirstFingerDown) / 1000000;
        if (mPackageName == null || mPackageName.equals(getPackageName())) {
            finish(true);
            return;
        }
        if (mServiceState == ServiceState.BOUND) {
            mAppLockService.unlockApp(mPackageName);
        } else {
            if (DEBUG_BIND)
                Log.w(TAG, "Not bound to lockservice (mServiceState="
                        + mServiceState + ")");
        }
        finish(true);
    }

    private void finish(boolean unlocked) {
        if (!unlocked && ACTION_COMPARE.equals(mAction)) {
            final Intent i = new Intent(Intent.ACTION_MAIN);
            i.addCategory(Intent.CATEGORY_HOME);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
        }
        
        //Yar add start
        if (mType == NavigationElement.TYPE_CHANGE) {
        	Dialogs.getChangePasswordDialog(getApplicationContext()).show();
        	mType = NavigationElement.TYPE_STATUS;
        }
        //Yar add end
        
        hideView();
//        if (SystemProperties.get("ro.goodix_fp_support").equals("1")) {
//        	stopFingerVerify();
//       }
        //hejianfeng add start
        if(mIrisUnlock!=null){
        	mIrisUnlock.stop();
        }
        //hejianfeng add end
    }

    private int getScreenOrientation() {
        String port = getString(R.string.pref_val_orientation_portrait);
        String auto = getString(R.string.pref_val_orientation_auto_rotate);
        String land = getString(R.string.pref_val_orientation_landscape);
        if (port.equals(options.orientation)) {
            return ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        } else if (land.equals(options.orientation)) {
            // workaround for older versions
            return getLandscapeCompat();
        } else if (auto.equals(options.orientation)) {
            return ActivityInfo.SCREEN_ORIENTATION_SENSOR;
        } else {
            // default to system setting
            return ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
        }
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    private Point getSizeCompat(Display display) {
        Point p = new Point();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR2) {
            p.x = display.getWidth();
            p.y = display.getHeight();
        } else {
            display.getSize(p);
        }
        return p;
    }

    /**
     * Hides the view from the window, and stops the service
     */
    private void hideView() {
        if (DEBUG_VIEW)
            Log.v(TAG, "called hideView" + " (mViewState=" + mViewState + ")");
        if (mViewState == ViewState.HIDING || mViewState == ViewState.HIDDEN) {
            Log.w(TAG, "called hideView not hiding (mViewState=" + mViewState
                    + ")");
            onViewHidden();
            return;
        }
        if (mViewState == ViewState.SHOWING) {
            cancelAnimations();
        }
        mViewState = ViewState.HIDING;
        hideViewAnimate();
    }

    private void hideViewAnimate() {
        if (DEBUG_VIEW)
            Log.v(TAG, "called hideViewAnimate" + " (mViewState=" + mViewState
                    + ")");
        // Log.d(TAG, "animating hide (resId=" + options.hideAnimationResId
        // + ",millis=" + options.hideAnimationMillis + ")");
        if (options.hideAnimationResId == 0 || options.hideAnimationMillis == 0) {
            onViewHidden();
            return;
        }

        mAnimHide = AnimationUtils.loadAnimation(this,
                options.hideAnimationResId);
        mAnimHide.setDuration(options.hideAnimationMillis);
        mAnimHide.setFillEnabled(true);
        mAnimHide.setDetachWallpaper(false);
        mAnimHide.setAnimationListener(new BaseAnimationListener() {

            @Override
            public void onAnimationEnd(Animation animation) {
                // Avoid ugly android error message
                new Handler().post(new Runnable() {

                    @Override
                    public void run() {
                        onViewHidden();
                    }
                });
            }

        });
        mContainer.startAnimation(mAnimHide);
    }

    /**
     * Cancels the animation but DOES NOT REMOVE THE VIEW FROM WINDOWMANAGER
     */
    private void cancelAnimations() {
        if (DEBUG_VIEW)
            Log.v(TAG, "called hideViewCancel" + " (mViewState=" + mViewState
                    + ")");
        if (mViewState == ViewState.HIDING) {
            mAnimHide.setAnimationListener(null);
            mAnimHide.cancel();
            mAnimHide = null;
        } else if (mViewState == ViewState.SHOWING) {
            mAnimShow.setAnimationListener(null);
            mAnimShow.cancel();
            mAnimShow = null;
        }
    }

    private void onViewHidden() {

        if (DEBUG_VIEW)
            Log.v(TAG, "called onViewHidden" + " (mViewState=" + mViewState
                    + ")");
        if (mViewState != ViewState.HIDDEN) {
            mViewState = ViewState.HIDDEN;
            mWindowManager.removeView(mRootView);
        }
        mAnimHide = null;

        // With stopSelf there is a problem with the rotation
        // If this isn't in, the ad view will not load

        stopSelf();
    }

    /**
     * Should be only called from
     *
     * @return
     */
    private View inflateRootView() {
        if (DEBUG_VIEW)
            Log.v(TAG, "called inflateRootView" + " (mViewState=" + mViewState
                    + ")");
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        LayoutInflater li = LayoutInflater.from(this);

        setTheme(R.style.LockActivityTheme);
        View root = (View) li.inflate(R.layout.layout_alias_locker, null);
        mContainer = (RelativeLayout) root.findViewById(R.id.lock_container);
        mViewBackground = (ImageView) root
                .findViewById(R.id.lock_iv_background);
        root.setOnKeyListener(this);
        root.setFocusable(true);
        root.setFocusableInTouchMode(true);

        mViewTitle = (TextView) root.findViewById(R.id.lock_tv_title);
        mViewMessage = (TextView) root.findViewById(R.id.lock_tv_footer);
        mAppIcon = (ImageView) root.findViewById(R.id.lock_iv_app_icon);
        mLockView = (ViewGroup) root.findViewById(R.id.lock_lockview);

        mFooterButtons = (LinearLayout) root
                .findViewById(R.id.lock_footer_buttons);
        mLeftButton = (Button) root.findViewById(R.id.lock_footer_b_left);
        mRightButton = (Button) root.findViewById(R.id.lock_footer_b_right);

        mRightButton.setOnClickListener(this);
        mLeftButton.setOnClickListener(this);

        mPasswordListener = new MyOnNumberListener();
        mPatternListener = new MyOnPatternListener();
        return root;
    }

    private void afterInflate() {
        setBackground();
//        if (SystemProperties.get("ro.goodix_fp_support").equals("1")) {
//        	startFingerVerify();
//        }
        switch (options.type) {
            case LockPreferences.TYPE_PATTERN:
                showPatternView();
                break;
            case LockPreferences.TYPE_PASSWORD:
                showPasswordView();
                break;
        }
        // Views
        if (ACTION_COMPARE.equals(mAction)) {
            mAppIcon.setVisibility(View.VISIBLE);
            mFooterButtons.setVisibility(View.GONE);
            ApplicationInfo ai = Util.getaApplicationInfo(mPackageName, this);
            if (ai != null) {
                // Load info of Locker
                String label = ai.loadLabel(getPackageManager()).toString();
                Drawable icon = ai.loadIcon(getPackageManager());
                Util.setBackgroundDrawable(mAppIcon, icon);
                mViewTitle.setText(label);
                if(options.type==LockPreferences.TYPE_PATTERN){
                	mViewMessage.setVisibility(View.VISIBLE);
                	mViewMessage.setText(R.string.pattern_information_idle);
                }else if(options.type==LockPreferences.TYPE_PASSWORD){
                	mViewMessage.setVisibility(View.VISIBLE);
                	mViewMessage.setText(R.string.locker_footer_default);
                }else{
                	 mViewMessage.setVisibility(View.GONE);
                }
            } else {
                // if we can't load, don't take up space
                mAppIcon.setVisibility(View.GONE);
            }
        } else if (ACTION_CREATE.equals(mAction)) {
            mAppIcon.setVisibility(View.GONE);
            mFooterButtons.setVisibility(View.VISIBLE);
            setupFirst();
        }
        //hejianfeng add start
        Log.v(TAG,"jeff mPackageName="+mPackageName);
        boolean isIris = Settings.System.getInt(getContentResolver(), "iris_state", 0) == 1;	//For iris control Yar add
        if(mPackageName != null && isIris && !(mPackageName.equals("com.magcomm.applocker"))){	//For iris control Yar modify
        	startIrisLock();
        }
        //hejianfeng add end
    }
    //hejianfeng add start
    private IrisUnlock mIrisUnlock;
    private void startIrisLock(){
    	mIrisUnlock.initializeView(mViewMessage);
    	mIrisUnlock.start();
    }
    @Override
    public void IrisSuccess(){
		Log.v(TAG,"jeff IrisSuccess()");
		exitSuccessCompare();
    }
    private void registerScreenActionReceiver(){   
        final IntentFilter filter = new IntentFilter();   
        filter.addAction(Intent.ACTION_SCREEN_OFF);   
        filter.addAction(Intent.ACTION_SCREEN_ON);   
        getApplicationContext().registerReceiver(receiver, filter);   
    }   
       
    private BroadcastReceiver receiver = new BroadcastReceiver(){   
       
        @Override   
        public void onReceive(final Context context, final Intent intent) {   
            // Do your action here   
        	if(intent.getAction().equals(Intent.ACTION_SCREEN_OFF)){
        		Log.v(TAG,"jeff onReceive action= ACTION_SCREEN_OFF");
        		if(mIrisUnlock!=null){
        			Log.v(TAG,"jeff onReceive action= mIrisUnlock.stop()");
                	mIrisUnlock.stop();
                }
        		if(receiver!=null){
        			getApplicationContext().unregisterReceiver(receiver);
        			receiver=null;
        		}
        		
        	}else if(intent.getAction().equals(Intent.ACTION_SCREEN_ON)){
        		Log.v(TAG,"jeff onReceive action= ACTION_SCREEN_ON");
        	}
       
        }   
       
    }; 
    //hejianfeng add end
    /**
     * Before inflating views
     */
    private boolean beforeInflate() {
        if (mIntent == null) {
            return false;
        }

        mAction = mIntent.getAction();
        if (mAction == null) {
            Log.w(TAG, "Finishing: No action specified");
            return false;
        }

        if (mIntent.hasExtra(EXTRA_PREFERENCES)) {
            options = (LockPreferences) mIntent
                    .getSerializableExtra(EXTRA_PREFERENCES);
        } else {
            options = new LockPreferences(this);
        }

        mPackageName = mIntent.getStringExtra(EXTRA_PACKAGENAME);

        if (!getPackageName().equals(mPackageName)) {
            Intent i = new Intent(this, AppLockService.class);
            if (mServiceState == ServiceState.NOT_BOUND) {
                if (DEBUG_BIND)
                    Log.v(TAG, "Binding service (mServiceState="
                            + mServiceState + ")");
                mServiceState = ServiceState.BINDING;
                bindService(i, mConnection, 0);
            } else {
                if (DEBUG_BIND)
                    Log.v(TAG,
                            "Not binding service in afterInflate (mServiceState="
                                    + mServiceState + ")");
            }
        }

        if (ACTION_CREATE.equals(mAction)) {
            options.patternStealth = false;
        }

        // animations

        mLayoutParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                // Whatsapp bug fixed!
                WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_FULLSCREEN
                        | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT);
        mLayoutParams.screenOrientation = getScreenOrientation();

        return true;
    }

    private void setBackground() {
        String def = getString(R.string.pref_val_bg_default);
        String blue = getString(R.string.pref_val_bg_blue);
        String dark_blue = getString(R.string.pref_val_bg_dark_blue);
        String green = getString(R.string.pref_val_bg_green);
        String purple = getString(R.string.pref_val_bg_purple);
        String red = getString(R.string.pref_val_bg_red);
        String orange = getString(R.string.pref_val_bg_orange);
        String turquoise = getString(R.string.pref_val_bg_turquoise);
        mViewBackground.setImageBitmap(null);
        if (blue.equals(options.background)) {
            mViewBackground.setBackgroundColor(getResources().getColor(
                    R.color.flat_blue));
        } else if (dark_blue.equals(options.background)) {
            mViewBackground.setBackgroundColor(getResources().getColor(
                    R.color.flat_dark_blue));
        } else if (green.equals(options.background)) {
            mViewBackground.setBackgroundColor(getResources().getColor(
                    R.color.flat_green));
        } else if (purple.equals(options.background)) {
            mViewBackground.setBackgroundColor(getResources().getColor(
                    R.color.flat_purple));
        } else if (red.equals(options.background)) {
            mViewBackground.setBackgroundColor(getResources().getColor(
                    R.color.flat_red));
        } else if (turquoise.equals(options.background)) {
            mViewBackground.setBackgroundColor(getResources().getColor(
                    R.color.flat_turquoise));
        } else if (orange.equals(options.background)) {
            mViewBackground.setBackgroundColor(getResources().getColor(
                    R.color.flat_orange));
        } else if (def.equals(options.background) || !setBackgroundFromUri()) {
            mViewBackground
                    .setImageResource(R.drawable.locker_default_background);
        }
    }

    private boolean setBackgroundFromUri() {
        if (options.background == null)
            return false;
        Uri uri = Uri.parse(options.background);
        if (uri == null)
            return false;

        Point size = getSizeCompat(mWindowManager.getDefaultDisplay());
        try {
            final Bitmap b = decodeSampledBitmapFromUri(uri, size.x, size.y);
            if (b == null) {
                return false;
            }
            mViewBackground.setImageBitmap(b);
        } catch (FileNotFoundException e) {
            Log.w(TAG, "Error setting background");
            return false;
        }
        return true;
    }

    private void setupFirst() {
        if (options.type == LockPreferences.TYPE_PATTERN) {
            mLockPatternView.setInStealthMode(false);
            mLockPatternView.clearPattern(PATTERN_DELAY);
            mViewTitle.setText(R.string.pattern_change_tit);
            mViewMessage.setText(R.string.pattern_change_head);
            mNewPattern = null;
        } else {
            mLockPasswordView.clearPassword();
            updatePassword();

            mViewTitle.setText(R.string.password_change_tit);
            mViewMessage.setText(R.string.password_change_head);
            mNewPassword = null;
        }
        mLeftButton.setText(android.R.string.cancel);
        mRightButton.setText(R.string.button_continue);
        mLeftButtonAction = LeftButtonAction.CANCEL;
        mRightButtonAction = RightButtonAction.CONTINUE;
    }

    private void setupSecond() {
        if (options.type == LockPreferences.TYPE_PATTERN) {
            mNewPattern = mLockPatternView.getPatternString();
            if (mNewPattern.length() == 0) {
                return;
            }
            mViewMessage.setText(R.string.pattern_change_confirm);
            mLockPatternView.clearPattern();
        } else {
            mNewPassword = mLockPasswordView.getPassword();
            if (mNewPassword.length() == 0) {
                Toast.makeText(this, R.string.password_empty,
                        Toast.LENGTH_SHORT).show();
                return;
            }else if(mNewPassword.length() < 4){
                return;
            }
            mLockPasswordView.setPassword("");
            updatePassword();
            mViewMessage.setText(R.string.password_change_confirm);
        }
        mLeftButton.setText(R.string.button_back);
        mRightButton.setText(R.string.button_confirm);
        mLeftButtonAction = LeftButtonAction.BACK;
        mRightButtonAction = RightButtonAction.CONFIRM;
    }

    private boolean showPasswordView() {
        mLockView.removeAllViews();
        mLockPatternView = null;
        LayoutInflater li = LayoutInflater.from(this);

        mTextViewPassword = (TextView) li.inflate(
                R.layout.view_lock_number_textview, null);
        mLockView.addView(mTextViewPassword);
        
        View view = li.inflate(R.layout.view_lock_blank, null);
        
        mLockView.addView(view);
        
        mLockPasswordView = (PasswordView) li.inflate(
                R.layout.view_lock_number, null);
        mLockView.addView(mLockPasswordView);

        mLockPasswordView.setListener(mPasswordListener);
        if (ACTION_CREATE.equals(mAction)) {
            mLockPasswordView.setOkButtonVisibility(View.INVISIBLE);
        } else {
            mLockPasswordView.setOkButtonVisibility(View.VISIBLE);
        }

        mLockPasswordView.setTactileFeedbackEnabled(options.vibration);
        mLockPasswordView.setSwitchButtons(options.passwordSwitchButtons);
        mLockPasswordView.setVisibility(View.VISIBLE);
        options.type = LockPreferences.TYPE_PASSWORD;
        return true;
    }

    private boolean showPatternView() {

        mLockView.removeAllViews();
        mLockPasswordView = null;
        LayoutInflater li = LayoutInflater.from(this);
        li.inflate(R.layout.view_lock_pattern, mLockView, true);

        mLockPatternView = (PatternView) mLockView
                .findViewById(R.id.patternView);
        mLockPatternView.setOnPatternListener(mPatternListener);
        mLockPatternView.setSelectedBitmap(options.patternCircleResId);
        Drawable gd = getResources().getDrawable(
                R.drawable.passwordview_button_background);
        Util.setBackgroundDrawable(mLockPatternView, gd);
        mLockPatternView.setSize(options.patternSize);
        mLockPatternView.setTactileFeedbackEnabled(options.vibration);
        mLockPatternView.setInStealthMode(options.patternStealth);
        mLockPatternView.setInErrorStealthMode(options.patternErrorStealth);
        mLockPatternView.onShow();
        mLockPatternView.setVisibility(View.VISIBLE);
        options.type = LockPreferences.TYPE_PATTERN;
        return true;
    }

    /**
     * Runs {@link #beforeInflate()}, inflates the view, adds it to the window,
     * and calls {@link #afterInflate()}<br>
     * It removes any previous view if it were present
     */
    private void showView() {
        if (DEBUG_VIEW)
            Log.v(TAG, "called showView" + " (mViewState=" + mViewState + ")");
        if (mViewState == ViewState.HIDING || mViewState == ViewState.SHOWING) {
            cancelAnimations();
        }

        if (mViewState != ViewState.HIDDEN) {
            if (DEBUG_VIEW)
                Log.w(TAG, "called showView but was not hidden");
            mWindowManager.removeView(mRootView);
        }

        // Prepare everything
        beforeInflate();
        // Create the view
        mRootView = inflateRootView();
        // Show the view
        mWindowManager.addView(mRootView, mLayoutParams);
        // Do some extra stuff when the view's ready
        afterInflate();

        mViewState = ViewState.SHOWING;
        showViewAnimate();
    }

    private void showViewAnimate() {
        if (DEBUG_VIEW)
            Log.v(TAG, "called showViewAnimate" + " (mViewState=" + mViewState
                    + ")");
        if (options.showAnimationResId == 0 || options.showAnimationMillis == 0) {
            onViewShown();
            return;
        }
        mAnimShow = AnimationUtils.loadAnimation(this,
                options.showAnimationResId);
        mAnimShow.setAnimationListener(new BaseAnimationListener() {

            @Override
            public void onAnimationEnd(Animation animation) {
                onViewShown();
            }
        });
        mAnimShow.setDuration(options.showAnimationMillis);
        mAnimShow.setFillEnabled(true);
        mContainer.startAnimation(mAnimShow);
    }


    private static abstract class BaseAnimationListener implements
            AnimationListener {

        @Override
        public void onAnimationStart(Animation animation) {
        }

        @Override
        public void onAnimationEnd(Animation animation) {
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }

    }

    private void onViewShown() {

        mTimeViewShown = System.nanoTime();
        if (DEBUG_VIEW)
            Log.v(TAG, "called onViewShown" + " (mViewState=" + mViewState
                    + ")");
        mViewState = ViewState.SHOWN;
        mAnimShow = null;
    }

    /**
     * Updates the password, trimming it if necessary, also updates
     * {@link #mTextViewPassword}
     */
    private void updatePassword() {
        String pwd = mLockPasswordView.getPassword();
        if (MAX_PASSWORD_LENGTH != 0) {
            if (pwd.length() >= MAX_PASSWORD_LENGTH) {
                mLockPasswordView.setPassword(pwd.substring(0,
                        MAX_PASSWORD_LENGTH));
            }
        }
        updatePasswordTextView(mLockPasswordView.getPassword());
    }

    private void updatePasswordTextView(String newText) {
        mTextViewPassword.setText(newText);
    }
}
