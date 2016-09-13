package com.magcomm.touch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.provider.MediaStore.Audio.Media;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.Toast;

import android.view.Window;
import android.view.WindowManager;

// modified by bruce for "m"
import android.media.AudioManager;

public class TouchLetterActivity extends Activity {
	/*
	 * #define KEY_TP_C 252 #define KEY_TP_E 253
	 */
	private ImageView iv = null;
	private static final int CMD_FINASH_ACTIVITY = 1;
	private int CMD_operate_action = 0;
	private PowerManager.WakeLock wakeLock;
	private static final boolean isEableSetting = true;
        private static TouchLetterActivity instance = null;
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i("jiaAAAAA", "enter MainActivity");
		// adde by bruce for no statusbar begin
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,    
            WindowManager.LayoutParams.FLAG_FULLSCREEN); 
        requestWindowFeature(Window.FEATURE_NO_TITLE);  
        // adde by bruce for no statusbar end
        
                long t1 = System.currentTimeMillis(); // 排序前取得当前时间  
		getWindow().addFlags(
				WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                                                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
						| WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
						| WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		setContentView(R.layout.activity_main);
		iv = (ImageView) findViewById(R.id.imageView1);
/*
		iv.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent in = new Intent(TouchLetterActivity.this,
						SetPreferenceActivity.class);
				TouchLetterActivity.this.startActivity(in);
			}
		});
*/
		Intent intent = this.getIntent();// 得到用于激活它的意图
		Bundle bundle = intent.getExtras();// .getExtras()得到intent所附带的额外数据
		
		showAnimationAndgoApp(bundle);
Log.i("jiaAAAAA","onCreate------------t1="+t1);
	}

	void showAnimationAndgoApp(Bundle bundle) {
		if (bundle != null) {
			String str = bundle.getString("letter", "a");// getString()返回指定key的值

			MyAnimationDrawable mad = null;
			if (str.equals("c")) {
				mad = new MyAnimationDrawable(
						(AnimationDrawable) getResources().getDrawable(
								R.anim.ani_letter_c)) {
					@Override
					void onAnimationEnd() {
						Message message = new Message();
						message.what = CMD_FINASH_ACTIVITY;
						myHandler.sendMessage(message);
					}
				};
				CMD_operate_action = 1;
			} else if (str.equals("e")) {
				mad = new MyAnimationDrawable(
						(AnimationDrawable) getResources().getDrawable(
								R.anim.ani_letter_e)) {
					@Override
					void onAnimationEnd() {

						Message message = new Message();
						message.what = CMD_FINASH_ACTIVITY;
						myHandler.sendMessage(message);
					}
				};
				CMD_operate_action = 2;
			} else if (str.equals("m")) {
				mad = new MyAnimationDrawable(
						(AnimationDrawable) getResources().getDrawable(
								R.anim.ani_letter_m)) {
					@Override
					void onAnimationEnd() {

						Message message = new Message();
						message.what = CMD_FINASH_ACTIVITY;
						myHandler.sendMessage(message);
					}
				};
				CMD_operate_action = 3;
			} else if (str.equals("o")) {
				mad = new MyAnimationDrawable(
						(AnimationDrawable) getResources().getDrawable(
								R.anim.ani_letter_o)) {
					@Override
					void onAnimationEnd() {

						Message message = new Message();
						message.what = CMD_FINASH_ACTIVITY;
						myHandler.sendMessage(message);
					}
				};
				CMD_operate_action = 4;
            } else if (str.equals("w")) {
                mad = new MyAnimationDrawable(
                        (AnimationDrawable) getResources().getDrawable(
                                R.anim.ani_letter_w)) {
                    @Override
                    void onAnimationEnd() {

                        Message message = new Message();
                        message.what = CMD_FINASH_ACTIVITY;
                        myHandler.sendMessage(message);
                    }
                };
                CMD_operate_action = 5;
            } else if (str.equals("up")) {
                mad = new MyAnimationDrawable(
                        (AnimationDrawable) getResources().getDrawable(
                                R.anim.ani_letter_up)) {
                    @Override
                    void onAnimationEnd() {

                        Message message = new Message();
                        message.what = CMD_FINASH_ACTIVITY;
                        myHandler.sendMessage(message);
                    }
                };
                CMD_operate_action = 6;
            } else if (str.equals("down")) {
                mad = new MyAnimationDrawable(
                        (AnimationDrawable) getResources().getDrawable(
                                R.anim.ani_letter_down)) {
                    @Override
                    void onAnimationEnd() {

                        Message message = new Message();
                        message.what = CMD_FINASH_ACTIVITY;
                        myHandler.sendMessage(message);
                    }
                };
                CMD_operate_action = 7;
            } else if (str.equals("left")) {
                mad = new MyAnimationDrawable(
                        (AnimationDrawable) getResources().getDrawable(
                                R.anim.ani_letter_left)) {
                    @Override
                    void onAnimationEnd() {

                        Message message = new Message();
                        message.what = CMD_FINASH_ACTIVITY;
                        myHandler.sendMessage(message);
                    }
                };
                CMD_operate_action = 8;
            } else if (str.equals("right")) {
                mad = new MyAnimationDrawable(
                        (AnimationDrawable) getResources().getDrawable(
                                R.anim.ani_letter_right)) {
                    @Override
                    void onAnimationEnd() {

                        Message message = new Message();
                        message.what = CMD_FINASH_ACTIVITY;
                        myHandler.sendMessage(message);
                    }
                };
                CMD_operate_action = 9;
			} else {
				mad = new MyAnimationDrawable(
						(AnimationDrawable) getResources().getDrawable(
								R.anim.ani_letter_o)) {
					@Override
					void onAnimationEnd() {

						Message message = new Message();
						message.what = CMD_FINASH_ACTIVITY;
						myHandler.sendMessage(message);
					}
				};

			}

			// 把这个动画“赐福”给某个ImageView
			iv.setImageDrawable(mad);
			// 开始吧
			mad.start();

		}
	}
	private Handler myHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case CMD_FINASH_ACTIVITY:
				String str = String.valueOf(msg.obj);

				if (CMD_operate_action == 1) {
					Log.i("jiaAAAAA", "enter camera");
					if (isEableSetting) {
						run_op_action(1);
					} else {
						StartOpenDial();
					}

				} else if (CMD_operate_action == 2) {
					Log.i("jiaAAAAA", "open baidu");
					// StartOpenMessage();
					if (isEableSetting) {
						run_op_action(2);
					} else {
						StartOpenBrowser();
					}

				} else if (CMD_operate_action == 3) {
					Log.i("jiaAAAAA", "open baidu");
					if (isEableSetting) {
						run_op_action(3);
					} else {
						startPlayMusic();
					}

				} else if (CMD_operate_action == 4) {
					Log.i("jiaAAAAA", "open baidu");
					// StartOpenMessage();
					if (isEableSetting) {
						run_op_action(4);
					} else {
						StartOpenMessage();
					}
				} else if (CMD_operate_action == 5) {
                    if (isEableSetting) {
                        run_op_action(CMD_operate_action);
                    } else {
                        StartOpenPhonebook();
                    }
                } else if (CMD_operate_action == 6) {
                    if (isEableSetting) {
                        run_op_action(CMD_operate_action);
                    } else {
                        StartUNlock();
                    }
                } else if (CMD_operate_action == 7) {
                    if (isEableSetting) {
                        run_op_action(CMD_operate_action);
                    } else {
                    	StartOpenCamera();
                    }
                } else if (CMD_operate_action == 8) {
                    startPlayPreviousMusic();
                } else if (CMD_operate_action == 9) {
                    startPlayNextMusic();
                }
				
				TouchLetterActivity.this.finish();
				break;

			default:
				break;
			}

		}
	};

    private void startExtraApp(String packageName) {
        final PackageManager pm = getPackageManager();
            Intent i = pm.getLaunchIntentForPackage(packageName);
            startActivity(i);
    }

	public void op_action(String s) {
		//hejianfeng@20141020 add start for  应用不存在
        try {
        if (s.equals(getResources().getString(R.string.music_index))) {
            startPlayMusic();
        } else if (s.equals(getResources().getString(R.string.camera_index))) {
            this.StartOpenCamera();
        } else if (s.equals(getResources().getString(R.string.brower_index))) {
            this.StartOpenBrowser();
        } else if (s.equals(getResources().getString(R.string.pause_index))) {
            this.startPauseMusic();
        } else if (s.equals(getResources().getString(R.string.stop_index))) {
            this.startStopMusic();
        } else if (s.equals(getResources().getString(R.string.previous_index))) {
            this.startPlayPreviousMusic();
        } else if (s.equals(getResources().getString(R.string.next_index))) {
            this.startPlayNextMusic();
        } else if (s.equals(getResources().getString(R.string.phonebook_index))) {
            this.StartOpenPhonebook();
        } else if (s.equals(getResources().getString(R.string.sms_index))) {
            this.StartOpenMessage();
        } else if (s.equals(getResources().getString(R.string.setting_index))) {
            this.StartOpenSetting();
        } else if (s.equals(getResources().getString(R.string.record_index))) {
            this.StartOpenRecord();
        } else if (s.equals(getResources().getString(R.string.wechat_index))) {
            this.StartOpenWechat();
        } else if (s.equals(getResources().getString(R.string.unlock_index))) {
            this.StartUNlock();
        } else if (s.equals(getResources().getString(R.string.dial_index))) {
            this.StartOpenDial();
        } else if (s.equals(getResources().getString(R.string.map_index))) {
            this.StartOpenMap();
        } else if (s.equals(getResources().getString(R.string.QQ_index))) {
            this.StartQQ();
        } else {
            startExtraApp(s);
        }
        } catch (Exception e) {
            Toast.makeText(this, getResources().getString(R.string.app_not_exist), Toast.LENGTH_LONG).show();
        }
        //hejianfeng@20141020 add end for  应用不存在
	}

	private static final String c_preference = "c_list_preferenc";
	private static final String e_preference = "e_list_preferenc";
	private static final String m_preference = "m_list_preferenc";
	private static final String o_preference = "o_list_preferenc";
    private static final String w_preference = "w_list_preferenc";
    private static final String up_preference = "up_list_preferenc";
    private static final String down_preference = "down_list_preferenc";

	public void run_op_action(int key) {
        SharedPreferences mySharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        Log.i("bruce_nan", "run_op_action_nfl: key = " + key);
		switch (key) {
		case 1:
			String m_c_preference = mySharedPreferences.getString(c_preference,
					getResources().getString(R.string.dial_index));

			op_action(m_c_preference);
			break;
		case 2:
			String m_e_preference = mySharedPreferences.getString(e_preference,
					getResources().getString(R.string.brower_index));
			op_action(m_e_preference);
			break;
		case 3:
			String m_m_preference = mySharedPreferences.getString(m_preference,
					getResources().getString(R.string.music_index));
			op_action(m_m_preference);
			break;
		case 4:
			String m_o_preference = mySharedPreferences.getString(o_preference,
					getResources().getString(R.string.sms_index));
			op_action(m_o_preference);
			break;
        case 5:
            String m_w_preference = mySharedPreferences.getString(w_preference,
                    getResources().getString(R.string.phonebook_index));
            op_action(m_w_preference);
            break;
        case 6:
            String m_up_preference = mySharedPreferences.getString(up_preference,
                    getResources().getString(R.string.unlock_index));
            op_action(m_up_preference);
            break;
        case 7:
            String m_down_preference = mySharedPreferences.getString(down_preference,
                    getResources().getString(R.string.camera_index));
            op_action(m_down_preference);
            break;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
        instance = null;
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();

	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub

		super.onStart();
		Intent intent = new Intent("intent.statusbar.update");
		intent.putExtra("cooeelock", false);
		sendBroadcast(intent);

	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}

	@Override
	public void onWindowFocusChanged(boolean hasWindowFocus) {
		// TODO Auto-generated method stub
		super.onWindowFocusChanged(hasWindowFocus);

		if (hasWindowFocus) {
			Intent intent = new Intent("intent.statusbar.update");
			intent.putExtra("cooeelock", false);
			sendBroadcast(intent);
		} else {
			Intent intent = new Intent("intent.statusbar.update");
			intent.putExtra("cooeelock", false);
			sendBroadcast(intent);
		}
long t2 = System.currentTimeMillis(); // 排序前取得当前时间  
Log.i("jiaAAAAA", "onWindowFocusChanged---------------t2="+t2);
	}

	public void connectMediaService() {

		Intent intent = new Intent();
		intent.setClassName("com.android.music",
				"com.android.music.MediaPlaybackService");
		// 绑定连接远程服务
		startService(intent);
	}

	private void startPlayNextMusic() {
		connectMediaService();
		this.myHandler.postDelayed(new Runnable(){
			@Override
			public void run() {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				intent.setAction("com.android.music.musicservicecommand");
				intent.putExtra("command", "next");
				sendBroadcast(intent);
				myHandler.removeCallbacks(this);
			}
			
		}, 1000);
	}

	private void startPlayPreviousMusic() {
		connectMediaService();
                this.myHandler.postDelayed(new Runnable(){
			@Override
			public void run() {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				intent.setAction("com.android.music.musicservicecommand");
				intent.putExtra("command", "previous");
				sendBroadcast(intent);
				myHandler.removeCallbacks(this);
			}
			
		}, 1000);
	}

	private void startPlayMusic() {
	    // modified by bruce for "m" begin
	    //Intent intent = new Intent();
        //intent.setClassName("com.android.music","com.android.music.MusicBrowserActivity");
        //startActivity(intent);
        final AudioManager audioManager = (AudioManager)TouchLetterActivity.this.getSystemService(Context.AUDIO_SERVICE);
        boolean isMusicActive = audioManager.isMusicActive();
        Log.i("bruce_nan", "startPlayMusic_nfl: isMusicActive = " + isMusicActive);
        if (isMusicActive){
            startPauseMusic();
        }else {
            connectMediaService();
            this.myHandler.postDelayed(new Runnable(){
    			@Override
    			public void run() {
    				// TODO Auto-generated method stub
    				Intent intent = new Intent();
    				intent.setAction("com.android.music.musicservicecommand");
    				intent.putExtra("command", "play");
    				sendBroadcast(intent);
    				myHandler.removeCallbacks(this);
    			}
    			
    		}, 1000);
        }
        // modified by bruce for "m" end	
	}

	private void startPauseMusic() {
		connectMediaService();
                this.myHandler.postDelayed(new Runnable(){
			@Override
			public void run() {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				intent.setAction("com.android.music.musicservicecommand");
				intent.putExtra("command", "pause");
				sendBroadcast(intent);
				myHandler.removeCallbacks(this);
			}
			
		}, 1000);
	}

	private void startStopMusic() {
		connectMediaService();
                this.myHandler.postDelayed(new Runnable(){
			@Override
			public void run() {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				intent.setAction("com.android.music.musicservicecommand");
				intent.putExtra("command", "stop");
				sendBroadcast(intent);
				myHandler.removeCallbacks(this);
			}
			
		}, 1000);
	}

	

	private void StartOpenCamera() {
		Intent intent = new Intent();
        intent.setClassName("com.mediatek.camera","com.android.camera.CameraActivity");
        startActivity(intent);
	/*
		Intent i = new Intent(Intent.ACTION_CAMERA_BUTTON, null);
		sendBroadcast(i);
    */
		/*
		 * Intent mIntent = new Intent(); ComponentName comp = new
		 * ComponentName("com.android.camera","com.android.camera.Camera");
		 * mIntent.setComponent(comp);
		 * mIntent.setAction("android.intent.action.VIEW");
		 * startActivity(mIntent);
		 */
	}

	private void StartOpenBrowser() {
        Log.i("bruce_nan", "StartOpenBrowser_nfl");
		//Uri uri = Uri.parse("http://www.google.com");
		Intent intent = new Intent();
		// intent.setClassName("com.baidu.browser.apps","com.baidu.browser.apps.BrowserActivity");
		//intent.setAction(Intent.ACTION_VIEW);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		//intent.setData(uri);
                intent.setClassName("com.android.browser","com.android.browser.BrowserActivity");
		startActivity(intent);

	}

	private void StartOpenPhonebook() {
	    Log.i("bruce_nan", "StartOpenPhonebook_nfl");
		Intent intent = new Intent();
        intent.setClassName("com.android.contacts","com.android.contacts.activities.PeopleActivity");
        startActivity(intent);
		//i.setAction(Intent.ACTION_GET_CONTENT);
		//i.setType("vnd.android.cursor.item/phone");
		//startActivityForResult(i, RESULT_OK);
		/*
		 * Uri uri = Uri.parse("content://contacts/people"); Intent it = new
		 * Intent(Intent.ACTION_PICK, uri); startActivityForResult(it,
		 * REQUEST_TEXT);
		 */
	}

	private void StartOpenMessage() {
		/*
		 * Uri uri = Uri.parse("smsto:"); Intent intent = new
		 * Intent(Intent.ACTION_SENDTO, uri); //intent.putExtra("sms_body",
		 * "Hello"); startActivity(intent);
		 */
		//Intent it = new Intent(Intent.ACTION_VIEW);
		// it.putExtra("sms_body", "The SMS text");
		//it.setType("vnd.android-dir/mms-sms");
		//startActivity(it);
		Intent intent = new Intent();
        intent.setClassName("com.android.mms","com.android.mms.ui.BootActivity");
        startActivity(intent);
	}

	private void StartOpenMediaMessage() {
		Intent intent = new Intent(Intent.ACTION_SEND);
		// intent.putExtra("sms_body", "Hello");
		Uri uri = Uri.parse("content://media/external/images/media/23");
		intent.putExtra(Intent.EXTRA_STREAM, uri);
		intent.setType("image/png");
		startActivity(intent);
	}

	private void StartOpenDial() {
		Uri uri = Uri.parse("tel:");
		Intent intent = new Intent(Intent.ACTION_DIAL, uri);
		startActivity(intent);
	}

	private void StartOpenAudio() {

		Intent intent = new Intent(Intent.ACTION_VIEW);
		Uri uri = Uri.parse("file:///sdcard/foo.mp3");
		intent.setDataAndType(uri, "audio/mp3");
		startActivity(intent);

		/*
		 * Uri uri = Uri.withAppendedPath(
		 * MediaStore.Audio.Media.INTERNAL_CONTENT_URI, "1"); Intent intent =
		 * new Intent(Intent.ACTION_VIEW, uri); startActivity(intent);
		 */
	}

	private void StartOpenSetting() {
		//Intent intent = new Intent(
		//		android.provider.Settings.ACTION_WIRELESS_SETTINGS);
        /*
		Intent intent = new Intent(
						android.provider.Settings.ACTION_SETTINGS);
		startActivityForResult(intent, 0);
        */
        Intent intent = new Intent();
        intent.setClassName("com.android.settings","com.android.settings.Settings");
        startActivity(intent);
	}

    private void StartOpenWechat() {
        Intent intent = new Intent();
        intent.setClassName("com.tencent.mm","com.tencent.mm.ui.LauncherUI");
        startActivity(intent);
    }
	
    
    private void StartUNlock() {
        //nothing
    }
    
    private void StartQQ() {
        Intent intent = new Intent();
        intent.setClassName("com.tencent.qqlite","com.tencent.qqlite.activity.SplashActivity");
        startActivity(intent);
    }
    
	private void StartOpenSearch() {
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_WEB_SEARCH);
		intent.putExtra(SearchManager.QUERY, "searchString");
		startActivity(intent);
	}

	private void StartOpenMap() {
		Uri uri = Uri.parse("geo:38.899533,-77.036476");
		Intent it = new Intent(Intent.ACTION_VIEW, uri);
		startActivity(it);
	}

	private void StartOpenPicture() {
		Intent i = new Intent();
		i.setType("image/*");
		i.setAction(Intent.ACTION_GET_CONTENT);
		startActivityForResult(i, 11);
	}

	private void StartOpenRecord() {
		Intent mi = new Intent(Media.RECORD_SOUND_ACTION);
		startActivity(mi);
	}

	private void StartOpenCall() {
		Uri uri = Uri.parse("tel:0800000123");
		Intent it = new Intent(Intent.ACTION_CALL, uri);
		startActivity(it);
		// 用這個，要在 AndroidManifest.xml 中，加上
		// <uses-permission id="android.permission.CALL_PHONE" />
	}

	private void StartOpenShare() {
		Intent it = new Intent(Intent.ACTION_SEND);
		it.putExtra(Intent.EXTRA_TEXT, "The email subject text");
		it.setType("text/plain");
		startActivity(Intent.createChooser(it, "Choose Email Client"));
	}

}

