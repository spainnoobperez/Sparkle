package com.sion.sparkle;

// Basic
import androidx.annotation.Keep;
import android.os.Build;
import android.content.Intent;
import android.content.Context;
import android.util.Log;

import android.view.SurfaceView;

import android.view.SurfaceHolder;
import android.view.Surface;

import android.view.WindowManager;
import android.view.Gravity;

// Input
import android.view.MotionEvent;
import android.view.KeyEvent;
import android.view.InputDevice; // Check input source
import android.view.inputmethod.InputMethodManager; // Software keyboard

// Broadcast receiver
import android.content.BroadcastReceiver;
import android.content.IntentFilter;


// Orientation control
import android.util.DisplayMetrics;
import android.content.pm.ActivityInfo;

//import android.graphics.PixelFormat;


public class SparkleView extends SurfaceView implements SurfaceHolder.Callback
{
    @Keep
    SparkleView(SparkleService sparkle)
    {
        super(sparkle);

        sparkle_ = sparkle;

        int windowType;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            windowType = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        else
            windowType = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT; // WindowManager.LayoutParams.TYPE_PHONE;

        // WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        int flags = 0;
        flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL; // allow any pointer events outside of the window to be sent to the windows behind it
        flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
        flags |= WindowManager.LayoutParams.FLAG_DIM_BEHIND;


        params_ = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT, 0, 0,
                windowType,
                flags,
                0);
        params_.gravity = Gravity.CENTER;
        params_.setTitle("Sparkle");


        setZOrderOnTop(true);
        setFocusableInTouchMode(true);
        getHolder().addCallback(this);
        //getHolder().setFormat(PixelFormat.TRANSLUCENT);
        //getHolder().setFormat(PixelFormat.OPAQUE);



        params_.x = 0;
        params_.y = 0;
        params_.width = 100;
        params_.height = 100;
        //params.alpha = 0.5F;
        //params.dimAmount = 1.0F;
        //params.format = PixelFormat.TRANSLUCENT;
        //params.format = PixelFormat.OPAQUE;


        setVisibility(VISIBLE);



        DisplayMetrics display_metrics = new DisplayMetrics();
        sparkle_.window_manager_.getDefaultDisplay().getMetrics(display_metrics);
        if (display_metrics.widthPixels > display_metrics.heightPixels)
            params_.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;
        else
            params_.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT;


        rmb_ = false;
        rmb_down_ = false;


        receiver_ = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                String action = intent.getAction();

                if (action.equals(sparkle_.ACTION_HIDE) || action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS))
                {
                    set_visible(false);
                }
                else if (action.equals(sparkle_.ACTION_SHOW))
                {
                    set_visible(true);
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(sparkle_.ACTION_HIDE);
        filter.addAction(sparkle_.ACTION_SHOW);
        filter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);

        sparkle_.window_manager_.addView(this, params_);
        sparkle_.registerReceiver(receiver_, filter);
    }

    @Keep
    public void set_native(long native__)
    {
        native_ = native__;
    }

    @Keep
    public void collapse()
    {
        sparkle_.unregisterReceiver(receiver_);
        sparkle_.window_manager_.removeView(this);
    }

    @Keep
    public void set_visible(boolean visible)
    {
        if (visible)
            setVisibility(VISIBLE);
        else
            setVisibility(INVISIBLE);
    }

    @Keep
    public void set_position(int x, int y)
    {
        params_.x = x;
        params_.y = y;
        sparkle_.window_manager_.updateViewLayout(this, params_);
    }

    @Keep
    public void set_size(int width, int height)
    {
        params_.width = width;
        params_.height = height;
        sparkle_.window_manager_.updateViewLayout(this, params_);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
    {
        if (native_ == 0) {return;}
        surface_changed(native_, holder.getSurface());
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder)
    {
        if (native_ == 0) {return;}
        surface_changed(native_, holder.getSurface());
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder)
    {
        if (native_ == 0) {return;}
        surface_changed(native_, null);
    }

    private static boolean hasSource(int sources, int source)
    {
        return (sources & source) == source;
    }

    public boolean onMotionEvent(MotionEvent event)
    {
        if (native_ == 0) {return false;}

        int source = event.getSource();

        if (hasSource(source, InputDevice.SOURCE_TOUCHSCREEN))
        {
            switch (event.getAction())
            {
                case MotionEvent.ACTION_DOWN:
                    if (rmb_)
                    {
                        pointer_motion(native_, event.getX(), event.getY());
                        pointer_button_down(native_, 2);
                        rmb_down_ = true;
                    }
                    else
                    {
                        touch_down(native_, 0, event.getX(), event.getY());
                    }
                    return true;
                case MotionEvent.ACTION_UP:
                    if (rmb_down_)
                    {
                        pointer_button_up(native_, 2);
                        rmb_down_ = false;
                    }
                    else
                    {
                        touch_up(native_, 0, event.getX(), event.getY());
                    }
                    return true;
                case MotionEvent.ACTION_HOVER_MOVE:
                    pointer_motion(native_, event.getX(), event.getY());
                    return true;
                case MotionEvent.ACTION_MOVE:
                    touch_motion(native_, 0, event.getX(), event.getY());
                    return true;
            }
        }
        else if (hasSource(source, InputDevice.SOURCE_STYLUS))
        {
            switch (event.getAction())
            {
                case MotionEvent.ACTION_DOWN:
                    if (rmb_ || (event.getButtonState() & MotionEvent.BUTTON_STYLUS_PRIMARY) == MotionEvent.BUTTON_STYLUS_PRIMARY)
                    {
                        pointer_motion(native_, event.getX(), event.getY());
                        pointer_button_down(native_, 2);
                        rmb_down_ = true;
                    }
                    else
                    {
                        pointer_motion(native_, event.getX(), event.getY());
                        pointer_button_down(native_, 1);
                    }
                    return true;
                case MotionEvent.ACTION_UP:
                    if (rmb_down_)
                    {
                        pointer_button_up(native_, 2);
                        rmb_down_ = false;
                    }
                    else
                    {
                        pointer_button_up(native_, 1);
                    }
                    return true;
                case MotionEvent.ACTION_HOVER_MOVE:
                    pointer_motion(native_, event.getX(), event.getY());
                    return true;
                case MotionEvent.ACTION_MOVE:
                    pointer_motion(native_, event.getX(), event.getY());
                    return true;
            }
        }
        else if (hasSource(source, InputDevice.SOURCE_MOUSE))
        {
            switch (event.getAction())
            {
                case MotionEvent.ACTION_BUTTON_PRESS:
                    pointer_button_down(native_, event.getActionButton());
                    return true;
                case MotionEvent.ACTION_BUTTON_RELEASE:
                    pointer_button_up(native_, event.getActionButton());
                    return true;
                case MotionEvent.ACTION_HOVER_MOVE:
                    pointer_motion(native_, event.getX(), event.getY());
                    return true;
                case MotionEvent.ACTION_MOVE:
                    pointer_motion(native_, event.getX(), event.getY());
                    return true;
                case MotionEvent.ACTION_SCROLL:
                    if (event.getAxisValue(MotionEvent.AXIS_VSCROLL) > 0.0f)
                    {
                        pointer_button_down(native_, 5);
                        pointer_button_up(native_, 5);
                    }
                    else
                    {
                        pointer_button_down(native_, 6);
                        pointer_button_up(native_, 6);
                    }
                    return true;
            }
        }

        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        if (onMotionEvent(event))
            return true;
        else
            return super.onTouchEvent(event);
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event)
    {
        if (onMotionEvent(event))
            return true;
        else
            return super.onGenericMotionEvent(event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (native_ == 0) {return false;}

        if (keyCode == KeyEvent.KEYCODE_BACK)
        {
            int source = event.getSource();
            if (hasSource(source, InputDevice.SOURCE_MOUSE))
            {
                pointer_button_down(native_, 2);
                return true;
            }

            set_visible(false);
        }
        else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP)
        {
            InputMethodManager imm = (InputMethodManager)sparkle_.getSystemService(sparkle_.INPUT_METHOD_SERVICE);
            //imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT);
            imm.toggleSoftInput(0, 0);
        }
        else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)
        {
            rmb_ = true;
        }

        key_down(native_, keyCode);

        return true;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event)
    {
        if (native_ == 0) {return false;}

        if (keyCode == KeyEvent.KEYCODE_BACK)
        {
            int source = event.getSource();
            if (hasSource(source, InputDevice.SOURCE_MOUSE))
            {
                pointer_button_up(native_, 2);
                return true;
            }
        }
        else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP)
        {
        }
        else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)
        {
            rmb_ = false;
        }

        key_up(native_, keyCode);

        return true;
    }


    public native void surface_changed(long user, Surface surface);

    public native void key_down(long user, int code);
    public native void key_up(long user, int code);

    public native void pointer_button_down(long user, int button);
    public native void pointer_button_up(long user, int button);
    public native void pointer_motion(long user, float x, float y);
    public native void pointer_enter(long user);
    public native void pointer_leave(long user);

    public native void touch_down(long user, int id, float x, float y);
    public native void touch_up(long user, int id, float x, float y);
    public native void touch_motion(long user, int id, float x, float y);


    private SparkleService sparkle_;
    private long native_ = 0;
    private WindowManager.LayoutParams params_;
    private boolean rmb_;
    private boolean rmb_down_;
    private BroadcastReceiver receiver_;
}
