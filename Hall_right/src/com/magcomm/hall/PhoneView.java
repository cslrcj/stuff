package com.magcomm.hall;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageButton;
public class PhoneView extends ImageButton {
	private Slidelistener slidelistener;
	public PhoneView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}
	public void setOnSlide(Slidelistener slidelistener){
		this.slidelistener=slidelistener;
	}
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		int action=event.getAction();
		float downY = 0;
		float upY = 0;
		if(action==MotionEvent.ACTION_DOWN){
			downY=event.getY();
		}else if(action==MotionEvent.ACTION_UP){
			upY=event.getY();
			if((upY-downY)<-50){
				slidelistener.SlideUp();
			}else if((upY-downY)>50){
				slidelistener.SlideDown();
			}else {
				slidelistener.SlideMiddle();
			}
		}
		return super.onTouchEvent(event);
	}
}
