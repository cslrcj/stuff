package com.magcomm.touch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SimpleAdapter.ViewBinder;

// add by bruce for vivo style
import android.widget.ImageButton;
import android.widget.TextView;

public class selectAllApp extends Activity {
	ListView lv;  
	SimpleAdapter adapter;  
    ArrayList<HashMap<String, Object>> items1 = new ArrayList<HashMap<String, Object>>();  

    // add by bruce for vivo style
    private TextView tvTitle = null;
    private ImageButton imgBnReturn;
    
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		initVivo(); // added by bruce for vivo UI
		//setContentView(R.layout.select_all_activity);
		//lv = (ListView) findViewById(R.id.listView1);  
        final PackageManager pm = getPackageManager();  
        // 得到PackageManager对象  
        List<PackageInfo> packs = pm  
                .getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);  
        // 得到系统 安装的所有程序包的PackageInfo对象  
   
        for (PackageInfo pi : packs) {  
            HashMap<String, Object> map = new HashMap<String, Object>();  
            //取到点击的包名  
            Intent i = pm.getLaunchIntentForPackage(pi.packageName); 
            
            if (i != null)  
            {
                if (!pi.applicationInfo.loadLabel(pm).equals("蓝牙")
                       && !pi.applicationInfo.loadLabel(pm).equals("SIM Toolkit")
                       && !pi.applicationInfo.loadLabel(pm).equals("SIM卡应用")
                       && !pi.applicationInfo.loadLabel(pm).equals("Bluetooth")
                       && !pi.applicationInfo.loadLabel(pm).equals("uni_launcher")
                       && !pi.applicationInfo.loadLabel(pm).equals("UNI Launcher")
                       && !pi.applicationInfo.loadLabel(pm).equals("R")
                       && !pi.applicationInfo.loadLabel(pm).equals("百度输入法")
                       && !pi.applicationInfo.loadLabel(pm).equals("Baidu Input")
                       && !pi.applicationInfo.loadLabel(pm).equals("Dev Tools")
                    ) {
                    map.put("icon", pi.applicationInfo.loadIcon(pm));  
                    // 图标  
                    map.put("appName", pi.applicationInfo.loadLabel(pm));  
                    // 应用名  
                    map.put("packageName", pi.packageName);  
                    // 包名  
                    items1.add(map);  
                }      
            }
            // 循环读取存到HashMap,再增加到ArrayList.一个HashMap就是一项  
        }  
   
        adapter = new SimpleAdapter(this, items1, R.layout.select_all_item, new String[] {  
                "icon", "appName", "packageName"}, new int[] { R.id.icon,  
                R.id.appName, R.id.packageName});  
        adapter.setViewBinder(new ViewBinder() {    
            public boolean setViewValue(  
                                View view,   
                                Object data,    
                             String textRepresentation) {    
                //判断是否为我们要处理的对象    
                if(view instanceof ImageView  && data instanceof Drawable){    
                    ImageView iv = (ImageView) view;    
                    iv.setImageDrawable((Drawable)data);
                    return true;    
                }else    
                return false;    
            }    
        });    
        //ImageView iv = (ImageView)findViewById(R.id.icon);
       
        // 参数:Context,ArrayList(item的集合),item的layout,包含ArrayList中Hashmap的key的数组,key所对应的值相对应的控件id  
        lv.setAdapter(adapter);  
        


        lv.setOnItemClickListener(new OnItemClickListener() {  
   
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // TODO Auto-generated method stub
                String packageName = (String) items1.get(position).get("packageName");
                String appName = (String) items1.get(position).get("appName");
                // 取到点击的包名
                Intent i = pm.getLaunchIntentForPackage(packageName);

                // 如果该程序不可启动（像系统自带的包，有很多是没有入口的）会返回NULL
                if (i != null) {
                    Intent intent = selectAllApp.this.getIntent();// 得到用于激活它的意图
                    Bundle bundle = intent.getExtras();// .getExtras()得到intent所附带的额外数据
                    if (bundle != null) {
                        final String str = bundle.getString("letter", "a");
                        Log.i("bruce_nan", "str = " + str);
                        if (!str.equals("a")) {
                            SharedPreferences mySharedPreferences = PreferenceManager.getDefaultSharedPreferences(selectAllApp.this);
                            SharedPreferences.Editor editor = mySharedPreferences.edit();
                            editor.putString(str, packageName);
                            editor.commit();
                            selectAllApp.this.finish();
                        }
                    }
                }
            }
        });
	}
	
	public class MyView extends View {
		Context mContext = null;

		Bitmap tmpBitmap = null;
		int w, h;
	

		public MyView(Context context, AttributeSet attrs, int defStyle) {
			super(context, attrs, defStyle);
			// TODO Auto-generated constructor stub
			mContext = context;

			WindowManager wManager = (WindowManager) mContext
					.getSystemService(Context.WINDOW_SERVICE);
			Display display = wManager.getDefaultDisplay();
			DisplayMetrics outMetrics = new DisplayMetrics();
			display.getMetrics(outMetrics);
			w = outMetrics.widthPixels;
			h = outMetrics.heightPixels;
			
		}

		public MyView(Context context, AttributeSet attrs) {
			super(context, attrs);
			// TODO Auto-generated constructor stub
			mContext = context;

			WindowManager wManager = (WindowManager) mContext
					.getSystemService(Context.WINDOW_SERVICE);
			Display display = wManager.getDefaultDisplay();
			DisplayMetrics outMetrics = new DisplayMetrics();
			display.getMetrics(outMetrics);
			w = outMetrics.widthPixels;
			h = outMetrics.heightPixels;

		}

		public MyView(Context context) {
			super(context);
			// TODO Auto-generated constructor stub
			mContext = context;

			WindowManager wManager = (WindowManager) mContext
					.getSystemService(Context.WINDOW_SERVICE);
			Display display = wManager.getDefaultDisplay();
			DisplayMetrics outMetrics = new DisplayMetrics();
			display.getMetrics(outMetrics);
			w = outMetrics.widthPixels;
			h = outMetrics.heightPixels;

			

		}

		@Override
		protected void onLayout(boolean changed, int l, int t, int r, int b) {
			// TODO Auto-generated method stub
			super.onLayout(changed, l, t, r, b);

		}

		@Override
		protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
			// TODO Auto-generated method stub
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		}

		@Override
		protected void onDraw(Canvas canvas) {
			// TODO Auto-generated method stub5
			super.onDraw(canvas);
			//canvas.drawBitmap(bitmap, matrix, paint)
		}
	}

    // add by bruce for vivo style begin
    private void initVivo(){
        setContentView(R.layout.select_all_activity);
        tvTitle = (TextView)findViewById(R.id.titleBg);
        tvTitle.setText(R.string.more);
        tvTitle.setOnClickListener(new android.view.View.OnClickListener(){
            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                selectAllApp.this.finish(); 
            }
        });

        imgBnReturn = (ImageButton)findViewById(R.id.imgLeft);
        imgBnReturn.setVisibility(View.VISIBLE);
        imgBnReturn.setOnClickListener(new android.view.View.OnClickListener(){
            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                selectAllApp.this.finish(); 
            }
        });

        lv = (ListView) findViewById(R.id.listView1);  
    }
    // add by bruce for vivo style end

}
