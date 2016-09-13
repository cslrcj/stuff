package com.magcomm.touch;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
//import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.view.View;
import android.util.Log;
import android.provider.Settings;
import android.preference.PreferenceCategory;
import android.preference.PreferenceActivity;

// add by bruce for vivo style
import android.widget.ImageButton;
import android.widget.TextView;

@SuppressLint("NewApi")
public class BodyFeelingPrefsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {
    private static final String bodyfeeling_preference = "body_feeling";
    private static final String bodyfeeling_category_preference = "bodyfeeling_category_key";
    private static final String free_screenshots_preference = "free_screenshots_key";
    //private static final String turn_to_mute_preference = "turn_to_mute_key";
    //private static final String swing_answer_preference = "swing_answer_key";
    private static SwitchPreference mScreenonSetting;
    private static CheckBoxPreference free_screenshots_connectionPref; 
    //private static CheckBoxPreference turn_to_mute_connectionPref; 
    //private static CheckBoxPreference swing_answer_connectionPref; 
    private PreferenceCategory mScreenonCategory;

    // add by bruce for vivo style
    private TextView tvTitle = null;
    private ImageButton imgBnReturn;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        
        super.onCreate(savedInstanceState);

        initVivo();

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.body_feeling_preferences);
        
        SharedPreferences mySharedPreferences = PreferenceManager.getDefaultSharedPreferences(BodyFeelingPrefsActivity.this);
        mScreenonSetting = (SwitchPreference)findPreference(bodyfeeling_preference);
        mScreenonCategory = (PreferenceCategory) findPreference(bodyfeeling_category_preference);
        //turn_to_mute_connectionPref = (CheckBoxPreference)findPreference(turn_to_mute_preference);
        //swing_answer_connectionPref = (CheckBoxPreference)findPreference(swing_answer_preference);
        free_screenshots_connectionPref = (CheckBoxPreference)findPreference(free_screenshots_preference);
       
        chooseEnable(mySharedPreferences);
    }

    private void chooseEnable(SharedPreferences mySharedPreferences){        
        boolean screenon_enable = Settings.System.getInt((BodyFeelingPrefsActivity.this).getContentResolver(),
                Settings.System.BODY_FEELING_SETTING,0) == 1 ? true:false;
        Log.i("bruce_nan", "chooseEnable_nfl: screenon_enable = " + screenon_enable);
        //if (!mySharedPreferences.getBoolean(ps_preference, true)){
        if (!screenon_enable){
            mScreenonSetting.setChecked(false);
            mScreenonSetting.setSelectable(true);
            //turn_to_mute_connectionPref.setEnabled(false);
            //turn_to_mute_connectionPref.setSelectable(false);
            //swing_answer_connectionPref.setEnabled(false);
            //swing_answer_connectionPref.setSelectable(false);
            free_screenshots_connectionPref.setEnabled(false);
            free_screenshots_connectionPref.setSelectable(false);
            Settings.System.putInt((BodyFeelingPrefsActivity.this).getContentResolver(), Settings.System.BODY_FEELING_SETTING, 0);
        }else{
            mScreenonSetting.setChecked(true);
            mScreenonSetting.setSelectable(true);
            //turn_to_mute_connectionPref.setEnabled(true);
            //turn_to_mute_connectionPref.setSelectable(true);
            //swing_answer_connectionPref.setEnabled(true);
            //swing_answer_connectionPref.setSelectable(true);
            free_screenshots_connectionPref.setEnabled(true);
            free_screenshots_connectionPref.setSelectable(true);
            Settings.System.putInt((BodyFeelingPrefsActivity.this).getContentResolver(), Settings.System.BODY_FEELING_SETTING, 1);
        }

        /*boolean isTurntoMuteEnable = Settings.System.getInt((BodyFeelingPrefsActivity.this).getContentResolver(),
            Settings.System.CALL_RING_FLIP_SLIENT,0) == 1;
        boolean isSwingAnswerEnable = Settings.System.getInt((BodyFeelingPrefsActivity.this).getContentResolver(),
            Settings.System.CALL_RING_SHAKE_TO_ANSWER,0) == 1;
        turn_to_mute_connectionPref.setChecked(isTurntoMuteEnable);
        swing_answer_connectionPref.setChecked(isSwingAnswerEnable);*/
        boolean isFreescreenshotsEnable = Settings.System.getInt((BodyFeelingPrefsActivity.this).getContentResolver(),
            Settings.System.FREE_SCREENSHOTS_SETTING,0) == 1;
        free_screenshots_connectionPref.setChecked(isFreescreenshotsEnable);

    }
    
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
       
        Preference connectionPref = findPreference(key);
        if (key.equals(bodyfeeling_preference)) {
            if (sharedPreferences.getBoolean(key, true)) {
                connectionPref.setSummary(getResources().getString(R.string.open));
                Settings.System.putInt((BodyFeelingPrefsActivity.this).getContentResolver(), 
                    Settings.System.BODY_FEELING_SETTING, 1);//hucheng add BODY_FEELING_SETTING
            } else {
                connectionPref.setSummary(getResources().getString(R.string.close));
                Settings.System.putInt((BodyFeelingPrefsActivity.this).getContentResolver(), 
                    Settings.System.BODY_FEELING_SETTING, 0);
            }
        } /*else if (key.equals(turn_to_mute_preference)) {//turn_to_mute
            if (sharedPreferences.getBoolean(key, true)){
                Settings.System.putInt((BodyFeelingPrefsActivity.this).getContentResolver(), 
                    Settings.System.CALL_RING_FLIP_SLIENT, 1);
            } else {
                Settings.System.putInt((BodyFeelingPrefsActivity.this).getContentResolver(), 
                    Settings.System.CALL_RING_FLIP_SLIENT, 0);
            }
        } else if (key.equals(swing_answer_preference)) {//swing_answer
            if (sharedPreferences.getBoolean(key, true)){
                Settings.System.putInt((BodyFeelingPrefsActivity.this).getContentResolver(), 
                    Settings.System.CALL_RING_SHAKE_TO_ANSWER, 1);
            } else {
                Settings.System.putInt((BodyFeelingPrefsActivity.this).getContentResolver(), 
                    Settings.System.CALL_RING_SHAKE_TO_ANSWER, 0);
            }
        } */
        else if(key.equals(free_screenshots_preference)){//free_screenshots
             if (sharedPreferences.getBoolean(key, true)){
                Settings.System.putInt((BodyFeelingPrefsActivity.this).getContentResolver(), 
                    Settings.System.FREE_SCREENSHOTS_SETTING, 1);
            } else {
                Settings.System.putInt((BodyFeelingPrefsActivity.this).getContentResolver(), 
                    Settings.System.FREE_SCREENSHOTS_SETTING, 0);
            }
        }
        chooseEnable(sharedPreferences);
    }

	@Override
	public void onResume() {
		super.onResume();
		getPreferenceManager().getSharedPreferences()
				.registerOnSharedPreferenceChangeListener(this);

	}

	@Override
	public void onPause() {
		getPreferenceManager().getSharedPreferences()
				.unregisterOnSharedPreferenceChangeListener(this);
		super.onPause();
	}

	// add by bruce for vivo style begin
    private void initVivo(){
        setContentView(R.layout.touchletter_preference_activity);
        tvTitle = (TextView)findViewById(R.id.titleBg);
        tvTitle.setText(R.string.body_feeling_setting);
        tvTitle.setOnClickListener(new android.view.View.OnClickListener(){
            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                BodyFeelingPrefsActivity.this.finish(); 
            }
        });

        imgBnReturn = (ImageButton)findViewById(R.id.imgLeft);
        imgBnReturn.setVisibility(View.VISIBLE);
        imgBnReturn.setOnClickListener(new android.view.View.OnClickListener(){
            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                BodyFeelingPrefsActivity.this.finish(); 
            }
        });
    }
    // add by bruce for vivo style end
}



