package com.magcomm.touch;

import java.util.List;
import java.util.regex.Pattern;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.Intent;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.util.AttributeSet;
import android.widget.EditText;

import android.util.Log;
public class NewListPreference extends ListPreference {

	private CharSequence[] entries;
	private CharSequence[] entryValues;
	private int selectedId;
	private Context cxt;
	private int indexOfValue;

	/**
	 * @param context
	 * @param attrs
	 */
	public NewListPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		cxt = context;
		//setDialogLayoutResource(R.layout.setting);
	}

	@Override
	protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {

		// TODO Auto-generated method stub
		super.onSetInitialValue(restoreValue, defaultValue);
		entries = getEntries();
		entryValues = getEntryValues();

		String value = getValue();// 这个可以删除，只是用于debug
        indexOfValue = this.findIndexOfValue(getSharedPreferences().getString(this.getKey(), ""));
		if (indexOfValue >= 0) {
			String key = String.valueOf(entries[indexOfValue]);
			if (null != key) {
				setSummary(key);
			}
        }else{
            final PackageManager pm = cxt.getPackageManager();
            List<PackageInfo> packs = pm.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);
            for (PackageInfo pi : packs) {
                Intent i = pm.getLaunchIntentForPackage(pi.packageName); 
                if ((i != null) && pi.packageName.equals(getSharedPreferences().getString(this.getKey(), ""))){
                    setSummary(pi.applicationInfo.loadLabel(pm));
                    break;
                }
            }
        }
    }

    private void updateSummary(){
        entries = getEntries();
		entryValues = getEntryValues();

		String value = getValue();
        indexOfValue = this.findIndexOfValue(getSharedPreferences().getString(this.getKey(), ""));
        Log.e("bruce_nan","NewListPreference_updateSummary_nfl: indexOfValue =" + indexOfValue);
		if (indexOfValue >= 0) {
			String key = String.valueOf(entries[indexOfValue]);
			if (null != key) {
				setSummary(key);
			}
        }else{
            Log.e("bruce_nan","NewListPreference_updateSummary_nfl_02");
            final PackageManager pm = cxt.getPackageManager();
            List<PackageInfo> packs = pm.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);
            for (PackageInfo pi : packs) {
                Intent i = pm.getLaunchIntentForPackage(pi.packageName); 
                if ((i != null) && pi.packageName.equals(getSharedPreferences().getString(this.getKey(), ""))){
                    setSummary(pi.applicationInfo.loadLabel(pm));
                    break;
                }
            }
        }
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        updateSummary();
    }

    static int cur_item;

    @Override
    protected void onPrepareDialogBuilder(Builder builder) {
	    // added by bruce begin 
        entries = getEntries();
		entryValues = getEntryValues();
		// added by bruce end
		// super.onPrepareDialogBuilder(builder);//不能调用父类的这个方法，否则点击列表项会关闭对话框
        final int more_Item = getValueIndex(cxt.getResources().getString(R.string.more_index));
		// added by bruce
        indexOfValue = this.findIndexOfValue(getSharedPreferences().getString(this.getKey(), ""));
	    Log.i("bruce_nan", "==== onPrepareDialogBuilder_nfl: indexOfValue = " + indexOfValue);
		builder.setSingleChoiceItems(entries, indexOfValue,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						//dialog.dismiss();
						cur_item = which;
						if (more_Item == cur_item) {

							Intent mIntent = new Intent();
							// wealthIntent.setClass(context,
							// WealthCalendarActivity.class);
							mIntent.setClass(cxt, selectAllApp.class);
							mIntent.putExtra("letter",
									NewListPreference.this.getKey());
							cxt.startActivity(mIntent);
                            dialog.dismiss();  
						} 
						
					}
				});
		builder.setPositiveButton(cxt.getResources().getString(R.string.touchletter_ok),new OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				if (more_Item == cur_item){
				
				}else {
				    setValue(getEntryValues()[cur_item].toString());
                    indexOfValue = NewListPreference.this.findIndexOfValue(getSharedPreferences().getString(
						NewListPreference.this.getKey(), ""));				
                }        
				dialog.dismiss();
			}
		});
		builder.setNegativeButton(cxt.getResources().getString(R.string.touchletter_cancel),new OnClickListener(){

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.dismiss();
			}
			
		}); 
	
	}

	

	private int getValueIndex(String value) {
		int len = getEntryValues().length;
		for (int i = 0; i < len; i++) {
			if (value.equals(getEntryValues()[i])) {
				return i;
			}
		}
		return 0;// 选中“自定义” 一项
	}
}