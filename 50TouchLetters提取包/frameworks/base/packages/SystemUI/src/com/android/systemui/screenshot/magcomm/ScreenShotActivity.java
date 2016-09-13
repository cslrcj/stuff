package com.android.systemui.screenshot.magcomm;


import com.android.systemui.screenshot.GlobalScreenshot;
import android.net.Uri;
import com.android.systemui.screenshot.magcomm.widget.ScreenShotView;
import java.io.File;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.view.View.OnClickListener;
import android.graphics.Region;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import com.android.systemui.R;
import android.graphics.drawable.Drawable;
import android.graphics.Path;

public class ScreenShotActivity extends Activity implements OnClickListener{
	private ScreenShotView screenshotView;
	private Button btnSave;
	private Button btnCancel;
	private Button btnReset;
	private RadioGroup radioGroup;
	private Button btnShare;
	private FrameLayout mFrameLayout;
	
	private int screenshot_x;
	private int screenshot_y;
	private int screenshot_m;
	private int screenshot_n;
	private int screenshot_width;
	private int screenshot_height;
	private GlobalScreenshot mGlobalScreenshot;
	private Bitmap screenshot_bitmap;
	private WindowManager mWindowManager;
	private boolean isDrag;
	private int downX;
	private int downY;
	private int down_x;
	private int down_y;
	private int down_m;
	private int down_n;
	private int screenWidth;
	private int screenHeight;
	private int beyondScale = 60;
	private boolean isScale;
	private Runnable finisher = new Runnable() {
		@Override
		public void run() {

		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_screen_shot);
		mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		screenWidth = mWindowManager.getDefaultDisplay().getWidth();
		screenHeight = mWindowManager.getDefaultDisplay().getHeight();
		screenshot_width=screenWidth;
		screenshot_height=screenHeight;
		mGlobalScreenshot = new GlobalScreenshot(this);
		mGlobalScreenshot.setAreaScreenShot(true);
		mGlobalScreenshot.takeScreenshot(finisher, false, false);
		initView();
	}

	private void initView() {
		mFrameLayout = (FrameLayout) findViewById(R.id.fl_screen);
		screenshot_bitmap = mGlobalScreenshot.getScreenBitmap();
		mFrameLayout.setBackground(new BitmapDrawable(screenshot_bitmap));
		screenshotView = (ScreenShotView) findViewById(R.id.view_screen);
		screenshotView.setOnTouchListener(new ScreenShotListener());
		screenshotView.setScreen(screenWidth, screenHeight);
		screenshotView.setReset(true);
		screenshotView.setBitmap(screenshot_bitmap);
		screenshotView.postInvalidate();
		btnSave = (Button) findViewById(R.id.btn_save);
		btnSave.setOnClickListener(this);
		btnReset = (Button) findViewById(R.id.btn_reset);
		btnReset.setOnClickListener(this);
		btnCancel = (Button) findViewById(R.id.btn_cancel);
		btnCancel.setOnClickListener(this);
		btnShare = (Button) findViewById(R.id.btn_share);
		btnShare.setOnClickListener(this);
		radioGroup=(RadioGroup)findViewById(R.id.radiogroup);
		radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				// TODO Auto-generated method stub
				switch (checkedId) {
				case R.id.rbt_free:
					screenshotView.setShape(3);
					screenshotView.postInvalidate();
					break;
				case R.id.rbt_rect:
					screenshotView.setShape(0);
					screenshotView.postInvalidate();
					break;
				case R.id.rbt_rectf:
					screenshotView.setShape(1);
					screenshotView.postInvalidate();

					break;
				case R.id.rbt_heart:
					screenshotView.setShape(2);
					screenshotView.postInvalidate();
					break;

				default:
					break;
				}
			}
		});
	}
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.btn_save:
			if(screenshot_width==screenWidth&&screenshot_height==screenHeight){
				mGlobalScreenshot.setScreenBitmap(screenshot_bitmap);
			}else{
				mGlobalScreenshot.setScreenBitmap(getClipBitmap());
			}
			mGlobalScreenshot.saveScreenshotInWorkerThread(finisher);
			finish();
			break;
		case R.id.btn_cancel:
				finish();
			break;
		case R.id.btn_share:
			senShare();
			finish();
			break;
		case R.id.btn_reset:
			 reset();
			break;

		default:
			break;
		}
	}
	private void senShare(){
		if(screenshot_width==screenWidth&&screenshot_height==screenHeight){
			mGlobalScreenshot.setScreenBitmap(screenshot_bitmap);
		}else{
			mGlobalScreenshot.setScreenBitmap(getClipBitmap());
		}
		mGlobalScreenshot.saveScreenshotInWorkerThread(finisher);
		File file =new File(mGlobalScreenshot.getImageFilePath());
		Uri uri =Uri.fromFile(file);
		Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("image/png");
        sharingIntent.putExtra(Intent.EXTRA_STREAM, uri);
        String title=getResources().getString(R.string.send_share);
        Intent chooserIntent = Intent.createChooser(sharingIntent,title);
        chooserIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
                | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(sharingIntent);
	}
	class ScreenShotListener implements OnTouchListener {

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			int getX = (int) event.getX();
			int getY = (int) event.getY();
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				if (isdrag(getX, getY)) {
					downX = getX;
					downY = getY;
					down_x = screenshot_x;
					down_y = screenshot_y;
					down_m = screenshot_m;
					down_n = screenshot_n;
					isDrag = true;
					screenshotView.setMoveDrag(getX, getY, true);
				} else if (isScale(getX, getY)) {
					if(screenshotView.getShape()==3){
						return false;
					}
					downX = getX;
					downY = getY;
					down_x = screenshot_x;
					down_y = screenshot_y;
					down_m = screenshot_m;
					down_n = screenshot_n;
					isScale = true;
				} else if((screenshot_x==0&&screenshot_y==0)||(screenshot_m==0&&screenshot_n==0)){
					screenshot_x = getX;
					screenshot_y = getY;
					screenshotView.setActionUp(false);
					screenshotView.setActionDown(true);
				}else {
					return false;
				}
			}
			else if (event.getAction() == MotionEvent.ACTION_MOVE) {
				if (isDrag) {
					dragMove(getX, getY);
				} else if (isScale) {
					scaleMove(getX, getY);
				} else {
					if(screenshotView.getShape()==3){
						setValue(getX, getY);
						screenshotView.setMoveSeat(getX, getY);
					}else{
						screenshot_m = getX;
						screenshot_n = getY;
					}
				}
				screenshotView.setSeat(screenshot_x, screenshot_y,
						screenshot_m, screenshot_n);
				screenshotView.postInvalidate();
			}
			else if (event.getAction() == MotionEvent.ACTION_UP) {
				screenshot_width = Math.abs(screenshot_m - screenshot_x);
				screenshot_height = Math.abs(screenshot_n - screenshot_y);
				int upX=Math.min(screenshot_m, screenshot_x);
				int upM=Math.max(screenshot_m, screenshot_x);
				int upY=Math.min(screenshot_n, screenshot_y);
				int upN=Math.max(screenshot_n, screenshot_y);
				screenshot_x=upX;
				screenshot_m=upM;
				screenshot_y=upY;
				screenshot_n=upN;
				screenshotView.setActionUp(true);
				if(screenshot_width<60&&screenshot_height<60){
					reset();
				}else{
					screenshotView.postInvalidate();
				}
				screenshotView.setMoveDrag(getX, getY, false);
				isDrag = false;
				isScale = false;
			}
			return true;
		}
	}
	private void dragMove(int getX,int getY){
		int xLenth=getX-downX;
		int yLenth=getY-downY;
		if((down_x+xLenth)>0&&(down_m+xLenth)<screenWidth){
			screenshot_x=down_x+xLenth;
			screenshot_m=down_m+xLenth;
		}
		if((down_y+yLenth)>0&&(down_n+yLenth)<screenHeight){
			screenshot_y=down_y+yLenth;
			screenshot_n=down_n+yLenth;
		}
		if((down_x+xLenth)>=0&&(down_m+xLenth)<=screenWidth&&(down_y+yLenth)>=0&&(down_n+yLenth)<=screenHeight){
			screenshotView.setMoveSeat(getX, getY);
		}
	}
	private boolean isdrag(int getX,int getY){
		if(getX>screenshot_x&&getX<screenshot_m&&getY>screenshot_y&&getY<screenshot_n){
			return true;
		}
		return false;
	}
	private boolean isScale(int getX,int getY){
		if(getX>(screenshot_x-beyondScale)&&getX<(screenshot_m+beyondScale)
				&&getY>(screenshot_y-beyondScale)
				&&getY<(screenshot_n+beyondScale)){
			return true;
		}
		return false;
	}
	private void scaleMove(int getX,int getY){
		int xLenth=getX-downX;
		int yLenth=getY-downY;
		if(downX<=((down_x+down_m)/2)){
			screenshot_x=down_x+xLenth;
		}else{
			screenshot_m=down_m+xLenth;
		}
		if(downY<=((down_y+down_n)/2)){
			screenshot_y=down_y+yLenth;
		}else{
			screenshot_n=down_n+yLenth;
		}
	}

	private Bitmap getClipBitmap() {
		Bitmap bitmap = Bitmap.createBitmap(screenWidth, screenHeight,
				Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		Paint paintMove = new Paint(Paint.FILTER_BITMAP_FLAG);
		paintMove.setColor(Color.WHITE);
		paintMove.setStrokeWidth(10);
		paintMove.setStyle(Style.STROKE);
		canvas.clipPath(screenshotView.getPath(), Region.Op.REPLACE);
		Bitmap mBitmap=screenshot_bitmap;
		canvas.drawBitmap(mBitmap, 0, 0, paintMove);
		Bitmap clipBitmap = Bitmap.createBitmap(bitmap, screenshot_x,
				screenshot_y, screenshot_width, screenshot_height);
		return clipBitmap;
	}

	private void setValue(int x, int y) {
		if (screenshot_x > x) {
			screenshot_x = x;
		}
		if (screenshot_y > y) {
			screenshot_y = y;
		}
		if (screenshot_m < x) {
			screenshot_m = x;
		}
		if (screenshot_n < y) {
			screenshot_n = y;
		}
	}

	private void reset() {
		screenshot_width = screenWidth;
		screenshot_height = screenHeight;
		screenshotView.setScreen(screenWidth, screenHeight);
		screenshot_x = 0;
		screenshot_y = 0;
		screenshot_m = 0;
		screenshot_n = 0;
		screenshotView.setSeat(screenshot_x, screenshot_y, screenshot_m, screenshot_n);
		screenshotView.setReset(true);
		screenshot_bitmap = mGlobalScreenshot.getScreenBitmap();
		screenshotView.setBitmap(screenshot_bitmap);
		screenshotView.postInvalidate();
	}
}
