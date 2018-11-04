package com.jsync.fileshare;

import android.Manifest;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

/**
 * Created by jaseem on 12/10/18.
 */

public class SearchSignalView extends SurfaceView implements Runnable{
    Thread mScanThread = null;
    Canvas mCanvas;
    Paint mPaint;
    int mScreenX;
    int mScreenY;
    int mLeft, mRight, mTop, mBottom;
    SurfaceHolder mSurfaceHolder;
    long mFPS;
    float angle;
    private float mXEnd, mYEnd, mYStart, mXStart;
    volatile boolean mRunning;
    private Context context;

    public SearchSignalView(Context context) {
        super(context);
        init(context);
    }

    public SearchSignalView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SearchSignalView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public SearchSignalView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    public void init(Context context){
        this.context = context;
        mSurfaceHolder = getHolder();
        mPaint = new Paint();

    }

    public void update(){
        float len = (float) (mScreenX * 0.8) / 2;

        angle = (angle + 8) % 360;

        mXEnd =(float) (Math.cos(Math.toRadians(angle)) * len) + mXStart;
        mYEnd =(float) (Math.sin(Math.toRadians(angle)) * len) + mYStart;


    }

    public void draw(){
        if (mSurfaceHolder.getSurface().isValid()){

            mXStart = mScreenX / 2;
            mYStart = mScreenY / 2;

            mCanvas = mSurfaceHolder.lockCanvas();
            Drawable d = ContextCompat.getDrawable(context, R.drawable.background_gradient);
            d.setBounds(mLeft, mTop, mRight, mBottom);
            d.draw(mCanvas);
            mPaint.setColor(Color.argb(255, 255, 255, 255));
            mPaint.setStrokeWidth(2f);
            mPaint.setStyle(Paint.Style.STROKE);
            mCanvas.drawCircle(mScreenX / 2, mScreenY / 2, (float) (mScreenX * 0.8) / 2, mPaint);

            /*mCanvas.save();
            mCanvas.rotate(angle, mScreenX / 2, mScreenY / 2);
            mCanvas.drawLine(mScreenX / 2, mScreenY / 2, mScreenX / 2 + ((float) (mScreenX * 0.8) / 2) - 4, mScreenY / 2, mPaint);
            mCanvas.restore();*/
            mCanvas.drawLine(mXStart, mYStart, mXEnd, mYEnd, mPaint);
            mSurfaceHolder.unlockCanvasAndPost(mCanvas);
        }
    }

    @Override
    public void run() {
        while (mRunning){
            long startFrameTime = System.currentTimeMillis();
            draw();
            update();
            long timeThisFrame = System.currentTimeMillis() - startFrameTime;
            if (timeThisFrame >= 1){
                mFPS = 1000/ timeThisFrame;
            }
        }
    }


    public void pause(){
        mRunning = false;
        try{
            mScanThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
            Log.i("Scan","Error: Joining Thread");
        }
        Log.i("Scan","Pause");
    }

    public void resume(){
        mRunning = true;
        mScanThread = new Thread(this);
        mScanThread.start();
        Log.i("Scan","Resume");
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mScreenX = right;
        mScreenY = bottom;
        mLeft = left;
        mTop = top;
        mRight = right;
        mBottom = bottom;
    }
}
