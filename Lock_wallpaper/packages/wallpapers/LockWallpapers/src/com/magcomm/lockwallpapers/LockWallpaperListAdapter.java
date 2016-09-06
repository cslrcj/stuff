package com.magcomm.lockwallpapers;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class LockWallpaperListAdapter extends BaseAdapter {
	private String[] wallpapapers;
	private Context mContext;
	private LockWallpapersManager lockWallpapersManager;
	public LockWallpaperListAdapter(Context context, String[] wallpapapers) {
		mContext = context;
		this.wallpapapers = wallpapapers;
		lockWallpapersManager=LockWallpapersManager.getInstance(mContext);
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return wallpapapers.length;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return wallpapapers[position];
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder holder;
		if (convertView == null) {
			LayoutInflater inflater = LayoutInflater.from(mContext);  
			convertView = inflater.inflate(R.layout.lock_wallpapers_item, null); 
			holder = new ViewHolder();
			holder.thumbnail = (ImageView) convertView
					.findViewById(R.id.thumbnail);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		if (holder.thumbnail != null) {
            holder.thumbnail.setImageResource(lockWallpapersManager.getResID(wallpapapers[position]));
        }
		return convertView;
	}
	private class ViewHolder {
		ImageView thumbnail;
	}
}
