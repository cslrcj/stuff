package com.magcomm.locker.ui;

import java.util.ArrayList;
import java.util.List;

import com.magcomm.locker.lock.AppLockService;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.provider.Settings;	//For iris control Yar add

import com.magcomm.applocker.R;
import android.util.Log;	//For iris control Yar add

public class NavigationAdapter extends BaseAdapter {

    private final LayoutInflater mInflater;
	private final List<NavigationElement> mItems;

	private boolean mServiceRunning = false;
	// For iris control Yar add start
	private boolean mIrisRunning = false;
	private Context mContext;
	// For iris control Yar add end

	public NavigationAdapter(Context context) {
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mServiceRunning = AppLockService.isRunning(context);
		mItems = new ArrayList<>();
		setupElements();
		// For iris control Yar add start
		mContext = context;
		mIrisRunning = Settings.System.getInt(mContext.getContentResolver(), "iris_state", 0) == 1;
		// For iris control Yar add end
	}

	public NavigationElement getItemFor(int type) {
		return mItems.get(getPositionFor(type));
	}

	public int getPositionFor(int type) {
		for (int i = 0; i < mItems.size(); i++) {
			if (mItems.get(i).type == type) {
				return i;
			}
		}
		return -1;
	}

	public int getTypeOf(int position) {
		return mItems.get(position).type;
	}

	public void setServiceState(boolean newState) {
		if (mServiceRunning != newState) {
			mServiceRunning = newState;
			notifyDataSetChanged();
		}
	}
	
	//For iris control Yar add start
	public void setIrisState(boolean newState) {
		if (mIrisRunning != newState) {
			mIrisRunning = newState;
			Settings.System.putInt(mContext.getContentResolver(), "iris_state", newState ? 1 : 0);
			notifyDataSetChanged();
		}
	}
	
	public boolean getIrisState() {
		return mIrisRunning;
	}
	//For iris control Yar add end

	private void addElement(int title, int type) {
		final NavigationElement el = new NavigationElement();
		el.title = title;
		el.type = type;
		mItems.add(el);
	}

	private void setupElements() {
		addElement(R.string.nav_status, NavigationElement.TYPE_STATUS);
		//For iris control Yar add start
		addElement(R.string.nav_iris, NavigationElement.TYPE_IRIS);
		//For iris control Yar add end
		addElement(R.string.nav_apps, NavigationElement.TYPE_APPS);
		addElement(R.string.nav_change, NavigationElement.TYPE_CHANGE);
		addElement(R.string.nav_settings, NavigationElement.TYPE_SETTINGS);
	}

	@Override
	public int getCount() {
		return mItems.size();
	}

	@Override
	public Object getItem(int position) {
		return mItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewGroup root = (ViewGroup) mInflater.inflate(
				R.layout.navigation_drawer_list_item, null);

		if (mItems.get(position).type == NavigationElement.TYPE_STATUS) {
			final CompoundButton cb = (CompoundButton) root.findViewById(R.id.navFlag);
			cb.setChecked(mServiceRunning);
			cb.setVisibility(View.VISIBLE);
		//For iris control Yar add start
		} else if (mItems.get(position).type == NavigationElement.TYPE_IRIS) {
			final CompoundButton cb = (CompoundButton) root.findViewById(R.id.navFlag);
			cb.setChecked(mIrisRunning);
			cb.setVisibility(View.VISIBLE);			
		}
		//For iris control Yar add end

		TextView navTitle = (TextView) root.findViewById(R.id.navTitle);
		navTitle.setText(mItems.get(position).title);
		return root;
	}

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	private static CompoundButton getSwitchCompat(Context c) {
			return new Switch(c);
	}
}
