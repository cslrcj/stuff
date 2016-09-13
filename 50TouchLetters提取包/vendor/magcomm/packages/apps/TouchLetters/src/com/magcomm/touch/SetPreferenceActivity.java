package com.magcomm.touch;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

@SuppressLint("NewApi")
public class SetPreferenceActivity extends Activity {
    // added by bruce for adjust touchletter begin
	private static final int ITEM_TOUCH_LETTER = 0;
	private static final int ITEM_AIR_SHUFFLE = 1;
	private static final int ITEM_SCREEN_GESTURE = 2;
	// added by bruce for adjust touchletter end

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

        // added by bruce for adjust touchletter begin
        Bundle bundle = this.getIntent().getExtras();
        int item_value = -1;
        item_value = bundle.getInt("item");
        Log.i("bruce_nan", "SetPreferenceActivity_nfl: item_value = " + item_value);

        switch(item_value){
            case ITEM_TOUCH_LETTER:
                getFragmentManager().beginTransaction()
				    .replace(android.R.id.content, new TouchLetterPrefsFragment()).commit();
                break;
            case ITEM_AIR_SHUFFLE:
                getFragmentManager().beginTransaction()
				    .replace(android.R.id.content, new AirShufflePrefsFragment()).commit();
                break;
            case ITEM_SCREEN_GESTURE:
                getFragmentManager().beginTransaction()
				    .replace(android.R.id.content, new ScreenonPrefsFragment()).commit();
                break;
            default:
                break;
        }
        
        /*
		getFragmentManager().beginTransaction()
				.replace(android.R.id.content, new PrefsFragment()).commit();
		*/
		// added by bruce for adjust touchletter end
	}
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		
		//getFragmentManager().beginTransaction()
		//.replace(android.R.id.content, new PrefsFragment()).commit();
	}
	
}
