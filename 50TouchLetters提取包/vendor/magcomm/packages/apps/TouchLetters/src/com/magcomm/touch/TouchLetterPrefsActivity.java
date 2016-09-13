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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.provider.Settings;
import android.preference.PreferenceActivity;

// added by bruce for write file begin
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
//import org.apache.http.util.EncodingUtils;
// added by bruce for write file end

// add by bruce for vivo style
import android.widget.ImageButton;
import android.widget.TextView;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import java.util.List;
import android.content.Intent;

@SuppressLint("NewApi")
public class TouchLetterPrefsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {
    private static final String hx_preference = "letter_touch";
    
    private static final String c_preference = "c_list_preferenc";
    private static final String e_preference = "e_list_preferenc";
    private static final String m_preference = "m_list_preferenc";
    private static final String o_preference = "o_list_preferenc";
    private static final String w_preference = "w_list_preferenc";
    private static final String up_preference = "up_list_preferenc";
    private static final String down_preference = "down_list_preferenc";
    private static final String lr_preference = "switch_music_preferenc";
    private static final String lightScreen_preference = "double";

    private static SwitchPreference mHX_SETTING;
    private static Preference c_connectionPref;
    private static Preference e_connectionPref;
    private static Preference m_connectionPref;
    private static Preference o_connectionPref;
    private static Preference w_connectionPref;
    private static Preference up_connectionPref;
    private static Preference down_connectionPref;
    private static CheckBoxPreference lr_connectionPref;
    private static CheckBoxPreference lightScreen_connectionPref;

    // add by bruce for vivo style
    private TextView tvTitle = null;
    private ImageButton imgBnReturn;

    private CharSequence[] mEntries;
    private CharSequence[] mEntryValues;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

        initVivo(); // added by bruce for vivo UI
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.touchletter_preferences);

        SharedPreferences mySharedPreferences = PreferenceManager.getDefaultSharedPreferences(TouchLetterPrefsActivity.this);
        String m_c_preference = mySharedPreferences.getString(c_preference, getResources().getString(R.string.camera));
        String m_e_preference = mySharedPreferences.getString(e_preference, getResources().getString(R.string.brower));
        String m_m_preference = mySharedPreferences.getString(m_preference, getResources().getString(R.string.music));
        String m_o_preference = mySharedPreferences.getString(o_preference, getResources().getString(R.string.sms));
        String m_w_preference = mySharedPreferences.getString(w_preference, getResources().getString(R.string.phonebook));
        String m_up_preference = mySharedPreferences.getString(up_preference, getResources().getString(R.string.unlock));
        String m_down_preference = mySharedPreferences.getString(down_preference, getResources().getString(R.string.dial));
        c_connectionPref = findPreference(c_preference);
        e_connectionPref = findPreference(e_preference);
        m_connectionPref = findPreference(m_preference);
        o_connectionPref = findPreference(o_preference);
        w_connectionPref = findPreference(w_preference);
        up_connectionPref = findPreference(up_preference);
        down_connectionPref = findPreference(down_preference);
        lr_connectionPref = (CheckBoxPreference)findPreference(lr_preference);
        lightScreen_connectionPref = (CheckBoxPreference)findPreference(lightScreen_preference);
        mHX_SETTING = (SwitchPreference)findPreference(hx_preference);

        Log.d("bruce_nan", "TouchLetterPrefsActivity_onCreate_nfl");
        chooseEnable(mySharedPreferences);

        updateFlagFile(); // added by bruce for update flag file
    }

    // added by bruce for update flag file begin
    private void updateFlagFile(){
        Log.d("bruce_nan", "updateFlagFile_nfl_01");
    	int bitFlags = 0x0000;
    	boolean touchletter_enable = false;
    	boolean touch_double_enable = false;
    	boolean touch_switch_music_enable = false;
    	
    	touchletter_enable = Settings.System.getInt((TouchLetterPrefsActivity.this).getContentResolver(),
    	    Settings.System.TOUCHLETTER_ONOFF, 0)  == 1 ? true:false;
    	if(touchletter_enable){
    		bitFlags |= 0x0001; // total switcher
    		bitFlags |= 0x0200; // draw up
    		bitFlags |= 0x0100; // draw down
    		bitFlags |= 0x0020; // draw c
    		bitFlags |= 0x0010; // draw e
    		bitFlags |= 0x0008; // draw w
    		bitFlags |= 0x0004; // draw m
    		bitFlags |= 0x0002; // draw o

        	touch_double_enable = Settings.System.getInt((TouchLetterPrefsActivity.this).getContentResolver(), 
        	    Settings.System.DOUBLE_SCREEN_ONOFF, 0) == 1 ? true:false;
        	if (touch_double_enable){
        	    bitFlags |= 0x0400; // double click
        	}

        	touch_switch_music_enable = Settings.System.getInt((TouchLetterPrefsActivity.this).getContentResolver(), 
        	    Settings.System.TOUCHSWITCH_MUSIC_ONOFF, 0) == 1 ? true:false;
        	if (touch_switch_music_enable){
        	    bitFlags |= 0x0040; // draw left
        	    bitFlags |= 0x0080; // draw right
        	}
    	}else {
    	    bitFlags = 0x0000;
    	}
    	
        String data = Integer.toBinaryString(bitFlags);
        while(data.length() < 11){
    		data = "0" + data; //11 codes, easy read for Driver.
    	}
    	MyFile.switchTouchLetter(TouchLetterPrefsActivity.this, data);
    }
    // added by bruce for update flag file end

    private void chooseEnable(SharedPreferences mySharedPreferences){        
        boolean touchletter_enable = Settings.System.getInt((TouchLetterPrefsActivity.this).getContentResolver(),
    	    Settings.System.TOUCHLETTER_ONOFF, 0) == 1 ? true:false;
    	boolean touch_double_enable = Settings.System.getInt((TouchLetterPrefsActivity.this).getContentResolver(), 
        	    Settings.System.DOUBLE_SCREEN_ONOFF, 0) == 1 ? true:false;
        boolean touch_switch_music_enable = Settings.System.getInt((TouchLetterPrefsActivity.this).getContentResolver(), 
        	    Settings.System.TOUCHSWITCH_MUSIC_ONOFF, 0) == 1 ? true:false;	    
        Log.d("bruce_nan", "chooseEnable_nfl_01: touchletter_enable = " + touchletter_enable 
            + "; touch_double_enable = " + touch_double_enable
            + "; touch_switch_music_enable = " + touch_switch_music_enable);
        //if (!mySharedPreferences.getBoolean(hx_preference, true)){
        if (!touchletter_enable){
            mHX_SETTING.setChecked(false);
            mHX_SETTING.setSelectable(true);
            c_connectionPref.setEnabled(false);
            e_connectionPref.setEnabled(false);
            m_connectionPref.setEnabled(false);
            o_connectionPref.setEnabled(false);
            w_connectionPref.setEnabled(false);
            up_connectionPref.setEnabled(false);
            down_connectionPref.setEnabled(false);            
            lr_connectionPref.setEnabled(false);
            lightScreen_connectionPref.setEnabled(false);
            c_connectionPref.setSelectable(false);
            e_connectionPref.setSelectable(false);
            m_connectionPref.setSelectable(false);
            o_connectionPref.setSelectable(false);
            w_connectionPref.setSelectable(false);
            up_connectionPref.setSelectable(false);
            down_connectionPref.setSelectable(false);
            lr_connectionPref.setSelectable(false);
            lightScreen_connectionPref.setSelectable(false);
        }else{
            mHX_SETTING.setChecked(true);
            mHX_SETTING.setSelectable(true);
            c_connectionPref.setEnabled(true);
            e_connectionPref.setEnabled(true);
            m_connectionPref.setEnabled(true);
            o_connectionPref.setEnabled(true);
            w_connectionPref.setEnabled(true);
            up_connectionPref.setEnabled(true);
            down_connectionPref.setEnabled(true);
            lr_connectionPref.setEnabled(true);
            lightScreen_connectionPref.setEnabled(true);
            c_connectionPref.setSelectable(true);
            e_connectionPref.setSelectable(true);
            m_connectionPref.setSelectable(true);
            o_connectionPref.setSelectable(true);
            w_connectionPref.setSelectable(true);
            up_connectionPref.setSelectable(true);
            down_connectionPref.setSelectable(true);
            lr_connectionPref.setSelectable(true);
            lightScreen_connectionPref.setSelectable(true);
            lr_connectionPref.setChecked(touch_switch_music_enable);
            lightScreen_connectionPref.setChecked(touch_double_enable);
        }       
    }
    
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // TODO Auto-generated method stub
        // Set summary to be the user-description for the selected value
        // if(!key.equals(this.getActivity().PRF_CHECK))
        Preference connectionPref = findPreference(key);
        if (key.equals(hx_preference)) {
            Log.i("bruce_nan", "onSharedPreferenceChanged_nfl_01");
            if (sharedPreferences.getBoolean(key, true)) {
                connectionPref.setSummary(getResources().getString(R.string.open));
                Settings.System.putInt((TouchLetterPrefsActivity.this).getContentResolver(), 
                Settings.System.TOUCHLETTER_ONOFF, 1);
                MyFile.OpenTouchletter(TouchLetterPrefsActivity.this);
            } else {
                connectionPref.setSummary(getResources().getString(R.string.close));
                Settings.System.putInt((TouchLetterPrefsActivity.this).getContentResolver(), 
                Settings.System.TOUCHLETTER_ONOFF, 0);
                MyFile.CloseTouchletter(TouchLetterPrefsActivity.this);
            }

            updateFlagFile();
        } else if (key.equals(lr_preference)) {
            Log.i("bruce_nan", "onSharedPreferenceChanged_nfl_02");
            if (sharedPreferences.getBoolean(key, true)){
                Settings.System.putInt((TouchLetterPrefsActivity.this).getContentResolver(), Settings.System.TOUCHSWITCH_MUSIC_ONOFF, 1);
            }else {
                Settings.System.putInt((TouchLetterPrefsActivity.this).getContentResolver(), Settings.System.TOUCHSWITCH_MUSIC_ONOFF, 0);
            }
            updateFlagFile();
        } else if (key.equals(lightScreen_preference)) {
            Log.i("bruce_nan", "onSharedPreferenceChanged_nfl_03");
            if (sharedPreferences.getBoolean(key, true)){
                Settings.System.putInt((TouchLetterPrefsActivity.this).getContentResolver(), Settings.System.DOUBLE_SCREEN_ONOFF, 1);
            }else {
                Settings.System.putInt((TouchLetterPrefsActivity.this).getContentResolver(), Settings.System.DOUBLE_SCREEN_ONOFF, 0);
            }

            updateFlagFile();
        }else {
            //connectionPref.setSummary(sharedPreferences.getString(key, ""));
			connectionPref.setSummary(getSummary(sharedPreferences.getString(key, "")));
        }
        chooseEnable(sharedPreferences);
    }

    private String getSummary(String values){
        String[] items = getResources().getStringArray(R.array.pref_font_types);
        String[] itemsvalues = getResources().getStringArray(R.array.pref_font_types_values);
        int index = -1;
        for (int i=0; i< itemsvalues.length; i++) {
            if (itemsvalues[i].equals(values)){
                index = i;
                break;
            }
        }
        if(index != -1){
            return items[index];
        }else{
            return values;
        }
    }

    private int findIndexOfValue(String value) {
        if (value != null && mEntryValues != null) {
            for (int i = mEntryValues.length - 1; i >= 0; i--) {
                if (mEntryValues[i].equals(value)) {
                    return i;
                }
            }
        }
        return -1;
    }

    private void updateSummary(){
        mEntries = getResources().getStringArray(R.array.pref_font_types);
        mEntryValues = getResources().getStringArray(R.array.pref_font_types_values);
        String[] keys = {c_preference, e_preference, m_preference, o_preference, w_preference, up_preference, down_preference};
        Preference[] prefs = {c_connectionPref, e_connectionPref, m_connectionPref, o_connectionPref, w_connectionPref, up_connectionPref, down_connectionPref};
        int indexOfValue = -1;
        
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(TouchLetterPrefsActivity.this);
        for (int index = 0; index < keys.length; index++) 
        {
            indexOfValue = findIndexOfValue(sharedPreferences.getString(keys[index], ""));
            if (indexOfValue >= 0) {
    			String key = String.valueOf(mEntries[indexOfValue]);
    			if (null != key) {
    				prefs[index].setSummary(key);
    			}
            }else{
                Log.e("bruce_nan","NewListPreference_updateSummary_nfl_02");
                final PackageManager pm = TouchLetterPrefsActivity.this.getPackageManager();
                List<PackageInfo> packs = pm.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);
                for (PackageInfo pi : packs) {
                    Intent i = pm.getLaunchIntentForPackage(pi.packageName); 
                    if ((i != null) && pi.packageName.equals(sharedPreferences.getString(keys[index], ""))){
                        prefs[index].setSummary(pi.applicationInfo.loadLabel(pm));
                        break;
                    }
                }
            }
        }
    }
    
	@Override
	public void onResume() {
		super.onResume();
		Log.i("bruce_nan", "TouchLetterPrefsActivity_onResume");
		getPreferenceManager().getSharedPreferences()
				.registerOnSharedPreferenceChangeListener(this);
        updateSummary();
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
        tvTitle.setText(R.string.hx_setting);
        tvTitle.setOnClickListener(new android.view.View.OnClickListener(){
            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                TouchLetterPrefsActivity.this.finish(); 
            }
        });

        imgBnReturn = (ImageButton)findViewById(R.id.imgLeft);
        imgBnReturn.setVisibility(View.VISIBLE);
        imgBnReturn.setOnClickListener(new android.view.View.OnClickListener(){
            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                TouchLetterPrefsActivity.this.finish(); 
            }
        });
    }
    // add by bruce for vivo style end
	
}

