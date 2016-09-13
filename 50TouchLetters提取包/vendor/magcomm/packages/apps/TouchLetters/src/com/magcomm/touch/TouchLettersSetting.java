package com.magcomm.touch;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.annotation.SuppressLint;

// add by bruce for vivo style
import android.widget.ImageButton;
import android.widget.TextView;

public class TouchLettersSetting extends Activity {
	private ListView mList = null;
	private static final int ITEM_TOUCH_LETTER = 0;
	//private static final int ITEM_AIR_SHUFFLE = 1;
	private static final int ITEM_SCREEN_GESTURE = 1;
	//hucheng add body_feeling_setting
	private static final int ITEM_BODY_FEELING = 2;

    // add by bruce for vivo style
    private TextView tvTitle = null;
    private ImageButton imgBnReturn;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.touchletter_main_activity);
		initVivo(); // add by bruce for vivo style

		mList = (ListView)findViewById(R.id.touchletter_list);
        /*
		String items[] = new String[] {this.getResources().getString(R.string.hx_setting),
										this.getResources().getString(R.string.ps_setting),
										this.getResources().getString(R.string.screenon_setting)};
        */
        //hucheng add body_feeling_setting
        String items[] = new String[] {this.getResources().getString(R.string.hx_setting),
										this.getResources().getString(R.string.screenon_setting),
										this.getResources().getString(R.string.body_feeling_setting)};
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items);
		mList.setAdapter(adapter);
		mList.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				Log.i("bruce_nan", "TouchLettersSetting_nfl: arg2 = " + arg2);
				switch(arg2){
					case ITEM_TOUCH_LETTER:
			            intent.setClass(TouchLettersSetting.this, TouchLetterPrefsActivity.class);
						//intent.putExtra("item", ITEM_TOUCH_LETTER);
						startActivity(intent);
						break;
                    /*
					case ITEM_AIR_SHUFFLE:
					    intent.setClass(TouchLettersSetting.this, AirShufflePrefsActivity.class);
						//intent.putExtra("item", ITEM_AIR_SHUFFLE);
						startActivity(intent);
						break;
                    */
				    case ITEM_SCREEN_GESTURE:
				        intent.setClass(TouchLettersSetting.this, ScreenonPrefsActivity.class);
						//intent.putExtra("item", ITEM_SCREEN_GESTURE);
						startActivity(intent);
				        break;
					case ITEM_BODY_FEELING:
				        intent.setClass(TouchLettersSetting.this, BodyFeelingPrefsActivity.class);
						//intent.putExtra("item", ITEM_SCREEN_GESTURE);
						startActivity(intent);
				        break;
					default:
						break;
				}
				
				
			}
			
		});
	}

	// add by bruce for vivo style begin
    private void initVivo(){
        setContentView(R.layout.touchletter_main_activity);
        tvTitle = (TextView)findViewById(R.id.titleBg);
        tvTitle.setText(R.string.app_name);
        tvTitle.setOnClickListener(new android.view.View.OnClickListener(){
            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                TouchLettersSetting.this.finish(); 
            }
        });

        imgBnReturn = (ImageButton)findViewById(R.id.imgLeft);
        imgBnReturn.setVisibility(View.VISIBLE);
        imgBnReturn.setOnClickListener(new android.view.View.OnClickListener(){
            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                TouchLettersSetting.this.finish(); 
            }
        });
    }
    // add by bruce for vivo style end
}
