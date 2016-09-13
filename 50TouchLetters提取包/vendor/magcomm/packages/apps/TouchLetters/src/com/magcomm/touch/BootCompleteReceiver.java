package com.magcomm.touch;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootCompleteReceiver extends BroadcastReceiver{
    
    @Override
    public void onReceive(Context context, Intent intent) {  
        String action = intent.getAction();
        Log.i("bruce_nan", "ScreenonPrefsFragment_onReceive_01: action = " + action);
        if (action.equals(Intent.ACTION_BOOT_COMPLETED)){
            String value = MyFile.readGloveFile(context);
            Log.i("bruce_nan", "ScreenonPrefsFragment_onReceive_02: value = " + value);
            MyFile.writeGloveFile(value);
        }
    }  
}

