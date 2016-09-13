package com.magcomm.touch;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.provider.Settings;
import android.preference.PreferenceCategory;

@SuppressLint("NewApi")
public class ScreenonPrefsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {
    private static final String screenon_preference = "screenon_touch";
    
    private static final String multifinger_preference = "screenon_multifinger_key";
    private static final String doubleclick_preference = "screenon_doubleclick_key";
    private static final String threefinger_preference = "screenon_threefinger_key";
    private static final String doublefinger_preference = "screenon_doublefinger_key";
    private static final String singlehandle_preference = "screenon_singlehandle_key";
    private static final String screenon_category_preference = "screenon_category_key";
    
    private static SwitchPreference mScreenonSetting;
    private static CheckBoxPreference multifinger_connectionPref;
    private static CheckBoxPreference doubleclick_connectionPref;
    private static CheckBoxPreference threefinger_connectionPref;
    private static CheckBoxPreference doublefinger_connectionPref;
    private static CheckBoxPreference singlehandle_connectionPref;
    private PreferenceCategory mScreenonCategory;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.screenon_preferences);
        
        SharedPreferences mySharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getActivity());
        mScreenonSetting = (SwitchPreference)findPreference(screenon_preference);
        mScreenonCategory = (PreferenceCategory) findPreference(screenon_category_preference);
        
        multifinger_connectionPref = (CheckBoxPreference)findPreference(multifinger_preference);
        doubleclick_connectionPref = (CheckBoxPreference)findPreference(doubleclick_preference);
        threefinger_connectionPref = (CheckBoxPreference)findPreference(threefinger_preference);
        doublefinger_connectionPref = (CheckBoxPreference)findPreference(doublefinger_preference);
        singlehandle_connectionPref = (CheckBoxPreference)findPreference(singlehandle_preference);
        mScreenonCategory.removePreference(singlehandle_connectionPref); // temp remove by bruce
        mScreenonCategory.removePreference(doublefinger_connectionPref); // temp remove by songkun
        Log.d("bruce_nan", "ScreenonPrefsFragment_onCreate_nfl");
        chooseEnable(mySharedPreferences);
    }

    private void chooseEnable(SharedPreferences mySharedPreferences){        
        boolean screenon_enable = Settings.System.getInt(this.getActivity().getContentResolver(),
                Settings.System.SCREENON_GESTURE_SETTING,0) == 1 ? true:false;
        Log.i("bruce_nan", "chooseEnable_nfl: screenon_enable = " + screenon_enable);
        //if (!mySharedPreferences.getBoolean(ps_preference, true)){
        if (!screenon_enable){
            mScreenonSetting.setChecked(false);
            mScreenonSetting.setSelectable(true);
            multifinger_connectionPref.setEnabled(false);
            doubleclick_connectionPref.setEnabled(false);
            threefinger_connectionPref.setEnabled(false);
            doublefinger_connectionPref.setEnabled(false);
            singlehandle_connectionPref.setEnabled(false);
            multifinger_connectionPref.setSelectable(false);
            doubleclick_connectionPref.setSelectable(false);
            threefinger_connectionPref.setSelectable(false);
            doublefinger_connectionPref.setSelectable(false);
            singlehandle_connectionPref.setSelectable(false);
            Settings.System.putInt(this.getActivity().getContentResolver(), Settings.System.SCREENON_GESTURE_SETTING, 0);
        }else{
            mScreenonSetting.setChecked(true);
            mScreenonSetting.setSelectable(true);
            multifinger_connectionPref.setEnabled(true);
            doubleclick_connectionPref.setEnabled(true);
            threefinger_connectionPref.setEnabled(true);
            doublefinger_connectionPref.setEnabled(true);
			singlehandle_connectionPref.setEnabled(true);
            multifinger_connectionPref.setSelectable(true);
            doubleclick_connectionPref.setSelectable(true);
            threefinger_connectionPref.setSelectable(true);
            doublefinger_connectionPref.setSelectable(true);
            singlehandle_connectionPref.setSelectable(true);
            Settings.System.putInt(this.getActivity().getContentResolver(), Settings.System.SCREENON_GESTURE_SETTING, 1);

            boolean isMultiFingerEnable = Settings.System.getInt(this.getActivity().getContentResolver(),
                Settings.System.SCREENON_MULTIFINGER_SETTING,0) == 1;
            boolean isDoubleClickEnable = Settings.System.getInt(this.getActivity().getContentResolver(),
                Settings.System.SCREENON_DOUBLECLICK_SETTING,0) == 1;
            boolean isThreeFingerEnable = Settings.System.getInt(this.getActivity().getContentResolver(),
                Settings.System.SCREENON_THREEFINGER_SETTING,0) == 1;
            boolean isDoubleFingerEnable = Settings.System.getInt(this.getActivity().getContentResolver(),
                Settings.System.SCREENON_DOUBLEFINGER_SETTING,0) == 1;
            boolean isSingleHandEnable = Settings.System.getInt(this.getActivity().getContentResolver(),
                Settings.System.SCREENON_SINGLEHAND_SETTING,0) == 1;
            multifinger_connectionPref.setChecked(isMultiFingerEnable);
            doubleclick_connectionPref.setChecked(isDoubleClickEnable);
            threefinger_connectionPref.setChecked(isThreeFingerEnable);
            doublefinger_connectionPref.setChecked(isDoubleFingerEnable);
            singlehandle_connectionPref.setChecked(isSingleHandEnable);
        }
    }
    
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // TODO Auto-generated method stub
        // Set summary to be the user-description for the selected value
        // if(!key.equals(this.getActivity().PRF_CHECK))
        Preference connectionPref = findPreference(key);
        if (key.equals(screenon_preference)) {
            if (sharedPreferences.getBoolean(key, true)) {
                connectionPref.setSummary(getResources().getString(R.string.open));
                Settings.System.putInt(this.getActivity().getContentResolver(), 
                    Settings.System.SCREENON_GESTURE_SETTING, 1);
                //MyFile.SwitchTouchPs(this.getActivity(), true);
                
            } else {
                connectionPref.setSummary(getResources().getString(R.string.close));
                Settings.System.putInt(this.getActivity().getContentResolver(), 
                    Settings.System.SCREENON_GESTURE_SETTING, 0);
                //MyFile.SwitchTouchPs(this.getActivity(), false);
            }
        } else if (key.equals(multifinger_preference)) {//multi finger
            if (sharedPreferences.getBoolean(key, true)){
                Settings.System.putInt(this.getActivity().getContentResolver(), 
                    Settings.System.SCREENON_MULTIFINGER_SETTING, 1);
            } else {
                Settings.System.putInt(this.getActivity().getContentResolver(), 
                    Settings.System.SCREENON_MULTIFINGER_SETTING, 0);
            }
        } else if (key.equals(doubleclick_preference)) {//double click
            if (sharedPreferences.getBoolean(key, true)){
                Settings.System.putInt(this.getActivity().getContentResolver(), 
                    Settings.System.SCREENON_DOUBLECLICK_SETTING, 1);
            } else {
                Settings.System.putInt(this.getActivity().getContentResolver(), 
                    Settings.System.SCREENON_DOUBLECLICK_SETTING, 0);
            }
        } else if (key.equals(threefinger_preference)) {//three finger
            if (sharedPreferences.getBoolean(key, true)){
                Settings.System.putInt(this.getActivity().getContentResolver(), 
                    Settings.System.SCREENON_THREEFINGER_SETTING, 1);
            } else {
                Settings.System.putInt(this.getActivity().getContentResolver(), 
                    Settings.System.SCREENON_THREEFINGER_SETTING, 0);
            }
        } else if (key.equals(doublefinger_preference)) {//double finger
            if (sharedPreferences.getBoolean(key, true)){
                Settings.System.putInt(this.getActivity().getContentResolver(), 
                    Settings.System.SCREENON_DOUBLEFINGER_SETTING, 1);
            } else {
                Settings.System.putInt(this.getActivity().getContentResolver(), 
                    Settings.System.SCREENON_DOUBLEFINGER_SETTING, 0);
            }
        } else if (key.equals(singlehandle_preference)) {//single handle
            if (sharedPreferences.getBoolean(key, true)){
                Settings.System.putInt(this.getActivity().getContentResolver(), 
                    Settings.System.SCREENON_SINGLEHAND_SETTING, 1);
            } else {
                Settings.System.putInt(this.getActivity().getContentResolver(), 
                    Settings.System.SCREENON_SINGLEHAND_SETTING, 0);
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
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	public void onPause() {
		getPreferenceManager().getSharedPreferences()
				.unregisterOnSharedPreferenceChangeListener(this);
		super.onPause();
	}
	
}


