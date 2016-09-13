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

@SuppressLint("NewApi")
public class AirShufflePrefsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {
    private static final String ps_preference = "ps_touch";
    
    private static final String gallery_preference = "ps_gallery_key";
    private static final String launcher_preference = "ps_launcher_key";
    private static final String music_preference = "ps_music_key";
    private static final String camera_preference = "ps_camera_key";
    private static final String fm_preference = "ps_fm_key";
    
    private static SwitchPreference mPS_SETTING;
    private static CheckBoxPreference gallery_connectionPref;
    private static CheckBoxPreference luncher_connectionPref;
    private static CheckBoxPreference music_connectionPref;
    private static CheckBoxPreference camera_connectionPref;
    private static CheckBoxPreference fm_connectionPref;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.airshuffle_preferences);

        SharedPreferences mySharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getActivity());
        mPS_SETTING = (SwitchPreference)findPreference(ps_preference);
        
        gallery_connectionPref = (CheckBoxPreference)findPreference(gallery_preference);
        luncher_connectionPref = (CheckBoxPreference)findPreference(launcher_preference);
        music_connectionPref = (CheckBoxPreference)findPreference(music_preference);
        camera_connectionPref = (CheckBoxPreference)findPreference(camera_preference);
        fm_connectionPref = (CheckBoxPreference)findPreference(fm_preference);

        Log.d("bruce_nan", "AirShufflePrefsFragment_onCreate_nfl");
        chooseEnable(mySharedPreferences);
    }

    private void chooseEnable(SharedPreferences mySharedPreferences){        
        boolean touchps_enable = Settings.System.getInt(this.getActivity().getContentResolver(),
                Settings.System.TOUCHPS_ONOFF,0) == 1 ? true:false;
        Log.i("bruce_nan", "chooseEnable_nfl_03: touchps_enable = " + touchps_enable);
        //if (!mySharedPreferences.getBoolean(ps_preference, true)){
        if (!touchps_enable){
            mPS_SETTING.setChecked(false);
            mPS_SETTING.setSelectable(true);
            gallery_connectionPref.setEnabled(false);
            luncher_connectionPref.setEnabled(false);
            music_connectionPref.setEnabled(false);
            camera_connectionPref.setEnabled(false);
            fm_connectionPref.setEnabled(false);
            gallery_connectionPref.setSelectable(false);
            luncher_connectionPref.setSelectable(false);
            music_connectionPref.setSelectable(false);
            camera_connectionPref.setSelectable(false);
            fm_connectionPref.setSelectable(false);
            Settings.System.putInt(this.getActivity().getContentResolver(), Settings.System.TOUCHPS_ONOFF, 0);
        }else{
            mPS_SETTING.setChecked(true);
            mPS_SETTING.setSelectable(true);
            gallery_connectionPref.setEnabled(true);
            luncher_connectionPref.setEnabled(true);
            music_connectionPref.setEnabled(true);
            camera_connectionPref.setEnabled(true);
			fm_connectionPref.setEnabled(true);
            gallery_connectionPref.setSelectable(true);
            luncher_connectionPref.setSelectable(true);
            music_connectionPref.setSelectable(true);
            camera_connectionPref.setSelectable(true);
            fm_connectionPref.setSelectable(true);
            Settings.System.putInt(this.getActivity().getContentResolver(), Settings.System.TOUCHPS_ONOFF, 1);

            boolean isGalleryEnable = Settings.System.getInt(this.getActivity().getContentResolver(),
                Settings.System.AIR_SHUFFLE_IMAGE_SETTING,0) == 1;
            boolean isLauncherEnable = Settings.System.getInt(this.getActivity().getContentResolver(),
                Settings.System.AIR_SHUFFLE_LAUNCHER_SETTING,0) == 1;
            boolean isMusicEnable = Settings.System.getInt(this.getActivity().getContentResolver(),
                Settings.System.AIR_SHUFFLE_MUSIC_SETTING,0) == 1;
            boolean isCameraEnable = Settings.System.getInt(this.getActivity().getContentResolver(),
                Settings.System.AIR_SHUFFLE_CAMERA_SETTING,0) == 1;
            boolean isFMEnable = Settings.System.getInt(this.getActivity().getContentResolver(),
                Settings.System.AIR_SHUFFLE_FM_SETTING,0) == 1;
            gallery_connectionPref.setChecked(isGalleryEnable);
            luncher_connectionPref.setChecked(isLauncherEnable);
            music_connectionPref.setChecked(isMusicEnable);
            camera_connectionPref.setChecked(isCameraEnable);
            fm_connectionPref.setChecked(isFMEnable);
        }
    }
    
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // TODO Auto-generated method stub
        // Set summary to be the user-description for the selected value
        // if(!key.equals(this.getActivity().PRF_CHECK))
        Preference connectionPref = findPreference(key);
        if (key.equals(ps_preference)) {
            if (sharedPreferences.getBoolean(key, true)) {
                connectionPref.setSummary(getResources().getString(R.string.open));
                Settings.System.putInt(this.getActivity().getContentResolver(), Settings.System.TOUCHPS_ONOFF, 1);
                MyFile.SwitchTouchPs(this.getActivity(), true);
                
            } else {
                connectionPref.setSummary(getResources().getString(R.string.close));
                Settings.System.putInt(this.getActivity().getContentResolver(), Settings.System.TOUCHPS_ONOFF, 0);
                MyFile.SwitchTouchPs(this.getActivity(), false);
            }
        } else if (key.equals(launcher_preference)) {//launcher
            if (sharedPreferences.getBoolean(key, true)){
                Settings.System.putInt(this.getActivity().getContentResolver(), Settings.System.AIR_SHUFFLE_LAUNCHER_SETTING, 1);
            } else {
                Settings.System.putInt(this.getActivity().getContentResolver(), Settings.System.AIR_SHUFFLE_LAUNCHER_SETTING, 0);
            }
        } else if (key.equals(gallery_preference)) {//gallery
            if (sharedPreferences.getBoolean(key, true)){
                Settings.System.putInt(this.getActivity().getContentResolver(), Settings.System.AIR_SHUFFLE_IMAGE_SETTING, 1);
            } else {
                Settings.System.putInt(this.getActivity().getContentResolver(), Settings.System.AIR_SHUFFLE_IMAGE_SETTING, 0);
            }
        } else if (key.equals(music_preference)) {//music
            if (sharedPreferences.getBoolean(key, true)){
                Settings.System.putInt(this.getActivity().getContentResolver(), Settings.System.AIR_SHUFFLE_MUSIC_SETTING, 1);
            } else {
                Settings.System.putInt(this.getActivity().getContentResolver(), Settings.System.AIR_SHUFFLE_MUSIC_SETTING, 0);
            }
        } else if (key.equals(camera_preference)) {//camera
            if (sharedPreferences.getBoolean(key, true)){
                Settings.System.putInt(this.getActivity().getContentResolver(), Settings.System.AIR_SHUFFLE_CAMERA_SETTING, 1);
            } else {
                Settings.System.putInt(this.getActivity().getContentResolver(), Settings.System.AIR_SHUFFLE_CAMERA_SETTING, 0);
            }
        } else if (key.equals(fm_preference)) {//fm
            if (sharedPreferences.getBoolean(key, true)){
                Settings.System.putInt(this.getActivity().getContentResolver(), Settings.System.AIR_SHUFFLE_FM_SETTING, 1);
            } else {
                Settings.System.putInt(this.getActivity().getContentResolver(), Settings.System.AIR_SHUFFLE_FM_SETTING, 0);
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
		//View v = inflater.inflate(R.layout.setting, null);
		//return v;
	}

	@Override
	public void onPause() {
		getPreferenceManager().getSharedPreferences()
				.unregisterOnSharedPreferenceChangeListener(this);
		super.onPause();
	}
	
}

