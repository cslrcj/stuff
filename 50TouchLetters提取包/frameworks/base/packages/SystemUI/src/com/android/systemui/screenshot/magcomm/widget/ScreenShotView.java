package com.android.systemui.screenshot.magcomm.widget;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path.Direction;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.graphics.Region;
import android.graphics.Matrix;
public class ScreenShotView extends View {
	private int x;
	private int y;
	private int m;
	private int n;
	private int moveX;
	private int moveY;
	private int screenshot_width;
	private int screenshot_height;
	private Paint paint;
	private Paint paintMove;
	private Paint paintCircle;
	private int radius = 20;
	private boolean actionUp = false;
	private boolean actionDown=false;
	private boolean isReset=false;
	private boolean isdrag=false;
	private Bitmap mBitmap;
	private Path mPath;
	private Path mFreePath;
	private int mWidth;
	private int mHeight;
	private int shape=3;
	private final static int RECT=0;
	private final static int RECTF=1;
	private final static int HEART=2;
	private final static int FREE=3;
	private Bitmap clipBitmap;
	public ScreenShotView(Context context) {
		super(context);
		paint = new Paint(Paint.FILTER_BITMAP_FLAG);
		paintMove = new Paint(Paint.FILTER_BITMAP_FLAG);
	}

	public ScreenShotView(Context context, AttributeSet attrs) {
		// TODO Auto-generated constructor stub
		super(context, attrs);
		paint = new Paint(Paint.FILTER_BITMAP_FLAG);
		paintMove = new Paint(Paint.FILTER_BITMAP_FLAG);
		paintCircle = new Paint(Paint.FILTER_BITMAP_FLAG);
		paint.setColor(Color.BLACK);
		paint.setAlpha(200);
		paintMove.setColor(Color.GRAY);
		paintMove.setStrokeWidth(5);
		paintMove.setStyle(Style.STROKE);
		paintCircle.setColor(Color.WHITE);
		mPath=new Path();
		mFreePath=new Path();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		canvas.drawRect(new Rect(0, 0, screenshot_width, screenshot_height),
				paint);
		if(!isReset){
			if (shape != FREE) {
				canvas.drawRect(
						new Rect(Math.min(x, m), Math.min(y, n),
								Math.max(x, m), Math.max(y, n)), paintMove);
				if (actionUp) {
					drawCircle(canvas, Math.min(x, m), Math.min(y, n),
							Math.max(x, m), Math.max(y, n));
					drawMove(canvas, Math.min(x, m), Math.min(y, n),
							Math.max(x, m), Math.max(y, n));
					drawCircle(canvas, Math.min(x, m), Math.min(y, n),
							Math.max(x, m), Math.max(y, n));
				} else {
					drawMove(canvas, Math.min(x, m), Math.min(y, n),
							Math.max(x, m), Math.max(y, n));
				}
			} else {
				drawFree(canvas, x, y, moveX, moveY);
			}
		}else{
			isReset=false;
		}
		super.onDraw(canvas);
	}

	public void setScreen(int width, int height) {
		screenshot_height = height;
		screenshot_width = width;
	}
	public void setBitmap(Bitmap bitmap){
		mBitmap=bitmap;
	}
	private void drawMove(Canvas canvas, int x, int y, int m, int n) {
		switch (shape) {
		case RECT:
			clipRect(canvas, x, y, m, n);
			break;
		case RECTF:
			clipRectF(canvas, x, y, m, n);
			break;
		case HEART:
			clipHeart(canvas, x, y, m, n);
			break;

		default:
			break;
		}
	}
	public void clipRect(Canvas canvas, int x, int y, int m, int n){
		mPath.reset();
		mPath.addRect(x, y, m, n, Direction.CW);
		canvas.clipPath(mPath, Region.Op.REPLACE);
		canvas.drawBitmap(mBitmap, 0, 0, paintMove);
	}
	public void clipRectF(Canvas canvas, int x, int y, int m, int n){
		mPath.reset();
		mPath.addOval(new RectF(x, y, m, n), Direction.CW);
		canvas.clipPath(mPath, Region.Op.REPLACE);
		canvas.drawBitmap(mBitmap, 0, 0, paintMove);
	}
	public void clipHeart(Canvas canvas, int x, int y, int m, int n){
		mPath.reset();
		mPath.addOval(new RectF(x+mWidth/10, y, x+mWidth/2, y+mHeight/3), Direction.CW);
		mPath.addOval(new RectF(x+mWidth/2, y, x+mWidth*9/10, y+mHeight/3), Direction.CW);
		mPath.addOval(new RectF(x, y, x+mWidth*6/10, y+mHeight*2/3), Direction.CW);
		mPath.addOval(new RectF(x, y, x+mWidth*6/10, y+mHeight*2/3), Direction.CW);
		mPath.addOval(new RectF(x+mWidth*4/10, y, m, y+mHeight*2/3), Direction.CW);
		mPath.moveTo(x, y+mHeight/3);
		mPath.quadTo(x, y+mHeight*2/3, x+mWidth/2, n);
		mPath.moveTo(x, y+mHeight/3);
		mPath.quadTo(m, y+mHeight/3, x+mWidth/2, n);
		mPath.moveTo(m, y+mHeight/3);
		mPath.quadTo(m, y+mHeight*2/3, x+mWidth/2, n);
		mPath.moveTo(x+mWidth/2, n);
		mPath.quadTo(x, y+mHeight/3, m, y+mHeight/3);

		canvas.clipPath(mPath, Region.Op.REPLACE);
		canvas.drawBitmap(mBitmap, 0, 0, paintMove);
	}
	private int mX,mY;
	private void drawFree(Canvas canvas, int x, int y, int m, int n) {
		if(actionDown){
			mFreePath.reset();
			mFreePath.moveTo(x, y);
			actionDown=false;
			mX=x;
			mY=y;
		}else if(isdrag){
			canvas.drawRect(
					new Rect(Math.min(x, this.m), Math.min(y, this.n), Math.max(x, this.m),
							Math.max(y, this.n)), paintMove);
        	drawCircle(canvas, Math.min(x, this.m), Math.min(y, this.n),
					Math.max(x, this.m), Math.max(y, this.n));
        	mFreePath.offset((int)((moveX-mX)), (int)((moveY-mY)));
        	mX=moveX;
        	mY=moveY;
        	canvas.clipPath(mFreePath, Region.Op.REPLACE);
    		canvas.drawBitmap(mBitmap, 0, 0, paintMove);
    		drawCircle(canvas, Math.min(x, this.m), Math.min(y, this.n),
					Math.max(x, this.m), Math.max(y, this.n));
		}else{
			int dx = Math.abs(mX-m);
	    	int dy = Math.abs(mY-n);
	        if (dx >= 4 || dy >= 4)
	        {
	        	mFreePath.quadTo((mX+m)/2, (mY+n)/2, m, n);
	            mX = m;
	            mY = n;
	        }
	        if(actionUp){
	        	canvas.drawRect(
						new Rect(Math.min(x, this.m), Math.min(y, this.n), Math.max(x, this.m),
								Math.max(y, this.n)), paintMove);
	        	drawCircle(canvas, Math.min(x, this.m), Math.min(y, this.n),
						Math.max(x, this.m), Math.max(y, this.n));
	        	mFreePath.lineTo(mX, mY);
	        	mFreePath.close();
	        	canvas.clipPath(mFreePath, Region.Op.REPLACE);
	    		canvas.drawBitmap(mBitmap, 0, 0, paintMove);
	    		drawCircle(canvas, Math.min(x, this.m), Math.min(y, this.n),
						Math.max(x, this.m), Math.max(y, this.n));
	        }else{
	        	canvas.drawPath(mFreePath, paintMove);
	        }
		}
	}
	public void setMoveSeat(int x,int y){
		moveX=x;
		moveY=y;
	}
	private void drawCircle(Canvas canvas, int x, int y, int m, int n) {
		canvas.drawCircle(x, y, radius, paintCircle);
		canvas.drawCircle(x, n, radius, paintCircle);
		canvas.drawCircle(m, y, radius, paintCircle);
		canvas.drawCircle(m, n, radius, paintCircle);
	}
	
	public void setShape(int shape){
		this.shape=shape;
	}
	public int getShape(){
		return shape;
	}
	public Path getPath(){
		if(shape==3){
			return mFreePath;
		}
		return mPath;
	}
	public void setSeat(int x, int y, int m, int n) {
		this.x = x;
		this.y = y;
		this.m = m;
		this.n = n;
		mWidth=Math.abs(m - x);
		mHeight = Math.abs(n - y);
	}

	public void setActionUp(boolean actionUp) {
		this.actionUp = actionUp;
	}
	public void setActionDown(boolean actionDown) {
		this.actionDown = actionDown;
	}
	public void setReset(boolean reset){
		isReset=reset;
	}
	public void setMoveDrag(int x,int y,boolean isdrag){
		mX=x;
		mY=y;
		this.isdrag=isdrag;
	}
}
